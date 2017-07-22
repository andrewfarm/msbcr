#version 120

attribute vec2 a_Position;
attribute vec2 a_TextureCoords;

varying vec2 v_TextureCoords;

void main() {
    v_TextureCoords = a_TextureCoords;
    gl_Position = vec4(a_Position, 0.0, 1.0);
}
