package fr.eriniumgroup.erinium_faction.client.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import fr.eriniumgroup.erinium_faction.features.minimap.MinimapConfig;
import fr.eriniumgroup.erinium_faction.features.minimap.Waypoint;
import fr.eriniumgroup.erinium_faction.features.minimap.WaypointManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;

/**
 * Overlay pour afficher les waypoints dans le monde (comme JourneyMap)
 */
@EventBusSubscriber(modid = "erinium_faction", value = Dist.CLIENT)
public class WaypointWorldOverlay {

    private static final int MIN_LABEL_DISTANCE = 10; // Distance min pour afficher le label

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiEvent.Post event) {
        render(event.getGuiGraphics());
    }

    private static void render(GuiGraphics graphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.options.hideGui) {
            return;
        }

        LocalPlayer player = mc.player;
        Font font = mc.font;

        // Get all waypoints for current dimension
        List<Waypoint> waypoints = WaypointManager.getInstance().getWaypointsForDimension(player.level().dimension());

        for (Waypoint waypoint : waypoints) {
            if (!waypoint.isEnabled()) {
                continue;
            }

            BlockPos wpPos = waypoint.getPosition();
            Vec3 playerPos = player.getEyePosition(1.0f);

            // Calculate distance
            double dx = wpPos.getX() - playerPos.x;
            double dy = wpPos.getY() - playerPos.y;
            double dz = wpPos.getZ() - playerPos.z;
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

            // Skip if too far (use config value)
            if (distance > MinimapConfig.waypointOverlayDistance) {
                continue;
            }

            // Project world position to screen
            Vec3 wpVec = new Vec3(wpPos.getX() + 0.5, wpPos.getY(), wpPos.getZ() + 0.5);
            ScreenPos screenPos = worldToScreen(wpVec, graphics.guiWidth(), graphics.guiHeight());

            if (screenPos == null) {
                continue; // Behind camera
            }

            // Render waypoint marker and label
            renderWaypointOnScreen(graphics, font, waypoint, screenPos, (int) distance);
        }
    }

    private static void renderWaypointOnScreen(GuiGraphics graphics, Font font, Waypoint waypoint, ScreenPos pos, int distance) {
        int x = pos.x();
        int y = pos.y();

        // Get waypoint color
        int color = waypoint.getEffectiveColor();

        // Render marker (small circle or icon)
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Draw a small filled circle as marker
        fillCircle(graphics, x, y, 4, color);

        // Render label with name and distance
        if (distance > MIN_LABEL_DISTANCE) {
            String label = waypoint.getName() + " (" + distance + "m)";
            int labelWidth = font.width(label);

            // Background for text
            graphics.fill(x - labelWidth / 2 - 2, y - 18, x + labelWidth / 2 + 2, y - 6, 0xCC000000);

            // Text - use waypoint color
            graphics.drawString(font, label, x - labelWidth / 2, y - 16, color, true);
        }

        RenderSystem.disableBlend();
    }

    private static void fillCircle(GuiGraphics graphics, int centerX, int centerY, int radius, int color) {
        Matrix4f matrix = graphics.pose().last().pose();
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);

        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        float a = ((color >> 24) & 0xFF) / 255.0f;

        // Center point
        buffer.addVertex(matrix, centerX, centerY, 0).setColor(r, g, b, a);

        // Circle points
        int segments = 16;
        for (int i = 0; i <= segments; i++) {
            double angle = 2.0 * Math.PI * i / segments;
            float x = (float) (centerX + radius * Math.cos(angle));
            float y = (float) (centerY + radius * Math.sin(angle));
            buffer.addVertex(matrix, x, y, 0).setColor(r, g, b, a);
        }

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    /**
     * Convert world coordinates to screen coordinates
     * Returns null if behind camera
     */
    private static ScreenPos worldToScreen(Vec3 worldPos, int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gameRenderer == null || mc.player == null) {
            return null;
        }

        // Get camera position
        Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();
        Vec3 relative = worldPos.subtract(cameraPos);

        // Get view vectors (as Vector3f from camera)
        Vector3f lookVec = mc.gameRenderer.getMainCamera().getLookVector();
        Vector3f upVec = mc.gameRenderer.getMainCamera().getUpVector();
        Vector3f leftVec = mc.gameRenderer.getMainCamera().getLeftVector();

        // Convert to Vec3 for dot product
        Vec3 look = new Vec3(lookVec.x(), lookVec.y(), lookVec.z());
        Vec3 up = new Vec3(upVec.x(), upVec.y(), upVec.z());
        Vec3 right = new Vec3(-leftVec.x(), -leftVec.y(), -leftVec.z());

        // Project onto camera plane
        double dotForward = relative.dot(look);

        // If behind camera, don't render
        if (dotForward <= 0) {
            return null;
        }

        double dotRight = relative.dot(right);
        double dotUp = relative.dot(up);

        // Get FOV
        double fov = mc.options.fov().get();
        double fovRad = Math.toRadians(fov);
        float aspectRatio = (float) screenWidth / screenHeight;

        // Calculate screen position
        double screenX = (dotRight / dotForward) / Math.tan(fovRad / 2.0) / aspectRatio;
        double screenY = (dotUp / dotForward) / Math.tan(fovRad / 2.0);

        // Convert to pixel coordinates
        int pixelX = (int) (screenWidth / 2.0 + screenX * screenWidth / 2.0);
        int pixelY = (int) (screenHeight / 2.0 - screenY * screenHeight / 2.0);

        // Clamp to screen bounds (with margin)
        int margin = 20;
        pixelX = Math.max(margin, Math.min(screenWidth - margin, pixelX));
        pixelY = Math.max(margin, Math.min(screenHeight - margin, pixelY));

        return new ScreenPos(pixelX, pixelY);
    }

    private record ScreenPos(int x, int y) {}
}
