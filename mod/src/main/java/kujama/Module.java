package kujama;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Item;

/** Base de todos los modulos. */
public abstract class Module {
    public final String name;
    public final Category category;
    public final Item icon;
    public String description = "";
    public boolean enabled;
    protected final Minecraft mc = Minecraft.getInstance();

    protected Module(String name, Category category, Item icon, boolean enabledByDefault) {
        this.name = name;
        this.category = category;
        this.icon = icon;
        this.enabled = enabledByDefault;
    }

    public Module desc(String text) {
        this.description = text;
        return this;
    }

    public void onTick() {}
    public void onEnable() {}
    public void onDisable() {}

    public void setEnabled(boolean value) {
        if (value == enabled) return;
        enabled = value;
        if (!ModuleManager.started) return; // se aplica en el primer tick, cuando las opciones ya existen
        if (value) onEnable(); else onDisable();
    }

    public void toggle() { setEnabled(!enabled); }
}
