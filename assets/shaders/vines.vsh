attribute vec4 a_position;
attribute vec4 a_color;
attribute vec2 a_texCoord0;

uniform mat4 u_projTrans;

varying vec4 v_color;
varying vec2 v_texCoords;

uniform float u_time;
uniform float u_attached;
uniform float u_vineLength;
uniform vec2 u_tilePosition;

void main()
{
    int corner = 0;
    int local_x = abs(u_tilePosition.x - a_position.x / 64.0) < .1 ? 0 : 1;
    int local_y = abs(u_tilePosition.y - a_position.y / 64.0) < .1 ? 0 : 1;
    float subY = u_tilePosition.y - a_position.y / 64.0;

    bool wave = true;

    if (u_attached == 0.0) {
        if (local_y == 1) {
            wave = false;
        }
    }

    v_color = a_color;
    v_texCoords = a_texCoord0;

    vec4 position = a_position;
    if (wave) {
        float swing = cos((u_time * 20.0 + u_tilePosition.x) / u_vineLength) * 0.15 * ((u_attached + subY + 0.5) * (u_attached + subY + 0.5) - abs(u_tilePosition.y - a_position.y / 64.0));
        position.x += swing;
    }

    gl_Position = u_projTrans * position;
}