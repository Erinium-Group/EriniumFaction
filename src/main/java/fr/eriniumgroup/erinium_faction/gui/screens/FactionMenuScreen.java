package fr.eriniumgroup.erinium_faction.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import fr.eriniumgroup.erinium_faction.gui.menus.FactionMenu;
import fr.eriniumgroup.erinium_faction.gui.screens.pages.*;
import fr.eriniumgroup.erinium_faction.init.EFScreens;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

/**
 * Faction Menu Screen - GUI principal pour les menus de faction
 * Taille fixe: 720x500 pixels (pas de scaling pour garder les slots alignés)
 *
 * Structure:
 * - Sidebar gauche (x=16-156): Logo, navigation, power indicator
 * - Panel principal (x=164-704): Header + contenu de page
 * - Chaque page a sa propre classe dans le package 'pages'
 *
 * Note: Les slots Minecraft ont des positions absolues définies côté serveur
 * et ne peuvent pas être scalés dynamiquement, d'où la taille fixe du GUI.
 */
public class FactionMenuScreen extends AbstractContainerScreen<FactionMenu> implements EFScreens.ScreenAccessor {
    private final Level world;
    private final int x, y, z;
    private final Player entity;
    private boolean menuStateUpdateActive = false;

    // Taille FIXE du GUI: 400×270 (PAS DE SCALING)
    // Les slots Minecraft ne peuvent pas être scalés car ils ont des positions fixes
    private static final int BASE_W = 400;
    private static final int BASE_H = 270;

    private double scaleX = 1.0;
    private double scaleY = 1.0;

    // Enum des pages (basées sur les SVG existants)
    public enum PageType {
        OVERVIEW("Overview"),
        MEMBERS("Members"),
        TERRITORY("Territory"),
        ALLIANCES("Alliances"),
        CHEST("Chest"),
        LEVEL("Level"),
        QUESTS("Quests"),
        ADMINSHOP("Shop"),
        SETTINGS_FACTION("Settings"),
        SETTINGS_PERMISSIONS("Permissions");

        final String label;
        PageType(String label) { this.label = label; }
    }

    private PageType currentPage = PageType.OVERVIEW;
    private final Map<PageType, FactionPage> pages = new HashMap<>();

    // Navigation scroll
    private int navScrollOffset = 0;
    private int maxNavScroll = 0;
    private boolean navScrollDragging = false;
    private int navDragStartY = 0;
    private int navScrollStartOffset = 0;

    private static final int NAV_BUTTON_HEIGHT = 17; // 32 * 0.54 = 17
    private static final int NAV_BUTTON_SPACING = 2; // 4 * 0.54 = 2
    private static final int NAV_AREA_HEIGHT = 170; // 316 * 0.54 = 170 (y=65 à y=235)

    public FactionMenuScreen(FactionMenu container, Inventory inventory, Component text) {
        super(container, inventory, text);
        this.world = container.world;
        this.x = container.x;
        this.y = container.y;
        this.z = container.z;
        this.entity = container.entity;
        this.imageWidth = BASE_W;
        this.imageHeight = BASE_H;
    }

    @Override
    public void updateMenuState(int elementType, String name, Object elementState) {
        menuStateUpdateActive = true;
        menuStateUpdateActive = false;
    }

    private void recomputeLayout() {
        int availW = this.width - 40;
        int availH = this.height - 40;

        if (availW > 0 && availH > 0) {
            double scaleByW = availW / (double) BASE_W;
            double scaleByH = availH / (double) BASE_H;
            double scale = Math.min(scaleByW, scaleByH);
            // Scale de 1.0x (400×270) à 2.0x (800×540) maximum
            scale = Math.max(1.0, Math.min(scale, 2.0));

            this.imageWidth = (int) Math.round(BASE_W * scale);
            this.imageHeight = (int) Math.round(BASE_H * scale);
            this.scaleX = scale;
            this.scaleY = scale;
        } else {
            this.imageWidth = BASE_W;
            this.imageHeight = BASE_H;
            this.scaleX = 1.0;
            this.scaleY = 1.0;
        }

        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;

        // Mettre à jour les positions des slots avec réflexion
        updateSlotPositionsWithReflection();
    }

