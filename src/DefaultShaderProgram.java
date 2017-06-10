import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL20.*;

/**
 * Created by Andrew on 6/8/17.
 */
public class DefaultShaderProgram extends ShaderProgram {

    private static final String U_MVP_MATRIX = "u_MvpMatrix";
    private static final String U_MODEL_MATRIX = "u_ModelMatrix";
    private static final String U_TEXTURE_UNIT = "u_TextureUnit";
    private static final String A_POSITION = "a_Position";
    private static final String A_NORMAL = "a_Normal";
    private static final String A_TEXTURE_COORDS = "a_TextureCoords";

    public final int uMvpMatrixLocation;
    public final int uModelMatrixLocation;
    public final int uTextureUnitLocation;
    public final int aPositionLocation;
    public final int aNormalLocation;
    public final int aTextureCoordsLocation;

    DefaultShaderProgram() {
        super(TextResourceReader.readFile("src/shaders/vertex_shader.glsl"),
                TextResourceReader.readFile("src/shaders/fragment_shader.glsl"));

        uMvpMatrixLocation = glGetUniformLocation(programID, U_MVP_MATRIX);
        uModelMatrixLocation = glGetUniformLocation(programID, U_MODEL_MATRIX);
        uTextureUnitLocation = glGetUniformLocation(programID, U_TEXTURE_UNIT);
        aPositionLocation = glGetAttribLocation(programID, A_POSITION);
        aNormalLocation = glGetAttribLocation(programID, A_NORMAL);
        aTextureCoordsLocation = glGetAttribLocation(programID, A_TEXTURE_COORDS);
    }

    void setMvpMatrix(Matrix4f m) {
        glUniformMatrix4fv(uMvpMatrixLocation, false, m.get(new float[16]));
    }

    void setModelMatrix(Matrix4f m) {
        glUniformMatrix4fv(uModelMatrixLocation, false, m.get(new float[16]));
    }

    void setTexture(int textureID) {
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textureID);
        glUniform1i(uTextureUnitLocation, 0);
    }
}
