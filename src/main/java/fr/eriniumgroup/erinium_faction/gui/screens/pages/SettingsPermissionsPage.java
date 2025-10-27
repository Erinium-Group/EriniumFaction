package fr.eriniumgroup.erinium_faction.gui.screens.pages;

import fr.eriniumgroup.erinium_faction.common.network.packets.FactionActionPacket;
import fr.eriniumgroup.erinium_faction.core.faction.FactionSnapshot;
import fr.eriniumgroup.erinium_faction.gui.screens.components.ScrollList;
import fr.eriniumgroup.erinium_faction.gui.screens.components.StyledButton;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

/**
 * Page Settings Permissions - Basée sur settings-permissions.svg
 * Permissions par rang avec layout vertical des ranks et scroll list des permissions
 */
public class SettingsPermissionsPage extends FactionPage {

    private enum Rank {
        OFFICER("Officer", 0xFFa855f7),
        MEMBER("Member", 0xFF3b82f6),
        RECRUIT("Recruit", 0xFF6a6a7e);

        final String name;
        final int color;

        Rank(String name, int color) {
            this.name = name;
            this.color = color;
        }
    }

    private static class Permission {
        String name;
        String description;

        Permission(String name, String description) {
            this.name = name;
            this.description = description;
        }
    }

    private Rank selectedRank = Rank.OFFICER;
    private ScrollList<Permission> permissionScrollList;
    private final Map<Rank, Set<String>> rankPermissions = new HashMap<>();
    private final List<StyledButton> actionButtons = new ArrayList<>();

    public SettingsPermissionsPage(Font font) {
        super(font);

        // Initialize permissions for each rank
        for (Rank rank : Rank.values()) {
            rankPermissions.put(rank, new HashSet<>());
        }
    }

    private void loadRealPermissions() {
        // Charger les vraies permissions depuis FactionSnapshot
        var data = getFactionData();
        if (data != null && data.ranks != null) {
            // Réinitialiser les permissions
            for (Rank rank : Rank.values()) {
                rankPermissions.get(rank).clear();
            }

            // Charger les permissions réelles
            for (FactionSnapshot.RankInfo rankInfo : data.ranks) {
                Rank rank = getRankByName(rankInfo.display);
                if (rank != null) {
                    // Convertir les permissions du format serveur au format GUI
                    for (String perm : rankInfo.perms) {
                        String guiPerm = convertServerPermToGui(perm);
                        if (guiPerm != null) {
                            rankPermissions.get(rank).add(guiPerm);
                        }
                    }
                }
            }
        }
    }

    private Rank getRankByName(String name) {
        for (Rank rank : Rank.values()) {
            if (rank.name.equalsIgnoreCase(name)) {
                return rank;
            }
        }
        return null;
    }

    private String convertServerPermToGui(String serverPerm) {
        // Mapping simple pour l'instant
        return switch (serverPerm) {
            case "faction.invite" -> "Invite Members";
            case "faction.kick" -> "Kick Members";
            case "faction.claim" -> "Claim Territory";
            case "faction.unclaim" -> "Unclaim Territory";
            case "faction.build" -> "Build in Territory";
            case "faction.break" -> "Break in Territory";
            case "faction.use.doors" -> "Use Doors";
            case "faction.use.buttons" -> "Use Buttons";
            case "faction.use.levers" -> "Use Levers";
            case "faction.use.containers" -> "Use Containers";
            case "faction.manage.permissions" -> "Manage Permissions";
            case "faction.manage.alliances" -> "Manage Alliances";
            default -> null;
        };
    }

    private String convertGuiPermToServer(String guiPerm) {
        return switch (guiPerm) {
            case "Invite Members" -> "faction.invite";
            case "Kick Members" -> "faction.kick";
            case "Claim Territory" -> "faction.claim";
            case "Unclaim Territory" -> "faction.unclaim";
            case "Build in Territory" -> "faction.build";
            case "Break in Territory" -> "faction.break";
            case "Use Doors" -> "faction.use.doors";
            case "Use Buttons" -> "faction.use.buttons";
            case "Use Levers" -> "faction.use.levers";
            case "Use Containers" -> "faction.use.containers";
            case "Manage Permissions" -> "faction.manage.permissions";
            case "Manage Alliances" -> "faction.manage.alliances";
            default -> guiPerm.toLowerCase().replace(" ", ".");
        };
    }

