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
 * Alas de murcielago con geometria propia, pegadas a la espalda del jugador.
 * Membrana con 4 dedos y borde festoneado, sombreada (mas clara cerca del hombro, mas oscura en los bordes).
 * Se animan con WingMotion: aletean, se abren, se pliegan al agacharte y se extienden al planear.
 */
public class KujamaWingsLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
    /** Alas activas ahora mismo (las pone el modulo). */
    public static volatile ModelWingsModule current;

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(Kujama.ID, "textures/entity/wing.png");

    // Puntos del ala: {distancia lateral desde la espalda, altura sobre los pies}
    private static final double[] S = {0.12, 1.38};                                   // hombro
    private static final double[][] TIPS = {{1.55, 2.10}, {1.85, 1.55}, {1.50, 0.95}, {0.95, 0.55}};
    private static final double[] B = {0.14, 0.80};                                   // union con el cuerpo
    private static final double V0 = S[1];

    // Borde de la membrana: punta, hueco, punta, hueco, punta, hueco, punta, cuerpo
    private static final double[][] RIM = new double[8][];
    private static final float[] SHADE = {0.85f, 0.60f, 0.85f, 0.60f, 0.85f, 0.60f, 0.85f, 1.0f};

    static {
        RIM[0] = TIPS[0];
        RIM[2] = TIPS[1];
        RIM[4] = TIPS[2];
        RIM[6] = TIPS[3];
        RIM[7] = B;
        for (int i = 0; i < 3; i++) {
            double mx = (TIPS[i][0] + TIPS[i + 1][0]) / 2, my = (TIPS[i][1] + TIPS[i + 1][1]) / 2;
            RIM[1 + 2 * i] = new double[]{mx + (S[0] - mx) * 0.28, my + (S[1] - my) * 0.28}; // hueco hacia el hombro
        }
    }

    /** Parametros de la pose del ala en este cuadro. */
    private record Pose(double cosT, double sinT, double cosP, double sinP, double scale) {}

    public KujamaWingsLayer(RenderLayerParent<AvatarRenderState, PlayerModel> parent) {
        super(parent);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light,
                       AvatarRenderState state, float yRot, float xRot) {
        ModelWingsModule wings = current;
        Minecraft mc = Minecraft.getInstance();
        if (wings == null || mc.player == null || state.isInvisible || Safe.disabled("wings")) return;

        // solo para el jugador local (los demas no tienen tus alas)
        double dx = state.x - mc.player.getX(), dy = state.y - mc.player.getY(), dz = state.z - mc.player.getZ();
        if (dx * dx + dy * dy + dz * dz > 1.0) return;

        float partial = state.ageInTicks - (float) Math.floor(state.ageInTicks);
        float phase = Anim.lerp(WingMotion.prevPhase, WingMotion.phase, partial);
        float open = Anim.lerp(WingMotion.prevOpen, WingMotion.open, partial);
        float amp = Anim.lerp(WingMotion.prevAmp, WingMotion.amp, partial);

        double theta = Math.toRadians(amp * 34.0 * Math.sin(phase));   // aleteo arriba/abajo
        double phi = Math.toRadians(70.0 - 40.0 * open);               // barrido hacia atras: plegada -> abierta
        double scale = 0.55 + 0.45 * open;                             // plegada = mas chica
        Pose pose = new Pose(Math.cos(theta), Math.sin(theta), Math.cos(phi), Math.sin(phi), scale);
        int membrane = wings.membraneRgb, bone = wings.boneRgb;

        collector.submitCustomGeometry(poseStack, RenderTypes.entityCutoutNoCull(TEXTURE),
                (p, buffer) -> Safe.run("wings", () -> drawWings(p, buffer, pose, membrane, bone, light)));
    }

    private static void drawWings(PoseStack.Pose p, VertexConsumer buf, Pose k, int membrane, int bone, int light) {
        for (int side = -1; side <= 1; side += 2) {
            double[] s = map(S, side, k, 0.0);
            int cs = shade(membrane, 1.20f);

            // membrana: abanico de triangulos desde el hombro
            for (int i = 0; i + 1 < RIM.length; i++) {
                double[] a = map(RIM[i], side, k, 0.0), b = map(RIM[i + 1], side, k, 0.0);
                int ca = shade(membrane, SHADE[i]), cb = shade(membrane, SHADE[i + 1]);
                quad(p, buf, s, cs, a, ca, b, cb, b, cb, light);
            }

            // dedos (hueso con una pequeña curva hacia arriba)
            for (double[] tip : TIPS) {
                double[] mid = {(S[0] + tip[0]) / 2.0, (S[1] + tip[1]) / 2.0 + 0.10};
                ribbon(p, buf, S, mid, 0.026, side, k, 0.010, bone, light);
                ribbon(p, buf, mid, tip, 0.018, side, k, 0.010, bone, light);
            }
            // borde oscuro de la membrana
            for (int i = 0; i + 1 < RIM.length; i++) {
                ribbon(p, buf, RIM[i], RIM[i + 1], 0.012, side, k, 0.006, bone, light);
            }
            ribbon(p, buf, B, S, 0.012, side, k, 0.006, bone, light);
        }
    }

    /** Punto del ala (lateral, altura) -> espacio del modelo del jugador (y hacia abajo, z hacia atras). */
    private static double[] map(double[] pt, int side, Pose k, double zOff) {
        double xw = pt[0] * k.scale(), yw = pt[1] - V0;
        double xp = xw * k.cosT() - yw * k.sinT();     // aleteo: gira alrededor del eje de la espalda
        double yp = xw * k.sinT() + yw * k.cosT();
        return new double[]{side * xp * k.cosP(), 1.501 - (V0 + yp), 0.16 + xp * k.sinP() + zOff};
    }

    private static void ribbon(PoseStack.Pose p, VertexConsumer buf, double[] from, double[] to, double half,
                               int side, Pose k, double zOff, int rgb, int light) {
        double[] a = map(new double[]{from[0], from[1] - half}, side, k, zOff);
        double[] b = map(new double[]{to[0], to[1] - half}, side, k, zOff);
        double[] c = map(new double[]{to[0], to[1] + half}, side, k, zOff);
        double[] d = map(new double[]{from[0], from[1] + half}, side, k, zOff);
        quad(p, buf, a, rgb, b, rgb, c, rgb, d, rgb, light);
    }

    private static int shade(int rgb, float f) {
        int r = Math.min(255, (int) (((rgb >> 16) & 255) * f));
        int g = Math.min(255, (int) (((rgb >> 8) & 255) * f));
        int b = Math.min(255, (int) ((rgb & 255) * f));
        return (r << 16) | (g << 8) | b;
    }

    private static void quad(PoseStack.Pose p, VertexConsumer buf, double[] a, int ca, double[] b, int cb,
                             double[] c, int cc, double[] d, int cd, int light) {
        vertex(p, buf, a, ca, light);
        vertex(p, buf, b, cb, light);
        vertex(p, buf, c, cc, light);
        vertex(p, buf, d, cd, light);
    }

    private static void vertex(PoseStack.Pose p, VertexConsumer buf, double[] pt, int rgb, int light) {
        buf.addVertex(p, (float) pt[0], (float) pt[1], (float) pt[2])
                .setColor((rgb >> 16) & 255, (rgb >> 8) & 255, rgb & 255, 255)
                .setUv(0.5f, 0.5f)
                .setOverlay(655360) // sin overlay de dano (equivale a OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(p, 0f, 0f, 1f);
    }
}
