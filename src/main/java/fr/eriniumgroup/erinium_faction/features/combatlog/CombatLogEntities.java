package fr.eriniumgroup.erinium_faction.features.combatlog;

import fr.eriniumgroup.erinium_faction.core.EFC;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Enregistrement des entités du système de combat logging
 */
public class CombatLogEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, EFC.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<CombatLogNPC>> COMBAT_LOG_NPC =
            ENTITIES.register("combat_log_npc", () -> EntityType.Builder.<CombatLogNPC>of(
                    (entityType, level) -> new CombatLogNPC(entityType, level),
                    MobCategory.MISC)
                    .sized(0.6F, 1.8F) // Taille d'un joueur
                    .clientTrackingRange(10)
                    .updateInterval(3)
                    .build("combat_log_npc"));

    /**
     * Enregistre les attributs des entités
     */
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(COMBAT_LOG_NPC.get(), CombatLogNPC.createAttributes().build());
    }
}
