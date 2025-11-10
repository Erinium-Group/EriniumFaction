package fr.eriniumgroup.erinium_faction.client.gui.bounty;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

/**
 * GUI de sélection de joueur avec leurs têtes
 * Taille: 400x270 (comme list)
 */
public class PlayerSelectionScreen extends Screen {
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

    private PlayerListWidget playerList;
    private final BiConsumer<UUID, String> onPlayerSelected;

    public PlayerSelectionScreen(BiConsumer<UUID, String> onPlayerSelected) {
        super(Component.translatable("erinium_faction.gui.bounty.select_player"));
        this.onPlayerSelected = onPlayerSelected;
    }

    @Override
    protected void init() {
        // Position centrée
        leftPos = (this.width - GUI_WIDTH) / 2;
        topPos = (this.height - GUI_HEIGHT) / 2;

        // Créer la scroll list
        int scrollLeft = leftPos + SCROLL_X;
        int scrollTop = topPos + SCROLL_Y;
        int scrollBottom = scrollTop + SCROLL_HEIGHT;

        playerList = new PlayerListWidget(minecraft, SCROLL_WIDTH, SCROLL_HEIGHT, scrollTop, scrollBottom, 40);
        playerList.setX(scrollLeft);
        addRenderableWidget(playerList);

        // Charger la liste des joueurs en ligne
        loadPlayers();

        // Bouton "Retour"
        int buttonWidth = 100;
        int buttonHeight = 25;
        int buttonX = leftPos + (GUI_WIDTH / 2) - (buttonWidth / 2);
        int buttonY = topPos + GUI_HEIGHT - 35;

        addRenderableWidget(Button.builder(
                Component.translatable("gui.back"),
                btn -> minecraft.setScreen(new BountyCreatorScreen())
        ).bounds(buttonX, buttonY, buttonWidth, buttonHeight).build());
    }

    private void loadPlayers() {
        if (minecraft.getConnection() == null) return;

        List<PlayerInfo> players = new ArrayList<>(minecraft.getConnection().getOnlinePlayers());

        // Filtrer le joueur local
        UUID localPlayerId = minecraft.player != null ? minecraft.player.getUUID() : null;

        playerList.clear();
        for (PlayerInfo playerInfo : players) {
            GameProfile profile = playerInfo.getProfile();

            // Ne pas afficher le joueur local
            if (profile.getId().equals(localPlayerId)) continue;

            playerList.addPlayerEntry(new PlayerListWidget.PlayerEntry(profile, this::selectPlayer));
        }
    }

    private void selectPlayer(UUID playerId, String playerName) {
        onPlayerSelected.accept(playerId, playerName);
        minecraft.setScreen(new BountyCreatorScreen(playerId, playerName));
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Ne rien faire - désactive le flou
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Dessiner le background
        graphics.blit(BACKGROUND, leftPos, topPos, 0, 0, GUI_WIDTH, GUI_HEIGHT, GUI_WIDTH, GUI_HEIGHT);

        // Rendre les widgets
        super.render(graphics, mouseX, mouseY, partialTick);

        // Titre
        Component title = Component.translatable("erinium_faction.gui.bounty.select_player");
        int titleX = leftPos + (GUI_WIDTH / 2) - (font.width(title) / 2);
        graphics.drawString(font, title, titleX, topPos + GUI_HEIGHT - 50, 0xFFFFFF);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    /**
     * Widget de liste scrollable pour les joueurs
     */
    private static class PlayerListWidget extends ObjectSelectionList<PlayerListWidget.PlayerEntry> {
        public PlayerListWidget(Minecraft minecraft, int width, int height, int top, int bottom, int itemHeight) {
            super(minecraft, width, height, top, itemHeight);
        }

        public void clear() {
            this.clearEntries();
        }

        public int addPlayerEntry(PlayerEntry entry) {
            return this.addEntry(entry);
        }

        /**
         * Entrée de joueur dans la liste
         */
        private static class PlayerEntry extends Entry<PlayerEntry> {
            private final GameProfile profile;
            private final BiConsumer<UUID, String> onSelect;

            public PlayerEntry(GameProfile profile, BiConsumer<UUID, String> onSelect) {
                this.profile = profile;
                this.onSelect = onSelect;
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

                // Rendre la tête du joueur (player head)
                ItemStack playerHead = new ItemStack(Items.PLAYER_HEAD);
                playerHead.set(DataComponents.PROFILE, new ResolvableProfile(profile));

                // Dessiner l'item (tête du joueur)
                graphics.renderItem(playerHead, left + 10, top + 12);

                // Nom du joueur
                Component playerName = Component.literal("§f" + profile.getName());
                graphics.drawString(mc.font, playerName, left + 40, top + 16, 0xFFFFFF);
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (button == 0) {
                    onSelect.accept(profile.getId(), profile.getName());
                    return true;
                }
                return false;
            }

            @Override
            public @NotNull Component getNarration() {
                return Component.literal(profile.getName());
            }
        }
    }
}
