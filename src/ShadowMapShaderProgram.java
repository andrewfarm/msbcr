import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL20.*;

/**
 * Created by Andrew on 6/18/17.
 */
public class ShadowMapShaderProgram extends ShaderProgram {

    private static final String U_DEPTH_BIAS_MVP_MATRIX = "u_DepthBiasMvpMatrix";
    private static final String U_DISPLACEMENT_MAP_UNIT = "u_DisplacementMapUnit";
    private static final String A_POSITION = "a_Position";
    private static final String A_TEXTURE_COORDS = "a_TextureCoords";

    public final int uDepthBiasMvpMatrixLocation;
    public final int uDisplacementMapUnitLocation;
    public final int aPositionLocation;
    public final int aTextureCoordsLocation;

    ShadowMapShaderProgram() {
        super(TextResourceReader.readFile("src/shaders/shadowmap_vertex_shader.glsl"),
                TextResourceReader.readFile("src/shaders/shadowmap_fragment_shader.glsl"));

        uDepthBiasMvpMatrixLocation = glGetUniformLocation(programID, U_DEPTH_BIAS_MVP_MATRIX);
        uDisplacementMapUnitLocation = glGetUniformLocation(programID, U_DISPLACEMENT_MAP_UNIT);
        aPositionLocation = glGetAttribLocation(programID, A_POSITION);
        aTextureCoordsLocation = glGetAttribLocation(programID, A_TEXTURE_COORDS);
    }

    void setDepthBiasMvpMatrix(Matrix4f m) {
        glUniformMatrix4fv(uDepthBiasMvpMatrixLocation, false, m.get(new float[16]));
    }

    void setDisplacementMap(int displacementMapID) {
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, displacementMapID);
        glUniform1i(uDisplacementMapUnitLocation, 0);
    }
}
