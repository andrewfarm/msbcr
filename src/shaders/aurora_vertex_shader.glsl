#version 120

#define AURORAL_OVAL_NOISE_AMP 0.1
#define AURORAL_OVAL_NOISE_FREQ 5.0

uniform mat4 u_MvpMatrix;
uniform float u_PolarAngle;
uniform float u_NoisePhase;

attribute vec2 a_Position;
attribute float a_TextureCoords;

varying float v_TextureCoords;

float cnoise(vec3 v);

void main() {
    v_TextureCoords = a_TextureCoords;
    float sinAzimuth = sin(a_Position.y);
    float cosAzimuth = cos(a_Position.y);
    float noisyPolarAngle = u_PolarAngle + cnoise(vec3(
            AURORAL_OVAL_NOISE_FREQ * cosAzimuth,
            AURORAL_OVAL_NOISE_FREQ * sinAzimuth,
            u_NoisePhase)) * AURORAL_OVAL_NOISE_AMP;
    float sinNoisyPolarAngle = sin(noisyPolarAngle);
    float cosNoisyPolarAngle = cos(noisyPolarAngle);
    vec3 noisyPosition = a_Position.x * vec3(
        cosAzimuth * sinNoisyPolarAngle,
        cosNoisyPolarAngle,
        -sinAzimuth * sinNoisyPolarAngle
    );
    gl_Position = u_MvpMatrix * vec4(noisyPosition, 1.0);
}
