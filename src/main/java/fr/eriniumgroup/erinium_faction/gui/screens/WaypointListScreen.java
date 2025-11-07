package fr.eriniumgroup.erinium_faction.gui.screens;

import fr.eriniumgroup.erinium_faction.client.waypoint.Waypoint;
import fr.eriniumgroup.erinium_faction.client.waypoint.WaypointManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.List;

/**
 * Écran de liste des waypoints avec boutons E/H/D pour chaque waypoint
 */
public class WaypointListScreen extends Screen {
    private final Screen parent;
    private final WaypointManager waypointManager;
    private List<Waypoint> waypoints;
    private int scrollOffset = 0;
    private static final int VISIBLE_ITEMS = 8;

    public WaypointListScreen(Screen parent, WaypointManager waypointManager) {
        super(Component.translatable("erinium_faction.waypoint.list"));
        this.parent = parent;
        this.waypointManager = waypointManager;
    }

    @Override
    protected void init() {
        super.init();
        refreshWaypoints();

        int centerX = this.width / 2;
        int startY = 60;

        this.addRenderableWidget(Button.builder(Component.translatable("erinium_faction.waypoint.button.create_new"), b -> {
            minecraft.setScreen(new WaypointEditScreen(this, waypointManager, null));
        }).bounds(centerX - 100, startY - 30, 120, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("erinium_faction.waypoint.button.back"), b -> {
            minecraft.setScreen(parent);
        }).bounds(centerX + 30, startY - 30, 70, 20).build());

        // Liste des waypoints (on les créera dans render)
    }

    private void refreshWaypoints() {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            String dimension = player.level().dimension().location().toString();
            waypoints = waypointManager.getWaypointsForDimension(dimension);
        } else {
            waypoints = waypointManager.getAllWaypoints();
        }
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
        // Ne rien render pour éviter le flou
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
        // Fond noir semi-transparent
        g.fill(0, 0, this.width, this.height, 0xCC000000);

        // Calculer la taille max et scale si nécessaire
        int maxWidth = 400;
        int maxHeight = 270;
        int itemHeight = 30;
        int startY = 60;

        int listWidth = Math.min(maxWidth, this.width - 40);
        int listHeight = Math.min(maxHeight - startY - 40, VISIBLE_ITEMS * itemHeight);

        // Scale si la fenêtre est trop petite
        float scale = 1.0f;
        int requiredWidth = listWidth + 40;
        int requiredHeight = listHeight + startY + 60;

        if (this.width < requiredWidth || this.height < requiredHeight) {
            float scaleX = (float) this.width / requiredWidth;
            float scaleY = (float) this.height / requiredHeight;
            scale = Math.min(scaleX, scaleY);

            g.pose().pushPose();
            g.pose().scale(scale, scale, scale);
            mouseX = (int) (mouseX / scale);
            mouseY = (int) (mouseY / scale);
        }

        super.render(g, mouseX, mouseY, partialTicks);

