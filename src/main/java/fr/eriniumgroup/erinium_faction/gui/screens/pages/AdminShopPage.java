package fr.eriniumgroup.erinium_faction.gui.screens.pages;

import fr.eriniumgroup.erinium_faction.core.EFC;
import fr.eriniumgroup.erinium_faction.gui.screens.components.ScrollList;
import fr.eriniumgroup.erinium_faction.gui.screens.components.TextHelper;
import fr.eriniumgroup.erinium_faction.gui.screens.components.ImageRenderer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * Page AdminShop - Basée sur adminshop.svg
 * Shop de la faction avec scroll list
 */
public class AdminShopPage extends FactionPage {

    private ScrollList<ShopItem> shopScrollList;

    // Textures pour les shop items
    private static final ResourceLocation SHOP_ITEM_NORMAL = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/shop/shop-item-normal.png");
    private static final ResourceLocation SHOP_ITEM_HOVER = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/shop/shop-item-hover.png");

    // Textures pour les boutons purchase
    private static final ResourceLocation BUTTON_PURCHASE_NORMAL = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/shop/button-purchase-normal.png");
    private static final ResourceLocation BUTTON_PURCHASE_HOVER = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/shop/button-purchase-hover.png");

    private static class ShopItem {
        String name;
        int price;
        int requiredLevel;
        String description;

        ShopItem(String name, int price, int requiredLevel, String description) {
            this.name = name;
            this.price = price;
            this.requiredLevel = requiredLevel;
            this.description = description;
        }
    }

    public AdminShopPage(Font font) {
        super(font);
    }

    private void initComponents(int leftPos, int topPos, double scaleX, double scaleY) {
        if (shopScrollList == null) {
            shopScrollList = new ScrollList<>(font, this::renderShopItem, sh(52, scaleY));

            List<ShopItem> items = new ArrayList<>();

            // Placeholder items
            items.add(new ShopItem("{{SHOP_ITEM_1_NAME}}", 1000, 1, "{{SHOP_ITEM_1_DESC}}"));
            items.add(new ShopItem("{{SHOP_ITEM_2_NAME}}", 2500, 3, "{{SHOP_ITEM_2_DESC}}"));
            items.add(new ShopItem("{{SHOP_ITEM_3_NAME}}", 5000, 5, "{{SHOP_ITEM_3_DESC}}"));
            items.add(new ShopItem("{{SHOP_ITEM_4_NAME}}", 10000, 8, "{{SHOP_ITEM_4_DESC}}"));
            items.add(new ShopItem("{{SHOP_ITEM_5_NAME}}", 25000, 10, "{{SHOP_ITEM_5_DESC}}"));

            // Examples with varying prices and levels
            items.add(new ShopItem("Iron Ingot x64", 500, 1, "Basic resource pack"));
            items.add(new ShopItem("Diamond x16", 2000, 3, "Precious gems"));
            items.add(new ShopItem("Netherite Ingot x4", 8000, 5, "Rare upgrade material"));
            items.add(new ShopItem("Enchanted Book (Mending)", 15000, 7, "Rare enchantment"));
            items.add(new ShopItem("Totem of Undying", 20000, 8, "Life-saving item"));
            items.add(new ShopItem("Elytra", 30000, 10, "Wings of flight"));
            items.add(new ShopItem("Beacon", 40000, 12, "Power beacon"));
            items.add(new ShopItem("Dragon Egg", 100000, 15, "Ultimate trophy"));

            shopScrollList.setItems(items);
            shopScrollList.setOnItemClick(item -> {
                EFC.log.info("§6Shop", "§aAttempting to purchase §e{} §afor §e${} §a(requires level §e{}§a)", item.name, item.price, item.requiredLevel);
            });
        }

        int x = sx(CONTENT_X, leftPos, scaleX);
        int y = sy(CONTENT_Y, topPos, scaleY);
        int w = sw(CONTENT_W, scaleX);
        int h = sh(CONTENT_H, scaleY);

        shopScrollList.setBounds(x, y + sh(27, scaleY), w, h - sh(27, scaleY));
    }

