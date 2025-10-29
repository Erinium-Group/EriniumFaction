package fr.eriniumgroup.erinium_faction.gui.screens.components;

import fr.eriniumgroup.erinium_faction.common.network.packets.FactionActionPacket;
import fr.eriniumgroup.erinium_faction.core.EFC;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Popup pour rechercher et envoyer une demande d'alliance à une faction
 */
public class AddAlliancePopup extends Popup {
    private StyledTextField searchField;
    private ScrollList<FactionEntry> factionList;
    private final List<FactionEntry> allFactions = new ArrayList<>();
    private final List<FactionEntry> filteredFactions = new ArrayList<>();

    private static class FactionEntry {
        String id;
        String name;
        int memberCount;

        FactionEntry(String id, String name, int memberCount) {
            this.id = id;
            this.name = name;
            this.memberCount = memberCount;
        }
    }

    public AddAlliancePopup(Font font) {
        super(font, BASE_WIDTH, BASE_HEIGHT);
    }

    /**
     * Charge la liste des factions disponibles
     * TODO: Cette méthode devra être appelée avec les vraies données du serveur
     */
    public void loadFactions(List<String> factionIds, List<String> factionNames, List<Integer> memberCounts) {
        allFactions.clear();
        for (int i = 0; i < factionIds.size() && i < factionNames.size(); i++) {
            int count = i < memberCounts.size() ? memberCounts.get(i) : 0;
            allFactions.add(new FactionEntry(factionIds.get(i), factionNames.get(i), count));
        }
        updateFilter();
    }

    @Override
    protected void onOpen() {
        // Initialiser le champ de recherche
        if (searchField == null) {
            searchField = new StyledTextField(font);
            searchField.setPlaceholder("Search factions...");
            searchField.setOnChange(text -> updateFilter());
        }
        searchField.setBounds(x + sw(10), y + sh(34), width - sw(20), sh(20));

        // Initialiser la liste de factions
        if (factionList == null) {
            factionList = new ScrollList<>(font, this::renderFactionEntry, sh(40));
            factionList.setOnItemClick(this::sendAllianceRequest);
        }
        factionList.setBounds(x + sw(10), y + sh(64), width - sw(20), height - sh(74));

        updateFilter();
    }

    @Override
    protected void onClose() {
        if (searchField != null) {
            searchField.setText("");
        }
    }

    private void updateFilter() {
        String search = searchField != null ? searchField.getText().toLowerCase(Locale.ROOT) : "";
        filteredFactions.clear();

        for (FactionEntry faction : allFactions) {
            if (search.isEmpty() || faction.name.toLowerCase(Locale.ROOT).contains(search)) {
                filteredFactions.add(faction);
            }
        }

        if (factionList != null) {
            factionList.setItems(new ArrayList<>(filteredFactions));
        }
    }

    private void renderFactionEntry(GuiGraphics g, FactionEntry faction, int x, int y, int width, int height, boolean hovered, Font font, int mouseX, int mouseY) {
        // Fond
        int bgColor = hovered ? 0x60667eea : 0xE61e1e2e;
        g.fill(x, y, x + width, y + height, bgColor);
        g.fill(x, y, x + width, y + 1, 0x50667eea);

        // Nom de la faction
        int maxNameWidth = width - 16;
        TextHelper.drawScaledText(g, font, faction.name, x + 8, y + 8, maxNameWidth, 0xFF00d2ff, true);

        // Nombre de membres
        String members = faction.memberCount + " members";
        g.drawString(font, members, x + 8, y + 22, 0xFFa0a0c0, false);
    }

    private void sendAllianceRequest(FactionEntry faction) {
        EFC.log.info("§6Alliance", "§aSending alliance request to §e{}", faction.name);

        // Envoyer la demande au serveur
        PacketDistributor.sendToServer(new FactionActionPacket(
            FactionActionPacket.ActionType.REQUEST_ALLIANCE,
            faction.id,
            ""
        ));

        // Fermer le popup
        close();
    }

    @Override
    protected String getTitle() {
        return "Add Alliance";
    }

    @Override
    protected void renderContent(GuiGraphics g, int mouseX, int mouseY) {
        // Mettre à jour les positions des composants à chaque frame
        if (searchField != null) {
            searchField.setBounds(x + sw(10), y + sh(34), width - sw(20), sh(20));
            searchField.render(g, mouseX, mouseY);
        }

        // Liste des factions
        if (factionList != null) {
            factionList.setBounds(x + sw(10), y + sh(64), width - sw(20), height - sh(74));
            factionList.render(g, mouseX, mouseY);
        }

        // Message si aucune faction trouvée
        if (filteredFactions.isEmpty()) {
            String noResults = "No factions found";
            int textWidth = font.width(noResults);
            g.drawString(font, noResults, x + (width - textWidth) / 2, y + sh(100), 0xFF808080, false);
        }
    }

    @Override
    protected boolean handleMouseClick(double mouseX, double mouseY, int button) {
        if (searchField != null && searchField.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (factionList != null && factionList.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        return false;
    }

    @Override
    protected boolean handleMouseRelease(double mouseX, double mouseY, int button) {
        if (factionList != null) {
            return factionList.mouseReleased(mouseX, mouseY, button);
        }
        return false;
    }

    @Override
    protected boolean handleMouseDrag(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (factionList != null) {
            return factionList.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }
        return false;
    }

    @Override
    protected boolean handleMouseScroll(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (factionList != null) {
            return factionList.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        return false;
    }

    @Override
    protected boolean handleKeyPress(int keyCode, int scanCode, int modifiers) {
        if (searchField != null) {
            return searchField.keyPressed(keyCode, scanCode, modifiers);
        }
        return false;
    }

    @Override
    protected boolean handleCharTyped(char codePoint, int modifiers) {
        if (searchField != null) {
            return searchField.charTyped(codePoint, modifiers);
        }
        return false;
    }

    // Les positions sont maintenant recalculées automatiquement à chaque frame dans renderContent()
}
