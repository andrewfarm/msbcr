uniform mat4 u_LightMvpMatrix;
uniform sampler2D u_DisplacementMapUnit;
uniform float u_SeaLevel;
uniform float u_TerrainScale;

attribute vec3 a_Position;
attribute vec2 a_TextureCoords;

void main() {
    float displacement = (texture2D(u_DisplacementMapUnit, a_TextureCoords).r - u_SeaLevel) * u_TerrainScale;
    vec3 displacedPosition = a_Position + (normalize(a_Position) * displacement);
    gl_Position = u_LightMvpMatrix * vec4(displacedPosition, 1.0);
}