    private void updateSlotPositionsWithReflection() {
        try {
            // Obtenir les champs x et y via réflexion
            java.lang.reflect.Field xField = net.minecraft.world.inventory.Slot.class.getDeclaredField("x");
            java.lang.reflect.Field yField = net.minecraft.world.inventory.Slot.class.getDeclaredField("y");
            xField.setAccessible(true);
            yField.setAccessible(true);

            // Si on n'est pas sur la page Chest, mettre les slots en dehors de l'écran
            if (currentPage != PageType.CHEST) {
                for (net.minecraft.world.inventory.Slot slot : this.menu.slots) {
                    xField.setInt(slot, -10000);
                    yField.setInt(slot, -10000);
                }
                return;
            }

            // Positions de base (identiques à FactionMenu)
            double baseX = 156;
            double baseChestY = 74;
            double baseInvY = 148;
            double baseHotbarY = 210;

            // Centrage : les slots sont espacés de 18px mais rendus en 16px
            double slotSpacing = 18;
            double scaledBaseX = baseX * this.scaleX;
            double scaledSpacing = slotSpacing * this.scaleX;
            int centerOffset = (int) Math.round((scaledSpacing - 16) / 2);

            int slotIndex = 0;

            // Faction chest slots (27 slots)
            double scaledChestY = baseChestY * this.scaleY;
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 9; col++) {
                    if (slotIndex < this.menu.slots.size()) {
                        net.minecraft.world.inventory.Slot slot = this.menu.slots.get(slotIndex);
                        xField.setInt(slot, (int) Math.round(scaledBaseX + col * scaledSpacing) + centerOffset);
                        yField.setInt(slot, (int) Math.round(scaledChestY + row * scaledSpacing) + centerOffset);
                    }
                    slotIndex++;
                }
            }

