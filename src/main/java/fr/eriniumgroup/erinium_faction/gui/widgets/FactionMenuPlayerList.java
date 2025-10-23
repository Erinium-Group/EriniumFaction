/*
 * The code of this mod element is always locked.
 *
 * You can register new events in this class too.
 *
 * If you want to make a plain independent class, create it using
 * Project Browser -> New... and make sure to make the class
 * outside fr.eriniumgroup.eriniumfaction as this package is managed by MCreator.
 *
 * If you change workspace package, modid or prefix, you will need
 * to manually adapt this file to these changes or remake it.
 *
 * This class will be added in the mod root package.
 */
package fr.eriniumgroup.erinium_faction.gui.widgets;

import fr.eriniumgroup.erinium_faction.common.util.EFUtils;
import fr.eriniumgroup.erinium_faction.procedures.GetFileStringValueProcedure;
import fr.eriniumgroup.erinium_faction.procedures.UuidFileProcedure;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class FactionMenuPlayerList extends AbstractSelectionList<FactionMenuPlayerList.Entry> {
    private int selectedIndex = -1;
    private int height;
    private int width;
    private int y;
    private int x;
    private Minecraft minecraft;
    private MinecraftServer server;
    private final Map<UUID, ItemStack> headCache = new ConcurrentHashMap<>();
    private final Map<UUID, CompletableFuture<ItemStack>> headFutures = new ConcurrentHashMap<>();


    public FactionMenuPlayerList(Minecraft minecraft, int x, int y, int itemWidth, int itemHeight, String param, MinecraftServer server) {
        super(minecraft, itemWidth, itemHeight, y, 18);
        this.setX(x); // Définit la position horizontale
        this.height = itemHeight;
        this.width = itemWidth;
        this.x = x;
        this.y = y;
        this.minecraft = minecraft;
        this.server = server;

        // Ajoute des entrées
        for (int i = 0; i < param.split(",").length; i++) {
            this.addEntry(new Entry(param.split(",")[i]));
        }
    }

    // Supprime tout fond indésirable
    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Désactiver le rendu de la dirt en fond
        this.renderListBackground(guiGraphics);

        // Convertir les coordonnées pour le Scissor Test
        int scissorX = this.getX();
        int scissorY = this.getY();
        int scissorWidth = this.width;
        int scissorHeight = this.height;

        // Activer le Scissor Test
        guiGraphics.enableScissor(scissorX, scissorY, scissorX + scissorWidth, scissorY + scissorHeight);

        // Appeler la méthode parente pour dessiner la liste
        //this.renderListItems(guiGraphics, mouseX, mouseY, partialTick);
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);

        // Désactiver le Scissor Test après le rendu
        guiGraphics.disableScissor();
    }

    @Override
    protected void renderListBackground(GuiGraphics guiGraphics) {
        // Laisse vide pour ne pas dessiner de fond par défaut
    }

    @Override
    protected void renderListSeparators(GuiGraphics guiGraphics) {
        // Laisse vide pour ne pas dessiner de séparateurs
    }

    // Ajuste la largeur des éléments (pour les aligner avec la scrollbar)
    @Override
    public int getRowWidth() {
        return this.getWidth() - 14; // Largeur de la liste moins un décalage pour la scrollbar
    }

    protected int getRowHeight() {
        return 18; // Hauteur de chaque item
    }

    // Ajuste la position de la scrollbar (pour être alignée à droite)
    @Override
    protected int getScrollbarPosition() {
        return this.getX() + this.width - 6; // Scrollbar 6 pixels à l'intérieur du bord droit
    }

    // Implémentation de la méthode obligatoire pour l'accessibilité
    @Override
    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        // Implémentation vide pour l'instant
        // Si nécessaire, ajoute une description comme : narrationElementOutput.add(NarratedElementType.TITLE, "Description ici");
    }

    // Gestionnaire pour dessiner chaque entrée
    protected class Entry extends AbstractSelectionList.Entry<Entry> {
        private final String text;

        // Constructeur de l'entrée avec un texte à afficher
        public Entry(String text) {
            this.text = text;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int y, int x, int itemWidth, int itemHeight, int mouseX, int mouseY, boolean isSelected, float partialTick) {
            // Dessine un fond pour chaque élément (facultatif)
            guiGraphics.fill(x + 1, y + 1, x + itemWidth - 1, y + 18, ARGBToInt.ARGBToInt(128, 17, 17, 44));
            /*guiGraphics.fill(x + 2, y + 2, x + 2 + 16 - 1, y + 2 + 16 - 1, ARGBToInt.ARGBToInt(255, 128, 128, 128));
            guiGraphics.fill(x + 2 + 16 + 1, y + 2, x + 2 + 16 + 1 + 16 - 1, y + 2 + 16 - 1, ARGBToInt.ARGBToInt(255, 128, 128, 128));*/

            try {
                UUID playerUUID = UUID.fromString(this.text.split(":")[0]);
                String rank = this.text.split(":")[1];
                ResourceLocation ranktexture = ResourceLocation.parse("erinium_faction:textures/screens/" + rank + ".png");
                String Playername = GetFileStringValueProcedure.execute(UuidFileProcedure.execute(String.valueOf(playerUUID)), "displayname");

                int headX = x + 1;
                int headY = y + 1;

                ItemStack head = headCache.get(playerUUID);
                if (head == null) {
                    // Placeholder immédiat
                    head = new ItemStack(Items.PLAYER_HEAD);
                    headCache.put(playerUUID, head);

                    // Déclenche une résolution asynchrone (offline OK) une seule fois
                    headFutures.computeIfAbsent(playerUUID, id -> EFUtils.Head.resolveProfileByUUID(id).thenApply(rp -> {
                        ItemStack s = new ItemStack(Items.PLAYER_HEAD);
                        s.set(DataComponents.PROFILE, rp);
                        return s;
                    }).whenComplete((s, err) -> {
                        if (s != null) headCache.put(playerUUID, s);
                        headFutures.remove(playerUUID);
                    }));
                }

                guiGraphics.renderItem(head, headX, headY);

                int rankx = x + 2 + 16 + 1;
                int ranky = y + 2;

                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(rankx, ranky, 0);
                float scaler = 15f / 128f;
                guiGraphics.pose().scale(scaler, scaler, 1f);
                guiGraphics.blit(ranktexture, 0, 0, 0, 0, 128, 128, 128, 128);
                guiGraphics.pose().popPose();

                guiGraphics.pose().pushPose();
                int textX = x + 2 + 16 + 1 + 16;
                int textY = y + 9 - 3;
                guiGraphics.pose().translate(textX, textY, 0);
                if (Minecraft.getInstance().font.width(Playername) > 82) {
                    float scalertext = 82f / Minecraft.getInstance().font.width(Playername);
                    guiGraphics.pose().scale(scalertext, scalertext, 1f);
                }
                guiGraphics.drawString(Minecraft.getInstance().font, Playername, 0, 0, ARGBToInt.ARGBToInt(255, 255, 255, 255));
                guiGraphics.pose().popPose();

            } catch (Exception e) {
                // Si ce n'est pas un UUID, affiche une tête par défaut
                guiGraphics.renderItem(new ItemStack(Items.PLAYER_HEAD), x + 2, y + 2);
            }

            // Dessine le texte, avec un léger décalage vers la droite
            //guiGraphics.drawString(Minecraft.getInstance().font, this.text, x + 10, y + 5, 0xFFFFFF);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            System.out.println("Cliqué sur : " + this.text);

            this.setFocused(false);
            return false;
        }
    }
}