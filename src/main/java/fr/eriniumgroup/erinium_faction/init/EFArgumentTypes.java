package fr.eriniumgroup.erinium_faction.init;

import fr.eriniumgroup.erinium_faction.commands.arguments.FactionArgumentType;
import fr.eriniumgroup.erinium_faction.core.EFC;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registration of custom Brigadier/Minecraft command argument types.
 * Required so the server can serialize the command tree to clients.
 */
public final class EFArgumentTypes {
    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> REGISTER = DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, EFC.MODID);

    public static final DeferredHolder<ArgumentTypeInfo<?, ?>, ArgumentTypeInfo<FactionArgumentType, ?>> FACTION =
            REGISTER.register("faction", () -> SingletonArgumentInfo.contextFree(FactionArgumentType::faction));

    private EFArgumentTypes() {}
}

