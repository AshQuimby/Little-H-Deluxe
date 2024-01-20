#version 120

varying vec4 v_color;
varying vec2 v_texCoords;

uniform vec3 u_tint;
uniform float u_strength = 0.0;

uniform sampler2D u_texture;

void main()
{
    vec3 color = texture2D(u_texture, v_texCoords).rgb;
    if (u_strength != 0.0) {

        color = mix(color, u_tint, u_strength);

        gl_FragColor = vec4(color, texture2D(u_texture, v_texCoords).a);
    } else {
        gl_FragColor = texture2D(u_texture, v_texCoords);
    }
}