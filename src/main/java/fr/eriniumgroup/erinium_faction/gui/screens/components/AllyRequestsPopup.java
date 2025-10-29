package fr.eriniumgroup.erinium_faction.gui.screens.components;

import fr.eriniumgroup.erinium_faction.common.network.packets.FactionActionPacket;
import fr.eriniumgroup.erinium_faction.core.EFC;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

/**
 * Popup pour accepter ou refuser les demandes d'alliance
 */
public class AllyRequestsPopup extends Popup {
    private ScrollList<AllyRequestEntry> requestList;
    private final List<AllyRequestEntry> requests = new ArrayList<>();

    // Textures pour les boutons
    private static final ResourceLocation BUTTON_ACCEPT = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/common/button-primary-normal.png");
    private static final ResourceLocation BUTTON_ACCEPT_HOVER = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/common/button-primary-hover.png");
    private static final ResourceLocation BUTTON_REFUSE = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/common/button-danger-normal.png");
    private static final ResourceLocation BUTTON_REFUSE_HOVER = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/common/button-danger-hover.png");

    private static class AllyRequestEntry {
        String id;
        String name;
        int memberCount;

        AllyRequestEntry(String id, String name, int memberCount) {
            this.id = id;
            this.name = name;
            this.memberCount = memberCount;
        }
    }

    public AllyRequestsPopup(Font font) {
        super(font, 320, 380);
    }

    /**
     * Charge la liste des demandes d'alliance
     */
    public void loadRequests(List<String> requestIds, List<String> requestNames, List<Integer> memberCounts) {
        requests.clear();
        for (int i = 0; i < requestIds.size() && i < requestNames.size(); i++) {
            int count = i < memberCounts.size() ? memberCounts.get(i) : 0;
            requests.add(new AllyRequestEntry(requestIds.get(i), requestNames.get(i), count));
        }

        if (requestList != null) {
            requestList.setItems(new ArrayList<>(requests));
        }
    }

    @Override
    protected void onOpen() {
        // Initialiser la liste des demandes
        if (requestList == null) {
            requestList = new ScrollList<>(font, this::renderRequestEntry, 60);
        }
        requestList.setBounds(x + 10, y + 34, width - 20, height - 44);
        requestList.setItems(new ArrayList<>(requests));
    }

    @Override
    protected void onClose() {
        // Rien à faire
    }

    private void renderRequestEntry(GuiGraphics g, AllyRequestEntry request, int x, int y, int width, int height, boolean hovered, Font font, int mouseX, int mouseY) {
        // Fond
        int bgColor = hovered ? 0x40667eea : 0xE61e1e2e;
        g.fill(x, y, x + width, y + height, bgColor);
        g.fill(x, y, x + width, y + 1, 0x50667eea);

        // Nom de la faction
        int maxNameWidth = width - 16;
        TextHelper.drawScaledText(g, font, request.name, x + 8, y + 6, maxNameWidth, 0xFF00d2ff, true);

        // Nombre de membres
        String members = request.memberCount + " members";
        g.drawString(font, members, x + 8, y + 18, 0xFFa0a0c0, false);

        // Boutons Accept / Refuse
        int buttonY = y + 34;
        int buttonWidth = 60;
        int buttonHeight = 18;
        int acceptX = x + width - 130;
        int refuseX = x + width - 65;

        // Bouton Accept
        boolean acceptHovered = mouseX >= acceptX && mouseX < acceptX + buttonWidth &&
                               mouseY >= buttonY && mouseY < buttonY + buttonHeight;
        ResourceLocation acceptTexture = acceptHovered ? BUTTON_ACCEPT_HOVER : BUTTON_ACCEPT;
        ImageRenderer.renderScaledImage(g, acceptTexture, acceptX, buttonY, buttonWidth, buttonHeight);
        int acceptTextWidth = font.width("Accept");
        g.drawString(font, "Accept", acceptX + (buttonWidth - acceptTextWidth) / 2, buttonY + 5, 0xFFffffff, false);

        // Bouton Refuse
        boolean refuseHovered = mouseX >= refuseX && mouseX < refuseX + buttonWidth &&
                               mouseY >= buttonY && mouseY < buttonY + buttonHeight;
        ResourceLocation refuseTexture = refuseHovered ? BUTTON_REFUSE_HOVER : BUTTON_REFUSE;
        ImageRenderer.renderScaledImage(g, refuseTexture, refuseX, buttonY, buttonWidth, buttonHeight);
        int refuseTextWidth = font.width("Refuse");
        g.drawString(font, "Refuse", refuseX + (buttonWidth - refuseTextWidth) / 2, buttonY + 5, 0xFFffffff, false);
    }

