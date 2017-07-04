#version 120

#define AMBIENT_STRENGTH 0.1

#define SHADOW_BIAS_COEF 0.0005
#define SHADOW_BIAS_MAX 0.002

uniform vec3 u_LightDirection; //must be normalized!
uniform sampler2D u_TextureUnit;
uniform sampler2D u_NormalMapUnit;
uniform sampler2D u_ShadowMapUnit;

varying vec4 v_PositionInLightSpace;
varying vec3 v_Normal;
varying vec3 v_Tangent;
varying vec3 v_Bitangent;
varying vec2 v_TextureCoords;

void main() {
    vec3 normalMapSample = texture2D(u_NormalMapUnit, v_TextureCoords).xyz - vec3(0.5);
    normalMapSample.y = -normalMapSample.y;
    mat3 tbnMatrix = mat3(normalize(v_Tangent), normalize(v_Bitangent), normalize(v_Normal));
//    vec3 normalizedNormal = normalize(tbnMatrix * normalMapSample);
    vec3 normalizedNormal = normalize(v_Normal); //TODO

    vec4 texColor = texture2D(u_TextureUnit, v_TextureCoords);
    texColor = vec4(v_TextureCoords, 0.0, 1.0); //TODO

    float totalLight = AMBIENT_STRENGTH;

    float shadowBias = max(
        abs(SHADOW_BIAS_COEF * tan(acos(dot(normalizedNormal, u_LightDirection)))), SHADOW_BIAS_MAX);
//    if (texture2D(u_ShadowMapUnit, v_PositionInLightSpace.xy).z >= v_PositionInLightSpace.z - shadowBias) {
        float directionalStrength = max(dot(normalizedNormal, u_LightDirection), 0.0);
        totalLight += (1.0 - AMBIENT_STRENGTH) * directionalStrength;
        totalLight = 1.0; //TODO
//    } TODO
    gl_FragColor = vec4((texColor * totalLight).rgb, 1.0);
}