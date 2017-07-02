#version 120

uniform vec3 u_Color;
uniform sampler2D u_textureUnit;

void main() {
    float texColor = texture2D(u_textureUnit, gl_PointCoord).r;
    gl_FragColor = vec4(u_Color * texColor, texColor);
}
