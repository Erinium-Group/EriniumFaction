package fr.eriniumgroup.erinium_faction.gui.screens.pages;

import fr.eriniumgroup.erinium_faction.gui.screens.components.ScrollList;
import fr.eriniumgroup.erinium_faction.gui.screens.components.TextHelper;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

/**
 * Page Alliances - Basée sur alliances.svg
 * Liste scrollable des alliances
 */
public class AlliancesPage extends FactionPage {

    private ScrollList<AllianceInfo> allianceScrollList;

    private static class AllianceInfo {
        String name;
        int memberCount;

        AllianceInfo(String name, int memberCount) {
            this.name = name;
            this.memberCount = memberCount;
        }
    }

    public AlliancesPage(Font font) {
        super(font);
    }

    private void initComponents(int leftPos, int topPos, double scaleX, double scaleY) {
        if (allianceScrollList == null) {
            allianceScrollList = new ScrollList<>(font, this::renderAllianceItem, sh(37, scaleY));
        }

        // Mettre à jour la liste avec les vraies données
        var data = getFactionData();
        List<AllianceInfo> alliances = new ArrayList<>();

        if (data != null && data.allies != null && !data.allies.isEmpty()) {
            for (String allyName : data.allies) {
                // Le memberCount n'est pas disponible pour les allies, utiliser une valeur par défaut
                alliances.add(new AllianceInfo(allyName, 0));
            }
        }

        if (alliances.isEmpty()) {
            // Message si aucune alliance
            alliances.add(new AllianceInfo("No alliances", 0));
        }

        allianceScrollList.setItems(alliances);

        int x = sx(CONTENT_X, leftPos, scaleX);
        int y = sy(CONTENT_Y, topPos, scaleY);
        int w = sw(CONTENT_W, scaleX);
        int h = sh(CONTENT_H, scaleY);

        allianceScrollList.setBounds(x, y + sh(27, scaleY), w, h - sh(27, scaleY));
    }

    private void renderAllianceItem(GuiGraphics g, AllianceInfo alliance, int x, int y, int width, int height, boolean hovered, Font font, int mouseX, int mouseY) {
        int bgColor = hovered ? 0x40667eea : 0xE61e1e2e;
        g.fill(x, y, x + width, y + height, bgColor);
        g.fill(x, y, x + width, y + 1, 0x8000d2ff);

        // Auto-scroll alliance name on hover
        int maxNameWidth = width - 18;
        boolean nameHovered = TextHelper.isPointInBounds(mouseX, mouseY, x + 9, y + 9, maxNameWidth, font.lineHeight);
        TextHelper.drawAutoScrollingText(g, font, alliance.name, x + 9, y + 9, maxNameWidth, 0xFF00d2ff, true, nameHovered, "alliance_" + alliance.name);
        g.drawString(font, translate("erinium_faction.gui.alliances.members", alliance.memberCount), x + 9, y + 19, 0xFFa0a0c0, false);
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

        allianceScrollList.render(g, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button, int leftPos, int topPos, double scaleX, double scaleY) {
        if (allianceScrollList == null) return false;
        return allianceScrollList.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button, int leftPos, int topPos, double scaleX, double scaleY) {
        if (allianceScrollList == null) return false;
        return allianceScrollList.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY, int leftPos, int topPos, double scaleX, double scaleY) {
        if (allianceScrollList == null) return false;
        return allianceScrollList.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY, int leftPos, int topPos, double scaleX, double scaleY) {
        if (allianceScrollList == null) return false;
        return allianceScrollList.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
}
