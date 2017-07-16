#define AMBIENT_STRENGTH 0.1

#define SHADOW_BIAS_COEF 0.0005
#define SHADOW_BIAS_MAX 0.002

// wavelengths in micrometers
#define WAVELENGTH_RED   0.650
#define WAVELENGTH_GREEN 0.532
#define WAVELENGTH_BLUE  0.473

uniform vec3 u_LightDirection; //must be normalized!
uniform sampler2D u_TextureUnit;
uniform sampler2D u_NormalMapUnit;
uniform sampler2D u_ShadowMapUnit;

varying vec3 v_Position;
varying vec3 v_PositionInLightSpace;
varying vec3 v_Normal;
varying vec3 v_Tangent;
varying vec3 v_Bitangent;
varying vec2 v_TextureCoords;

void main() {
    vec3 normalMapSample = texture2D(u_NormalMapUnit, v_TextureCoords).xyz - vec3(0.5);
    normalMapSample.y = -normalMapSample.y;
    mat3 tbnMatrix = mat3(normalize(v_Tangent), normalize(v_Bitangent), normalize(v_Normal));
    vec3 normalizedNormal = normalize(tbnMatrix * normalMapSample);

    vec4 texColor = texture2D(u_TextureUnit, v_TextureCoords);

    float totalLight = AMBIENT_STRENGTH;

    float shadowBias = max(
        abs(SHADOW_BIAS_COEF * tan(acos(dot(normalizedNormal, u_LightDirection)))), SHADOW_BIAS_MAX);
    if (texture2D(u_ShadowMapUnit, v_PositionInLightSpace.xy).z >= v_PositionInLightSpace.z - shadowBias) {
        float directionalStrength = max(dot(normalizedNormal, u_LightDirection), 0.0);
        totalLight += (1.0 - AMBIENT_STRENGTH) * directionalStrength;
    }
    vec3 surfaceColor = (texColor * totalLight).rgb;
    float r = surfaceScatter_rayleigh(WAVELENGTH_RED,   surfaceColor.r, v_Position);
    float g = surfaceScatter_rayleigh(WAVELENGTH_GREEN, surfaceColor.g, v_Position);
    float b = surfaceScatter_rayleigh(WAVELENGTH_BLUE,  surfaceColor.b, v_Position);
    gl_FragColor = vec4(r, g, b, 1.0);
}