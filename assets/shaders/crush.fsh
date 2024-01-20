varying vec4 v_color;
varying vec2 v_texCoords;

uniform sampler2D u_texture;

void main()
{
    float amount = 4.0;

    vec2 texSize = 1.0 / textureSize(u_texture, 0);
    vec2 quantizedCoords = floor(gl_FragCoord.xy / amount);
    quantizedCoords *= amount;
    quantizedCoords += 0.5 * amount;
    quantizedCoords *= texSize;

    vec4 color = texture2D(u_texture, quantizedCoords);

    gl_FragColor = color;
}