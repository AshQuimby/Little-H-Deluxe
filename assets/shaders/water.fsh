uniform mat4 u_projTrans;

varying vec4 v_color;
varying vec2 v_texCoords;

uniform sampler2D u_texture;

uniform float u_tick;

void main()
{
    gl_FragColor = texture2D(u_texture, v_texCoords + vec2(sin(u_tick), 0.0));
}