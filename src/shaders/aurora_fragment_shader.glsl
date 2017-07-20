#version 120

#define NORTHERN_LIGHTS_AMP 1.0
#define NORTHERN_LIGHTS_FREQ 100.0
#define NORTHERN_LIGHTS_OCTAVES 1

uniform sampler1D u_TextureUnit;
uniform float u_NoisePhase;

varying vec2 v_CircularPosition;
varying float v_TextureCoords;

float cnoise(vec3 v);

void main() {
    float noise = 0.0;
    float amp = NORTHERN_LIGHTS_AMP;
    float freq = NORTHERN_LIGHTS_FREQ;
    for (int octave = 0; octave < NORTHERN_LIGHTS_OCTAVES; octave++) {
        noise += amp * cnoise(vec3(freq * v_CircularPosition, u_NoisePhase));
        amp *= 0.5;
        freq *= 2.0;
    }
    gl_FragColor = pow(1.0 - v_TextureCoords, 2.0) * texture1D(u_TextureUnit, v_TextureCoords) *
        (0.5 + 0.5 * noise);
}
