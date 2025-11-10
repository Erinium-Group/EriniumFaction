package fr.eriniumgroup.erinium_faction.client.renderer.nameplate;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.eriniumgroup.erinium_faction.client.data.PlayerFactionCache;
import fr.eriniumgroup.erinium_faction.client.data.PlayerLevelCache;
import fr.eriniumgroup.erinium_faction.common.config.EFClientConfig;
import fr.eriniumgroup.erinium_faction.features.vanish.VanishClientData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.player.Player;
import org.joml.Matrix4f;
import com.mojang.math.Axis;

import java.util.ArrayList;
import java.util.List;

/**
 * Renderer de nameplate custom appelé depuis EntityRendererMixin
 *
 * AJOUTER UN NOUVEAU COMPOSANT:
 * 1. Créer une méthode renderMonComposant(poseStack, bufferSource, player, currentY, maxWidth, font, packedLight)
 * 2. L'ajouter dans setupComponents() avec sa hauteur et sa condition
 * 3. C'est tout!
 */
public class NameplateMixinRenderer {

    /**
     * Classe interne pour représenter un composant de la nameplate
     */
    private static class NameplateElement {
        String id;
        int height;
        boolean enabled;
        ElementRenderer renderer;

        NameplateElement(String id, int height, boolean enabled, ElementRenderer renderer) {
            this.id = id;
            this.height = height;
            this.enabled = enabled;
            this.renderer = renderer;
        }
    }

    @FunctionalInterface
    private interface ElementRenderer {
        void render(PoseStack poseStack, MultiBufferSource bufferSource, Player player, int y, int maxWidth, Font font, int light, boolean behindBlock);
    }

    /**
     * Point d'entrée principal - appelé par le mixin
     */
    public static boolean renderCustomNameplate(Player player, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        // Ne pas afficher la nameplate si le joueur est en vanish
        if (VanishClientData.isVanished(player.getUUID())) {
            return true; // Cancel le rendu vanilla aussi
        }

        Minecraft mc = Minecraft.getInstance();
        Player clientPlayer = mc.player;

        // Ne pas afficher notre propre nameplate
        if (player == clientPlayer) {
            return true;
        }

        // Cacher la nameplate si le joueur sneak
        if (player.isCrouching()) {
            return true; // Cancel vanilla aussi
        }

        // Cacher la nameplate si le joueur porte un BARRIER sur la tête
        net.minecraft.world.item.ItemStack helmet = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD);
        if (helmet.is(net.minecraft.world.item.Items.BARRIER)) {
            return true; // Cancel vanilla aussi
        }

        Font font = mc.font;
        int maxWidth = EFClientConfig.NAMEPLATE_MAX_WIDTH.get();

        // Détecter si le joueur est derrière un block (occlusion)
        boolean behindBlock = isPlayerBehindBlock(mc, player);

        // Configurer les éléments à afficher
        List<NameplateElement> elements = setupComponents(player, font, maxWidth, behindBlock);

        // Calculer hauteur totale
        int totalHeight = elements.stream()
            .filter(e -> e.enabled)
            .mapToInt(e -> e.height)
            .sum();

        if (totalHeight == 0) {
            return false; // Rien à afficher
        }

        poseStack.pushPose();

        // Translater au-dessus de la tête
        poseStack.translate(0.0, player.getBbHeight() + 0.5, 0.0);

        // Rotation billboarding : faire pivoter pour faire face à la caméra
        // Rotation Y (yaw) pour tourner horizontalement vers le joueur
        poseStack.mulPose(Axis.YP.rotationDegrees(-mc.getEntityRenderDispatcher().camera.getYRot()));
        // Rotation X (pitch) pour s'incliner selon l'angle de vue
        poseStack.mulPose(Axis.XP.rotationDegrees(mc.getEntityRenderDispatcher().camera.getXRot()));

        // Scale
        float scale = 0.025f;
        poseStack.scale(-scale, -scale, scale);

        int padding = 4;
        int startY = -totalHeight - padding;

        // Dessiner le fond EN PREMIER (le plus en arrière)
        poseStack.pushPose();
        poseStack.translate(0, 0, 0.1F); // Le plus loin
        int bgColor = EFClientConfig.NAMEPLATE_BACKGROUND_COLOR.get();
        int bgX1 = -maxWidth / 2 - padding;
        int bgX2 = maxWidth / 2 + padding;
        int bgY1 = startY;
        int bgY2 = startY + totalHeight + padding;
        drawBackground(poseStack, bufferSource, bgX1, bgY1, bgX2, bgY2, bgColor, packedLight, behindBlock);
        poseStack.popPose();

