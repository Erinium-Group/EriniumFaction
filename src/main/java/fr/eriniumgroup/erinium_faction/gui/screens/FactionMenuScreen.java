package fr.eriniumgroup.erinium_faction.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import fr.eriniumgroup.erinium_faction.core.EFC;
import fr.eriniumgroup.erinium_faction.core.faction.Faction;
import fr.eriniumgroup.erinium_faction.core.faction.FactionManager;
import fr.eriniumgroup.erinium_faction.core.faction.FactionSnapshot;
import fr.eriniumgroup.erinium_faction.core.faction.Permission;
import fr.eriniumgroup.erinium_faction.gui.menus.FactionMenu;
import fr.eriniumgroup.erinium_faction.gui.screens.components.ToastManager;
import fr.eriniumgroup.erinium_faction.gui.screens.pages.*;
import fr.eriniumgroup.erinium_faction.gui.screens.components.ImageRenderer;
import fr.eriniumgroup.erinium_faction.init.EFScreens;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    private final ToastManager toastManager = ToastManager.getInstance();

    // Taille FIXE du GUI: 400×270 (PAS DE SCALING)
    // Les slots Minecraft ne peuvent pas être scalés car ils ont des positions fixes
    private static final int BASE_W = 400;
    private static final int BASE_H = 270;

    private double scaleX = 1.0;
    private double scaleY = 1.0;
    // Récupérer les données de faction
    FactionSnapshot factionData;

    // Enum des pages (basées sur les SVG existants)
    public enum PageType {
        OVERVIEW("erinium_faction.gui.nav.overview"),
        MEMBERS("erinium_faction.gui.nav.members"),
        TERRITORY("erinium_faction.gui.nav.territory"),
        ALLIANCES("erinium_faction.gui.nav.alliances"),
        CHEST("erinium_faction.gui.nav.chest"),
        BANK("erinium_faction.gui.nav.bank"),
        LEVEL("erinium_faction.gui.nav.level"),
        QUESTS("erinium_faction.gui.nav.quests"),
        ADMINSHOP("erinium_faction.gui.nav.shop"),
        SETTINGS_FACTION("erinium_faction.gui.nav.settings"),
        SETTINGS_PERMISSIONS("erinium_faction.gui.nav.permissions");

        final String translationKey;
        PageType(String translationKey) { this.translationKey = translationKey; }

        public Component getLocalizedLabel() {
            return Component.translatable(this.translationKey);
        }
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

    // Textures
    private static final ResourceLocation MAIN_BACKGROUND = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/common/main-background.png");
    private static final ResourceLocation NAV_BUTTON_NORMAL = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/common/nav-button-normal.png");
    private static final ResourceLocation NAV_BUTTON_HOVER = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/common/nav-button-hover.png");
    private static final ResourceLocation NAV_BUTTON_SELECTED = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/common/nav-button-selected.png");
    private static final ResourceLocation PROGRESSBAR_EMPTY = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/common/progressbar-empty.png");
    private static final ResourceLocation PROGRESSBAR_FILLED = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/common/progressbar-filled-100.png");
    private static final ResourceLocation CLOSE_BUTTON_NORMAL = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/common/close-button-normal.png");
    private static final ResourceLocation CLOSE_BUTTON_HOVER = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/common/close-button-hover.png");

    public FactionMenuScreen(FactionMenu container, Inventory inventory, Component text) {
        super(container, inventory, text);
        this.world = container.world;
        this.x = container.x;
        this.y = container.y;
        this.z = container.z;
        this.entity = container.entity;
        this.imageWidth = BASE_W;
        this.imageHeight = BASE_H;

        // Initialiser les données de faction depuis le snapshot du menu
        if (container.snapshot != null) {
            FactionClientData.setFactionData(container.snapshot);
            factionData = FactionClientData.getFactionData();
        }
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

            // Faction chest slots (utiliser FACTION_CHEST_ROWS au lieu de hardcoder 3)
            double scaledChestY = baseChestY * this.scaleY;
            for (int row = 0; row < FactionMenu.FACTION_CHEST_ROWS; row++) {
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
        toastManager.render(guiGraphics, mouseX, mouseY, partialTicks);
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
        toastManager.tick();
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
        // Utiliser l'image du background principal
        ImageRenderer.renderScaledImage(g, MAIN_BACKGROUND, leftPos, topPos, imageWidth, imageHeight);
    }

    private void renderSidebar(GuiGraphics g, int mouseX, int mouseY) {
        // Note: Les backgrounds de la sidebar sont maintenant dans l'image principale

        // Logo
        int logoX = sx(47);
        int logoY = sy(28);
        g.fill(logoX - sw(10), logoY - sh(10), logoX + sw(10), logoY + sh(10), 0xCCec4899);

        String factionName = factionData != null ? factionData.displayName : "No Faction";
        String factionInitial = factionData.id.isEmpty() ? "?" : factionData.id.substring(0, 1).toUpperCase();

        g.drawCenteredString(font, factionInitial, logoX, logoY - sh(3), 0xFFffffff);
        g.drawCenteredString(font, factionName, logoX, sy(51) - 10, 0xFF00d2ff);

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

            // Utiliser les images au lieu de g.fill
            ResourceLocation buttonTexture;
            if (isSelected) {
                buttonTexture = NAV_BUTTON_SELECTED;
            } else if (isHovered) {
                buttonTexture = NAV_BUTTON_HOVER;
            } else {
                buttonTexture = NAV_BUTTON_NORMAL;
            }
            ImageRenderer.renderScaledImage(g, buttonTexture, btnX, btnY, btnW, btnH);

            int textColor = isSelected ? 0xFFffffff : (isHovered ? 0xFFe0e0ff : 0xFFb8b8d0);

            // Tronquer le texte si trop long pour le bouton
            int maxNavTextWidth = btnW - sw(10); // Marge de 5px de chaque côté
            fr.eriniumgroup.erinium_faction.gui.screens.components.TextHelper.drawScaledText(
                g, font, page.getLocalizedLabel().getString(),
                btnX + sw(5), btnY + sh(6), maxNavTextWidth, textColor, isSelected
            );
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

        // Power indicator (background déjà dans l'image principale)
        int pwX = sx(13);
        int pwY = sy(235);
        int pwW = sw(68);
        int pwCenterX = pwX + pwW / 2;

        g.drawCenteredString(font, Component.translatable("erinium_faction.gui.sidebar.power").getString(), pwCenterX, pwY + sh(5), 0xFFa0a0c0);

        // Bar avec données réelles - Utiliser les images
        int barX = sx(18);
        int barY = sy(248);
        int barW = sw(60);
        int barH = sh(4);

        double currentPower = factionData != null ? factionData.currentPower : 0;
        double maxPower = factionData != null && factionData.maxPower > 0 ? factionData.maxPower : 100;
        int powerPercent = maxPower > 0 ? (int) ((currentPower * 100) / maxPower) : 0;

        // Barre vide
        ImageRenderer.renderScaledImage(g, PROGRESSBAR_EMPTY, barX, barY, barW, barH);

        // Barre remplie (proportionnelle au pourcentage)
        if (powerPercent > 0) {
            int filledWidth = (barW * powerPercent / 100);
            // Utiliser scissor pour couper la barre remplie à la bonne longueur
            g.enableScissor(barX, barY, barX + filledWidth, barY + barH);
            ImageRenderer.renderScaledImage(g, PROGRESSBAR_FILLED, barX, barY, barW, barH);
            g.disableScissor();
        }

        String powerText = String.format("%.1f/%.1f", currentPower, maxPower);
        g.drawCenteredString(font, powerText, pwCenterX, pwY + sh(17) + 2, 0xFF00d2ff);
    }

    private void renderMainPanel(GuiGraphics g, int mouseX, int mouseY) {
        // Note: Les backgrounds du main panel sont maintenant dans l'image principale

        // Header text
        int hX = sx(95);
        int hY = sy(13);
        int hW = sw(289);

        g.drawString(font, Component.translatable("erinium_faction.gui.header.faction", currentPage.getLocalizedLabel()).getString(), hX + sw(7), hY + sh(11), 0xFFffffff, true);

        // Close button - Utiliser les images
        int closeX = sx(373);
        int closeY = sy(17);
        int closeW = sw(11);
        int closeH = sh(11);
        boolean closeHovered = mouseX >= closeX && mouseX < closeX + closeW && mouseY >= closeY && mouseY < closeY + closeH;

        ResourceLocation closeTexture = closeHovered ? CLOSE_BUTTON_HOVER : CLOSE_BUTTON_NORMAL;
        ImageRenderer.renderScaledImage(g, closeTexture, closeX, closeY, closeW, closeH);

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
            pages.put(PageType.BANK, new BankPage(font));
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
    public void removed() {
        super.removed();
        // Nettoyer les données quand le GUI est fermé
        FactionClientData.clear();
        // Nettoyer les pages pour qu'elles soient recréées à la prochaine ouverture
        pages.clear();
    }

    /**
     * Méthode appelée pour mettre à jour les données de faction
     * Appelée quand un FactionDataPacket est reçu
     */
    public void updateFactionData(FactionSnapshot snapshot) {
        FactionClientData.setFactionData(snapshot);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (toastManager.mouseClicked(mouseX, mouseY, button)) return true;
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
                    EFC.log.warn("§6GUI", "§cAccess denied to page: §e{}", targetPage.getLocalizedLabel().getString());
                    return true; // Consommer le clic sans changer de page
                }

                currentPage = targetPage;
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
                return hasPermission(Permission.ACCESS_CHEST);
            case SETTINGS_FACTION:
                // Vérifier si le joueur est le propriétaire (owner) de la faction
                return factionData.ownerUUID != null && factionData.ownerUUID.equals(entity.getUUID());
            case SETTINGS_PERMISSIONS:
                return hasPermission(Permission.MANAGE_PERMISSIONS);
            case ADMINSHOP:
                return hasPermission(Permission.MANAGE_SHOP);
            case ALLIANCES:
                return hasPermission(Permission.MANAGE_ALLIANCES);
            default:
                return true; // Pages publiques
        }
    }

    // Surcharge pour accepter l'enum Permission (recommandé)
    private boolean hasPermission(Permission permission) {
        return hasPermission(permission.getServerKey());
    }

    // Méthode originale pour accepter les strings
    private boolean hasPermission(String permission) {
        // Utiliser les données du snapshot (client-side) au lieu d'appeler le serveur
        UUID playerUUID = entity.getUUID();

        // Vérifier si le joueur est le owner (a toutes les permissions)
        if (factionData.ownerUUID != null && factionData.ownerUUID.equals(playerUUID)) {
            return true;
        }

        // Récupérer le rank du joueur depuis le snapshot
        String rankId = factionData.membersRank.get(playerUUID);
        if (rankId == null) {
            ToastManager.error(
                Component.translatable("erinium_faction.gui.toast.error.perm"),
                Component.translatable("erinium_faction.gui.toast.error.nopermission", permission)
            );
            return false;
        }

        // Chercher le rank dans les définitions
        for (FactionSnapshot.RankInfo rank : factionData.ranks) {
            if (rank.id.equals(rankId)) {
                // Vérifier si le rank a la permission
                if (rank.perms.contains(permission)) {
                    return true;
                }
                break;
            }
        }

        ToastManager.error(
            Component.translatable("erinium_faction.gui.toast.error.perm"),
            Component.translatable("erinium_faction.gui.toast.error.nopermission", permission)
        );
        return false;
    }
}
