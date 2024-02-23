uniform mat4 u_projTrans;

varying vec4 v_color;
varying vec2 v_texCoords;

uniform sampler2D u_texture;
uniform float u_time;
uniform vec2 u_tilePosition;

void main()
{
    vec4 color = texture2D(u_texture, v_texCoords);
    gl_FragColor = color;
}