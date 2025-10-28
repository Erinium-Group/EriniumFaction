package fr.eriniumgroup.erinium_faction.gui.screens.pages;

import fr.eriniumgroup.erinium_faction.common.network.packets.FactionActionPacket;
import fr.eriniumgroup.erinium_faction.core.EFC;
import fr.eriniumgroup.erinium_faction.core.faction.FactionSnapshot;
import fr.eriniumgroup.erinium_faction.gui.screens.components.ScrollList;
import fr.eriniumgroup.erinium_faction.gui.screens.components.StyledButton;
import fr.eriniumgroup.erinium_faction.gui.screens.components.TextHelper;
import fr.eriniumgroup.erinium_faction.gui.screens.components.ImageRenderer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

/**
 * Page Settings Permissions - Basée sur settings-permissions.svg
 * Permissions par rang avec layout vertical des ranks et scroll list des permissions
 */
public class SettingsPermissionsPage extends FactionPage {

    // Textures pour les checkboxes
    private static final ResourceLocation CHECKBOX_UNCHECKED = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/permissions/checkbox-unchecked.png");
    private static final ResourceLocation CHECKBOX_CHECKED = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/permissions/checkbox-checked.png");
    private static final ResourceLocation CHECKBOX_HOVER = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/permissions/checkbox-hover.png");

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
        String key;         // Clé de traduction (ex: "invite_members")
        String name;        // Nom traduit (ex: "Inviter des membres")
        String description; // Description traduite

        Permission(String key, String name, String description) {
            this.key = key;
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
                    EFC.log.info("§6Permissions", "§aLoading permissions for rank §e{} §a({} perms)", rank.getDisplayName(), rankInfo.perms.size());

                    // Convertir les permissions du format serveur au format GUI
                    for (String perm : rankInfo.perms) {
                        String guiPerm = convertServerPermToGui(perm);
                        if (guiPerm != null) {
                            rankPermissions.get(rank).add(guiPerm);
                            EFC.log.debug("§6Permissions", "  §a- §e{} §a-> §e{}", perm, guiPerm);
                        } else {
                            EFC.log.warn("§6Permissions", "  §c- §e{} §c-> §cNO MAPPING!", perm);
                        }
                    }

                    EFC.log.info("§6Permissions", "§aLoaded §e{} §aGUI permissions for §e{}", rankPermissions.get(rank).size(), rank.getDisplayName());
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
        // Mapping complet de toutes les permissions
        return switch (serverPerm) {
            case "faction.invite" -> translate("erinium_faction.gui.permissions.invite_members");
            case "faction.kick" -> translate("erinium_faction.gui.permissions.kick_members");
            case "faction.claim" -> translate("erinium_faction.gui.permissions.claim_territory");
            case "faction.unclaim" -> translate("erinium_faction.gui.permissions.unclaim_territory");
            case "faction.build" -> translate("erinium_faction.gui.permissions.build");
            case "faction.break" -> translate("erinium_faction.gui.permissions.break");
            case "faction.use.doors" -> translate("erinium_faction.gui.permissions.use_doors");
            case "faction.use.buttons" -> translate("erinium_faction.gui.permissions.use_buttons");
            case "faction.use.levers" -> translate("erinium_faction.gui.permissions.use_levers");
            case "faction.use.containers" -> translate("erinium_faction.gui.permissions.use_containers");
            case "faction.manage.permissions" -> translate("erinium_faction.gui.permissions.manage_permissions");
            case "faction.manage.alliances" -> translate("erinium_faction.gui.permissions.manage_alliances");
            case "faction.manage.economy" -> translate("erinium_faction.gui.permissions.manage_economy");
            case "faction.use.teleports" -> translate("erinium_faction.gui.permissions.use_teleports");
            case "faction.set.home" -> translate("erinium_faction.gui.permissions.set_home");
            case "faction.create.warps" -> translate("erinium_faction.gui.permissions.create_warps");
            case "faction.delete.warps" -> translate("erinium_faction.gui.permissions.delete_warps");
            case "faction.manage.shop" -> translate("erinium_faction.gui.permissions.manage_shop");
            case "faction.access.chest" -> translate("erinium_faction.gui.permissions.access_chest");
            case "faction.manage.chest" -> translate("erinium_faction.gui.permissions.manage_chest");
            default -> null;
        };
    }

