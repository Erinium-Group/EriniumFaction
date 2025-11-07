package fr.eriniumgroup.erinium_faction.gui.screens;

import fr.eriniumgroup.erinium_faction.common.block.entity.FaceConfiguration;
import fr.eriniumgroup.erinium_faction.common.block.entity.FaceMode;
import fr.eriniumgroup.erinium_faction.common.network.packets.FaceConfigPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

/**
 * Écran de configuration des faces - Style Mekanism
 */
public class FaceConfigScreen extends Screen {
    private final Screen parentScreen;
    private final BlockPos machinePos;
    private final FaceConfiguration config;

    private static final int GRID_SIZE = 60;
    private static final int SPACING = 15;

    private Button autoInputButton;
    private Button autoOutputButton;

    public FaceConfigScreen(Screen parentScreen, BlockPos machinePos, FaceConfiguration config) {
        super(Component.translatable("gui.erinium_faction.face_config.title"));
        this.parentScreen = parentScreen;
        this.machinePos = machinePos;
        this.config = config.copy();
    }

    @Override
    protected void init() {
        super.init();

        int screenCenterX = this.width / 2;
        int screenCenterY = this.height / 2;

        // Bouton Auto Input
        autoInputButton = Button.builder(
            Component.translatable("gui.erinium_faction.face_config.auto_input")
                .append(": ")
                .append(Component.translatable(config.isAutoInput() ? "gui.erinium_faction.face_config.on" : "gui.erinium_faction.face_config.off")),
            btn -> {
                config.setAutoInput(!config.isAutoInput());
                updateAutoInputButton();
                sendConfigUpdate(FaceConfigPacket.ConfigAction.TOGGLE_AUTO_INPUT, Direction.NORTH, FaceMode.NONE);
            }
        ).bounds(screenCenterX - 150, screenCenterY + 120, 140, 20).build();

        // Bouton Auto Output
        autoOutputButton = Button.builder(
            Component.translatable("gui.erinium_faction.face_config.auto_output")
                .append(": ")
                .append(Component.translatable(config.isAutoOutput() ? "gui.erinium_faction.face_config.on" : "gui.erinium_faction.face_config.off")),
            btn -> {
                config.setAutoOutput(!config.isAutoOutput());
                updateAutoOutputButton();
                sendConfigUpdate(FaceConfigPacket.ConfigAction.TOGGLE_AUTO_OUTPUT, Direction.NORTH, FaceMode.NONE);
            }
        ).bounds(screenCenterX + 10, screenCenterY + 120, 140, 20).build();

        // Bouton Fermer
        Button closeButton = Button.builder(Component.translatable("gui.done"), btn -> onClose())
            .bounds(screenCenterX - 50, screenCenterY + 150, 100, 20).build();

        this.addRenderableWidget(autoInputButton);
        this.addRenderableWidget(autoOutputButton);
        this.addRenderableWidget(closeButton);
    }

    private void updateAutoInputButton() {
        if (autoInputButton != null) {
            autoInputButton.setMessage(Component.translatable("gui.erinium_faction.face_config.auto_input")
                .append(": ")
                .append(Component.translatable(config.isAutoInput() ? "gui.erinium_faction.face_config.on" : "gui.erinium_faction.face_config.off")));
        }
    }

    private void updateAutoOutputButton() {
        if (autoOutputButton != null) {
            autoOutputButton.setMessage(Component.translatable("gui.erinium_faction.face_config.auto_output")
                .append(": ")
                .append(Component.translatable(config.isAutoOutput() ? "gui.erinium_faction.face_config.on" : "gui.erinium_faction.face_config.off")));
        }
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);

        int screenCenterX = this.width / 2;
        int screenCenterY = this.height / 2;

        // Titre
        graphics.drawCenteredString(this.font, this.title, screenCenterX, screenCenterY - 110, 0x00DD00);

