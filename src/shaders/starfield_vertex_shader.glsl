#version 120

uniform mat4 u_VpMatrix;

attribute vec3 a_Position;

varying vec3 v_Position;

void main() {
    v_Position = a_Position;
    v_Position.z = -v_Position.z;

    gl_Position = u_VpMatrix * vec4(a_Position, 1.0);
    gl_Position = gl_Position.xyww;
}
