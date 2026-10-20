package com.kujama09.screen;

import com.kujama09.Kujama09Client;
import com.kujama09.anim.Colors;
import com.kujama09.anim.Easing;
import com.kujama09.anim.ParticleField;
import com.kujama09.config.KujamaConfig;
import com.kujama09.render.Backdrop;
import com.kujama09.render.LogoRenderer;
import com.kujama09.widget.KujamaButton;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerWarningScreen;
import net.minecraft.client.gui.screen.option.AccessibilityOptionsScreen;
import net.minecraft.client.gui.screen.option.CreditsAndAttributionScreen;
import net.minecraft.client.gui.screen.option.LanguageOptionsScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Menú principal personalizado. Sustituye visualmente al TitleScreen de vanilla y abre las pantallas
 * reales de Minecraft. Conserva los accesos de idioma y accesibilidad, la versión del juego y el aviso
 * legal de copyright (que abre la pantalla oficial de créditos y atribuciones).
 */
public final class KujamaTitleScreen extends Screen {
    private static final Identifier LANGUAGE_ICON = Identifier.of("minecraft", "icon/language");
    private static final Identifier ACCESSIBILITY_ICON = Identifier.of("minecraft", "icon/accessibility");
    private static final long MS = 1_000_000L;

    private final boolean fadeFromBlack;
    private final long openNanos = System.nanoTime();
    private final ParticleField particles = new ParticleField();

    private boolean mascotVisible;
    private int mascotX;
    private int mascotY;
    private int mascotSize;
    private float logoCx;
    private float logoCy;
    private float logoScale;

