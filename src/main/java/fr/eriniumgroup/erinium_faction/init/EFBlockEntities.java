package fr.eriniumgroup.erinium_faction.init;

import fr.eriniumgroup.erinium_faction.common.block.entity.*;
import fr.eriniumgroup.erinium_faction.common.block.entity.EriniumChestBlockEntity;
import fr.eriniumgroup.erinium_faction.core.EFC;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class EFBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> REGISTER = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, EFC.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TitaniumCompressorBlockEntity>> TITANIUM_COMPRESSOR = REGISTER.register(
        "titanium_compressor",
        () -> BlockEntityType.Builder.of(TitaniumCompressorBlockEntity::new, EFBlocks.TITANIUM_COMPRESSOR.get()).build(null)
    );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TitaniumBatteryTier1BlockEntity>> TITANIUM_BATTERY_TIER1 = REGISTER.register(
        "titanium_battery_tier1",
        () -> BlockEntityType.Builder.of(TitaniumBatteryTier1BlockEntity::new, EFBlocks.TITANIUM_BATTERY_TIER1.get()).build(null)
    };
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EriniumChestBlockEntity>> ERINIUM_CHEST = REGISTER.register(
            "erinium_chest",
            () -> BlockEntityType.Builder.of(EriniumChestBlockEntity::new, EFBlocks.ERINIUM_CHEST.get()).build(null)
    );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TitaniumCreativeBatteryBlockEntity>> TITANIUM_CREATIVE_BATTERY = REGISTER.register(
            "titanium_creative_battery",
            () -> BlockEntityType.Builder.of(TitaniumCreativeBatteryBlockEntity::new, EFBlocks.TITANIUM_CREATIVE_BATTERY.get()).build(null)
    );

    private EFBlockEntities() {
    }

}