            // Player inventory slots (27 slots)
            double scaledInvY = baseInvY * this.scaleY;
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 9; col++) {
                    if (slotIndex < this.menu.slots.size()) {
                        net.minecraft.world.inventory.Slot slot = this.menu.slots.get(slotIndex);
                        xField.setInt(slot, (int) Math.round(scaledBaseX + col * scaledSpacing) + centerOffset);
                        yField.setInt(slot, (int) Math.round(scaledInvY + row * scaledSpacing) + centerOffset);
                    }
                    slotIndex++;
                }
            }

            // Player hotbar slots (9 slots)
            double scaledHotbarY = baseHotbarY * this.scaleY;
            for (int col = 0; col < 9; col++) {
                if (slotIndex < this.menu.slots.size()) {
                    net.minecraft.world.inventory.Slot slot = this.menu.slots.get(slotIndex);
                    xField.setInt(slot, (int) Math.round(scaledBaseX + col * scaledSpacing) + centerOffset);
                    yField.setInt(slot, (int) Math.round(scaledHotbarY) + centerOffset);
                }
                slotIndex++;
            }
        } catch (Exception e) {
            System.err.println("[FactionGUI] Failed to update slot positions with reflection: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Helpers de positionnement avec scaling
    private int sx(int base) { return this.leftPos + (int) Math.round(base * this.scaleX); }
    private int sy(int base) { return this.topPos + (int) Math.round(base * this.scaleY); }
    private int sw(int base) { return (int) Math.round(base * this.scaleX); }
    private int sh(int base) { return (int) Math.round(base * this.scaleY); }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        // Ne rendre les tooltips de slots que si on est sur la page Chest
        if (currentPage == PageType.CHEST) {
            this.renderTooltip(guiGraphics, mouseX, mouseY);
        }
    }

    @Override
    protected void renderSlot(GuiGraphics guiGraphics, net.minecraft.world.inventory.Slot slot) {
        // Ne rendre les slots que si on est sur la page Chest
        if (currentPage == PageType.CHEST) {
            super.renderSlot(guiGraphics, slot);
        }
    }

    @Override
    public net.minecraft.world.inventory.Slot getSlotUnderMouse() {
        // Ne permettre la sélection de slot que si on est sur la page Chest
        if (currentPage == PageType.CHEST) {
            return super.getSlotUnderMouse();
        }
        return null;
    }

    @Override
    public void containerTick() {
        super.containerTick();
        // Tick page for animations
        FactionPage page = pages.get(currentPage);
        if (page != null) {
            page.tick();
        }
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        renderBackground(g);
        renderSidebar(g, mouseX, mouseY);
        renderMainPanel(g, mouseX, mouseY);

        RenderSystem.disableBlend();
    }

    private void renderBackground(GuiGraphics g) {
        // Background cosmic
        g.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF0f0c29);

        // Bordure cyan (scaled for 400x270)
        int borderX = sx(4);
        int borderY = sy(4);
        int borderW = sw(392); // 400 - 8
        int borderH = sh(262); // 270 - 8

        // Glow (2px instead of 3)
        g.fill(borderX, borderY, borderX + borderW, borderY + 2, 0x8000d2ff);
        g.fill(borderX, borderY + borderH - 2, borderX + borderW, borderY + borderH, 0x8000d2ff);
        g.fill(borderX, borderY, borderX + 2, borderY + borderH, 0x8000d2ff);
        g.fill(borderX + borderW - 2, borderY, borderX + borderW, borderY + borderH, 0x8000d2ff);

        // Line
        g.fill(borderX, borderY, borderX + borderW, borderY + 1, 0xFF00d2ff);
        g.fill(borderX, borderY + borderH - 1, borderX + borderW, borderY + borderH, 0xFF00d2ff);
        g.fill(borderX, borderY, borderX + 1, borderY + borderH, 0xFF00d2ff);
        g.fill(borderX + borderW - 1, borderY, borderX + borderW, borderY + borderH, 0xFF00d2ff);
    }

    private void renderSidebar(GuiGraphics g, int mouseX, int mouseY) {
        // Sidebar (scaled for 400x270)
        int sbX = sx(9);
        int sbY = sy(9);
        int sbW = sw(77);
        int sbH = sh(252);

        g.fill(sbX, sbY, sbX + sbW, sbY + sbH, 0xF816161f);
        g.fill(sbX, sbY, sbX + sbW, sbY + 1, 0x50667eea);

        // Header
        int hX = sx(13);
        int hY = sy(13);
        int hW = sw(68);

        g.fill(hX, hY, hX + hW, hY + sh(43), 0xCC1a1a2e);
        g.fill(hX, hY, hX + hW, hY + 2, 0xFF00d2ff);

        // Logo
        int logoX = sx(47);
        int logoY = sy(28);
        g.fill(logoX - sw(10), logoY - sh(10), logoX + sw(10), logoY + sh(10), 0xCCec4899);
        g.drawCenteredString(font, "E", logoX, logoY - sh(3), 0xFFffffff);
        g.drawCenteredString(font, "{{FACTION_NAME}}", logoX, sy(51), 0xFF00d2ff);

        // Navigation area avec scroll
        int navX = sx(13);
        int navY = sy(65);
        int navW = sw(68);
        int navH = sh(NAV_AREA_HEIGHT);

        // Enable scissor pour clipping
        g.enableScissor(navX, navY, navX + navW, navY + navH);

        PageType[] navPages = PageType.values();
        for (int i = 0; i < navPages.length; i++) {
            int btnX = navX;
            int btnY = navY + (int)((i * (NAV_BUTTON_HEIGHT + NAV_BUTTON_SPACING)) * scaleY) - (int)(navScrollOffset * scaleY);
            int btnW = navW;
            int btnH = sh(NAV_BUTTON_HEIGHT);

            // Skip si hors de la zone visible
            if (btnY + btnH < navY || btnY > navY + navH) continue;

            PageType page = navPages[i];
            boolean isSelected = (currentPage == page);
            boolean isHovered = mouseX >= btnX && mouseX < btnX + btnW && mouseY >= btnY && mouseY < btnY + btnH &&
                               mouseY >= navY && mouseY < navY + navH;

            if (isSelected) {
                g.fill(btnX, btnY, btnX + btnW, btnY + btnH, 0xFF667eea);
                g.fill(btnX, btnY, btnX + sw(20), btnY + 2, 0xFF00d2ff);
            } else if (isHovered) {
                g.fill(btnX, btnY, btnX + btnW, btnY + btnH, 0x802a2a3e);
            } else {
                g.fill(btnX, btnY, btnX + btnW, btnY + btnH, 0xCC2a2a3e);
            }

            int textColor = isSelected ? 0xFFffffff : (isHovered ? 0xFFe0e0ff : 0xFFb8b8d0);
            g.drawString(font, page.label, btnX + sw(5), btnY + sh(6), textColor, isSelected);
        }

        g.disableScissor();

        // Scrollbar pour navigation
        if (maxNavScroll > 0) {
            int scrollbarX = navX + navW - sw(2);
            int scrollbarH = navH;

            g.fill(scrollbarX, navY, scrollbarX + sw(2), navY + scrollbarH, 0x802a2a3e);

            int thumbHeight = (int) Math.max(sh(10), ((double) navH / (navH + (int)(maxNavScroll * scaleY))) * scrollbarH);
            int thumbY = navY + (int) ((double) navScrollOffset / maxNavScroll * (scrollbarH - thumbHeight));

            boolean thumbHovered = mouseX >= scrollbarX && mouseX < scrollbarX + sw(2) &&
                                  mouseY >= thumbY && mouseY < thumbY + thumbHeight;

            int thumbColor = navScrollDragging ? 0xFF8b5cf6 : (thumbHovered ? 0xFF667eea : 0xFF4a4a5e);
            g.fill(scrollbarX, thumbY, scrollbarX + sw(2), thumbY + thumbHeight, thumbColor);
        }

        // Power indicator
        int pwX = sx(13);
        int pwY = sy(235);
        int pwW = sw(68);
        int pwCenterX = pwX + pwW / 2;

        g.fill(pwX, pwY, pwX + pwW, pwY + sh(26), 0xE61a1a2e);
        g.drawCenteredString(font, "POWER", pwCenterX, pwY + sh(5), 0xFFa0a0c0);

        // Bar
        int barX = sx(18);
        int barY = sy(248);
        int barW = sw(60);
        int barH = sh(4);

        g.fill(barX, barY, barX + barW, barY + barH, 0xFF2a2a3e);
        int powerPercent = 75;
        g.fill(barX, barY, barX + (barW * powerPercent / 100), barY + barH, 0xFFa855f7);

        g.drawCenteredString(font, "{{POWER_CURRENT}}/{{POWER_MAX}}", pwCenterX, pwY + sh(17), 0xFF00d2ff);
    }

    private void renderMainPanel(GuiGraphics g, int mouseX, int mouseY) {
        // Main panel
        int pX = sx(90);
        int pY = sy(9);
        int pW = sw(297);
        int pH = sh(252);

        g.fill(pX, pY, pX + pW, pY + pH, 0xF01e1e2e);

        // Header
        int hX = sx(95);
        int hY = sy(13);
        int hW = sw(289);

        g.fill(hX, hY, hX + hW, hY + sh(26), 0xCC1a1a2e);
        g.fill(hX, hY, hX + hW, hY + 2, 0xFF00d2ff);
        g.fill(hX, hY, hX + sw(22), hY + 2, 0xFF00d2ff);

        g.drawString(font, "Faction " + currentPage.label, hX + sw(7), hY + sh(11), 0xFFffffff, true);

        // Close button
        int closeX = sx(373);
        int closeY = sy(17);
        boolean closeHovered = mouseX >= closeX && mouseX < closeX + sw(11) && mouseY >= closeY && mouseY < closeY + sh(11);

        g.fill(closeX, closeY, closeX + sw(11), closeY + sh(11), closeHovered ? 0xFFff4444 : 0xCCef4444);
        g.drawCenteredString(font, "X", closeX + sw(5), closeY + sh(3), 0xFFffffff);

        // Render page content
        FactionPage page = pages.get(currentPage);
        if (page != null) {
            page.render(g, leftPos, topPos, scaleX, scaleY, mouseX, mouseY);
        }
    }

