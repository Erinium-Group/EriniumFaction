package fr.eriniumgroup.erinium_faction.features.homes;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;
import java.util.UUID;

/**
 * Gestionnaire des homes
 */
public class HomesManager {

    public static Optional<HomeData> getHome(ServerPlayer player, String homeName) {
        UUID uuid = player.getUUID();
        var server = player.getServer();
        if (server == null) return Optional.empty();

        HomesSavedData data = HomesSavedData.get(server);
        var playerHomes = data.getPlayerHomes(uuid);
        return playerHomes.flatMap(h -> h.getHome(homeName));
    }

    public static void setHome(ServerPlayer player, String homeName) {
        UUID uuid = player.getUUID();
        var server = player.getServer();
        if (server == null) return;

        HomesSavedData data = HomesSavedData.get(server);
        PlayerHomesData playerHomes = data.getOrCreatePlayerHomes(uuid);

        HomeData home = new HomeData(homeName, player.level().dimension().location().toString(), player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());

        playerHomes.setHome(homeName, home);
        data.setDirty();
    }

    public static boolean deleteHome(ServerPlayer player, String homeName) {
        UUID uuid = player.getUUID();
        var server = player.getServer();
        if (server == null) return false;

        HomesSavedData data = HomesSavedData.get(server);
        var playerHomes = data.getPlayerHomes(uuid);
        return playerHomes.map(h -> {
            boolean removed = h.removeHome(homeName);
            if (removed) data.setDirty();
            return removed;
        }).orElse(false);
    }

    public static void teleportHome(ServerPlayer player, String homeName) {
        var server = player.getServer();
        if (server == null) return;

        HomesConfig config = HomesConfig.get(server);
        // démarrer warmup/cooldown
        HomeTeleportService.tryStartTeleport(player, homeName);
    }

    /**
     * Exécute la téléportation sans délai, utilisé par le service une fois le warmup terminé.
     */
    public static void performTeleport(ServerPlayer player, String homeName) {
        var server = player.getServer();
        if (server == null) return;

        var playerHomes = HomesSavedData.get(server).getPlayerHomes(player.getUUID());
        if (playerHomes.isEmpty()) return;

        var home = playerHomes.get().getHome(homeName);
        if (home.isEmpty()) return;

        HomeData homeData = home.get();
        ServerLevel level = server.getLevel(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, net.minecraft.resources.ResourceLocation.parse(homeData.getDimension())));
        if (level == null) return;

        // Respect cross-dimension config
        HomesConfig config = HomesConfig.get(server);
        if (!config.isAllowCrossDimensionTeleport() && !level.dimension().equals(player.level().dimension())) {
            player.displayClientMessage(net.minecraft.network.chat.Component.translatable("erinium_faction.tp.crossdim_denied"), true);
            return;
        }

        player.teleportTo(level, homeData.getX(), homeData.getY(), homeData.getZ(), homeData.getYaw(), homeData.getPitch());
    }

    public static Optional<PlayerHomesData> getPlayerHomes(ServerPlayer player) {
        var server = player.getServer();
        if (server == null) return Optional.empty();
        return Optional.ofNullable(HomesSavedData.get(server).getPlayerHomes(player.getUUID()).orElse(null));
    }

    public static HomesConfig getConfig(ServerPlayer player) {
        var server = player.getServer();
        if (server == null) return new HomesConfig();
        return HomesConfig.get(server);
    }
}
