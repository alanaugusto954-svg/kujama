package kujama.modules;

import kujama.Category;
import kujama.Module;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.item.Item;

/** Base de los cosmeticos hechos con particulas (solo los ves vos, es del lado del cliente). */
public abstract class ParticleCosmetic extends Module {
    protected int ticks;

    protected ParticleCosmetic(String name, Item icon) {
        super(name, Category.VISUAL, icon, false);
    }

    protected boolean active() {
        return mc.player != null && mc.level != null && !mc.isPaused();
    }

    /** Crea una particula con vida corta para que siga al jugador sin dejar estela. */
    protected void emit(ParticleOptions type, double x, double y, double z, double vx, double vy, double vz, int life) {
        Particle p = mc.particleEngine.createParticle(type, x, y, z, vx, vy, vz);
        if (p != null) p.setLifetime(life);
    }
}
