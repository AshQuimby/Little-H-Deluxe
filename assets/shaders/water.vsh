attribute vec4 a_position;
attribute vec4 a_color;
attribute vec2 a_texCoord0;

uniform mat4 u_projTrans;

varying vec4 v_color;
varying vec2 v_texCoords;

uniform float u_time;
uniform ivec4 u_neighbors;
uniform vec2 u_tilePosition;

void main()
{
    bool wavy = true;
    if (abs(u_tilePosition.x + 1.0 - a_position.x / 64.0) < .1 && u_neighbors[0] == 1) {
        wavy = false;
    }
    if (abs(u_tilePosition.x - a_position.x / 64.0) < .1 && u_neighbors[1] == 1) {
        wavy = false;
    }
    if (abs(u_tilePosition.y + 1.0 - a_position.y / 64.0) < .1 && u_neighbors[2] == 1) {
        wavy = false;
    }
    if (abs(u_tilePosition.y - a_position.y / 64.0) < .1 && u_neighbors[3] == 1) {
        wavy = false;
    }

    v_color = a_color;
    v_texCoords = a_texCoord0;

    vec4 position = a_position;
    if (wavy) {
        position.x += cos(u_time * 8.0 + a_position.x + a_position.y) * 4.0;
        position.y += sin(u_time * 8.0 + a_position.x + a_position.y) * 4.0;
    }

    gl_Position = u_projTrans * position;
}