    private void initComponents(int leftPos, int topPos, double scaleX, double scaleY) {
        if (permissionScrollList == null) {
            permissionScrollList = new ScrollList<>(font, this::renderPermissionItem, sh(24, scaleY));

            List<Permission> permissions = new ArrayList<>();
            permissions.add(new Permission("Invite Members", "Allow inviting new members to the faction"));
            permissions.add(new Permission("Kick Members", "Allow removing members from the faction"));
            permissions.add(new Permission("Claim Territory", "Allow claiming new chunks for the faction"));
            permissions.add(new Permission("Unclaim Territory", "Allow unclaiming chunks"));
            permissions.add(new Permission("Build in Territory", "Allow placing blocks in faction territory"));
            permissions.add(new Permission("Break in Territory", "Allow breaking blocks in faction territory"));
            permissions.add(new Permission("Use Doors", "Allow using doors in faction territory"));
            permissions.add(new Permission("Use Buttons", "Allow using buttons in faction territory"));
            permissions.add(new Permission("Use Levers", "Allow using levers in faction territory"));
            permissions.add(new Permission("Use Containers", "Allow accessing chests and containers"));
            permissions.add(new Permission("Manage Permissions", "Allow modifying rank permissions"));
            permissions.add(new Permission("Manage Alliances", "Allow creating and breaking alliances"));
            permissions.add(new Permission("Manage Economy", "Allow depositing/withdrawing from faction bank"));
            permissions.add(new Permission("Use Teleports", "Allow using faction home and warps"));
            permissions.add(new Permission("Set Home", "Allow setting faction home location"));
            permissions.add(new Permission("Create Warps", "Allow creating faction warps"));
            permissions.add(new Permission("Delete Warps", "Allow deleting faction warps"));
            permissions.add(new Permission("Manage Shop", "Allow buying items from faction shop"));
            permissions.add(new Permission("Access Chest", "Allow accessing faction chest"));
            permissions.add(new Permission("Manage Chest", "Allow depositing to faction chest"));

            permissionScrollList.setItems(permissions);
            permissionScrollList.setOnItemClick(perm -> {
                Set<String> perms = rankPermissions.get(selectedRank);
                String rankId = selectedRank.name.toLowerCase();
                String serverPerm = convertGuiPermToServer(perm.name);

                if (perms.contains(perm.name)) {
                    perms.remove(perm.name);
                    // Envoyer au serveur
                    PacketDistributor.sendToServer(new FactionActionPacket(
                        FactionActionPacket.ActionType.REMOVE_RANK_PERMISSION,
                        rankId,
                        serverPerm
                    ));
                } else {
                    perms.add(perm.name);
                    // Envoyer au serveur
                    PacketDistributor.sendToServer(new FactionActionPacket(
                        FactionActionPacket.ActionType.ADD_RANK_PERMISSION,
                        rankId,
                        serverPerm
                    ));
                }
            });

            // Charger les vraies permissions
            loadRealPermissions();

            int x = sx(CONTENT_X, leftPos, scaleX);
            int y = sy(CONTENT_Y, topPos, scaleY);

            permissionScrollList.setBounds(x, y + sh(49, scaleY), sw(CONTENT_W, scaleX), sh(146, scaleY));

            // Action buttons
            actionButtons.clear();

            StyledButton saveBtn = new StyledButton(font, "Refresh Data", () -> {
                // Recharger les données depuis le serveur
                loadRealPermissions();
            });
            saveBtn.setPrimary(true);
            saveBtn.setBounds(x, y + sh(200, scaleY), sw(85, scaleX), sh(17, scaleY));
            actionButtons.add(saveBtn);

            StyledButton resetBtn = new StyledButton(font, "Reset to Default", () -> {
                System.out.println("SettingsPermissionsPage: Resetting to default permissions");
                rankPermissions.get(Rank.OFFICER).clear();
                rankPermissions.get(Rank.MEMBER).clear();
                rankPermissions.get(Rank.RECRUIT).clear();
                // Re-add defaults
                rankPermissions.get(Rank.OFFICER).addAll(Arrays.asList(
                    "Invite Members", "Kick Members", "Claim Territory", "Unclaim Territory",
                    "Build in Territory", "Break in Territory", "Use Doors", "Use Buttons",
                    "Use Levers", "Use Containers", "Manage Permissions", "Manage Alliances"
                ));
                rankPermissions.get(Rank.MEMBER).addAll(Arrays.asList(
                    "Invite Members", "Claim Territory", "Build in Territory", "Break in Territory",
                    "Use Doors", "Use Buttons", "Use Levers", "Use Containers"
                ));
                rankPermissions.get(Rank.RECRUIT).addAll(Arrays.asList(
                    "Build in Territory", "Break in Territory", "Use Doors", "Use Containers"
                ));
            });
            resetBtn.setBounds(x + sw(91, scaleX), y + sh(200, scaleY), sw(85, scaleX), sh(17, scaleY));
            actionButtons.add(resetBtn);

            StyledButton grantAllBtn = new StyledButton(font, "Grant All to " + selectedRank.name, () -> {
                System.out.println("SettingsPermissionsPage: Granting all permissions to " + selectedRank.name);
                for (Permission perm : permissionScrollList.getItems()) {
                    rankPermissions.get(selectedRank).add(perm.name);
                }
            });
            grantAllBtn.setBounds(x + sw(182, scaleX), y + sh(200, scaleY), sw(85, scaleX), sh(17, scaleY));
            actionButtons.add(grantAllBtn);
        }
    }

