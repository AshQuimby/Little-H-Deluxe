vec2 curve(vec2 uv)
{
    float amount = 1;
    uv = (uv - 0.5) * (2.0 - amount / 12);
    uv *= 1.1;
    uv.x *= 1.0 + pow((abs(uv.y) / 5.0 * amount), 2.0);
    uv.y *= 1.0 + pow((abs(uv.x) / 4.0 * amount), 2.0);
    uv  = (uv / 2.0) + 0.5;
    uv = uv * 0.92 + 0.04;
    return uv;
}

uniform mat4 u_projTrans;

varying vec4 v_color;
varying vec2 v_texCoords;

uniform sampler2D u_texture;

void main()
{
    vec2 warped = curve(v_texCoords);
    if (warped.x < 0. || warped.y < 0. || warped.x >= 1. || warped.y >= 1.) {
        discard;
    }

    vec4 base = texture2D(u_texture, warped);
    gl_FragColor = base * v_color;
}