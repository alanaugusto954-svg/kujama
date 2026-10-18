package kujama;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Pantalla para mover un elemento del HUD con flechas (se ve el HUD real de fondo). */
public class HudMoveScreen extends Screen {
    private final Screen parent;
    private final HudModule module;

    public HudMoveScreen(Screen parent, HudModule module) {
        super(Component.literal("Mover " + module.name));
        this.parent = parent;
        this.module = module;
    }

    @Override
    protected void init() {
        int cx = width / 2, cy = height / 2;
        addRenderableWidget(Button.builder(Component.literal("^"), b -> module.y -= 4).bounds(cx - 10, cy - 34, 20, 20).build());
        addRenderableWidget(Button.builder(Component.literal("v"), b -> module.y += 4).bounds(cx - 10, cy - 6, 20, 20).build());
        addRenderableWidget(Button.builder(Component.literal("<"), b -> module.x -= 4).bounds(cx - 34, cy - 20, 20, 20).build());
        addRenderableWidget(Button.builder(Component.literal(">"), b -> module.x += 4).bounds(cx + 14, cy - 20, 20, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Volver"), b -> onClose()).bounds(cx - 40, cy + 24, 80, 20).build());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        super.render(g, mouseX, mouseY, delta);
        g.drawCenteredString(font, "Mover: " + module.name, width / 2, height / 2 - 58, 0xFF1BD96A);
    }

    @Override
    public void onClose() {
        ModuleManager.save();
        minecraft.setScreen(parent);
    }
}
