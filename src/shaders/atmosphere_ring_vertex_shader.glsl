#version 120

uniform mat4 u_AtmosphereRingMvpMatrix;

attribute vec2 a_Position;
attribute float a_Alpha;

varying float v_Alpha;

void main() {
    v_Alpha = a_Alpha;
    gl_Position = u_AtmosphereRingMvpMatrix * vec4(a_Position, 0.0, 1.0);
}
