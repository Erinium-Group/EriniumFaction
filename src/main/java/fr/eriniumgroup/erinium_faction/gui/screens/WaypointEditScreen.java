package fr.eriniumgroup.erinium_faction.gui.screens;

import fr.eriniumgroup.erinium_faction.client.waypoint.Waypoint;
import fr.eriniumgroup.erinium_faction.client.waypoint.WaypointManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Écran de création ou édition d'un waypoint
 */
public class WaypointEditScreen extends Screen {
    private final Screen parent;
    private final WaypointManager waypointManager;
    private final Waypoint editingWaypoint; // null si création, non-null si édition
    private final boolean isEditing;

    private EditBox nameField;
    private EditBox xField;
    private EditBox yField;
    private EditBox zField;
    private EditBox rField;
    private EditBox gField;
    private EditBox bField;

    private Button currentPosButton;
    private Button saveButton;
    private Button cancelButton;

    private String errorMessage = "";

    public WaypointEditScreen(Screen parent, WaypointManager waypointManager, Waypoint editingWaypoint) {
        super(Component.translatable(editingWaypoint == null ? "erinium_faction.waypoint.create" : "erinium_faction.waypoint.edit"));
        this.parent = parent;
        this.waypointManager = waypointManager;
        this.editingWaypoint = editingWaypoint;
        this.isEditing = editingWaypoint != null;
    }

    @Override
    protected void init() {
        super.init();

        // Décaler tout vers la gauche
        int leftX = 50;
        int startY = this.height / 2 - 100;

        this.addRenderableWidget(Button.builder(Component.translatable("erinium_faction.waypoint.field.name"), b -> {})
                .bounds(leftX, startY, 60, 20).build()).active = false;
        nameField = new EditBox(this.font, leftX + 65, startY, 200, 20, Component.translatable("erinium_faction.waypoint.field.name"));
        nameField.setMaxLength(32);
        if (isEditing) nameField.setValue(editingWaypoint.getName());
        this.addRenderableWidget(nameField);

        this.addRenderableWidget(Button.builder(Component.literal("X:"), b -> {})
                .bounds(leftX, startY + 30, 25, 20).build()).active = false;
        xField = new EditBox(this.font, leftX + 30, startY + 30, 60, 20, Component.literal("X"));
        xField.setMaxLength(10);
        if (isEditing) xField.setValue(String.valueOf(editingWaypoint.getX()));
        this.addRenderableWidget(xField);

        this.addRenderableWidget(Button.builder(Component.literal("Y:"), b -> {})
                .bounds(leftX + 100, startY + 30, 25, 20).build()).active = false;
        yField = new EditBox(this.font, leftX + 130, startY + 30, 60, 20, Component.literal("Y"));
        yField.setMaxLength(10);
        if (isEditing) yField.setValue(String.valueOf(editingWaypoint.getY()));
        this.addRenderableWidget(yField);

        this.addRenderableWidget(Button.builder(Component.literal("Z:"), b -> {})
                .bounds(leftX + 200, startY + 30, 25, 20).build()).active = false;
        zField = new EditBox(this.font, leftX + 230, startY + 30, 60, 20, Component.literal("Z"));
        zField.setMaxLength(10);
        if (isEditing) zField.setValue(String.valueOf(editingWaypoint.getZ()));
        this.addRenderableWidget(zField);

        currentPosButton = Button.builder(Component.translatable("erinium_faction.waypoint.button.current_pos"), b -> {
            Player player = Minecraft.getInstance().player;
            if (player != null) {
                xField.setValue(String.valueOf((int) player.getX()));
                yField.setValue(String.valueOf((int) player.getY()));
                zField.setValue(String.valueOf((int) player.getZ()));
            }
        }).bounds(leftX, startY + 60, 100, 20).build();
        this.addRenderableWidget(currentPosButton);

        this.addRenderableWidget(Button.builder(Component.translatable("erinium_faction.waypoint.field.color"), b -> {})
                .bounds(leftX, startY + 90, 80, 20).build()).active = false;

        this.addRenderableWidget(Button.builder(Component.literal("R:"), b -> {})
                .bounds(leftX + 85, startY + 90, 25, 20).build()).active = false;
        rField = new EditBox(this.font, leftX + 115, startY + 90, 50, 20, Component.literal("R"));
        rField.setMaxLength(3);
        if (isEditing) rField.setValue(String.valueOf(editingWaypoint.getColorR()));
        else rField.setValue("255");
        this.addRenderableWidget(rField);

        this.addRenderableWidget(Button.builder(Component.literal("G:"), b -> {})
                .bounds(leftX + 175, startY + 90, 25, 20).build()).active = false;
        gField = new EditBox(this.font, leftX + 205, startY + 90, 50, 20, Component.literal("G"));
        gField.setMaxLength(3);
        if (isEditing) gField.setValue(String.valueOf(editingWaypoint.getColorG()));
        else gField.setValue("255");
        this.addRenderableWidget(gField);

        this.addRenderableWidget(Button.builder(Component.literal("B:"), b -> {})
                .bounds(leftX + 265, startY + 90, 25, 20).build()).active = false;
        bField = new EditBox(this.font, leftX + 295, startY + 90, 50, 20, Component.literal("B"));
        bField.setMaxLength(3);
        if (isEditing) bField.setValue(String.valueOf(editingWaypoint.getColorB()));
        else bField.setValue("255");
        this.addRenderableWidget(bField);

        int colorY = startY + 120;
        addColorButton(leftX, colorY, 255, 0, 0, "Rouge");
        addColorButton(leftX + 40, colorY, 0, 255, 0, "Vert");
        addColorButton(leftX + 80, colorY, 0, 0, 255, "Bleu");
        addColorButton(leftX + 120, colorY, 255, 255, 0, "Jaune");
        addColorButton(leftX + 160, colorY, 255, 0, 255, "Magenta");
        addColorButton(leftX + 200, colorY, 0, 255, 255, "Cyan");
        addColorButton(leftX + 240, colorY, 255, 255, 255, "Blanc");
        addColorButton(leftX + 280, colorY, 255, 165, 0, "Orange");

        saveButton = Button.builder(Component.translatable(isEditing ? "erinium_faction.waypoint.button.save" : "erinium_faction.waypoint.button.create"), b -> {
            if (saveWaypoint()) {
                minecraft.setScreen(parent);
            }
        }).bounds(leftX, startY + 160, 100, 20).build();
        this.addRenderableWidget(saveButton);

        cancelButton = Button.builder(Component.translatable("erinium_faction.waypoint.button.cancel"), b -> {
            minecraft.setScreen(parent);
        }).bounds(leftX + 110, startY + 160, 75, 20).build();
        this.addRenderableWidget(cancelButton);
    }

