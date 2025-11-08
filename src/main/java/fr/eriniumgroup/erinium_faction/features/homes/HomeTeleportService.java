package fr.eriniumgroup.erinium_faction.features.homes;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Gère le warmup et le cooldown pour /home.
 * Tolérance: ignorer les changements de hauteur (Y) pour éviter les faux positifs
 * dus à l'auto-jump, slabs, carpets, etc. Seul un déplacement horizontal (X/Z) ou
 * un changement de dimension annule.
 */
public class HomeTeleportService {

    private static final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final Map<UUID, PendingTeleport> pendings = new HashMap<>();
    private static final Map<UUID, BlockPos> startBlockPositions = new HashMap<>();
    private static final Map<UUID, String> startDimensions = new HashMap<>();
    private static boolean registered = false;

    public static void init() {
        // Enregistrement différé : on s'abonne lors du premier /home
    }

    private static void ensureRegistered() {
        if (registered) return;
        NeoForge.EVENT_BUS.addListener(HomeTeleportService::onServerTick);
        NeoForge.EVENT_BUS.addListener(HomeTeleportService::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(HomeTeleportService::onHurt);
        registered = true;
    }

    public static boolean tryStartTeleport(ServerPlayer player, String homeName) {
        ensureRegistered();
        MinecraftServer server = player.getServer();
        if (server == null) return false;
        HomesConfig cfg = HomesConfig.get(server);

        long now = server.getTickCount();
        long cdTicks = cfg.getCooldownSeconds() * 20L;
        long nextAllowed = cooldowns.getOrDefault(player.getUUID(), 0L);
        if (now < nextAllowed) {
            long remain = (nextAllowed - now + 19) / 20;
            player.displayClientMessage(Component.translatable("erinium_faction.tp.cooldown", remain), true);
            return false;
        }

        int warmup = cfg.getWarmupSeconds();
        if (warmup <= 0) {
            HomesManager.performTeleport(player, homeName);
            cooldowns.put(player.getUUID(), now + cdTicks);
            return true;
        }
        long finishAt = now + warmup * 20L;
        UUID id = player.getUUID();
        pendings.put(id, new PendingTeleport(homeName, finishAt));
        startBlockPositions.put(id, player.blockPosition());
        startDimensions.put(id, player.level().dimension().location().toString());
        player.displayClientMessage(Component.translatable("erinium_faction.tp.warmup", warmup), true);
        return true;
    }

    public static void onServerTick(ServerTickEvent.Post evt) {
        MinecraftServer server = evt.getServer();
        long now = server.getTickCount();
        if (pendings.isEmpty()) return;
        pendings.entrySet().removeIf(entry -> {
            UUID id = entry.getKey();
            PendingTeleport pt = entry.getValue();
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            if (player == null) {
                cleanup(id);
                return true; // drop
            }
            if (now >= pt.finishTick) {
                HomesManager.performTeleport(player, pt.homeName);
                long cdTicks = HomesConfig.get(server).getCooldownSeconds() * 20L;
                cooldowns.put(id, now + cdTicks);
                cleanup(id);
                return true; // done
            }
            return false;
        });
    }

    public static void onPlayerTick(PlayerTickEvent.Post evt) {
        if (!(evt.getEntity() instanceof ServerPlayer sp)) return;
        UUID id = sp.getUUID();
        if (!pendings.containsKey(id)) return;
        BlockPos startBlock = startBlockPositions.get(id);
        String dim = startDimensions.get(id);
        if (startBlock == null || dim == null) return;
        BlockPos current = sp.blockPosition();
        boolean horizontalMoved = (current.getX() != startBlock.getX()) || (current.getZ() != startBlock.getZ());
        boolean dimChanged = !sp.level().dimension().location().toString().equals(dim);
        if (horizontalMoved || dimChanged) {
            cancelOnMove(sp);
        }
    }

    public static void onHurt(LivingDamageEvent.Pre evt) {
        if (!(evt.getEntity() instanceof ServerPlayer sp)) return;
        if (pendings.remove(sp.getUUID()) != null) {
            cleanup(sp.getUUID());
            sp.displayClientMessage(Component.translatable("erinium_faction.tp.canceled.damage"), true);
        }
    }

    public static void cancelOnMove(ServerPlayer player) {
        if (pendings.remove(player.getUUID()) != null) {
            cleanup(player.getUUID());
            player.displayClientMessage(Component.translatable("erinium_faction.tp.canceled.move"), true);
        }
    }

    private static void cleanup(UUID id) {
        startBlockPositions.remove(id);
        startDimensions.remove(id);
    }

    private record PendingTeleport(String homeName, long finishTick) {
    }
}
