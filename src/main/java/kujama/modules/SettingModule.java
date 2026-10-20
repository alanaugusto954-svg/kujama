package kujama.modules;

import kujama.Category;
import kujama.Module;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.world.item.Item;

import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * Kujama Boost: al activarlo baja una opcion de video a su valor liviano; al desactivarlo
 * (o al cerrar el juego) devuelve el valor original que tenias.
 */
public class SettingModule<T> extends Module {
    private final Function<Options, OptionInstance<T>> option;
    private final UnaryOperator<T> optimizer;
    private T previous;
    private boolean applied;

    public SettingModule(String name, Item icon, Function<Options, OptionInstance<T>> option, UnaryOperator<T> optimizer) {
        super(name, Category.OPTIMIZACION, icon, false);
        this.option = option;
        this.optimizer = optimizer;
    }

    @Override
    public void onEnable() {
        OptionInstance<T> o = option.apply(mc.options);
        previous = o.get();
        applied = true;
        o.set(optimizer.apply(previous));
    }

    @Override
    public void onDisable() {
        if (!applied) return;
        option.apply(mc.options).set(previous);
        applied = false;
    }
}
