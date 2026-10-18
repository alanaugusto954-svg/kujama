package kujama;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Menu para activar/desactivar modulos y mover los elementos del HUD. */
public class ClickGuiScreen extends Screen {
    private final Screen parent;

    public ClickGuiScreen(Screen parent) {
        super(Component.literal("Kujama"));
        this.parent = parent;
    }

    private static Component label(Module m) {
        return Component.literal(m.name + ": " + (m.enabled ? "ON" : "OFF"));
    }

    @Override
    protected void init() {
        int y = 36;
        int left = width / 2 - 130;
        for (Module m : ModuleManager.all()) {
            addRenderableWidget(Button.builder(label(m), b -> {
                m.toggle();
                b.setMessage(label(m));
            }).bounds(left, y, 150, 20).build());

            if (m instanceof HudModule h) {
                addRenderableWidget(Button.builder(Component.literal("<"), b -> h.x -= 5).bounds(left + 156, y, 20, 20).build());
                addRenderableWidget(Button.builder(Component.literal(">"), b -> h.x += 5).bounds(left + 178, y, 20, 20).build());
                addRenderableWidget(Button.builder(Component.literal("^"), b -> h.y -= 5).bounds(left + 200, y, 20, 20).build());
                addRenderableWidget(Button.builder(Component.literal("v"), b -> h.y += 5).bounds(left + 222, y, 20, 20).build());
            }
            y += 24;
        }
        addRenderableWidget(Button.builder(Component.literal("Cerrar"), b -> onClose())
                .bounds(width / 2 - 50, Math.min(height - 28, y + 8), 100, 20).build());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        super.render(g, mouseX, mouseY, delta);
        g.drawCenteredString(font, Component.literal("Kujama Client - modulos"), width / 2, 16, 0xFFFFFFFF);
    }

    @Override
    public void onClose() {
        ModuleManager.save();
        minecraft.setScreen(parent);
    }
}
