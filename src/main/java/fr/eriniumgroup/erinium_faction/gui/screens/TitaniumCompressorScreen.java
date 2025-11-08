package fr.eriniumgroup.erinium_faction.gui.screens;

import fr.eriniumgroup.erinium_faction.gui.menus.TitaniumCompressorMenu;
import fr.eriniumgroup.erinium_faction.gui.widgets.FaceConfigButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class TitaniumCompressorScreen extends AbstractContainerScreen<TitaniumCompressorMenu> {
    // Textures
    private static final ResourceLocation PANEL_MAIN = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/panel_main.png");
    private static final ResourceLocation SLOT_INPUT = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/slot_input.png");
    private static final ResourceLocation SLOT_OUTPUT = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/slot_output.png");
    private static final ResourceLocation SLOT_INVENTORY = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/slot_inventory.png");
    private static final ResourceLocation ENERGY_BAR_BG = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/energy_bar_bg.png");
    private static final ResourceLocation ENERGY_BAR_FILL = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/energy_bar_fill.png");
    private static final ResourceLocation PROGRESS_BG = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/progress_arrow_bg.png");
    private static final ResourceLocation PROGRESS_EMPTY = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/progress_arrow_empty.png");
    private static final ResourceLocation PROGRESS_FILL = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/progress_arrow_fill.png");
    private static final ResourceLocation PROGRESS_PARTICLE = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/progress_arrow_particle.png");
    private static final ResourceLocation PROGRESS_COMPLETE = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/progress_arrow_complete.png");

    private FaceConfigButton configButton;

    public TitaniumCompressorScreen(TitaniumCompressorMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    private int getProgress() {
        return this.menu.getProgress();
    }

    private int getEnergyStored() {
        return this.menu.getEnergyStored();
    }

    @Override
    protected void init() {
        super.init();

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        configButton = new FaceConfigButton(
            x + this.imageWidth - 25,
            y + 5,
            this.menu.getBlockEntity().getBlockPos(),
            this.menu.getBlockEntity().getFaceConfiguration(),
            this
        );
        this.addRenderableWidget(configButton);
    }

    @Override
    protected void renderBg(GuiGraphics gg, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // Background principal
        gg.blit(PANEL_MAIN, x, y, 0, 0, this.imageWidth, this.imageHeight, 176, 166);

        // Slots
        // Input slot (56, 35)
        gg.blit(SLOT_INPUT, x + 56 - 1, y + 35 - 1, 0, 0, 18, 18, 18, 18);
        // Output slot (116, 35)
        gg.blit(SLOT_OUTPUT, x + 116 - 1, y + 35 - 1, 0, 0, 18, 18, 18, 18);

        // Inventory slots (8, 84) - 9 colonnes x 3 lignes
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                gg.blit(SLOT_INVENTORY, x + 8 + col * 18 - 1, y + 84 + row * 18 - 1, 0, 0, 18, 18, 18, 18);
            }
        }

        // Hotbar slots (8, 142) - 9 colonnes
        for (int col = 0; col < 9; col++) {
            gg.blit(SLOT_INVENTORY, x + 8 + col * 18 - 1, y + 142 - 1, 0, 0, 18, 18, 18, 18);
        }

        // === ENERGY BAR (10, 20, 14x44) ===
        // Background
        gg.blit(ENERGY_BAR_BG, x + 10, y + 20, 0, 0, 14, 44, 14, 44);

        // Fill (crop from bottom based on energy %)
        int energy = getEnergyStored();
        int maxEnergy = 50000; // Max energy capacity

        if (maxEnergy > 0 && energy > 0) {
            int fillHeight = (int) (42 * ((float) energy / maxEnergy));
            int yOffset = 42 - fillHeight;

            // Blit from bottom up
            gg.blit(ENERGY_BAR_FILL,
                x + 11, y + 21 + yOffset,  // screen position
                0, yOffset,                 // texture position
                12, fillHeight,             // size
                12, 42);                    // texture size
        }

        // === PROGRESS ARROW - 48x8 texture, SCALED avec PoseStack ===
        // Espace disponible entre input(56+18=74) et output(116) = 42 pixels
        // Scale factor: 36/48 = 0.75
        float scale = 0.75f;
        int arrowX = x + 76;
        int arrowY = y + 41;

        gg.pose().pushPose();
        gg.pose().translate(arrowX, arrowY, 0);
        gg.pose().scale(scale, scale, 1.0f);

        // Background - texture complète 48x8 (statique)
        gg.blit(PROGRESS_BG, 0, 0, 0, 0, 48, 8, 48, 8);

        // Progress
        int progress = getProgress();
        int maxProgress = 100;

        if (maxProgress > 0 && progress > 0) {
            float progressPercent = (float) progress / maxProgress;
            int fillWidth = (int) (43 * Math.min(progressPercent, 1.0f)); // 43px max

            if (fillWidth > 0) {
                // FILL - texture 43x6 qui grandit dynamiquement, centré dans le bg (48x8)
                // Centré: X offset = (48-43)/2 = 2.5 ≈ 3, Y offset = (8-6)/2 = 1
                gg.blit(PROGRESS_FILL, 3, 1, 0, 0, fillWidth, 6, 43, 6);
            }
        }

        gg.pose().popPose();
    }

    @Override
    protected void renderLabels(GuiGraphics gg, int mouseX, int mouseY) {
        // Titre en cyan
        gg.drawString(this.font, this.title, 8, 6, 0x00FFFF, false);
        // Inventory en violet
        gg.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, 0x9D4EDD, false);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);

        // Config button tooltip
        if (configButton != null && configButton.isHovered()) {
            graphics.renderTooltip(this.font, Component.translatable("gui.erinium_faction.face_config.tooltip"), mouseX, mouseY);
        }

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // Energy bar tooltip
        if (mouseX >= x + 10 && mouseX <= x + 24 && mouseY >= y + 20 && mouseY <= y + 64) {
            int energy = getEnergyStored();
            int maxEnergy = 50000;

            Component tooltip = Component.literal(
                String.format("Energy: %,d / %,d FE", energy, maxEnergy)
            );
            graphics.renderTooltip(this.font, tooltip, mouseX, mouseY);
        }

        // Progress tooltip
        if (mouseX >= x + 76 && mouseX <= x + 112 && mouseY >= y + 41 && mouseY <= y + 47) {
            int progress = getProgress();
            int maxProgress = 100;

            if (maxProgress > 0 && progress > 0) {
                int percent = (int) (100 * ((float) progress / maxProgress));
                Component tooltip = Component.literal(
                    String.format("Progress: %d%% (%d / %d ticks)", percent, progress, maxProgress)
                );
                graphics.renderTooltip(this.font, tooltip, mouseX, mouseY);
            }
        }
    }
}
