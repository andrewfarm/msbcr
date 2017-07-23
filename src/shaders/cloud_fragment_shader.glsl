#define AMBIENT_STRENGTH 0.1

#define SHADOW_BIAS_COEF 0.0005
#define SHADOW_BIAS_MAX 0.002

#define CLOUD_AMP 1.0
#define CLOUD_FREQ 5.0
#define CLOUD_OCTAVES 8

uniform sampler2D u_CloudCoverUnit;
uniform sampler2D u_ShadowMapUnit;
uniform float u_NoisePhase;

varying vec3 v_Position;
varying vec3 v_PositionInModelSpace;
varying vec3 v_PositionInLightSpace;
varying vec3 v_Normal;
varying vec2 v_TextureCoords;

void main() {
    vec3 normalizedNormal = normalize(v_Normal);

    float noise = 0.0;
    float amp = CLOUD_AMP;
    float freq = CLOUD_FREQ;
    for (int octave = 0; octave < CLOUD_OCTAVES; octave++) {
        noise += amp * cnoise(vec4(freq * v_PositionInModelSpace, u_NoisePhase));
        amp *= 0.5;
        freq *= 2.0;
    }

    float totalLight = AMBIENT_STRENGTH;
    float shadowBias = max(
        abs(SHADOW_BIAS_COEF * tan(acos(dot(normalizedNormal, u_LightDirection)))), SHADOW_BIAS_MAX);
    if (texture2D(u_ShadowMapUnit, v_PositionInLightSpace.xy).z >= v_PositionInLightSpace.z - shadowBias) {
        float directionalStrength = max(dot(normalizedNormal, u_LightDirection), 0.0);
        totalLight += (1.0 - AMBIENT_STRENGTH) * directionalStrength;
    }

    float r = surfaceScatter_rayleigh(WAVELENGTH_RED,   totalLight, v_Position);
    float g = surfaceScatter_rayleigh(WAVELENGTH_GREEN, totalLight, v_Position);
    float b = surfaceScatter_rayleigh(WAVELENGTH_BLUE,  totalLight, v_Position);
    gl_FragColor = vec4(r, g, b, clamp(noise, 0.0, 1.0));
}
