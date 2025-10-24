package fr.eriniumgroup.erinium_faction.core.power;

import fr.eriniumgroup.erinium_faction.common.config.EFConfig;
import fr.eriniumgroup.erinium_faction.core.EFC;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

@EventBusSubscriber
public class PowerManager {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, EFC.MODID);
    public static final net.neoforged.neoforge.registries.DeferredHolder<AttachmentType<?>, AttachmentType<PlayerPower>> PLAYER_POWER = ATTACHMENTS.register("player_power", () -> AttachmentType.serializable(PlayerPower::new).build());

    public static PlayerPower get(ServerPlayer sp) {
        return sp.getData(PLAYER_POWER);
    }

    private static int tickCounter = 0;

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone e) {
        if (!(e.getEntity() instanceof ServerPlayer sp) || !(e.getOriginal() instanceof ServerPlayer orig)) return;
        PlayerPower src = orig.getData(PLAYER_POWER);
        PlayerPower dst = new PlayerPower();
        dst.setMaxPower(src.getMaxPower());
        dst.setPower(src.getPower());
        sp.setData(PLAYER_POWER, dst);
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer sp)) return;
        initDefaults(sp);
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer sp)) return;
        initDefaults(sp);
    }

    private static void initDefaults(ServerPlayer sp) {
        PlayerPower pp = sp.getData(PLAYER_POWER);
        if (pp.getMaxPower() <= 0) {
            pp.setMaxPower(EFConfig.PLAYER_BASE_MAX_POWER.get());
            pp.setPower(EFConfig.PLAYER_BASE_MAX_POWER.get() / 2.0);
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post e) {
        tickCounter++;
        if (tickCounter < 1200) return; // ~1 minute
        tickCounter = 0;
        // regen player power pour les joueurs en ligne
        var server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        double regen = EFConfig.PLAYER_POWER_REGEN_PER_MINUTE.get();
        for (ServerPlayer sp : server.getPlayerList().getPlayers()) {
            PlayerPower pp = sp.getData(PLAYER_POWER);
            double before = pp.getPower();
            pp.addPower(regen);
            if (pp.getPower() != before) {
                // optionnel: notifier/sync via EFVariables ailleurs
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer sp)) return;
        PlayerPower pp = sp.getData(PLAYER_POWER);
        double loss = Math.min(pp.getPower(), EFConfig.PLAYER_POWER_LOSS_ON_DEATH.get());
        if (loss > 0) {
            pp.addPower(-loss);
            // La faction perd aussi cette valeur pour rester cohérente si elle additionne le power des joueurs
            var f = fr.eriniumgroup.erinium_faction.core.faction.FactionManager.getFactionOf(sp.getUUID());
            if (f != null) {
                f.setPower(f.getPower() - loss);
            }
        }
    }
}