        // Affichage du cube 3D aplati
        renderMekanismStyle3D(graphics, screenCenterX, screenCenterY, mouseX, mouseY);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderMekanismStyle3D(GuiGraphics graphics, int centerX, int centerY, int mouseX, int mouseY) {
        // Vue isométrique : on affiche 3 faces visibles du cube

        // FACE AVANT (NORTH) - Grande face
        int frontX = centerX - GRID_SIZE;
        int frontY = centerY - GRID_SIZE / 2;
        renderFace(graphics, frontX, frontY, Direction.NORTH, mouseX, mouseY);

        // FACE DROITE (EAST) - Petite face en perspective
        int rightX = centerX + GRID_SIZE + SPACING;
        int rightY = centerY - GRID_SIZE / 2;
        renderFaceSmall(graphics, rightX, rightY, Direction.EAST, mouseX, mouseY);

        // FACE HAUT (UP) - Petite face en haut
        int topX = centerX - GRID_SIZE + GRID_SIZE / 2;
        int topY = centerY - GRID_SIZE - SPACING;
        renderFaceSmall(graphics, topX, topY, Direction.UP, mouseX, mouseY);

        // Les autres faces en petit (DOWN, SOUTH, WEST)
        int smallSize = GRID_SIZE / 3;

        // DOWN (bas)
        renderFaceVerySmall(graphics, centerX - GRID_SIZE - smallSize - 5, centerY + GRID_SIZE, Direction.DOWN, mouseX, mouseY);

        // SOUTH (arrière)
        renderFaceVerySmall(graphics, centerX, centerY + GRID_SIZE + SPACING, Direction.SOUTH, mouseX, mouseY);

        // WEST (gauche)
        renderFaceVerySmall(graphics, centerX - GRID_SIZE * 2 - SPACING - 5, centerY - GRID_SIZE / 2, Direction.WEST, mouseX, mouseY);
    }

    private void renderFace(GuiGraphics graphics, int x, int y, Direction face, int mouseX, int mouseY) {
        FaceMode mode = config.getFaceMode(face);
        int color = mode.getColor();

        boolean hovered = mouseX >= x && mouseX < x + GRID_SIZE && mouseY >= y && mouseY < y + GRID_SIZE;

        // Fond dégradé
        graphics.fill(x, y, x + GRID_SIZE, y + GRID_SIZE, 0xFF000000 | color);

        // Bordure brillante si hover
        int borderColor = hovered ? 0xFFFFFF00 : 0xFF808080;
        graphics.fill(x, y, x + GRID_SIZE, y + 1, borderColor);
        graphics.fill(x, y + GRID_SIZE - 1, x + GRID_SIZE, y + GRID_SIZE, borderColor);
        graphics.fill(x, y, x + 1, y + GRID_SIZE, borderColor);
        graphics.fill(x + GRID_SIZE - 1, y, x + GRID_SIZE, y + GRID_SIZE, borderColor);

        // Texte : Nom direction + Mode
        String dirName = face.getName().toUpperCase().substring(0, 1);
        graphics.drawCenteredString(this.font, dirName, x + GRID_SIZE / 2, y + 15, 0xFFFFFF);

        String modeName = mode.getSerializedName().substring(0, Math.min(4, mode.getSerializedName().length())).toUpperCase();
        graphics.drawCenteredString(this.font, modeName, x + GRID_SIZE / 2, y + GRID_SIZE / 2 + 5, 0xCCCCCC);

        // Tooltip
        if (hovered) {
            Component tooltip = Component.literal(face.getName().toUpperCase())
                .append(" → ")
                .append(Component.translatable("gui.erinium_faction.face_mode." + mode.getSerializedName()))
                .append("\n§7Click to cycle");
            graphics.renderTooltip(this.font, this.font.split(tooltip, 150), mouseX, mouseY);
        }
    }

