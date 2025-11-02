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
    public static final DeferredRegister<CreativeModeTab> REGISTER = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, EFC.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ERINIUM_FACTION_TAB = REGISTER.register("erinium_faction_tab", () -> CreativeModeTab.builder()
        .title(Component.translatable("itemGroup.erinium_faction"))
        .icon(() -> new ItemStack(EFItems.STATS_RESET_TOKEN.get()))
        .displayItems((parameters, output) -> {
            // Ajouter tous les items du mod ici
            output.accept(EFItems.STATS_RESET_TOKEN.get());
            // Blocs
            output.accept(EFBlocks.SILVER_ORE.get());
            output.accept(EFBlocks.DEEPSLATE_SILVER_ORE.get());
            output.accept(EFBlocks.TITANIUM_ORE.get());
            output.accept(EFBlocks.DEEPSLATE_TITANIUM_ORE.get());
            // Items silver
            output.accept(EFItems.RAW_SILVER.get());
            output.accept(EFItems.SILVER_INGOT.get());
            output.accept(EFItems.RAW_TITANIUM.get());
            output.accept(EFItems.TITANIUM_INGOT.get());
        })
        .build());
}
