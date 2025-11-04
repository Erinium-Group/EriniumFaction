package fr.eriniumgroup.erinium_faction.client.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import fr.eriniumgroup.erinium_faction.client.waypoint.Waypoint;
import fr.eriniumgroup.erinium_faction.client.waypoint.WaypointManager;
import fr.eriniumgroup.erinium_faction.common.config.EFClientConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Renderer pour afficher les waypoints dans le monde 3D
 * Style JourneyMap - affiche les waypoints quand on regarde dans leur direction
 */
public class WaypointOverlayRenderer {

    /**
     * Render les waypoints dans le monde 3D
     */
    public static void render(GuiGraphics g, int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        WaypointManager waypointManager = fr.eriniumgroup.erinium_faction.client.EFClient.getWaypointManager();
        if (waypointManager == null) return;

        String dimension = player.level().dimension().location().toString();
        List<Waypoint> waypoints = waypointManager.getVisibleWaypointsForDimension(dimension);
        if (waypoints.isEmpty()) return;

        // Obtenir la distance max depuis la config
        int maxDistance = EFClientConfig.WAYPOINT_MAX_OVERLAY_DISTANCE.get();

        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.getPosition();

        PoseStack poseStack = g.pose();

        RenderSystem.enableBlend();

        for (Waypoint wp : waypoints) {
            // Position du waypoint
            double wpX = wp.getX() + 0.5;
            double wpY = wp.getY();
            double wpZ = wp.getZ() + 0.5;

            // Calculer la distance
            double dx = wpX - cameraPos.x;
            double dy = wpY - cameraPos.y;
            double dz = wpZ - cameraPos.z;
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

            // Skip si trop loin
            if (distance > maxDistance) continue;

            // Calculer la position à l'écran
            Vec3 waypointPos = new Vec3(wpX, wpY, wpZ);
            Vec3 toWaypoint = waypointPos.subtract(cameraPos).normalize();

            // Vecteur de vue de la caméra
            org.joml.Vector3f lookVec = camera.getLookVector();
            Vec3 viewVector = new Vec3(lookVec.x(), lookVec.y(), lookVec.z());

            // Calculer l'angle entre la vue et le waypoint
            double dotProduct = viewVector.dot(toWaypoint);

            // Seulement afficher si dans le champ de vision (environ 90 degrés devant)
            if (dotProduct < 0.3) continue;

            // Projeter sur l'écran
            Vec3 projected = projectToScreen(waypointPos, cameraPos, camera, screenWidth, screenHeight);

            if (projected == null) continue;

            int screenX = (int) projected.x;
            int screenY = (int) projected.y;

            // Vérifier si dans l'écran
            if (screenX < 0 || screenX >= screenWidth || screenY < 0 || screenY >= screenHeight) continue;

            // Dessiner le waypoint
            poseStack.pushPose();

            // Point de couleur
            int dotSize = 6;
            g.fill(screenX - dotSize / 2, screenY - dotSize / 2, screenX + dotSize / 2, screenY + dotSize / 2, wp.getColorARGB());

            // Bordure noire
            g.fill(screenX - dotSize / 2 - 1, screenY - dotSize / 2 - 1, screenX + dotSize / 2 + 1, screenY - dotSize / 2, 0xFF000000);
            g.fill(screenX - dotSize / 2 - 1, screenY + dotSize / 2, screenX + dotSize / 2 + 1, screenY + dotSize / 2 + 1, 0xFF000000);
            g.fill(screenX - dotSize / 2 - 1, screenY - dotSize / 2, screenX - dotSize / 2, screenY + dotSize / 2, 0xFF000000);
            g.fill(screenX + dotSize / 2, screenY - dotSize / 2, screenX + dotSize / 2 + 1, screenY + dotSize / 2, 0xFF000000);

            // Nom et distance
            String text = String.format("%s (%.0fm)", wp.getName(), distance);
            int textWidth = mc.font.width(text);

            // Fond semi-transparent pour le texte
            g.fill(screenX - textWidth / 2 - 2, screenY - dotSize / 2 - 12, screenX + textWidth / 2 + 2, screenY - dotSize / 2 - 2, 0xAA000000);

            // Texte centré au-dessus du point
            g.drawCenteredString(mc.font, text, screenX, screenY - dotSize / 2 - 10, wp.getColorARGB());

            poseStack.popPose();
        }

        RenderSystem.disableBlend();
    }

    /**
     * Projeter une position 3D sur l'écran 2D
     */
    private static Vec3 projectToScreen(Vec3 worldPos, Vec3 cameraPos, Camera camera, int screenWidth, int screenHeight) {
        // Calculer la position relative à la caméra
        Vec3 relative = worldPos.subtract(cameraPos);

        // Obtenir les vecteurs de direction de la caméra
        org.joml.Vector3f forwardVec = camera.getLookVector();
        Vec3 forward = new Vec3(forwardVec.x(), forwardVec.y(), forwardVec.z());

        org.joml.Vector3f upVec = camera.getUpVector();
        Vec3 up = new Vec3(upVec.x(), upVec.y(), upVec.z());

        Vec3 right = forward.cross(up);

        // Projeter sur le plan de la caméra
        double forwardDist = relative.dot(forward);

        // Si derrière la caméra, ne pas afficher
        if (forwardDist <= 0) return null;

        double rightDist = relative.dot(right);
        double upDist = relative.dot(up);

        // Calculer les coordonnées d'écran avec FOV
        double fov = mc.options.fov().get();
        double scale = screenHeight / (2.0 * Math.tan(Math.toRadians(fov / 2.0)));

        double screenX = screenWidth / 2.0 + (rightDist / forwardDist) * scale;
        double screenY = screenHeight / 2.0 - (upDist / forwardDist) * scale;

        return new Vec3(screenX, screenY, 0);
    }

    private static final Minecraft mc = Minecraft.getInstance();
}
