package kujama;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** Menu oscuro de modulos: aparece con fundido, las tarjetas reaccionan al mouse y las barras cambian de color animadas. */
public class KujamaMenuScreen extends Screen {
    private static final int CW = 100, CH = 64, GAP = 6, COLS = 3, PAD = 14, BAR = 14, CHIP_W = 74, CHIP_STEP = 79;
    private static final int PANEL_W = PAD * 2 + COLS * CW + (COLS - 1) * GAP;

    private static final int C_PANEL = 0xF2141820, C_BORDER = 0xFF2C313B, C_CARD = 0xFF1E222B, C_CARD_HOVER = 0xFF2A303C,
            C_ON = 0xFF1FA85A, C_OFF = 0xFFB23A5C, C_BTN = 0xFF2A2F38, C_BTN_HOVER = 0xFF363C47, C_ACCENT = 0xFF1BD96A,
            C_TEXT = 0xFFFFFFFF, C_MUTED = 0xFFB4BAC4, C_DARK = 0xFF0B1F12;

    private static Category tab = Category.OPTIMIZACION;

    private final Screen parent;
    private final List<Module> shown = new ArrayList<>();
    private final float[] hover = new float[16];
    private final float[] onAnim = new float[16];
    private int px, py, panelH, headerY, chipsY, gridTop, footerY;
    private final long openAt = System.currentTimeMillis();
    private long lastNano;
    private float tabX = -1f;

    public KujamaMenuScreen(Screen parent) {
        super(Component.literal("Kujama"));
        this.parent = parent;
    }

    private int cardX(int i) { return px + PAD + (i % COLS) * (CW + GAP); }
    private int cardY(int i) { return gridTop + (i / COLS) * (CH + GAP); }
    private int chipX(int i) { return px + PAD + i * CHIP_STEP; }
    private float openProgress() { return Anim.easeOutCubic(Math.min(1f, (System.currentTimeMillis() - openAt) / 200f)); }

    @Override
    protected void init() {
        shown.clear();
        for (Module m : ModuleManager.all()) if (m.category == tab) shown.add(m);
        for (int i = 0; i < onAnim.length; i++) {
            hover[i] = 0f;
            onAnim[i] = i < shown.size() && shown.get(i).enabled ? 1f : 0f;
        }

        int rows = Math.max(1, (shown.size() + COLS - 1) / COLS);
        int gridH = rows * CH + (rows - 1) * GAP;
        panelH = PAD * 2 + 16 + 8 + 14 + 8 + gridH + 8 + 16;
        px = (width - PANEL_W) / 2;
        py = Math.max(4, (height - panelH) / 2);
        headerY = py + PAD;
        chipsY = headerY + 16 + 8;
        gridTop = chipsY + 14 + 8;
        footerY = gridTop + gridH + 8;

        addRenderableWidget(Button.builder(Component.empty(), b -> onClose())
                .bounds(px + PANEL_W - PAD - 14, headerY, 14, 14).build());
        addRenderableWidget(Button.builder(Component.empty(), b -> minecraft.setScreen(new HudEditorScreen(this, null)))
                .bounds(editX(), headerY, 70, 14).build());

        Category[] cats = Category.values();
        for (int i = 0; i < cats.length; i++) {
            Category c = cats[i];
            addRenderableWidget(Button.builder(Component.empty(), b -> { tab = c; rebuildWidgets(); })
                    .bounds(chipX(i), chipsY, CHIP_W, 14).build());
        }

        for (int i = 0; i < shown.size(); i++) {
            Module m = shown.get(i);
            int cx = cardX(i), cy = cardY(i);
            addRenderableWidget(Button.builder(Component.empty(), b -> m.toggle())
                    .bounds(cx, cy + CH - BAR, CW, BAR).build());
            if (m instanceof HudModule h) {
                addRenderableWidget(Button.builder(Component.empty(), b -> minecraft.setScreen(new HudEditorScreen(this, h)))
                        .bounds(cx, cy + CH - BAR * 2, CW, BAR).build());
            }
        }

        int bw = 84;
        addRenderableWidget(Button.builder(Component.empty(), b -> shown.forEach(m -> m.setEnabled(true)))
                .bounds(px + PANEL_W - PAD - bw * 2 - 4, footerY, bw, 14).build());
        addRenderableWidget(Button.builder(Component.empty(), b -> shown.forEach(m -> m.setEnabled(false)))
                .bounds(px + PANEL_W - PAD - bw, footerY, bw, 14).build());
    }

