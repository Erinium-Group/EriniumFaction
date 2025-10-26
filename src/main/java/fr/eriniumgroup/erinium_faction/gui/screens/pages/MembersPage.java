package fr.eriniumgroup.erinium_faction.gui.screens.pages;

import fr.eriniumgroup.erinium_faction.gui.screens.components.ScrollList;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

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
        int power;
        boolean online;

        MemberInfo(String name, String rank, int power, boolean online) {
            this.name = name;
            this.rank = rank;
            this.power = power;
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

            // Ajouter des membres d'exemple
            List<MemberInfo> members = new ArrayList<>();
            members.add(new MemberInfo("{{MEMBER_1_NAME}}", "{{MEMBER_1_RANK}}", 100, true));
            members.add(new MemberInfo("{{MEMBER_2_NAME}}", "{{MEMBER_2_RANK}}", 85, true));
            members.add(new MemberInfo("{{MEMBER_3_NAME}}", "{{MEMBER_3_RANK}}", 92, false));
            members.add(new MemberInfo("{{MEMBER_4_NAME}}", "{{MEMBER_4_RANK}}", 78, true));
            members.add(new MemberInfo("{{MEMBER_5_NAME}}", "{{MEMBER_5_RANK}}", 88, false));
            // Exemples supplémentaires pour montrer le scroll
            members.add(new MemberInfo("PlayerExample1", "Member", 75, true));
            members.add(new MemberInfo("PlayerExample2", "Officer", 82, true));
            members.add(new MemberInfo("PlayerExample3", "Recruit", 45, false));
            members.add(new MemberInfo("PlayerExample4", "Member", 70, true));
            members.add(new MemberInfo("PlayerExample5", "Member", 65, false));

            memberScrollList.setItems(members);
            memberScrollList.setOnItemClick(member -> {
                System.out.println("MembersPage: Clicked on member " + member.name);
            });
        }

        // Update positions
        int x = sx(CONTENT_X, leftPos, scaleX);
        int y = sy(CONTENT_Y, topPos, scaleY);
        int w = sw(CONTENT_W, scaleX);
        int h = sh(CONTENT_H, scaleY);

        memberScrollList.setBounds(x, y + sh(15, scaleY), w, h - sh(21, scaleY));
    }

    private void renderMemberItem(GuiGraphics g, MemberInfo member, int x, int y, int width, int height, boolean hovered, Font font) {
        // Background
        int bgColor = hovered ? 0x40667eea : 0xE61e1e2e;
        g.fill(x, y, x + width, y + height, bgColor);
        g.fill(x, y, x + width, y + 1, 0x50667eea);

        // Online indicator (plus petit)
        int indicatorColor = member.online ? 0xFF10b981 : 0xFF6a6a7e;
        g.fill(x + 4, y + 7, x + 8, y + 11, indicatorColor);

        // Member name (en haut)
        int nameColor = member.online ? 0xFFffffff : 0xFF9a9aae;
        g.drawString(font, member.name, x + 12, y + 4, nameColor, false);

        // Rank (en bas)
        int rankColor = getRankColor(member.rank);
        g.drawString(font, member.rank, x + 12, y + 16, rankColor, false);

        // Power bar (réduite pour petit GUI)
        int barX = x + width - 55;
        int barY = y + 5;
        int barW = 40;  // Réduit de 100 à 40
        int barH = 8;

        g.fill(barX, barY, barX + barW, barY + barH, 0xFF2a2a3e);
        int powerPercent = Math.min(100, member.power);
        g.fill(barX, barY, barX + (barW * powerPercent / 100), barY + barH, 0xFFa855f7);

        // Power text (en dessous de la barre)
        String powerText = String.valueOf(member.power);
        g.drawString(font, powerText, barX + barW / 2 - font.width(powerText) / 2, y + 17, 0xFF00d2ff, false);
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
        g.drawString(font, "FACTION MEMBERS", x + sw(5, scaleX), y + sh(5, scaleY), 0xFFffffff, true);

        // Stats
        String statsText = "{{MEMBERS_ONLINE}}/{{MEMBER_COUNT}} Online";
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
