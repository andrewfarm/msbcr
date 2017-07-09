#version 120

uniform mat4 u_MvpMatrix;
uniform mat4 u_ModelMatrix;
uniform mat4 u_LightBiasMvpMatrix;
uniform sampler2D u_DisplacementMapUnit;
uniform float u_SeaLevel;
uniform float u_TerrainScale;
uniform float u_Morph;

attribute vec3 a_Position;
attribute vec3 a_Normal;
attribute vec2 a_TextureCoords;
attribute vec2 a_TextureCoordsAdj1;
attribute vec2 a_TextureCoordsAdj2;

varying vec4 v_PositionInLightSpace;
varying vec3 v_Normal;
varying vec3 v_Tangent;
varying vec3 v_Bitangent;
varying vec2 v_TextureCoords;

void main() {
    float fineDisplacement = (texture2D(u_DisplacementMapUnit, a_TextureCoords).r - u_SeaLevel) * u_TerrainScale;
    float coarseDisplacement1 = (texture2D(u_DisplacementMapUnit, a_TextureCoordsAdj1).r - u_SeaLevel) * u_TerrainScale;
    float coarseDisplacement2 = (texture2D(u_DisplacementMapUnit, a_TextureCoordsAdj2).r - u_SeaLevel) * u_TerrainScale;
    float coarseDisplacement = (coarseDisplacement1 + coarseDisplacement2) * 0.5;
    float displacement = mix(coarseDisplacement, fineDisplacement, u_Morph);
    gl_PointSize = 50.0; //TODO
    vec3 displacedPosition = a_Position * (1 + displacement);
    mat3 normalMatrix = mat3(u_ModelMatrix);
    v_Normal = normalMatrix * a_Normal;
    //For sphere, tangent is the normal rotated 90 degrees about y
    v_Tangent = vec3(-v_Normal.z, 0.0, v_Normal.x);
    v_Bitangent = cross(v_Normal, v_Tangent);
    v_TextureCoords = a_TextureCoordsAdj1; //TODO
    v_PositionInLightSpace = u_LightBiasMvpMatrix * vec4(displacedPosition, 1.0);
    gl_Position = u_MvpMatrix * vec4(displacedPosition, 1.0);
}