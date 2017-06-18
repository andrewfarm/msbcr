uniform vec3 u_LightDirection;
uniform vec3 u_CamPos;

varying vec3 v_Position;
varying vec3 v_Normal;

void main() {
    vec3 reflected = reflect(u_LightDirection, normalize(v_Normal));
    vec3 lookVector = u_CamPos - v_Position;
    float specularStrength = pow(max(dot(normalize(lookVector), normalize(reflected)), 0.0), 10.0);
    gl_FragColor = vec4(0.0, 0.2, 0.8, 0.2) + vec4(1.0) * 0.6 * specularStrength;
}
