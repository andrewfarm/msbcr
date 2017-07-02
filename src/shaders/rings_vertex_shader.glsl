#version 120

uniform mat4 u_MvpMatrix;
uniform mat4 u_LightBiasMvpMatrix;

attribute vec3 a_Position;
attribute float a_TexCoords;

varying vec4 v_PositionInLightSpace;
varying float v_TexCoords;

void main() {
    v_PositionInLightSpace = u_LightBiasMvpMatrix * vec4(a_Position, 1.0);
    v_TexCoords = a_TexCoords;
    gl_Position = u_MvpMatrix * vec4(a_Position, 1.0);
}
