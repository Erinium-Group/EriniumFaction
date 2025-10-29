package fr.eriniumgroup.erinium_faction.client.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/**
 * Overlay pour remplacer les barres vanilla (vie, faim, armure) par des barres personnalisées
 */
@EventBusSubscriber(modid = "erinium_faction", value = Dist.CLIENT)
public class HealthBarOverlay {

    private static final ResourceLocation HEALTH_BAR_BACKGROUND = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/barres/health-bar-background.png");
    private static final ResourceLocation HEALTH_BAR_FILL = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/barres/health-bar-fill.png");
    private static final ResourceLocation HUNGER_BAR_BACKGROUND = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/barres/hunger-bar-background.png");
    private static final ResourceLocation HUNGER_BAR_FILL = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/barres/hunger-bar-fill.png");
    private static final ResourceLocation ARMOR_BAR_BACKGROUND = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/barres/armor-bar-background.png");
    private static final ResourceLocation ARMOR_BAR_FILL = ResourceLocation.fromNamespaceAndPath("erinium_faction", "textures/gui/components/barres/armor-bar-fill.png");

    /**
     * Annule le rendu des barres vanilla
     */
    @SubscribeEvent
    public static void onRenderHealthBar(RenderGuiLayerEvent.Pre event) {
        if (event.getName().equals(VanillaGuiLayers.PLAYER_HEALTH)) {
            event.setCanceled(true);
        }
        if (event.getName().equals(VanillaGuiLayers.FOOD_LEVEL)) {
            event.setCanceled(true);
        }
        if (event.getName().equals(VanillaGuiLayers.ARMOR_LEVEL)) {
            event.setCanceled(true);
        }
    }

    /**
     * Rend les nouvelles barres personnalisées (vie, faim, armure)
     */
    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiLayerEvent.Post event) {
        // Render après la hotbar pour être au bon moment
        if (!event.getName().equals(VanillaGuiLayers.HOTBAR)) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui || mc.player.isCreative() || mc.player.isSpectator()) {
            return;
        }

        Player player = mc.player;
        GuiGraphics graphics = event.getGuiGraphics();

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        int barWidth = 90;
        int barHeight = 9;

        RenderSystem.enableBlend();

        // ===== BARRE DE VIE (gauche) =====
        float currentHealth = player.getHealth();
        float maxHealth = player.getMaxHealth();

        int healthX = screenWidth / 2 - 91;
        int healthY = screenHeight - 39;

        graphics.blit(HEALTH_BAR_BACKGROUND, healthX, healthY, 0, 0, barWidth, barHeight, barWidth, barHeight);

        float healthPercentage = Math.max(0, Math.min(1, currentHealth / maxHealth));
        int healthFillWidth = (int) (88 * healthPercentage);

        if (healthFillWidth > 0) {
            graphics.blit(HEALTH_BAR_FILL, healthX + 1, healthY + 1, 0, 0, healthFillWidth, 7, 88, 7);
        }

        String healthText = String.format("%.0f / %.0f", currentHealth, maxHealth);
        int healthTextWidth = mc.font.width(healthText);
        int healthTextX = healthX + (barWidth - healthTextWidth) / 2;
        int healthTextY = healthY + 1;

        graphics.drawString(mc.font, healthText, healthTextX + 1, healthTextY + 1, 0x000000, false);
        graphics.drawString(mc.font, healthText, healthTextX, healthTextY, 0xFFFFFFFF, false);

        // ===== BARRE DE FAIM (droite) =====
        FoodData foodData = player.getFoodData();
        int currentFood = foodData.getFoodLevel();
        int maxFood = 20;

        int hungerX = screenWidth / 2 + 1;
        int hungerY = screenHeight - 39;

        graphics.blit(HUNGER_BAR_BACKGROUND, hungerX, hungerY, 0, 0, barWidth, barHeight, barWidth, barHeight);

        float hungerPercentage = Math.max(0, Math.min(1, (float) currentFood / maxFood));
        int hungerFillWidth = (int) (88 * hungerPercentage);

        if (hungerFillWidth > 0) {
            graphics.blit(HUNGER_BAR_FILL, hungerX + 1, hungerY + 1, 0, 0, hungerFillWidth, 7, 88, 7);
        }

        String hungerText = String.format("%d / %d", currentFood, maxFood);
        int hungerTextWidth = mc.font.width(hungerText);
        int hungerTextX = hungerX + (barWidth - hungerTextWidth) / 2;
        int hungerTextY = hungerY + 1;

        graphics.drawString(mc.font, hungerText, hungerTextX + 1, hungerTextY + 1, 0x000000, false);
        graphics.drawString(mc.font, hungerText, hungerTextX, hungerTextY, 0xFFFFFFFF, false);

        // ===== BARRE D'ARMURE (au-dessus de la vie) =====
        int armorValue = player.getArmorValue();
        int maxArmor = 20;

        if (armorValue > 0) {
            int armorX = screenWidth / 2 - 91;
            int armorY = screenHeight - 49; // 10px au-dessus de la barre de vie

            graphics.blit(ARMOR_BAR_BACKGROUND, armorX, armorY, 0, 0, barWidth, barHeight, barWidth, barHeight);

            float armorPercentage = Math.max(0, Math.min(1, (float) armorValue / maxArmor));
            int armorFillWidth = (int) (88 * armorPercentage);

            if (armorFillWidth > 0) {
                graphics.blit(ARMOR_BAR_FILL, armorX + 1, armorY + 1, 0, 0, armorFillWidth, 7, 88, 7);
            }

            String armorText = String.format("%d / %d", armorValue, maxArmor);
            int armorTextWidth = mc.font.width(armorText);
            int armorTextX = armorX + (barWidth - armorTextWidth) / 2;
            int armorTextY = armorY + 1;

            graphics.drawString(mc.font, armorText, armorTextX + 1, armorTextY + 1, 0x000000, false);
            graphics.drawString(mc.font, armorText, armorTextX, armorTextY, 0xFFFFFFFF, false);
        }

        RenderSystem.disableBlend();
    }
}
