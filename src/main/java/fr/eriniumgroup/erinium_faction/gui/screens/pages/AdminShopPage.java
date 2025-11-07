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
    private double currentScaleY = 1.0; // Pour passer le scale à renderShopItem

    // Textures pour les shop items
    private static final ResourceLocation SHOP_ITEM_NORMAL = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/shop/shop-item-normal.png");
    private static final ResourceLocation SHOP_ITEM_HOVER = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/shop/shop-item-hover.png");

    // Textures pour les boutons purchase
    private static final ResourceLocation BUTTON_PURCHASE_NORMAL = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/shop/button-purchase-normal.png");
    private static final ResourceLocation BUTTON_PURCHASE_HOVER = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/shop/button-purchase-hover.png");

    private static class ShopItem {
        String id; // ID unique pour l'achat
        String name;
        int price;
        int requiredLevel;
        String description;
        boolean purchased; // Si l'item est déjà acheté (pour les achats uniques)

        ShopItem(String id, String name, int price, int requiredLevel, String description) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.requiredLevel = requiredLevel;
            this.description = description;
            this.purchased = false;
        }

        ShopItem setPurchased(boolean purchased) {
            this.purchased = purchased;
            return this;
        }
    }

    public AdminShopPage(Font font) {
        super(font);
    }

    private void initComponents(int leftPos, int topPos, double scaleX, double scaleY) {
        if (shopScrollList == null) {
            shopScrollList = new ScrollList<>(font, this::renderShopItem, sh(52, scaleY));

            List<ShopItem> items = new ArrayList<>();

            // Fonctionnalité Bannière Custom (achat unique)
            var data = getFactionData();
            boolean hasBanner = data != null && data.hasCustomBanner;
            items.add(new ShopItem("custom_banner", "Bannière Custom", 50000, 1,
                "Permet de créer une bannière personnalisée 64x32 pixels pour votre faction. Achat unique.")
                .setPurchased(hasBanner));

            // Placeholder items
            items.add(new ShopItem("placeholder_1", "{{SHOP_ITEM_1_NAME}}", 1000, 1, "{{SHOP_ITEM_1_DESC}}"));
            items.add(new ShopItem("placeholder_2", "{{SHOP_ITEM_2_NAME}}", 2500, 3, "{{SHOP_ITEM_2_DESC}}"));
            items.add(new ShopItem("placeholder_3", "{{SHOP_ITEM_3_NAME}}", 5000, 5, "{{SHOP_ITEM_3_DESC}}"));
            items.add(new ShopItem("placeholder_4", "{{SHOP_ITEM_4_NAME}}", 10000, 8, "{{SHOP_ITEM_4_DESC}}"));

            // Examples with varying prices and levels
            items.add(new ShopItem("iron_64", "Iron Ingot x64", 500, 1, "Basic resource pack"));
            items.add(new ShopItem("diamond_16", "Diamond x16", 2000, 3, "Precious gems"));
            items.add(new ShopItem("netherite_4", "Netherite Ingot x4", 8000, 5, "Rare upgrade material"));
            items.add(new ShopItem("book_mending", "Enchanted Book (Mending)", 15000, 7, "Rare enchantment"));
            items.add(new ShopItem("totem", "Totem of Undying", 20000, 8, "Life-saving item"));
            items.add(new ShopItem("elytra", "Elytra", 30000, 10, "Wings of flight"));
            items.add(new ShopItem("beacon", "Beacon", 40000, 12, "Power beacon"));
            items.add(new ShopItem("dragon_egg", "Dragon Egg", 100000, 15, "Ultimate trophy"));

            shopScrollList.setItems(items);
            shopScrollList.setOnItemClick(item -> {
                if (item.purchased) {
                    EFC.log.info("§6Shop", "§cItem already purchased: §e{}", item.name);
                    return;
                }
                EFC.log.info("§6Shop", "§aAttempting to purchase §e{} §afor §e${} §a(requires level §e{}§a)", item.name, item.price, item.requiredLevel);
                // Envoyer le paquet d'achat
                net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                    new fr.eriniumgroup.erinium_faction.common.network.packets.ShopPurchasePacket(item.id)
                );
            });
        }

        int x = sx(CONTENT_X, leftPos, scaleX);
        int y = sy(CONTENT_Y, topPos, scaleY);
        int w = sw(CONTENT_W, scaleX);
        int h = sh(CONTENT_H, scaleY);

        shopScrollList.setBounds(x, y + sh(27, scaleY), w, h - sh(27, scaleY));
    }

    private void renderShopItem(GuiGraphics g, ShopItem item, int x, int y, int width, int height, boolean hovered, Font font, int mouseX, int mouseY) {
        // Utiliser les images avec le scaling fourni par ScrollList (width et height sont déjà scalés)
        ResourceLocation itemTexture = hovered ? SHOP_ITEM_HOVER : SHOP_ITEM_NORMAL;
        ImageRenderer.renderScaledImage(g, itemTexture, x, y, width, height);

        // Calculer le scaling factor basé sur la hauteur
        // height vient de ScrollList qui utilise sh(52, scaleY), donc height/52 = scaleY
        double scale = height / 52.0;

        // Note: L'espace pour l'item icon est réservé mais non rendu
        // Utiliser g.renderItem() ici plus tard pour afficher un vrai item Minecraft
        // Position de l'item: x + (int) Math.round(7 * scale), y + (height - 32) / 2

        // Calculate max width for text (account for icon, margins, and buy button)
        int buyTextWidth = font.width(translate("erinium_faction.gui.shop.button.buy"));
        int maxTextWidth = width - (int) Math.round(49 * scale) - (int) Math.round(16 * scale) - buyTextWidth - 10;

        // Toutes les positions doivent être scalées proportionnellement
        int textStartX = x + (int) Math.round(49 * scale);

        // Item name with scaling (keep for fitting)
        TextHelper.drawScaledText(g, font, item.name, textStartX, y + (int) Math.round(6 * scale), maxTextWidth, 0xFFffffff, true);

        // Description with auto-scroll on hover
        int descY = y + (int) Math.round(15 * scale);
        boolean descHovered = TextHelper.isPointInBounds(mouseX, mouseY, textStartX, descY, maxTextWidth, font.lineHeight);
        TextHelper.drawAutoScrollingText(g, font, item.description, textStartX, descY, maxTextWidth, 0xFFa0a0c0, false, descHovered, "shop_desc_" + item.name);

        // Price
        String priceText = translate("erinium_faction.gui.shop.price", item.price);
        g.drawString(font, priceText, textStartX, y + (int) Math.round(26 * scale), 0xFFfbbf24, false);

        // Required level
        String levelText = translate("erinium_faction.gui.shop.level_required", item.requiredLevel);
        int levelColor = 0xFF00d2ff;
        g.drawString(font, levelText, textStartX, y + (int) Math.round(35 * scale), levelColor, false);

        // Buy button indicator ou status "Acheté"
        if (item.purchased) {
            String purchasedText = translate("erinium_faction.gui.shop.purchased");
            int purchasedX = x + width - font.width(purchasedText) - (int) Math.round(12 * scale);
            g.drawString(font, purchasedText, purchasedX, y + height / 2 - 4, 0xFF10b981, false);
        } else {
            String buyText = translate("erinium_faction.gui.shop.button.buy");
            int buyX = x + width - buyTextWidth - (int) Math.round(16 * scale);
            g.drawString(font, buyText, buyX, y + height / 2 - 4, hovered ? 0xFF10b981 : 0xFF6a6a7e, false);
        }
    }

    @Override
    public void render(GuiGraphics g, int leftPos, int topPos, double scaleX, double scaleY, int mouseX, int mouseY) {
        currentScaleY = scaleY; // Stocker pour utilisation dans renderShopItem
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
