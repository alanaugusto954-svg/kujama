package kujama.modules;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.item.Item;

import java.util.concurrent.ThreadLocalRandom;

/** Aura de particulas alrededor del jugador: espiral que sube, o chispas que salen del cuerpo. */
public class AuraModule extends ParticleCosmetic {
    private final ParticleOptions type;
    private final boolean sparks;

    public AuraModule(String name, Item icon, ParticleOptions type, boolean sparks) {
        super(name, icon);
        this.type = type;
        this.sparks = sparks;
    }

    @Override
    public void onTick() {
        if (!active()) return;
        ticks++;
        double cx = mc.player.getX(), cy = mc.player.getY(), cz = mc.player.getZ();

        if (!sparks) {
            for (int k = 0; k < 3; k++) {
                double a = ticks * 0.30 + k * Math.PI * 2 / 3;
                double h = (ticks * 0.04 + k * 0.66) % 2.0;
                emit(type, cx + Math.cos(a) * 0.75, cy + h, cz + Math.sin(a) * 0.75, 0, 0.01, 0, 8);
            }
        } else {
            ThreadLocalRandom r = ThreadLocalRandom.current();
            for (int k = 0; k < 2; k++) {
                double a = r.nextDouble() * Math.PI * 2;
                double b = (r.nextDouble() - 0.5) * Math.PI;
                double dx = Math.cos(b) * Math.cos(a), dy = Math.sin(b), dz = Math.cos(b) * Math.sin(a);
                emit(type, cx + dx * 0.9, cy + 1.0 + dy * 0.9, cz + dz * 0.9, dx * 0.03, dy * 0.03, dz * 0.03, 6);
            }
        }
    }
}
