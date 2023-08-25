uniform mat4 u_projTrans;

varying vec4 v_color;
varying vec2 v_texCoords;

uniform sampler2D u_texture;
uniform sampler2D u_glint;

uniform float u_tick;

void main()
{
    vec4 base = texture2D(u_texture, v_texCoords);
    gl_FragColor = base + texture2D(u_glint, fract(v_texCoords + u_tick / 60.)) * .3 * base.a;
}