    private int editX() { return px + PANEL_W - PAD - 14 - 4 - 70; }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float delta) {
        g.fill(0, 0, width, height, Anim.alpha(0xAA05070B, openProgress()));
    }

    private static boolean in(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        super.render(g, mouseX, mouseY, delta);

        long now = System.nanoTime();
        float dt = lastNano == 0 ? 0.016f : Math.min(0.1f, (now - lastNano) / 1_000_000_000f);
        lastNano = now;
        float p = openProgress();
        int oy = (int) ((1f - p) * 14f);

        Draw.roundRect(g, px - 1, py - 1 + oy, PANEL_W + 2, panelH + 2, 6, Anim.alpha(C_BORDER, p));
        Draw.roundRect(g, px, py + oy, PANEL_W, panelH, 6, Anim.alpha(C_PANEL, p));

        // encabezado
        int hy = headerY + oy;
        Draw.roundRect(g, px + PAD, hy, 14, 14, 4, Anim.alpha(C_ACCENT, p));
        Draw.pixelGlyph(g, 'K', px + PAD + 4, hy + 3, 1, Anim.alpha(C_DARK, p));
        g.drawString(font, "KUJAMA CLIENT", px + PAD + 20, hy + 3, Anim.textAlpha(C_TEXT, p), false);
        boolean hoverEdit = in(mouseX, mouseY, editX(), headerY, 70, 14);
        Draw.roundRect(g, editX(), hy, 70, 14, 4, Anim.alpha(hoverEdit ? C_BTN_HOVER : C_BTN, p));
        g.drawCenteredString(font, "EDITAR HUD", editX() + 35, hy + 3, Anim.textAlpha(C_ACCENT, p));
        int closeX = px + PANEL_W - PAD - 14;
        boolean hoverClose = in(mouseX, mouseY, closeX, headerY, 14, 14);
        Draw.roundRect(g, closeX, hy, 14, 14, 4, Anim.alpha(hoverClose ? C_OFF : C_BTN, p));
        g.drawCenteredString(font, "X", closeX + 7, hy + 3, Anim.textAlpha(C_TEXT, p));

        // pestanas con indicador deslizante
        Category[] cats = Category.values();
        int sel = tab.ordinal();
        float target = chipX(sel);
        tabX = tabX < 0 ? target : Anim.approach(tabX, target, dt, 18f);
        int cy0 = chipsY + oy;
        for (int i = 0; i < cats.length; i++) {
            boolean h = in(mouseX, mouseY, chipX(i), chipsY, CHIP_W, 14);
            Draw.roundRect(g, chipX(i), cy0, CHIP_W, 14, 4, Anim.alpha(h ? C_BTN_HOVER : C_BTN, p));
        }
        Draw.roundRect(g, Math.round(tabX), cy0, CHIP_W, 14, 4, Anim.alpha(C_ACCENT, p));
        for (int i = 0; i < cats.length; i++) {
            g.drawCenteredString(font, cats[i].label, chipX(i) + CHIP_W / 2, cy0 + 3,
                    Anim.textAlpha(i == sel ? C_DARK : C_MUTED, p));
        }

        // tarjetas
        String hoverDesc = null;
        for (int i = 0; i < shown.size(); i++) {
            Module m = shown.get(i);
            int cx = cardX(i), cy = cardY(i) + oy;
            boolean hov = in(mouseX, mouseY, cardX(i), cardY(i), CW, CH);
            hover[i] = Anim.approach(hover[i], hov ? 1f : 0f, dt, 16f);
            onAnim[i] = Anim.approach(onAnim[i], m.enabled ? 1f : 0f, dt, 14f);
            if (hov) hoverDesc = m.description;

            Draw.roundRect(g, cx, cy, CW, CH, 4, Anim.alpha(Anim.mix(C_CARD, C_CARD_HOVER, hover[i]), p));
            int bob = Math.round(hover[i] * 2f);
            g.renderItem(new ItemStack(m.icon), cx + CW / 2 - 8, cy + 4 - bob);
            g.drawCenteredString(font, font.plainSubstrByWidth(m.name, CW - 6), cx + CW / 2, cy + 23,
                    Anim.textAlpha(C_TEXT, p));

            if (m instanceof HudModule) {
                int my = cy + CH - BAR * 2;
                boolean hm = in(mouseX, mouseY, cardX(i), cardY(i) + CH - BAR * 2, CW, BAR);
                g.fill(cx, my, cx + CW, my + BAR, Anim.alpha(hm ? C_BTN_HOVER : C_BTN, p));
                g.drawCenteredString(font, "MOVER", cx + CW / 2, my + 3, Anim.textAlpha(C_MUTED, p));
            }
            int ty = cy + CH - BAR;
            Draw.roundRect(g, cx, ty, CW, BAR, 4, Anim.alpha(Anim.mix(C_OFF, C_ON, onAnim[i]), p));
            g.drawCenteredString(font, m.enabled ? "ACTIVADO" : "DESACTIVADO", cx + CW / 2, ty + 3, Anim.textAlpha(C_TEXT, p));
        }

        // pie
        int fy = footerY + oy;
        String text = hoverDesc != null && !hoverDesc.isEmpty() ? hoverDesc : "Pasa el mouse sobre un modulo";
        g.drawString(font, font.plainSubstrByWidth(text, PANEL_W - PAD * 2 - 180), px + PAD, fy + 3,
                Anim.textAlpha(C_MUTED, p), false);
        int bw = 84;
        int b1 = px + PANEL_W - PAD - bw * 2 - 4, b2 = px + PANEL_W - PAD - bw;
        boolean h1 = in(mouseX, mouseY, b1, footerY, bw, 14), h2 = in(mouseX, mouseY, b2, footerY, bw, 14);
        Draw.roundRect(g, b1, fy, bw, 14, 4, Anim.alpha(h1 ? C_BTN_HOVER : C_BTN, p));
        g.drawCenteredString(font, "ACTIVAR TODO", b1 + bw / 2, fy + 3, Anim.textAlpha(C_ACCENT, p));
        Draw.roundRect(g, b2, fy, bw, 14, 4, Anim.alpha(h2 ? C_BTN_HOVER : C_BTN, p));
        g.drawCenteredString(font, "APAGAR TODO", b2 + bw / 2, fy + 3, Anim.textAlpha(C_MUTED, p));
    }

    @Override
    public void onClose() {
        ModuleManager.save();
        minecraft.setScreen(parent);
    }
}
