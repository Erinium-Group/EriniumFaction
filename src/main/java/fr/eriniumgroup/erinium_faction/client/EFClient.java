package fr.eriniumgroup.erinium_faction.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import fr.eriniumgroup.erinium_faction.client.overlay.FactionTitleOverlay;
import fr.eriniumgroup.erinium_faction.client.overlay.MinimapOverlayRenderer;
import fr.eriniumgroup.erinium_faction.common.config.EFClientConfig;
import fr.eriniumgroup.erinium_faction.common.network.packets.ClaimsMapDataMessage;
import fr.eriniumgroup.erinium_faction.core.EFC;
import fr.eriniumgroup.erinium_faction.gui.MinimapOverlayConfig;
import fr.eriniumgroup.erinium_faction.gui.screens.FactionMapScreen;
import fr.eriniumgroup.erinium_faction.gui.screens.MinimapFullscreenScreen;
import fr.eriniumgroup.erinium_faction.gui.screens.TitaniumCompressorScreen;
import fr.eriniumgroup.erinium_faction.init.EFMenus;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

import java.util.Locale;

@EventBusSubscriber(modid = EFC.MODID, value = Dist.CLIENT)
public class EFClient {
    public static KeyMapping OPEN_MAP;
    public static KeyMapping MINIMAP_ZOOM_IN;
    public static KeyMapping MINIMAP_ZOOM_OUT;

    // Minimap overlay
    private static MinimapOverlayConfig minimapConfig;
    private static MinimapOverlayRenderer minimapRenderer;
    private static long lastMinimapDataRequest = 0;
    private static final long MINIMAP_REQUEST_INTERVAL_MS = 1000; // Demander toutes les 1 secondes

