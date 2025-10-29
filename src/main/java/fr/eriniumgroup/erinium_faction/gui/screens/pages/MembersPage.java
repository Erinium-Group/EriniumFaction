package fr.eriniumgroup.erinium_faction.gui.screens.pages;

import fr.eriniumgroup.erinium_faction.core.faction.Permission;
import fr.eriniumgroup.erinium_faction.gui.screens.components.ContextMenu;
import fr.eriniumgroup.erinium_faction.gui.screens.components.ScrollList;
import fr.eriniumgroup.erinium_faction.gui.screens.components.TextHelper;
import fr.eriniumgroup.erinium_faction.gui.screens.components.ImageRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Page Members - Basée sur members.svg
 * Liste scrollable des membres avec leurs stats
 */
public class MembersPage extends FactionPage {

    private ScrollList<MemberInfo> memberScrollList;
    private ContextMenu contextMenu;

    // Textures pour les member cards
    private static final ResourceLocation MEMBER_CARD_NORMAL = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/members/member-card-normal.png");
    private static final ResourceLocation MEMBER_CARD_HOVER = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/members/member-card-hover.png");

    // Textures pour les boutons d'action des membres
    private static final ResourceLocation BUTTON_PROMOTE_NORMAL = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/members/button-promote-normal.png");
    private static final ResourceLocation BUTTON_PROMOTE_HOVER = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/members/button-promote-hover.png");
    private static final ResourceLocation BUTTON_DEMOTE_NORMAL = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/members/button-demote-normal.png");
    private static final ResourceLocation BUTTON_DEMOTE_HOVER = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/members/button-demote-hover.png");
    private static final ResourceLocation BUTTON_KICK_NORMAL = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/members/button-kick-normal.png");
    private static final ResourceLocation BUTTON_KICK_HOVER = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/members/button-kick-hover.png");

    // Textures pour la barre de power
    private static final ResourceLocation PROGRESSBAR_EMPTY = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/common/progressbar-empty.png");
    private static final ResourceLocation PROGRESSBAR_FILLED = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/common/progressbar-filled-100.png");

    // Classe pour les infos de membre
    private static class MemberInfo {
        UUID uuid;
        String name;
        String rankId;
        String rankDisplay;
        int rankPriority;
        double power;
        double maxPower;
        boolean online;

        MemberInfo(UUID uuid, String name, String rankId, String rankDisplay, int rankPriority, double power, double maxPower, boolean online) {
            this.uuid = uuid;
            this.name = name;
            this.rankId = rankId;
            this.rankDisplay = rankDisplay;
            this.rankPriority = rankPriority;
            this.power = power;
            this.maxPower = maxPower;
            this.online = online;
        }
    }

