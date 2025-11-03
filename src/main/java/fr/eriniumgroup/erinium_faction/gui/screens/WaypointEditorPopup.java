package fr.eriniumgroup.erinium_faction.gui.screens;

import fr.eriniumgroup.erinium_faction.features.minimap.Waypoint;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.UUID;

/**
 * Interface pour les écrans qui peuvent ouvrir le popup waypoint
 */
interface WaypointEditorParent {
    void closeWaypointEditor(boolean save, WaypointEditorPopup.WaypointData data);
}

/**
 * Popup pour créer/éditer un waypoint
 */
public class WaypointEditorPopup {
    private final Object parent; // Can be MinimapFullscreenScreen or WaypointListScreen
    private final Waypoint existingWaypoint;
    private final boolean isNew;

    private final int popupX;
    private final int popupY;
    private final int popupWidth = 360;
    private final int popupHeight = 270; // Compact height

    private EditBox nameField;
    private EditBox xField;
    private EditBox yField;
    private EditBox zField;

    // RGB fields for custom color
    private EditBox rField;
    private EditBox gField;
    private EditBox bField;

    private Waypoint.WaypointColor selectedColor;
    private int customRGB = 0xFFFFFF; // Default white
    private Button[] colorButtons;
    private Button saveButton;
    private Button cancelButton;
    private Button deleteButton;
    private Button useCurrentPosButton;

    private BlockPos currentPosition;
    private ResourceKey<Level> currentDimension;

    public WaypointEditorPopup(Object parent, Waypoint existing, BlockPos currentPos, ResourceKey<Level> dimension) {
        this.parent = parent;
        this.existingWaypoint = existing;
        this.isNew = (existing == null);
        this.currentPosition = currentPos;
        this.currentDimension = dimension;

        Minecraft mc = Minecraft.getInstance();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        this.popupX = (screenWidth - popupWidth) / 2;
        this.popupY = (screenHeight - popupHeight) / 2;

        init();
    }

