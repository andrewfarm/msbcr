#version 120

#define ATMOSPHERE_COLOR vec3(0.6, 0.8, 1.0)

varying float v_Alpha;

void main() {
    gl_FragColor = vec4(ATMOSPHERE_COLOR, v_Alpha);
}
