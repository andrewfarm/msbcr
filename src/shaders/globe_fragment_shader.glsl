#define AMBIENT_STRENGTH 0.1

uniform vec3 u_LightDirection; //must be normalized!
uniform sampler2D u_TextureUnit;
uniform sampler2D u_ShadowMapUnit;

varying vec4 v_PositionInLightSpace;
varying vec3 v_Normal;
varying vec2 v_TextureCoords;

void main() {
    float directionalStrength = max(dot(normalize(v_Normal), u_LightDirection), 0.0);
//    vec4 texColor = texture2D(u_TextureUnit, v_TextureCoords);
    vec4 texColor = texture2D(u_ShadowMapUnit, v_PositionInLightSpace.xy);//.r * vec4(v_TextureCoords.xy, 0.0, 1.0);

    float totalLight = AMBIENT_STRENGTH + (1.0 - AMBIENT_STRENGTH) * directionalStrength;
    gl_FragColor = vec4((texColor/* * totalLight*/).rgb, 1.0);//TODO
//    gl_FragColor = vec4(gl_FragColor.r * v_PositionInLightSpace.xyz, 1.0);

//    if (texture2D(u_ShadowMapUnit, v_PositionInLightSpace.xy).z < v_PositionInLightSpace.z) {
//        gl_FragColor = vec4(1.0, 0.0, 1.0, 1.0);
//    }
//    gl_FragColor = vec4(v_PositionInLightSpace.xyz, 1.0);
}