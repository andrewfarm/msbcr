#define AMBIENT_STRENGTH 0.2
#define LIGHT_DIRECTION vec3(-1.0, 0.0, 0.0)

uniform sampler2D u_TextureUnit;

varying vec3 v_Normal;
varying vec2 v_TextureCoords;
uniform sampler2D u_DisplacementMapUnit;

void main() {
    float directionalStrength = max(dot(normalize(v_Normal), normalize(LIGHT_DIRECTION)), 0.0);
    vec4 texColor = texture2D(u_TextureUnit, v_TextureCoords);

    float totalLight = AMBIENT_STRENGTH + (1.0 - AMBIENT_STRENGTH) * directionalStrength;
    gl_FragColor = texColor * totalLight;
}