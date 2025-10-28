package fr.eriniumgroup.erinium_faction.gui.screens.pages;

import fr.eriniumgroup.erinium_faction.gui.screens.components.ScrollList;
import fr.eriniumgroup.erinium_faction.gui.screens.components.TextHelper;
import fr.eriniumgroup.erinium_faction.gui.screens.components.ImageRenderer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * Page Quests - Basée sur quests.svg
 * Quêtes quotidiennes (6) et hebdomadaires (1) avec scroll list
 */
public class QuestsPage extends FactionPage {

    private ScrollList<QuestInfo> questScrollList;

    // Textures pour les quest cards
    private static final ResourceLocation QUEST_CARD_DAILY_NORMAL = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/quests/quest-card-daily-normal.png");
    private static final ResourceLocation QUEST_CARD_DAILY_HOVER = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/quests/quest-card-daily-hover.png");
    private static final ResourceLocation QUEST_CARD_DAILY_COMPLETED = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/quests/quest-card-daily-completed.png");
    private static final ResourceLocation QUEST_CARD_WEEKLY_NORMAL = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/quests/quest-card-weekly-normal.png");
    private static final ResourceLocation QUESTBAR_BLUE_EMPTY = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/quests/questbar-blue-empty.png");
    private static final ResourceLocation QUESTBAR_BLUE_FILLED = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/quests/questbar-blue-filled-100.png");
    private static final ResourceLocation QUESTBAR_SUCCESS_GREEN = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/quests/questbar-success-green-filled-100.png");

    // Textures pour les boutons claim
    private static final ResourceLocation BUTTON_CLAIM_NORMAL = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/quests/button-claim-normal.png");
    private static final ResourceLocation BUTTON_CLAIM_HOVER = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/quests/button-claim-hover.png");

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

    private void renderQuestItem(GuiGraphics g, QuestInfo quest, int x, int y, int width, int height, boolean hovered, Font font, int mouseX, int mouseY) {
        boolean isCompleted = quest.progress >= 100;
        int progressColor = isCompleted ? 0xFF10b981 : (quest.isWeekly ? 0xFFfbbf24 : 0xFF00d2ff);

        // Utiliser les images au lieu de g.fill
        ResourceLocation cardTexture;
        if (isCompleted && !quest.isWeekly) {
            cardTexture = QUEST_CARD_DAILY_COMPLETED;
        } else if (quest.isWeekly) {
            cardTexture = QUEST_CARD_WEEKLY_NORMAL;
        } else {
            cardTexture = hovered ? QUEST_CARD_DAILY_HOVER : QUEST_CARD_DAILY_NORMAL;
        }
        ImageRenderer.renderScaledImage(g, cardTexture, x, y, width, height);

        // Calculate max width for text
        int maxTextWidth = width - 12;

        // Quest name with scaling (keep for fitting)
        TextHelper.drawScaledText(g, font, quest.name, x + 6, y + 4, maxTextWidth, 0xFFffffff, true);

        // Description with auto-scroll on hover
        boolean descHovered = TextHelper.isPointInBounds(mouseX, mouseY, x + 6, y + 17, maxTextWidth, font.lineHeight);
        TextHelper.drawAutoScrollingText(g, font, quest.description, x + 6, y + 17, maxTextWidth, 0xFFa0a0c0, false, descHovered, "quest_desc_" + quest.name);

        // Progress bar (plus bas) - Utiliser les images
        int barX = x + 6;
        int barY = y + 32;
        int barW = width - 12;
        int barH = 8;

        // Barre vide
        ImageRenderer.renderScaledImage(g, QUESTBAR_BLUE_EMPTY, barX, barY, barW, barH);

        // Barre remplie (proportionnelle au pourcentage)
        if (quest.progress > 0) {
            int filledWidth = (barW * quest.progress / 100);
            g.enableScissor(barX, barY, barX + filledWidth, barY + barH);
            // Utiliser la barre verte si completed, sinon bleue
            ResourceLocation barFilled = isCompleted ? QUESTBAR_SUCCESS_GREEN : QUESTBAR_BLUE_FILLED;
            ImageRenderer.renderScaledImage(g, barFilled, barX, barY, barW, barH);
            g.disableScissor();
        }

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
        g.drawString(font, translate("erinium_faction.gui.quests.title"), x + sw(9, scaleX), y + sh(9, scaleY), 0xFFffffff, true);

        // Tronquer le texte info si trop long
        String questInfoText = translate("erinium_faction.gui.quests.info", 6, 1);
        int maxInfoWidth = sw(78, scaleX);
        TextHelper.drawScaledText(g, font, questInfoText, x + w - maxInfoWidth, y + sh(9, scaleY), maxInfoWidth, 0xFF00d2ff, false);

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
