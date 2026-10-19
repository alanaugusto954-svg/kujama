package com.kujama09;

import com.kujama09.config.KujamaConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Punto de entrada del cliente. Solo carga la configuración y registra el evento que
 * marca la intro como "ya mostrada" si el jugador entra a un mundo/servidor antes de
 * ver el menú (por ejemplo con quick play desde el launcher).
 */
public final class Kujama09Client implements ClientModInitializer {
    public static final String MOD_ID = "kujama09";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        KujamaConfig.load();
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> IntroState.markShown());
        LOGGER.info("kujama09 inicializado");
    }
}
