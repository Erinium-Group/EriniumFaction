package fr.eriniumgroup.erinium_faction.features.homes;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Gère le warmup et le cooldown pour /home.
 */
@EventBusSubscriber
public class HomeTeleportService {

    private static final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final Map<UUID, PendingTeleport> pendings = new HashMap<>();
    private static final Map<UUID, Vec3> startPositions = new HashMap<>();
    private static final Map<UUID, String> startDimensions = new HashMap<>();

    public static void init() {
        // rien: @EventBusSubscriber gère l'abonnement
    }

    public static boolean tryStartTeleport(ServerPlayer player, String homeName) {
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
        pendings.put(player.getUUID(), new PendingTeleport(homeName, finishAt));
        startPositions.put(player.getUUID(), player.position());
        startDimensions.put(player.getUUID(), player.level().dimension().location().toString());
        player.displayClientMessage(Component.translatable("erinium_faction.tp.warmup", warmup), true);
        return true;
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post evt) {
        MinecraftServer server = evt.getServer();
        long now = server.getTickCount();
        if (pendings.isEmpty()) return;
        pendings.entrySet().removeIf(entry -> {
            UUID id = entry.getKey();
            PendingTeleport pt = entry.getValue();
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            if (player == null) {
                startPositions.remove(id);
                startDimensions.remove(id);
                return true; // drop
            }
            if (now >= pt.finishTick) {
                HomesManager.performTeleport(player, pt.homeName);
                long cdTicks = HomesConfig.get(server).getCooldownSeconds() * 20L;
                cooldowns.put(id, now + cdTicks);
                startPositions.remove(id);
                startDimensions.remove(id);
                return true; // done
            }
            return false;
        });
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post evt) {
        if (!(evt.getEntity() instanceof ServerPlayer sp)) return;
        UUID id = sp.getUUID();
        if (!pendings.containsKey(id)) return;
        Vec3 start = startPositions.get(id);
        String dim = startDimensions.get(id);
        if (start == null || dim == null) return;
        // Annuler si changement de dimension ou mouvement significatif (>0.1 bloc) ou changement de bloc
        Vec3 current = sp.position();
        double dx = current.x - start.x;
        double dy = current.y - start.y;
        double dz = current.z - start.z;
        boolean moved = (dx * dx + dy * dy + dz * dz) > 0.01; // ~0.1 bloc
        boolean blockChanged = sp.blockPosition().getX() != (int) start.x || sp.blockPosition().getY() != (int) start.y || sp.blockPosition().getZ() != (int) start.z;
        boolean dimChanged = !sp.level().dimension().location().toString().equals(dim);
        if (moved || blockChanged || dimChanged) {
            cancelOnMove(sp);
        }
    }

    @SubscribeEvent
    public static void onHurt(LivingDamageEvent.Pre evt) {
        if (!(evt.getEntity() instanceof ServerPlayer sp)) return;
        if (pendings.remove(sp.getUUID()) != null) {
            startPositions.remove(sp.getUUID());
            startDimensions.remove(sp.getUUID());
            sp.displayClientMessage(Component.translatable("erinium_faction.tp.canceled.damage"), true);
        }
    }

    public static void cancelOnMove(ServerPlayer player) {
        if (pendings.remove(player.getUUID()) != null) {
            startPositions.remove(player.getUUID());
            startDimensions.remove(player.getUUID());
            player.displayClientMessage(Component.translatable("erinium_faction.tp.canceled.move"), true);
        }
    }

    private record PendingTeleport(String homeName, long finishTick) {
    }
}
