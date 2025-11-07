package fr.eriniumgroup.erinium_faction.init;

import fr.eriniumgroup.erinium_faction.core.EFC;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import fr.eriniumgroup.erinium_faction.init.EFBlockEntities;

/**
 * Onglet créatif personnalisé pour Erinium Faction
 */
public class EFCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> REGISTER = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, EFC.MODID);

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
            // Machines
            output.accept(EFBlocks.TITANIUM_COMPRESSOR.get());
            // Batteries
            // Creative battery: remplie par défaut
            output.accept(EFBlocks.TITANIUM_CREATIVE_BATTERY.get());
            // TitaniumBatteryTier1: vide
            output.accept(new ItemStack(EFBlocks.TITANIUM_BATTERY_TIER1.get()));
            // TitaniumBatteryTier1: pleine via BLOCK_ENTITY_DATA (CustomData)
            ItemStack fullBatt = new ItemStack(EFBlocks.TITANIUM_BATTERY_TIER1.get());
            var tag = new net.minecraft.nbt.CompoundTag();
            tag.putInt("energy", 100000);
            // Ajout de l'id de BlockEntity requis par le composant BLOCK_ENTITY_DATA
            tag.putString("id", EFBlockEntities.TITANIUM_BATTERY_TIER1.getId().toString());
            fullBatt.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(tag));
            output.accept(fullBatt);
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
        })
        .build());
}
