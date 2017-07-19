#version 120

#define NORTHERN_LIGHTS_AMP 1.0
#define NORTHERN_LIGHTS_FREQ 100.0

uniform float u_NoisePhase;

varying vec2 v_CircularPosition;
varying float v_TextureCoords;

float cnoise(vec3 v);

void main() {
    gl_FragColor = vec4(1.0, 0.0, 1.0, 1.0) *
        (0.5 + 0.5 * NORTHERN_LIGHTS_AMP * cnoise(vec3(NORTHERN_LIGHTS_FREQ * v_CircularPosition, u_NoisePhase)));
}
