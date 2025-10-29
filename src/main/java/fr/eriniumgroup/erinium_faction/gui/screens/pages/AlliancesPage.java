package fr.eriniumgroup.erinium_faction.gui.screens.pages;

import fr.eriniumgroup.erinium_faction.common.network.packets.FactionActionPacket;
import fr.eriniumgroup.erinium_faction.core.EFC;
import fr.eriniumgroup.erinium_faction.gui.screens.components.*;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

/**
 * Page Alliances - Basée sur alliances.svg
 * Liste scrollable des alliances
 */
public class AlliancesPage extends FactionPage {

    private ScrollList<AllianceInfo> allianceScrollList;
    private final List<StyledButton> actionButtons = new ArrayList<>();
    private AddAlliancePopup addAlliancePopup;
    private AllyRequestsPopup allyRequestsPopup;

    // Textures pour les alliance cards
    private static final ResourceLocation ALLIANCE_CARD_NORMAL = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/alliances/alliance-card-normal.png");
    private static final ResourceLocation ALLIANCE_CARD_HOVER = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/alliances/alliance-card-hover.png");

    // Textures pour les boutons d'action des alliances
    private static final ResourceLocation BUTTON_ADD_ALLIANCE_NORMAL = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/alliances/button-add-alliance-normal.png");
    private static final ResourceLocation BUTTON_ADD_ALLIANCE_HOVER = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/alliances/button-add-alliance-hover.png");
    private static final ResourceLocation BUTTON_REMOVE_NORMAL = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/alliances/button-remove-normal.png");
    private static final ResourceLocation BUTTON_REMOVE_HOVER = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/alliances/button-remove-hover.png");

    private static class AllianceInfo {
        String id;
        String name;
        int memberCount;

        AllianceInfo(String id, String name, int memberCount) {
            this.id = id;
            this.name = name;
            this.memberCount = memberCount;
        }
    }

    public AlliancesPage(Font font) {
        super(font);
        // Initialiser les popups
        addAlliancePopup = new AddAlliancePopup(font);
        allyRequestsPopup = new AllyRequestsPopup(font);
    }

    private void initComponents(int leftPos, int topPos, double scaleX, double scaleY) {
        if (allianceScrollList == null) {
            allianceScrollList = new ScrollList<>(font, this::renderAllianceItem, sh(45, scaleY));
        }

        // Mettre à jour la liste avec les vraies données
        var data = getFactionData();
        List<AllianceInfo> alliances = new ArrayList<>();

        if (data != null && data.allies != null && !data.allies.isEmpty()) {
            for (String allyId : data.allies) {
                // Le memberCount n'est pas disponible pour les allies, utiliser une valeur par défaut
                // TODO: Récupérer les vraies données depuis le serveur
                alliances.add(new AllianceInfo(allyId, allyId, 0));
            }
        }

        if (alliances.isEmpty()) {
            // Message si aucune alliance
            alliances.add(new AllianceInfo("", "No alliances", 0));
        }

        allianceScrollList.setItems(alliances);

        int x = sx(CONTENT_X, leftPos, scaleX);
        int y = sy(CONTENT_Y, topPos, scaleY);
        int w = sw(CONTENT_W, scaleX);
        int h = sh(CONTENT_H, scaleY);

        allianceScrollList.setBounds(x, y + sh(55, scaleY), w, h - sh(55, scaleY));

        // Créer les boutons si nécessaire
        if (actionButtons.isEmpty()) {
            // Bouton "Add Alliance"
            StyledButton addButton = new StyledButton(font, translate("erinium_faction.gui.alliances.add"), () -> {
                // Ouvrir le popup
                if (addAlliancePopup != null) {
                    // Demander la liste des factions au serveur
                    net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                        new fr.eriniumgroup.erinium_faction.common.network.packets.FactionListRequestMessage()
                    );

                    // Charger les données depuis le cache (elles seront vides au premier clic, puis remplies après la réponse)
                    List<String> factionIds = FactionListCache.getFactionIds();
                    List<String> factionNames = FactionListCache.getFactionNames();
                    List<Integer> memberCounts = FactionListCache.getMemberCounts();

                    addAlliancePopup.loadFactions(factionIds, factionNames, memberCounts);

                    var mc = net.minecraft.client.Minecraft.getInstance();
                    addAlliancePopup.open(mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
                }
            });
            addButton.setPrimary(true);
            actionButtons.add(addButton);

            // Bouton "Requests" (n'apparaît que s'il y a des demandes)
            StyledButton requestsButton = new StyledButton(font, "", () -> {
                if (allyRequestsPopup != null && data != null) {
                    // Demander la validation des demandes au serveur
                    net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                        new fr.eriniumgroup.erinium_faction.common.network.packets.ValidateAllyRequestsMessage()
                    );

                    // Charger les données depuis le cache (elles seront remplies par la réponse du serveur)
                    List<String> requestIds = AllyRequestsCache.getRequestIds();
                    List<String> requestNames = AllyRequestsCache.getRequestNames();
                    List<Integer> memberCounts = new ArrayList<>();
                    for (int i = 0; i < requestIds.size(); i++) {
                        memberCounts.add(0);
                    }
                    allyRequestsPopup.loadRequests(requestIds, requestNames, memberCounts);

                    var mc = net.minecraft.client.Minecraft.getInstance();
                    allyRequestsPopup.open(mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
                }
            });
            requestsButton.setPrimary(false);
            actionButtons.add(requestsButton);
        }

        // Mettre à jour les positions et le texte des boutons
        actionButtons.get(0).setBounds(x, y + sh(27, scaleY), sw(85, scaleX), sh(20, scaleY));

        // Mettre à jour le bouton requests avec le nombre de demandes
        int requestCount = data != null && data.allyRequests != null ? data.allyRequests.size() : 0;
        actionButtons.get(1).setText(translate("erinium_faction.gui.alliances.requests", requestCount));
        actionButtons.get(1).setBounds(x + sw(90, scaleX), y + sh(27, scaleY), sw(95, scaleX), sh(20, scaleY));
        actionButtons.get(1).setEnabled(requestCount > 0); // Actif seulement s'il y a des demandes
    }

