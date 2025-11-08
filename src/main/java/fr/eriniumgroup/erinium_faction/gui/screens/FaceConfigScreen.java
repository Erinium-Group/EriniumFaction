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
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

/**
 * Écran de configuration des faces - Style Cyber Astral avec textures PNG
 */
public class FaceConfigScreen extends Screen {
    private final Screen parentScreen;
    private final BlockPos machinePos;
    private final FaceConfiguration config;

    // Textures
    private static final ResourceLocation BG_STARFIELD = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/bg_starfield.png");
    private static final ResourceLocation BG_GRADIENT = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/bg_gradient_dark.png");
    private static final ResourceLocation GLOW_LINE_CYAN = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/glow_line_cyan.png");
    private static final ResourceLocation LED_ON = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/led_on.png");
    private static final ResourceLocation LED_OFF = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/led_off.png");
    private static final ResourceLocation CORNER_ACCENT = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/corner_accent.png");
    private static final ResourceLocation BUTTON_LARGE = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/button_large.png");
    private static final ResourceLocation BUTTON_SMALL = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/button_small.png");

    // Dimensions cube
    private static final int FACE_LARGE = 60;
    private static final int FACE_MEDIUM = 40;
    private static final int FACE_SMALL = 30;

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
        // Pas de boutons vanilla - on les rend avec des textures custom
    }

    private Component getAutoInputText() {
        return Component.literal("AUTO INPUT: " + (config.isAutoInput() ? "ON" : "OFF"));
    }

    private Component getAutoOutputText() {
        return Component.literal("AUTO OUTPUT: " + (config.isAutoOutput() ? "ON" : "OFF"));
    }

    private void updateAutoInputButton() {
        if (autoInputButton != null) {
            autoInputButton.setMessage(getAutoInputText());
        }
    }

    private void updateAutoOutputButton() {
        if (autoOutputButton != null) {
            autoOutputButton.setMessage(getAutoOutputText());
        }
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Background panel centré - MAX 400x270
        int panelWidth = 400;
        int panelHeight = 270;
        int panelX = centerX - panelWidth / 2;
        int panelY = centerY - panelHeight / 2;

        // Fond sombre semi-transparent
        graphics.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xDD0a0e27);

        // Border cyan
        graphics.fill(panelX, panelY, panelX + panelWidth, panelY + 2, 0xFF00FFFF);
        graphics.fill(panelX, panelY + panelHeight - 2, panelX + panelWidth, panelY + panelHeight, 0xFF00FFFF);
        graphics.fill(panelX, panelY, panelX + 2, panelY + panelHeight, 0xFF00FFFF);
        graphics.fill(panelX + panelWidth - 2, panelY, panelX + panelWidth, panelY + panelHeight, 0xFF00FFFF);

        // Titre
        graphics.drawCenteredString(this.font, this.title, centerX, panelY + 10, 0x00FFFF);

        // Ligne cyan sous titre
        int lineWidth = 150;
        graphics.blit(GLOW_LINE_CYAN, centerX - lineWidth/2, panelY + 22, 0, 0, lineWidth, 4, 200, 4);

        // Cube 3D - centré
        int cubeX = panelX + 115;
        int cubeY = panelY + 95;
        renderCube3D(graphics, cubeX, cubeY, mouseX, mouseY);

        // Légende TOUT à droite
        int legendX = panelX + panelWidth - 145;
        int legendY = panelY + 35;
        renderLegend(graphics, legendX, legendY);

        // Render custom buttons
        renderCustomButtons(graphics, panelX, panelY, panelWidth, panelHeight);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderCustomButtons(GuiGraphics graphics, int panelX, int panelY, int panelWidth, int panelHeight) {
        int buttonY = panelY + panelHeight - 30;

        // Auto Input button - button_large.png (120x20)
        int btn1X = panelX + 10;
        graphics.blit(BUTTON_LARGE, btn1X, buttonY, 0, 0, 120, 20, 120, 20);
        // LED indicator
        ResourceLocation led1 = config.isAutoInput() ? LED_ON : LED_OFF;
        graphics.blit(led1, btn1X + 5, buttonY + 6, 0, 0, 8, 8, 8, 8);
        // Text
        graphics.drawString(this.font, "AUTO INPUT", btn1X + 18, buttonY + 6, 0xFFFFFF, false);

        // Auto Output button - button_large.png (120x20)
        int btn2X = panelX + 140;
        graphics.blit(BUTTON_LARGE, btn2X, buttonY, 0, 0, 120, 20, 120, 20);
        // LED indicator
        ResourceLocation led2 = config.isAutoOutput() ? LED_ON : LED_OFF;
        graphics.blit(led2, btn2X + 5, buttonY + 6, 0, 0, 8, 8, 8, 8);
        // Text
        graphics.drawString(this.font, "AUTO OUTPUT", btn2X + 18, buttonY + 6, 0xFFFFFF, false);

        // Done button - button_small.png (60x20)
        int btn3X = panelX + 270;
        graphics.blit(BUTTON_SMALL, btn3X, buttonY, 0, 0, 60, 20, 60, 20);
        // Text
        graphics.drawString(this.font, "DONE", btn3X + 15, buttonY + 6, 0xFFFFFF, false);
    }

    private void renderLegend(GuiGraphics graphics, int x, int y) {
        graphics.drawString(this.font, "MODES:", x, y, 0x00FFFF, false);
        y += 12;

        // Dessiner chaque mode avec son icône
        FaceMode[] modes = FaceMode.values();
        for (int i = 0; i < modes.length; i++) {
            FaceMode mode = modes[i];
            ResourceLocation texture = getFaceTexture(mode);

            // Icône 14x14 - texture 60x60 scaled
            int iconSize = 14;
            float iconScale = iconSize / 60.0f;

            graphics.pose().pushPose();
            graphics.pose().translate(x, y, 0);
            graphics.pose().scale(iconScale, iconScale, 1.0f);
            graphics.blit(texture, 0, 0, 0, 0, 60, 60, 60, 60);
            graphics.pose().popPose();

            // Nom du mode avec sa couleur
            int color = mode.getColor();
            graphics.drawString(this.font, mode.getDisplayName(), x + 17, y + 3, color, false);

            y += 17;
        }
    }

    private void renderCorner(GuiGraphics graphics, int x, int y, boolean flipX, boolean flipY) {
        // TODO: Utiliser CORNER_ACCENT texture avec flip si besoin
        // Pour l'instant juste dessiner des lignes
        int color = 0x5500FFFF;
        if (flipX && flipY) {
            graphics.fill(x, y, x + 20, y + 1, color);
            graphics.fill(x, y, x + 1, y + 20, color);
        } else if (flipX) {
            graphics.fill(x, y, x + 20, y + 1, color);
            graphics.fill(x, y, x + 1, y + 20, color);
        } else if (flipY) {
            graphics.fill(x, y, x + 20, y + 1, color);
            graphics.fill(x, y, x + 1, y + 20, color);
        } else {
            graphics.fill(x, y, x + 20, y + 1, color);
            graphics.fill(x, y, x + 1, y + 20, color);
        }
    }

    private void renderCube3D(GuiGraphics graphics, int cubeX, int cubeY, int mouseX, int mouseY) {
        // Disposition en cube 3D isométrique - CENTRÉ
        // NORTH (40x40) - face principale au centre
        renderFace(graphics, cubeX - 20, cubeY, 40, Direction.NORTH, mouseX, mouseY);

        // UP (32x32) - au-dessus, centré
        renderFace(graphics, cubeX - 16, cubeY - 36, 32, Direction.UP, mouseX, mouseY);

        // EAST (32x32) - à droite, centré
        renderFace(graphics, cubeX + 24, cubeY + 4, 32, Direction.EAST, mouseX, mouseY);

        // WEST (32x32) - à gauche, centré
        renderFace(graphics, cubeX - 56, cubeY + 4, 32, Direction.WEST, mouseX, mouseY);

        // DOWN (24x24) - en bas à gauche, espacé
        renderFace(graphics, cubeX - 50, cubeY + 42, 24, Direction.DOWN, mouseX, mouseY);

        // SOUTH (24x24) - en bas au centre
        renderFace(graphics, cubeX - 12, cubeY + 48, 24, Direction.SOUTH, mouseX, mouseY);
    }

    private void renderFace(GuiGraphics graphics, int x, int y, int size, Direction face, int mouseX, int mouseY) {
        FaceMode mode = config.getFaceMode(face);
        ResourceLocation texture = getFaceTexture(mode);

        boolean hovered = mouseX >= x && mouseX < x + size && mouseY >= y && mouseY < y + size;

        // Texture de la face - 60x60 scaled au size voulu
        float faceScale = size / 60.0f;
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        graphics.pose().scale(faceScale, faceScale, 1.0f);
        graphics.blit(texture, 0, 0, 0, 0, 60, 60, 60, 60);
        graphics.pose().popPose();

        // Border jaune si hover
        if (hovered) {
            graphics.fill(x, y, x + size, y + 1, 0xFFFFFF00);
            graphics.fill(x, y + size - 1, x + size, y + size, 0xFFFFFF00);
            graphics.fill(x, y, x + 1, y + size, 0xFFFFFF00);
            graphics.fill(x + size - 1, y, x + size, y + size, 0xFFFFFF00);

            // Tooltip
            Component tooltip = Component.literal(face.getName().toUpperCase() + " → " + mode.getSerializedName().toUpperCase());
            graphics.renderTooltip(this.font, tooltip, mouseX, mouseY);
        }
    }

    private ResourceLocation getFaceTexture(FaceMode mode) {
        return switch (mode) {
            case NONE -> ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/face_none.png");
            case INPUT -> ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/face_input.png");
            case OUTPUT -> ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/face_output.png");
            case INPUT_OUTPUT -> ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/face_input_output.png");
            case ENERGY -> ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/face_energy.png");
            case FUEL -> ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/face_fuel.png");
        };
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int centerX = this.width / 2;
            int centerY = this.height / 2;
            int panelX = centerX - 200;
            int panelY = centerY - 135;

            int cubeX = panelX + 115;
            int cubeY = panelY + 95;

            // Check face clicks
            if (checkFaceClick(cubeX - 20, cubeY, 40, Direction.NORTH, mouseX, mouseY)) return true;
            if (checkFaceClick(cubeX - 16, cubeY - 36, 32, Direction.UP, mouseX, mouseY)) return true;
            if (checkFaceClick(cubeX + 24, cubeY + 4, 32, Direction.EAST, mouseX, mouseY)) return true;
            if (checkFaceClick(cubeX - 56, cubeY + 4, 32, Direction.WEST, mouseX, mouseY)) return true;
            if (checkFaceClick(cubeX - 50, cubeY + 42, 24, Direction.DOWN, mouseX, mouseY)) return true;
            if (checkFaceClick(cubeX - 12, cubeY + 48, 24, Direction.SOUTH, mouseX, mouseY)) return true;

            // Check button clicks
            int buttonY = panelY + panelHeight - 30;
            int btn1X = panelX + 10;
            int btn2X = panelX + 140;
            int btn3X = panelX + 270;

            if (mouseX >= btn1X && mouseX <= btn1X + 120 && mouseY >= buttonY && mouseY <= buttonY + 20) {
                config.setAutoInput(!config.isAutoInput());
                sendConfigUpdate(FaceConfigPacket.ConfigAction.TOGGLE_AUTO_INPUT, Direction.NORTH, FaceMode.NONE);
                return true;
            }
            if (mouseX >= btn2X && mouseX <= btn2X + 120 && mouseY >= buttonY && mouseY <= buttonY + 20) {
                config.setAutoOutput(!config.isAutoOutput());
                sendConfigUpdate(FaceConfigPacket.ConfigAction.TOGGLE_AUTO_OUTPUT, Direction.NORTH, FaceMode.NONE);
                return true;
            }
            if (mouseX >= btn3X && mouseX <= btn3X + 60 && mouseY >= buttonY && mouseY <= buttonY + 20) {
                onClose();
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private int panelHeight = 270;

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

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Ne rien faire - pas de background flou
    }
}