    private void renderPermissionItem(GuiGraphics g, Permission perm, int x, int y, int width, int height, boolean hovered, Font font) {
        boolean hasPermission = rankPermissions.get(selectedRank).contains(perm.name);
        int bgColor = hovered ? 0x40667eea : 0xE61e1e2e;
        g.fill(x, y, x + width, y + height, bgColor);
        g.fill(x, y, x + width, y + 1, 0x50667eea);

        // Checkbox (plus petite pour petit GUI)
        int checkSize = 10;
        int checkX = x + 4;
        int checkY = y + 7;
        int checkColor = hasPermission ? 0xFF10b981 : 0xFF2a2a3e;
        g.fill(checkX, checkY, checkX + checkSize, checkY + checkSize, checkColor);
        g.fill(checkX, checkY, checkX + checkSize, checkY + 1, 0x50667eea);

        if (hasPermission) {
            g.drawCenteredString(font, "✓", checkX + checkSize / 2, checkY + 1, 0xFFffffff);
        }

        // Permission name (décalé pour ne pas chevaucher la checkbox)
        g.drawString(font, perm.name, x + 18, y + 3, 0xFFffffff, false);

        // Description (également décalée)
        g.drawString(font, perm.description, x + 18, y + 12, 0xFF9a9aae, false);
    }

