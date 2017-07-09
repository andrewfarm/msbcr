uniform mat4 u_LightMvpMatrix;
uniform sampler2D u_DisplacementMapUnit;
uniform float u_SeaLevel;
uniform float u_TerrainScale;
uniform float u_Morph;

attribute vec3 a_Position;
attribute vec2 a_TextureCoords;
attribute vec2 a_TextureCoordsAdj1;
attribute vec2 a_TextureCoordsAdj2;

void main() {
    float fineDisplacement = (texture2D(u_DisplacementMapUnit, a_TextureCoords).r - u_SeaLevel) * u_TerrainScale;
    float coarseDisplacementAdj1 = (texture2D(u_DisplacementMapUnit, a_TextureCoordsAdj1).r - u_SeaLevel) * u_TerrainScale;
    float coarseDisplacementAdj2 = (texture2D(u_DisplacementMapUnit, a_TextureCoordsAdj2).r - u_SeaLevel) * u_TerrainScale;
    float coarseDisplacement = (coarseDisplacementAdj1 + coarseDisplacementAdj2) * 0.5;
    float displacement = mix(coarseDisplacement, fineDisplacement, u_Morph);
    vec3 displacedPosition = a_Position + (normalize(a_Position) * displacement);
    gl_Position = u_LightMvpMatrix * vec4(displacedPosition, 1.0);
}