        // Rendre chaque élément PAR-DESSUS le fond
        poseStack.pushPose();
        poseStack.translate(0, 0, 0.05F); // Devant le fond mais derrière les barres
        int currentY = startY + padding;
        for (NameplateElement element : elements) {
            if (element.enabled) {
                element.renderer.render(poseStack, bufferSource, player, currentY, maxWidth, font, packedLight, behindBlock);
                currentY += element.height;
            }
        }
        poseStack.popPose();

        poseStack.popPose();
        return true; // Annuler vanilla
    }

    /**
     * CONFIGURATION DES COMPOSANTS - Modifier l'ordre ici pour changer l'ordre d'affichage
     */
    private static List<NameplateElement> setupComponents(Player player, Font font, int maxWidth, boolean behindBlock) {
        List<NameplateElement> elements = new ArrayList<>();

        String factionName = PlayerFactionCache.getFactionName(player.getUUID());
        int level = PlayerLevelCache.getLevel(player.getUUID());

        // ORDRE D'AFFICHAGE (de haut en bas) - Changer l'ordre des lignes ci-dessous pour réorganiser
        boolean showLevel = EFClientConfig.NAMEPLATE_SHOW_LEVEL.get() && level > 0 && !behindBlock;
        boolean showFaction = EFClientConfig.NAMEPLATE_SHOW_FACTION.get() && factionName != null && !factionName.isEmpty() && !behindBlock;

        elements.add(new NameplateElement("level", 7,
            showLevel,
            NameplateMixinRenderer::renderLevel));

        elements.add(new NameplateElement("faction", 8,
            showFaction,
            NameplateMixinRenderer::renderFaction));

        elements.add(new NameplateElement("name", font.lineHeight + 2,
            true, // Toujours afficher le nom
            NameplateMixinRenderer::renderName));

        elements.add(new NameplateElement("health", 10,
            !behindBlock, // Cacher la santé si le joueur est derrière un block
            NameplateMixinRenderer::renderHealthBar));

        return elements;
    }

    // ==================== COMPOSANTS DE RENDU ====================

    /**
     * COMPOSANT: Niveau du joueur (petit, en haut)
     */
    private static void renderLevel(PoseStack poseStack, MultiBufferSource bufferSource, Player player, int y, int maxWidth, Font font, int light, boolean behindBlock) {
        int level = PlayerLevelCache.getLevel(player.getUUID());
        poseStack.pushPose();
        poseStack.scale(0.5f, 0.5f, 0.5f);
        String levelText = "Niv. " + level;
        int levelColor = EFClientConfig.NAMEPLATE_LEVEL_COLOR.get();
        int levelWidth = font.width(levelText);
        font.drawInBatch(levelText, -levelWidth / 2.0f, y / 0.5f, levelColor, false,
            poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, light);
        poseStack.popPose();
    }

    /**
     * COMPOSANT: Nom de la faction (moyen)
     */
    private static void renderFaction(PoseStack poseStack, MultiBufferSource bufferSource, Player player, int y, int maxWidth, Font font, int light, boolean behindBlock) {
        String factionName = PlayerFactionCache.getFactionName(player.getUUID());
        poseStack.pushPose();
        poseStack.scale(0.7f, 0.7f, 0.7f);
        String truncated = font.plainSubstrByWidth(factionName, (int)(maxWidth / 0.7f));
        int factionColor = EFClientConfig.NAMEPLATE_FACTION_COLOR.get();
        int factionWidth = font.width(truncated);
        font.drawInBatch(truncated, -factionWidth / 2.0f, y / 0.7f, factionColor, false,
            poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, light);
        poseStack.popPose();
    }

    /**
     * COMPOSANT: Nom du joueur (normal)
     */
    private static void renderName(PoseStack poseStack, MultiBufferSource bufferSource, Player player, int y, int maxWidth, Font font, int light, boolean behindBlock) {
        String playerName = player.getName().getString();
        String truncated = font.plainSubstrByWidth(playerName, maxWidth);
        int nameColor = EFClientConfig.NAMEPLATE_NAME_COLOR.get();
        int nameWidth = font.width(truncated);
        // Si derrière un block, utiliser SEE_THROUGH, sinon NORMAL
        Font.DisplayMode mode = behindBlock ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL;
        font.drawInBatch(truncated, -nameWidth / 2.0f, y, nameColor, false,
            poseStack.last().pose(), bufferSource, mode, 0, light);
    }

    /**
     * COMPOSANT: Barre de vie avec texte
     */
    private static void renderHealthBar(PoseStack poseStack, MultiBufferSource bufferSource, Player player, int y, int maxWidth, Font font, int light, boolean behindBlock) {
        float health = player.getHealth();
        float maxHealth = player.getMaxHealth();
        float healthPercent = health / maxHealth;

        int barWidth = maxWidth - 4;
        int barX1 = -barWidth / 2;
        int barX2 = barWidth / 2;
        int barY1 = y;
        int barY2 = y + 8;

        // Couleur dynamique selon la santé
        int barColor = EFClientConfig.NAMEPLATE_HEALTH_BAR_COLOR.get();
        if (healthPercent <= 0.25f) {
            barColor = 0xFFFF0000; // Rouge
        } else if (healthPercent <= 0.5f) {
            barColor = 0xFFFFAA00; // Orange
        } else if (healthPercent <= 0.75f) {
            barColor = 0xFFFFFF00; // Jaune
        }

        // Les barres doivent être rendues avec leurs propres z-offsets
        // Fond de la barre (derrière la barre remplie)
        poseStack.pushPose();
        poseStack.translate(0, 0, -0.02F); // Derrière la barre remplie
        int bgBarColor = EFClientConfig.NAMEPLATE_HEALTH_BAR_BACKGROUND_COLOR.get();
        drawBackground(poseStack, bufferSource, barX1, barY1, barX2, barY2, bgBarColor, light, false);
        poseStack.popPose();

        // Barre remplie (devant le fond de barre)
        poseStack.pushPose();
        poseStack.translate(0, 0, -0.04F); // Encore plus devant
        int filledWidth = (int) (barWidth * healthPercent);
        if (filledWidth > 0) {
            drawBackground(poseStack, bufferSource, barX1, barY1, barX1 + filledWidth, barY2, barColor, light, false);
        }
        poseStack.popPose();

        // Texte de la barre (TOUT DEVANT)
        poseStack.pushPose();
        poseStack.translate(0, 0, -0.06F); // Le plus devant de tous
        poseStack.scale(0.6f, 0.6f, 0.6f);
        String healthText = String.format("%.1f / %.1f", health, maxHealth);
        int textColor = EFClientConfig.NAMEPLATE_HEALTH_TEXT_COLOR.get();
        int healthWidth = font.width(healthText);
        int textY = (int)((barY1 + 1) / 0.6f);
        font.drawInBatch(healthText, -healthWidth / 2.0f, textY, textColor, false,
            poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, light);
        poseStack.popPose();
    }

    // ==================== UTILITAIRES ====================

    /**
     * Vérifie si le joueur est derrière un block (occlusion)
     */
    private static boolean isPlayerBehindBlock(Minecraft mc, Player player) {
        if (mc.player == null || mc.level == null) {
            return false;
        }

        // Raycasting depuis la caméra vers le joueur
        net.minecraft.world.phys.Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();
        net.minecraft.world.phys.Vec3 playerPos = player.getEyePosition(mc.getTimer().getGameTimeDeltaPartialTick(false));

        // Clip pour voir s'il y a un block entre nous et le joueur
        net.minecraft.world.level.ClipContext context = new net.minecraft.world.level.ClipContext(
            cameraPos,
            playerPos,
            net.minecraft.world.level.ClipContext.Block.COLLIDER,
            net.minecraft.world.level.ClipContext.Fluid.NONE,
            mc.player
        );

        net.minecraft.world.phys.BlockHitResult result = mc.level.clip(context);

        // Si on hit un block avant d'atteindre le joueur, il est derrière
        return result.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK;
    }

    /**
     * Dessine un rectangle de fond
     */
    private static void drawBackground(PoseStack poseStack, MultiBufferSource bufferSource, float x1, float y1, float x2, float y2, int color, int light, boolean seeThrough) {
        Matrix4f matrix = poseStack.last().pose();
        // TOUJOURS utiliser gui() - ce RenderType n'a PAS de depth test
        // Il sera TOUJOURS rendu par-dessus tout (bannières, blocks, etc)
        // C'est ce qui garantit que la nameplate est toujours visible
        RenderType renderType = RenderType.gui();
        VertexConsumer consumer = bufferSource.getBuffer(renderType);

        int a = (color >> 24) & 0xFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        // Quad (4 vertices) - Z à 0 pour éviter problèmes de clipping
        consumer.addVertex(matrix, x1, y1, 0).setColor(r, g, b, a).setUv(0, 0).setLight(light);
        consumer.addVertex(matrix, x1, y2, 0).setColor(r, g, b, a).setUv(0, 1).setLight(light);
        consumer.addVertex(matrix, x2, y2, 0).setColor(r, g, b, a).setUv(1, 1).setLight(light);
        consumer.addVertex(matrix, x2, y1, 0).setColor(r, g, b, a).setUv(1, 0).setLight(light);
    }
}
