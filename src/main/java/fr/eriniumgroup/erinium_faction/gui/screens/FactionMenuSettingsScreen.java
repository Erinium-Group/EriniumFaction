package fr.eriniumgroup.erinium_faction.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import fr.eriniumgroup.erinium_faction.common.network.packets.FactionMenuSettingsButtonMessage;
import fr.eriniumgroup.erinium_faction.gui.menus.FactionMenuSettingsMenu;
import fr.eriniumgroup.erinium_faction.init.EFScreens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

public class FactionMenuSettingsScreen extends AbstractContainerScreen<FactionMenuSettingsMenu> implements EFScreens.ScreenAccessor {
    private final Level world;
    private final int x, y, z;
    private final Player entity;
    private boolean menuStateUpdateActive = false;

    // État de la faction
    private boolean toggleOpenState = false;
    private boolean toggleModePublic = false;
    private boolean toggleSafezoneState = false;
    private String factionName = "";
    private String factionDisplayName = "";
    private String factionDescription = "";
    private List<RankData> ranks = new ArrayList<>();

    // Dimensions de base et scaling
    private static final int BASE_W = 500;
    private static final int BASE_H = 320;
    private double scaleX = 1.0, scaleY = 1.0;

    // Onglets
    private enum Tab { GENERAL, RANKS, PERMISSIONS }
    private Tab currentTab = Tab.GENERAL;

    // Widgets de saisie
    private EditBox displayNameBox;
    private EditBox descriptionBox;
    private List<EditBox> rankEditBoxes = new ArrayList<>();

    // Données de rangs
    private static class RankData {
        String id;
        String display;
        int priority;
        Set<String> perms = new HashSet<>();

        RankData(String id, String display, int priority) {
            this.id = id;
            this.display = display;
            this.priority = priority;
        }
    }

    // Toggle custom widget (switch avec point coulissant) - Version améliorée
    private static class DotToggleButton extends AbstractWidget {
        private boolean state;
        private final java.util.function.Consumer<Boolean> onToggle;
        private final int onColor = 0xFF2ECC71;   // Vert
        private final int offColor = 0xFFE74C3C;  // Rouge
        private final Component labelOn;
        private final Component labelOff;

        public DotToggleButton(int x, int y, int w, int h, boolean initial, Component labelOn, Component labelOff, java.util.function.Consumer<Boolean> onToggle) {
            super(x, y, w, h, Component.empty());
            this.state = initial;
            this.onToggle = onToggle;
            this.labelOn = labelOn;
            this.labelOff = labelOff;
            setTooltip(Tooltip.create(Component.empty()));
        }

        @Override
        protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
            int trackX0 = this.getX();
            int trackY0 = this.getY();
            int trackX1 = this.getX() + this.width;
            int trackY1 = this.getY() + this.height;

            // Fond arrondi avec dégradé
            int bg = state ? onColor : offColor;
            g.fill(trackX0, trackY0, trackX1, trackY1, bg);

            // Bordure
            g.fill(trackX0, trackY0, trackX1, trackY0 + 1, 0x88FFFFFF);
            g.fill(trackX0, trackY1 - 1, trackX1, trackY1, 0x44000000);
            g.fill(trackX0, trackY0, trackX0 + 1, trackY1, 0x66FFFFFF);
            g.fill(trackX1 - 1, trackY0, trackX1, trackY1, 0x33000000);

            // Position du dot
            int margin = 2;
            int dotSize = this.height - margin * 2;
            int dotX = state ? (trackX1 - margin - dotSize) : (trackX0 + margin);
            int dotY = trackY0 + margin;

            // Dot blanc avec effet 3D
            g.fill(dotX, dotY, dotX + dotSize, dotY + dotSize, 0xFFFFFFFF);
            g.fill(dotX, dotY, dotX + dotSize, dotY + 1, 0xAAFFFFFF);
            g.fill(dotX, dotY + dotSize - 1, dotX + dotSize, dotY + dotSize, 0x44000000);

            // Texte centré
            Component lab = state ? labelOn : labelOff;
            var font = Minecraft.getInstance().font;
            int w = font.width(lab);
            int cx = this.getX() + (this.width - w) / 2;
            int cy = this.getY() + (this.height - font.lineHeight) / 2 + 1;
            g.drawString(font, lab, cx, cy, 0xFF000000, false);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narration) {
            narration.add(NarratedElementType.TITLE, this.state ? labelOn : labelOff);
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            this.state = !this.state;
            if (onToggle != null) onToggle.accept(this.state);
        }

