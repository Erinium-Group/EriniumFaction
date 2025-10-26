package fr.eriniumgroup.erinium_faction.player.level.gui;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Item qui ouvre l'interface de distribution des points
 */
public class PlayerStatsItem extends Item {

    public PlayerStatsItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            openStatsMenu(serverPlayer);
        }

        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    public static void openStatsMenu(ServerPlayer player) {
        player.openMenu(new SimpleMenuProvider((id, inventory, p) -> new PlayerStatsMenu(id, inventory), Component.literal("Statistiques du Joueur")));
    }
}

