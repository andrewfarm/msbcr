varying vec3 v_Position;

void main() {
    float r = inScatter_rayleigh_doIntersection(WAVELENGTH_RED,   v_Position);
    float g = inScatter_rayleigh_doIntersection(WAVELENGTH_GREEN, v_Position);
    float b = inScatter_rayleigh_doIntersection(WAVELENGTH_BLUE,  v_Position);
    gl_FragColor = vec4(r, g, b, 1.0);
}
