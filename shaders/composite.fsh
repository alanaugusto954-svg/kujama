#version 120

// ============================================================================
//  Kujama Skies - cielos animados para Iris (Minecraft 1.21.x)
//  Reemplaza solo los pixeles de cielo (profundidad = 1.0); el terreno queda como en el juego.
//    1 = Aurora boreal sobre noche estrellada
//    2 = Tormenta violeta con rayos
//    3 = Cielo de fuego con brasas
// ============================================================================

#define SKY_STYLE 2          // [0 1 2 3]
#define SKY_INTENSITY 1.0    // [0.5 0.75 1.0]
#define LIGHTNING_RATE 2     // [1 2 3]
#define WORLD_FLASH 1        // [0 1]

uniform sampler2D colortex0;
uniform sampler2D depthtex0;
uniform mat4 gbufferProjectionInverse;
uniform mat4 gbufferModelViewInverse;
uniform float frameTimeCounter;
uniform float rainStrength;

varying vec2 texcoord;

// ---------------------------------------------------------------- ruido
float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    float a = hash(i);
    float b = hash(i + vec2(1.0, 0.0));
    float c = hash(i + vec2(0.0, 1.0));
    float d = hash(i + vec2(1.0, 1.0));
    return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
}

float fbm(vec2 p) {
    float v = 0.0;
    float a = 0.5;
    for (int i = 0; i < 5; i++) {
        v += a * noise(p);
        p = p * 2.03 + vec2(17.1, 9.2);
        a *= 0.5;
    }
    return v;
}

float wrapAngle(float a) {
    return a - 6.2831853 * floor((a + 3.1415926) / 6.2831853);
}

float stars(vec3 d, float t) {
    vec3 g = floor(d * 260.0);
    float h = hash(g.xy + g.z * 37.17);
    float tw = 0.65 + 0.35 * sin(t * 2.5 + h * 60.0);
    return step(0.9975, h) * tw;
}

// ---------------------------------------------------------------- 1: aurora
vec3 skyAurora(vec3 d, float t) {
    float up = clamp(d.y, 0.0, 1.0);
    vec3 col = mix(vec3(0.020, 0.035, 0.100), vec3(0.000, 0.008, 0.040), pow(up, 0.6));
    col += vec3(0.030, 0.070, 0.160) * pow(1.0 - up, 4.0);
    col += vec3(1.0) * stars(d, t) * smoothstep(0.02, 0.2, d.y);

    if (d.y > 0.0) {
        vec3 aur = vec3(0.0);
        for (int i = 0; i < 10; i++) {
            float fi = float(i);
            float h = 1.0 + fi * 0.20;
            vec2 p = d.xz * (h / d.y);
            float n = fbm(vec2(p.x * 0.10 + t * 0.02, p.y * 0.30));
            float wave = sin(p.y * 0.22 + n * 6.0 + t * 0.35 + fi * 0.5) * 0.5 + 0.5;
            float a = smoothstep(0.58, 0.95, wave) * smoothstep(0.18, 0.55, n) * (1.0 - fi / 10.0) * smoothstep(0.0, 0.25, d.y);
            vec3 c = mix(vec3(0.10, 1.00, 0.55), vec3(0.45, 0.35, 1.00), fi / 10.0 + n * 0.3);
            aur += c * a * 0.27;
        }
        col += aur;
    }
    return col;
}

// ---------------------------------------------------------------- 2: tormenta
// x = intensidad del destello (0..1), y = numero de tramo (cambia la posicion de los rayos)
vec2 lightningState(float t) {
    float period = 4.0 / float(LIGHTNING_RATE);
    float slot = floor(t / period);
    float local = fract(t / period);
    float rnd = hash(vec2(slot, 3.7));
    float hasBolt = step(0.45, rnd);
    float env = hasBolt * exp(-local * 9.0) * (0.65 + 0.35 * sin(local * 70.0));
    return vec2(max(env, 0.0), slot);
}

