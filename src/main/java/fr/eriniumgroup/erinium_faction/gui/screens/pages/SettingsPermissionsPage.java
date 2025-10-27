package fr.eriniumgroup.erinium_faction.gui.screens.pages;

import fr.eriniumgroup.erinium_faction.common.network.packets.FactionActionPacket;
import fr.eriniumgroup.erinium_faction.core.EFC;
import fr.eriniumgroup.erinium_faction.core.faction.FactionSnapshot;
import fr.eriniumgroup.erinium_faction.gui.screens.components.ScrollList;
import fr.eriniumgroup.erinium_faction.gui.screens.components.StyledButton;
import fr.eriniumgroup.erinium_faction.gui.screens.components.TextHelper;
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
        OFFICER("officer", 0xFFa855f7),
        MEMBER("member", 0xFF3b82f6),
        RECRUIT("recruit", 0xFF6a6a7e);

        final String key;  // Clé de traduction
        final int color;

        Rank(String key, int color) {
            this.key = key;
            this.color = color;
        }

        String getDisplayName() {
            return net.minecraft.network.chat.Component.translatable("erinium_faction.gui.permissions.rank." + key).getString();
        }

        String getId() {
            return key;
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
            if (rank.getDisplayName().equalsIgnoreCase(name) || rank.getId().equalsIgnoreCase(name)) {
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
        // Toujours recharger les permissions depuis le snapshot pour avoir les dernières données
        loadRealPermissions();

        if (permissionScrollList == null) {
            permissionScrollList = new ScrollList<>(font, this::renderPermissionItem, sh(24, scaleY));

            // Charger les permissions depuis les clés de langue
            List<Permission> permissions = new ArrayList<>();
            String[] permKeys = {
                "invite_members", "kick_members", "claim_territory", "unclaim_territory",
                "build", "break", "use_doors", "use_buttons", "use_levers", "use_containers",
                "manage_permissions", "manage_alliances", "manage_economy", "use_teleports",
                "set_home", "create_warps", "delete_warps", "manage_shop", "access_chest", "manage_chest"
            };

            for (String key : permKeys) {
                String name = translate("erinium_faction.gui.permissions." + key);
                String desc = translate("erinium_faction.gui.permissions." + key + ".desc");
                permissions.add(new Permission(name, desc));
            }

            permissionScrollList.setItems(permissions);
            permissionScrollList.setOnItemClick(perm -> {
                Set<String> perms = rankPermissions.get(selectedRank);
                String rankId = selectedRank.getId();
                String serverPerm = convertGuiPermToServer(perm.name);

                if (perms.contains(perm.name)) {
                    // Retirer localement pour feedback immédiat
                    perms.remove(perm.name);
                    // Envoyer au serveur
                    PacketDistributor.sendToServer(new FactionActionPacket(
                        FactionActionPacket.ActionType.REMOVE_RANK_PERMISSION,
                        rankId,
                        serverPerm
                    ));
                } else {
                    // Ajouter localement pour feedback immédiat
                    perms.add(perm.name);
                    // Envoyer au serveur
                    PacketDistributor.sendToServer(new FactionActionPacket(
                        FactionActionPacket.ActionType.ADD_RANK_PERMISSION,
                        rankId,
                        serverPerm
                    ));
                }
            });

            int x = sx(CONTENT_X, leftPos, scaleX);
            int y = sy(CONTENT_Y, topPos, scaleY);

            permissionScrollList.setBounds(x, y + sh(49, scaleY), sw(CONTENT_W, scaleX), sh(146, scaleY));

            // Action buttons
            actionButtons.clear();

            StyledButton saveBtn = new StyledButton(font, translate("erinium_faction.gui.permissions.button.refresh"), () -> {
                // Recharger les données depuis le serveur
                loadRealPermissions();
            });
            saveBtn.setPrimary(true);
            saveBtn.setBounds(x, y + sh(200, scaleY), sw(85, scaleX), sh(17, scaleY));
            actionButtons.add(saveBtn);

            StyledButton resetBtn = new StyledButton(font, translate("erinium_faction.gui.permissions.button.reset"), () -> {
                EFC.log.info("§6Permissions", "§aResetting to default permissions");

                // Définir les permissions par défaut pour chaque rang
                Map<Rank, List<String>> defaultPerms = new HashMap<>();
                defaultPerms.put(Rank.OFFICER, Arrays.asList(
                    "Invite Members", "Kick Members", "Claim Territory", "Unclaim Territory",
                    "Build in Territory", "Break in Territory", "Use Doors", "Use Buttons",
                    "Use Levers", "Use Containers", "Manage Permissions", "Manage Alliances"
                ));
                defaultPerms.put(Rank.MEMBER, Arrays.asList(
                    "Invite Members", "Claim Territory", "Build in Territory", "Break in Territory",
                    "Use Doors", "Use Buttons", "Use Levers", "Use Containers"
                ));
                defaultPerms.put(Rank.RECRUIT, Arrays.asList(
                    "Build in Territory", "Break in Territory", "Use Doors", "Use Containers"
                ));

                // Pour chaque rang, supprimer toutes les permissions actuelles puis ajouter les permissions par défaut
                for (Rank rank : Rank.values()) {
                    String rankId = rank.getId();
                    Set<String> currentPerms = new HashSet<>(rankPermissions.get(rank));

                    // Supprimer toutes les permissions actuelles
                    for (String perm : currentPerms) {
                        String serverPerm = convertGuiPermToServer(perm);
                        PacketDistributor.sendToServer(new FactionActionPacket(
                            FactionActionPacket.ActionType.REMOVE_RANK_PERMISSION,
                            rankId,
                            serverPerm
                        ));
                    }

                    // Ajouter les permissions par défaut
                    List<String> defaults = defaultPerms.getOrDefault(rank, new ArrayList<>());
                    for (String perm : defaults) {
                        String serverPerm = convertGuiPermToServer(perm);
                        PacketDistributor.sendToServer(new FactionActionPacket(
                            FactionActionPacket.ActionType.ADD_RANK_PERMISSION,
                            rankId,
                            serverPerm
                        ));
                    }

                    // Mettre à jour localement pour feedback immédiat
                    rankPermissions.get(rank).clear();
                    rankPermissions.get(rank).addAll(defaults);
                }
            });
            resetBtn.setBounds(x + sw(91, scaleX), y + sh(200, scaleY), sw(85, scaleX), sh(17, scaleY));
            actionButtons.add(resetBtn);

            StyledButton grantAllBtn = new StyledButton(font, translate("erinium_faction.gui.permissions.button.grant_all", selectedRank.getDisplayName()), () -> {
                EFC.log.info("§6Permissions", "§aGranting all permissions to §e{}", selectedRank.getDisplayName());
                String rankId = selectedRank.getId();

                // Pour chaque permission dans la liste
                for (Permission perm : permissionScrollList.getItems()) {
                    // Si la permission n'est pas déjà accordée
                    if (!rankPermissions.get(selectedRank).contains(perm.name)) {
                        String serverPerm = convertGuiPermToServer(perm.name);
                        PacketDistributor.sendToServer(new FactionActionPacket(
                            FactionActionPacket.ActionType.ADD_RANK_PERMISSION,
                            rankId,
                            serverPerm
                        ));
                        // Mettre à jour localement pour feedback immédiat
                        rankPermissions.get(selectedRank).add(perm.name);
                    }
                }
            });
            grantAllBtn.setBounds(x + sw(182, scaleX), y + sh(200, scaleY), sw(85, scaleX), sh(17, scaleY));
            actionButtons.add(grantAllBtn);
        }
    }

    private void renderPermissionItem(GuiGraphics g, Permission perm, int x, int y, int width, int height, boolean hovered, Font font, int mouseX, int mouseY) {
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

        // Permission name with scaling (décalé pour ne pas chevaucher la checkbox)
        int maxNameWidth = width - 22;
        TextHelper.drawScaledText(g, font, perm.name, x + 18, y + 3, maxNameWidth, 0xFFffffff, false);

        // Description with auto-scroll on hover (également décalée)
        boolean descHovered = TextHelper.isPointInBounds(mouseX, mouseY, x + 18, y + 12, maxNameWidth, font.lineHeight);
        TextHelper.drawAutoScrollingText(g, font, perm.description, x + 18, y + 12, maxNameWidth, 0xFF9a9aae, false, descHovered, "perm_desc_" + perm.name);
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
        g.drawString(font, translate("erinium_faction.gui.permissions.title"), x + sw(9, scaleX), y + sh(9, scaleY), 0xFFffffff, true);

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
            g.drawCenteredString(font, rank.getDisplayName(), rankX + rankBtnWidth / 2, rankY + 2, 0xFFffffff);

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

        // Check permission scroll list FIRST (avant les rank buttons)
        if (permissionScrollList != null) {
            boolean handled = permissionScrollList.mouseClicked(mouseX, mouseY, button);
            if (handled) {
                return true;
            }
        }

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
                // Update grant all button text
                if (!actionButtons.isEmpty()) {
                    actionButtons.get(2).setText(translate("erinium_faction.gui.permissions.button.grant_all", selectedRank.getDisplayName()));
                }
                return true;
            }
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
