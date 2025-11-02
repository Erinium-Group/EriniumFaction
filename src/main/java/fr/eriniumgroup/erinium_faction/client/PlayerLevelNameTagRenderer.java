package fr.eriniumgroup.erinium_faction.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.eriniumgroup.erinium_faction.core.EFC;
import fr.eriniumgroup.erinium_faction.features.level.PlayerLevelAttachments;
import fr.eriniumgroup.erinium_faction.features.level.PlayerLevelData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import net.neoforged.neoforge.common.util.TriState;
import org.joml.Matrix4f;

/**
 * Affiche le niveau du joueur au-dessus de sa tête avec un cadre stylisé
 */
@EventBusSubscriber(modid = EFC.MODID, value = Dist.CLIENT)
public class PlayerLevelNameTagRenderer {

    @SubscribeEvent
    public static void onRenderNameTag(RenderNameTagEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        Minecraft mc = Minecraft.getInstance();
        boolean isLocalPlayer = player == mc.player;

        // Si c'est le joueur local, cacher le pseudo par défaut mais afficher le niveau
        if (isLocalPlayer) {
            event.setCanRender(TriState.FALSE); // Cache le pseudo par défaut
        }

        PlayerLevelData data = player.getData(PlayerLevelAttachments.PLAYER_LEVEL_DATA);
        if (data == null) return;

        int level = data.getLevel();
        if (level <= 0) return;

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource buffer = event.getMultiBufferSource();
        Font font = mc.font;

        poseStack.pushPose();

        // Position : si c'est le joueur local, centrer le niveau, sinon au-dessus du nom
        double yOffset = isLocalPlayer ? 0.0 : 0.35;
        poseStack.translate(0.0, yOffset, 0.0);

        // Faire face à la caméra
        poseStack.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());

        // Scale pour la taille
        float scale = 0.025f;
        poseStack.scale(-scale, -scale, scale);

        // Créer le texte du niveau avec style selon le niveau
        Component levelText = getLevelComponent(level);

        // Calculer la largeur du texte
        int textWidth = font.width(levelText);
        float x = -textWidth / 2.0f;

        // Dessiner le cadre stylisé
        drawStyledFrame(poseStack, buffer, level, textWidth);

        // Dessiner le texte du niveau
        Matrix4f matrix = poseStack.last().pose();
        font.drawInBatch(levelText, x, 0, 0xFFFFFFFF, false, matrix, buffer,
            Font.DisplayMode.NORMAL, 0, 15728880);

        poseStack.popPose();
    }

    private static Component getLevelComponent(int level) {
        String prefix = getLevelPrefix(level);
        int color = getLevelColor(level);

        return Component.literal(prefix + " Niv. " + level)
            .withStyle(style -> style.withColor(color).withBold(level >= 50));
    }

    private static String getLevelPrefix(int level) {
        if (level >= 100) return "⚜";
        if (level >= 75) return "✦";
        if (level >= 50) return "✧";
        if (level >= 25) return "★";
        return "●";
    }

    private static int getLevelColor(int level) {
        if (level >= 100) return 0xFFD700;
        if (level >= 75) return 0xFF00FF;
        if (level >= 50) return 0x00FFFF;
        if (level >= 25) return 0x55FF55;
        return 0xFFFFFF;
    }

    private static void drawStyledFrame(PoseStack poseStack, MultiBufferSource buffer, int level, int textWidth) {
        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer consumer = buffer.getBuffer(RenderType.gui());

        float padding = 4;
        float x1 = -textWidth / 2.0f - padding;
        float x2 = textWidth / 2.0f + padding;
        float y1 = -2;
        float y2 = 10;

        int bgColor = getBackgroundColor(level);

        if (level >= 100) {
            float glowPadding = 1.5f;
            int glowColor = 0x60FFD700;
            fillRect(consumer, matrix, x1 - glowPadding, y1 - glowPadding,
                    x2 + glowPadding, y2 + glowPadding, glowColor);
        } else if (level >= 75) {
            float glowPadding = 1.0f;
            int glowColor = 0x40FF00FF;
            fillRect(consumer, matrix, x1 - glowPadding, y1 - glowPadding,
                    x2 + glowPadding, y2 + glowPadding, glowColor);
        }

        fillRect(consumer, matrix, x1, y1, x2, y2, bgColor);

        int frameColor = getFrameColor(level);
        float borderWidth = level >= 50 ? 1.0f : 0.75f;

        fillRect(consumer, matrix, x1, y1, x2, y1 + borderWidth, frameColor);
        fillRect(consumer, matrix, x1, y2 - borderWidth, x2, y2, frameColor);
        fillRect(consumer, matrix, x1, y1, x1 + borderWidth, y2, frameColor);
        fillRect(consumer, matrix, x2 - borderWidth, y1, x2, y2, frameColor);

        if (level >= 75) {
            float cornerSize = 2.5f;
            int cornerColor = getCornerColor(level);

            fillRect(consumer, matrix, x1 - 0.5f, y1 - 0.5f, x1 + cornerSize, y1 + cornerSize, cornerColor);
            fillRect(consumer, matrix, x2 - cornerSize, y1 - 0.5f, x2 + 0.5f, y1 + cornerSize, cornerColor);
            fillRect(consumer, matrix, x1 - 0.5f, y2 - cornerSize, x1 + cornerSize, y2 + 0.5f, cornerColor);
            fillRect(consumer, matrix, x2 - cornerSize, y2 - cornerSize, x2 + 0.5f, y2 + 0.5f, cornerColor);
        }
    }

    private static int getFrameColor(int level) {
        if (level >= 100) return 0xFFFFD700;
        if (level >= 75) return 0xFFFF00FF;
        if (level >= 50) return 0xFF00FFFF;
        if (level >= 25) return 0xFF55FF55;
        return 0xFFAAAAAA;
    }

    private static int getBackgroundColor(int level) {
        if (level >= 100) return 0xE0000000;
        if (level >= 75) return 0xD0000000;
        if (level >= 50) return 0xC0000000;
        if (level >= 25) return 0xB0000000;
        return 0xA0000000;
    }

    private static int getCornerColor(int level) {
        if (level >= 100) return 0xFFFFFF00;
        if (level >= 75) return 0xFFFF66FF;
        return 0xFF66FFFF;
    }

    private static void fillRect(VertexConsumer consumer, Matrix4f matrix, float x1, float y1, float x2, float y2, int color) {
        int a = (color >> 24) & 0xFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        consumer.addVertex(matrix, x1, y1, 0).setColor(r, g, b, a);
        consumer.addVertex(matrix, x1, y2, 0).setColor(r, g, b, a);
        consumer.addVertex(matrix, x2, y2, 0).setColor(r, g, b, a);
        consumer.addVertex(matrix, x2, y1, 0).setColor(r, g, b, a);
    }
}