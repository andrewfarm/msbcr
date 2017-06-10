#define LIGHT_DIRECTION vec4(-1.0, 1.0, 0.0, 1.0)

varying vec4 v_Normal;

void main() {
    float value = max(dot(normalize(v_Normal), normalize(LIGHT_DIRECTION)), 0.0);
    gl_FragColor = vec4(value);
}