    public KujamaTitleScreen(boolean fadeFromBlack) {
        super(Text.translatable("narrator.screen.title"));
        this.fadeFromBlack = fadeFromBlack;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    private float elapsed() {
        return (System.nanoTime() - openNanos) / 1_000_000_000f;
    }

    @Override
    protected void init() {
        boolean showMods = FabricLoader.getInstance().isModLoaded("modmenu");
        int rows = showMods ? 6 : 5;
        int bh = 22;
        int gap = 5;
        int bw = this.width < 200 ? this.width - 20 : Math.max(150, Math.min(220, Math.round(this.width * 0.30f)));

        mascotVisible = this.width >= 520 && this.height >= 240;
        int cx = mascotVisible ? Math.min(Math.round(this.width * 0.64f), this.width - bw / 2 - 12) : this.width / 2;

        int textW = LogoRenderer.textWidth(this.textRenderer);
        logoScale = Math.max(2f, Math.min(5f, bw * 1.25f / textW));
        int logoH = Math.round(9f * logoScale);
        int total = logoH + 14 + rows * bh + (rows - 1) * gap;
        if (total > this.height - 20) {
            logoScale = 2f;
            gap = 2;
            logoH = Math.round(9f * logoScale);
            total = logoH + 10 + rows * bh + (rows - 1) * gap;
        }
        int top = Math.max(8, (this.height - total) / 2 - 6);
        logoCx = cx;
        logoCy = top + logoH / 2f;
        int y = top + logoH + 14;
        int left = cx - bw / 2;

        long delay = 0L;
        final long step = 70L * MS;

        this.addDrawableChild(new KujamaButton(left, y, bw, bh, Text.translatable("menu.singleplayer"),
                () -> this.client.setScreen(new SelectWorldScreen(this)), openNanos + delay));
        y += bh + gap;
        delay += step;

        KujamaButton multiplayer = this.addDrawableChild(new KujamaButton(left, y, bw, bh,
                Text.translatable("menu.multiplayer"), () -> openMultiplayer(), openNanos + delay));
        multiplayer.active = this.client.isMultiplayerEnabled();
        y += bh + gap;
        delay += step;

        if (showMods) {
            this.addDrawableChild(new KujamaButton(left, y, bw, bh, Text.translatable("kujama09.menu.mods"),
                    () -> openModMenu(), openNanos + delay));
            y += bh + gap;
            delay += step;
        }

        // Fila: [idioma] [Minecraft Settings] [accesibilidad]
        int icon = bh;
        KujamaButton language = this.addDrawableChild(new KujamaButton(left, y, icon, bh,
                Text.translatable("narrator.button.language"), LANGUAGE_ICON,
                () -> this.client.setScreen(new LanguageOptionsScreen(this, this.client.options,
                        this.client.getLanguageManager())), openNanos + delay));
        language.setTooltip(Tooltip.of(Text.translatable("narrator.button.language")));
        this.addDrawableChild(new KujamaButton(left + icon + gap, y, bw - 2 * (icon + gap), bh,
                Text.translatable("kujama09.menu.minecraft_settings"),
                () -> this.client.setScreen(new OptionsScreen(this, this.client.options)), openNanos + delay));
        KujamaButton accessibility = this.addDrawableChild(new KujamaButton(left + bw - icon, y, icon, bh,
                Text.translatable("narrator.button.accessibility"), ACCESSIBILITY_ICON,
                () -> this.client.setScreen(new AccessibilityOptionsScreen(this, this.client.options)),
                openNanos + delay));
        accessibility.setTooltip(Tooltip.of(Text.translatable("narrator.button.accessibility")));
        y += bh + gap;
        delay += step;

        this.addDrawableChild(new KujamaButton(left, y, bw, bh, Text.translatable("kujama09.menu.settings"),
                () -> this.client.setScreen(new KujamaSettingsScreen(this)), openNanos + delay));
        y += bh + gap;
        delay += step;

        this.addDrawableChild(new KujamaButton(left, y, bw, bh, Text.translatable("menu.quit"),
                () -> this.client.scheduleStop(), openNanos + delay));

        // Aviso legal: abre la pantalla oficial de créditos y atribuciones, igual que vanilla.
        Text credits = Text.translatable("title.credits");
        int creditsWidth = this.textRenderer.getWidth(credits) + 8;
        this.addDrawableChild(new KujamaButton(this.width - creditsWidth - 2, this.height - 14,
                creditsWidth, 12, credits,
                () -> this.client.setScreen(new CreditsAndAttributionScreen(this)), openNanos));

        // Posición de la mascota (a la izquierda de la columna de botones).
        mascotSize = Math.min(Math.round(this.height * 0.66f), Math.round(this.width * 0.34f));
        mascotX = Math.max(8, Math.round(this.width * 0.06f));
        mascotY = this.height - mascotSize - Math.round(this.height * 0.03f);
    }

    private void openMultiplayer() {
        Screen screen = this.client.options.skipMultiplayerWarning
                ? new MultiplayerScreen(this)
                : new MultiplayerWarningScreen(this);
        this.client.setScreen(screen);
    }

    /** Mod Menu es opcional: se abre por reflexión para no depender de él al compilar. */
    private void openModMenu() {
        try {
            Class<?> modsScreen = Class.forName("com.terraformersmc.modmenu.gui.ModsScreen");
            Screen screen = (Screen) modsScreen.getConstructor(Screen.class).newInstance(this);
            this.client.setScreen(screen);
        } catch (ReflectiveOperationException | ClassCastException e) {
            Kujama09Client.LOGGER.warn("No se pudo abrir Mod Menu", e);
        }
    }

    /** Escenario + partículas + mascota. Quedan por debajo de los botones. */
    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        KujamaConfig config = KujamaConfig.get();
        float t = elapsed();
        float intensity = config.animationIntensity;
        Backdrop.drawScene(context, this.width, this.height, t, 1f, intensity, config.quality, true);
        Backdrop.drawParticles(context, particles, this.width, this.height, config, 1f);

        if (mascotVisible) {
            boolean effects = intensity > 0.01f;
            float enter = effects ? Easing.easeOutCubic(t / 0.8f) : 1f;
            float bob = effects ? (float) Math.sin(t * 1.6f) * 4f * intensity : 0f;
            int slide = Math.round((1f - enter) * -mascotSize * 0.4f);
            Backdrop.drawMascot(context, mascotX + slide, Math.round(mascotY + bob), mascotSize, enter);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        KujamaConfig config = KujamaConfig.get();
        float intensity = config.animationIntensity;
        boolean effects = intensity > 0.01f;
        float t = elapsed();

        super.render(context, mouseX, mouseY, delta);

        // Logotipo con flotación suave y un brillo que lo recorre cada pocos segundos.
        float logoAlpha = effects ? Easing.smooth(t / 0.5f) : 1f;
        float floatY = effects ? (float) Math.sin(t * 1.8f) * 2f * intensity : 0f;
        float shine = -1f;
        if (effects) {
            float cycle = t % 6f;
            if (cycle < 1.2f) {
                shine = Easing.easeInOutCubic(cycle / 1.2f);
            }
        }
        LogoRenderer.draw(context, this.textRenderer, logoCx, logoCy + floatY, logoScale, logoAlpha, shine);

        // Versión del juego (abajo a la izquierda, como en vanilla).
        String version = "Minecraft " + this.client.getGameVersion() + " / Fabric";
        context.drawTextWithShadow(this.textRenderer, version, 2, this.height - 10, 0xFFFFFFFF);

        // Transición desde la intro: fundido de negro a menú.
        if (fadeFromBlack) {
            float black = 1f - Easing.smooth(t / 0.7f);
            if (black > 0.01f) {
                context.fill(0, 0, this.width, this.height, Colors.withAlpha(Colors.BLACK, black));
            }
        }
    }
}
