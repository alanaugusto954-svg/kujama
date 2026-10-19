package com.kujama09;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Estado estático (vive mientras el proceso de Minecraft esté abierto) que garantiza
 * que la intro se decida una sola vez por inicio completo del cliente.
 */
public final class IntroState {
    private static final AtomicBoolean SHOWN = new AtomicBoolean(false);

    private IntroState() {
    }

    /** Devuelve true solo la primera vez que se llama en este inicio del cliente. */
    public static boolean tryBegin() {
        return SHOWN.compareAndSet(false, true);
    }

    /** Marca la intro como ya consumida (por ejemplo al unirse a un mundo o servidor). */
    public static void markShown() {
        SHOWN.set(true);
    }
}
