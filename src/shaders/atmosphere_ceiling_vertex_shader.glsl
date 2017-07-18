#version 120

uniform mat4 u_MvpMatrix;
uniform mat4 u_ModelMatrix;

attribute vec3 a_Position;

varying vec3 v_Position;

void main() {
    v_Position = (u_ModelMatrix * vec4(a_Position, 1.0)).xyz;
    gl_Position = u_MvpMatrix * vec4(a_Position, 1.0);
}
