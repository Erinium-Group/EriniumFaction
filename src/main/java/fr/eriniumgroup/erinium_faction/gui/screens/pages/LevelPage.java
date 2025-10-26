package fr.eriniumgroup.erinium_faction.gui.screens.pages;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Page Level - Basée sur faction-level.svg
 * Niveau et XP de la faction
 */
public class LevelPage extends FactionPage {

    public LevelPage(Font font) {
        super(font);
    }

    @Override
    public void render(GuiGraphics g, int leftPos, int topPos, double scaleX, double scaleY, int mouseX, int mouseY) {
        int x = sx(CONTENT_X, leftPos, scaleX);
        int y = sy(CONTENT_Y, topPos, scaleY);

        // Level Display (réduit pour ne pas toucher la bordure)
        g.fill(x, y, x + sw(267, scaleX), y + sh(65, scaleY), 0xE61e1e2e);
        g.fill(x, y, x + sw(267, scaleX), y + 1, 0xFFfbbf24);

        // Level badge
        int badgeX = x + sw(44, scaleX);
        int badgeY = y + sh(32, scaleY);
        g.fill(badgeX - sw(25, scaleX), badgeY - sh(24, scaleY), badgeX + sw(25, scaleX), badgeY + sh(24, scaleY), 0xFFfbbf24);

        g.drawCenteredString(font, "LEVEL", badgeX, badgeY - sh(8, scaleY), 0xFFffffff);
        g.drawCenteredString(font, "{{FACTION_LEVEL}}", badgeX, badgeY + sh(5, scaleY), 0xFFffffff);

        // Level info
        int infoX = x + sw(83, scaleX);
        g.drawString(font, "{{LEVEL_TITLE}}", infoX, y + sh(16, scaleY), 0xFFfbbf24, true);
        g.drawString(font, "Max Claims: {{MAX_CLAIMS}}", infoX, y + sh(27, scaleY), 0xFFa0a0c0, false);
        g.drawString(font, "Max Members: {{MAX_MEMBERS}}", infoX, y + sh(37, scaleY), 0xFFa0a0c0, false);

        // XP Bar (réduit pour 400x270)
        int barY = y + sh(70, scaleY);
        g.drawString(font, "XP PROGRESS", x + sw(4, scaleX), barY, 0xFFa0a0c0, false);

        int barBgY = barY + sh(10, scaleY);
        int barH = sh(11, scaleY);
        int barX = x + sw(4, scaleX);
        int barW = sw(267, scaleX);  // 275 - 8 de marge
        g.fill(barX, barBgY, barX + barW, barBgY + barH, 0xFF2a2a3e);
        int xpPercent = 75; // {{XP_CURRENT}} / {{XP_MAX}}
        g.fill(barX, barBgY, barX + (barW * xpPercent / 100), barBgY + barH, 0xFFa855f7);

        // Center text vertically in the bar
        g.drawCenteredString(font, "{{XP_CURRENT}} / {{XP_MAX}} XP", barX + barW / 2, barBgY + barH / 2 - 4, 0xFFffffff);

        // Benefits (réduit pour ne pas toucher la bordure)
        int benefitsY = y + sh(97, scaleY);
        g.fill(x, benefitsY, x + sw(267, scaleX), benefitsY + sh(17, scaleY), 0x802a2a3e);
        g.drawString(font, "LEVEL BENEFITS", x + sw(4, scaleX), benefitsY + sh(6, scaleY), 0xFFffffff, true);

        int benefitY = benefitsY + sh(24, scaleY);
        for (int i = 0; i < 3; i++) {
            g.drawString(font, "{{BENEFIT_" + (i+1) + "_DESC}}", x + sw(9, scaleX), benefitY + sh(i * 24, scaleY), 0xFF10b981, false);
        }
    }
}
