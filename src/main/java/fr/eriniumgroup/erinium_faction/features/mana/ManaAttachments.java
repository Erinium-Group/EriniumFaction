package fr.eriniumgroup.erinium_faction.features.mana;

import fr.eriniumgroup.erinium_faction.EriniumFaction;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ManaAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, EriniumFaction.MODID);
    public static final Supplier<AttachmentType<PlayerManaData>> PLAYER_MANA = ATTACHMENTS.register("player_mana", () -> AttachmentType.serializable(PlayerManaData::new).build());
}