        // Titre
        g.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);

        int centerX = this.width / 2;

        String info = Component.translatable("erinium_faction.waypoint.count", waypointManager.getWaypointCount(), WaypointManager.getMaxWaypoints()).getString();
        g.drawString(this.font, info, centerX - listWidth / 2, startY - 15, 0xCCCCCC, false);

        // Cadre de liste
        g.fill(centerX - listWidth / 2 - 2, startY - 2, centerX + listWidth / 2 + 2, startY + listHeight + 2, 0xFF000000);
        g.fill(centerX - listWidth / 2, startY, centerX + listWidth / 2, startY + listHeight, 0xAA333333);

        // Afficher les waypoints
        int maxScroll = Math.max(0, waypoints.size() - VISIBLE_ITEMS);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));

        for (int i = 0; i < VISIBLE_ITEMS && (i + scrollOffset) < waypoints.size(); i++) {
            Waypoint wp = waypoints.get(i + scrollOffset);
            int y = startY + i * itemHeight;

            // Fond
            boolean hover = mouseX >= centerX - listWidth / 2 && mouseX <= centerX + listWidth / 2 &&
                           mouseY >= y && mouseY < y + itemHeight;
            if (hover) {
                g.fill(centerX - listWidth / 2, y, centerX + listWidth / 2, y + itemHeight, 0x55FFFFFF);
            }

            // Point de couleur
            int colorDotSize = 10;
            g.fill(centerX - listWidth / 2 + 5, y + 10, centerX - listWidth / 2 + 5 + colorDotSize, y + 10 + colorDotSize, wp.getColorARGB());

            // Nom et coordonnées
            String name = wp.getName();
            String coords = String.format("(%d, %d, %d)", wp.getX(), wp.getY(), wp.getZ());
            g.drawString(this.font, name, centerX - listWidth / 2 + 20, y + 5, wp.isVisible() ? 0xFFFFFF : 0x888888, false);
            g.drawString(this.font, coords, centerX - listWidth / 2 + 20, y + 16, 0xAAAAAA, false);

            // Boutons E, H, D
            int btnX = centerX + listWidth / 2 - 100;
            int btnY = y + 5;
            int btnSize = 20;

            if (renderButton(g, "E", btnX, btnY, btnSize, mouseX, mouseY)) {
                if (isMouseOverButton(mouseX, mouseY, btnX, btnY, btnSize)) {
                    g.renderTooltip(this.font, Component.translatable("erinium_faction.waypoint.button.edit"), mouseX, mouseY);
                }
            }

            btnX += btnSize + 5;
            String hideText = wp.isVisible() ? "H" : "S";
            if (renderButton(g, hideText, btnX, btnY, btnSize, mouseX, mouseY)) {
                if (isMouseOverButton(mouseX, mouseY, btnX, btnY, btnSize)) {
                    g.renderTooltip(this.font, Component.translatable(wp.isVisible() ? "erinium_faction.waypoint.button.hide" : "erinium_faction.waypoint.button.show"), mouseX, mouseY);
                }
            }

            btnX += btnSize + 5;
            if (renderButton(g, "D", btnX, btnY, btnSize, mouseX, mouseY)) {
                if (isMouseOverButton(mouseX, mouseY, btnX, btnY, btnSize)) {
                    g.renderTooltip(this.font, Component.translatable("erinium_faction.waypoint.button.delete"), mouseX, mouseY);
                }
            }
        }

        // Indicateur de scroll
        if (waypoints.size() > VISIBLE_ITEMS) {
            String scrollInfo = String.format("%d-%d / %d", scrollOffset + 1, Math.min(scrollOffset + VISIBLE_ITEMS, waypoints.size()), waypoints.size());
            g.drawCenteredString(this.font, scrollInfo, centerX, startY + listHeight + 5, 0xAAAAAA);
        }

        // Pop la pose si on a scale
        if (scale < 1.0f) {
            g.pose().popPose();
        }
    }

    private boolean renderButton(GuiGraphics g, String text, int x, int y, int size, int mouseX, int mouseY) {
        boolean hover = isMouseOverButton(mouseX, mouseY, x, y, size);
        int color = hover ? 0xFFFFFFFF : 0xFFAAAAAA;
        int bgColor = hover ? 0xFF555555 : 0xFF333333;

        g.fill(x, y, x + size, y + size, bgColor);
        g.fill(x, y, x + size, y + 1, color); // Top border
        g.fill(x, y, x + 1, y + size, color); // Left border
        g.fill(x + size - 1, y, x + size, y + size, color); // Right border
        g.fill(x, y + size - 1, x + size, y + size, color); // Bottom border

        g.drawCenteredString(this.font, text, x + size / 2, y + size / 2 - 4, color);
        return hover;
    }

    private boolean isMouseOverButton(int mouseX, int mouseY, int x, int y, int size) {
        return mouseX >= x && mouseX < x + size && mouseY >= y && mouseY < y + size;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

        int centerX = this.width / 2;
        int startY = 60;
        int itemHeight = 30;
        int listWidth = 400;

        for (int i = 0; i < VISIBLE_ITEMS && (i + scrollOffset) < waypoints.size(); i++) {
            Waypoint wp = waypoints.get(i + scrollOffset);
            int y = startY + i * itemHeight;

            int btnX = centerX + listWidth / 2 - 100;
            int btnY = y + 5;
            int btnSize = 20;

            // E = Edit
            if (isMouseOverButton((int) mouseX, (int) mouseY, btnX, btnY, btnSize)) {
                minecraft.setScreen(new WaypointEditScreen(this, waypointManager, wp));
                return true;
            }

            // H = Hide toggle
            btnX += btnSize + 5;
            if (isMouseOverButton((int) mouseX, (int) mouseY, btnX, btnY, btnSize)) {
                wp.toggleVisible();
                waypointManager.updateWaypoint(wp);
                return true;
            }

            // D = Delete
            btnX += btnSize + 5;
            if (isMouseOverButton((int) mouseX, (int) mouseY, btnX, btnY, btnSize)) {
                waypointManager.removeWaypoint(wp);
                refreshWaypoints();
                this.rebuildWidgets();
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (waypoints.size() > VISIBLE_ITEMS) {
            scrollOffset -= (int) scrollY;
            scrollOffset = Math.max(0, Math.min(scrollOffset, waypoints.size() - VISIBLE_ITEMS));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
