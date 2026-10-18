package kujama.modules;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;

/** Alas dibujadas con particulas: contorno + "huesos", con aleteo suave. */
public class WingsModule extends ParticleCosmetic {
    // Contorno del ala derecha: {distancia desde la espalda, altura sobre los pies}
    private static final double[][] OUTLINE = {
            {0.15, 1.35}, {0.45, 1.55}, {0.80, 1.75}, {1.15, 1.90}, {1.45, 1.95}, {1.35, 1.70}, {1.45, 1.45},
            {1.25, 1.30}, {1.30, 1.05}, {1.05, 0.95}, {1.05, 0.75}, {0.80, 0.75}, {0.65, 0.60}, {0.45, 0.80}, {0.20, 0.85}
    };
    private static final double[][][] BONES = {
            {{0.15, 1.35}, {1.45, 1.95}},
            {{0.15, 1.35}, {1.30, 1.05}},
            {{0.15, 1.35}, {0.80, 0.75}}
    };
    private static final List<double[]> POINTS = new ArrayList<>();

    static {
        addPolyline(OUTLINE);
        for (double[][] bone : BONES) addPolyline(bone);
    }

    private static void addPolyline(double[][] pts) {
        for (int i = 0; i + 1 < pts.length; i++) {
            double dx = pts[i + 1][0] - pts[i][0], dy = pts[i + 1][1] - pts[i][1];
            int n = Math.max(1, (int) Math.ceil(Math.hypot(dx, dy) / 0.16));
            for (int k = 0; k < n; k++) {
                double t = k / (double) n;
                POINTS.add(new double[]{pts[i][0] + dx * t, pts[i][1] + dy * t});
            }
        }
    }

    private final ParticleOptions type;

    public WingsModule(String name, Item icon, ParticleOptions type) {
        super(name, icon);
        this.type = type;
    }

    @Override
    public void onTick() {
        if (!active()) return;
        ticks++;
        if (ticks % 2 != 0) return;

        double yaw = Math.toRadians(mc.player.yBodyRot);
        double rx = -Math.cos(yaw), rz = -Math.sin(yaw); // hacia la derecha del jugador
        double bx = Math.sin(yaw), bz = -Math.cos(yaw);  // hacia atras
        double phi = Math.toRadians(58 + 12 * Math.sin(ticks * 0.25)); // apertura + aleteo
        double cph = Math.cos(phi), sph = Math.sin(phi);

        double baseX = mc.player.getX() + bx * 0.2;
        double baseZ = mc.player.getZ() + bz * 0.2;
        double baseY = mc.player.getY() - (mc.player.isShiftKeyDown() ? 0.15 : 0.0);

        for (int side = -1; side <= 1; side += 2) {
            for (double[] p : POINTS) {
                double u = p[0], v = p[1];
                double x = baseX + (rx * side * cph + bx * sph) * u;
                double z = baseZ + (rz * side * cph + bz * sph) * u;
                emit(type, x, baseY + v, z, 0, 0, 0, 3);
            }
        }
    }
}
