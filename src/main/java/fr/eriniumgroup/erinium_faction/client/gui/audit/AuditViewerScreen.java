package fr.eriniumgroup.erinium_faction.client.gui.audit;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.eriniumgroup.erinium_faction.common.network.packets.AuditQueryRequestPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public class AuditViewerScreen extends Screen {
    private EditBox playerBox, blockBox, eventBox, fromBox, toBox, limitBox;
    private EditBox queryBox;
    private ResultsList resultsList;
    private int offset = 0;

    // Mémorise l'ancrage pour dessiner des labels propres
    private int baseX, baseY;

    private static final List<String> INCOMING_BUFFER = new ArrayList<>();
    private final List<String> allResults = new ArrayList<>();

    private enum SortKey { TIMESTAMP, EVENT, PLAYER, BLOCK }
    private SortKey sortKey = SortKey.TIMESTAMP;
    private boolean sortAscending = true;
    private Button sortKeyBtn, sortOrderBtn;

    public AuditViewerScreen() { super(Component.translatable("erinium_faction.gui.audit.title")); }

    @Override
    protected void init() {
        int x = this.width / 2 - 180;
        int y = this.height / 2 - 130;
        int w = 120;
        int h = 20;

        // Nouvelle grille verticale pour éviter tout chevauchement
        int row1Y = y + 10;
        int row2Y = y + 40;
        int btnY = y + 70;
        int queryY = y + 100;
        int sortY = y + 125;
        int listTopY = y + 150;

        this.baseX = x;
        this.baseY = y; // ancre globale si besoin

        playerBox = addRenderableWidget(new EditBox(font, x, row1Y, w, h, Component.translatable("erinium_faction.gui.audit.player_uuid")));
        bindPlaceholder(playerBox, "erinium_faction.gui.audit.player_uuid.suggestion", null);
        playerBox.setTooltip(Tooltip.create(Component.translatable("erinium_faction.gui.audit.player_uuid.tooltip")));

        blockBox = addRenderableWidget(new EditBox(font, x + 130, row1Y, w, h, Component.translatable("erinium_faction.gui.audit.block_id")));
        bindPlaceholder(blockBox, "erinium_faction.gui.audit.block_id.suggestion", null);
        blockBox.setTooltip(Tooltip.create(Component.translatable("erinium_faction.gui.audit.block_id.tooltip")));

        eventBox = addRenderableWidget(new EditBox(font, x + 260, row1Y, w, h, Component.translatable("erinium_faction.gui.audit.event")));
        bindPlaceholder(eventBox, "erinium_faction.gui.audit.event.suggestion", null);
        eventBox.setTooltip(Tooltip.create(Component.translatable("erinium_faction.gui.audit.event.tooltip")));

        fromBox = addRenderableWidget(new EditBox(font, x, row2Y, w, h, Component.translatable("erinium_faction.gui.audit.from")));
        bindPlaceholder(fromBox, "erinium_faction.gui.audit.from.suggestion", null);
        fromBox.setTooltip(Tooltip.create(Component.translatable("erinium_faction.gui.audit.from.tooltip")));

        toBox = addRenderableWidget(new EditBox(font, x + 130, row2Y, w, h, Component.translatable("erinium_faction.gui.audit.to")));
        bindPlaceholder(toBox, "erinium_faction.gui.audit.to.suggestion", null);
        toBox.setTooltip(Tooltip.create(Component.translatable("erinium_faction.gui.audit.to.tooltip")));

        limitBox = addRenderableWidget(new EditBox(font, x + 260, row2Y, w, h, Component.translatable("erinium_faction.gui.audit.limit")));
        limitBox.setValue("50");
        bindPlaceholder(limitBox, "erinium_faction.gui.audit.limit.suggestion", null);
        limitBox.setTooltip(Tooltip.create(Component.translatable("erinium_faction.gui.audit.limit.tooltip")));

        addRenderableWidget(Button.builder(Component.translatable("erinium_faction.gui.audit.search"), btn -> { offset = 0; doSearch(); })
                .bounds(x, btnY, 80, 20).tooltip(Tooltip.create(Component.translatable("erinium_faction.gui.audit.search.tooltip"))).build());
        addRenderableWidget(Button.builder(Component.translatable("erinium_faction.gui.audit.clear"), btn -> clearResults())
                .bounds(x + 90, btnY, 80, 20).tooltip(Tooltip.create(Component.translatable("erinium_faction.gui.audit.clear.tooltip"))).build());
        addRenderableWidget(Button.builder(Component.translatable("erinium_faction.gui.audit.prev"), btn -> { offset = Math.max(0, offset - getPageSize()); doSearch(); })
                .bounds(x + 180, btnY, 20, 20).tooltip(Tooltip.create(Component.translatable("erinium_faction.gui.audit.prev.tooltip"))).build());
        addRenderableWidget(Button.builder(Component.translatable("erinium_faction.gui.audit.next"), btn -> { offset = offset + getPageSize(); doSearch(); })
                .bounds(x + 205, btnY, 20, 20).tooltip(Tooltip.create(Component.translatable("erinium_faction.gui.audit.next.tooltip"))).build());

        // Barre de recherche (filtre client)
        queryBox = addRenderableWidget(new EditBox(font, x, queryY, 360, 20, Component.translatable("erinium_faction.gui.audit.query")));
        bindPlaceholder(queryBox, "erinium_faction.gui.audit.query.suggestion", this::applyFilter);
        queryBox.setTooltip(Tooltip.create(Component.translatable("erinium_faction.gui.audit.query.tooltip")));

        // Contrôles de tri
        sortKeyBtn = addRenderableWidget(Button.builder(Component.literal(""), b -> {
            switch (sortKey) {
                case TIMESTAMP -> sortKey = SortKey.EVENT;
                case EVENT -> sortKey = SortKey.PLAYER;
                case PLAYER -> sortKey = SortKey.BLOCK;
                case BLOCK -> sortKey = SortKey.TIMESTAMP;
            }
            updateSortButtonsLabels();
            applyFilter();
        }).bounds(x, sortY, 160, 20).tooltip(Tooltip.create(Component.translatable("erinium_faction.gui.audit.sort.key.tooltip"))).build());

        sortOrderBtn = addRenderableWidget(Button.builder(Component.literal(""), b -> {
            sortAscending = !sortAscending;
            updateSortButtonsLabels();
            applyFilter();
        }).bounds(x + 170, sortY, 160, 20).tooltip(Tooltip.create(Component.translatable("erinium_faction.gui.audit.sort.order.tooltip"))).build());
        updateSortButtonsLabels();

        resultsList = addRenderableWidget(new ResultsList(Minecraft.getInstance(), this.width, this.height, listTopY, this.height - 36));

        if (!INCOMING_BUFFER.isEmpty()) {
            var copy = List.copyOf(INCOMING_BUFFER);
            INCOMING_BUFFER.clear();
            pushResults(copy);
        }
    }

    private void bindPlaceholder(EditBox box, String translationKey, Runnable onChange) {
        final String suggestion = Component.translatable(translationKey).getString();
        box.setSuggestion(suggestion);
        Consumer<String> hook = s -> {
            box.setSuggestion(s.isEmpty() ? suggestion : "");
            if (onChange != null) onChange.run();
        };
        // Conserver l’éventuel responder existant si besoin
        box.setResponder(hook::accept);
    }

    private void updateSortButtonsLabels() {
        Component keyLabel = switch (sortKey) {
            case TIMESTAMP -> Component.translatable("erinium_faction.gui.audit.sort.key.timestamp");
            case EVENT -> Component.translatable("erinium_faction.gui.audit.sort.key.event");
            case PLAYER -> Component.translatable("erinium_faction.gui.audit.sort.key.player");
            case BLOCK -> Component.translatable("erinium_faction.gui.audit.sort.key.block");
        };
        sortKeyBtn.setMessage(Component.translatable("erinium_faction.gui.audit.sort.key", keyLabel));
        sortOrderBtn.setMessage(Component.translatable("erinium_faction.gui.audit.sort.order", Component.translatable(sortAscending ? "erinium_faction.gui.audit.sort.order.asc" : "erinium_faction.gui.audit.sort.order.desc")));
    }

    private int getPageSize() { return parseInt(limitBox.getValue(), 50); }

    private void doSearch() {
        // Nettoie la liste pour préparer l'affichage de la page demandée
        clearResults();
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
        resultsList.setScrollAmount(0);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, this.width, this.height, 0xB0000000);
        super.render(g, mouseX, mouseY, partialTick);

        // Titre et infos
        g.drawString(font, title, this.width / 2 - font.width(title) / 2, 12, 0xFFFFFF);
        g.drawString(font, Component.translatable("erinium_faction.gui.audit.offset", offset), this.width / 2 + 100, 12, 0xAAAAAA);

        // Libellés positionnés juste au-dessus de chaque rangée
        int labelColor = 0xFFB0B0B0;
        int row1Y = this.height / 2 - 130 + 10;
        int row2Y = this.height / 2 - 130 + 40;
        int queryY = this.height / 2 - 130 + 100;
        int lx = this.width / 2 - 180;
        g.drawString(font, Component.translatable("erinium_faction.gui.audit.player_uuid.label"), lx, row1Y - 10, labelColor);
        g.drawString(font, Component.translatable("erinium_faction.gui.audit.block_id.label"), lx + 130, row1Y - 10, labelColor);
        g.drawString(font, Component.translatable("erinium_faction.gui.audit.event.label"), lx + 260, row1Y - 10, labelColor);
        g.drawString(font, Component.translatable("erinium_faction.gui.audit.from.label"), lx, row2Y - 10, labelColor);
        g.drawString(font, Component.translatable("erinium_faction.gui.audit.to.label"), lx + 130, row2Y - 10, labelColor);
        g.drawString(font, Component.translatable("erinium_faction.gui.audit.limit.label"), lx + 260, row2Y - 10, labelColor);
        g.drawString(font, Component.translatable("erinium_faction.gui.audit.query.label"), lx, queryY - 10, labelColor);

        // En-tête de colonnes de la liste
        int listLeft = this.width / 2 - (resultsList.getRowWidth() / 2);
        int headerY = resultsList.getListTop() - 10;
        g.drawString(font, Component.translatable("erinium_faction.gui.audit.header"), listLeft + 4, headerY, 0xFFCAD4DF);
    }

    public static void pushResults(List<String> jsonLines) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof AuditViewerScreen scr) {
            // On n'efface plus ici: on accumule les entrées reçues (utile si le serveur envoie par paquets)
            scr.allResults.addAll(jsonLines);
            scr.applyFilter();
        } else {
            INCOMING_BUFFER.addAll(jsonLines);
        }
    }

    private void applyFilter() {
        String q = queryBox == null ? null : queryBox.getValue();
        String needle = (q == null ? "" : q.trim().toLowerCase());
        List<String> matched = new ArrayList<>();
        if (needle.isEmpty()) {
            matched.addAll(allResults);
        } else {
            for (String line : allResults) {
                if (line.toLowerCase().contains(needle)) matched.add(line);
            }
        }
        // Tri
        Comparator<String> cmp = Comparator.comparing(this::sortKeyOf, String.CASE_INSENSITIVE_ORDER);
        if (!sortAscending) cmp = cmp.reversed();
        matched.sort(cmp);

        resultsList.clear();
        for (String line : matched) resultsList.add(line);
        resultsList.setScrollAmount(0);
    }

    private String sortKeyOf(String jsonLine) {
        try {
            JsonObject obj = JsonParser.parseString(jsonLine).getAsJsonObject();
            return switch (sortKey) {
                case TIMESTAMP -> (obj.has("ts") ? obj.get("ts").getAsString() : "");
                case EVENT -> (obj.has("event") ? obj.get("event").getAsString() : "");
                case PLAYER -> (obj.has("player") && obj.get("player").isJsonObject() && obj.getAsJsonObject("player").has("name")) ? obj.getAsJsonObject("player").get("name").getAsString() : "";
                case BLOCK -> (obj.has("block") ? obj.get("block").getAsString() : "");
            };
        } catch (Exception e) {
            return "";
        }
    }

    private static class ResultsList extends ObjectSelectionList<ResultsList.Entry> {
        private final int listTop;
        public ResultsList(Minecraft mc, int width, int height, int top, int bottom) {
            super(mc, width, height, top, bottom);
            this.listTop = top;
            // Hauteur de ligne définie via override getRowHeight() (ne pas assigner itemHeight qui est final)
        }
        public int getListTop() { return this.listTop; }
        public void add(String jsonLine) {
            try { this.addEntry(new Entry(JsonParser.parseString(jsonLine).getAsJsonObject())); } catch (Exception ignored) {}
        }
        public void clear() { this.clearEntries(); }

        @Override
        protected int getScrollbarPosition() { return super.getScrollbarPosition(); }
        @Override
        public int getRowWidth() { return width - 12; }
        @Override
        protected boolean isSelectedItem(int index) { return false; }
        // Méthode locale pour définir une hauteur “logique” de ligne
        protected int localRowHeight() { return 22; }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
            // Défilement de 3 lignes par cran
            double step = localRowHeight() * 3.0;
            setScrollAmount(getScrollAmount() - (deltaY * step));
            return true;
        }

        public static class Entry extends ObjectSelectionList.Entry<Entry> {
            private final JsonObject obj;
            public Entry(JsonObject obj) { this.obj = obj; }
            @Override
            public void render(GuiGraphics g, int idx, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTick) {
                String ts = obj.has("ts") ? obj.get("ts").getAsString() : "?";
                String ev = obj.has("event") ? obj.get("event").getAsString() : "?";
                String who = obj.has("player") && obj.get("player").getAsJsonObject().has("name") ? obj.get("player").getAsJsonObject().get("name").getAsString() : "-";
                g.drawString(Minecraft.getInstance().font, ts + "  •  " + ev + "  •  " + who, left + 4, top + 3, 0xFFD2D2D2);
                String dim = obj.has("dim") ? obj.get("dim").getAsString() : "";
                String pos = obj.has("pos") ? obj.get("pos").toString() : "";
                String blk = obj.has("block") ? obj.get("block").getAsString() : "";
                g.drawString(Minecraft.getInstance().font, (blk.isEmpty()?"":"["+blk+"] ") + pos + (dim.isEmpty()?"":" @"+dim), left + 6, top + 11, 0xFF8CA0B3);
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
    }
}
