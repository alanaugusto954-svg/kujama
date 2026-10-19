package com.kujama09.screen;

import com.kujama09.anim.Colors;
import com.kujama09.config.KujamaConfig;
import com.kujama09.render.Backdrop;
import com.kujama09.render.LogoRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;

/** Pantalla "KUJAMA09 Settings". Los cambios se guardan en config/kujama09.json al cerrarla. */
public final class KujamaSettingsScreen extends Screen {
    private static final int COL_W = 150;
    private static final int GAP = 8;
    private static final int ROW_H = 20;
    private static final int ROW_STEP = 24;

    private final Screen parent;
    private final long openNanos = System.nanoTime();

    public KujamaSettingsScreen(Screen parent) {
        super(Text.translatable("kujama09.settings.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        KujamaConfig config = KujamaConfig.get();
        int leftX = this.width / 2 - COL_W - GAP / 2;
        int rightX = this.width / 2 + GAP / 2;
        int y0 = 56;

        // Columna izquierda
        this.addDrawableChild(CyclingButtonWidget.onOffBuilder(config.introEnabled).build(
                leftX, y0, COL_W, ROW_H, Text.translatable("kujama09.settings.intro"),
                (button, value) -> config.introEnabled = value));

        this.addDrawableChild(new ValueSlider(leftX, y0 + ROW_STEP, COL_W, ROW_H,
                2.0, 8.0, config.introDurationSeconds,
                v -> Text.translatable("kujama09.settings.intro_duration", String.format("%.1f", snapHalf(v))),
                v -> config.introDurationSeconds = (float) snapHalf(v)));

        this.addDrawableChild(CyclingButtonWidget.onOffBuilder(config.particles).build(
                leftX, y0 + 2 * ROW_STEP, COL_W, ROW_H, Text.translatable("kujama09.settings.particles"),
                (button, value) -> config.particles = value));

        this.addDrawableChild(CyclingButtonWidget.<KujamaConfig.Quality>builder(
                        q -> Text.translatable(q.translationKey))
                .values(KujamaConfig.Quality.values())
                .initially(config.quality)
                .build(leftX, y0 + 3 * ROW_STEP, COL_W, ROW_H, Text.translatable("kujama09.settings.quality"),
                        (button, value) -> config.quality = value));

        // Columna derecha
        this.addDrawableChild(CyclingButtonWidget.onOffBuilder(config.introSound).build(
                rightX, y0, COL_W, ROW_H, Text.translatable("kujama09.settings.intro_sound"),
                (button, value) -> config.introSound = value));

        this.addDrawableChild(CyclingButtonWidget.onOffBuilder(config.menuSounds).build(
                rightX, y0 + ROW_STEP, COL_W, ROW_H, Text.translatable("kujama09.settings.menu_sounds"),
                (button, value) -> config.menuSounds = value));

        this.addDrawableChild(new ValueSlider(rightX, y0 + 2 * ROW_STEP, COL_W, ROW_H,
                0.0, 1.0, config.animationIntensity,
                v -> Text.translatable("kujama09.settings.intensity", Long.toString(Math.round(v * 100.0))),
                v -> config.animationIntensity = (float) v));

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("kujama09.settings.reset"), b -> {
            KujamaConfig.get().resetToDefaults();
            KujamaConfig.save();
            this.client.setScreen(new KujamaSettingsScreen(parent));
        }).dimensions(rightX, y0 + 3 * ROW_STEP, COL_W, ROW_H).build());

        // Fila inferior
        int bottomY = y0 + 4 * ROW_STEP + 8;
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("kujama09.settings.preview"), b -> {
            KujamaConfig.save();
            this.client.setScreen(new IntroScreen(new KujamaSettingsScreen(parent)));
        }).dimensions(leftX, bottomY, COL_W, ROW_H).build());

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), b -> this.close())
                .dimensions(rightX, bottomY, COL_W, ROW_H).build());
    }

    private static double snapHalf(double v) {
        return Math.round(v * 2.0) / 2.0;
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }

    @Override
    public void removed() {
        KujamaConfig.save();
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        KujamaConfig config = KujamaConfig.get();
        float t = (System.nanoTime() - openNanos) / 1_000_000_000f;
        Backdrop.drawScene(context, this.width, this.height, t, 1f, config.animationIntensity, config.quality, true);
        context.fill(0, 0, this.width, this.height, 0xB0050B1F);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        LogoRenderer.draw(context, this.textRenderer, this.width / 2f, 20f, 2f, 1f, -1f);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 36, Colors.YELLOW);
    }

    /** Slider genérico: mapea 0..1 a [min, max] y notifica el valor real. */
    private static final class ValueSlider extends SliderWidget {
        private final double min;
        private final double max;
        private final DoubleFunction<Text> label;
        private final DoubleConsumer onChange;

        ValueSlider(int x, int y, int width, int height, double min, double max, double current,
                    DoubleFunction<Text> label, DoubleConsumer onChange) {
            super(x, y, width, height, Text.empty(), (current - min) / (max - min));
            this.min = min;
            this.max = max;
            this.label = label;
            this.onChange = onChange;
            this.updateMessage();
        }

        private double real() {
            return min + this.value * (max - min);
        }

        @Override
        protected void updateMessage() {
            if (label != null) {
                this.setMessage(label.apply(real()));
            }
        }

        @Override
        protected void applyValue() {
            onChange.accept(real());
        }
    }
}