    @Override
    public void render(GuiGraphics g, int leftPos, int topPos, double scaleX, double scaleY, int mouseX, int mouseY) {
        initComponents(leftPos, topPos, scaleX, scaleY);

        int x = sx(CONTENT_X, leftPos, scaleX);
        int y = sy(CONTENT_Y, topPos, scaleY);
        int w = sw(CONTENT_W, scaleX);

        // Recalculer les positions à chaque frame pour le scaling
        permissionScrollList.setBounds(x, y + sh(49, scaleY), sw(CONTENT_W, scaleX), sh(146, scaleY));
        if (actionButtons.size() >= 3) {
            actionButtons.get(0).setBounds(x, y + sh(200, scaleY), sw(85, scaleX), sh(17, scaleY));
            actionButtons.get(1).setBounds(x + sw(91, scaleX), y + sh(200, scaleY), sw(85, scaleX), sh(17, scaleY));
            actionButtons.get(2).setBounds(x + sw(182, scaleX), y + sh(200, scaleY), sw(85, scaleX), sh(17, scaleY));
        }

        // Header
        g.fill(x, y, x + w, y + sh(22, scaleY), 0xE61e1e2e);
        g.fill(x, y, x + w, y + 1, 0xFF00d2ff);
        g.drawString(font, "RANK PERMISSIONS", x + sw(9, scaleX), y + sh(9, scaleY), 0xFFffffff, true);

        // Rank selector (3 buttons horizontally)
        int rankY = y + sh(27, scaleY);
        int rankBtnWidth = sw(92, scaleX);
        int rankBtnHeight = sh(17, scaleY);
        int rankSpacing = sw(6, scaleX);

        for (int i = 0; i < Rank.values().length; i++) {
            Rank rank = Rank.values()[i];
            int rankX = x + i * (rankBtnWidth + rankSpacing);
            boolean isSelected = selectedRank == rank;
            boolean isHovered = mouseX >= rankX && mouseX < rankX + rankBtnWidth &&
                               mouseY >= rankY && mouseY < rankY + rankBtnHeight;

            int bgColor = isSelected ? rank.color : (isHovered ? 0xFF3a3a4e : 0xCC2a2a3e);
            g.fill(rankX, rankY, rankX + rankBtnWidth, rankY + rankBtnHeight, bgColor);
            g.fill(rankX, rankY, rankX + rankBtnWidth, rankY + 1, rank.color & 0x80FFFFFF);

            // Center text vertically (mieux espacé)
            g.drawCenteredString(font, rank.name, rankX + rankBtnWidth / 2, rankY + 2, 0xFFffffff);

            // Show permission count (plus bas et mieux espacé)
            int permCount = rankPermissions.get(rank).size();
            g.drawCenteredString(font, String.valueOf(permCount), rankX + rankBtnWidth / 2, rankY + 10, 0xFFa0a0c0);
        }

        // Permissions list
        permissionScrollList.render(g, mouseX, mouseY);

        // Action buttons
        for (StyledButton btn : actionButtons) {
            btn.render(g, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button, int leftPos, int topPos, double scaleX, double scaleY) {
        int x = sx(CONTENT_X, leftPos, scaleX);
        int y = sy(CONTENT_Y, topPos, scaleY);

        // Check rank selector
        int rankY = y + sh(27, scaleY);
        int rankBtnWidth = sw(92, scaleX);
        int rankBtnHeight = sh(17, scaleY);
        int rankSpacing = sw(6, scaleX);

        for (int i = 0; i < Rank.values().length; i++) {
            Rank rank = Rank.values()[i];
            int rankX = x + i * (rankBtnWidth + rankSpacing);

            if (mouseX >= rankX && mouseX < rankX + rankBtnWidth &&
                mouseY >= rankY && mouseY < rankY + rankBtnHeight) {
                selectedRank = rank;
                System.out.println("SettingsPermissionsPage: Selected rank " + rank.name);
                // Update grant all button text
                if (!actionButtons.isEmpty()) {
                    actionButtons.get(2).setText("Grant All to " + selectedRank.name);
                }
                return true;
            }
        }

        // Check permission scroll list
        if (permissionScrollList != null && permissionScrollList.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        // Check buttons
        for (StyledButton btn : actionButtons) {
            if (btn.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button, int leftPos, int topPos, double scaleX, double scaleY) {
        if (permissionScrollList == null) return false;
        return permissionScrollList.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY, int leftPos, int topPos, double scaleX, double scaleY) {
        if (permissionScrollList == null) return false;
        return permissionScrollList.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY, int leftPos, int topPos, double scaleX, double scaleY) {
        if (permissionScrollList == null) return false;
        return permissionScrollList.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
}