    private List<ColorButton> colorButtons = new ArrayList<>();

    private void addColorButton(int x, int y, int r, int g, int b, String name) {
        ColorButton btn = new ColorButton(x, y, 30, 20, r, g, b, button -> {
            rField.setValue(String.valueOf(r));
            gField.setValue(String.valueOf(g));
            bField.setValue(String.valueOf(b));
        });
        colorButtons.add(btn);
        this.addRenderableWidget(btn);
    }

    // Classe interne pour les boutons de couleur
    private static class ColorButton extends Button {
        private final int color;

        public ColorButton(int x, int y, int width, int height, int r, int g, int b, OnPress onPress) {
            super(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
            this.color = 0xFF000000 | (r << 16) | (g << 8) | b;
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            // Dessiner un rectangle de couleur
            graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, color);
            // Bordure blanche si hover
            if (this.isHovered()) {
                graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + 1, 0xFFFFFFFF);
                graphics.fill(this.getX(), this.getY() + this.height - 1, this.getX() + this.width, this.getY() + this.height, 0xFFFFFFFF);
                graphics.fill(this.getX(), this.getY(), this.getX() + 1, this.getY() + this.height, 0xFFFFFFFF);
                graphics.fill(this.getX() + this.width - 1, this.getY(), this.getX() + this.width, this.getY() + this.height, 0xFFFFFFFF);
            }
        }
    }

    private boolean saveWaypoint() {
        errorMessage = "";

        String name = nameField.getValue().trim();
        if (name.isEmpty()) {
            errorMessage = Component.translatable("erinium_faction.waypoint.error.empty_name").getString();
            return false;
        }

        int x, y, z, r, g, b;
        try {
            x = Integer.parseInt(xField.getValue());
            y = Integer.parseInt(yField.getValue());
            z = Integer.parseInt(zField.getValue());
            r = Integer.parseInt(rField.getValue());
            g = Integer.parseInt(gField.getValue());
            b = Integer.parseInt(bField.getValue());
        } catch (NumberFormatException e) {
            errorMessage = Component.translatable("erinium_faction.waypoint.error.invalid_number").getString();
            return false;
        }

        if (r < 0 || r > 255 || g < 0 || g > 255 || b < 0 || b > 255) {
            errorMessage = Component.translatable("erinium_faction.waypoint.error.invalid_rgb").getString();
            return false;
        }

        Player player = Minecraft.getInstance().player;
        if (player == null) {
            errorMessage = Component.translatable("erinium_faction.waypoint.error.no_player").getString();
            return false;
        }

        String dimension = player.level().dimension().location().toString();

        if (isEditing) {
            editingWaypoint.setName(name);
            editingWaypoint.setX(x);
            editingWaypoint.setY(y);
            editingWaypoint.setZ(z);
            editingWaypoint.setColor(r, g, b);
            waypointManager.updateWaypoint(editingWaypoint);
        } else {
            if (!waypointManager.canAddWaypoint()) {
                errorMessage = Component.translatable("erinium_faction.waypoint.error.limit", WaypointManager.getMaxWaypoints()).getString();
                return false;
            }
            Waypoint newWaypoint = new Waypoint(name, x, y, z, dimension, r, g, b);
            waypointManager.addWaypoint(newWaypoint);
        }

        return true;
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
        // Ne rien render pour éviter le flou
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
        // Fond noir semi-transparent
        g.fill(0, 0, this.width, this.height, 0xCC000000);

        super.render(g, mouseX, mouseY, partialTicks);

        // Titre
        g.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);

        // Afficher erreur
        if (!errorMessage.isEmpty()) {
            g.drawCenteredString(this.font, errorMessage, this.width / 2, this.height / 2 + 80, 0xFF5555);
        }

        int previewX = this.width / 2 + 170;
        int previewY = this.height / 2 - 10;
        try {
            int red = Integer.parseInt(rField.getValue());
            int green = Integer.parseInt(gField.getValue());
            int blue = Integer.parseInt(bField.getValue());
            int color = 0xFF000000 | (red << 16) | (green << 8) | blue;
            g.fill(previewX, previewY, previewX + 30, previewY + 30, color);
            g.drawString(this.font, Component.translatable("erinium_faction.waypoint.preview").getString(), previewX - 5, previewY + 35, 0xCCCCCC, false);
        } catch (NumberFormatException ignored) {}
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
