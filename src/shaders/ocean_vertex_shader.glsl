uniform mat4 u_MvpMatrix;
uniform mat4 u_ModelMatrix;

attribute vec3 a_Position;
attribute vec3 a_Normal;

varying vec3 v_Position;
varying vec3 v_Normal;

void main() {
    v_Position = (u_ModelMatrix * vec4(a_Position, 1.0)).xyz;
    v_Normal = (u_ModelMatrix * vec4(a_Normal, 1.0)).xyz;
    gl_Position = u_MvpMatrix * vec4(a_Position, 1.0);
}
