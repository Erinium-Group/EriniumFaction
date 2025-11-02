package fr.eriniumgroup.erinium_faction.features.jobs.data;

import fr.eriniumgroup.erinium_faction.EriniumFaction;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

/**
 * Enregistrement des attachments pour le système de métiers des joueurs
 */
public class JobsDataAttachment {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
        DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, EriniumFaction.MODID);

    public static final Supplier<AttachmentType<JobsData>> JOBS_DATA = ATTACHMENTS.register(
        "jobs_data",
        () -> AttachmentType.<JobsData>builder(JobsData::new)
            .serialize(new JobsData.JobsDataCodec())
            .build()
    );
}
