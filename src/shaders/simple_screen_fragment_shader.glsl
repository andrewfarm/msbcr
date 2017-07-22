#version 120

uniform sampler2D u_TextureUnit;

varying vec2 v_TextureCoords;

void main() {
    gl_FragColor = texture2D(u_TextureUnit, v_TextureCoords);
}