    private void renderAllianceItem(GuiGraphics g, AllianceInfo alliance, int x, int y, int width, int height, boolean hovered, Font font, int mouseX, int mouseY) {
        // Utiliser les images au lieu de g.fill
        ResourceLocation cardTexture = hovered ? ALLIANCE_CARD_HOVER : ALLIANCE_CARD_NORMAL;
        ImageRenderer.renderScaledImage(g, cardTexture, x, y, width, height);

        // Auto-scroll alliance name on hover
        int maxNameWidth = width - 70; // Réduire pour laisser place au bouton
        boolean nameHovered = TextHelper.isPointInBounds(mouseX, mouseY, x + 9, y + 9, maxNameWidth, font.lineHeight);
        TextHelper.drawAutoScrollingText(g, font, alliance.name, x + 9, y + 9, maxNameWidth, 0xFF00d2ff, true, nameHovered, "alliance_" + alliance.name);
        g.drawString(font, translate("erinium_faction.gui.alliances.members", alliance.memberCount), x + 9, y + 22, 0xFFa0a0c0, false);

        // Bouton "Remove" si ce n'est pas le message "No alliances"
        if (!alliance.id.isEmpty()) {
            int buttonX = x + width - 60;
            int buttonY = y + 8;
            int buttonWidth = 50;
            int buttonHeight = 20;

            boolean buttonHovered = mouseX >= buttonX && mouseX < buttonX + buttonWidth &&
                                   mouseY >= buttonY && mouseY < buttonY + buttonHeight;

            ResourceLocation buttonTexture = buttonHovered ? BUTTON_REMOVE_HOVER : BUTTON_REMOVE_NORMAL;
            ImageRenderer.renderScaledImage(g, buttonTexture, buttonX, buttonY, buttonWidth, buttonHeight);

            String removeText = "Remove";
            int textWidth = font.width(removeText);
            g.drawString(font, removeText, buttonX + (buttonWidth - textWidth) / 2, buttonY + 6, 0xFFffffff, false);
        }
    }

