uniform mat4 u_MvpMatrix;
uniform mat4 u_ModelMatrix;

attribute vec4 a_Position;
attribute vec4 a_Normal;

varying vec4 v_Normal;

void main() {
    v_Normal = u_ModelMatrix * a_Normal;
    gl_Position = u_MvpMatrix * a_Position;
}