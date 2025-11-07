package fr.eriniumgroup.erinium_faction.init;

import fr.eriniumgroup.erinium_faction.common.network.packets.MenuStateUpdateMessage;
import fr.eriniumgroup.erinium_faction.core.EFC;
import fr.eriniumgroup.erinium_faction.gui.menus.FactionMenu;
import fr.eriniumgroup.erinium_faction.gui.menus.PlayerStatsMenu;
import fr.eriniumgroup.erinium_faction.gui.menus.TitaniumCompressorMenu;
import fr.eriniumgroup.erinium_faction.gui.menus.EriniumChestMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Map;

public class EFMenus {
    public static final DeferredRegister<MenuType<?>> REGISTER = DeferredRegister.create(Registries.MENU, EFC.MODID);
    public static final DeferredHolder<MenuType<?>, MenuType<FactionMenu>> FACTION_MENU = REGISTER.register("faction_menu", () -> IMenuTypeExtension.create(FactionMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<PlayerStatsMenu>> PLAYER_STATS_MENU = REGISTER.register("player_stats_menu", () -> IMenuTypeExtension.create(PlayerStatsMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<TitaniumCompressorMenu>> TITANIUM_COMPRESSOR_MENU = REGISTER.register("titanium_compressor_menu", () -> IMenuTypeExtension.create(TitaniumCompressorMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<EriniumChestMenu>> ERINIUM_CHEST = REGISTER.register("erinium_chest", () -> IMenuTypeExtension.create(EriniumChestMenu::new));

    public interface MenuAccessor {
        Map<String, Object> getMenuState();

        Map<Integer, Slot> getSlots();

        default void sendMenuStateUpdate(Player player, int elementType, String name, Object elementState, boolean needClientUpdate) {
            getMenuState().put(elementType + ":" + name, elementState);
            if (player instanceof ServerPlayer serverPlayer) {
                PacketDistributor.sendToPlayer(serverPlayer, new MenuStateUpdateMessage(elementType, name, elementState));
            } else if (player.level().isClientSide) {
                try {
                    Class.forName("fr.eriniumgroup.erinium_faction.client.ClientMenuHelper");
                    fr.eriniumgroup.erinium_faction.client.ClientMenuHelper.localScreenUpdate(elementType, name, elementState, needClientUpdate);
                } catch (Throwable ignored) {
                    // en environnement serveur, la classe client n'existe pas
                }
                PacketDistributor.sendToServer(new MenuStateUpdateMessage(elementType, name, elementState));
            }
        }

        default <T> T getMenuState(int elementType, String name, T defaultValue) {
            try {
                return (T) getMenuState().getOrDefault(elementType + ":" + name, defaultValue);
            } catch (ClassCastException e) {
                return defaultValue;
            }
        }
    }

    private EFMenus() {
    }
}
