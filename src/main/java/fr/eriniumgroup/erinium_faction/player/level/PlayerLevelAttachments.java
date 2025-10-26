package fr.eriniumgroup.erinium_faction.player.level;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import java.util.function.Supplier;
import fr.eriniumgroup.erinium_faction.EriniumFaction;

/**
 * Enregistrement des attachments pour le système de niveau des joueurs
 */
public class PlayerLevelAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
        DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, EriniumFaction.MODID);

    public static final Supplier<AttachmentType<PlayerLevelData>> PLAYER_LEVEL_DATA = ATTACHMENTS.register(
        "player_level_data",
        () -> AttachmentType.serializable(PlayerLevelData::new).build()
    );
}