    public MembersPage(Font font) {
        super(font);
        contextMenu = new ContextMenu(font);
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
                String rankId = data.membersRank.getOrDefault(uuid, "member");

                // Trouver les informations du rank (display name et priority)
                String rankDisplay = rankId;
                int rankPriority = 0;
                if (data.ranks != null) {
                    for (var rankInfo : data.ranks) {
                        if (rankInfo.id.equals(rankId)) {
                            rankDisplay = rankInfo.display;
                            rankPriority = rankInfo.priority;
                            break;
                        }
                    }
                }

                // Récupérer le power et status en ligne depuis FactionSnapshot
                double power = data.membersPower.getOrDefault(uuid, 0.0);
                double maxPower = data.membersMaxPower.getOrDefault(uuid, 100.0);
                boolean online = data.membersOnline.getOrDefault(uuid, false);
                members.add(new MemberInfo(uuid, name, rankId, rankDisplay, rankPriority, power, maxPower, online));
            }
        }

        if (members.isEmpty()) {
            // Ajouter un message si aucun membre
            members.add(new MemberInfo(null, translate("erinium_faction.gui.members.none"), "", translate("erinium_faction.gui.members.rank_na"), 0, 0, 0, false));
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
        // Background - Utiliser les images
        ResourceLocation cardTexture = hovered ? MEMBER_CARD_HOVER : MEMBER_CARD_NORMAL;
        ImageRenderer.renderScaledImage(g, cardTexture, x, y, width, height);

        // Online indicator (plus petit)
        int indicatorColor = member.online ? 0xFF10b981 : 0xFF6a6a7e;
        g.fill(x + 4, y + 7, x + 8, y + 11, indicatorColor);

        // Member name (en haut) - auto-scroll on hover
        int nameColor = member.online ? 0xFFffffff : 0xFF9a9aae;
        int nameMaxWidth = width - 70; // Laisser place pour la barre de power
        boolean nameHovered = TextHelper.isPointInBounds(mouseX, mouseY, x + 12, y + 4, nameMaxWidth, font.lineHeight);
        TextHelper.drawAutoScrollingText(g, font, member.name, x + 12, y + 4, nameMaxWidth, nameColor, false, nameHovered, "member_name_" + member.name);

        // Rank (en bas) - auto-scroll on hover
        int rankColor = getRankColor(member.rankDisplay);
        boolean rankHovered = TextHelper.isPointInBounds(mouseX, mouseY, x + 12, y + 16, nameMaxWidth, font.lineHeight);
        TextHelper.drawAutoScrollingText(g, font, member.rankDisplay, x + 12, y + 16, nameMaxWidth, rankColor, false, rankHovered, "member_rank_" + member.name);

        // Power bar (réduite pour petit GUI) - Utiliser les images
        int barX = x + width - 55;
        int barY = y + 5;
        int barW = 40;  // Réduit de 100 à 40
        int barH = 8;

        // Barre vide
        ImageRenderer.renderScaledImage(g, PROGRESSBAR_EMPTY, barX, barY, barW, barH);

        // Barre remplie
        int powerPercent = member.maxPower > 0 ? (int) Math.round((member.power / member.maxPower) * 100) : 0;
        powerPercent = Math.min(100, Math.max(0, powerPercent));
        if (powerPercent > 0) {
            int filledWidth = (barW * powerPercent / 100);
            g.enableScissor(barX, barY, barX + filledWidth, barY + barH);
            ImageRenderer.renderScaledImage(g, PROGRESSBAR_FILLED, barX, barY, barW, barH);
            g.disableScissor();
        }

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

        // Render context menu on top
        contextMenu.render(g, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button, int leftPos, int topPos, double scaleX, double scaleY) {
        // Priorité au context menu
        if (contextMenu.isVisible()) {
            return contextMenu.mouseClicked(mouseX, mouseY, button);
        }

        if (memberScrollList == null) return false;

        // Clic droit pour ouvrir le menu contextuel
        if (button == 1) { // Clic droit
            // Trouver le membre sous la souris
            MemberInfo clickedMember = findMemberAtPosition(mouseX, mouseY, scaleY);
            if (clickedMember != null && clickedMember.uuid != null) {
                openContextMenu(clickedMember, (int) mouseX, (int) mouseY);
                return true;
            }
        }

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

    /**
     * Trouve le membre à la position de la souris
     */
    private MemberInfo findMemberAtPosition(double mouseX, double mouseY, double scaleY) {
        if (memberScrollList == null) return null;

        List<MemberInfo> items = memberScrollList.getItems();
        if (items == null || items.isEmpty()) return null;

        // Récupérer les bounds de la scroll list
        int listX = memberScrollList.getX();
        int listY = memberScrollList.getY();
        int listWidth = memberScrollList.getWidth();
        int listHeight = memberScrollList.getHeight();
        int itemHeight = sh(30, scaleY); // Utiliser le scaleY correct

        // Calculer l'offset de scroll
        int scrollOffset = memberScrollList.getScrollOffset();

        for (int i = 0; i < items.size(); i++) {
            int itemY = listY + i * itemHeight - scrollOffset;

            // Vérifier si la souris est sur cet item et si l'item est visible
            if (mouseX >= listX && mouseX < listX + listWidth &&
                mouseY >= itemY && mouseY < itemY + itemHeight &&
                itemY + itemHeight > listY && itemY < listY + listHeight) {
                return items.get(i);
            }
        }

        return null;
    }

    /**
     * Ouvre le menu contextuel pour un membre
     */
    private void openContextMenu(MemberInfo member, int x, int y) {
        var data = getFactionData();
        if (data == null) return;

        var minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;

        UUID currentPlayerUUID = minecraft.player.getUUID();

        // Ne pas ouvrir le menu contextuel sur soi-même
        if (currentPlayerUUID.equals(member.uuid)) {
            return;
        }

        // Récupérer les informations du joueur actuel
        String currentPlayerRankId = data.membersRank.getOrDefault(currentPlayerUUID, "member");
        int currentPlayerRankPriority = 0;
        if (data.ranks != null) {
            for (var rankInfo : data.ranks) {
                if (rankInfo.id.equals(currentPlayerRankId)) {
                    currentPlayerRankPriority = rankInfo.priority;
                    break;
                }
            }
        }

        // Trouver le rank le plus haut et le plus bas
        int highestRankPriority = Integer.MIN_VALUE;
        int lowestRankPriority = Integer.MAX_VALUE;
        if (data.ranks != null && !data.ranks.isEmpty()) {
            for (var rankInfo : data.ranks) {
                highestRankPriority = Math.max(highestRankPriority, rankInfo.priority);
                lowestRankPriority = Math.min(lowestRankPriority, rankInfo.priority);
            }
        }

        // Vérifier les permissions du joueur actuel
        boolean hasPromotePermission = data.hasPermission(currentPlayerUUID, Permission.PROMOTE_MEMBERS);
        boolean hasDemotePermission = data.hasPermission(currentPlayerUUID, Permission.DEMOTE_MEMBERS);
        boolean hasKickPermission = data.hasPermission(currentPlayerUUID, Permission.KICK_MEMBERS);

        // Vérifier la hiérarchie: le joueur actuel doit avoir un rank supérieur au membre ciblé
        boolean canManageMember = currentPlayerRankPriority > member.rankPriority;

        // PROMOTE: Possible si on a la permission, si on peut gérer ce membre, et si le membre n'est pas déjà au rank le plus haut
        boolean canPromote = hasPromotePermission && canManageMember && member.rankPriority < highestRankPriority;

        // DEMOTE: Possible si on a la permission, si on peut gérer ce membre, et si le membre n'est pas déjà au rank le plus bas
        boolean canDemote = hasDemotePermission && canManageMember && member.rankPriority > lowestRankPriority;

        // KICK: Possible si on a la permission et si on peut gérer ce membre
        boolean canKick = hasKickPermission && canManageMember;

        // Créer le menu contextuel
        contextMenu.clearItems();

        contextMenu.addItem(translate("erinium_faction.gui.members.context.promote"), () -> {
            // TODO: Envoyer un packet au serveur pour promouvoir le membre
            showInfo("Promote", "Promoting " + member.name);
        }, canPromote);

        contextMenu.addItem(translate("erinium_faction.gui.members.context.demote"), () -> {
            // TODO: Envoyer un packet au serveur pour dégrader le membre
            showInfo("Demote", "Demoting " + member.name);
        }, canDemote);

        contextMenu.addItem(translate("erinium_faction.gui.members.context.kick"), () -> {
            // TODO: Envoyer un packet au serveur pour kicker le membre
            showInfo("Kick", "Kicking " + member.name);
        }, canKick);

        contextMenu.open(x, y);
    }
}
