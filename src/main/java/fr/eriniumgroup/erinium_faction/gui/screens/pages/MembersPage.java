package fr.eriniumgroup.erinium_faction.gui.screens.pages;

import fr.eriniumgroup.erinium_faction.gui.screens.components.ScrollList;
import fr.eriniumgroup.erinium_faction.gui.screens.components.TextHelper;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Page Members - Basée sur members.svg
 * Liste scrollable des membres avec leurs stats
 */
public class MembersPage extends FactionPage {

    private ScrollList<MemberInfo> memberScrollList;

    // Classe pour les infos de membre
    private static class MemberInfo {
        String name;
        String rank;
        double power;
        double maxPower;
        boolean online;

        MemberInfo(String name, String rank, double power, double maxPower, boolean online) {
            this.name = name;
            this.rank = rank;
            this.power = power;
            this.maxPower = maxPower;
            this.online = online;
        }
    }

    public MembersPage(Font font) {
        super(font);
    }

    private void initComponents(int leftPos, int topPos, double scaleX, double scaleY) {
        if (memberScrollList == null) {
            // Hauteur augmentée pour que tout soit visible (30 au lieu de 16)
            memberScrollList = new ScrollList<>(font, this::renderMemberItem, sh(30, scaleY));
        }

        // Mettre à jour la liste avec les vraies données
        var data = getFactionData();
        List<MemberInfo> members = new ArrayList<>();

        if (data != null && data.memberNames != null) {
            for (var entry : data.memberNames.entrySet()) {
                UUID uuid = entry.getKey();
                String name = entry.getValue();
                String rank = data.membersRank.getOrDefault(uuid, "Member");
                // Récupérer le power et status en ligne depuis FactionSnapshot
                double power = data.membersPower.getOrDefault(uuid, 0.0);
                double maxPower = data.membersMaxPower.getOrDefault(uuid, 100.0);
                boolean online = data.membersOnline.getOrDefault(uuid, false);
                members.add(new MemberInfo(name, rank, power, maxPower, online));
            }
        }

        if (members.isEmpty()) {
            // Ajouter un message si aucun membre
            members.add(new MemberInfo(translate("erinium_faction.gui.members.none"), translate("erinium_faction.gui.members.rank_na"), 0, 0, false));
        }

        memberScrollList.setItems(members);

        // Update positions
        int x = sx(CONTENT_X, leftPos, scaleX);
        int y = sy(CONTENT_Y, topPos, scaleY);
        int w = sw(CONTENT_W, scaleX);
        int h = sh(CONTENT_H, scaleY);

        memberScrollList.setBounds(x, y + sh(15, scaleY), w, h - sh(21, scaleY));
    }

    private void renderMemberItem(GuiGraphics g, MemberInfo member, int x, int y, int width, int height, boolean hovered, Font font, int mouseX, int mouseY) {
        // Background
        int bgColor = hovered ? 0x40667eea : 0xE61e1e2e;
        g.fill(x, y, x + width, y + height, bgColor);
        g.fill(x, y, x + width, y + 1, 0x50667eea);

        // Online indicator (plus petit)
        int indicatorColor = member.online ? 0xFF10b981 : 0xFF6a6a7e;
        g.fill(x + 4, y + 7, x + 8, y + 11, indicatorColor);

        // Member name (en haut) - auto-scroll on hover
        int nameColor = member.online ? 0xFFffffff : 0xFF9a9aae;
        int nameMaxWidth = width - 70; // Laisser place pour la barre de power
        boolean nameHovered = TextHelper.isPointInBounds(mouseX, mouseY, x + 12, y + 4, nameMaxWidth, font.lineHeight);
        TextHelper.drawAutoScrollingText(g, font, member.name, x + 12, y + 4, nameMaxWidth, nameColor, false, nameHovered, "member_name_" + member.name);

        // Rank (en bas) - auto-scroll on hover
        int rankColor = getRankColor(member.rank);
        boolean rankHovered = TextHelper.isPointInBounds(mouseX, mouseY, x + 12, y + 16, nameMaxWidth, font.lineHeight);
        TextHelper.drawAutoScrollingText(g, font, member.rank, x + 12, y + 16, nameMaxWidth, rankColor, false, rankHovered, "member_rank_" + member.name);

        // Power bar (réduite pour petit GUI)
        int barX = x + width - 55;
        int barY = y + 5;
        int barW = 40;  // Réduit de 100 à 40
        int barH = 8;

        g.fill(barX, barY, barX + barW, barY + barH, 0xFF2a2a3e);
        int powerPercent = member.maxPower > 0 ? (int) Math.round((member.power / member.maxPower) * 100) : 0;
        powerPercent = Math.min(100, Math.max(0, powerPercent));
        g.fill(barX, barY, barX + (barW * powerPercent / 100), barY + barH, 0xFFa855f7);

        // Power text (en dessous de la barre) - Affiche power / maxPower en Double (1 décimale)
        String powerText = String.format("%.1f/%.1f", member.power, member.maxPower);
        int textWidth = font.width(powerText);
        g.drawString(font, powerText, barX + barW / 2 - textWidth / 2, y + 17, 0xFF00d2ff, false);
    }

    private int getRankColor(String rank) {
        if (rank.contains("Leader")) return 0xFFec4899;
        if (rank.contains("Officer")) return 0xFFa855f7;
        if (rank.contains("Member")) return 0xFF3b82f6;
        if (rank.contains("Recruit")) return 0xFF6a6a7e;
        return 0xFFffffff;
    }

    @Override
    public void render(GuiGraphics g, int leftPos, int topPos, double scaleX, double scaleY, int mouseX, int mouseY) {
        initComponents(leftPos, topPos, scaleX, scaleY);

        int x = sx(CONTENT_X, leftPos, scaleX);
        int y = sy(CONTENT_Y, topPos, scaleY);
        int w = sw(CONTENT_W, scaleX);

        // Header
        g.fill(x, y, x + w, y + sh(12, scaleY), 0xE61e1e2e);
        g.fill(x, y, x + w, y + 1, 0xFF00d2ff);
        g.drawString(font, translate("erinium_faction.gui.members.title"), x + sw(5, scaleX), y + sh(5, scaleY), 0xFFffffff, true);

        // Stats avec vraies données
        var data = getFactionData();
        String statsText = data != null ? translate("erinium_faction.gui.members.count", data.membersCount) : translate("erinium_faction.gui.members.count", 0);
        g.drawString(font, statsText, x + w - font.width(statsText) - sw(5, scaleX), y + sh(5, scaleY), 0xFF00d2ff, false);

        // Member list
        memberScrollList.render(g, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button, int leftPos, int topPos, double scaleX, double scaleY) {
        if (memberScrollList == null) return false;
        return memberScrollList.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button, int leftPos, int topPos, double scaleX, double scaleY) {
        if (memberScrollList == null) return false;
        return memberScrollList.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY, int leftPos, int topPos, double scaleX, double scaleY) {
        if (memberScrollList == null) return false;
        return memberScrollList.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY, int leftPos, int topPos, double scaleX, double scaleY) {
        if (memberScrollList == null) return false;
        return memberScrollList.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
}
