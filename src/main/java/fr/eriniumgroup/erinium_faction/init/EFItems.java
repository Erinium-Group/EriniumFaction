package fr.eriniumgroup.erinium_faction.init;

import fr.eriniumgroup.erinium_faction.core.EFC;
import fr.eriniumgroup.erinium_faction.player.level.item.StatsResetTokenItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class EFItems {
    public static final DeferredRegister<Item> REGISTER = DeferredRegister.create(Registries.ITEM, EFC.MOD_ID);

    public static final DeferredHolder<Item, StatsResetTokenItem> STATS_RESET_TOKEN = REGISTER.register("stats_reset_token", () -> new StatsResetTokenItem(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON)));

    private EFItems() {
    }
}
