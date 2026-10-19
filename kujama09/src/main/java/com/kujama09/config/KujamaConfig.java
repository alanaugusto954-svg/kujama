package com.kujama09.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kujama09.Kujama09Client;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Configuración persistente, guardada en {@code config/kujama09.json}. */
public final class KujamaConfig {

    public enum Quality {
        LOW("kujama09.quality.low"),
        MEDIUM("kujama09.quality.medium"),
        HIGH("kujama09.quality.high");

        public final String translationKey;

        Quality(String translationKey) {
            this.translationKey = translationKey;
        }
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("kujama09.json");
    private static KujamaConfig instance = new KujamaConfig();

    public boolean introEnabled = true;
    public float introDurationSeconds = 4.5f;
    public boolean particles = true;
    /** Sonido de la intro: desactivado por defecto. */
    public boolean introSound = false;
    /** Sonidos de clic de los botones personalizados. */
    public boolean menuSounds = true;
    /** 0 = sin animaciones decorativas, 1 = intensidad completa. */
    public float animationIntensity = 1.0f;
    public Quality quality = Quality.MEDIUM;

    public static KujamaConfig get() {
        return instance;
    }

    public static void load() {
        KujamaConfig loaded = null;
        if (Files.isRegularFile(FILE)) {
            try {
                loaded = GSON.fromJson(Files.readString(FILE, StandardCharsets.UTF_8), KujamaConfig.class);
            } catch (Exception e) {
                Kujama09Client.LOGGER.warn("No se pudo leer {}, se usan valores por defecto", FILE, e);
            }
        }
        instance = loaded != null ? loaded : new KujamaConfig();
        instance.sanitize();
        save();
    }

    public static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(instance), StandardCharsets.UTF_8);
        } catch (IOException e) {
            Kujama09Client.LOGGER.warn("No se pudo guardar {}", FILE, e);
        }
    }

    public void resetToDefaults() {
        KujamaConfig d = new KujamaConfig();
        introEnabled = d.introEnabled;
        introDurationSeconds = d.introDurationSeconds;
        particles = d.particles;
        introSound = d.introSound;
        menuSounds = d.menuSounds;
        animationIntensity = d.animationIntensity;
        quality = d.quality;
    }

    private void sanitize() {
        if (Float.isNaN(introDurationSeconds)) introDurationSeconds = 4.5f;
        introDurationSeconds = Math.max(2.0f, Math.min(8.0f, introDurationSeconds));
        if (Float.isNaN(animationIntensity)) animationIntensity = 1.0f;
        animationIntensity = Math.max(0.0f, Math.min(1.0f, animationIntensity));
        if (quality == null) quality = Quality.MEDIUM;
    }
}
