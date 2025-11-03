package fr.eriniumgroup.erinium_faction.gui.screens;

import fr.eriniumgroup.erinium_faction.common.network.packets.WaypointActionPacket;
import fr.eriniumgroup.erinium_faction.features.minimap.Waypoint;
import fr.eriniumgroup.erinium_faction.features.minimap.WaypointManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

/**
 * Écran pour gérer la liste des waypoints
 */
public class WaypointListScreen extends Screen {
    private static final int GUI_WIDTH = 300;
    private static final int GUI_HEIGHT = 240;

    private final Screen parent;
    private int guiLeft;
    private int guiTop;

    private List<Waypoint> waypoints = new ArrayList<>();
    private int scrollOffset = 0;
    private static final int ITEMS_PER_PAGE = 8;

    private WaypointEditorPopup editorPopup = null;

    public WaypointListScreen(Screen parent) {
        super(Component.literal("Waypoints Manager"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        guiLeft = (width - GUI_WIDTH) / 2;
        guiTop = (height - GUI_HEIGHT) / 2;

        // Charger tous les waypoints
        waypoints.clear();
        waypoints.addAll(WaypointManager.getInstance().getAllWaypoints());

        // Bouton Close
        addRenderableWidget(Button.builder(Component.literal("Close"), btn -> onClose())
            .bounds(guiLeft + GUI_WIDTH / 2 - 40, guiTop + GUI_HEIGHT - 25, 80, 20)
            .build());

        // Bouton Add New Waypoint
        addRenderableWidget(Button.builder(Component.literal("+ Add New"), btn -> {
            if (minecraft != null && minecraft.player != null) {
                editorPopup = new WaypointEditorPopup(
                    this,
                    null,
                    minecraft.player.blockPosition(),
                    minecraft.player.level().dimension()
                );
            }
        }).bounds(guiLeft + 10, guiTop + GUI_HEIGHT - 25, 80, 20).build());

        // Scroll buttons - moved to far right to avoid overlapping waypoints
        addRenderableWidget(Button.builder(Component.literal("▲"), btn -> {
            scrollOffset = Math.max(0, scrollOffset - 1);
        }).bounds(guiLeft + GUI_WIDTH + 5, guiTop + 35, 20, 20).build());

        addRenderableWidget(Button.builder(Component.literal("▼"), btn -> {
            int maxScroll = Math.max(0, waypoints.size() - ITEMS_PER_PAGE);
            scrollOffset = Math.min(maxScroll, scrollOffset + 1);
        }).bounds(guiLeft + GUI_WIDTH + 5, guiTop + GUI_HEIGHT - 50, 20, 20).build());
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Override to remove blur overlay - no blur effect wanted
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // If popup is open, only render the popup (block everything else)
        if (editorPopup != null) {
            // Render dark overlay to block background
            graphics.fill(0, 0, graphics.guiWidth(), graphics.guiHeight(), 0xDD000000);

            // Render editor popup on top
            editorPopup.render(graphics, mouseX, mouseY, partialTick);
            return; // Don't render anything else
        }

        // Background
        graphics.fill(guiLeft, guiTop, guiLeft + GUI_WIDTH, guiTop + GUI_HEIGHT, 0xDD1a1a1a);

        // Border
        graphics.fill(guiLeft - 2, guiTop - 2, guiLeft + GUI_WIDTH + 2, guiTop, 0xFFd4af37);
        graphics.fill(guiLeft - 2, guiTop + GUI_HEIGHT, guiLeft + GUI_WIDTH + 2, guiTop + GUI_HEIGHT + 2, 0xFFd4af37);
        graphics.fill(guiLeft - 2, guiTop, guiLeft, guiTop + GUI_HEIGHT, 0xFFd4af37);
        graphics.fill(guiLeft + GUI_WIDTH, guiTop, guiLeft + GUI_WIDTH + 2, guiTop + GUI_HEIGHT, 0xFFd4af37);

        // Title
        graphics.drawCenteredString(font, title, guiLeft + GUI_WIDTH / 2, guiTop + 10, 0xFFffd700);

        // Render waypoint list
        renderWaypointList(graphics, mouseX, mouseY);

        // Render buttons
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderWaypointList(GuiGraphics graphics, int mouseX, int mouseY) {
        int listX = guiLeft + 10;
        int listY = guiTop + 35;
        int itemHeight = 20;

        int endIndex = Math.min(waypoints.size(), scrollOffset + ITEMS_PER_PAGE);

        for (int i = scrollOffset; i < endIndex; i++) {
            Waypoint wp = waypoints.get(i);
            int y = listY + (i - scrollOffset) * itemHeight;

            // Background pour l'item
            int bgColor = (i % 2 == 0) ? 0x40333333 : 0x40222222;
            graphics.fill(listX, y, listX + 270, y + itemHeight - 2, bgColor);

            // Couleur indicator
            graphics.fill(listX + 2, y + 2, listX + 14, y + 14, wp.getEffectiveColor());

            // Nom
            String name = wp.getName();
            if (name.length() > 20) {
                name = name.substring(0, 20) + "...";
            }
            graphics.drawString(font, name, listX + 18, y + 5, wp.isEnabled() ? 0xFFFFFFFF : 0xFF888888, false);

            // Position
            String pos = "(" + wp.getPosition().getX() + ", " + wp.getPosition().getY() + ", " + wp.getPosition().getZ() + ")";
            graphics.drawString(font, pos, listX + 120, y + 5, 0xFF888888, false);

            // Buttons: Edit, Toggle, Delete - repositioned more to the right
            int btnY = y + 2;
            int btnStartX = listX + 200; // Starting position for buttons

            // Edit button
            if (isMouseOver(mouseX, mouseY, btnStartX, btnY, 20, 15)) {
                graphics.fill(btnStartX, btnY, btnStartX + 20, btnY + 15, 0x80FFFFFF);
            }
            graphics.drawString(font, "E", btnStartX + 6, btnY + 3, 0xFF00FF00, false);

            // Toggle button (show/hide)
            if (isMouseOver(mouseX, mouseY, btnStartX + 25, btnY, 20, 15)) {
                graphics.fill(btnStartX + 25, btnY, btnStartX + 45, btnY + 15, 0x80FFFFFF);
            }
            graphics.drawString(font, wp.isEnabled() ? "H" : "S", btnStartX + 31, btnY + 3, wp.isEnabled() ? 0xFFFFAA00 : 0xFF00FF00, false);

            // Delete button
            if (isMouseOver(mouseX, mouseY, btnStartX + 50, btnY, 20, 15)) {
                graphics.fill(btnStartX + 50, btnY, btnStartX + 70, btnY + 15, 0x80FFFFFF);
            }
            graphics.drawString(font, "X", btnStartX + 56, btnY + 3, 0xFFFF0000, false);
        }

        // Info text
        String info = "Total: " + waypoints.size() + " waypoints";
        graphics.drawString(font, info, listX, guiTop + GUI_HEIGHT - 45, 0xFF888888, false);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Editor popup priority for keyboard
        if (editorPopup != null) {
            return editorPopup.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        // Editor popup priority for text input
        if (editorPopup != null) {
            return editorPopup.charTyped(codePoint, modifiers);
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public void tick() {
        super.tick();
        // Update popup tick to keep EditBox fields responsive
        if (editorPopup != null) {
            editorPopup.tick();
        }

        // Refresh waypoint list if count changed (to handle server sync)
        int currentCount = WaypointManager.getInstance().getAllWaypoints().size();
        if (currentCount != waypoints.size()) {
            waypoints.clear();
            waypoints.addAll(WaypointManager.getInstance().getAllWaypoints());
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Editor popup priority - BLOCK ALL CLICKS if popup is open
        if (editorPopup != null) {
            editorPopup.mouseClicked(mouseX, mouseY, button);
            return true; // Always consume the click when popup is open
        }

        // Check buttons
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        // Check waypoint item clicks
        if (button == 0) {
            int listX = guiLeft + 10;
            int listY = guiTop + 35;
            int itemHeight = 20;

            int endIndex = Math.min(waypoints.size(), scrollOffset + ITEMS_PER_PAGE);

            for (int i = scrollOffset; i < endIndex; i++) {
                Waypoint wp = waypoints.get(i);
                int y = listY + (i - scrollOffset) * itemHeight;
                int btnY = y + 2;
                int btnStartX = listX + 200; // Same as in render

                // Edit button
                if (isMouseOver(mouseX, mouseY, btnStartX, btnY, 20, 15)) {
                    editorPopup = new WaypointEditorPopup(
                        this,
                        wp,
                        wp.getPosition(),
                        wp.getDimension()
                    );
                    return true;
                }

                // Toggle button
                if (isMouseOver(mouseX, mouseY, btnStartX + 25, btnY, 20, 15)) {
                    wp.setEnabled(!wp.isEnabled());
                    // Send update to server
                    PacketDistributor.sendToServer(new WaypointActionPacket(
                        WaypointActionPacket.Action.UPDATE,
                        wp.getId(),
                        wp.getName(),
                        wp.getPosition(),
                        wp.getDimension(),
                        wp.getColor(),
                        wp.getCustomColor(),
                        wp.isEnabled()
                    ));
                    // Refresh list to reflect changes
                    waypoints.clear();
                    waypoints.addAll(WaypointManager.getInstance().getAllWaypoints());
                    return true;
                }

                // Delete button
                if (isMouseOver(mouseX, mouseY, btnStartX + 50, btnY, 20, 15)) {
                    // Send delete to server
                    PacketDistributor.sendToServer(new WaypointActionPacket(
                        WaypointActionPacket.Action.REMOVE,
                        wp.getId(),
                        "",
                        wp.getPosition(),
                        wp.getDimension(),
                        wp.getColor(),
                        wp.getCustomColor(),
                        wp.isEnabled()
                    ));
                    // Remove locally
                    WaypointManager.getInstance().removeWaypoint(wp.getId());
                    waypoints.remove(i);
                    return true;
                }
            }
        }

        return false;
    }

    public void closeWaypointEditor(boolean save, WaypointEditorPopup.WaypointData data) {
        if (save && data != null) {
            WaypointActionPacket.Action action;

            if (data.isNew()) {
                action = WaypointActionPacket.Action.ADD;
            } else {
                action = WaypointActionPacket.Action.UPDATE;
            }

            PacketDistributor.sendToServer(new WaypointActionPacket(
                action,
                data.id(),
                data.name(),
                data.position(),
                data.dimension(),
                data.color(),
                data.customRGB(),
                data.enabled()
            ));

            // Refresh list
            waypoints.clear();
            waypoints.addAll(WaypointManager.getInstance().getAllWaypoints());
        }
        editorPopup = null;
    }

    private boolean isMouseOver(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreen(parent);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
