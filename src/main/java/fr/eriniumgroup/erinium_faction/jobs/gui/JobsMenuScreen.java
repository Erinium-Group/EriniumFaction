package fr.eriniumgroup.erinium_faction.jobs.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import fr.eriniumgroup.erinium_faction.gui.screens.components.ImageRenderer;
import fr.eriniumgroup.erinium_faction.jobs.JobData;
import fr.eriniumgroup.erinium_faction.jobs.JobType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Écran principal du système de métiers
 * Affiche la liste de tous les métiers avec leur progression
 */
public class JobsMenuScreen extends Screen {

    private static final int GUI_WIDTH = 400;
    private static final int GUI_HEIGHT = 270;

    // Assets
    private static final ResourceLocation BG_FULL = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/jobs/bg-main-full.png");
    private static final ResourceLocation PANEL_HEADER = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/jobs/panel-header-gold.png");
    private static final ResourceLocation PANEL_DARK = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/jobs/panel-dark.png");
    private static final ResourceLocation BUTTON_CLOSE = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/jobs/button-close.png");
    private static final ResourceLocation ICON_PLACEHOLDER = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/jobs/icon-placeholder-64x64.png");
    private static final ResourceLocation PROGRESSBAR_TRACK = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/jobs/progressbar-track-220x8.png");
    private static final ResourceLocation SCROLLBAR_TRACK = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/jobs/scrollbar-track-142.png");
    private static final ResourceLocation SCROLLBAR_THUMB = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/jobs/scrollbar-thumb-gold.png");

    private JobData[] jobs;
    private int leftPos;
    private int topPos;
    private int scrollOffset = 0;
    private boolean isDragging = false;
    private boolean hasDragged = false;
    private int dragStartY = 0;
    private int dragStartOffset = 0;
    private static final int DRAG_THRESHOLD = 3; // Minimum pixels to move before considering it a drag
    private static final int MAX_VISIBLE_JOBS = 2; // Only 2 jobs visible at 72px height each

