package fr.eriniumgroup.erinium_faction.common.network.packets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import fr.eriniumgroup.erinium_faction.EriniumFaction;
import fr.eriniumgroup.erinium_faction.jobs.JobType;
import fr.eriniumgroup.erinium_faction.jobs.config.JobConfig;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

/**
 * Paquet pour synchroniser les configurations de métiers du serveur vers le client
 */
public record SyncJobsConfigPacket(Map<JobType, JobConfig> configs) implements CustomPacketPayload {

    private static final Gson GSON = new GsonBuilder().create();

    public static final Type<SyncJobsConfigPacket> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(EriniumFaction.MODID, "sync_jobs_config")
    );

    public static final StreamCodec<ByteBuf, SyncJobsConfigPacket> STREAM_CODEC = new StreamCodec<>() {
        @Nonnull
        @Override
        public SyncJobsConfigPacket decode(@Nonnull ByteBuf buffer) {
            String json = ByteBufCodecs.STRING_UTF8.decode(buffer);
            Map<JobType, JobConfig> configs = new HashMap<>();

            // Désérialiser le JSON avec TypeToken pour avoir les bons types
            try {
                java.lang.reflect.Type gsonType = new TypeToken<Map<String, JobConfig>>(){}.getType();
                Map<String, JobConfig> rawConfigs = GSON.fromJson(json, gsonType);

                for (Map.Entry<String, JobConfig> entry : rawConfigs.entrySet()) {
                    JobType jobType = JobType.valueOf(entry.getKey());
                    configs.put(jobType, entry.getValue());
                }
            } catch (Exception e) {
                System.err.println("[ERROR] Failed to deserialize job configs: " + e.getMessage());
                e.printStackTrace();
            }

            return new SyncJobsConfigPacket(configs);
        }

        @Override
        public void encode(@Nonnull ByteBuf buffer, @Nonnull SyncJobsConfigPacket packet) {
            // Sérialiser les configs en JSON
            Map<String, JobConfig> rawConfigs = new HashMap<>();
            for (Map.Entry<JobType, JobConfig> entry : packet.configs.entrySet()) {
                rawConfigs.put(entry.getKey().name(), entry.getValue());
            }

            String json = GSON.toJson(rawConfigs);
            ByteBufCodecs.STRING_UTF8.encode(buffer, json);
        }
    };

    @Nonnull
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
