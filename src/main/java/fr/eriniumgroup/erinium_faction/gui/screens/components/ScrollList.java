package fr.eriniumgroup.erinium_faction.gui.screens.components;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * ScrollList stylisée pour le GUI de faction
 * Permet de scroller à travers une liste d'éléments
 */
public class ScrollList<T> {
    private final Font font;
    private final List<T> items = new ArrayList<>();
    private final ItemRenderer<T> renderer;

    private int x, y, width, height;
    private int itemHeight;
    private int scrollOffset = 0;
    private int maxScroll = 0;

    private boolean isDragging = false;
    private int dragStartY = 0;
    private int scrollStartOffset = 0;

    private Consumer<T> onItemClick;

    public interface ItemRenderer<T> {
        void render(GuiGraphics g, T item, int x, int y, int width, int height, boolean hovered, Font font);
    }

    public ScrollList(Font font, ItemRenderer<T> renderer, int itemHeight) {
        this.font = font;
        this.renderer = renderer;
        this.itemHeight = itemHeight;
    }

    public void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        updateMaxScroll();
    }

    public void setItems(List<T> items) {
        this.items.clear();
        this.items.addAll(items);
        updateMaxScroll();
    }

    public void addItem(T item) {
        this.items.add(item);
        updateMaxScroll();
    }

    public void clearItems() {
        this.items.clear();
        this.scrollOffset = 0;
        updateMaxScroll();
    }

    public void setOnItemClick(Consumer<T> callback) {
        this.onItemClick = callback;
    }

    public List<T> getItems() {
        return items;
    }

    private void updateMaxScroll() {
        int totalHeight = items.size() * itemHeight;
        maxScroll = Math.max(0, totalHeight - height);
        scrollOffset = Math.min(scrollOffset, maxScroll);
    }

    public void render(GuiGraphics g, int mouseX, int mouseY) {
        // Background
        g.fill(x, y, x + width, y + height, 0xE61e1e2e);
        g.fill(x, y, x + width, y + 1, 0x80667eea);

        // Scissor pour clipping
        g.enableScissor(x, y, x + width, y + height);

        int visibleItems = (int) Math.ceil((double) height / itemHeight) + 1;
        int startIndex = scrollOffset / itemHeight;
        int endIndex = Math.min(items.size(), startIndex + visibleItems);

        for (int i = startIndex; i < endIndex; i++) {
            T item = items.get(i);
            int itemY = y + (i * itemHeight) - scrollOffset;

            boolean hovered = mouseX >= x && mouseX < x + width - 8 &&
                            mouseY >= itemY && mouseY < itemY + itemHeight;

            renderer.render(g, item, x, itemY, width - 8, itemHeight, hovered, font);
        }

        g.disableScissor();

        // Scrollbar
        if (maxScroll > 0) {
            renderScrollbar(g, mouseX, mouseY);
        }
    }

    private void renderScrollbar(GuiGraphics g, int mouseX, int mouseY) {
        int scrollbarX = x + width - 6;
        int scrollbarHeight = height;

        // Scrollbar track
        g.fill(scrollbarX, y, scrollbarX + 6, y + scrollbarHeight, 0x802a2a3e);

        // Scrollbar thumb
        int thumbHeight = Math.max(20, (int)((double)height / (height + maxScroll) * scrollbarHeight));
        int thumbY = y + (int)((double)scrollOffset / maxScroll * (scrollbarHeight - thumbHeight));

        boolean thumbHovered = mouseX >= scrollbarX && mouseX < scrollbarX + 6 &&
                              mouseY >= thumbY && mouseY < thumbY + thumbHeight;

        int thumbColor = isDragging ? 0xFF8b5cf6 : (thumbHovered ? 0xFF667eea : 0xFF4a4a5e);
        g.fill(scrollbarX, thumbY, scrollbarX + 6, thumbY + thumbHeight, thumbColor);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return false;

        // Check scrollbar
        int scrollbarX = x + width - 6;
        if (mouseX >= scrollbarX && mouseX < scrollbarX + 6 && mouseY >= y && mouseY < y + height) {
            isDragging = true;
            dragStartY = (int) mouseY;
            scrollStartOffset = scrollOffset;
            return true;
        }

        // Check item click
        if (mouseX >= x && mouseX < x + width - 8 && mouseY >= y && mouseY < y + height) {
            int relativeY = (int) mouseY - y + scrollOffset;
            int clickedIndex = relativeY / itemHeight;

            if (clickedIndex >= 0 && clickedIndex < items.size() && onItemClick != null) {
                onItemClick.accept(items.get(clickedIndex));
                System.out.println("ScrollList: Item clicked at index " + clickedIndex);
                return true;
            }
        }

        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && isDragging) {
            isDragging = false;
            return true;
        }
        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isDragging && maxScroll > 0) {
            int deltaY = (int) mouseY - dragStartY;
            int scrollbarHeight = height;
            int thumbHeight = Math.max(20, (int)((double)height / (height + maxScroll) * scrollbarHeight));

            double scrollRatio = (double) deltaY / (scrollbarHeight - thumbHeight);
            scrollOffset = (int) (scrollStartOffset + scrollRatio * maxScroll);
            scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset));

            return true;
        }
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height) {
            scrollOffset -= (int) (scrollY * 20);
            scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset));
            return true;
        }
        return false;
    }
}
