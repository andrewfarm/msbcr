uniform mat4 u_DepthBiasMvpMatrix;

attribute vec3 a_Position;

void main() {
    gl_Position = u_DepthBiasMvpMatrix * vec4(a_Position, 1.0);
}
