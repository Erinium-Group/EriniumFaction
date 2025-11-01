package fr.eriniumgroup.erinium_faction.features.topluck;

import fr.eriniumgroup.erinium_faction.EriniumFaction;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

/**
 * Enregistrement des attachments pour TopLuck (par joueur)
 */
public class TopLuckAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, EriniumFaction.MODID);

    public static final Supplier<AttachmentType<PlayerTopLuckData>> PLAYER_TOPLUCK = ATTACHMENTS.register(
            "player_topluck",
            () -> AttachmentType.serializable(PlayerTopLuckData::new).build()
    );
}

