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

        // Récupérer les données de faction
        var data = getFactionData();
        int level = data != null ? data.level : 1;
        int xp = data != null ? data.xp : 0;
        int xpRequired = data != null ? data.xpRequired : 100;
        int maxClaims = data != null ? data.maxClaims : 0;
        int maxMembers = data != null ? data.maxPlayers : 0;

        // Level Display (réduit pour ne pas toucher la bordure)
        g.fill(x, y, x + sw(267, scaleX), y + sh(65, scaleY), 0xE61e1e2e);
        g.fill(x, y, x + sw(267, scaleX), y + 1, 0xFFfbbf24);

        // Level badge
        int badgeX = x + sw(44, scaleX);
        int badgeY = y + sh(32, scaleY);
        g.fill(badgeX - sw(25, scaleX), badgeY - sh(24, scaleY), badgeX + sw(25, scaleX), badgeY + sh(24, scaleY), 0xFFfbbf24);

        g.drawCenteredString(font, translate("erinium_faction.gui.level.label"), badgeX, badgeY - sh(8, scaleY), 0xFFffffff);
        g.drawCenteredString(font, String.valueOf(level), badgeX, badgeY + sh(5, scaleY), 0xFFffffff);

        // Level info
        int infoX = x + sw(83, scaleX);
        g.drawString(font, translate("erinium_faction.gui.level.faction_level", level), infoX, y + sh(16, scaleY), 0xFFfbbf24, true);
        g.drawString(font, translate("erinium_faction.gui.level.max_claims", maxClaims), infoX, y + sh(27, scaleY), 0xFFa0a0c0, false);
        g.drawString(font, translate("erinium_faction.gui.level.max_members", maxMembers), infoX, y + sh(37, scaleY), 0xFFa0a0c0, false);

        // XP Bar (réduit pour 400x270)
        int barY = y + sh(70, scaleY);
        g.drawString(font, translate("erinium_faction.gui.level.xp_progress"), x + sw(4, scaleX), barY, 0xFFa0a0c0, false);

        int barBgY = barY + sh(10, scaleY);
        int barH = sh(11, scaleY);
        int barX = x + sw(4, scaleX);
        int barW = sw(267, scaleX);  // 275 - 8 de marge
        g.fill(barX, barBgY, barX + barW, barBgY + barH, 0xFF2a2a3e);
        int xpPercent = xpRequired > 0 ? (xp * 100 / xpRequired) : 0;
        g.fill(barX, barBgY, barX + (barW * xpPercent / 100), barBgY + barH, 0xFFa855f7);

        // Center text vertically in the bar
        g.drawCenteredString(font, translate("erinium_faction.gui.level.xp_format", xp, xpRequired), barX + barW / 2, barBgY + barH / 2 - 4, 0xFFffffff);

        // Benefits (réduit pour ne pas toucher la bordure)
        int benefitsY = y + sh(97, scaleY);
        g.fill(x, benefitsY, x + sw(267, scaleX), benefitsY + sh(17, scaleY), 0x802a2a3e);
        g.drawString(font, translate("erinium_faction.gui.level.benefits_title"), x + sw(4, scaleX), benefitsY + sh(6, scaleY), 0xFFffffff, true);

        int benefitY = benefitsY + sh(24, scaleY);
        String[] benefits = {
            "Unlock additional claims per level",
            "Increase faction member capacity",
            "Access to higher tier perks"
        };
        for (int i = 0; i < benefits.length; i++) {
            g.drawString(font, benefits[i], x + sw(9, scaleX), benefitY + sh(i * 24, scaleY), 0xFF10b981, false);
        }
    }
}
