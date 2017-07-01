#version 120

uniform sampler1D u_TextureUnit;

varying float v_TexCoords;

void main() {
    gl_FragColor = texture1D(u_TextureUnit, v_TexCoords);
}
