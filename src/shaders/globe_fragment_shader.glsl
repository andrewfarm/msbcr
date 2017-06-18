#define AMBIENT_STRENGTH 0.2

uniform vec3 u_LightDirection; //must be normalized!
uniform sampler2D u_TextureUnit;

varying vec3 v_Normal;
varying vec2 v_TextureCoords;
uniform sampler2D u_DisplacementMapUnit;

void main() {
    float directionalStrength = max(dot(normalize(v_Normal), u_LightDirection), 0.0);
    vec4 texColor = texture2D(u_TextureUnit, v_TextureCoords);

    float totalLight = AMBIENT_STRENGTH + (1.0 - AMBIENT_STRENGTH) * directionalStrength;
    gl_FragColor = vec4((texColor * totalLight).rgb, 1.0);
}