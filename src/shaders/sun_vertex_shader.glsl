#version 120

uniform mat4 u_VpMatrix;
uniform float u_Size;

attribute vec3 a_Position;

void main() {
    gl_Position = u_VpMatrix * vec4(a_Position, 1.0);
    gl_Position = gl_Position.xyww;
    gl_PointSize = u_Size;
}