    private void init() {
        Minecraft mc = Minecraft.getInstance();

        // Name field (compact)
        nameField = new EditBox(mc.font, popupX + 20, popupY + 40, popupWidth - 40, 18, Component.literal("Name"));
        nameField.setMaxLength(50);
        if (!isNew && existingWaypoint != null) {
            nameField.setValue(existingWaypoint.getName());
        } else {
            nameField.setValue("New Waypoint");
        }

        // Coordinate fields (compact, en ligne)
        BlockPos initialPos = isNew ? currentPosition : existingWaypoint.getPosition();

        xField = new EditBox(mc.font, popupX + 35, popupY + 75, 65, 16, Component.literal("X"));
        xField.setValue(String.valueOf(initialPos.getX()));
        xField.setMaxLength(10);

        yField = new EditBox(mc.font, popupX + 135, popupY + 75, 50, 16, Component.literal("Y"));
        yField.setValue(String.valueOf(initialPos.getY()));
        yField.setMaxLength(10);

        zField = new EditBox(mc.font, popupX + 220, popupY + 75, 65, 16, Component.literal("Z"));
        zField.setValue(String.valueOf(initialPos.getZ()));
        zField.setMaxLength(10);

        // Use current position button (compact)
        useCurrentPosButton = Button.builder(Component.literal("Current Pos"), btn -> {
            xField.setValue(String.valueOf(currentPosition.getX()));
            yField.setValue(String.valueOf(currentPosition.getY()));
            zField.setValue(String.valueOf(currentPosition.getZ()));
        }).bounds(popupX + 295, popupY + 73, 55, 18).build();

        // Color selection (compact, 3 par ligne)
        selectedColor = isNew ? Waypoint.WaypointColor.RED : existingWaypoint.getColor();
        colorButtons = new Button[Waypoint.WaypointColor.values().length];

        // RGB fields for custom color
        if (!isNew && existingWaypoint != null) {
            customRGB = existingWaypoint.getCustomColor() & 0xFFFFFF; // Remove alpha
        }

        int r = (customRGB >> 16) & 0xFF;
        int g = (customRGB >> 8) & 0xFF;
        int b = customRGB & 0xFF;

        rField = new EditBox(mc.font, popupX + 55, popupY + 195, 32, 16, Component.literal("R"));
        rField.setValue(String.valueOf(r));
        rField.setMaxLength(3);

        gField = new EditBox(mc.font, popupX + 120, popupY + 195, 32, 16, Component.literal("G"));
        gField.setValue(String.valueOf(g));
        gField.setMaxLength(3);

        bField = new EditBox(mc.font, popupX + 185, popupY + 195, 32, 16, Component.literal("B"));
        bField.setValue(String.valueOf(b));
        bField.setMaxLength(3);

        // Action buttons (compact)
        cancelButton = Button.builder(Component.translatable("gui.cancel"), btn -> {
            closePopup(false, null);
        }).bounds(popupX + 20, popupY + 240, 70, 20).build();

        if (!isNew) {
            deleteButton = Button.builder(Component.translatable("gui.erinium_faction.waypoint.delete"), btn -> {
                closePopup(true, new WaypointData(
                    existingWaypoint.getId(),
                    "",
                    BlockPos.ZERO,
                    currentDimension,
                    Waypoint.WaypointColor.RED,
                    0xFFFFFF,
                    false, // deleted
                    false
                ));
            }).bounds(popupX + 100, popupY + 240, 70, 20).build();
        }

        saveButton = Button.builder(Component.translatable("gui.done"), btn -> {
            save();
        }).bounds(popupX + (isNew ? 180 : 260), popupY + 240, 70, 20).build();
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Overlay opaque pour bloquer tout ce qui est derrière (incluant les boutons de l'écran)
        graphics.fill(0, 0, graphics.guiWidth(), graphics.guiHeight(), 0xCC000000);

        // Popup background
        graphics.fill(popupX, popupY, popupX + popupWidth, popupY + popupHeight, 0xEE1a1a1a);

        // Border
        graphics.fill(popupX - 2, popupY - 2, popupX + popupWidth + 2, popupY, 0xFFd4af37);
        graphics.fill(popupX - 2, popupY + popupHeight, popupX + popupWidth + 2, popupY + popupHeight + 2, 0xFFd4af37);
        graphics.fill(popupX - 2, popupY, popupX, popupY + popupHeight, 0xFFd4af37);
        graphics.fill(popupX + popupWidth, popupY, popupX + popupWidth + 2, popupY + popupHeight, 0xFFd4af37);

        // Title
        Minecraft mc = Minecraft.getInstance();
        graphics.drawCenteredString(mc.font,
            Component.literal(isNew ? "Add Waypoint" : "Edit Waypoint"),
            popupX + popupWidth / 2, popupY + 10, 0xFFffd700);

        // Labels
        graphics.drawString(mc.font, "Name:", popupX + 20, popupY + 30, 0xFFFFFFFF);
        graphics.drawString(mc.font, "Position:", popupX + 20, popupY + 62, 0xFFFFFFFF);

        graphics.drawString(mc.font, "X:", popupX + 20, popupY + 78, 0xFF87CEEB, false);
        graphics.drawString(mc.font, "Y:", popupX + 110, popupY + 78, 0xFF87CEEB, false);
        graphics.drawString(mc.font, "Z:", popupX + 195, popupY + 78, 0xFF87CEEB, false);

        graphics.drawString(mc.font, "Color:", popupX + 20, popupY + 100, 0xFFFFFFFF);

        // Render fields
        nameField.render(graphics, mouseX, mouseY, partialTick);
        xField.render(graphics, mouseX, mouseY, partialTick);
        yField.render(graphics, mouseX, mouseY, partialTick);
        zField.render(graphics, mouseX, mouseY, partialTick);

        // Render RGB fields inline
        graphics.drawString(mc.font, "RGB:", popupX + 20, popupY + 182, 0xFFFFFFFF, false);

        // Indicateur si CUSTOM est sélectionné
        if (selectedColor == Waypoint.WaypointColor.CUSTOM) {
            graphics.drawString(mc.font, "(active)", popupX + 55, popupY + 182, 0xFF00FF00, false);
        } else {
            graphics.drawString(mc.font, "(inactive)", popupX + 55, popupY + 182, 0xFF888888, false);
        }

        graphics.drawString(mc.font, "R", popupX + 50, popupY + 198, 0xFFFF5555, false);
        graphics.drawString(mc.font, "G", popupX + 115, popupY + 198, 0xFF55FF55, false);
        graphics.drawString(mc.font, "B", popupX + 180, popupY + 198, 0xFF5555FF, false);

        rField.render(graphics, mouseX, mouseY, partialTick);
        gField.render(graphics, mouseX, mouseY, partialTick);
        bField.render(graphics, mouseX, mouseY, partialTick);

        // Preview of custom color
        try {
            int r = Math.min(255, Math.max(0, Integer.parseInt(rField.getValue())));
            int g = Math.min(255, Math.max(0, Integer.parseInt(gField.getValue())));
            int b = Math.min(255, Math.max(0, Integer.parseInt(bField.getValue())));
            int previewColor = 0xFF000000 | (r << 16) | (g << 8) | b;
            graphics.fill(popupX + 225, popupY + 195, popupX + 245, popupY + 211, previewColor);

            // Border autour du preview
            graphics.fill(popupX + 224, popupY + 194, popupX + 246, popupY + 195, 0xFFFFFFFF);
            graphics.fill(popupX + 224, popupY + 211, popupX + 246, popupY + 212, 0xFFFFFFFF);
            graphics.fill(popupX + 224, popupY + 195, popupX + 225, popupY + 211, 0xFFFFFFFF);
            graphics.fill(popupX + 245, popupY + 195, popupX + 246, popupY + 211, 0xFFFFFFFF);
        } catch (NumberFormatException ignored) {}

        // Render color buttons (1 ligne de 7 boutons compacts)
        int colorX = popupX + 55;
        int colorY = popupY + 115;
        int colorSize = 16;
        int spacing = 38;

        Waypoint.WaypointColor[] colors = Waypoint.WaypointColor.values();
        for (int i = 0; i < colors.length; i++) {
            Waypoint.WaypointColor color = colors[i];
            int btnX = colorX + (i * spacing);
            int btnY = colorY;

            // Square with color
            int squareColor = color.getColor();
            graphics.fill(btnX, btnY, btnX + colorSize, btnY + colorSize, squareColor);

            // Selected border
            if (color == selectedColor) {
                graphics.fill(btnX - 2, btnY - 2, btnX + colorSize + 2, btnY, 0xFFffd700);
                graphics.fill(btnX - 2, btnY + colorSize, btnX + colorSize + 2, btnY + colorSize + 2, 0xFFffd700);
                graphics.fill(btnX - 2, btnY, btnX, btnY + colorSize, 0xFFffd700);
                graphics.fill(btnX + colorSize, btnY, btnX + colorSize + 2, btnY + colorSize, 0xFFffd700);
            }
        }

        // Render buttons
        useCurrentPosButton.render(graphics, mouseX, mouseY, partialTick);
        cancelButton.render(graphics, mouseX, mouseY, partialTick);
        if (deleteButton != null) {
            deleteButton.render(graphics, mouseX, mouseY, partialTick);
        }
        saveButton.render(graphics, mouseX, mouseY, partialTick);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Check if clicked outside popup
        if (mouseX < popupX || mouseX > popupX + popupWidth ||
            mouseY < popupY || mouseY > popupY + popupHeight) {
            closePopup(false, null);
            return true;
        }

        // Handle EditBox clicks manually with proper focus management
        boolean fieldClicked = false;

        // Check each field and set focus accordingly
        if (nameField.isMouseOver(mouseX, mouseY)) {
            nameField.setFocused(true);
            nameField.mouseClicked(mouseX, mouseY, button);
            fieldClicked = true;
        } else {
            nameField.setFocused(false);
        }

        if (xField.isMouseOver(mouseX, mouseY)) {
            xField.setFocused(true);
            xField.mouseClicked(mouseX, mouseY, button);
            fieldClicked = true;
        } else {
            xField.setFocused(false);
        }

        if (yField.isMouseOver(mouseX, mouseY)) {
            yField.setFocused(true);
            yField.mouseClicked(mouseX, mouseY, button);
            fieldClicked = true;
        } else {
            yField.setFocused(false);
        }

        if (zField.isMouseOver(mouseX, mouseY)) {
            zField.setFocused(true);
            zField.mouseClicked(mouseX, mouseY, button);
            fieldClicked = true;
        } else {
            zField.setFocused(false);
        }

        if (rField.isMouseOver(mouseX, mouseY)) {
            rField.setFocused(true);
            rField.mouseClicked(mouseX, mouseY, button);
            fieldClicked = true;
        } else {
            rField.setFocused(false);
        }

        if (gField.isMouseOver(mouseX, mouseY)) {
            gField.setFocused(true);
            gField.mouseClicked(mouseX, mouseY, button);
            fieldClicked = true;
        } else {
            gField.setFocused(false);
        }

        if (bField.isMouseOver(mouseX, mouseY)) {
            bField.setFocused(true);
            bField.mouseClicked(mouseX, mouseY, button);
            fieldClicked = true;
        } else {
            bField.setFocused(false);
        }

        if (fieldClicked) {
            return true;
        }

        // Color buttons (manual check)
        int colorX = popupX + 55;
        int colorY = popupY + 115;
        int colorSize = 16;
        int spacing = 38;

        Waypoint.WaypointColor[] colors = Waypoint.WaypointColor.values();
        for (int i = 0; i < colors.length; i++) {
            Waypoint.WaypointColor color = colors[i];
            int btnX = colorX + (i * spacing);
            int btnY = colorY;

            if (mouseX >= btnX && mouseX <= btnX + colorSize &&
                mouseY >= btnY && mouseY <= btnY + colorSize) {
                selectedColor = color;
                return true;
            }
        }

        // Buttons
        if (useCurrentPosButton.mouseClicked(mouseX, mouseY, button)) return true;
        if (cancelButton.mouseClicked(mouseX, mouseY, button)) return true;
        if (deleteButton != null && deleteButton.mouseClicked(mouseX, mouseY, button)) return true;
        if (saveButton.mouseClicked(mouseX, mouseY, button)) return true;

        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (nameField.keyPressed(keyCode, scanCode, modifiers)) return true;
        if (xField.keyPressed(keyCode, scanCode, modifiers)) return true;
        if (yField.keyPressed(keyCode, scanCode, modifiers)) return true;
        if (zField.keyPressed(keyCode, scanCode, modifiers)) return true;
        if (rField.keyPressed(keyCode, scanCode, modifiers)) return true;
        if (gField.keyPressed(keyCode, scanCode, modifiers)) return true;
        if (bField.keyPressed(keyCode, scanCode, modifiers)) return true;
        return false;
    }

    public boolean charTyped(char codePoint, int modifiers) {
        if (nameField.charTyped(codePoint, modifiers)) return true;
        if (xField.charTyped(codePoint, modifiers)) return true;
        if (yField.charTyped(codePoint, modifiers)) return true;
        if (zField.charTyped(codePoint, modifiers)) return true;
        if (rField.charTyped(codePoint, modifiers)) return true;
        if (gField.charTyped(codePoint, modifiers)) return true;
        if (bField.charTyped(codePoint, modifiers)) return true;
        return false;
    }

    public void tick() {
        // This method is called from parent screen to keep EditBox fields responsive
        // EditBox doesn't have a tick() method, but we need this for compatibility
    }

    private void save() {
        try {
            String name = nameField.getValue();
            int x = Integer.parseInt(xField.getValue());
            int y = Integer.parseInt(yField.getValue());
            int z = Integer.parseInt(zField.getValue());

            BlockPos pos = new BlockPos(x, y, z);
            UUID id = isNew ? UUID.randomUUID() : existingWaypoint.getId();

            // Get custom RGB if selected
            int finalRGB = 0xFFFFFF;
            if (selectedColor == Waypoint.WaypointColor.CUSTOM) {
                try {
                    int r = Math.min(255, Math.max(0, Integer.parseInt(rField.getValue())));
                    int g = Math.min(255, Math.max(0, Integer.parseInt(gField.getValue())));
                    int b = Math.min(255, Math.max(0, Integer.parseInt(bField.getValue())));
                    finalRGB = (r << 16) | (g << 8) | b;
                } catch (NumberFormatException ignored) {}
            }

            WaypointData data = new WaypointData(id, name, pos, currentDimension, selectedColor, finalRGB, true, isNew);
            closePopup(true, data);
        } catch (NumberFormatException e) {
            // Invalid coordinates, do nothing
        }
    }

    private void closePopup(boolean save, WaypointData data) {
        if (parent instanceof MinimapFullscreenScreen screen) {
            screen.closeWaypointEditor(save, data);
        } else if (parent instanceof WaypointListScreen screen) {
            screen.closeWaypointEditor(save, data);
        }
    }

    public record WaypointData(
        UUID id,
        String name,
        BlockPos position,
        ResourceKey<Level> dimension,
        Waypoint.WaypointColor color,
        int customRGB,
        boolean enabled,
        boolean isNew
    ) {}
}
