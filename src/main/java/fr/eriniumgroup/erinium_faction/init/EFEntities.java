package fr.eriniumgroup.erinium_faction.init;

import fr.eriniumgroup.erinium_faction.EriniumFaction;
import fr.eriniumgroup.erinium_faction.common.entity.RocketEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Enregistrement des entités customisées
 */
public class EFEntities {
    public static final DeferredRegister<EntityType<?>> REGISTER = DeferredRegister.create(Registries.ENTITY_TYPE, EriniumFaction.MODID);

    /**
     * Entité Rocket - Fusée montable SpaceX-style
     */
    public static final DeferredHolder<EntityType<?>, EntityType<RocketEntity>> ROCKET = REGISTER.register("rocket",
        () -> EntityType.Builder.<RocketEntity>of(RocketEntity::new, MobCategory.MISC)
            .sized(2.0F, 6.0F) // Largeur et hauteur de la hitbox
            .clientTrackingRange(10) // Distance de tracking
            .updateInterval(3) // Intervalle de mise à jour réseau
            .build("rocket")
    );
}
