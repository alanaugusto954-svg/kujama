package kujama;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Evita que un error en un efecto grafico cierre el juego: se registra en el log y,
 * si una funcion falla 3 veces, se desactiva hasta reiniciar.
 */
public final class Safe {
    private static final Logger LOG = LoggerFactory.getLogger("Kujama");
    private static final Map<String, Integer> FAILS = new HashMap<>();

    private Safe() {}

    public static boolean run(String feature, Runnable r) {
        if (disabled(feature)) return false;
        try {
            r.run();
            return true;
        } catch (Throwable t) {
            int n = FAILS.merge(feature, 1, Integer::sum);
            LOG.error("[Kujama] Error en '{}' (intento {}/3)", feature, n, t);
            return false;
        }
    }

    public static boolean disabled(String feature) {
        return FAILS.getOrDefault(feature, 0) >= 3;
    }
}
