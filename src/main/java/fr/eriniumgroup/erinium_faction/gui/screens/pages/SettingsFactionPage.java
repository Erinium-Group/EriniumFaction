package fr.eriniumgroup.erinium_faction.gui.screens.pages;

import fr.eriniumgroup.erinium_faction.gui.screens.components.StyledButton;
import fr.eriniumgroup.erinium_faction.gui.screens.components.StyledTextField;
import fr.eriniumgroup.erinium_faction.gui.screens.components.StyledToggle;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

/**
 * Page Settings Faction - Basée sur settings-faction.svg
 * Paramètres de la faction avec vrais inputs et boutons
 */
public class SettingsFactionPage extends FactionPage {

    private StyledTextField nameField;
    private StyledTextField descriptionField;
    private StyledToggle openToJoinToggle;
    private final List<StyledButton> actionButtons = new ArrayList<>();

    public SettingsFactionPage(Font font) {
        super(font);
    }

    private void initComponents(int leftPos, int topPos, double scaleX, double scaleY) {
        if (nameField == null) {
            int x = sx(CONTENT_X, leftPos, scaleX);
            int y = sy(CONTENT_Y, topPos, scaleY);
            int inputWidth = sw(200, scaleX);  // Réduit pour rentrer dans 275px
            int inputHeight = sh(15, scaleY);

            // Récupérer les données de faction
            var data = getFactionData();

            // Text fields (réduits et mieux espacés) avec vraies données
            nameField = new StyledTextField(font);
            nameField.setPlaceholder("Faction Name");
            nameField.setText(data != null ? data.displayName : "");
            nameField.setBounds(x + sw(70, scaleX), y + sh(27, scaleY), inputWidth, inputHeight);

            descriptionField = new StyledTextField(font);
            descriptionField.setPlaceholder("Faction Description");
            descriptionField.setText(data != null && data.description != null ? data.description : "");
            descriptionField.setBounds(x + sw(70, scaleX), y + sh(54, scaleY), inputWidth, inputHeight);

            // Toggle basé sur le mode de faction
            boolean isOpen = data != null && "OPEN".equalsIgnoreCase(data.mode);
            openToJoinToggle = new StyledToggle(font, "Open to Join", isOpen, state -> {
                System.out.println("SettingsFactionPage: Open to Join set to " + state);
            });
            openToJoinToggle.setBounds(x + sw(4, scaleX), y + sh(108, scaleY));

            // Action buttons (réduits)
            actionButtons.clear();

            StyledButton saveBtn = new StyledButton(font, "Save", () -> {
                System.out.println("SettingsFactionPage: Saving changes...");
                System.out.println("  Name: " + nameField.getText());
                System.out.println("  Description: " + descriptionField.getText());
                System.out.println("  Open to Join: " + openToJoinToggle.getState());
            });
            saveBtn.setPrimary(true);
            saveBtn.setBounds(x + sw(4, scaleX), y + sh(140, scaleY), sw(85, scaleX), sh(17, scaleY));
            actionButtons.add(saveBtn);

            StyledButton resetBtn = new StyledButton(font, "Reset", () -> {
                System.out.println("SettingsFactionPage: Resetting to defaults");
                var resetData = getFactionData();
                nameField.setText(resetData != null ? resetData.displayName : "");
                descriptionField.setText(resetData != null && resetData.description != null ? resetData.description : "");
                openToJoinToggle.setState(resetData != null && "OPEN".equalsIgnoreCase(resetData.mode));
            });
            resetBtn.setBounds(x + sw(95, scaleX), y + sh(140, scaleY), sw(85, scaleX), sh(17, scaleY));
            actionButtons.add(resetBtn);

            StyledButton disbandBtn = new StyledButton(font, "Disband", () -> {
                System.out.println("SettingsFactionPage: WARNING - Attempting to disband faction");
            });
            // Positionné après Reset: 95 + 85 + 6 espacement = 186
            disbandBtn.setBounds(x + sw(186, scaleX), y + sh(140, scaleY), sw(85, scaleX), sh(17, scaleY));
            actionButtons.add(disbandBtn);
        }
    }

