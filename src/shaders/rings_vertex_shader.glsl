#version 120

uniform mat4 u_MvpMatrix;

attribute vec3 a_Position;
attribute float a_TexCoords;

varying float v_TexCoords;

void main() {
    v_TexCoords = a_TexCoords;
    gl_Position = u_MvpMatrix * vec4(a_Position, 1.0);
}