    // Zone cliquable du bouton HUD courant
    private static int btnX, btnY, btnS;
    private static boolean mouseOver;


    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent e) {
        // Charger la position de la minimap
        e.enqueueWork(MinimapConfig::loadPosition);
    }

    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent e) {
        // Enregistrer l’écran de la machine Titanium Compressor via l’event dédié (1.21+)
        e.register(EFMenus.TITANIUM_COMPRESSOR_MENU.get(), TitaniumCompressorScreen::new);
    }

    @SubscribeEvent
    public static void onRegisterKeys(RegisterKeyMappingsEvent e) {
        InputConstants.Type type = InputConstants.Type.KEYSYM;
        int code = resolveDefaultKeyFromConfig();
        OPEN_MAP = new KeyMapping("key.erinium_faction.map", type, code, "key.categories.erinium_faction");
        MINIMAP_ZOOM_IN = new KeyMapping("key.erinium_faction.minimap_zoom_in", type, InputConstants.KEY_EQUALS, "key.categories.erinium_faction");
        MINIMAP_ZOOM_OUT = new KeyMapping("key.erinium_faction.minimap_zoom_out", type, InputConstants.KEY_MINUS, "key.categories.erinium_faction");
        e.register(OPEN_MAP);
        e.register(MINIMAP_ZOOM_IN);
        e.register(MINIMAP_ZOOM_OUT);

        // Initialiser la minimap
        minimapConfig = MinimapOverlayConfig.load();
        minimapRenderer = new MinimapOverlayRenderer(minimapConfig);
    }

    private static int resolveDefaultKeyFromConfig() {
        String conf = EFClientConfig.MAP_DEFAULT_KEY.get();
        if (conf == null || conf.isBlank()) return InputConstants.KEY_M;
        String s = conf.trim();
        try {
            String id = s.toLowerCase(Locale.ROOT);
            if (id.startsWith("key.")) {
                InputConstants.Key k = InputConstants.getKey(id);
                return k != null ? k.getValue() : InputConstants.KEY_M;
            }
            if (s.length() == 1) {
                char c = Character.toLowerCase(s.charAt(0));
                InputConstants.Key k = InputConstants.getKey("key.keyboard." + c);
                return k != null ? k.getValue() : InputConstants.KEY_M;
            }
            InputConstants.Key k = InputConstants.getKey("key.keyboard." + id);
            return k != null ? k.getValue() : InputConstants.KEY_M;
        } catch (Throwable t) {
            return InputConstants.KEY_M;
        }
    }

    private static boolean allowKeyOpen() {
        String mode = EFClientConfig.MAP_OPEN_CONTROL.get();
        return "KEY".equalsIgnoreCase(mode) || "BOTH".equalsIgnoreCase(mode);
    }

    private static boolean allowButtonOpen() {
        String mode = EFClientConfig.MAP_OPEN_CONTROL.get();
        return "BUTTON".equalsIgnoreCase(mode) || "BOTH".equalsIgnoreCase(mode);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post e) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (allowKeyOpen() && OPEN_MAP != null && mc.screen == null && OPEN_MAP.consumeClick()) {
            mc.setScreen(new FactionMapScreen());
        }

        // Gérer les keybinds de zoom de la minimap
        if (minimapConfig != null && minimapConfig.enabled && mc.screen == null) {
            if (MINIMAP_ZOOM_IN != null && MINIMAP_ZOOM_IN.consumeClick()) {
                minimapConfig.cellSize = Math.min(16, minimapConfig.cellSize + 1);
                minimapConfig.save();
            }
            if (MINIMAP_ZOOM_OUT != null && MINIMAP_ZOOM_OUT.consumeClick()) {
                minimapConfig.cellSize = Math.max(2, minimapConfig.cellSize - 1);
                minimapConfig.save();
            }

            // Demander les données de la minimap au serveur régulièrement
            long now = System.currentTimeMillis();
            if (now - lastMinimapDataRequest >= MINIMAP_REQUEST_INTERVAL_MS) {
                lastMinimapDataRequest = now;
                int chunkX = mc.player.chunkPosition().x;
                int chunkZ = mc.player.chunkPosition().z;
                String dim = mc.player.level().dimension().location().toString();
                int radius = 16; // Rayon fixe pour la minimap

                fr.eriniumgroup.erinium_faction.common.network.packets.ClaimsMapRequestMessage req =
                    new fr.eriniumgroup.erinium_faction.common.network.packets.ClaimsMapRequestMessage(dim, chunkX, chunkZ, radius);
                net.neoforged.neoforge.network.PacketDistributor.sendToServer(req);
            }
        }
    }

    private static boolean isDebugOverlayActive(Minecraft mc) {
        try {
            // Tentative 1: options.renderDebug (bool direct)
            var fld = mc.options.getClass().getField("renderDebug");
            Object v = fld.get(mc.options);
            if (v instanceof Boolean b) return b;
            // Certaines versions stockent un OptionInstance<Boolean>
            if (v != null && v.getClass().getName().endsWith("OptionInstance")) {
                try {
                    var mGet = v.getClass().getMethod("get");
                    Object gv = mGet.invoke(v);
                    if (gv instanceof Boolean b2) return b2;
                } catch (Throwable ignored) {
                }
            }
        } catch (Throwable ignored) {
        }
        try {
            // Tentative 2: mc.gui.getDebugOverlay().showDebugScreen()
            var mGetDbg = mc.gui.getClass().getMethod("getDebugOverlay");
            Object dbg = mGetDbg.invoke(mc.gui);
            if (dbg != null) {
                for (String name : new String[]{"showDebugScreen", "isShowing", "shouldShowDebugScreen"}) {
                    try {
                        var m = dbg.getClass().getMethod(name);
                        Object r = m.invoke(dbg);
                        if (r instanceof Boolean b) return b;
                    } catch (Throwable ignored) {
                    }
                }
            }
        } catch (Throwable ignored) {
        }
        // Si non détecté, considérer comme non debug
        return false;
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiEvent.Post e) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return;
        if (mc.player == null) return;

        GuiGraphics g = e.getGuiGraphics();
        int w = mc.getWindow().getGuiScaledWidth();
        int h = mc.getWindow().getGuiScaledHeight();

        // Render minimap overlay (même si settings screen est ouvert)
        boolean isSettingsScreen = mc.screen instanceof fr.eriniumgroup.erinium_faction.gui.screens.MinimapOverlaySettingsScreen;
        if (minimapRenderer != null && minimapConfig != null && minimapConfig.enabled && (mc.screen == null || isSettingsScreen)) {
            minimapRenderer.render(g, w, h);
        }

        // Render map button (seulement si aucun screen)
        if (mc.screen != null) return;
        if (!allowButtonOpen()) return;
        if (EFClientConfig.MAP_BUTTON_HIDE_IN_DEBUG.get() && isDebugOverlayActive(mc)) return;

        int size = Math.max(10, Math.min(64, EFClientConfig.MAP_BUTTON_SIZE.get()));
        int offX = Math.max(0, EFClientConfig.MAP_BUTTON_OFFSET_X.get());
        int offY = Math.max(0, EFClientConfig.MAP_BUTTON_OFFSET_Y.get());
        String anchor = EFClientConfig.MAP_BUTTON_ANCHOR.get();

        int x = 0, y = 0;
        switch (anchor.toUpperCase(Locale.ROOT)) {
            case "TOP_LEFT" -> {
                x = offX;
                y = offY;
            }
            case "TOP_RIGHT" -> {
                x = w - offX - size;
                y = offY;
            }
            case "BOTTOM_LEFT" -> {
                x = offX;
                y = h - offY - size;
            }
            case "BOTTOM_RIGHT" -> {
                x = w - offX - size;
                y = h - offY - size;
            }
            default -> {
                x = w - offX - size;
                y = offY;
            }
        }
        btnX = x;
        btnY = y;
        btnS = size;

        // Tentative d’afficher une texture personnalisée
        String theme = EFClientConfig.MAP_BUTTON_THEME.get();
        boolean dark = switch (theme.toUpperCase(Locale.ROOT)) {
            case "LIGHT" -> false;
            case "DARK" -> true;
            default -> mc.options.darkMojangStudiosBackground().get();
        };
        String rl = dark ? EFClientConfig.MAP_BUTTON_TEXTURE_DARK.get() : EFClientConfig.MAP_BUTTON_TEXTURE_LIGHT.get();
        try {
            ResourceLocation tex = ResourceLocation.parse(rl);
            RenderSystem.enableBlend();
            g.blit(tex, x, y, 0, 0, size, size, size, size);
            RenderSystem.disableBlend();
        } catch (Throwable ex) {
            // Fallback vectoriel si texture manquante
            RenderSystem.enableBlend();
            int bg = 0xAA000000;
            g.fill(x, y, x + size, y + size, bg);
            g.fill(x, y, x + size, y + 1, 0x55FFFFFF);
            g.fill(x, y + size - 1, x + size, y + size, 0x33000000);
            int pad = Math.max(2, size / 8);
            int ix = x + pad, iy = y + pad;
            int iw = size - pad * 2, ih = size - pad * 2;
            g.fill(ix, iy, ix + iw / 3 - 1, iy + ih, 0xFF2ECC71);
            g.fill(ix + iw / 3, iy, ix + 2 * iw / 3 - 1, iy + ih, 0xFFE67E22);
            g.fill(ix + 2 * iw / 3, iy, ix + iw, iy + ih, 0xFF3498DB);
            RenderSystem.disableBlend();
        }

        // Déterminer si la souris survole (pour tooltip optionnel)
        double rawX = mc.mouseHandler.xpos();
        double rawY = mc.mouseHandler.ypos();
        int sw = mc.getWindow().getScreenWidth();
        int sh = mc.getWindow().getScreenHeight();
        int gw = mc.getWindow().getGuiScaledWidth();
        int gh = mc.getWindow().getGuiScaledHeight();
        double mx = rawX * gw / Math.max(1, sw);
        double my = rawY * gh / Math.max(1, sh);
        mouseOver = (mx >= btnX && mx < btnX + btnS && my >= btnY && my < btnY + btnS);

        if (mouseOver && EFClientConfig.MAP_BUTTON_TOOLTIP.get()) {
            g.drawString(mc.font, Component.translatable("key.erinium_faction.map"), x, y - 10, 0xFFFFFF, true);
        }
    }

    @SubscribeEvent
    public static void onMouseButton(InputEvent.MouseButton.Pre e) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) return;
        if (mc.screen != null) return;

        // Convertir les coordonnées curseur -> espace GUI
        double rawX = mc.mouseHandler.xpos();
        double rawY = mc.mouseHandler.ypos();
        int sw = mc.getWindow().getScreenWidth();
        int sh = mc.getWindow().getScreenHeight();
        int gw = mc.getWindow().getGuiScaledWidth();
        int gh = mc.getWindow().getGuiScaledHeight();
        double mx = rawX * gw / Math.max(1, sw);
        double my = rawY * gh / Math.max(1, sh);

        // Map button
        if (allowButtonOpen()) {
            if (EFClientConfig.MAP_BUTTON_HIDE_IN_DEBUG.get() && isDebugOverlayActive(mc)) return;
            if (e.getAction() != 1) return; // GLFW_PRESS
            if (mx >= btnX && mx < btnX + btnS && my >= btnY && my < btnY + btnS) {
                mc.setScreen(new FactionMapScreen());
                e.setCanceled(true);
            }
        }
    }

    // Méthodes publiques pour gérer la minimap overlay
    public static void updateMinimapData(ClaimsMapDataMessage data) {
        if (minimapRenderer != null) {
            minimapRenderer.updateData(data);
        }
    }

    public static MinimapOverlayConfig getMinimapConfig() {
        return minimapConfig;
    }

    public static void reloadMinimapConfig() {
        minimapConfig = MinimapOverlayConfig.load();
        if (minimapRenderer != null) {
            minimapRenderer.setConfig(minimapConfig);
        }
    }

    public static void reloadMinimapColors() {
        if (minimapRenderer != null) {
            minimapRenderer.reloadColors();
        }
    }
}