    private String convertKeyToServer(String key) {
        // Convertir la clé de traduction en permission serveur
        return switch (key) {
            case "invite_members" -> "faction.invite";
            case "kick_members" -> "faction.kick";
            case "claim_territory" -> "faction.claim";
            case "unclaim_territory" -> "faction.unclaim";
            case "build" -> "faction.build";
            case "break" -> "faction.break";
            case "use_doors" -> "faction.use.doors";
            case "use_buttons" -> "faction.use.buttons";
            case "use_levers" -> "faction.use.levers";
            case "use_containers" -> "faction.use.containers";
            case "manage_permissions" -> "faction.manage.permissions";
            case "manage_alliances" -> "faction.manage.alliances";
            case "manage_economy" -> "faction.manage.economy";
            case "use_teleports" -> "faction.use.teleports";
            case "set_home" -> "faction.set.home";
            case "create_warps" -> "faction.create.warps";
            case "delete_warps" -> "faction.delete.warps";
            case "manage_shop" -> "faction.manage.shop";
            case "access_chest" -> "faction.access.chest";
            case "manage_chest" -> "faction.manage.chest";
            default -> null;
        };
    }

    private String convertGuiPermToServer(String guiPerm) {
        // Utiliser une approche inverse : tester toutes les traductions
        String inviteMembers = translate("erinium_faction.gui.permissions.invite_members");
        String kickMembers = translate("erinium_faction.gui.permissions.kick_members");
        String claimTerritory = translate("erinium_faction.gui.permissions.claim_territory");
        String unclaimTerritory = translate("erinium_faction.gui.permissions.unclaim_territory");
        String build = translate("erinium_faction.gui.permissions.build");
        String breakPerm = translate("erinium_faction.gui.permissions.break");
        String useDoors = translate("erinium_faction.gui.permissions.use_doors");
        String useButtons = translate("erinium_faction.gui.permissions.use_buttons");
        String useLevers = translate("erinium_faction.gui.permissions.use_levers");
        String useContainers = translate("erinium_faction.gui.permissions.use_containers");
        String managePermissions = translate("erinium_faction.gui.permissions.manage_permissions");
        String manageAlliances = translate("erinium_faction.gui.permissions.manage_alliances");
        String manageEconomy = translate("erinium_faction.gui.permissions.manage_economy");
        String useTeleports = translate("erinium_faction.gui.permissions.use_teleports");
        String setHome = translate("erinium_faction.gui.permissions.set_home");
        String createWarps = translate("erinium_faction.gui.permissions.create_warps");
        String deleteWarps = translate("erinium_faction.gui.permissions.delete_warps");
        String manageShop = translate("erinium_faction.gui.permissions.manage_shop");
        String accessChest = translate("erinium_faction.gui.permissions.access_chest");
        String manageChest = translate("erinium_faction.gui.permissions.manage_chest");

        if (guiPerm.equals(inviteMembers)) return "faction.invite";
        if (guiPerm.equals(kickMembers)) return "faction.kick";
        if (guiPerm.equals(claimTerritory)) return "faction.claim";
        if (guiPerm.equals(unclaimTerritory)) return "faction.unclaim";
        if (guiPerm.equals(build)) return "faction.build";
        if (guiPerm.equals(breakPerm)) return "faction.break";
        if (guiPerm.equals(useDoors)) return "faction.use.doors";
        if (guiPerm.equals(useButtons)) return "faction.use.buttons";
        if (guiPerm.equals(useLevers)) return "faction.use.levers";
        if (guiPerm.equals(useContainers)) return "faction.use.containers";
        if (guiPerm.equals(managePermissions)) return "faction.manage.permissions";
        if (guiPerm.equals(manageAlliances)) return "faction.manage.alliances";
        if (guiPerm.equals(manageEconomy)) return "faction.manage.economy";
        if (guiPerm.equals(useTeleports)) return "faction.use.teleports";
        if (guiPerm.equals(setHome)) return "faction.set.home";
        if (guiPerm.equals(createWarps)) return "faction.create.warps";
        if (guiPerm.equals(deleteWarps)) return "faction.delete.warps";
        if (guiPerm.equals(manageShop)) return "faction.manage.shop";
        if (guiPerm.equals(accessChest)) return "faction.access.chest";
        if (guiPerm.equals(manageChest)) return "faction.manage.chest";

        // Pas de fallback - retourner null si pas reconnu
        EFC.log.warn("§6Permissions", "§cUnknown permission GUI: §e{}", guiPerm);
        return null;
    }

    private boolean componentsInitialized = false;

