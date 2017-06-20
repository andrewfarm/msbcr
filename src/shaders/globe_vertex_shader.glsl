uniform mat4 u_MvpMatrix;
uniform mat4 u_ModelMatrix;
uniform mat4 u_LightBiasMvpMatrix;
uniform sampler2D u_DisplacementMapUnit;

attribute vec3 a_Position;
attribute vec3 a_Normal;
attribute vec2 a_TextureCoords;

varying vec4 v_PositionInLightSpace;
varying vec3 v_Normal;
varying vec2 v_TextureCoords;

void main() {
    float displacement = (texture2D(u_DisplacementMapUnit, a_TextureCoords).r - 0.6314) * 0.25;
    vec3 displacedPosition = a_Position + (normalize(a_Position) * displacement);
    v_Normal = (u_ModelMatrix * vec4(a_Normal, 1.0)).xyz;
    v_TextureCoords = a_TextureCoords;
    v_PositionInLightSpace = u_LightBiasMvpMatrix * vec4(displacedPosition, 1.0);
    v_PositionInLightSpace = v_PositionInLightSpace * 0.5 + 0.5;
    gl_Position = u_MvpMatrix * vec4(displacedPosition, 1.0);
}