    private void renderFaceSmall(GuiGraphics graphics, int x, int y, Direction face, int mouseX, int mouseY) {
        int size = GRID_SIZE * 2 / 3;
        FaceMode mode = config.getFaceMode(face);
        int color = mode.getColor();

        boolean hovered = mouseX >= x && mouseX < x + size && mouseY >= y && mouseY < y + size;

        graphics.fill(x, y, x + size, y + size, 0xFF000000 | color);

        int borderColor = hovered ? 0xFFFFFF00 : 0xFF808080;
        graphics.fill(x, y, x + size, y + 1, borderColor);
        graphics.fill(x, y + size - 1, x + size, y + size, borderColor);
        graphics.fill(x, y, x + 1, y + size, borderColor);
        graphics.fill(x + size - 1, y, x + size, y + size, borderColor);

        String dirName = face.getName().toUpperCase().substring(0, 1);
        graphics.drawCenteredString(this.font, dirName, x + size / 2, y + size / 2 - 3, 0xFFFFFF);

        if (hovered) {
            Component tooltip = Component.literal(face.getName().toUpperCase())
                .append(" → ")
                .append(Component.translatable("gui.erinium_faction.face_mode." + mode.getSerializedName()))
                .append("\n§7Click to cycle");
            graphics.renderTooltip(this.font, this.font.split(tooltip, 150), mouseX, mouseY);
        }
    }

    private void renderFaceVerySmall(GuiGraphics graphics, int x, int y, Direction face, int mouseX, int mouseY) {
        int size = GRID_SIZE / 2;
        FaceMode mode = config.getFaceMode(face);
        int color = mode.getColor();

        boolean hovered = mouseX >= x && mouseX < x + size && mouseY >= y && mouseY < y + size;

        graphics.fill(x, y, x + size, y + size, 0xFF000000 | color);

        int borderColor = hovered ? 0xFFFFFF00 : 0xFF606060;
        graphics.fill(x, y, x + size, y + 1, borderColor);
        graphics.fill(x, y + size - 1, x + size, y + size, borderColor);
        graphics.fill(x, y, x + 1, y + size, borderColor);
        graphics.fill(x + size - 1, y, x + size, y + size, borderColor);

        if (hovered) {
            Component tooltip = Component.literal(face.getName().toUpperCase())
                .append(" → ")
                .append(Component.translatable("gui.erinium_faction.face_mode." + mode.getSerializedName()))
                .append("\n§7Click to cycle");
            graphics.renderTooltip(this.font, this.font.split(tooltip, 150), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int centerX = this.width / 2;
            int centerY = this.height / 2;

            // FRONT (grande face)
            if (checkFaceClick(centerX - GRID_SIZE, centerY - GRID_SIZE / 2, GRID_SIZE, Direction.NORTH, mouseX, mouseY)) {
                return true;
            }

            // RIGHT (face droite)
            if (checkFaceClick(centerX + GRID_SIZE + SPACING, centerY - GRID_SIZE / 2, GRID_SIZE * 2 / 3, Direction.EAST, mouseX, mouseY)) {
                return true;
            }

            // TOP (face haut)
            if (checkFaceClick(centerX - GRID_SIZE + GRID_SIZE / 2, centerY - GRID_SIZE - SPACING, GRID_SIZE * 2 / 3, Direction.UP, mouseX, mouseY)) {
                return true;
            }

            // DOWN, SOUTH, WEST
            int smallSize = GRID_SIZE / 2;
            if (checkFaceClick(centerX - GRID_SIZE - smallSize - 5, centerY + GRID_SIZE, smallSize, Direction.DOWN, mouseX, mouseY)) {
                return true;
            }
            if (checkFaceClick(centerX, centerY + GRID_SIZE + SPACING, smallSize, Direction.SOUTH, mouseX, mouseY)) {
                return true;
            }
            if (checkFaceClick(centerX - GRID_SIZE * 2 - SPACING - 5, centerY - GRID_SIZE / 2, smallSize, Direction.WEST, mouseX, mouseY)) {
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean checkFaceClick(int x, int y, int size, Direction face, double mouseX, double mouseY) {
        if (mouseX >= x && mouseX < x + size && mouseY >= y && mouseY < y + size) {
            FaceMode currentMode = config.getFaceMode(face);
            FaceMode nextMode = currentMode.next();
            config.setFaceMode(face, nextMode);
            sendConfigUpdate(FaceConfigPacket.ConfigAction.SET_FACE_MODE, face, nextMode);
            return true;
        }
        return false;
    }

    private void sendConfigUpdate(FaceConfigPacket.ConfigAction action, Direction face, FaceMode mode) {
        PacketDistributor.sendToServer(new FaceConfigPacket(machinePos, action, face, mode));
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(parentScreen);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

