#version 120

uniform mat4 u_MvpMatrix;
uniform float u_SinPolarAngle;
uniform float u_CosPolarAngle;
uniform float u_NoisePhase;

attribute vec2 a_Position;
attribute float a_TextureCoords;

varying float v_TextureCoords;

void main() {
    v_TextureCoords = a_TextureCoords;
    float rSinPolarAngle = a_Position.x * u_SinPolarAngle;
    gl_Position = u_MvpMatrix * vec4(
        cos(a_Position.y) * rSinPolarAngle,
        a_Position.x * u_CosPolarAngle,
        -sin(a_Position.y) * rSinPolarAngle,
        1.0
    );
}