    @Override
    public void render(GuiGraphics g, int leftPos, int topPos, double scaleX, double scaleY, int mouseX, int mouseY) {
        initComponents(leftPos, topPos, scaleX, scaleY);

        int x = sx(CONTENT_X, leftPos, scaleX);
        int y = sy(CONTENT_Y, topPos, scaleY);
        int w = sw(CONTENT_W, scaleX);

        // Header
        g.fill(x, y, x + w, y + sh(22, scaleY), 0xE61e1e2e);
        g.fill(x, y, x + w, y + 1, 0xFF00d2ff);
        g.drawString(font, translate("erinium_faction.gui.alliances.title"), x + sw(9, scaleX), y + sh(9, scaleY), 0xFFffffff, true);

        // Boutons d'action
        for (StyledButton button : actionButtons) {
            button.render(g, mouseX, mouseY);
        }

        // Liste des alliances
        allianceScrollList.render(g, mouseX, mouseY);

        // Popups (rendu par-dessus tout)
        if (addAlliancePopup != null) {
            addAlliancePopup.render(g, mouseX, mouseY);
        }
        if (allyRequestsPopup != null) {
            allyRequestsPopup.render(g, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button, int leftPos, int topPos, double scaleX, double scaleY) {
        // Priorité aux popups
        if (addAlliancePopup != null && addAlliancePopup.isVisible()) {
            return addAlliancePopup.mouseClicked(mouseX, mouseY, button);
        }
        if (allyRequestsPopup != null && allyRequestsPopup.isVisible()) {
            return allyRequestsPopup.mouseClicked(mouseX, mouseY, button);
        }

        // Boutons d'action
        for (StyledButton btn : actionButtons) {
            if (btn.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }

        // Vérifier les clics sur les boutons "Remove" dans la liste
        int x = sx(CONTENT_X, leftPos, scaleX);
        int y = sy(CONTENT_Y, topPos, scaleY);
        int w = sw(CONTENT_W, scaleX);

        if (allianceScrollList != null) {
            var data = getFactionData();
            if (data != null && data.allies != null) {
                int itemHeight = sh(45, scaleY);
                int listY = y + sh(55, scaleY);

                for (int i = 0; i < allianceScrollList.getItems().size(); i++) {
                    AllianceInfo alliance = allianceScrollList.getItems().get(i);
                    if (alliance.id.isEmpty()) continue; // Skip "No alliances"

                    // TODO: Calculer correctement la position de l'item en tenant compte du scroll
                    int itemY = listY + i * itemHeight;
                    if (itemY < listY || itemY >= listY + sh(CONTENT_H - 55, scaleY)) continue;

                    int buttonX = x + w - 60;
                    int buttonY = itemY + 8;
                    int buttonWidth = 50;
                    int buttonHeight = 20;

                    if (mouseX >= buttonX && mouseX < buttonX + buttonWidth &&
                        mouseY >= buttonY && mouseY < buttonY + buttonHeight) {
                        // Clic sur le bouton Remove
                        EFC.log.info("§6Alliance", "§cRemoving alliance with §e{}", alliance.name);
                        PacketDistributor.sendToServer(new FactionActionPacket(
                            FactionActionPacket.ActionType.REMOVE_ALLIANCE,
                            alliance.id,
                            ""
                        ));
                        return true;
                    }
                }
            }

            return allianceScrollList.mouseClicked(mouseX, mouseY, button);
        }

        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button, int leftPos, int topPos, double scaleX, double scaleY) {
        if (addAlliancePopup != null && addAlliancePopup.isVisible()) {
            return addAlliancePopup.mouseReleased(mouseX, mouseY, button);
        }
        if (allyRequestsPopup != null && allyRequestsPopup.isVisible()) {
            return allyRequestsPopup.mouseReleased(mouseX, mouseY, button);
        }
        if (allianceScrollList == null) return false;
        return allianceScrollList.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY, int leftPos, int topPos, double scaleX, double scaleY) {
        if (addAlliancePopup != null && addAlliancePopup.isVisible()) {
            return addAlliancePopup.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }
        if (allyRequestsPopup != null && allyRequestsPopup.isVisible()) {
            return allyRequestsPopup.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }
        if (allianceScrollList == null) return false;
        return allianceScrollList.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY, int leftPos, int topPos, double scaleX, double scaleY) {
        if (addAlliancePopup != null && addAlliancePopup.isVisible()) {
            return addAlliancePopup.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        if (allyRequestsPopup != null && allyRequestsPopup.isVisible()) {
            return allyRequestsPopup.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        if (allianceScrollList == null) return false;
        return allianceScrollList.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers, int leftPos, int topPos, double scaleX, double scaleY) {
        if (addAlliancePopup != null && addAlliancePopup.isVisible()) {
            addAlliancePopup.keyPressed(keyCode, scanCode, modifiers);
            return true; // Toujours consommer l'événement si le popup est visible
        }
        if (allyRequestsPopup != null && allyRequestsPopup.isVisible()) {
            allyRequestsPopup.keyPressed(keyCode, scanCode, modifiers);
            return true; // Toujours consommer l'événement si le popup est visible
        }
        return false;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers, int leftPos, int topPos, double scaleX, double scaleY) {
        if (addAlliancePopup != null && addAlliancePopup.isVisible()) {
            addAlliancePopup.charTyped(codePoint, modifiers);
            return true; // Toujours consommer l'événement si le popup est visible
        }
        if (allyRequestsPopup != null && allyRequestsPopup.isVisible()) {
            allyRequestsPopup.charTyped(codePoint, modifiers);
            return true; // Toujours consommer l'événement si le popup est visible
        }
        return false;
    }
}
