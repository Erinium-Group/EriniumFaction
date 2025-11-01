package fr.eriniumgroup.erinium_faction.gui.screens;

import fr.eriniumgroup.erinium_faction.common.network.packets.TopLuckSyncMessage;
import fr.eriniumgroup.erinium_faction.gui.screens.components.ScrollList;
import fr.eriniumgroup.erinium_faction.gui.screens.components.StyledButton;
import fr.eriniumgroup.erinium_faction.gui.screens.components.TextHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TopLuckScreen extends Screen {
    private final TopLuckSyncMessage data;

    private static final int BASE_W = 320;
    private static final int BASE_H = 220;
    private double scaleX = 1.0, scaleY = 1.0;
    private int leftPos, topPos, imageWidth, imageHeight;

    private ScrollList<TopLuckSyncMessage.CategoryEntry> catList;
    private ScrollList<TopLuckSyncMessage.BlockEntry> blockList;
    private final List<StyledButton> buttons = new ArrayList<>();
    private final Map<String, ItemStack> blockIconCache = new HashMap<>();
    private final Map<String, ItemStack> categoryIconCache = new HashMap<>();

    private Double prevScreenEffectScale = null;

    public TopLuckScreen(TopLuckSyncMessage data) {
        super(Component.literal("TopLuck"));
        this.data = data;
    }

    @Override
    protected void init() {
        super.init();
        // Désactiver le blur global pendant l’ouverture
        try {
            var opts = Minecraft.getInstance().options;
            var optInst = opts.screenEffectScale();
            prevScreenEffectScale = optInst.get();
            optInst.set(0.0);
        } catch (Throwable ignored) {}
        recomputeLayout();
        Font font = Minecraft.getInstance().font;
        if (catList == null) catList = new ScrollList<>(font, this::renderCategory, sh(18));
        if (blockList == null) blockList = new ScrollList<>(font, this::renderBlock, sh(18));
        // Désactiver background texturé pour éviter l’effet flou
        catList.setTexturedBackground(false);
        blockList.setTexturedBackground(false);
        catList.setBounds(sx(12), sy(34), sw(140), sh(150));
        blockList.setBounds(sx(160), sy(34), sw(148), sh(150));
        catList.setItems(data.categories());
        blockList.setItems(data.blocks());
        // Si aucune catégorie, élargir la liste des blocs
        if (data.categories().isEmpty()) {
            blockList.setBounds(sx(12), sy(34), sw(296), sh(150));
        }

        buttons.clear();
        StyledButton close = new StyledButton(font, Component.translatable("gui.done").getString(), this::onClose);
        close.setBounds(sx(120), sy(190), sw(80), sh(16));
        buttons.add(close);
    }

    private void recomputeLayout() {
        int availW = this.width - 40;
        int availH = this.height - 40;
        double scale = Math.min(availW / (double) BASE_W, availH / (double) BASE_H);
        scale = Math.max(1.0, Math.min(scale, 2.0));
        imageWidth = (int) Math.round(BASE_W * scale);
        imageHeight = (int) Math.round(BASE_H * scale);
        scaleX = scaleY = scale;
        leftPos = (this.width - imageWidth) / 2;
        topPos = (this.height - imageHeight) / 2;
    }

    private int sx(int base) { return leftPos + (int)Math.round(base * scaleX); }
    private int sy(int base) { return topPos + (int)Math.round(base * scaleY); }
    private int sw(int base) { return (int)Math.round(base * scaleX); }
    private int sh(int base) { return (int)Math.round(base * scaleY); }

    private void renderCategory(GuiGraphics g, TopLuckSyncMessage.CategoryEntry c, int x, int y, int w, int h, boolean hovered, Font font, int mouseX, int mouseY) {
        if (hovered) g.fill(x, y, x+w, y+h, 0x402a2a3e);
        // Icône de catégorie (16x16)
        ItemStack icon = resolveCategoryIcon(c.icon());
        g.renderItem(icon, x + 2, y + Math.max(0, (h - 16) / 2));

        int nameX = x + 2 + 18;
        int nameW = w - 18 - 6;
        // Label
        TextHelper.drawScaledText(g, font, c.label(), nameX, y + 2, nameW, 0xFFa0a0c0, false);
        // Valeur: count et pourcentage + poids
        String value = String.format("%d (%.1f%%) w=%.1f", c.count(), c.ratio()*100.0, c.weight());
        // Aligné à droite
        int vw = font.width(value);
        int vx = x + w - vw - 4;
        g.drawString(font, value, Math.max(nameX, vx), y + 12, 0xFFffffff, false);
    }

    private void renderBlock(GuiGraphics g, TopLuckSyncMessage.BlockEntry b, int x, int y, int w, int h, boolean hovered, Font font, int mouseX, int mouseY) {
        // Couleur selon le ratio (0% -> gris, 10%+ -> vif). Clampé à 0..1 sur une échelle logique (par ex. 0..0.1)
        double r = Math.min(1.0, Math.max(0.0, b.ratio() * 10.0));
        boolean nether = isNetherFamily(b.id());
        int color = lerpColor(0xFFb8b8d0, nether ? 0xFFF59E0B : 0xFF00D2FF, r);
        if (hovered) g.fill(x, y, x+w, y+h, (color & 0x00FFFFFF) | 0x20111111);
        int barW = (int) Math.round((w - 4) * r);
        if (barW > 0) g.fill(x + 2, y + h - 3, x + 2 + barW, y + h - 2, (color & 0xFFFFFF) | 0x80000000);

        int iconX = x + 2;
        int iconY = y + Math.max(0, (h - 16) / 2);
        ItemStack stack = resolveBlockItem(b.id());
        g.renderItem(stack, iconX, iconY);

        int nameX = iconX + 18;
        int nameW = w - 18 - 6;
        String displayName = stack.getHoverName().getString();
        TextHelper.drawScaledText(g, font, displayName, nameX, y + 2, nameW, 0xFFe0e0ff, false);

        String val = String.format("%.1f%% vs %,d", b.ratio()*100.0, b.baselineCount());
        int vw = font.width(val);
        int vx = x + w - vw - 4;
        g.drawString(font, val, Math.max(nameX, vx), y + 12, color, false);
    }

    private boolean isNetherFamily(String id) {
        return id != null && (id.contains(":nether_") || id.contains("ancient_debris"));
    }

    private int lerpColor(int from, int to, double t) {
        t = Math.max(0.0, Math.min(1.0, t));
        int a1 = (from >>> 24) & 0xFF, r1 = (from >>> 16) & 0xFF, g1 = (from >>> 8) & 0xFF, b1 = from & 0xFF;
        int a2 = (to >>> 24) & 0xFF, r2 = (to >>> 16) & 0xFF, g2 = (to >>> 8) & 0xFF, b2 = to & 0xFF;
        int a = (int) Math.round(a1 + (a2 - a1) * t);
        int rC = (int) Math.round(r1 + (r2 - r1) * t);
        int gC = (int) Math.round(g1 + (g2 - g1) * t);
        int bC = (int) Math.round(b1 + (b2 - b1) * t);
        return (a << 24) | (rC << 16) | (gC << 8) | bC;
    }

    private ItemStack resolveCategoryIcon(String iconId) {
        if (iconId == null || iconId.isEmpty()) return new ItemStack(Items.BOOK);
        ItemStack cached = categoryIconCache.get(iconId);
        if (cached != null) return cached;
        try {
            ResourceLocation rl = ResourceLocation.parse(iconId);
            // Essayer d'abord un item direct
            Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(rl);
            if (item != Items.AIR) {
                ItemStack st = new ItemStack(item);
                categoryIconCache.put(iconId, st);
                return st;
            }
            // Sinon essayer un block -> item
            Block b = BuiltInRegistries.BLOCK.get(rl);
            if (b != null) {
                Item it = b.asItem();
                if (it != Items.AIR) {
                    ItemStack st = new ItemStack(it);
                    categoryIconCache.put(iconId, st);
                    return st;
                }
            }
        } catch (Exception ignored) {}
        ItemStack fallback = new ItemStack(Items.BOOK);
        categoryIconCache.put(iconId, fallback);
        return fallback;
    }

    private ItemStack resolveBlockItem(String blockId) {
        if (blockId == null || blockId.isEmpty()) return new ItemStack(Items.BARRIER);
        ItemStack cached = blockIconCache.get(blockId);
        if (cached != null) return cached;
        try {
            ResourceLocation rl = ResourceLocation.parse(blockId);
            Block b = BuiltInRegistries.BLOCK.get(rl);
            if (b != null) {
                Item it = b.asItem();
                if (it != Items.AIR) {
                    ItemStack st = new ItemStack(it);
                    blockIconCache.put(blockId, st);
                    return st;
                }
            }
        } catch (Exception ignored) {}
        ItemStack fallback = new ItemStack(Items.BARRIER);
        blockIconCache.put(blockId, fallback);
        return fallback;
    }

    @Override
    public void removed() {
        super.removed();
        // Restaurer la valeur de blur globale
        try {
            if (prevScreenEffectScale != null) {
                Minecraft.getInstance().options.screenEffectScale().set(prevScreenEffectScale);
                prevScreenEffectScale = null;
            }
        } catch (Throwable ignored) {}
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
        g.fill(0, 0, this.width, this.height, 0xFF000000);
        int panelColor = 0xFF212130;
        g.fill(leftPos, topPos, leftPos+imageWidth, topPos+imageHeight, panelColor);
        String title = Component.translatable("erinium_faction.gui.topluck.title").getString();
        if (data.target() != null && !data.target().isEmpty()) title += " - " + data.target();
        g.drawString(this.font, title, sx(12), sy(12), 0xFFFFFFFF);
        g.drawString(this.font, Component.translatable("erinium_faction.gui.topluck.total", data.total()).getString(), sx(12), sy(24), 0xFFa0a0c0);
        if (catList != null) catList.render(g, mouseX, mouseY);
        if (blockList != null) blockList.render(g, mouseX, mouseY);
        for (var b : buttons) b.render(g, mouseX, mouseY);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (var b : buttons) if (b.mouseClicked(mouseX, mouseY, button)) return true;
        if (catList != null && catList.mouseClicked(mouseX, mouseY, button)) return true;
        if (blockList != null && blockList.mouseClicked(mouseX, mouseY, button)) return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (catList != null && catList.mouseReleased(mouseX, mouseY, button)) return true;
        if (blockList != null && blockList.mouseReleased(mouseX, mouseY, button)) return true;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (catList != null && catList.mouseDragged(mouseX, mouseY, button, dragX, dragY)) return true;
        if (blockList != null && blockList.mouseDragged(mouseX, mouseY, button, dragX, dragY)) return true;
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (catList != null && catList.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) return true;
        if (blockList != null && blockList.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) return true;
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
}

