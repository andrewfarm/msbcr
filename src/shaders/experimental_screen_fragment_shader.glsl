#version 120

#define EXPOSURE 1.5

#define PERLIN

uniform sampler2D u_TextureUnit;
uniform float u_NoisePhase;

varying vec2 v_TextureCoords;

float cnoise(vec3 v);

float hdr(float light) {
    return 1.0 - exp(-EXPOSURE * light);
}

vec3 hdr(vec3 color) {
    return vec3(hdr(color.r), hdr(color.g), hdr(color.b));
}

void main() {
    float time = 0.0;
    #ifdef PERLIN
    vec2 noise = vec2(0.0);
    float amp = 0.025;
    float freq = 20.0;
    for (int i = 0; i < 1; i++) {
        noise += amp * vec2(
            cnoise(vec3(freq * v_TextureCoords, u_NoisePhase * 0.0005)),
            cnoise(vec3(freq * v_TextureCoords, u_NoisePhase * 0.0005 + 20.0)));
        amp *= 0.5;
        freq *= 2.0;
    }
    #else
    vec2 noise = 0.01 * vec2(sin(u_NoisePhase + 300.0 * v_TextureCoords.x),
        cos(u_NoisePhase + 300.0 * v_TextureCoords.y));
    #endif
    vec3 texColor = texture2D(u_TextureUnit, v_TextureCoords + noise).rgb;
    gl_FragColor = vec4(hdr(texColor), 1.0);
}
