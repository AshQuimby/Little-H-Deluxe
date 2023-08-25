attribute vec4 a_position;
attribute vec4 a_color;
attribute vec2 a_texCoord0;

uniform mat4 u_projTrans;

varying vec4 v_color;
varying vec2 v_texCoords;

uniform float u_time;
uniform mat3 u_neighbors;
uniform vec2 u_tilePosition;

void main()
{
    int corner = 0;
    int local_x = abs(u_tilePosition.x - a_position.x / 64.0) < .1 ? 0 : 1;
    int local_y = abs(u_tilePosition.y - a_position.y / 64.0) < .1 ? 0 : 1;

    bool wave = true;

    if (local_x == 1 && local_y == 1) {
        if (u_neighbors[0][0] == 1.0 || u_neighbors[0][1] == 1.0 || u_neighbors[1][0] == 1.0) {
            wave = false;
        }
    } else if (local_x == 1 && local_y == 0) {
        if (u_neighbors[1][0] == 1.0 || u_neighbors[2][0] == 1.0 || u_neighbors[2][1] == 1.0) {
            wave = false;
        }
    } else if (local_x == 0 && local_y == 1) {
        if (u_neighbors[0][1] == 1.0 || u_neighbors[0][2] == 1.0 || u_neighbors[1][2] == 1.0) {
            wave = false;
        }
    } else if (local_x == 0 && local_y == 0) {
        if (u_neighbors[1][2] == 1.0 || u_neighbors[2][2] == 1.0 || u_neighbors[2][1] == 1.0) {
            wave = false;
        }
    }

    v_color = a_color;
    v_texCoords = a_texCoord0;

    vec4 position = a_position;
    if (wave) {
        position.x += cos(u_time * 8.0 + a_position.x + a_position.y) * 4.0;
        position.y += sin(u_time * 8.0 + a_position.x + a_position.y) * 4.0;
    }

    gl_Position = u_projTrans * position;
}