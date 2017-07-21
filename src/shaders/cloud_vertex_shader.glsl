#version 120

uniform mat4 u_MvpMatrix;
uniform mat4 u_ModelMatrix;
uniform mat4 u_LightBiasMvpMatrix;

attribute vec4 a_Position;
attribute vec3 a_Normal;
attribute vec2 a_TextureCoords;

varying vec3 v_Position;
varying vec3 v_PositionInModelSpace;
varying vec3 v_PositionInLightSpace;
varying vec3 v_Normal;
varying vec2 v_TextureCoords;

void main() {
    v_Position = (u_ModelMatrix * a_Position).xyz;
    v_PositionInModelSpace = a_Position.xyz;
    v_PositionInLightSpace = (u_LightBiasMvpMatrix * a_Position).xyz;
    v_Normal = mat3(u_ModelMatrix) * a_Normal;
    v_TextureCoords = a_TextureCoords;
    gl_Position = u_MvpMatrix * a_Position;
}
