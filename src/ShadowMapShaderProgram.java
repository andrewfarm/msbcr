import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL20.glGetAttribLocation;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;

/**
 * Created by Andrew on 6/18/17.
 */
public class ShadowMapShaderProgram extends ShaderProgram {

    private static final String U_DEPTH_BIAS_MVP_MATRIX = "u_DepthBiasMvpMatrix";
    private static final String A_POSITION = "a_Position";

    public final int uDepthBiasMvpMatrixLocation;
    public final int aPositionLocation;

    ShadowMapShaderProgram() {
        super(TextResourceReader.readFile("src/shaders/shadowmap_vertex_shader.glsl"),
                TextResourceReader.readFile("src/shaders/shadowmap_fragment_shader.glsl"));

        uDepthBiasMvpMatrixLocation = glGetUniformLocation(programID, U_DEPTH_BIAS_MVP_MATRIX);
        aPositionLocation = glGetAttribLocation(programID, A_POSITION);
    }

    void setDepthBiasMvpMatrix(Matrix4f m) {
        glUniformMatrix4fv(uDepthBiasMvpMatrixLocation, false, m.get(new float[16]));
    }
}
