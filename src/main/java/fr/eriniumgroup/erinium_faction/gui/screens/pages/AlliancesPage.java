package fr.eriniumgroup.erinium_faction.gui.screens.pages;

import fr.eriniumgroup.erinium_faction.gui.screens.components.ScrollList;
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

            List<AllianceInfo> alliances = new ArrayList<>();
            alliances.add(new AllianceInfo("{{ALLIANCE_1_NAME}}", 10));
            alliances.add(new AllianceInfo("{{ALLIANCE_2_NAME}}", 8));
            alliances.add(new AllianceInfo("{{ALLIANCE_3_NAME}}", 15));
            // Exemples
            for (int i = 0; i < 5; i++) {
                alliances.add(new AllianceInfo("Alliance Example " + (i + 1), 5 + i * 2));
            }

            allianceScrollList.setItems(alliances);
            allianceScrollList.setOnItemClick(alliance -> {
                System.out.println("AlliancesPage: Clicked on alliance " + alliance.name);
            });
        }

        int x = sx(CONTENT_X, leftPos, scaleX);
        int y = sy(CONTENT_Y, topPos, scaleY);
        int w = sw(CONTENT_W, scaleX);
        int h = sh(CONTENT_H, scaleY);

        allianceScrollList.setBounds(x, y + sh(27, scaleY), w, h - sh(27, scaleY));
    }

    private void renderAllianceItem(GuiGraphics g, AllianceInfo alliance, int x, int y, int width, int height, boolean hovered, Font font) {
        int bgColor = hovered ? 0x40667eea : 0xE61e1e2e;
        g.fill(x, y, x + width, y + height, bgColor);
        g.fill(x, y, x + width, y + 1, 0x8000d2ff);

        g.drawString(font, alliance.name, x + 9, y + 9, 0xFF00d2ff, true);
        g.drawString(font, "Members: " + alliance.memberCount, x + 9, y + 19, 0xFFa0a0c0, false);
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
        g.drawString(font, "ALLIED FACTIONS", x + sw(9, scaleX), y + sh(9, scaleY), 0xFFffffff, true);

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
