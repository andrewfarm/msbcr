#version 120

uniform sampler1D u_TextureUnit;
uniform sampler2D u_ShadowMapUnit;

varying vec4 v_PositionInLightSpace;
varying float v_TexCoords;

void main() {
    gl_FragColor = texture1D(u_TextureUnit, v_TexCoords);
    if (texture2D(u_ShadowMapUnit, v_PositionInLightSpace.xy).z < v_PositionInLightSpace.z) {
        gl_FragColor = vec4(gl_FragColor.rgb * 0.1, 1.0);
    }
}
