#version 120

uniform sampler2D u_TextureUnit;

varying vec2 v_TextureCoords;

void main() {
    gl_FragColor = vec4(1.0, 0.0, 1.0, 1.0);
}
