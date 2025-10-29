package fr.eriniumgroup.erinium_faction.gui.screens.pages;

import fr.eriniumgroup.erinium_faction.common.network.packets.FactionActionPacket;
import fr.eriniumgroup.erinium_faction.core.EFC;
import fr.eriniumgroup.erinium_faction.core.faction.FactionSnapshot;
import fr.eriniumgroup.erinium_faction.core.faction.Permission;
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

    private static class PermissionDisplay {
        Permission permission; // Permission enum
        String name;           // Nom traduit (ex: "Inviter des membres")
        String description;    // Description traduite

        PermissionDisplay(Permission permission, String name, String description) {
            this.permission = permission;
            this.name = name;
            this.description = description;
        }
    }

    private Rank selectedRank = Rank.OFFICER;
    private ScrollList<PermissionDisplay> permissionScrollList;
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
        // Utiliser l'enum pour la conversion
        Permission perm = Permission.fromServerKey(serverPerm);
        if (perm != null) {
            return translate(perm.getTranslationKey());
        }

        // Compatibilité avec les anciennes clés (faction.build -> block.place, etc.)
        if ("faction.build".equals(serverPerm)) {
            perm = Permission.BUILD;
        } else if ("faction.break".equals(serverPerm)) {
            perm = Permission.BREAK;
        }

        if (perm != null) {
            return translate(perm.getTranslationKey());
        }

        EFC.log.warn("§6Permissions", "§cUnknown server permission: §e{}", serverPerm);
        return null;
    }

    private String convertGuiPermToServer(String guiPerm) {
        // Rechercher dans toutes les permissions celle qui correspond à la traduction
        for (Permission perm : Permission.all()) {
            String translated = translate(perm.getTranslationKey());
            if (translated.equals(guiPerm)) {
                return perm.getServerKey();
            }
        }

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

            // Charger les permissions depuis l'enum
            List<PermissionDisplay> permissions = new ArrayList<>();
            for (Permission perm : Permission.all()) {
                String name = translate(perm.getTranslationKey());
                String desc = translate(perm.getDescriptionKey());
                permissions.add(new PermissionDisplay(perm, name, desc));
            }

            permissionScrollList.setItems(permissions);
            permissionScrollList.setOnItemClick(permDisplay -> {
                Set<String> perms = rankPermissions.get(selectedRank);
                String rankId = selectedRank.getId();
                String serverPerm = permDisplay.permission.getServerKey();

                if (perms.contains(permDisplay.name)) {
                    // Retirer localement pour feedback immédiat
                    perms.remove(permDisplay.name);
                    // Envoyer au serveur
                    PacketDistributor.sendToServer(new FactionActionPacket(
                        FactionActionPacket.ActionType.REMOVE_RANK_PERMISSION,
                        rankId,
                        serverPerm
                    ));
                } else {
                    // Ajouter localement pour feedback immédiat
                    perms.add(permDisplay.name);
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

                // Définir les permissions par défaut pour chaque rang (utiliser l'enum)
                Map<Rank, List<Permission>> defaultPerms = new HashMap<>();
                defaultPerms.put(Rank.OFFICER, Arrays.asList(
                    Permission.INVITE_MEMBERS, Permission.KICK_MEMBERS, Permission.CLAIM_TERRITORY, Permission.UNCLAIM_TERRITORY,
                    Permission.BUILD, Permission.BREAK, Permission.INTERACT, Permission.USE_CONTAINERS,
                    Permission.MANAGE_PERMISSIONS, Permission.MANAGE_ALLIANCES
                ));
                defaultPerms.put(Rank.MEMBER, Arrays.asList(
                    Permission.INVITE_MEMBERS, Permission.CLAIM_TERRITORY, Permission.BUILD, Permission.BREAK, Permission.INTERACT, Permission.USE_CONTAINERS
                ));
                defaultPerms.put(Rank.RECRUIT, Arrays.asList(
                    Permission.BUILD, Permission.BREAK, Permission.INTERACT, Permission.USE_CONTAINERS
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
                    List<Permission> defaults = defaultPerms.getOrDefault(rank, new ArrayList<>());
                    Set<String> defaultPermsTranslated = new HashSet<>();
                    for (Permission perm : defaults) {
                        PacketDistributor.sendToServer(new FactionActionPacket(
                            FactionActionPacket.ActionType.ADD_RANK_PERMISSION,
                            rankId,
                            perm.getServerKey()
                        ));
                        // Stocker la traduction pour la mise à jour locale
                        defaultPermsTranslated.add(translate(perm.getTranslationKey()));
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
                for (PermissionDisplay permDisplay : permissionScrollList.getItems()) {
                    // Si la permission n'est pas déjà accordée
                    if (!rankPermissions.get(selectedRank).contains(permDisplay.name)) {
                        PacketDistributor.sendToServer(new FactionActionPacket(
                            FactionActionPacket.ActionType.ADD_RANK_PERMISSION,
                            rankId,
                            permDisplay.permission.getServerKey()
                        ));
                        // Mettre à jour localement pour feedback immédiat
                        rankPermissions.get(selectedRank).add(permDisplay.name);
                    }
                }
            });
            grantAllBtn.setBounds(x + sw(182, scaleX), y + sh(200, scaleY), sw(85, scaleX), sh(17, scaleY));
            actionButtons.add(grantAllBtn);
        }
    }

    private void renderPermissionItem(GuiGraphics g, PermissionDisplay perm, int x, int y, int width, int height, boolean hovered, Font font, int mouseX, int mouseY) {
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