    @Override
    protected String getTitle() {
        return "Alliance Requests (" + requests.size() + ")";
    }

    @Override
    protected void renderContent(GuiGraphics g, int mouseX, int mouseY) {
        // Liste des demandes
        if (requestList != null) {
            requestList.render(g, mouseX, mouseY);
        }

        // Message si aucune demande
        if (requests.isEmpty()) {
            String noRequests = "No pending requests";
            int textWidth = font.width(noRequests);
            g.drawString(font, noRequests, x + (width - textWidth) / 2, y + 100, 0xFF808080, false);
        }
    }

    @Override
    protected boolean handleMouseClick(double mouseX, double mouseY, int button) {
        if (requestList == null) return false;

        // Vérifier les clics sur les boutons Accept/Refuse
        for (int i = 0; i < requests.size(); i++) {
            AllyRequestEntry request = requests.get(i);

            // Calculer la position de l'item dans la liste
            // TODO: Calculer correctement la position de l'item en tenant compte du scroll
            int itemY = y + 34 + i * 60;
            if (itemY < y + 34 || itemY >= y + height - 10) continue; // En dehors de la vue

            int buttonY = itemY + 34;
            int buttonWidth = 60;
            int buttonHeight = 18;
            int acceptX = x + 10 + width - 20 - 130;
            int refuseX = x + 10 + width - 20 - 65;

            // Clic sur Accept
            if (mouseX >= acceptX && mouseX < acceptX + buttonWidth &&
                mouseY >= buttonY && mouseY < buttonY + buttonHeight) {
                acceptRequest(request);
                return true;
            }

            // Clic sur Refuse
            if (mouseX >= refuseX && mouseX < refuseX + buttonWidth &&
                mouseY >= buttonY && mouseY < buttonY + buttonHeight) {
                refuseRequest(request);
                return true;
            }
        }

        return requestList.mouseClicked(mouseX, mouseY, button);
    }

    private void acceptRequest(AllyRequestEntry request) {
        EFC.log.info("§6Alliance", "§aAccepting alliance request from §e{}", request.name);

        // Envoyer l'acceptation au serveur
        PacketDistributor.sendToServer(new FactionActionPacket(
            FactionActionPacket.ActionType.ACCEPT_ALLIANCE,
            request.id,
            ""
        ));

        // Retirer de la liste locale
        requests.remove(request);
        if (requestList != null) {
            requestList.setItems(new ArrayList<>(requests));
        }

        // Fermer si plus de demandes
        if (requests.isEmpty()) {
            close();
        }
    }

    private void refuseRequest(AllyRequestEntry request) {
        EFC.log.info("§6Alliance", "§cRefusing alliance request from §e{}", request.name);

        // Envoyer le refus au serveur
        PacketDistributor.sendToServer(new FactionActionPacket(
            FactionActionPacket.ActionType.REFUSE_ALLIANCE,
            request.id,
            ""
        ));

        // Retirer de la liste locale
        requests.remove(request);
        if (requestList != null) {
            requestList.setItems(new ArrayList<>(requests));
        }

        // Fermer si plus de demandes
        if (requests.isEmpty()) {
            close();
        }
    }

    @Override
    protected boolean handleMouseRelease(double mouseX, double mouseY, int button) {
        if (requestList != null) {
            return requestList.mouseReleased(mouseX, mouseY, button);
        }
        return false;
    }

    @Override
    protected boolean handleMouseDrag(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (requestList != null) {
            return requestList.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }
        return false;
    }

    @Override
    protected boolean handleMouseScroll(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (requestList != null) {
            return requestList.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        return false;
    }

    @Override
    public void open(int screenWidth, int screenHeight) {
        super.open(screenWidth, screenHeight);
        // Mettre à jour les positions après le calcul du centre
        if (requestList != null) {
            requestList.setBounds(x + 10, y + 34, width - 20, height - 44);
        }
    }

    public int getRequestCount() {
        return requests.size();
    }
}
