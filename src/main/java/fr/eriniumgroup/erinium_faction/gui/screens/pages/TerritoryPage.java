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
 * Page Territory - Basée sur territory.svg
 * Affiche les territoires claimés avec scroll list
 */
public class TerritoryPage extends FactionPage {

    private ScrollList<ClaimInfo> claimScrollList;

    // Textures
    private static final ResourceLocation CLAIM_CARD_NORMAL = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/territory/claim-card-normal.png");
    private static final ResourceLocation CLAIM_CARD_HOVER = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/territory/claim-card-hover.png");
    private static final ResourceLocation STAT_CARD_NORMAL = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/territory/stat-card-normal.png");

    private static class ClaimInfo {
        String coords;
        String dimension;

        ClaimInfo(String coords, String dimension) {
            this.coords = coords;
            this.dimension = dimension;
        }
    }

    public TerritoryPage(Font font) {
        super(font);
    }

    private void initComponents(int leftPos, int topPos, double scaleX, double scaleY) {
        if (claimScrollList == null) {
            claimScrollList = new ScrollList<>(font, this::renderClaimItem, sh(26, scaleY));
        }

        // Récupérer les vraies données de claims depuis FactionSnapshot
        var data = getFactionData();
        List<ClaimInfo> claims = new ArrayList<>();

        if (data != null && data.claimsList != null) {
            for (var claim : data.claimsList) {
                // Format des coordonnées: X: chunkX, Z: chunkZ
                String coords = "X: " + claim.chunkX + ", Z: " + claim.chunkZ;
                // Simplifier le nom de dimension (ex: minecraft:overworld -> Overworld)
                String dimName = claim.dimension;
                if (dimName.contains(":")) {
                    dimName = dimName.substring(dimName.lastIndexOf(':') + 1);
                }
                // Première lettre en majuscule
                if (!dimName.isEmpty()) {
                    dimName = dimName.substring(0, 1).toUpperCase() + dimName.substring(1);
                }
                claims.add(new ClaimInfo(coords, dimName));
            }
        }

        claimScrollList.setItems(claims);

        int x = sx(CONTENT_X, leftPos, scaleX);
        int y = sy(CONTENT_Y, topPos, scaleY);
        int w = sw(CONTENT_W, scaleX);

        claimScrollList.setBounds(x, y + sh(59, scaleY), w, sh(151, scaleY));
    }

    private void renderClaimItem(GuiGraphics g, ClaimInfo claim, int x, int y, int width, int height, boolean hovered, Font font, int mouseX, int mouseY) {
        // Utiliser les images au lieu de g.fill
        ResourceLocation cardTexture = hovered ? CLAIM_CARD_HOVER : CLAIM_CARD_NORMAL;
        ImageRenderer.renderScaledImage(g, cardTexture, x, y, width, height);

        // Auto-scroll coords on hover
        int maxCoordsWidth = width - 18;
        boolean coordsHovered = TextHelper.isPointInBounds(mouseX, mouseY, x + 9, y + 5, maxCoordsWidth, font.lineHeight);
        TextHelper.drawAutoScrollingText(g, font, claim.coords, x + 9, y + 5, maxCoordsWidth, 0xFFffffff, true, coordsHovered, "claim_" + claim.coords);
        g.drawString(font, translate("erinium_faction.gui.territory.label.dimension", claim.dimension), x + 9, y + 14, 0xFFa0a0c0, false);
    }

    @Override
    public void render(GuiGraphics g, int leftPos, int topPos, double scaleX, double scaleY, int mouseX, int mouseY) {
        initComponents(leftPos, topPos, scaleX, scaleY);

        int x = sx(CONTENT_X, leftPos, scaleX);
        int y = sy(CONTENT_Y, topPos, scaleY);

        // Stats avec vraies données
        var data = getFactionData();
        String claimsText = data != null ? String.valueOf(data.claims) : "0";
        String maxClaimsText = data != null ? String.valueOf(data.maxClaims) : "0";
        String powerText = data != null ? String.format("%.1f", data.currentPower) : "0";

        renderStatCard(g, x, y, "CHUNKS", claimsText, 0xFFa855f7, scaleX, scaleY);
        renderStatCard(g, x + sw(97, scaleX), y, "MAX", maxClaimsText, 0xFF00d2ff, scaleX, scaleY);
        renderStatCard(g, x + sw(194, scaleX), y, "POWER", powerText, 0xFF10b981, scaleX, scaleY);

        // List
        claimScrollList.render(g, mouseX, mouseY);
    }

    private void renderStatCard(GuiGraphics g, int x, int y, String label, String value, int color, double scaleX, double scaleY) {
        int w = sw(92, scaleX);
        int h = sh(32, scaleY);

        // Utiliser l'image au lieu de g.fill
        ImageRenderer.renderScaledImage(g, STAT_CARD_NORMAL, x, y, w, h);

        // Dessiner une ligne de couleur en haut pour différencier les types
        g.fill(x, y, x + w, y + 1, color & 0x80FFFFFF);

        g.drawString(font, label, x + sw(9, scaleX), y + sh(11, scaleY), 0xFFa0a0c0, false);
        g.drawString(font, value, x + sw(9, scaleX), y + sh(24, scaleY), color, true);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button, int leftPos, int topPos, double scaleX, double scaleY) {
        if (claimScrollList == null) return false;
        return claimScrollList.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button, int leftPos, int topPos, double scaleX, double scaleY) {
        if (claimScrollList == null) return false;
        return claimScrollList.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY, int leftPos, int topPos, double scaleX, double scaleY) {
        if (claimScrollList == null) return false;
        return claimScrollList.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY, int leftPos, int topPos, double scaleX, double scaleY) {
        if (claimScrollList == null) return false;
        return claimScrollList.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
}
