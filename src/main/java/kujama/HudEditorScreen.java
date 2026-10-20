package kujama;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** Editor de HUD: arrastra cada elemento con el mouse (se ajusta a una cuadricula de 2 px). */
public class HudEditorScreen extends Screen {
    private static final int GRID = 2;
    private final Screen parent;
    private HudModule selected;
    private HudModule dragging;
    private int offX, offY;

    public HudEditorScreen(Screen parent, HudModule preselect) {
        super(Component.literal("Editar HUD"));
        this.parent = parent;
        this.selected = preselect;
    }

    @Override
    protected void init() {
        addRenderableWidget(Button.builder(Component.literal("Listo"), b -> onClose())
                .bounds(width / 2 - 40, height - 28, 80, 20).build());
    }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float delta) {
        g.fill(0, 0, width, height, 0x66000000);
    }

    private static boolean contains(HudModule h, double mx, double my) {
        return mx >= h.x - 2 && mx < h.x + h.w + 2 && my >= h.y - 2 && my < h.y + h.h + 2;
    }

    private static void outline(GuiGraphics g, int x, int y, int w, int h, int c) {
        g.fill(x, y, x + w, y + 1, c);
        g.fill(x, y + h - 1, x + w, y + h, c);
        g.fill(x, y, x + 1, y + h, c);
        g.fill(x + w - 1, y, x + w, y + h, c);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        super.render(g, mouseX, mouseY, delta);
        for (int gx = 0; gx < width; gx += 20) g.fill(gx, 0, gx + 1, height, 0x14FFFFFF);
        for (int gy = 0; gy < height; gy += 20) g.fill(0, gy, width, gy + 1, 0x14FFFFFF);

        for (Module m : ModuleManager.all()) {
            if (!(m instanceof HudModule h) || !h.enabled || h.w <= 0) continue;
            boolean sel = h == selected;
            boolean hov = contains(h, mouseX, mouseY);
            int c = sel ? 0xFF1BD96A : hov ? 0xFFFFFFFF : 0x99B4BAC4;
            outline(g, h.x - 2, h.y - 2, h.w + 4, h.h + 4, c);
            g.drawString(font, h.name, h.x - 2, h.y - 12, c, false);
        }
        g.drawCenteredString(font, "Arrastra los elementos para moverlos. Esc para salir.", width / 2, 10, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (super.mouseClicked(event, doubleClick)) return true;
        if (event.button() != 0) return false;
        HudModule hit = null;
        for (Module m : ModuleManager.all()) {
            if (m instanceof HudModule h && h.enabled && h.w > 0 && contains(h, event.x(), event.y())) hit = h;
        }
        if (hit == null) return false;
        selected = hit;
        dragging = hit;
        offX = (int) event.x() - hit.x;
        offY = (int) event.y() - hit.y;
        return true;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (dragging == null) return super.mouseDragged(event, dx, dy);
        int nx = Math.round(((int) event.x() - offX) / (float) GRID) * GRID;
        int ny = Math.round(((int) event.y() - offY) / (float) GRID) * GRID;
        dragging.x = Math.max(0, Math.min(nx, Math.max(0, width - dragging.w)));
        dragging.y = Math.max(0, Math.min(ny, Math.max(0, height - dragging.h)));
        return true;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (dragging == null) return super.mouseReleased(event);
        dragging = null;
        ModuleManager.save();
        return true;
    }

    @Override
    public void onClose() {
        ModuleManager.save();
        minecraft.setScreen(parent);
    }
}