float boltShape(vec3 d, float slot, float k) {
    float az = atan(d.x, d.z);
    float a0 = (hash(vec2(slot, 9.1 + k)) * 2.0 - 1.0) * 3.1415926 + k * 2.0943951;
    float path = (fbm(vec2(d.y * 7.0, slot * 1.7 + k * 5.3)) - 0.5) * 0.9 * (1.0 - d.y * 0.4);
    float dist = abs(wrapAngle(az - a0 - path));
    float vertical = smoothstep(-0.02, 0.06, d.y) * (1.0 - smoothstep(0.55, 0.95, d.y));
    float core = exp(-dist * dist * 12000.0);
    float glow = exp(-dist * dist * 500.0) * 0.22;
    return (core + glow) * vertical;
}

vec3 skyStorm(vec3 d, float t, vec2 ls) {
    float up = clamp(d.y, 0.0, 1.0);
    vec3 base = mix(vec3(0.28, 0.08, 0.45), vec3(0.08, 0.015, 0.16), pow(up, 0.55));
    vec2 p = d.xz / (abs(d.y) + 0.35);
    float c1 = fbm(p * 1.6 + vec2(t * 0.03, -t * 0.02));
    float c2 = fbm(p * 3.2 - vec2(t * 0.05, t * 0.01));
    vec3 col = base * (0.55 + 0.95 * c1) + vec3(0.35, 0.12, 0.70) * c2 * c2 * 0.9;

    float bolts = 0.0;
    if (ls.x > 0.01) {
        bolts = boltShape(d, ls.y, 0.0) + boltShape(d, ls.y, 1.0) + boltShape(d, ls.y, 2.0);
    }
    col += vec3(0.80, 0.70, 1.00) * bolts * ls.x * 3.0;
    col += vec3(0.35, 0.20, 0.70) * ls.x * 0.9 * (0.4 + 0.6 * c1);
    return col;
}

// ---------------------------------------------------------------- 3: fuego
vec3 skyFire(vec3 d, float t) {
    float h = clamp(d.y, 0.0, 1.0);
    vec2 p = d.xz / (abs(d.y) + 0.50);
    float n = fbm(p * 1.1 + vec2(t * 0.06, t * 0.03));
    float n2 = fbm(p * 2.2 - vec2(t * 0.09, 0.0) + n);
    vec3 dark = vec3(0.10, 0.02, 0.01);
    vec3 hot = vec3(1.00, 0.45, 0.05);
    vec3 glow = vec3(1.00, 0.85, 0.35);
    vec3 col = mix(dark, hot, smoothstep(0.25, 0.85, n * 0.7 + n2 * 0.5));
    col += glow * pow(n2, 4.0) * 0.8;
    col *= 0.7 + 0.5 * (1.0 - h);

    vec2 g = vec2(atan(d.x, d.z) * 16.0, d.y * 16.0 - t * 0.7);
    vec2 cell = floor(g);
    float r = hash(cell);
    vec2 f = fract(g) - 0.5;
    float ember = step(0.94, r) * smoothstep(0.18, 0.0, length(f)) * smoothstep(0.0, 0.2, d.y);
    col += vec3(1.0, 0.6, 0.2) * ember * 1.5;
    return col;
}

// ---------------------------------------------------------------- principal
void main() {
    vec4 color = texture2D(colortex0, texcoord);
    float depth = texture2D(depthtex0, texcoord).r;
    float t = frameTimeCounter;

#if SKY_STYLE == 2
    vec2 ls = lightningState(t);
#endif

    if (depth >= 1.0) {
        vec4 ndc = vec4(texcoord * 2.0 - 1.0, 1.0, 1.0);
        vec4 view = gbufferProjectionInverse * ndc;
        view.xyz /= view.w;
        vec3 d = normalize((gbufferModelViewInverse * vec4(view.xyz, 0.0)).xyz);

        vec3 sky = color.rgb;
#if SKY_STYLE == 1
        sky = skyAurora(d, t);
#elif SKY_STYLE == 2
        sky = skyStorm(d, t, ls);
#elif SKY_STYLE == 3
        sky = skyFire(d, t);
#endif
        sky *= 1.0 - 0.45 * rainStrength;
        color.rgb = mix(color.rgb, sky, SKY_INTENSITY);
    }
#if SKY_STYLE == 2 && WORLD_FLASH == 1
    else {
        // el relampago ilumina un poco el terreno
        color.rgb += vec3(0.30, 0.22, 0.55) * ls.x * 0.22;
    }
#endif

    gl_FragData[0] = color;
}