    private void renderShopItem(GuiGraphics g, ShopItem item, int x, int y, int width, int height, boolean hovered, Font font, int mouseX, int mouseY) {
        // Utiliser les images au lieu de g.fill
        ResourceLocation itemTexture = hovered ? SHOP_ITEM_HOVER : SHOP_ITEM_NORMAL;
        ImageRenderer.renderScaledImage(g, itemTexture, x, y, width, height);

        // Item icon area (placeholder purple square)
        int iconSize = sh(32, 1.0);
        g.fill(x + 7, y + 6, x + 7 + iconSize, y + 6 + iconSize, 0x60a855f7);
        g.fill(x + 7, y + 6, x + 7 + iconSize, y + 7, 0xFFa855f7);
        g.fill(x + 7, y + 6, x + 7, y + 6 + iconSize, 0xFFa855f7);

        // Calculate max width for text (account for icon, margins, and buy button)
        int buyTextWidth = font.width(translate("erinium_faction.gui.shop.button.buy"));
        int maxTextWidth = width - 49 - 16 - buyTextWidth - 10;

        // Item name with scaling (keep for fitting)
        TextHelper.drawScaledText(g, font, item.name, x + 49, y + 6, maxTextWidth, 0xFFffffff, true);

        // Description with auto-scroll on hover
        boolean descHovered = TextHelper.isPointInBounds(mouseX, mouseY, x + 49, y + 15, maxTextWidth, font.lineHeight);
        TextHelper.drawAutoScrollingText(g, font, item.description, x + 49, y + 15, maxTextWidth, 0xFFa0a0c0, false, descHovered, "shop_desc_" + item.name);

        // Price
        String priceText = translate("erinium_faction.gui.shop.price", item.price);
        g.drawString(font, priceText, x + 49, y + 26, 0xFFfbbf24, false);

        // Required level
        String levelText = translate("erinium_faction.gui.shop.level_required", item.requiredLevel);
        int levelColor = 0xFF00d2ff;
        g.drawString(font, levelText, x + 49, y + 35, levelColor, false);

        // Buy button indicator
        String buyText = translate("erinium_faction.gui.shop.button.buy");
        g.drawString(font, buyText, x + width - buyTextWidth - 16, y + height / 2 - 4, hovered ? 0xFF10b981 : 0xFF6a6a7e, false);
    }

    @Override
    public void render(GuiGraphics g, int leftPos, int topPos, double scaleX, double scaleY, int mouseX, int mouseY) {
        initComponents(leftPos, topPos, scaleX, scaleY);

        int x = sx(CONTENT_X, leftPos, scaleX);
        int y = sy(CONTENT_Y, topPos, scaleY);
        int w = sw(CONTENT_W, scaleX);

        // Header
        g.fill(x, y, x + w, y + sh(22, scaleY), 0xE61e1e2e);
        g.fill(x, y, x + w, y + 1, 0xFF00d2ff);
        g.drawString(font, translate("erinium_faction.gui.shop.title"), x + sw(9, scaleX), y + sh(9, scaleY), 0xFFffffff, true);

        // Balance display avec vraies données
        var data = getFactionData();
        String balanceText = data != null ? translate("erinium_faction.gui.shop.balance", data.bank) : translate("erinium_faction.gui.shop.balance", 0);
        g.drawString(font, balanceText, x + w - font.width(balanceText) - sw(9, scaleX), y + sh(9, scaleY), 0xFFfbbf24, false);

        shopScrollList.render(g, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button, int leftPos, int topPos, double scaleX, double scaleY) {
        if (shopScrollList == null) return false;
        return shopScrollList.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button, int leftPos, int topPos, double scaleX, double scaleY) {
        if (shopScrollList == null) return false;
        return shopScrollList.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY, int leftPos, int topPos, double scaleX, double scaleY) {
        if (shopScrollList == null) return false;
        return shopScrollList.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY, int leftPos, int topPos, double scaleX, double scaleY) {
        if (shopScrollList == null) return false;
        return shopScrollList.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
}
