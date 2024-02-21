varying vec4 v_color;
varying vec2 v_texCoords;

uniform sampler2D u_texture;
uniform float u_amount;
uniform vec2 u_offset;

void main()
{
    vec2 texSize = 1.0 / textureSize(u_texture, 0);
    vec2 quantizedCoords = floor(gl_FragCoord.xy / u_amount);
    quantizedCoords *= u_amount;
    quantizedCoords += 0.5 * u_amount;
    quantizedCoords *= texSize;
    quantizedCoords += u_offset;

    vec4 color = texture2D(u_texture, quantizedCoords);

    gl_FragColor = color;
}