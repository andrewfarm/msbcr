#version 120

#define EXPOSURE 1.0

uniform sampler2D u_TextureUnit;

varying vec2 v_TextureCoords;

float hdr(float light) {
    return 1.0 - exp(-EXPOSURE * light);
}

vec3 hdr(vec3 color) {
    return vec3(hdr(color.r), hdr(color.g), hdr(color.b));
}

void main() {
    vec3 texColor = texture2D(u_TextureUnit, v_TextureCoords).rgb;
    gl_FragColor = vec4(hdr(texColor), 1.0);
}