    private void initComponents(int leftPos, int topPos, double scaleX, double scaleY) {
        // Charger les permissions réelles au premier chargement
        if (!componentsInitialized) {
            loadRealPermissions();
            componentsInitialized = true;
        }

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
                permissions.add(new Permission(key, name, desc));
            }

            permissionScrollList.setItems(permissions);
            permissionScrollList.setOnItemClick(perm -> {
                Set<String> perms = rankPermissions.get(selectedRank);
                String rankId = selectedRank.getId();
                String serverPerm = convertKeyToServer(perm.key);

                if (serverPerm == null) {
                    EFC.log.error("§6Permissions", "§cCannot convert permission key §e{} §cto server format", perm.key);
                    return;
                }

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

                // Définir les permissions par défaut pour chaque rang (utiliser les CLÉS)
                Map<Rank, List<String>> defaultPermKeys = new HashMap<>();
                defaultPermKeys.put(Rank.OFFICER, Arrays.asList(
                    "invite_members", "kick_members", "claim_territory", "unclaim_territory",
                    "build", "break", "use_doors", "use_buttons", "use_levers", "use_containers",
                    "manage_permissions", "manage_alliances"
                ));
                defaultPermKeys.put(Rank.MEMBER, Arrays.asList(
                    "invite_members", "claim_territory", "build", "break",
                    "use_doors", "use_buttons", "use_levers", "use_containers"
                ));
                defaultPermKeys.put(Rank.RECRUIT, Arrays.asList(
                    "build", "break", "use_doors", "use_containers"
                ));

                // Pour chaque rang, supprimer toutes les permissions actuelles puis ajouter les permissions par défaut
                for (Rank rank : Rank.values()) {
                    String rankId = rank.getId();
                    Set<String> currentPerms = new HashSet<>(rankPermissions.get(rank));

                    // Supprimer toutes les permissions actuelles
                    for (String perm : currentPerms) {
                        String serverPerm = convertGuiPermToServer(perm);
                        if (serverPerm != null) {
                            PacketDistributor.sendToServer(new FactionActionPacket(
                                FactionActionPacket.ActionType.REMOVE_RANK_PERMISSION,
                                rankId,
                                serverPerm
                            ));
                        }
                    }

                    // Ajouter les permissions par défaut
                    List<String> defaultKeys = defaultPermKeys.getOrDefault(rank, new ArrayList<>());
                    Set<String> defaultPermsTranslated = new HashSet<>();
                    for (String key : defaultKeys) {
                        String serverPerm = convertKeyToServer(key);
                        if (serverPerm != null) {
                            PacketDistributor.sendToServer(new FactionActionPacket(
                                FactionActionPacket.ActionType.ADD_RANK_PERMISSION,
                                rankId,
                                serverPerm
                            ));
                            // Stocker la traduction pour la mise à jour locale
                            defaultPermsTranslated.add(translate("erinium_faction.gui.permissions." + key));
                        }
                    }

                    // Mettre à jour localement pour feedback immédiat
                    rankPermissions.get(rank).clear();
                    rankPermissions.get(rank).addAll(defaultPermsTranslated);
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
                        String serverPerm = convertKeyToServer(perm.key);
                        if (serverPerm != null) {
                            PacketDistributor.sendToServer(new FactionActionPacket(
                                FactionActionPacket.ActionType.ADD_RANK_PERMISSION,
                                rankId,
                                serverPerm
                            ));
                            // Mettre à jour localement pour feedback immédiat
                            rankPermissions.get(selectedRank).add(perm.name);
                        }
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

        // Checkbox (plus petite pour petit GUI) - Utiliser les images
        int checkSize = 10;
        int checkX = x + 4;
        int checkY = y + 7;

        // Déterminer quelle checkbox afficher
        boolean checkHovered = mouseX >= checkX && mouseX < checkX + checkSize &&
                              mouseY >= checkY && mouseY < checkY + checkSize;
        ResourceLocation checkboxTexture;
        if (checkHovered) {
            checkboxTexture = CHECKBOX_HOVER;
        } else if (hasPermission) {
            checkboxTexture = CHECKBOX_CHECKED;
        } else {
            checkboxTexture = CHECKBOX_UNCHECKED;
        }
        ImageRenderer.renderScaledImage(g, checkboxTexture, checkX, checkY, checkSize, checkSize);

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

            // Center text vertically avec troncature
            int maxRankTextWidth = rankBtnWidth - 4;
            TextHelper.drawCenteredScaledText(g, font, rank.getDisplayName(), rankX + rankBtnWidth / 2, rankY + 2, maxRankTextWidth, 0xFFffffff);

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
