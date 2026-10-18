package kujama;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** Menu de modulos con tarjetas (estilo cliente PvP): categorias arriba, cada modulo con barra ACTIVADO/DESACTIVADO. */
public class KujamaMenuScreen extends Screen {
    private static final int CW = 100, CH = 64, GAP = 6, COLS = 3, PAD = 14, BAR = 14;
    private static final int PANEL_W = PAD * 2 + COLS * CW + (COLS - 1) * GAP;

    private static final int C_PANEL = 0xF2141820, C_BORDER = 0xFF2C313B, C_CARD = 0xFF1E222B, C_CARD_HOVER = 0xFF272C37,
            C_ON = 0xFF1FA85A, C_OFF = 0xFFB23A5C, C_BTN = 0xFF2A2F38, C_ACCENT = 0xFF1BD96A,
            C_TEXT = 0xFFFFFFFF, C_MUTED = 0xFFB4BAC4;

    private static Category tab = Category.OPTIMIZACION;

    private final Screen parent;
    private final List<Module> shown = new ArrayList<>();
    private int px, py, panelH, chipsY, gridTop, footerY;

    public KujamaMenuScreen(Screen parent) {
        super(Component.literal("Kujama"));
        this.parent = parent;
    }

    private int cardX(int i) { return px + PAD + (i % COLS) * (CW + GAP); }
    private int cardY(int i) { return gridTop + (i / COLS) * (CH + GAP); }

    @Override
    protected void init() {
        shown.clear();
        for (Module m : ModuleManager.all()) if (m.category == tab) shown.add(m);

        int rows = Math.max(1, (shown.size() + COLS - 1) / COLS);
        int gridH = rows * CH + (rows - 1) * GAP;
        panelH = PAD * 2 + 16 + 8 + 14 + 8 + gridH + 8 + 16;
        px = (width - PANEL_W) / 2;
        py = Math.max(4, (height - panelH) / 2);
        int headerY = py + PAD;
        chipsY = headerY + 16 + 8;
        gridTop = chipsY + 14 + 8;
        footerY = gridTop + gridH + 8;

        // cerrar
        addRenderableWidget(Button.builder(Component.empty(), b -> onClose())
                .bounds(px + PANEL_W - PAD - 14, headerY, 14, 14).build());

        // categorias
        Category[] cats = Category.values();
        for (int i = 0; i < cats.length; i++) {
            Category c = cats[i];
            addRenderableWidget(Button.builder(Component.empty(), b -> { tab = c; rebuildWidgets(); })
                    .bounds(chipX(i), chipsY, 74, 14).build());
        }

        // tarjetas
        for (int i = 0; i < shown.size(); i++) {
            Module m = shown.get(i);
            int cx = cardX(i), cy = cardY(i);
            addRenderableWidget(Button.builder(Component.empty(), b -> m.toggle())
                    .bounds(cx, cy + CH - BAR, CW, BAR).build());
            if (m instanceof HudModule h) {
                addRenderableWidget(Button.builder(Component.empty(), b -> minecraft.setScreen(new HudMoveScreen(this, h)))
                        .bounds(cx, cy + CH - BAR * 2, CW, BAR).build());
            }
        }

        // activar / desactivar todo
        int bw = 84;
        addRenderableWidget(Button.builder(Component.empty(), b -> shown.forEach(m -> m.setEnabled(true)))
                .bounds(px + PANEL_W - PAD - bw * 2 - 4, footerY, bw, 14).build());
        addRenderableWidget(Button.builder(Component.empty(), b -> shown.forEach(m -> m.setEnabled(false)))
                .bounds(px + PANEL_W - PAD - bw, footerY, bw, 14).build());
    }

    private int chipX(int i) { return px + PAD + i * 79; }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        super.render(g, mouseX, mouseY, delta);

        // panel
        g.fill(px - 1, py - 1, px + PANEL_W + 1, py + panelH + 1, C_BORDER);
        g.fill(px, py, px + PANEL_W, py + panelH, C_PANEL);

        // encabezado
        int hy = py + PAD;
        g.fill(px + PAD, hy, px + PAD + 14, hy + 14, C_ACCENT);
        g.drawString(font, "K", px + PAD + 4, hy + 3, 0xFF0B1F12, false);
        g.drawString(font, "KUJAMA CLIENT", px + PAD + 20, hy + 3, C_TEXT, false);
        int closeX = px + PANEL_W - PAD - 14;
        g.fill(closeX, hy, closeX + 14, hy + 14, C_BTN);
        g.drawCenteredString(font, "X", closeX + 7, hy + 3, C_TEXT);

        // categorias
        Category[] cats = Category.values();
        for (int i = 0; i < cats.length; i++) {
            int x = chipX(i);
            boolean sel = cats[i] == tab;
            g.fill(x, chipsY, x + 74, chipsY + 14, sel ? C_ACCENT : C_BTN);
            g.drawCenteredString(font, cats[i].label, x + 37, chipsY + 3, sel ? 0xFF0B1F12 : C_MUTED);
        }

        // tarjetas
        String hoverDesc = null;
        for (int i = 0; i < shown.size(); i++) {
            Module m = shown.get(i);
            int cx = cardX(i), cy = cardY(i);
            boolean hover = mouseX >= cx && mouseX < cx + CW && mouseY >= cy && mouseY < cy + CH;
            if (hover) hoverDesc = m.description;
            g.fill(cx, cy, cx + CW, cy + CH, hover ? C_CARD_HOVER : C_CARD);
            g.renderItem(new ItemStack(m.icon), cx + CW / 2 - 8, cy + 4);
            g.drawCenteredString(font, font.plainSubstrByWidth(m.name, CW - 6), cx + CW / 2, cy + 23, C_TEXT);
            if (m instanceof HudModule) {
                int my = cy + CH - BAR * 2;
                g.fill(cx, my, cx + CW, my + BAR, C_BTN);
                g.drawCenteredString(font, "MOVER", cx + CW / 2, my + 3, C_MUTED);
            }
            int ty = cy + CH - BAR;
            g.fill(cx, ty, cx + CW, ty + BAR, m.enabled ? C_ON : C_OFF);
            g.drawCenteredString(font, m.enabled ? "ACTIVADO" : "DESACTIVADO", cx + CW / 2, ty + 3, C_TEXT);
        }

        // pie: descripcion + botones
        String text = hoverDesc != null && !hoverDesc.isEmpty() ? hoverDesc : "Pasa el mouse sobre un modulo";
        g.drawString(font, font.plainSubstrByWidth(text, PANEL_W - PAD * 2 - 180), px + PAD, footerY + 3, C_MUTED, false);
        int bw = 84;
        int b1 = px + PANEL_W - PAD - bw * 2 - 4, b2 = px + PANEL_W - PAD - bw;
        g.fill(b1, footerY, b1 + bw, footerY + 14, C_BTN);
        g.drawCenteredString(font, "ACTIVAR TODO", b1 + bw / 2, footerY + 3, C_ACCENT);
        g.fill(b2, footerY, b2 + bw, footerY + 14, C_BTN);
        g.drawCenteredString(font, "APAGAR TODO", b2 + bw / 2, footerY + 3, C_MUTED);
    }

    @Override
    public void onClose() {
        ModuleManager.save();
        minecraft.setScreen(parent);
    }
}
