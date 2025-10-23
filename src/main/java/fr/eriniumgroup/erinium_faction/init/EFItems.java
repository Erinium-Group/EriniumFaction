package fr.eriniumgroup.erinium_faction.init;

import fr.eriniumgroup.erinium_faction.core.EFC;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;

public class EFItems {
    public static final DeferredRegister<Item> REGISTER = DeferredRegister.create(Registries.ITEM, EFC.MOD_ID);

    private EFItems() {
    }
}
