package fr.eriniumgroup.erinium_faction.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import fr.eriniumgroup.erinium_faction.common.network.packets.FactionMenuSettingsButtonMessage;
import fr.eriniumgroup.erinium_faction.gui.menus.FactionMenuSettingsMenu;
import fr.eriniumgroup.erinium_faction.init.EFScreens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
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

import java.util.Objects;

public class FactionMenuSettingsScreen extends AbstractContainerScreen<FactionMenuSettingsMenu> implements EFScreens.ScreenAccessor {
    private final Level world;
    private final int x, y, z;
    private final Player entity;
    private boolean menuStateUpdateActive = false;

    private boolean toggleOpenState = false;
    private boolean toggleModePublic = false; // true=PUBLIC, false=INVITE_ONLY
    private boolean toggleSafezoneState = false;

    private static final int BASE_W = 420;
    private static final int BASE_H = 240;
    private double scaleX = 1.0, scaleY = 1.0;

    // Toggle custom widget (switch avec point coulissant)
    private static class DotToggleButton extends AbstractWidget {
        private boolean state;
        private final java.util.function.Consumer<Boolean> onToggle;
        private final int onColor;   // ARGB
        private final int offColor;  // ARGB
        private final Component labelOn;
        private final Component labelOff;

        public DotToggleButton(int x, int y, int w, int h, boolean initial, int onColor, int offColor, Component labelOn, Component labelOff, java.util.function.Consumer<Boolean> onToggle) {
            super(x, y, w, h, Component.empty());
            this.state = initial;
            this.onToggle = onToggle;
            this.onColor = onColor;
            this.offColor = offColor;
            this.labelOn = labelOn;
            this.labelOff = labelOff;
            setTooltip(Tooltip.create(Component.literal("")));
        }

        @Override
        protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
            int trackX0 = this.getX();
            int trackY0 = this.getY();
            int trackX1 = this.getX() + this.width;
            int trackY1 = this.getY() + this.height;
            // Fond de l'interrupteur (rouge/vert selon l'état)
            int bg = (state ? onColor : offColor);
            g.fill(trackX0, trackY0, trackX1, trackY1, bg);
            // Bordure légère
            g.fill(trackX0, trackY0, trackX1, trackY0 + 1, 0x55FFFFFF);
            g.fill(trackX0, trackY1 - 1, trackX1, trackY1, 0x33000000);

            // Position du point (dot)
            int margin = 3;
            int dotSize = this.height - margin * 2;
            int dotX = state ? (trackX1 - margin - dotSize) : (trackX0 + margin);
            int dotY = trackY0 + margin;
            // Dot blanc avec ombrage
            g.fill(dotX, dotY, dotX + dotSize, dotY + dotSize, 0xFFFFFFFF);
            g.fill(dotX, dotY, dotX + dotSize, dotY + 1, 0x55FFFFFF);
            g.fill(dotX, dotY + dotSize - 1, dotX + dotSize, dotY + dotSize, 0x22000000);

            // Libellé au centre de la piste (ON/OFF)
            Component lab = state ? labelOn : labelOff;
            var font = net.minecraft.client.Minecraft.getInstance().font;
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
        guiGraphics.blit(ResourceLocation.parse("erinium_faction:textures/screens/empty.png"), this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, BASE_W, BASE_H);
        // Titre
        guiGraphics.drawString(this.font, Component.translatable("erinium_faction.faction.menu.settings"), sx(12), sy(10), 0xFFFFFF, false);
        // Sous-titre retiré (EFUtils supprimé)
        RenderSystem.disableBlend();
    }

    @Override
    public boolean keyPressed(int key, int b, int c) {
        if (key == 256) {
            assert Objects.requireNonNull(this.minecraft).player != null;
            this.minecraft.player.closeContainer();
            return true;
        }
        return super.keyPressed(key, b, c);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
    }

    @Override
    public void init() {
        super.init();
        recomputeLayout();
        int toggleW = sw(120);
        int toggleH = sh(20);
        int xLeft = sx(11);
        int yBase = sy(50);
        int dy = sh(26);

        // Libellés
        var lblOpen = Component.translatable("erinium_faction.settings.label.open");
        var lblMode = Component.translatable("erinium_faction.settings.label.mode");
        var lblSafe = Component.translatable("erinium_faction.settings.label.safezone");

        // Dessin des libellés via renderLabels, on stocke les positions dans des champs si nécessaire
        // Ici on dessine directement dans renderBg pour simplicité (statique)

        var tOpen = new DotToggleButton(xLeft + sw(140), yBase, toggleW, toggleH, toggleOpenState,
                0xFF2ECC71, 0xFFE74C3C,
                Component.literal("OPEN"), Component.literal("CLOSED"),
                (st) -> PacketDistributor.sendToServer(new FactionMenuSettingsButtonMessage(0, x, y, z))
        );
        tOpen.setTooltip(Tooltip.create(Component.translatable("erinium_faction.settings.tooltip.open")));
        this.addRenderableWidget(tOpen);

        var tMode = new DotToggleButton(xLeft + sw(140), yBase + dy, toggleW, toggleH, toggleModePublic,
                0xFF2ECC71, 0xFFE74C3C,
                Component.literal("PUBLIC"), Component.literal("INVITE"),
                (st) -> PacketDistributor.sendToServer(new FactionMenuSettingsButtonMessage(2, x, y, z))
        );
        tMode.setTooltip(Tooltip.create(Component.translatable("erinium_faction.settings.tooltip.mode")));
        this.addRenderableWidget(tMode);

        var tSafe = new DotToggleButton(xLeft + sw(140), yBase + dy * 2, toggleW, toggleH, toggleSafezoneState,
                0xFF2ECC71, 0xFFE74C3C,
                Component.literal("ON"), Component.literal("OFF"),
                (st) -> PacketDistributor.sendToServer(new FactionMenuSettingsButtonMessage(3, x, y, z))
        );
        tSafe.setTooltip(Tooltip.create(Component.translatable("erinium_faction.settings.tooltip.safezone")));
        this.addRenderableWidget(tSafe);

        // Dessiner les labels ici pour éviter un état global supplémentaire
        var f = this.font;
        this.addRenderableOnly((g, mx, my, pt) -> {
            g.drawString(f, lblOpen, xLeft, yBase + (toggleH - f.lineHeight) / 2, 0xFFFFFF, false);
            g.drawString(f, lblMode, xLeft, yBase + dy + (toggleH - f.lineHeight) / 2, 0xFFFFFF, false);
            g.drawString(f, lblSafe, xLeft, yBase + dy * 2 + (toggleH - f.lineHeight) / 2, 0xFFFFFF, false);
        });
    }

    public static void onSettingsState(fr.eriniumgroup.erinium_faction.common.network.packets.FactionSettingsStateMessage msg) {
        if (net.minecraft.client.Minecraft.getInstance().screen instanceof FactionMenuSettingsScreen s) {
            s.toggleOpenState = msg.isOpen();
            s.toggleModePublic = msg.isPublicMode();
            s.toggleSafezoneState = msg.isSafezone();
            // re-init pour refléter visuellement les états
            s.init();
        }
    }

    @Override
    public void resize(Minecraft mc, int width, int height) {
        super.resize(mc, width, height);
        recomputeLayout();
        this.init();
    }
}