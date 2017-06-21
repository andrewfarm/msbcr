#define AMBIENT_STRENGTH 0.1

#define NADIR_ALPHA 0.2
#define HORIZON_ALPHA 1.0

uniform vec3 u_LightDirection;
uniform vec3 u_CamPos;

varying vec3 v_Position;
varying vec3 v_Normal;

void main() {
    vec3 normalizedNormal = normalize(v_Normal);

    float directionalStrength = max(dot(normalizedNormal, u_LightDirection), 0.0);
    float totalFlatLight = AMBIENT_STRENGTH + (1.0 - AMBIENT_STRENGTH) * directionalStrength;

    vec3 reflected = normalize(reflect(-u_LightDirection, normalizedNormal));
    vec3 lookVector = normalize(u_CamPos - v_Position);
    float specularStrength = pow(max(dot(reflected, lookVector), 0.0), 4.0);

    float alphaScale = 1.0 - dot(normalizedNormal, lookVector);
    float alpha = (HORIZON_ALPHA - NADIR_ALPHA) * alphaScale + NADIR_ALPHA;

    gl_FragColor = vec4(vec3(0.0, 0.2, 0.5) * totalFlatLight, alpha) +
        vec4(1.0) * 0.4 * specularStrength;
}
