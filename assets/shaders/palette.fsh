#version 130

uniform float u_hueBias = 2.0;
uniform float u_satBias = 1.0;
uniform float u_valBias = 2.0;

vec3 rgb2hsv(vec3 c)
{
    vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
    vec4 p = mix(vec4(c.bg, K.wz), vec4(c.gb, K.xy), step(c.b, c.g));
    vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));

    float d = q.x - min(q.w, q.y);
    float e = 1.0e-10;
    return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);
}

vec3 hsv2rgb(vec3 c)
{
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

float calcFitness(vec3 color, vec3 hsv)
{
    return min(abs(color.x - hsv.x), 1 - abs(color.x - hsv.x)) * u_hueBias + abs(color.y - hsv.y) * u_satBias + abs(color.z - hsv.z) * u_valBias;
}

// Allowed colors in the palette
uniform vec3 u_allowedPalette[128];
uniform int u_paletteSize;
uniform mat4 u_projTrans;

in vec4 v_color;
in vec2 v_texCoords;

uniform sampler2D u_texture;

void main()
{
    vec4 color = texture2D(u_texture, v_texCoords);

    vec3 hsv = rgb2hsv(vec3(color.r, color.g, color.b));

    vec3 bestFit = u_allowedPalette[0];
    vec3 secondBestFit = u_allowedPalette[0];

    // Arbitrarily big number
    float fitness = 10000;
    for (int i = 0; i < u_paletteSize; i++) {
        vec3 color = u_allowedPalette[i];
        float fit = calcFitness(color, hsv);
        if (fit < fitness) {
            fitness = fit;
            secondBestFit = bestFit;
            bestFit = color;
        }
    }

    bool dither = false;

    if (calcFitness((bestFit + secondBestFit) / 2, hsv) < (fitness -0.05)) {
        dither = true;
    }

    float ditherSize = 4;

    if (dither && mod(floor(gl_FragCoord.x / ditherSize) + floor(gl_FragCoord.y / ditherSize), 2) == 0) {
        color = vec4(hsv2rgb(secondBestFit), 1);
    } else {
        color = vec4(hsv2rgb(bestFit), 1);
    }
    gl_FragColor = color;
}