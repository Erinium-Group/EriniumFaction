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
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
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

    private static final List<String> INCOMING_BUFFER = new ArrayList<>();
    private final List<String> allResults = new ArrayList<>();

    private enum SortKey {TIMESTAMP, EVENT, PLAYER, BLOCK}

    private SortKey sortKey = SortKey.TIMESTAMP;
    private boolean sortAscending = true;
    private Button sortKeyBtn, sortOrderBtn;

    // Supprimer les champs inutilisés reloadBtn/exportBtn
    private String lastP, lastB, lastE;
    private long lastFrom, lastTo;
    private int lastLimit, lastOffset;

    public AuditViewerScreen() {
        super(Component.translatable("erinium_faction.gui.audit.title"));
    }

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

        playerBox = addRenderableWidget(new EditBox(font, x, row1Y, w, h, Component.translatable("erinium_faction.gui.audit.player_uuid")));
        playerBox.setMaxLength(36); // UUID 36 chars
        bindPlaceholder(playerBox, "erinium_faction.gui.audit.player_uuid.suggestion", null);
        playerBox.setTooltip(Tooltip.create(Component.translatable("erinium_faction.gui.audit.player_uuid.tooltip")));

        blockBox = addRenderableWidget(new EditBox(font, x + 130, row1Y, w, h, Component.translatable("erinium_faction.gui.audit.block_id")));
        blockBox.setMaxLength(128); // block id + éventuelles propriétés
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

        addRenderableWidget(Button.builder(Component.translatable("erinium_faction.gui.audit.search"), btn -> {
            offset = 0;
            doSearch();
        }).bounds(x, btnY, 80, 20).tooltip(Tooltip.create(Component.translatable("erinium_faction.gui.audit.search.tooltip"))).build());
        addRenderableWidget(Button.builder(Component.translatable("erinium_faction.gui.audit.clear"), btn -> clearResults()).bounds(x + 90, btnY, 80, 20).tooltip(Tooltip.create(Component.translatable("erinium_faction.gui.audit.clear.tooltip"))).build());
        addRenderableWidget(Button.builder(Component.translatable("erinium_faction.gui.audit.prev"), btn -> {
            offset = Math.max(0, offset - getPageSize());
            doSearch();
        }).bounds(x + 180, btnY, 20, 20).tooltip(Tooltip.create(Component.translatable("erinium_faction.gui.audit.prev.tooltip"))).build());
        addRenderableWidget(Button.builder(Component.translatable("erinium_faction.gui.audit.next"), btn -> {
            offset = offset + getPageSize();
            doSearch();
        }).bounds(x + 205, btnY, 20, 20).tooltip(Tooltip.create(Component.translatable("erinium_faction.gui.audit.next.tooltip"))).build());

        // Barre de recherche (filtre client)
        queryBox = addRenderableWidget(new EditBox(font, x, queryY, 360, 20, Component.translatable("erinium_faction.gui.audit.query")));
        bindPlaceholder(queryBox, "erinium_faction.gui.audit.query.suggestion", this::applyFilter);
        queryBox.setTooltip(Tooltip.create(Component.translatable("erinium_faction.gui.audit.query.tooltip")));

        // Contrôles de tri
        sortKeyBtn = addRenderableWidget(Button.builder(Component.literal(""), btn -> {
            switch (sortKey) {
                case TIMESTAMP -> sortKey = SortKey.EVENT;
                case EVENT -> sortKey = SortKey.PLAYER;
                case PLAYER -> sortKey = SortKey.BLOCK;
                case BLOCK -> sortKey = SortKey.TIMESTAMP;
            }
            updateSortButtonsLabels();
            applyFilter();
        }).bounds(x, sortY, 160, 20).tooltip(Tooltip.create(Component.translatable("erinium_faction.gui.audit.sort.key.tooltip"))).build());

        sortOrderBtn = addRenderableWidget(Button.builder(Component.literal(""), btn -> {
            sortAscending = !sortAscending;
            updateSortButtonsLabels();
            applyFilter();
        }).bounds(x + 170, sortY, 160, 20).tooltip(Tooltip.create(Component.translatable("erinium_faction.gui.audit.sort.order.tooltip"))).build());
        updateSortButtonsLabels();

        resultsList = addRenderableWidget(new ResultsList(Minecraft.getInstance(), this.width, this.height, listTopY, 28));

        // Ajout des boutons Reload / Export en local
        int reloadExportY = y + 70;
        addRenderableWidget(Button.builder(Component.translatable("erinium_faction.gui.audit.reload"), btn -> rerunLastQuery()).bounds(x + 230, reloadExportY, 70, 20).tooltip(Tooltip.create(Component.translatable("erinium_faction.gui.audit.reload.tooltip"))).build());
        addRenderableWidget(Button.builder(Component.translatable("erinium_faction.gui.audit.export"), btn -> doExport()).bounds(x + 305, reloadExportY, 70, 20).tooltip(Tooltip.create(Component.translatable("erinium_faction.gui.audit.export.tooltip"))).build());

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
        box.setResponder(hook);
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

    private int getPageSize() {
        try {
            return Integer.parseInt(limitBox.getValue().trim());
        } catch (Exception e) {
            return 50;
        }
    }

    private void doSearch() {
        clearResults();
        String p = emptyToNull(playerBox.getValue());
        String b = emptyToNull(blockBox.getValue());
        String e = emptyToNull(eventBox.getValue());
        long from = parseInstantEpochMs(fromBox.getValue());
        long to = parseInstantEpochMs(toBox.getValue());
        int limit = getPageSize();
        // mémoriser pour reload
        lastP = p;
        lastB = b;
        lastE = e;
        lastFrom = from;
        lastTo = to;
        lastLimit = limit;
        lastOffset = offset;
        PacketDistributor.sendToServer(new AuditQueryRequestPacket(p, b, e, from, to, limit, offset));
    }

    private void rerunLastQuery() {
        if (lastLimit <= 0) lastLimit = getPageSize();
        PacketDistributor.sendToServer(new AuditQueryRequestPacket(lastP, lastB, lastE, lastFrom, lastTo, lastLimit, lastOffset));
    }

    private void doExport() {
        // Export des entrées actuellement filtrées (matchees) vers le presse-papiers
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < resultsList.itemCount(); i++) {
            var e = resultsList.entryAt(i);
            if (e instanceof ResultsList.Entry re) {
                sb.append(re.rawJson()).append('\n');
            }
        }
        String out = sb.toString();
        if (!out.isEmpty()) {
            Minecraft.getInstance().keyboardHandler.setClipboard(out);
            Minecraft.getInstance().getToasts().addToast(new SystemToast(SystemToast.SystemToastId.PERIODIC_NOTIFICATION, Component.translatable("erinium_faction.gui.audit.export"), Component.translatable("erinium_faction.gui.audit.export.toast")));
        }
    }

    private static String emptyToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private static long parseInstantEpochMs(String s) {
        try {
            return Instant.parse(s.trim()).toEpochMilli();
        } catch (Exception e) {
            return 0L;
        }
    }

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

        // Compteur de résultats
        int matchedCount = resultsList.itemCount();
        if (matchedCount > 0) {
            String countText = Component.translatable("erinium_faction.gui.audit.count", matchedCount, allResults.size()).getString();
            g.drawString(font, countText, this.width / 2 - font.width(countText) / 2, headerY + 20, 0xAAAAAA);
        } else {
            // Message si aucun résultat
            String emptyText = Component.translatable("erinium_faction.gui.audit.empty").getString();
            g.drawString(font, emptyText, this.width / 2 - font.width(emptyText) / 2, headerY + 20, 0xFF5555);
        }
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
                case PLAYER ->
                        (obj.has("player") && obj.get("player").isJsonObject() && obj.getAsJsonObject("player").has("name")) ? obj.getAsJsonObject("player").get("name").getAsString() : "";
                case BLOCK -> (obj.has("block") ? obj.get("block").getAsString() : "");
            };
        } catch (Exception e) {
            return "";
        }
    }

    private static class ResultsList extends ObjectSelectionList<ResultsList.Entry> {
        private final int listTop;

        public ResultsList(Minecraft mc, int width, int height, int top, int itemHeight) {
            super(mc, width, height, top, itemHeight);
            this.listTop = top;
        }

        @Override
        public int getRowWidth() {
            return width - 12;
        }


        public int getListTop() {
            return this.listTop;
        }

        public void add(String jsonLine) {
            try {
                this.addEntry(new Entry(JsonParser.parseString(jsonLine).getAsJsonObject()));
            } catch (Exception ignored) {
            }
        }

        public void clear() {
            this.clearEntries();
        }

        public int itemCount() {
            return this.getItemCount();
        }

        public Entry entryAt(int index) {
            return this.getEntry(index);
        }

        @Override
        protected int getScrollbarPosition() {
            return super.getScrollbarPosition();
        }

        public static class Entry extends ObjectSelectionList.Entry<Entry> {
            private final JsonObject obj;
            // Icône éventuelle associée au bloc / item
            private final net.minecraft.world.item.ItemStack iconStack;
            private final boolean hasIcon;

            public Entry(JsonObject obj) {
                this.obj = obj;
                // Construction de l’icône si applicable
                this.iconStack = buildIcon();
                this.hasIcon = !this.iconStack.isEmpty();
            }

            @Override
            public void render(@Nonnull GuiGraphics g, int idx, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTick) {
                Minecraft mc = Minecraft.getInstance();
                var font = mc.font;
                String ts = safe(obj, "ts");
                String ev = safe(obj, "event");
                String who = obj.has("player") && obj.get("player").isJsonObject() && obj.getAsJsonObject("player").has("name") ? obj.getAsJsonObject("player").get("name").getAsString() : "-";
                String blk = safe(obj, "block");
                String dim = safe(obj, "dim");
                String pos = obj.has("pos") ? obj.get("pos").toString() : "";
                int eventColor = eventColorOf(ev);
                int borderColor = hovered ? 0xFF47545F : 0xFF3A444C;
                int bgColor = hovered ? 0xCC1E2328 : 0xB01A1F24;
                g.fill(left, top, left + width - 4, top + height, borderColor);
                g.fill(left + 1, top + 1, left + width - 5, top + height - 1, bgColor);
                g.fill(left + 1, top + 1, left + 5, top + height - 1, eventColor);
                // Accent à droite dépendant de l’événement
                g.fill(left + width - 6, top + 1, left + width - 2, top + height - 1, eventColor);
                // Légère ombre basse
                g.fill(left + 1, top + height - 2, left + width - 5, top + height - 1, 0x40000000);
                // Couleurs de texte
                int tsColor = 0xFF9BB0C6;
                int whoColor = 0xFFEAD547;
                int mainColor = 0xFFE0E6EB;
                int secondColor = 0xFF8CA5B9;

                // Position du texte et icône
                final int iconSize = 16;
                final int gap = 6; // espace entre icône et texte
                int iconX = left + 6; // plus collé à gauche (après l'accent)
                int iconY = top + 5;
                int textLeft = hasIcon ? (iconX + iconSize + gap) : (left + 13);
                // Centrage vertical des 2 lignes (selon font et hauteur de ligne)
                int lineH = font.lineHeight;
                int twoLines = lineH * 2 + 2; // marge 2px entre lignes
                int yMain = top + Math.max(3, (height - twoLines) / 2);
                int ySecond = yMain + lineH + 2;
                // Espace réservé à droite pour l’icône de dimension
                int rightIconSize = Math.min(14, height - 8);
                int rightIconPadding = 6;
                int rightReserved = 0;

                // Rendu de l’icône bloc/item si présente
                if (hasIcon) {
                    g.renderFakeItem(iconStack, iconX, iconY);
                    g.renderItemDecorations(font, iconStack, iconX, iconY);
                }

                // Rendu de l’icône de dimension (resource-packable)
                int dimIconX = left + width - rightIconSize - 7; // à l’intérieur, près de l’accent droit
                int dimIconY = top + (height - rightIconSize) / 2;
                var dimIcon = resolveDimensionIcon(dim);
                if (hasResource(dimIcon)) {
                    rightReserved = rightIconSize + rightIconPadding;
                    g.blit(dimIcon, dimIconX, dimIconY, 0, 0, rightIconSize, rightIconSize, rightIconSize, rightIconSize);
                } else {
                    // Fallback: petite icône vectorielle selon la dimension
                    rightReserved = rightIconSize + rightIconPadding;
                    drawFallbackDimIcon(g, dim, dimIconX, dimIconY, rightIconSize);
                }

                int maxWidth = (left + width - 10 - rightReserved) - textLeft; // plus de marge à droite

                // Ligne principale (timestamp | event | player) avec recolor segmentaire
                // Construit morceau par morceau pour limiter le dépassement
                String sep = "  |  ";
                int xCur = textLeft;
                // tronquer chaque segment si besoin, en gardant la lisibilité
                String aDraw = ts;
                if (font.width(aDraw) > maxWidth) {
                    aDraw = font.plainSubstrByWidth(aDraw, maxWidth - font.width("…")) + "…";
                }
                g.drawString(font, aDraw, xCur, yMain, tsColor);
                xCur += font.width(aDraw);
                if (xCur + font.width(sep) < textLeft + maxWidth) {
                    g.drawString(font, sep, xCur, yMain, mainColor);
                    xCur += font.width(sep);
                }
                String bDraw = ev;
                if (xCur + font.width(bDraw) > textLeft + maxWidth) {
                    bDraw = font.plainSubstrByWidth(bDraw, (textLeft + maxWidth) - xCur - font.width("…")) + "…";
                }
                if (xCur < textLeft + maxWidth) {
                    g.drawString(font, bDraw, xCur, yMain, eventColor);
                    xCur += font.width(bDraw);
                }
                if (xCur + font.width(sep) < textLeft + maxWidth) {
                    g.drawString(font, sep, xCur, yMain, mainColor);
                    xCur += font.width(sep);
                }
                String cDraw = who;
                if (xCur + font.width(cDraw) > textLeft + maxWidth) {
                    cDraw = font.plainSubstrByWidth(cDraw, (textLeft + maxWidth) - xCur - font.width("…")) + "…";
                }
                if (xCur < textLeft + maxWidth) g.drawString(font, cDraw, xCur, yMain, whoColor);

                // Ligne secondaire (bloc / pos / dim), tronquée si nécessaire
                String second = (blk.isEmpty() ? "" : blk) + (pos.isEmpty() ? "" : "  " + pos) + (dim.isEmpty() ? "" : "  @" + dim);
                if (!second.isEmpty()) {
                    String sDraw = second;
                    if (font.width(sDraw) > maxWidth)
                        sDraw = font.plainSubstrByWidth(sDraw, maxWidth - font.width("…")) + "…";
                    g.drawString(font, sDraw, textLeft, ySecond, secondColor);
                }

                // Tooltip contextuel si survol
                if (hovered && mouseX >= left && mouseX <= left + width && mouseY >= top && mouseY <= top + height) {
                    java.util.List<Component> tips = new java.util.ArrayList<>();
                    tips.add(Component.literal(ts).withColor(tsColor));
                    tips.add(Component.literal(ev).withColor(eventColor));
                    tips.add(Component.literal(who).withColor(whoColor));
                    if (!blk.isEmpty()) tips.add(Component.literal("Block: " + blk));
                    if (!pos.isEmpty()) tips.add(Component.literal("Pos: " + pos));
                    if (!dim.isEmpty()) tips.add(Component.literal("Dim: " + dim));
                    if (hasIcon) tips.add(Component.translatable(iconStack.getItem().getDescriptionId()));
                    g.renderTooltip(Minecraft.getInstance().font, tips, java.util.Optional.empty(), mouseX, mouseY);
                }
            }

            private net.minecraft.world.item.ItemStack buildIcon() {
                try {
                    String id = getIdField(obj, "item");
                    if (id.isEmpty()) id = getIdField(obj, "block");
                    id = sanitizeRegistryId(id);
                    if (!id.isEmpty()) {
                        var rl = net.minecraft.resources.ResourceLocation.tryParse(id);
                        if (rl != null) {
                            // Essai item direct
                            var optItem = net.minecraft.core.registries.BuiltInRegistries.ITEM.getOptional(rl);
                            if (optItem.isPresent()) return new net.minecraft.world.item.ItemStack(optItem.get());
                            // Essai bloc -> item
                            var optBlock = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getOptional(rl);
                            if (optBlock.isPresent()) {
                                var it = optBlock.get().asItem();
                                if (it != net.minecraft.world.item.Items.AIR)
                                    return new net.minecraft.world.item.ItemStack(it);
                            }
                        }
                    }
                } catch (Exception ignored) {
                }
                return net.minecraft.world.item.ItemStack.EMPTY;
            }

            private String getIdField(JsonObject o, String key) {
                try {
                    if (!o.has(key)) return "";
                    var el = o.get(key);
                    if (el.isJsonPrimitive() && el.getAsJsonPrimitive().isString()) return el.getAsString();
                    if (el.isJsonObject()) {
                        var ob = el.getAsJsonObject();
                        if (ob.has("id") && ob.get("id").isJsonPrimitive()) return ob.get("id").getAsString();
                        if (ob.has("name") && ob.get("name").isJsonPrimitive()) return ob.get("name").getAsString();
                    }
                } catch (Exception ignored) {
                }
                return "";
            }

            private String sanitizeRegistryId(String raw) {
                if (raw == null) return "";
                String s = raw.trim();
                if (s.isEmpty()) return "";
                // Extraire entre accolades: Block{minecraft:stone}
                int b1 = s.indexOf('{');
                int b2 = s.indexOf('}');
                if (b1 >= 0 && b2 > b1) s = s.substring(b1 + 1, b2);
                // enlever les propriétés: minecraft:stone[foo=bar]
                int prop = s.indexOf('[');
                if (prop > 0) s = s.substring(0, prop);
                // remplacer espaces et uppercases
                s = s.replace(' ', '_').toLowerCase();
                // certains logs utilisent '.' au lieu de ':'
                if (!s.contains(":") && s.contains(".")) {
                    int dot = s.indexOf('.');
                    s = s.substring(0, dot) + ":" + s.substring(dot + 1);
                }
                // namespace par défaut
                if (!s.contains(":")) s = "minecraft:" + s;
                return s;
            }

            private String safe(JsonObject o, String k) {
                try {
                    return o.has(k) ? o.get(k).getAsString() : "";
                } catch (Exception e) {
                    return "";
                }
            }

            public String rawJson() {
                return obj.toString();
            }

            @Override
            public boolean mouseClicked(double mx, double my, int button) {
                if (button == 0) {
                    Minecraft mc = Minecraft.getInstance();
                    mc.keyboardHandler.setClipboard(obj.toString());
                    mc.getToasts().addToast(new SystemToast(SystemToast.SystemToastId.PERIODIC_NOTIFICATION, Component.translatable("erinium_faction.gui.audit.copied"), Component.literal("")));
                    return true;
                }
                return false;
            }

            @Override
            public @NotNull Component getNarration() {
                return Component.empty();
            }

            private net.minecraft.resources.ResourceLocation resolveDimensionIcon(String dimId) {
                String id = (dimId == null || dimId.isBlank()) ? "minecraft:overworld" : dimId.trim();
                net.minecraft.resources.ResourceLocation dimRL = net.minecraft.resources.ResourceLocation.tryParse(id);
                if (dimRL == null) dimRL = net.minecraft.resources.ResourceLocation.parse("minecraft:overworld");
                // Utilitaire pour construire sans accèder au constructeur privé
                java.util.function.BiFunction<String,String,net.minecraft.resources.ResourceLocation> rl = (ns, path) -> net.minecraft.resources.ResourceLocation.parse(ns + ":" + path);
                net.minecraft.resources.ResourceLocation candidate1 = rl.apply(dimRL.getNamespace(), "textures/gui/dimensions/" + dimRL.getPath() + ".png");
                if (hasResource(candidate1)) return candidate1;
                net.minecraft.resources.ResourceLocation candidate2 = rl.apply("erinium_faction", "textures/gui/dimensions/" + dimRL.getNamespace() + "/" + dimRL.getPath() + ".png");
                if (hasResource(candidate2)) return candidate2;
                return rl.apply("erinium_faction", "textures/gui/dimensions/overworld.png");
            }

            private boolean hasResource(net.minecraft.resources.ResourceLocation rl) {
                try {
                    return Minecraft.getInstance().getResourceManager().getResource(rl).isPresent();
                } catch (Exception e) {
                    return false;
                }
            }

            private void drawFallbackDimIcon(GuiGraphics g, String dimId, int x, int y, int size) {
                String d = dimId == null ? "" : dimId;
                int fill;
                if (d.contains("the_nether")) fill = 0xFFB0352F;
                else if (d.contains("the_end")) fill = 0xFF7D5BE6;
                else if (d.contains("overworld") || d.isEmpty()) fill = 0xFF3BA55C;
                else fill = 0xFF4DA6FF;
                int border = 0xFF1D2328;
                g.fill(x, y, x + size, y + size, border);
                g.fill(x + 1, y + 1, x + size - 1, y + size - 1, fill);
                g.fill(x + 2, y + size / 2, x + size - 2, y + size / 2 + 1, 0x40FFFFFF);
            }

            private int eventColorOf(String evRaw) {
                String e = evRaw == null ? "" : evRaw.toLowerCase();
                if (e.contains("place")) return 0xFF57C84D;
                if (e.contains("break") || e.contains("destroy")) return 0xFFF28C38;
                if (e.contains("interact") || e.contains("use") || e.contains("click")) return 0xFF4DA6FF;
                if (e.contains("open") || e.contains("container")) return 0xFFB07EFF;
                return 0xFF6F7D89;
            }
        }
    }
}
