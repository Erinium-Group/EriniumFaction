package fr.eriniumgroup.erinium_faction.common.network.packets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import fr.eriniumgroup.erinium_faction.common.blockentity.RocketMakerBlockEntity;
import fr.eriniumgroup.erinium_faction.core.EFC;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Packet pour exporter une recette du Rocket Maker vers un fichier JSON
 */
public record ExportRecipePacket(BlockPos pos) implements CustomPacketPayload {
    public static final Type<ExportRecipePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(EFC.MOD_ID, "export_recipe"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ExportRecipePacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            ExportRecipePacket::pos,
            ExportRecipePacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleData(ExportRecipePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            BlockEntity be = player.level().getBlockEntity(packet.pos);
            if (!(be instanceof RocketMakerBlockEntity rocketMaker)) return;

            // Generate JSON from current inventory
            JsonObject recipeJson = new JsonObject();
            recipeJson.addProperty("type", "erinium_faction:rocket_making");

            JsonObject pattern = new JsonObject();
            boolean hasItems = false;

            // Scan all 63 craft slots
            for (int i = 0; i < 63; i++) {
                ItemStack stack = rocketMaker.getInventory().getStackInSlot(i);
                if (!stack.isEmpty()) {
                    hasItems = true;
                    JsonObject slotData = new JsonObject();

                    // Get item registry name
                    ResourceLocation itemId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
                    slotData.addProperty("item", itemId.toString());

                    pattern.add(String.valueOf(i), slotData);
                }
            }

            if (!hasItems) {
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§c[RocketMaker] No items in craft grid!"));
                return;
            }

            recipeJson.add("pattern", pattern);

            // Output: air (you'll change manually later)
            JsonObject result = new JsonObject();
            result.addProperty("id", "minecraft:air");
            result.addProperty("count", 1);
            recipeJson.add("result", result);

            // Save to file
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String fileName = "rocket_making_exported_" + timestamp + ".json";

            // Save in run/recipes/ folder
            File outputDir = new File("recipes");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }

            File outputFile = new File(outputDir, fileName);

            try (FileWriter writer = new FileWriter(outputFile)) {
                gson.toJson(recipeJson, writer);
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§a[RocketMaker] Recipe exported to: §e" + outputFile.getAbsolutePath()
                ));
                EFC.log.info("Recipe exported to: " + outputFile.getAbsolutePath());
            } catch (IOException e) {
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§c[RocketMaker] Failed to export recipe: " + e.getMessage()
                ));
                EFC.log.error("Failed to export recipe", e);
            }
        });
    }
}