//    @Override
//    public boolean keyPressed(int key, int b, int c) {
//        if (key == 256) {
//            if (this.minecraft != null && this.minecraft.player != null) this.minecraft.player.closeContainer();
//            return true;
//        }
//        return super.keyPressed(key, b, c);
//    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
    }

    @Override
    public void init() {
        super.init();
        recomputeLayout();

        // Initialiser les pages (font est maintenant disponible)
        if (pages.isEmpty()) {
            pages.put(PageType.OVERVIEW, new OverviewPage(font));
            pages.put(PageType.MEMBERS, new MembersPage(font));
            pages.put(PageType.TERRITORY, new TerritoryPage(font));
            pages.put(PageType.ALLIANCES, new AlliancesPage(font));
            pages.put(PageType.CHEST, new ChestPage(font, this.menu));
            pages.put(PageType.LEVEL, new LevelPage(font));
            pages.put(PageType.QUESTS, new QuestsPage(font));
            pages.put(PageType.ADMINSHOP, new AdminShopPage(font));
            pages.put(PageType.SETTINGS_FACTION, new SettingsFactionPage(font));
            pages.put(PageType.SETTINGS_PERMISSIONS, new SettingsPermissionsPage(font));
        }

        // Calculer le maxNavScroll
        int totalNavHeight = PageType.values().length * (NAV_BUTTON_HEIGHT + NAV_BUTTON_SPACING);
        maxNavScroll = Math.max(0, totalNavHeight - NAV_AREA_HEIGHT);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

        // Navigation scrollbar
        int navX = sx(13);
        int navY = sy(65);
        int navW = sw(68);
        int navH = sh(NAV_AREA_HEIGHT);
        int scrollbarX = navX + navW - sw(2);

        if (maxNavScroll > 0 && mouseX >= scrollbarX && mouseX < scrollbarX + sw(2) &&
            mouseY >= navY && mouseY < navY + navH) {
            navScrollDragging = true;
            navDragStartY = (int) mouseY;
            navScrollStartOffset = navScrollOffset;
            return true;
        }

        // Navigation buttons
        PageType[] navPages = PageType.values();
        for (int i = 0; i < navPages.length; i++) {
            int btnX = navX;
            int btnY = navY + (int)((i * (NAV_BUTTON_HEIGHT + NAV_BUTTON_SPACING)) * scaleY) - (int)(navScrollOffset * scaleY);
            int btnW = navW - sw(8); // Exclure scrollbar
            int btnH = sh(NAV_BUTTON_HEIGHT);

            // Skip si hors zone visible
            if (btnY + btnH < navY || btnY > navY + navH) continue;

            if (mouseX >= btnX && mouseX < btnX + btnW && mouseY >= btnY && mouseY < btnY + btnH &&
                mouseY >= navY && mouseY < navY + navH) {

                PageType targetPage = navPages[i];

                // Vérifier si le joueur peut accéder à cette page
                if (!canAccessPage(targetPage)) {
                    // Afficher un message d'erreur ou jouer un son
                    System.out.println("Access denied to page: " + targetPage.label);
                    return true; // Consommer le clic sans changer de page
                }

                currentPage = targetPage;
                System.out.println("Page changed to: " + currentPage.label);
                // Mettre à jour les positions des slots (hors écran si pas Chest)
                updateSlotPositionsWithReflection();
                return true;
            }
        }

        // Close button
        int closeX = sx(373);
        int closeY = sy(17);
        if (mouseX >= closeX && mouseX < closeX + sw(11) && mouseY >= closeY && mouseY < closeY + sh(11)) {
            if (this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.player.closeContainer();
            }
            return true;
        }

        // Delegate to page first
        FactionPage page = pages.get(currentPage);
        if (page != null) {
            boolean handled = page.mouseClicked(mouseX, mouseY, button, leftPos, topPos, scaleX, scaleY);
            if (handled) return true;
        }

        // Let parent handle slot clicks
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && navScrollDragging) {
            navScrollDragging = false;
            return true;
        }

        // Delegate to page
        FactionPage page = pages.get(currentPage);
        if (page != null) {
            boolean handled = page.mouseReleased(mouseX, mouseY, button, leftPos, topPos, scaleX, scaleY);
            if (handled) return true;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (navScrollDragging && maxNavScroll > 0) {
            int navY = sy(65);
            int navH = sh(NAV_AREA_HEIGHT);
            int deltaY = (int) mouseY - navDragStartY;
            int thumbHeight = (int) Math.max(sh(10), ((double) navH / (navH + (int)(maxNavScroll * scaleY))) * navH);

            double scrollRatio = (double) deltaY / (navH - thumbHeight);
            navScrollOffset = (int) (navScrollStartOffset + scrollRatio * maxNavScroll);
            navScrollOffset = Math.max(0, Math.min(maxNavScroll, navScrollOffset));
            return true;
        }

        // Delegate to page
        FactionPage page = pages.get(currentPage);
        if (page != null) {
            boolean handled = page.mouseDragged(mouseX, mouseY, button, dragX, dragY, leftPos, topPos, scaleX, scaleY);
            if (handled) return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        // Scroll navigation sidebar
        int navX = sx(13);
        int navY = sy(65);
        int navW = sw(68);
        int navH = sh(NAV_AREA_HEIGHT);

        if (mouseX >= navX && mouseX < navX + navW && mouseY >= navY && mouseY < navY + navH && maxNavScroll > 0) {
            navScrollOffset -= (int) (scrollY * 20);
            navScrollOffset = Math.max(0, Math.min(maxNavScroll, navScrollOffset));
            return true;
        }

        // Delegate to page
        FactionPage page = pages.get(currentPage);
        if (page != null) {
            boolean handled = page.mouseScrolled(mouseX, mouseY, scrollX, scrollY, leftPos, topPos, scaleX, scaleY);
            if (handled) return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Delegate to page
        FactionPage page = pages.get(currentPage);
        if (page != null) {
            boolean handled = page.keyPressed(keyCode, scanCode, modifiers, leftPos, topPos, scaleX, scaleY);
            if (handled) return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        // Delegate to page
        FactionPage page = pages.get(currentPage);
        if (page != null) {
            boolean handled = page.charTyped(codePoint, modifiers, leftPos, topPos, scaleX, scaleY);
            if (handled) return true;
        }

        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public void resize(net.minecraft.client.Minecraft mc, int width, int height) {
        super.resize(mc, width, height);
        recomputeLayout();
        this.init();
    }

    private boolean canAccessPage(PageType page) {
        // Vérifier les permissions selon la page
        switch (page) {
            case CHEST:
                return hasPermission("ACCESS_CHEST");
            case SETTINGS_FACTION:
            case SETTINGS_PERMISSIONS:
                return hasPermission("MANAGE_FACTION");
            case ADMINSHOP:
                return hasPermission("USE_SHOP");
            default:
                return true; // Pages publiques
        }
    }

    private boolean hasPermission(String permission) {
        // Récupérer les permissions du joueur depuis FactionManager
        // return FactionManager.playerHasPermission(entity.getUUID(), permission);
        return true; // Pour l'instant, toutes les permissions sont accordées
    }
}
