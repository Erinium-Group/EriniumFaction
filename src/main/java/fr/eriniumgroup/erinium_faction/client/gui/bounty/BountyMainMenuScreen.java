package fr.eriniumgroup.erinium_faction.client.gui.bounty;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * GUI du menu principal du système de bounty
 * Taille: 300x250 max avec scale
 */
public class BountyMainMenuScreen extends Screen {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/bounty/little_menu.png");
    private static final int GUI_WIDTH = 300;
    private static final int GUI_HEIGHT = 250;

    private int leftPos;
    private int topPos;
    private float scale = 1.0f;

    public BountyMainMenuScreen() {
        super(Component.translatable("erinium_faction.gui.bounty.title"));
    }

    @Override
    protected void init() {
        // Calculer la position et le scale
        calculateScaleAndPosition();

        // Position centrée
        leftPos = (this.width - GUI_WIDTH) / 2;
        topPos = (this.height - GUI_HEIGHT) / 2;

        // Boutons au centre
        int buttonWidth = 180;
        int buttonHeight = 30;
        int centerX = leftPos + (GUI_WIDTH / 2) - (buttonWidth / 2);
        int startY = topPos + 70;

        // Bouton "Placer une prime"
        addRenderableWidget(Button.builder(
                Component.translatable("erinium_faction.gui.bounty.place"),
                btn -> minecraft.setScreen(new BountyCreatorScreen())
        ).bounds(centerX, startY, buttonWidth, buttonHeight).build());

        // Bouton "Liste des primes"
        addRenderableWidget(Button.builder(
                Component.translatable("erinium_faction.gui.bounty.list"),
                btn -> minecraft.setScreen(new BountyListScreen())
        ).bounds(centerX, startY + 40, buttonWidth, buttonHeight).build());

        // Bouton "Fermer"
        addRenderableWidget(Button.builder(
                Component.translatable("gui.done"),
                btn -> onClose()
        ).bounds(centerX, startY + 80, buttonWidth, buttonHeight).build());
    }

    private void calculateScaleAndPosition() {
        // Scale si l'écran est plus grand que la GUI
        float scaleW = (float) this.width / GUI_WIDTH;
        float scaleH = (float) this.height / GUI_HEIGHT;
        scale = Math.min(1.0f, Math.min(scaleW, scaleH));
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Ne rien faire - désactive le flou du background par défaut
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Dessiner le background de la GUI
        graphics.blit(BACKGROUND, leftPos, topPos, 0, 0, GUI_WIDTH, GUI_HEIGHT, GUI_WIDTH, GUI_HEIGHT);

        // Rendre les widgets
        super.render(graphics, mouseX, mouseY, partialTick);

        // Titre (après les widgets pour qu'il soit au-dessus)
        Component title = Component.translatable("erinium_faction.gui.bounty.title");
        int titleX = leftPos + (GUI_WIDTH / 2) - (font.width(title) / 2);
        graphics.drawString(font, title, titleX, topPos + 20, 0xFFFFFF);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
