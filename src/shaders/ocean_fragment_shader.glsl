#define AMBIENT_STRENGTH 0.1

#define NADIR_ALPHA 0.2
#define HORIZON_ALPHA 1.0

//uniform vec3 u_LightDirection;
//uniform vec3 u_CamPos;
uniform sampler2D u_ElevationMapUnit;
uniform float u_SeaLevel;

varying vec3 v_Position;
varying vec3 v_Normal;
varying vec2 v_TextureCoords;

void main() {
    if (texture2D(u_ElevationMapUnit, v_TextureCoords).r > u_SeaLevel) discard;

    vec3 normalizedNormal = normalize(v_Normal);

    float directionalStrength = max(dot(normalizedNormal, u_LightDirection), 0.0);
    float totalFlatLight = AMBIENT_STRENGTH + (1.0 - AMBIENT_STRENGTH) * directionalStrength;

    vec3 reflected = normalize(reflect(-u_LightDirection, normalizedNormal));
    vec3 lookVector = normalize(u_CamPos - v_Position);
    float specularStrength = pow(max(dot(reflected, lookVector), 0.0), 4.0);

    float alphaScale = 1.0 - dot(normalizedNormal, lookVector);
    float alpha = (HORIZON_ALPHA - NADIR_ALPHA) * alphaScale + NADIR_ALPHA;

    vec4 surfaceColor = vec4(vec3(0.0, 0.2, 0.5) * totalFlatLight, alpha) +
        vec4(1.0) * 0.4 * specularStrength;
    float r = surfaceScatter_rayleigh(WAVELENGTH_RED,   surfaceColor.r, v_Position);
    float g = surfaceScatter_rayleigh(WAVELENGTH_GREEN, surfaceColor.g, v_Position);
    float b = surfaceScatter_rayleigh(WAVELENGTH_BLUE,  surfaceColor.b, v_Position);
    gl_FragColor = vec4(r, g, b, surfaceColor.a);
}