    public JobsMenuScreen() {
        super(Component.literal("Jobs System"));
        this.jobs = JobData.createExampleData();
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - GUI_WIDTH) / 2;
        this.topPos = (this.height - GUI_HEIGHT) / 2;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);

        // Background + Border
        ImageRenderer.renderScaledImage(g, BG_FULL, leftPos, topPos, GUI_WIDTH, GUI_HEIGHT);

        // Header avec gradient
        ImageRenderer.renderScaledImage(g, PANEL_HEADER, leftPos + 8, topPos + 8, 384, 40);

        // Titre
        g.drawString(font, "JOBS SYSTEM", leftPos + 20, topPos + 20, 0xfbbf24, false);
        g.drawString(font, "Master different professions and unlock unique abilities",
                    leftPos + 20, topPos + 34, 0x9ca3af, false);

        // Close button
        ImageRenderer.renderScaledImage(g, BUTTON_CLOSE, leftPos + 372, topPos + 16, 14, 14);

        // Scroll container
        ImageRenderer.renderScaledImage(g, PANEL_DARK, leftPos + 8, topPos + 54, 384, 208);

        // Render jobs list avec scroll
        renderJobsList(g, mouseX, mouseY);

        // Scrollbar
        renderScrollbar(g);
    }

    private void renderJobsList(GuiGraphics g, int mouseX, int mouseY) {
        int startY = topPos + 60;
        int itemHeight = 72;
        int spacing = 4;

        // Enable scissor pour clipper le contenu qui dépasse
        int scissorX = leftPos + 14;
        int scissorY = topPos + 54;
        int scissorWidth = 372;
        int scissorHeight = 202; // Hauteur de la zone scrollable (208 - 6px margin bottom)

        g.enableScissor(scissorX, scissorY, scissorX + scissorWidth, scissorY + scissorHeight);

        for (int i = 0; i < jobs.length; i++) {
            int yOffset = startY + (i * (itemHeight + spacing)) - scrollOffset;

            // Ne render que si au moins partiellement visible dans la zone scissor
            if (yOffset + itemHeight > scissorY && yOffset < scissorY + scissorHeight) {
                renderJobItem(g, jobs[i], leftPos + 14, yOffset, mouseX, mouseY, scissorX, scissorY, scissorWidth, scissorHeight);
            }
        }

        g.disableScissor();
    }

    private void renderJobItem(GuiGraphics g, JobData job, int x, int y, int mouseX, int mouseY, int scissorX, int scissorY, int scissorWidth, int scissorHeight) {
        JobType type = job.getType();

        // Card background avec bordure colorée
        ResourceLocation cardBg = ResourceLocation.fromNamespaceAndPath("erinium_faction",
            "textures/gui/jobs/listitem-large-" + type.getColorName() + ".png");
        ImageRenderer.renderScaledImage(g, cardBg, x, y, 372, 72);

        // Icon placeholder 64x64
        ImageRenderer.renderScaledImage(g, ICON_PLACEHOLDER, x + 6, y + 4, 64, 64);

        // Emoji au centre de l'icône (temporaire)
        g.drawString(font, type.getEmoji(), x + 38 - font.width(type.getEmoji()) / 2, y + 36, type.getColor(), false);

        // Job name
        g.drawString(font, type.getDisplayName(), x + 78, y + 20, type.getColor(), false);

        // Level
        g.drawString(font, "Level " + job.getLevel(), x + 78, y + 36, 0x9ca3af, false);

        // XP Bar - check if visible in scissor area before rendering
        if (y + 54 > scissorY && y + 46 < scissorY + scissorHeight) {
            // XP Bar track
            ImageRenderer.renderScaledImage(g, PROGRESSBAR_TRACK, x + 78, y + 46, 220, 8);

            // XP Bar fill
            int fillWidth = (int) (220 * job.getExperienceProgress());
            if (fillWidth > 0) {
                ResourceLocation fillTexture = ResourceLocation.fromNamespaceAndPath("erinium_faction",
                    "textures/gui/jobs/progressbar-fill-220x8-" + type.getColorName() + ".png");

                // Temporarily disable main scissor, render fill, then restore
                g.disableScissor();
                g.enableScissor(x + 78, y + 46, x + 78 + fillWidth, y + 54);
                ImageRenderer.renderScaledImage(g, fillTexture, x + 78, y + 46, 220, 8);
                g.disableScissor();
                // Restore main scissor
                g.enableScissor(scissorX, scissorY, scissorX + scissorWidth, scissorY + scissorHeight);
            }

            // XP Text (ajusté pour rester dans les limites - max 372px de largeur)
            String xpText = job.getExperience() + "/" + job.getExperienceToNextLevel() + " XP (" + job.getExperiencePercentage() + "%)";
            int textWidth = font.width(xpText);
            int textX = Math.min(x + 306, x + 372 - textWidth - 4); // 4px de marge
            g.drawString(font, xpText, textX, y + 58, 0x9ca3af, false);
        }
    }

    private void renderScrollbar(GuiGraphics g) {
        int scrollbarX = leftPos + 390;
        int scrollbarY = topPos + 54;

        // Track (utilise toute la hauteur disponible)
        ResourceLocation trackTexture = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/jobs/scrollbar-track-202.png");
        ImageRenderer.renderScaledImage(g, trackTexture, scrollbarX, scrollbarY, 4, 202);

        // Thumb (proportionnel au contenu)
        int totalHeight = jobs.length * 76; // 72 + 4 spacing
        int visibleHeight = 202;
        int thumbHeight = Math.max(20, (visibleHeight * visibleHeight) / totalHeight);
        int maxScroll = Math.max(0, totalHeight - visibleHeight);
        int thumbY = scrollbarY + (maxScroll > 0 ? (scrollOffset * (visibleHeight - thumbHeight)) / maxScroll : 0);

        g.enableScissor(scrollbarX, thumbY, scrollbarX + 4, thumbY + thumbHeight);
        ImageRenderer.renderScaledImage(g, SCROLLBAR_THUMB, scrollbarX, thumbY, 4, thumbHeight);
        g.disableScissor();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int totalHeight = jobs.length * 76;
        int visibleHeight = 202;
        int maxScroll = Math.max(0, totalHeight - visibleHeight);

        scrollOffset -= (int) (scrollY * 20);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));

        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Close button
        if (mouseX >= leftPos + 372 && mouseX <= leftPos + 386 &&
            mouseY >= topPos + 16 && mouseY <= topPos + 30) {
            this.onClose();
            return true;
        }

        // Check if clicking on scrollbar area for immediate dragging
        int scrollbarX = leftPos + 390;
        int scrollbarY = topPos + 54;
        if (mouseX >= scrollbarX && mouseX <= scrollbarX + 4 &&
            mouseY >= scrollbarY && mouseY <= scrollbarY + 202) {
            isDragging = true;
            hasDragged = true; // Scrollbar drag is immediate
            dragStartY = (int) mouseY;
            dragStartOffset = scrollOffset;
            return true;
        }

        // Prepare for potential drag in scroll area (but don't activate yet)
        if (mouseX >= leftPos + 14 && mouseX <= leftPos + 386 &&
            mouseY >= topPos + 54 && mouseY <= topPos + 256) {
            isDragging = true;
            hasDragged = false; // Not dragged yet, might be a click
            dragStartY = (int) mouseY;
            dragStartOffset = scrollOffset;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        // If we didn't actually drag (just clicked), check for job item clicks
        if (isDragging && !hasDragged) {
            int startY = topPos + 60;
            int itemHeight = 72;
            int spacing = 4;

            for (int i = 0; i < jobs.length; i++) {
                int yOffset = startY + (i * (itemHeight + spacing)) - scrollOffset;

                if (mouseX >= leftPos + 14 && mouseX <= leftPos + 386 &&
                    mouseY >= yOffset && mouseY < yOffset + itemHeight &&
                    yOffset + itemHeight >= topPos + 54 && yOffset < topPos + 256) {
                    // Open job detail screen
                    if (minecraft != null) {
                        minecraft.setScreen(new JobDetailScreen(jobs[i]));
                    }
                    isDragging = false;
                    hasDragged = false;
                    return true;
                }
            }
        }

        isDragging = false;
        hasDragged = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isDragging) {
            int deltaY = (int) mouseY - dragStartY;

            // Check if we've moved enough to consider it a drag
            if (!hasDragged && Math.abs(deltaY) > DRAG_THRESHOLD) {
                hasDragged = true;
            }

            // Only scroll if we've confirmed it's a drag
            if (hasDragged) {
                int totalHeight = jobs.length * 76;
                int visibleHeight = 202;
                int maxScroll = Math.max(0, totalHeight - visibleHeight);

                // Appliquer le déplacement
                scrollOffset = dragStartOffset + deltaY;
                scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
            }
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
