import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL20.*;

/**
 * Created by Andrew on 6/18/17.
 */
public class OceanShaderProgram extends ShaderProgram {

    private static final String U_MVP_MATRIX = "u_MvpMatrix";
    private static final String U_MODEL_MATRIX = "u_ModelMatrix";
    private static final String U_LIGHT_DIRECTION = "u_LightDirection";
    private static final String U_CAM_POS = "u_CamPos";
    private static final String A_POSITION = "a_Position";
    private static final String A_NORMAL = "a_Normal";

    public final int uMvpMatrixLocation;
    public final int uModelMatrixLocation;
    public final int uLightDirectionLocation;
    public final int uCamPosLocation;
    public final int aPositionLocation;
    public final int aNormalLocation;

    OceanShaderProgram() {
        super(TextResourceReader.readFile("src/shaders/ocean_vertex_shader.glsl"),
                TextResourceReader.readFile("src/shaders/ocean_fragment_shader.glsl"));

        uMvpMatrixLocation = glGetUniformLocation(programID, U_MVP_MATRIX);
        uModelMatrixLocation = glGetUniformLocation(programID, U_MODEL_MATRIX);
        uLightDirectionLocation = glGetUniformLocation(programID, U_LIGHT_DIRECTION);
        uCamPosLocation = glGetUniformLocation(programID, U_CAM_POS);
        aPositionLocation = glGetAttribLocation(programID, A_POSITION);
        aNormalLocation = glGetAttribLocation(programID, A_NORMAL);
    }
    
    void setMvpMatrix(Matrix4f m) {
        glUniformMatrix4fv(uMvpMatrixLocation, false, m.get(new float[16]));
    }

    void setModelMatrix(Matrix4f m) {
        glUniformMatrix4fv(uModelMatrixLocation, false, m.get(new float[16]));
    }

    void setLightDirection(float x, float y, float z) {
        glUniform3f(uLightDirectionLocation, x, y, z);
    }

    void setCamPos(float x, float y, float z) {
        glUniform3f(uCamPosLocation, x, y, z);
    }
}