        public void setState(boolean value) {
            this.state = value;
        }

        public boolean getState() {
            return this.state;
        }
    }

    // Bouton d'onglet
    private class TabButton extends Button {
        private final Tab tab;

        public TabButton(int x, int y, int width, int height, Component text, Tab tab, OnPress onPress) {
            super(x, y, width, height, text, onPress, DEFAULT_NARRATION);
            this.tab = tab;
        }

        @Override
        protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
            boolean selected = currentTab == tab;
            int bg = selected ? 0xFF3498DB : (isHovered ? 0xFF2C3E50 : 0xFF34495E);

            g.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, bg);

            // Bordure
            if (selected) {
                g.fill(this.getX(), this.getY() + this.height - 2, this.getX() + this.width, this.getY() + this.height, 0xFF2ECC71);
            }

            var font = Minecraft.getInstance().font;
            int w = font.width(getMessage());
            int cx = this.getX() + (this.width - w) / 2;
            int cy = this.getY() + (this.height - font.lineHeight) / 2;
            g.drawString(font, getMessage(), cx, cy, 0xFFFFFFFF, false);
        }
    }

    // Checkbox personnalisée pour les permissions
    private static class PermissionCheckbox extends AbstractWidget {
        private boolean checked;
        private final java.util.function.Consumer<Boolean> onChange;
        private static final int BOX_SIZE = 16;

        public PermissionCheckbox(int x, int y, boolean initial, java.util.function.Consumer<Boolean> onChange) {
            super(x, y, BOX_SIZE, BOX_SIZE, Component.empty());
            this.checked = initial;
            this.onChange = onChange;
        }

        @Override
        protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
            int x = this.getX();
            int y = this.getY();

            // Fond de la checkbox
            int bgColor = checked ? 0xFF00BCD4 : 0xFF2C3E50; // Cyan si activé, sombre si désactivé
            g.fill(x, y, x + BOX_SIZE, y + BOX_SIZE, bgColor);

            // Bordure
            int borderColor = isHovered ? 0xFFFFFFFF : 0x88FFFFFF;
            g.fill(x, y, x + BOX_SIZE, y + 1, borderColor); // Top
            g.fill(x, y + BOX_SIZE - 1, x + BOX_SIZE, y + BOX_SIZE, borderColor); // Bottom
            g.fill(x, y, x + 1, y + BOX_SIZE, borderColor); // Left
            g.fill(x + BOX_SIZE - 1, y, x + BOX_SIZE, y + BOX_SIZE, borderColor); // Right

            // Checkmark si activé
            if (checked) {
                // Dessiner un checkmark (✓)
                g.fill(x + 3, y + 8, x + 6, y + 13, 0xFFFFFFFF);  // Ligne verticale droite
                g.fill(x + 6, y + 10, x + 13, y + 5, 0xFFFFFFFF); // Ligne diagonale

                // Amélioration du rendu du checkmark
                g.fill(x + 4, y + 8, x + 7, y + 12, 0xFFFFFFFF);
                g.fill(x + 7, y + 7, x + 12, y + 4, 0xFFFFFFFF);
            }
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narration) {
            narration.add(NarratedElementType.TITLE, Component.literal(checked ? "Activé" : "Désactivé"));
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            this.checked = !this.checked;
            if (onChange != null) {
                onChange.accept(this.checked);
            }
        }

        public void setChecked(boolean checked) {
            this.checked = checked;
        }

        public boolean isChecked() {
            return this.checked;
        }
    }

    public FactionMenuSettingsScreen(FactionMenuSettingsMenu container, Inventory inventory, Component text) {
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
        int availW = this.width - 20;
        int availH = this.height - 20;
        int targetW = BASE_W;
        int targetH = BASE_H;
        if (availW > 0 && availH > 0) {
            double scaleByW = availW / (double) BASE_W;
            double scaleByH = availH / (double) BASE_H;
            double scale = Math.min(scaleByW, scaleByH);
            scale = Math.max(1.0, Math.min(scale, 2.5));
            targetW = (int) Math.round(BASE_W * scale);
            targetH = (int) Math.round(BASE_H * scale);
        }
        this.imageWidth = targetW;
        this.imageHeight = targetH;
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
        this.scaleX = this.imageWidth / (double) BASE_W;
        this.scaleY = this.imageHeight / (double) BASE_H;
    }

    private int sx(int base) {
        return this.leftPos + (int) Math.round(base * this.scaleX);
    }

    private int sy(int base) {
        return this.topPos + (int) Math.round(base * this.scaleY);
    }

    private int sw(int base) {
        return (int) Math.round(base * this.scaleX);
    }

    private int sh(int base) {
        return (int) Math.round(base * this.scaleY);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Fond principal (ne pas afficher si popup ouverte)
        if (editingRank == null) {
            guiGraphics.blit(ResourceLocation.parse("erinium_faction:textures/screens/empty.png"),
                this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, BASE_W, BASE_H);
        } else {
            // Fond sombre pour popup
            guiGraphics.fill(0, 0, this.width, this.height, 0x99000000);
        }

        RenderSystem.disableBlend();
    }

    @Override
    public boolean keyPressed(int key, int b, int c) {
        // Vérifier si un EditBox a le focus
        boolean editBoxFocused = (displayNameBox != null && displayNameBox.isFocused()) ||
                                 (descriptionBox != null && descriptionBox.isFocused());

        // Si un EditBox a le focus
        if (editBoxFocused) {
            // ESC pour retirer le focus, pas pour fermer le GUI
            if (key == 256) {
                if (displayNameBox != null && displayNameBox.isFocused()) {
                    displayNameBox.setFocused(false);
                }
                if (descriptionBox != null && descriptionBox.isFocused()) {
                    descriptionBox.setFocused(false);
                }
                return true;
            }

            // Bloquer la touche E (69) et la touche d'inventaire
            if (key == 69) {
                // Laisser passer le caractère 'e' vers l'EditBox mais bloquer la fermeture
                if (displayNameBox != null && displayNameBox.isFocused()) {
                    return displayNameBox.keyPressed(key, b, c);
                }
                if (descriptionBox != null && descriptionBox.isFocused()) {
                    return descriptionBox.keyPressed(key, b, c);
                }
                return true;
            }

            // Laisser l'EditBox gérer toutes les autres touches
            if (displayNameBox != null && displayNameBox.isFocused()) {
                return displayNameBox.keyPressed(key, b, c);
            }
            if (descriptionBox != null && descriptionBox.isFocused()) {
                return descriptionBox.keyPressed(key, b, c);
            }
        }

        // ESC pour fermer (seulement si aucun EditBox n'a le focus)
        if (key == 256) {
            // Si popup ouverte, la fermer d'abord
            if (editingRank != null) {
                closePermissionsPopup();
                return true;
            }

            // Sinon fermer le GUI
            if (this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.player.closeContainer();
            }
            return true;
        }

        return super.keyPressed(key, b, c);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        // Empêcher la fermeture de l'inventaire avec E si un champ a le focus
        if (keyCode == 69) { // E key
            if ((displayNameBox != null && displayNameBox.isFocused()) ||
                (descriptionBox != null && descriptionBox.isFocused())) {
                return true; // Bloquer complètement
            }
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        // Ne pas mettre le jeu en pause quand ce GUI est ouvert
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        // Si un champ a le focus, ne pas fermer avec ESC (on gère ça manuellement)
        if ((displayNameBox != null && displayNameBox.isFocused()) ||
            (descriptionBox != null && descriptionBox.isFocused())) {
            return false;
        }
        return true;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        // Gérer la saisie de caractères dans les EditBox
        if (displayNameBox != null && displayNameBox.isFocused()) {
            return displayNameBox.charTyped(chr, modifiers);
        }
        if (descriptionBox != null && descriptionBox.isFocused()) {
            return descriptionBox.charTyped(chr, modifiers);
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
    }

    @Override
    public void init() {
        super.init();
        recomputeLayout();

        // Nettoyage
        rankEditBoxes.clear();

        // Barre d'onglets (toujours visible)
        int tabW = sw(BASE_W / 3 - 10);
        int tabH = sh(24);
        int tabY = sy(8);
        int tabStartX = sx(10);

        addRenderableWidget(new TabButton(tabStartX, tabY, tabW, tabH,
            Component.translatable("erinium_faction.settings.tab.general"), Tab.GENERAL,
            btn -> {
                if (editingRank == null) { // Ne pas changer d'onglet si popup ouverte
                    currentTab = Tab.GENERAL;
                    rebuildWidgets();
                }
            }));

        addRenderableWidget(new TabButton(tabStartX + tabW + sw(5), tabY, tabW, tabH,
            Component.translatable("erinium_faction.settings.tab.ranks"), Tab.RANKS,
            btn -> {
                if (editingRank == null) {
                    currentTab = Tab.RANKS;
                    rebuildWidgets();
                }
            }));

        addRenderableWidget(new TabButton(tabStartX + (tabW + sw(5)) * 2, tabY, tabW, tabH,
            Component.translatable("erinium_faction.settings.tab.permissions"), Tab.PERMISSIONS,
            btn -> {
                if (editingRank == null) {
                    currentTab = Tab.PERMISSIONS;
                    rebuildWidgets();
                }
            }));

        // Contenu selon l'onglet
        buildTabContent();
    }

    protected void rebuildWidgets() {
        this.clearWidgets();
        this.init();
    }

    private void buildTabContent() {
        // Si popup de permissions ouverte, l'afficher par-dessus
        if (editingRank != null) {
            buildPermissionsPopup();
            return;
        }

        switch (currentTab) {
            case GENERAL -> buildGeneralTab();
            case RANKS -> buildRanksTab();
            case PERMISSIONS -> buildPermissionsTab();
        }
    }

    private void buildGeneralTab() {
        int contentX = sx(15);
        int contentY = sy(45);
        int labelW = sw(120);
        int fieldW = sw(300);
        int fieldH = sh(20);
        int spacing = sh(30);
        int toggleW = sw(100);
        int y = contentY;

        // Display Name
        final int displayY = y;
        addRenderableOnly((g, mx, my, pt) ->
            g.drawString(this.font, Component.translatable("erinium_faction.settings.display_name"),
                contentX, displayY + (fieldH - this.font.lineHeight) / 2, 0xFFFFFF, false)
        );

        displayNameBox = new EditBox(this.font, contentX + labelW, y, fieldW, fieldH, Component.empty());
        displayNameBox.setMaxLength(32);
        displayNameBox.setValue(factionDisplayName);
        displayNameBox.setResponder(s -> {
            factionDisplayName = s;
            PacketDistributor.sendToServer(new FactionMenuSettingsButtonMessage(10, this.x, this.y, this.z, s));
        });
        addRenderableWidget(displayNameBox);
        y += spacing;

        // Description
        final int descY = y;
        addRenderableOnly((g, mx, my, pt) ->
            g.drawString(this.font, Component.translatable("erinium_faction.settings.description"),
                contentX, descY + (fieldH - this.font.lineHeight) / 2, 0xFFFFFF, false)
        );

        descriptionBox = new EditBox(this.font, contentX + labelW, y, fieldW, fieldH * 2, Component.empty());
        descriptionBox.setMaxLength(256);
        descriptionBox.setValue(factionDescription);
        descriptionBox.setResponder(s -> {
            factionDescription = s;
            PacketDistributor.sendToServer(new FactionMenuSettingsButtonMessage(11, this.x, this.y, this.z, s));
        });
        addRenderableWidget(descriptionBox);
        y += spacing + sh(20);

        // Mode Public/Invite
        final int modeY = y;
        addRenderableOnly((g, mx, my, pt) ->
            g.drawString(this.font, Component.translatable("erinium_faction.settings.mode"),
                contentX, modeY + (fieldH - this.font.lineHeight) / 2, 0xFFFFFF, false)
        );

        var tMode = new DotToggleButton(contentX + labelW, y, toggleW, fieldH, toggleModePublic,
            Component.literal("PUBLIC"), Component.literal("INVITE"),
            st -> PacketDistributor.sendToServer(new FactionMenuSettingsButtonMessage(2, this.x, this.y, this.z)));
        tMode.setTooltip(Tooltip.create(Component.translatable("erinium_faction.settings.tooltip.mode")));
        addRenderableWidget(tMode);
        y += spacing;

        // Open/Closed
        final int openY = y;
        addRenderableOnly((g, mx, my, pt) ->
            g.drawString(this.font, Component.translatable("erinium_faction.settings.open"),
                contentX, openY + (fieldH - this.font.lineHeight) / 2, 0xFFFFFF, false)
        );

        var tOpen = new DotToggleButton(contentX + labelW, y, toggleW, fieldH, toggleOpenState,
            Component.literal("OPEN"), Component.literal("CLOSED"),
            st -> PacketDistributor.sendToServer(new FactionMenuSettingsButtonMessage(0, this.x, this.y, this.z)));
        tOpen.setTooltip(Tooltip.create(Component.translatable("erinium_faction.settings.tooltip.open")));
        addRenderableWidget(tOpen);
        y += spacing;

        // Safezone (admin only)
        final int safeY = y;
        addRenderableOnly((g, mx, my, pt) ->
            g.drawString(this.font, Component.translatable("erinium_faction.settings.safezone"),
                contentX, safeY + (fieldH - this.font.lineHeight) / 2, 0xFFFFFF, false)
        );

        var tSafe = new DotToggleButton(contentX + labelW, y, toggleW, fieldH, toggleSafezoneState,
            Component.literal("ON"), Component.literal("OFF"),
            st -> PacketDistributor.sendToServer(new FactionMenuSettingsButtonMessage(3, this.x, this.y, this.z)));
        tSafe.setTooltip(Tooltip.create(Component.translatable("erinium_faction.settings.tooltip.safezone")));
        addRenderableWidget(tSafe);
    }

    private void buildRanksTab() {
        int contentX = sx(15);
        int contentY = sy(45);
        int rankH = sh(35);
        int y = contentY;

        // En-tête
        final int headerY = y;
        addRenderableOnly((g, mx, my, pt) ->
            g.drawString(this.font, Component.translatable("erinium_faction.settings.ranks.title"),
                contentX, headerY, 0xFFFFFF, true)
        );
        y += sh(20);

        // Liste des rangs
        for (int i = 0; i < ranks.size(); i++) {
            RankData rank = ranks.get(i);
            final int finalY = y;

            // Fond de rang
            addRenderableOnly((g, mx, my, pt) -> {
                g.fill(contentX, finalY, contentX + sw(460), finalY + rankH - 2, 0x44000000);
                g.fill(contentX, finalY + rankH - 2, contentX + sw(460), finalY + rankH, 0x222ECC71);
            });

            // Nom du rang (non éditable)
            addRenderableOnly((g, mx, my, pt) -> {
                g.drawString(this.font, rank.display, contentX + sw(10), finalY + sh(8), 0xFFFFFF, false);
                g.drawString(this.font, Component.literal("§7Priority: §e" + rank.priority),
                    contentX + sw(10), finalY + sh(20), 0xAAAAAA, false);
            });

            // Bouton pour gérer les permissions
            addRenderableWidget(Button.builder(
                Component.translatable("erinium_faction.settings.ranks.manage_perms"),
                btn -> openRankPermissionsPopup(rank))
                .bounds(contentX + sw(350), finalY + sh(6), sw(100), sh(22))
                .build());

            y += rankH + sh(3);
        }
    }

    private void buildPermissionsTab() {
        int contentX = sx(15);
        int y = sy(45);

        // Info générale sur les permissions
        final int infoY = y;
        addRenderableOnly((g, mx, my, pt) ->
            g.drawString(this.font, Component.translatable("erinium_faction.settings.perms.info"),
                contentX, infoY, 0xFFFFFF, true)
        );
        y += sh(25);

        // Liste des permissions communes
        String[] commonPerms = {
            "faction.invite", "faction.kick", "faction.claim",
            "faction.unclaim", "faction.manage.home", "faction.manage.warps",
            "faction.manage.description", "faction.manage.bank"
        };

        for (String perm : commonPerms) {
            final int finalY = y;
            addRenderableOnly((g, mx, my, pt) ->
                g.drawString(this.font, Component.literal("§7• §e" + perm), contentX, finalY, 0xFFFFFF, false)
            );
            y += sh(15);
        }
    }

    // Gestion de la popup de permissions
    private RankData editingRank = null;
    private List<PermissionToggle> permissionToggles = new ArrayList<>();

    private static class PermissionToggle {
        String permission;
        boolean enabled;
        PermissionCheckbox checkbox;

        PermissionToggle(String permission, boolean enabled) {
            this.permission = permission;
            this.enabled = enabled;
        }
    }

    private void openRankPermissionsPopup(RankData rank) {
        editingRank = rank;
        rebuildWidgets();
    }

    private void closePermissionsPopup() {
        editingRank = null;
        permissionToggles.clear();
        rebuildWidgets();
    }

    private void buildPermissionsPopup() {
        if (editingRank == null) return;

        int popupW = sw(400);
        int popupH = sh(260);
        int popupX = sx(50);
        int popupY = sy(30);

        // Fond semi-transparent
        addRenderableOnly((g, mx, my, pt) -> {
            g.fill(0, 0, this.width, this.height, 0x88000000);
            g.fill(popupX, popupY, popupX + popupW, popupY + popupH, 0xFF2C3E50);
            g.fill(popupX, popupY, popupX + popupW, popupY + 2, 0xFF3498DB);
            g.fill(popupX, popupY + popupH - 2, popupX + popupW, popupY + popupH, 0xFF3498DB);
        });

        // Titre
        final int titleY = popupY + sh(8);
        addRenderableOnly((g, mx, my, pt) -> {
            Component title = Component.literal("§e" + editingRank.display + " §7- Permissions");
            int titleW = this.font.width(title);
            g.drawString(this.font, title, popupX + (popupW - titleW) / 2, titleY, 0xFFFFFF, true);
        });

        // Liste des permissions disponibles
        String[] allPerms = {
            "faction.invite", "faction.kick", "faction.claim", "faction.unclaim",
            "faction.manage.home", "faction.manage.warps", "faction.manage.description",
            "faction.manage.bank", "faction.manage.settings", "faction.manage.*"
        };

        int permY = popupY + sh(35);
        int permX = popupX + sw(10);
        int permSpacing = sh(22);
        int scrollOffset = 0;

        permissionToggles.clear();

        for (String perm : allPerms) {
            boolean hasPerm = editingRank.perms.contains(perm);
            PermissionToggle pt = new PermissionToggle(perm, hasPerm);
            permissionToggles.add(pt);

            final int finalPermY = permY + scrollOffset;
            final String finalPerm = perm;

            // Checkbox
            pt.checkbox = new PermissionCheckbox(permX, finalPermY + sh(2), hasPerm, isChecked -> {
                pt.enabled = isChecked;

                // Mettre à jour les permissions locales
                if (isChecked) {
                    editingRank.perms.add(pt.permission);
                } else {
                    editingRank.perms.remove(pt.permission);
                }

                // Envoyer au serveur (buttonID 20 = add perm, 21 = remove perm)
                PacketDistributor.sendToServer(new FactionMenuSettingsButtonMessage(
                    isChecked ? 20 : 21, this.x, this.y, this.z,
                    editingRank.id + ":" + pt.permission));
            });

            addRenderableWidget(pt.checkbox);

            // Label permission (à droite de la checkbox)
            addRenderableOnly((g, mx, my, ptick) -> {
                g.drawString(this.font, Component.literal(finalPerm),
                    permX + 22, finalPermY + sh(4), 0xFFFFFF, false);
            });

            scrollOffset += permSpacing;
        }

        // Bouton fermer
        addRenderableWidget(Button.builder(
            Component.translatable("gui.done"),
            btn -> closePermissionsPopup())
            .bounds(popupX + popupW / 2 - sw(40), popupY + popupH - sh(30), sw(80), sh(20))
            .build());
    }

    public static void onSettingsState(fr.eriniumgroup.erinium_faction.common.network.packets.FactionSettingsStateMessage msg) {
        if (net.minecraft.client.Minecraft.getInstance().screen instanceof FactionMenuSettingsScreen s) {
            s.toggleOpenState = msg.isOpen();
            s.toggleModePublic = msg.isPublicMode();
            s.toggleSafezoneState = msg.isSafezone();
            s.factionDisplayName = msg.displayName() != null ? msg.displayName() : "";
            s.factionDescription = msg.description() != null ? msg.description() : "";

            // Rangs (si le message les contient)
            s.ranks.clear();
            if (msg.ranks() != null) {
                for (var rankInfo : msg.ranks()) {
                    RankData rd = new RankData(rankInfo.id, rankInfo.display, rankInfo.priority);
                    rd.perms.addAll(rankInfo.perms);
                    s.ranks.add(rd);
                }
            }

            // Re-init pour refléter visuellement les états
            s.rebuildWidgets();
        }
    }

    @Override
    public void resize(Minecraft mc, int width, int height) {
        super.resize(mc, width, height);
        recomputeLayout();
        this.init();
    }
}