package fr.eriniumgroup.erinium_faction.gui.screens.pages;

import fr.eriniumgroup.erinium_faction.gui.screens.components.ScrollList;
import fr.eriniumgroup.erinium_faction.gui.screens.components.StyledButton;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

/**
 * Page Overview - Basée sur overview.svg
 * Affiche les stats principales et infos de la faction avec scroll list
 */
public class OverviewPage extends FactionPage {

    private ScrollList<InfoItem> infoScrollList;
    private final List<StyledButton> actionButtons = new ArrayList<>();

    // Classe pour les items de la liste
    private static class InfoItem {
        String label;
        String value;
        int color;

        InfoItem(String label, String value, int color) {
            this.label = label;
            this.value = value;
            this.color = color;
        }
    }

    public OverviewPage(Font font) {
        super(font);
    }

    private void initComponents(int leftPos, int topPos, double scaleX, double scaleY) {
        if (infoScrollList == null) {
            // Créer scroll list pour infos
            infoScrollList = new ScrollList<>(font, this::renderInfoItem, sh(13, scaleY));

            // Ajouter des items d'exemple
            List<InfoItem> items = new ArrayList<>();
            items.add(new InfoItem("Name:", "{{FACTION_NAME}}", 0xFF00d2ff));
            items.add(new InfoItem("Leader:", "{{LEADER_NAME}}", 0xFFa855f7));
            items.add(new InfoItem("Created:", "{{CREATION_DATE}}", 0xFFffffff));
            items.add(new InfoItem("Members:", "{{MEMBER_COUNT}}", 0xFF3b82f6));
            items.add(new InfoItem("Claims:", "{{CLAIM_COUNT}}", 0xFFa855f7));
            items.add(new InfoItem("Kills:", "{{KILL_COUNT}}", 0xFFef4444));
            items.add(new InfoItem("Description:", "{{FACTION_DESCRIPTION}}", 0xFFb8b8d0));
            items.add(new InfoItem("Recent Activity:", "{{RECENT_ACTIVITY_TEXT}}", 0xFF00d2ff));

            infoScrollList.setItems(items);

            // Créer boutons d'action
            actionButtons.clear();

            StyledButton inviteBtn = new StyledButton(font, "Invite", () -> {
                System.out.println("OverviewPage: Invite button clicked");
            });
            inviteBtn.setPrimary(true);
            actionButtons.add(inviteBtn);

            StyledButton manageBtn = new StyledButton(font, "Manage", () -> {
                System.out.println("OverviewPage: Manage button clicked");
            });
            actionButtons.add(manageBtn);

            StyledButton leaveBtn = new StyledButton(font, "Leave", () -> {
                System.out.println("OverviewPage: Leave button clicked");
            });
            actionButtons.add(leaveBtn);
        }

        // Update positions (scaled for 400x270)
        int x = sx(CONTENT_X, leftPos, scaleX);
        int y = sy(CONTENT_Y, topPos, scaleY);
        int w = sw(CONTENT_W, scaleX);

        // Stats cards: 3 cards side by side
        // Info scroll list below cards
        // Buttons at bottom

        infoScrollList.setBounds(x, y + sh(62, scaleY), w, sh(130, scaleY));

        // Position buttons
        int btnY = y + sh(195, scaleY);
        int btnW = sw(88, scaleX);
        int btnH = sh(17, scaleY);
        int btnSpacing = sw(6, scaleX);

        for (int i = 0; i < actionButtons.size(); i++) {
            int btnX = x + i * (btnW + btnSpacing);
            actionButtons.get(i).setBounds(btnX, btnY, btnW, btnH);
        }
    }

    private void renderInfoItem(GuiGraphics g, InfoItem item, int x, int y, int width, int height, boolean hovered, Font font) {
        if (hovered) {
            g.fill(x, y, x + width, y + height, 0x40667eea);
        }

        g.drawString(font, item.label, x + 4, y + 2, 0xFFa0a0c0, false);
        // Décaler les valeurs plus à droite pour éviter chevauchement
        g.drawString(font, item.value, x + 80, y + 2, item.color, item.label.equals("Name:"));
    }

    @Override
    public void render(GuiGraphics g, int leftPos, int topPos, double scaleX, double scaleY, int mouseX, int mouseY) {
        initComponents(leftPos, topPos, scaleX, scaleY);

        int x = sx(CONTENT_X, leftPos, scaleX);
        int y = sy(CONTENT_Y, topPos, scaleY);
        int w = sw(CONTENT_W, scaleX);

        // Stats cards (3 cards: Members, Claims, Kills) - scaled for 400x270
        renderStatCard(g, x, y, "MEMBERS", "{{MEMBER_COUNT}}", 0xFF3b82f6, scaleX, scaleY);
        renderStatCard(g, x + sw(92, scaleX), y, "CLAIMS", "{{CLAIM_COUNT}}", 0xFFa855f7, scaleX, scaleY);
        renderStatCard(g, x + sw(184, scaleX), y, "KILLS", "{{KILL_COUNT}}", 0xFFef4444, scaleX, scaleY);

        // Info scroll list
        infoScrollList.render(g, mouseX, mouseY);

        // Action buttons
        for (StyledButton btn : actionButtons) {
            btn.render(g, mouseX, mouseY);
        }
    }

    private void renderStatCard(GuiGraphics g, int x, int y, String label, String value, int color, double scaleX, double scaleY) {
        int w = sw(88, scaleX);
        int h = sh(50, scaleY);

        g.fill(x, y, x + w, y + h, 0xE61e1e2e);
        g.fill(x, y, x + w, y + 1, color & 0x80FFFFFF);

        g.drawString(font, label, x + sw(9, scaleX), y + sh(26, scaleY), 0xFFa0a0c0, false);
        g.drawString(font, value, x + sw(9, scaleX), y + sh(37, scaleY), color, true);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button, int leftPos, int topPos, double scaleX, double scaleY) {
        // Check buttons
        for (StyledButton btn : actionButtons) {
            if (btn.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }

        // Check scroll list
        if (infoScrollList == null) return false;
        return infoScrollList.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button, int leftPos, int topPos, double scaleX, double scaleY) {
        if (infoScrollList == null) return false;
        return infoScrollList.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY, int leftPos, int topPos, double scaleX, double scaleY) {
        if (infoScrollList == null) return false;
        return infoScrollList.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY, int leftPos, int topPos, double scaleX, double scaleY) {
        if (infoScrollList == null) return false;
        return infoScrollList.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
}
