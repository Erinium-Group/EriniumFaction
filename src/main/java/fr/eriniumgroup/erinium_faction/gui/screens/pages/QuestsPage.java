package fr.eriniumgroup.erinium_faction.gui.screens.pages;

import fr.eriniumgroup.erinium_faction.gui.screens.components.ScrollList;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

/**
 * Page Quests - Basée sur quests.svg
 * Quêtes quotidiennes (6) et hebdomadaires (1) avec scroll list
 */
public class QuestsPage extends FactionPage {

    private ScrollList<QuestInfo> questScrollList;

    private static class QuestInfo {
        String name;
        String description;
        int progress; // 0-100
        boolean isWeekly;
        String resetTime;

        QuestInfo(String name, String description, int progress, boolean isWeekly, String resetTime) {
            this.name = name;
            this.description = description;
            this.progress = progress;
            this.isWeekly = isWeekly;
            this.resetTime = resetTime;
        }
    }

    public QuestsPage(Font font) {
        super(font);
    }

    private void initComponents(int leftPos, int topPos, double scaleX, double scaleY) {
        if (questScrollList == null) {
            // Augmenté à 60 pour mieux espacer les textes
            questScrollList = new ScrollList<>(font, this::renderQuestItem, sh(60, scaleY));

            List<QuestInfo> quests = new ArrayList<>();

            // 6 daily quests
            quests.add(new QuestInfo("{{QUEST_1_NAME}}", "{{QUEST_1_DESC}}", 50, false, "{{DAILY_RESET_TIME}}"));
            quests.add(new QuestInfo("{{QUEST_2_NAME}}", "{{QUEST_2_DESC}}", 75, false, "{{DAILY_RESET_TIME}}"));
            quests.add(new QuestInfo("{{QUEST_3_NAME}}", "{{QUEST_3_DESC}}", 30, false, "{{DAILY_RESET_TIME}}"));
            quests.add(new QuestInfo("{{QUEST_4_NAME}}", "{{QUEST_4_DESC}}", 90, false, "{{DAILY_RESET_TIME}}"));
            quests.add(new QuestInfo("{{QUEST_5_NAME}}", "{{QUEST_5_DESC}}", 10, false, "{{DAILY_RESET_TIME}}"));
            quests.add(new QuestInfo("{{QUEST_6_NAME}}", "{{QUEST_6_DESC}}", 65, false, "{{DAILY_RESET_TIME}}"));

            // 1 weekly quest
            quests.add(new QuestInfo("{{WEEKLY_QUEST_1_NAME}}", "{{WEEKLY_QUEST_1_DESC}}", 40, true, "{{WEEKLY_RESET_TIME}}"));

            questScrollList.setItems(quests);
        }

        int x = sx(CONTENT_X, leftPos, scaleX);
        int y = sy(CONTENT_Y, topPos, scaleY);
        int w = sw(CONTENT_W, scaleX);
        int h = sh(CONTENT_H, scaleY);

        questScrollList.setBounds(x, y + sh(27, scaleY), w, h - sh(27, scaleY));
    }

    private void renderQuestItem(GuiGraphics g, QuestInfo quest, int x, int y, int width, int height, boolean hovered, Font font) {
        int bgColor = hovered ? 0x40667eea : 0xE61e1e2e;
        int borderColor = quest.isWeekly ? 0xFFfbbf24 : 0xFF00d2ff;
        int progressColor = quest.isWeekly ? 0xFFfbbf24 : 0xFF00d2ff;

        g.fill(x, y, x + width, y + height, bgColor);
        g.fill(x, y, x + width, y + 1, borderColor);

        // Quest name (en haut)
        g.drawString(font, quest.name, x + 6, y + 4, 0xFFffffff, true);

        // Description (mieux espacée)
        g.drawString(font, quest.description, x + 6, y + 17, 0xFFa0a0c0, false);

        // Progress bar (plus bas)
        int barX = x + 6;
        int barY = y + 32;
        int barW = width - 12;
        g.fill(barX, barY, barX + barW, barY + 8, 0xFF2a2a3e);
        g.fill(barX, barY, barX + (barW * quest.progress / 100), barY + 8, progressColor);

        // Progress text and reset time (sous la barre)
        g.drawString(font, quest.progress + "%", x + 6, y + 44, progressColor, false);
        String resetText = "Resets: " + quest.resetTime;
        g.drawString(font, resetText, x + width - font.width(resetText) - 6, y + 44, 0xFF6a6a7e, false);
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
        g.drawString(font, "FACTION QUESTS", x + sw(9, scaleX), y + sh(9, scaleY), 0xFFffffff, true);
        g.drawString(font, "6 Daily + 1 Weekly", x + w - sw(78, scaleX), y + sh(9, scaleY), 0xFF00d2ff, false);

        questScrollList.render(g, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button, int leftPos, int topPos, double scaleX, double scaleY) {
        if (questScrollList == null) return false;
        return questScrollList.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button, int leftPos, int topPos, double scaleX, double scaleY) {
        if (questScrollList == null) return false;
        return questScrollList.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY, int leftPos, int topPos, double scaleX, double scaleY) {
        if (questScrollList == null) return false;
        return questScrollList.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY, int leftPos, int topPos, double scaleX, double scaleY) {
        if (questScrollList == null) return false;
        return questScrollList.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
}
