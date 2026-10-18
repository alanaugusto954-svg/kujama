package kujama;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

/**
 * Alas con forma real (geometria propia), pegadas a la espalda del jugador.
 * Usa la misma "capa" que usan las alas de elitros, la capa o las orejas de Deadmau5.
 * Fisica simple: las alas aletean suave y se abren mas cuanto mas rapido caminas; al agacharte se pliegan.
 */
public class KujamaWingsLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
    /** Alas activas ahora mismo (las pone el modulo). */
    public static volatile ModelWingsModule current;

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Kujama.ID, "textures/entity/wing.png");

    // Contorno del ala: {distancia lateral desde la espalda, altura sobre los pies}
    private static final double[][] OUTLINE = {
            {0.15, 1.35}, {0.45, 1.55}, {0.80, 1.75}, {1.15, 1.90}, {1.45, 1.95}, {1.35, 1.70}, {1.45, 1.45},
            {1.25, 1.30}, {1.30, 1.05}, {1.05, 0.95}, {1.05, 0.75}, {0.80, 0.75}, {0.65, 0.60}, {0.45, 0.80}, {0.20, 0.85}
    };
    // "Huesos" del ala
    private static final double[][][] BONES = {
            {{0.15, 1.35}, {1.45, 1.95}},
            {{0.15, 1.35}, {1.30, 1.05}},
            {{0.15, 1.35}, {0.80, 0.75}}
    };

    public KujamaWingsLayer(RenderLayerParent<AvatarRenderState, PlayerModel> parent) {
        super(parent);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light,
                       AvatarRenderState state, float yRot, float xRot) {
        ModelWingsModule wings = current;
        Minecraft mc = Minecraft.getInstance();
        if (wings == null || mc.player == null || state.isInvisible) return;

        // solo para el jugador local (los demas no tienen tus alas)
        double dx = state.x - mc.player.getX(), dy = state.y - mc.player.getY(), dz = state.z - mc.player.getZ();
        if (dx * dx + dy * dy + dz * dz > 1.0) return;

        float speed = Math.min(state.walkAnimationSpeed, 1.0f);
        double flap = Math.sin(state.ageInTicks * (0.12 + speed * 0.25));
        double openDeg = 48 + (state.isDiscrete ? 14 : 0) + flap * (3 + 22 * speed);
        double phi = Math.toRadians(openDeg);
        double cph = Math.cos(phi), sph = Math.sin(phi);
        int membrane = wings.membraneRgb, bone = wings.boneRgb;

        collector.submitCustomGeometry(poseStack, RenderTypes.entityCutoutNoCull(TEXTURE), (pose, buffer) -> {
            for (int side = -1; side <= 1; side += 2) {
                // membrana: abanico de triangulos desde el hombro
                double[] p0 = map(OUTLINE[0], side, cph, sph, 0.0);
                for (int i = 1; i + 1 < OUTLINE.length; i++) {
                    double[] a = map(OUTLINE[i], side, cph, sph, 0.0);
                    double[] b = map(OUTLINE[i + 1], side, cph, sph, 0.0);
                    quad(pose, buffer, p0, a, b, b, membrane, light);
                }
                // borde del ala
                for (int i = 0; i + 1 < OUTLINE.length; i++) {
                    ribbon(pose, buffer, OUTLINE[i], OUTLINE[i + 1], 0.02, side, cph, sph, 0.006, bone, light);
                }
                // huesos
                for (double[][] seg : BONES) {
                    ribbon(pose, buffer, seg[0], seg[1], 0.03, side, cph, sph, 0.010, bone, light);
                }
            }
        });
    }

    /** Pasa un punto del ala (lateral, altura) al espacio del modelo del jugador (y hacia abajo, z hacia atras). */
    private static double[] map(double[] p, int side, double cph, double sph, double zOff) {
        return new double[]{side * p[0] * cph, 1.501 - p[1], 0.16 + p[0] * sph + zOff};
    }

    private static void ribbon(PoseStack.Pose pose, VertexConsumer buffer, double[] from, double[] to, double half,
                               int side, double cph, double sph, double zOff, int rgb, int light) {
        double[] a = map(new double[]{from[0], from[1] - half}, side, cph, sph, zOff);
        double[] b = map(new double[]{to[0], to[1] - half}, side, cph, sph, zOff);
        double[] c = map(new double[]{to[0], to[1] + half}, side, cph, sph, zOff);
        double[] d = map(new double[]{from[0], from[1] + half}, side, cph, sph, zOff);
        quad(pose, buffer, a, b, c, d, rgb, light);
    }

    private static void quad(PoseStack.Pose pose, VertexConsumer buffer,
                             double[] a, double[] b, double[] c, double[] d, int rgb, int light) {
        vertex(pose, buffer, a, rgb, light);
        vertex(pose, buffer, b, rgb, light);
        vertex(pose, buffer, c, rgb, light);
        vertex(pose, buffer, d, rgb, light);
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer buffer, double[] p, int rgb, int light) {
        buffer.addVertex(pose, (float) p[0], (float) p[1], (float) p[2])
                .setColor((rgb >> 16) & 255, (rgb >> 8) & 255, rgb & 255, 255)
                .setUv(0.5f, 0.5f)
                .setOverlay(655360) // sin overlay de dano (equivale a OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, 0f, 0f, 1f);
    }
}
