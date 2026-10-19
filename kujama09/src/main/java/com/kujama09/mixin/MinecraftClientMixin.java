package com.kujama09.mixin;

import com.kujama09.IntroState;
import com.kujama09.config.KujamaConfig;
import com.kujama09.screen.IntroScreen;
import com.kujama09.screen.KujamaTitleScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Cada vez que algo pide mostrar el menú principal vanilla (al iniciar, al salir de un mundo,
 * al desconectarse, al volver desde un submenú...) se sustituye por el menú de kujama09.
 *
 * Si es la primera vez en este inicio del cliente y la intro está activada, se muestra antes la
 * intro, que al terminar abre el menú personalizado. Como MinecraftClient#setScreen se invoca recién
 * cuando el juego ya terminó de preparar su interfaz, la intro empieza en el primer punto seguro.
 */
@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @ModifyVariable(method = "setScreen", at = @At("HEAD"), argsOnly = true)
    private Screen kujama09$replaceTitleScreen(Screen screen) {
        if (!(screen instanceof TitleScreen)) {
            return screen;
        }
        boolean first = IntroState.tryBegin();
        if (first && KujamaConfig.get().introEnabled) {
            return new IntroScreen(new KujamaTitleScreen(true));
        }
        return new KujamaTitleScreen(false);
    }
}
