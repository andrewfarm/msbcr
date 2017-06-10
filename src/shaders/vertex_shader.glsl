uniform mat4 u_MvpMatrix;
uniform mat4 u_ModelMatrix;

attribute vec3 a_Position;
attribute vec3 a_Normal;
attribute vec2 a_TextureCoords;

varying vec3 v_Normal;
varying vec2 v_TextureCoords;

void main() {
    v_Normal = (u_ModelMatrix * vec4(a_Normal, 1.0)).xyz;
    v_TextureCoords = a_TextureCoords;
    gl_Position = u_MvpMatrix * vec4(a_Position, 1.0);
}