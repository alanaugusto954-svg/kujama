package com.kujama09.widget;

import com.kujama09.anim.Colors;
import com.kujama09.anim.Easing;
import com.kujama09.config.KujamaConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Botón funcional (hereda de ClickableWidget: foco por teclado, narrador, tooltip y clic) con apariencia
 * propia: entrada deslizante escalonada, transición suave de color al pasar el cursor y desplazamiento.
 * Las transiciones usan el tiempo real transcurrido, no el número de fotogramas.
 */
public final class KujamaButton extends ClickableWidget {
    private static final int ICON_SIZE = 15;

    /** Acción al pulsar el botón. */
    @FunctionalInterface
    public interface Action {
        void run();
    }

    private final Action action;
    private final long showAtNanos;
    private final Identifier icon;
    private float hover;
    private long lastNanos;

    public KujamaButton(int x, int y, int width, int height, Text message, Action action, long showAtNanos) {
        this(x, y, width, height, message, null, action, showAtNanos);
    }

    /** Botón con un sprite de GUI de vanilla en lugar de texto (el texto se usa para el narrador). */
    public KujamaButton(int x, int y, int width, int height, Text message, Identifier icon,
                        Action action, long showAtNanos) {
        super(x, y, width, height, message);
        this.action = action;
        this.icon = icon;
        this.showAtNanos = showAtNanos;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        KujamaConfig config = KujamaConfig.get();
        float intensity = config.animationIntensity;
        boolean animate = intensity > 0.01f;

        long now = System.nanoTime();
        float dt = lastNanos == 0L ? 0f : Math.min(0.1f, (now - lastNanos) / 1_000_000_000f);
        lastNanos = now;

        float target = (this.active && (this.isHovered() || this.isFocused())) ? 1f : 0f;
        if (animate) {
            hover += (target - hover) * (1f - (float) Math.exp(-dt * 14f));
        } else {
            hover = target;
        }

        float appear = 1f;
        if (animate) {
            appear = Easing.easeOutCubic((now - showAtNanos) / 320_000_000f);
        }
        if (appear <= 0.02f) {
            return;
        }

        int w = this.getWidth();
        int h = this.getHeight();
        int x = this.getX() + Math.round((1f - appear) * -36f * intensity + 5f * hover * intensity);
        int y = this.getY();

        // Sombra
        context.fill(x + 3, y + 3, x + w + 3, y + h + 3, Colors.withAlpha(0x66000000, appear));

        int hoverTop = icon != null ? 0xFFFF9F1C : Colors.YELLOW;
        int hoverBottom = icon != null ? Colors.DEEP_ORANGE : Colors.ORANGE;
        int border = Colors.lerp(0xFF3E78D6, 0xFF111111, hover);
        int top = Colors.lerp(0xF2224A9A, hoverTop, hover);
        int bottom = Colors.lerp(0xF20F2557, hoverBottom, hover);
        if (!this.active) {
            border = 0xFF555555;
            top = 0xCC3A3A3A;
            bottom = 0xCC262626;
        }
        context.fill(x, y, x + w, y + h, Colors.withAlpha(border, appear));
        context.fillGradient(x + 2, y + 2, x + w - 2, y + h - 2,
                Colors.withAlpha(top, appear), Colors.withAlpha(bottom, appear));
        context.fill(x + 2, y + 2, x + w - 2, y + 3, Colors.withAlpha(0x40FFFFFF, appear));

        if (icon != null) {
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, icon, x + (w - ICON_SIZE) / 2,
                    y + (h - ICON_SIZE) / 2, ICON_SIZE, ICON_SIZE, appear * (this.active ? 1f : 0.5f));
            return;
        }

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        Text message = this.getMessage();
        int textColor = this.active ? Colors.lerp(Colors.WHITE, 0xFF101010, hover) : 0xFFA0A0A0;
        int textWidth = textRenderer.getWidth(message);
        context.drawText(textRenderer, message, x + (w - textWidth) / 2, y + (h - 8) / 2,
                Colors.withAlpha(textColor, appear), hover < 0.5f);
    }

    @Override
    public void onClick(Click click, boolean doubled) {
        action.run();
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (!this.active || !this.visible) {
            return false;
        }
        if (input.isEnterOrSpace()) {
            this.playDownSound(MinecraftClient.getInstance().getSoundManager());
            action.run();
            return true;
        }
        return false;
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        builder.put(NarrationPart.TITLE, this.getNarrationMessage());
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
        if (KujamaConfig.get().menuSounds) {
            super.playDownSound(soundManager);
        }
    }
}
