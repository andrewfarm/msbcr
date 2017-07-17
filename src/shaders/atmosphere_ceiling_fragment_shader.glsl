varying vec3 v_Position;

void main() {
    float r = inScatter_rayleigh(WAVELENGTH_RED,   u_CamPos, v_Position);
    float g = inScatter_rayleigh(WAVELENGTH_GREEN, u_CamPos, v_Position);
    float b = inScatter_rayleigh(WAVELENGTH_BLUE,  u_CamPos, v_Position);
    gl_FragColor = vec4(r, g, b, 1.0);
}
