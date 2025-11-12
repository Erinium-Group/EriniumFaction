package fr.eriniumgroup.erinium_faction.init;

import fr.eriniumgroup.erinium_faction.core.EFC;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Onglet créatif personnalisé pour Erinium Faction
 */
public class EFCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> REGISTER = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, EFC.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ERINIUM_FACTION_TAB = REGISTER.register("erinium_faction_tab", () -> CreativeModeTab.builder()
        .title(Component.translatable("itemGroup.erinium_faction"))
        .icon(() -> new ItemStack(EFItems.STATS_RESET_TOKEN.get()))
        .displayItems((parameters, output) -> {
            output.accept(EFItems.STATS_RESET_TOKEN.get());
            // Blocs
            output.accept(EFBlocks.SILVER_ORE.get());
            output.accept(EFBlocks.DEEPSLATE_SILVER_ORE.get());
            output.accept(EFBlocks.SILVER_BLOCK.get());
            output.accept(EFBlocks.TITANIUM_ORE.get());
            output.accept(EFBlocks.DEEPSLATE_TITANIUM_ORE.get());
            output.accept(EFBlocks.TITANIUM_BLOCK.get());
            output.accept(EFBlocks.ERINIUM_ORE.get());
            output.accept(EFBlocks.DEEPSLATE_ERINIUM_ORE.get());
            output.accept(EFBlocks.ERINIUM_BLOCK.get());
            // Machines
            output.accept(EFBlocks.TITANIUM_COMPRESSOR.get());
            // Batteries (retirées)
            // Coffre Erinium
            output.accept(EFBlocks.ERINIUM_CHEST.get());
            // Items silver
            output.accept(EFItems.RAW_SILVER.get());
            output.accept(EFItems.SILVER_INGOT.get());
            output.accept(EFItems.SILVER_SCRAP.get());
            // Items titanium
            output.accept(EFItems.RAW_TITANIUM.get());
            output.accept(EFItems.TITANIUM_INGOT.get());
            output.accept(EFItems.TITANIUM_PLATE.get());
            // Wires
            output.accept(EFItems.COPPER_WIRE.get());
            output.accept(EFItems.SILVER_WIRE.get());
            output.accept(EFItems.TITANIUM_WIRE.get());
            // Items Erinium
            output.accept(EFItems.ERINIUM_INGOT.get());
            // Armure Erinium
            output.accept(EFItems.ERINIUM_HELMET.get());
            output.accept(EFItems.ERINIUM_CHESTPLATE.get());
            output.accept(EFItems.ERINIUM_LEGGINGS.get());
            output.accept(EFItems.ERINIUM_BOOTS.get());
            // Outils Erinium
            output.accept(EFItems.ERINIUM_SWORD.get());
            output.accept(EFItems.ERINIUM_PICKAXE.get());
            output.accept(EFItems.ERINIUM_AXE.get());
            output.accept(EFItems.ERINIUM_SHOVEL.get());
            output.accept(EFItems.ERINIUM_HOE.get());
            // Death Scythe
            output.accept(EFItems.DEATH_SCYTHE.get());
        })
        .build());
}
