import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL20.glGetAttribLocation;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;

/**
 * Created by Andrew on 6/8/17.
 */
public class DefaultShaderProgram extends ShaderProgram {

    private static final String U_MVP_MATRIX = "u_MvpMatrix";
    private static final String U_MODEL_MATRIX = "u_ModelMatrix";
    private static final String A_POSITION = "a_Position";
    private static final String A_NORMAL = "a_Normal";

    public final int uMvpMatrixLocation;
    public final int uModelMatrixLocation;
    public final int aPositionLocation;
    public final int aNormalLocation;

    DefaultShaderProgram() {
        super(TextResourceReader.readFile("src/shaders/vertex_shader.glsl"),
                TextResourceReader.readFile("src/shaders/fragment_shader.glsl"));

        uMvpMatrixLocation = glGetUniformLocation(programID, U_MVP_MATRIX);
        uModelMatrixLocation = glGetUniformLocation(programID, U_MODEL_MATRIX);
        aPositionLocation = glGetAttribLocation(programID, A_POSITION);
        aNormalLocation = glGetAttribLocation(programID, A_NORMAL);
    }

    void setMvpMatrix(Matrix4f m) {
        glUniformMatrix4fv(uMvpMatrixLocation, false, m.get(new float[16]));
    }
    void setModelMatrix(Matrix4f m) {
        glUniformMatrix4fv(uModelMatrixLocation, false, m.get(new float[16]));
    }
}
