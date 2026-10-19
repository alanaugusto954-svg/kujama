package kujama;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.GuiGraphics;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public final class ModuleManager {
    private static final List<Module> MODULES = new ArrayList<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /** Pasa a true en el primer tick del cliente. */
    public static boolean started;

    private static int frames;

    /** Cantidad de cuadros dibujados desde que arranco el juego (para medir FPS). */
    public static int frames() { return frames; }

    private ModuleManager() {}

    public static void register(Module m) { MODULES.add(m); }
    public static List<Module> all() { return MODULES; }

    /** Busca un modulo por su nombre (o null). */
    public static Module find(String name) {
        for (Module m : MODULES) if (m.name.equals(name)) return m;
        return null;
    }

    public static boolean isOn(String name) {
        Module m = find(name);
        return m != null && m.enabled;
    }

    public static void tick() {
        if (!started) {
            started = true;
            for (Module m : MODULES) if (m.enabled) m.onEnable();
        }
        for (Module m : MODULES) if (m.enabled) Safe.run("tick:" + m.name, m::onTick);
    }

    public static void render(GuiGraphics g) {
        frames++;
        for (Module m : MODULES) {
            if (m.enabled && m instanceof HudModule h) {
                h.beginFrame();
                Safe.run("hud:" + h.name, () -> h.render(g));
            }
        }
    }

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("kujama.json");
    }

    public static void load() {
        Path p = path();
        if (!Files.exists(p)) return;
        try {
            JsonObject root = JsonParser.parseString(Files.readString(p)).getAsJsonObject();
            for (Module m : MODULES) {
                if (!root.has(m.name)) continue;
                JsonObject o = root.getAsJsonObject(m.name);
                if (o.has("enabled")) m.setEnabled(o.get("enabled").getAsBoolean());
                if (m instanceof HudModule h) {
                    if (o.has("x")) h.x = o.get("x").getAsInt();
                    if (o.has("y")) h.y = o.get("y").getAsInt();
                }
            }
        } catch (Exception e) {
            System.err.println("[Kujama] No pude leer la config: " + e);
        }
    }

    public static void save() {
        JsonObject root = new JsonObject();
        for (Module m : MODULES) {
            JsonObject o = new JsonObject();
            o.addProperty("enabled", m.enabled);
            if (m instanceof HudModule h) {
                o.addProperty("x", h.x);
                o.addProperty("y", h.y);
            }
            root.add(m.name, o);
        }
        try {
            Files.writeString(path(), GSON.toJson(root));
        } catch (IOException e) {
            System.err.println("[Kujama] No pude guardar la config: " + e);
        }
    }

    /** Al cerrar el juego: guarda y deja todo como estaba (por ejemplo, restaura el FOV). */
    public static void shutdown() {
        save();
        if (!started) return;
        for (Module m : MODULES) if (m.enabled) m.onDisable();
    }
}
