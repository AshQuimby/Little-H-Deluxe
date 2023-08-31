attribute vec4 a_position;
attribute vec4 a_color;
attribute vec2 a_texCoord0;

uniform mat4 u_projTrans;

varying vec4 v_color;
varying vec2 v_texCoords;

uniform float u_time;
uniform float u_attached;
uniform vec2 u_tilePosition;

void main()
{
    int corner = 0;
    int local_x = abs(u_tilePosition.x - a_position.x / 64.0) < .1 ? 0 : 1;
    int local_y = abs(u_tilePosition.y - a_position.y / 64.0) < .1 ? 0 : 1;

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
        position.x += cos(u_time * 4.0 + a_position.x + a_position.y) * 2.0 * ((u_attached + 0.5));
        position.y += sin(u_time * 4.0 + a_position.x + a_position.y) * 4.0;
    }

    gl_Position = u_projTrans * position;
}