    @Override
    public void render(GuiGraphics g, int leftPos, int topPos, double scaleX, double scaleY, int mouseX, int mouseY) {
        initComponents(leftPos, topPos, scaleX, scaleY);

        int x = sx(CONTENT_X, leftPos, scaleX);
        int y = sy(CONTENT_Y, topPos, scaleY);
        int w = sw(CONTENT_W, scaleX);

        // Recalculer les positions à chaque frame pour le scaling
        int inputWidth = sw(200, scaleX);
        int inputHeight = sh(15, scaleY);
        nameField.setBounds(x + sw(70, scaleX), y + sh(27, scaleY), inputWidth, inputHeight);
        descriptionField.setBounds(x + sw(70, scaleX), y + sh(54, scaleY), inputWidth, inputHeight);
        openToJoinToggle.setBounds(x + sw(4, scaleX), y + sh(108, scaleY));

        // Recalculer positions des boutons
        actionButtons.get(0).setBounds(x + sw(4, scaleX), y + sh(140, scaleY), sw(85, scaleX), sh(17, scaleY));
        actionButtons.get(1).setBounds(x + sw(95, scaleX), y + sh(140, scaleY), sw(85, scaleX), sh(17, scaleY));
        actionButtons.get(2).setBounds(x + sw(186, scaleX), y + sh(140, scaleY), sw(85, scaleX), sh(17, scaleY));

        // Header
        g.fill(x, y, x + w, y + sh(22, scaleY), 0xE61e1e2e);
        g.fill(x, y, x + w, y + 1, 0xFF00d2ff);
        g.drawString(font, "FACTION SETTINGS", x + sw(9, scaleX), y + sh(9, scaleY), 0xFFffffff, true);

        // Labels (réduits et mieux alignés)
        g.drawString(font, "Name:", x + sw(4, scaleX), y + sh(31, scaleY), 0xFFa0a0c0, false);
        g.drawString(font, "Desc:", x + sw(4, scaleX), y + sh(58, scaleY), 0xFFa0a0c0, false);
        g.drawString(font, "MOTD:", x + sw(4, scaleX), y + sh(85, scaleY), 0xFFa0a0c0, false);

        // Render text fields
        nameField.render(g, mouseX, mouseY);
        descriptionField.render(g, mouseX, mouseY);

        // Render toggle
        openToJoinToggle.render(g, mouseX, mouseY);

        // Render buttons
        for (StyledButton btn : actionButtons) {
            btn.render(g, mouseX, mouseY);
        }
    }

    @Override
    public void tick() {
        if (nameField != null) {
            nameField.tick();
            descriptionField.tick();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button, int leftPos, int topPos, double scaleX, double scaleY) {
        // Check text fields
        if (nameField != null) {
            nameField.mouseClicked(mouseX, mouseY, button);
            descriptionField.mouseClicked(mouseX, mouseY, button);
        }

        // Check toggle
        if (openToJoinToggle != null) {
            openToJoinToggle.mouseClicked(mouseX, mouseY, button);
        }

        // Check buttons
        for (StyledButton btn : actionButtons) {
            if (btn.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers, int leftPos, int topPos, double scaleX, double scaleY) {
        if (nameField != null) {
            if (nameField.keyPressed(keyCode, scanCode, modifiers)) return true;
            if (descriptionField.keyPressed(keyCode, scanCode, modifiers)) return true;
        }
        return false;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers, int leftPos, int topPos, double scaleX, double scaleY) {
        if (nameField != null) {
            if (nameField.charTyped(codePoint, modifiers)) return true;
            if (descriptionField.charTyped(codePoint, modifiers)) return true;
        }
        return false;
    }
}
