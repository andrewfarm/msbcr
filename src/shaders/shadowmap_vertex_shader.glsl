uniform mat4 u_LightMvpMatrix;
uniform sampler2D u_DisplacementMapUnit;

attribute vec3 a_Position;
attribute vec2 a_TextureCoords;
varying vec4 v_Position; //TODO

void main() {
    float displacement = (texture2D(u_DisplacementMapUnit, a_TextureCoords).r - 0.6314) * 0.25;
    vec3 displacedPosition = a_Position + (normalize(a_Position) * displacement);
    gl_Position = u_LightMvpMatrix * vec4(displacedPosition, 1.0);
    v_Position = gl_Position;
}
