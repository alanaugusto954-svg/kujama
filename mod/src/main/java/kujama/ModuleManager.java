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

    private ModuleManager() {}

    public static void register(Module m) { MODULES.add(m); }
    public static List<Module> all() { return MODULES; }

    public static void tick() {
        for (Module m : MODULES) if (m.enabled) m.onTick();
    }

    public static void render(GuiGraphics g) {
        for (Module m : MODULES) if (m.enabled && m instanceof HudModule h) h.render(g);
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
        for (Module m : MODULES) if (m.enabled) m.onDisable();
    }
}
