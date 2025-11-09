package fr.eriniumgroup.erinium_faction.client.gui.audit;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.eriniumgroup.erinium_faction.common.network.packets.AuditQueryRequestPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class AuditViewerScreen extends Screen {
    private EditBox playerBox, blockBox, eventBox, fromBox, toBox, limitBox;
    private EditBox queryBox;
    private ResultsList resultsList;
    private int offset = 0;

    private static final List<String> INCOMING_BUFFER = new ArrayList<>();
    private final List<String> allResults = new ArrayList<>();

    public AuditViewerScreen() { super(Component.translatable("erinium_faction.gui.audit.title")); }

    @Override
    protected void init() {
        int x = this.width / 2 - 180;
        int y = this.height / 2 - 130;
        int w = 120;
        int h = 20;

        playerBox = addRenderableWidget(new EditBox(font, x, y, w, h, Component.translatable("erinium_faction.gui.audit.player_uuid")));
        blockBox = addRenderableWidget(new EditBox(font, x + 130, y, w, h, Component.translatable("erinium_faction.gui.audit.block_id")));
        eventBox = addRenderableWidget(new EditBox(font, x + 260, y, w, h, Component.translatable("erinium_faction.gui.audit.event")));

        fromBox = addRenderableWidget(new EditBox(font, x, y + 25, w, h, Component.translatable("erinium_faction.gui.audit.from")));
        toBox = addRenderableWidget(new EditBox(font, x + 130, y + 25, w, h, Component.translatable("erinium_faction.gui.audit.to")));
        limitBox = addRenderableWidget(new EditBox(font, x + 260, y + 25, w, h, Component.translatable("erinium_faction.gui.audit.limit")));
        limitBox.setValue("50");

        addRenderableWidget(Button.builder(Component.translatable("erinium_faction.gui.audit.search"), btn -> { offset = 0; doSearch(); }).bounds(x, y + 50, 80, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("erinium_faction.gui.audit.clear"), btn -> clearResults()).bounds(x + 90, y + 50, 80, 20).build());
        addRenderableWidget(Button.builder(Component.literal("<"), btn -> { offset = Math.max(0, offset - getPageSize()); doSearch(); }).bounds(x + 180, y + 50, 20, 20).build());
        addRenderableWidget(Button.builder(Component.literal(">"), btn -> { offset = offset + getPageSize(); doSearch(); }).bounds(x + 205, y + 50, 20, 20).build());

        // Barre de recherche (filtre client)
        queryBox = addRenderableWidget(new EditBox(font, x, y + 75, 360, 20, Component.translatable("erinium_faction.gui.audit.query")));
        queryBox.setResponder(s -> applyFilter());

        resultsList = addRenderableWidget(new ResultsList(Minecraft.getInstance(), this.width, this.height, y + 100, this.height - 30));

        if (!INCOMING_BUFFER.isEmpty()) {
            var copy = List.copyOf(INCOMING_BUFFER);
            INCOMING_BUFFER.clear();
            pushResults(copy);
        }
    }

    private int getPageSize() { return parseInt(limitBox.getValue(), 50); }

    private void doSearch() {
        String p = emptyToNull(playerBox.getValue());
        String b = emptyToNull(blockBox.getValue());
        String e = emptyToNull(eventBox.getValue());
        long from = parseInstantEpochMs(fromBox.getValue());
        long to = parseInstantEpochMs(toBox.getValue());
        int limit = getPageSize();
        PacketDistributor.sendToServer(new AuditQueryRequestPacket(p, b, e, from, to, limit, offset));
    }

    private static String emptyToNull(String s) { return s == null || s.isBlank() ? null : s.trim(); }
    private static int parseInt(String s, int def) { try { return Integer.parseInt(s.trim()); } catch (Exception e) { return def; } }
    private static long parseInstantEpochMs(String s) { try { return Instant.parse(s.trim()).toEpochMilli(); } catch (Exception e) { return 0L; } }

    private void clearResults() {
        allResults.clear();
        resultsList.clear();
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, this.width, this.height, 0xB0000000);
        super.render(g, mouseX, mouseY, partialTick);
        g.drawString(font, title, this.width / 2 - font.width(title) / 2, 12, 0xFFFFFF);
        g.drawString(font, Component.literal("Offset: " + offset), this.width / 2 + 100, 12, 0xAAAAAA);
    }

    public static void pushResults(List<String> jsonLines) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof AuditViewerScreen scr) {
            scr.allResults.clear();
            scr.allResults.addAll(jsonLines);
            scr.applyFilter();
        } else {
            INCOMING_BUFFER.addAll(jsonLines);
        }
    }

    private void applyFilter() {
        String q = queryBox == null ? null : queryBox.getValue();
        String needle = (q == null ? "" : q.trim().toLowerCase());
        resultsList.clear();
        if (needle.isEmpty()) {
            for (String line : allResults) resultsList.add(line);
        } else {
            for (String line : allResults) {
                if (line.toLowerCase().contains(needle)) resultsList.add(line);
            }
        }
    }

    private static class ResultsList extends ObjectSelectionList<ResultsList.Entry> {
        public ResultsList(Minecraft mc, int width, int height, int top, int bottom) { super(mc, width, height, top, bottom); }
        public void add(String jsonLine) {
            try { this.addEntry(new Entry(JsonParser.parseString(jsonLine).getAsJsonObject())); } catch (Exception ignored) {}
        }
        public void clear() { this.clearEntries(); }

        public static class Entry extends ObjectSelectionList.Entry<Entry> {
            private final JsonObject obj;
            public Entry(JsonObject obj) { this.obj = obj; }
            @Override
            public void render(GuiGraphics g, int idx, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTick) {
                String ts = obj.has("ts") ? obj.get("ts").getAsString() : "?";
                String ev = obj.has("event") ? obj.get("event").getAsString() : "?";
                String who = obj.has("player") && obj.get("player").getAsJsonObject().has("name") ? obj.get("player").getAsJsonObject().get("name").getAsString() : "-";
                g.drawString(Minecraft.getInstance().font, ts + "  •  " + ev + "  •  " + who, left + 4, top + 2, 0xFFD2D2D2);
                String dim = obj.has("dim") ? obj.get("dim").getAsString() : "";
                String pos = obj.has("pos") ? obj.get("pos").toString() : "";
                String blk = obj.has("block") ? obj.get("block").getAsString() : "";
                g.drawString(Minecraft.getInstance().font, (blk.isEmpty()?"":"["+blk+"] ") + pos + (dim.isEmpty()?"":" @"+dim), left + 6, top + 10, 0xFF8CA0B3);
            }
            @Override public boolean mouseClicked(double mx, double my, int b) {
                if (b == 0) {
                    Minecraft mc = Minecraft.getInstance();
                    mc.keyboardHandler.setClipboard(obj.toString());
                    mc.getToasts().addToast(new SystemToast(SystemToast.SystemToastId.PERIODIC_NOTIFICATION, Component.translatable("erinium_faction.gui.audit.copied"), Component.literal("")));
                    return true;
                }
                return false;
            }
            @Override public @NotNull Component getNarration() { return Component.empty(); }
        }

        @Override
        protected int getScrollbarPosition() { return super.getScrollbarPosition() + 30; }
        @Override
        public int getRowWidth() { return width - 60; }
    }
}
