package fr.eriniumgroup.erinium_faction.client.gui.bounty;

import fr.eriniumgroup.erinium_faction.common.network.packets.BountyDataPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * GUI pour afficher la liste des bounties actives
 * Taille: 400x270 max avec scale
 * Scroll list: 370x250 à position 15,10
 */
public class BountyListScreen extends Screen {
    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/bounty/list.png");
    private static final int GUI_WIDTH = 400;
    private static final int GUI_HEIGHT = 270;
    private static final int SCROLL_WIDTH = 370;
    private static final int SCROLL_HEIGHT = 250;
    private static final int SCROLL_X = 15;
    private static final int SCROLL_Y = 10;

    private int leftPos;
    private int topPos;
    private float scale = 1.0f;

    private BountyListWidget bountyList;

    public BountyListScreen() {
        super(Component.translatable("erinium_faction.gui.bounty.list.title"));
    }

    @Override
    protected void init() {
        // Calculer la position et le scale
        calculateScaleAndPosition();

        // Position centrée
        leftPos = (this.width - GUI_WIDTH) / 2;
        topPos = (this.height - GUI_HEIGHT) / 2;

        // Créer la scroll list
        int scrollLeft = leftPos + SCROLL_X;
        int scrollTop = topPos + SCROLL_Y;
        int scrollBottom = scrollTop + SCROLL_HEIGHT;

        bountyList = new BountyListWidget(minecraft, SCROLL_WIDTH, SCROLL_HEIGHT, scrollTop, scrollBottom, 40);
        bountyList.setX(scrollLeft);
        addRenderableWidget(bountyList);

        // Charger les bounties
        loadBounties();

        // Bouton "Retour"
        int buttonWidth = 100;
        int buttonHeight = 25;
        int buttonX = leftPos + (GUI_WIDTH / 2) - (buttonWidth / 2);
        int buttonY = topPos + GUI_HEIGHT - 35;

        addRenderableWidget(Button.builder(
                Component.translatable("gui.back"),
                btn -> minecraft.setScreen(new BountyMainMenuScreen())
        ).bounds(buttonX, buttonY, buttonWidth, buttonHeight).build());
    }

    private void calculateScaleAndPosition() {
        float scaleW = (float) this.width / GUI_WIDTH;
        float scaleH = (float) this.height / GUI_HEIGHT;
        scale = Math.min(1.0f, Math.min(scaleW, scaleH));
    }

    private void loadBounties() {
        List<BountyDataPacket.BountyEntry> bounties = BountyClientData.getBounties();
        bountyList.clear();
        for (BountyDataPacket.BountyEntry bounty : bounties) {
            bountyList.addBountyEntry(new BountyListWidget.BountyEntry(bounty));
        }
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Ne rien faire - désactive le flou du background par défaut
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Dessiner le background
        graphics.blit(BACKGROUND, leftPos, topPos, 0, 0, GUI_WIDTH, GUI_HEIGHT, GUI_WIDTH, GUI_HEIGHT);

        // Rendre les widgets
        super.render(graphics, mouseX, mouseY, partialTick);

        // Titre (après les widgets)
        Component title = Component.translatable("erinium_faction.gui.bounty.list.title");
        int titleX = leftPos + (GUI_WIDTH / 2) - (font.width(title) / 2);
        graphics.drawString(font, title, titleX, topPos + GUI_HEIGHT - 50, 0xFFFFFF);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    /**
     * Widget de liste scrollable pour les bounties
     */
    private static class BountyListWidget extends ObjectSelectionList<BountyListWidget.BountyEntry> {
        public BountyListWidget(Minecraft minecraft, int width, int height, int top, int bottom, int itemHeight) {
            super(minecraft, width, height, top, itemHeight);
        }

        public void clear() {
            this.clearEntries();
        }

        public int addBountyEntry(BountyEntry entry) {
            return this.addEntry(entry);
        }

        /**
         * Entrée de bounty dans la liste
         */
        private static class BountyEntry extends Entry<BountyEntry> {
            private final BountyDataPacket.BountyEntry bounty;

            public BountyEntry(BountyDataPacket.BountyEntry bounty) {
                this.bounty = bounty;
            }

            @Override
            public void render(@NotNull GuiGraphics graphics, int index, int top, int left, int width, int height,
                               int mouseX, int mouseY, boolean hovered, float partialTick) {
                Minecraft mc = Minecraft.getInstance();

                // Background de l'entrée
                if (hovered) {
                    graphics.fill(left, top, left + width, top + height, 0x80FFFFFF);
                } else {
                    graphics.fill(left, top, left + width, top + height, 0x40000000);
                }

                // Nom du joueur
                Component playerName = Component.literal("§c" + bounty.targetName());
                graphics.drawString(mc.font, playerName, left + 10, top + 5, 0xFFFFFF);

                // Montant
                Component amount = Component.literal("§e" + String.format("%.2f", bounty.totalAmount()) + "$");
                graphics.drawString(mc.font, amount, left + 10, top + 18, 0xFFFF00);

                // Temps restant
                long timeSeconds = bounty.timeRemaining();
                String timeStr = formatTime(timeSeconds);
                Component timeRemaining = Component.literal("§7Expire dans: §f" + timeStr);
                graphics.drawString(mc.font, timeRemaining, left + width - mc.font.width(timeRemaining) - 10, top + 12, 0xAAAAAA);
            }

            private String formatTime(long seconds) {
                if (seconds < 60) {
                    return seconds + "s";
                } else if (seconds < 3600) {
                    long minutes = seconds / 60;
                    return minutes + "m";
                } else if (seconds < 86400) {
                    long hours = seconds / 3600;
                    return hours + "h";
                } else {
                    long days = seconds / 86400;
                    return days + "j";
                }
            }

            @Override
            public @NotNull Component getNarration() {
                return Component.literal(bounty.targetName() + " - " + bounty.totalAmount() + "$");
            }
        }
    }
}
