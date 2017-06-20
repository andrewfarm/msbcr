import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;

/**
 * Created by Andrew on 6/8/17.
 */
public class GlobeShaderProgram extends ShaderProgram {

    private static final String U_MVP_MATRIX = "u_MvpMatrix";
    private static final String U_MODEL_MATRIX = "u_ModelMatrix";
    private static final String U_LIGHT_BIAS_MVP_MATRIX = "u_LightBiasMvpMatrix";
    private static final String U_LIGHT_DIRECTION = "u_LightDirection";
    private static final String U_TEXTURE_UNIT = "u_TextureUnit";
    private static final String U_DISPLACEMENT_MAP_UNIT = "u_DisplacementMapUnit";
    private static final String U_SHADOW_MAP_UNIT = "u_ShadowMapUnit";
    private static final String U_SEA_LEVEL = "u_SeaLevel";
    private static final String U_TERRAIN_SCALE = "u_TerrainScale";
    private static final String A_POSITION = "a_Position";
    private static final String A_NORMAL = "a_Normal";
    private static final String A_TEXTURE_COORDS = "a_TextureCoords";

    public final int uMvpMatrixLocation;
    public final int uModelMatrixLocation;
    public final int uLightBiasMvpMatrixLocation;
    public final int uLightDirectionLocation;
    public final int uTextureUnitLocation;
    public final int uDisplacementMapUnitLocation;
    public final int uShadowMapUnitLocation;
    public final int uSeaLevelLocation;
    public final int uTerrainScaleLocation;
    public final int aPositionLocation;
    public final int aNormalLocation;
    public final int aTextureCoordsLocation;

    GlobeShaderProgram() {
        super(TextResourceReader.readFile("src/shaders/globe_vertex_shader.glsl"),
                TextResourceReader.readFile("src/shaders/globe_fragment_shader.glsl"));

        uMvpMatrixLocation = glGetUniformLocation(programID, U_MVP_MATRIX);
        uModelMatrixLocation = glGetUniformLocation(programID, U_MODEL_MATRIX);
        uLightBiasMvpMatrixLocation = glGetUniformLocation(programID, U_LIGHT_BIAS_MVP_MATRIX);
        uLightDirectionLocation = glGetUniformLocation(programID, U_LIGHT_DIRECTION);
        uTextureUnitLocation = glGetUniformLocation(programID, U_TEXTURE_UNIT);
        uDisplacementMapUnitLocation = glGetUniformLocation(programID, U_DISPLACEMENT_MAP_UNIT);
        uShadowMapUnitLocation = glGetUniformLocation(programID, U_SHADOW_MAP_UNIT);
        uSeaLevelLocation = glGetUniformLocation(programID, U_SEA_LEVEL);
        uTerrainScaleLocation = glGetUniformLocation(programID, U_TERRAIN_SCALE);
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

    void setLightBiasMvpMatrix(Matrix4f m) {
        glUniformMatrix4fv(uLightBiasMvpMatrixLocation, false, m.get(new float[16]));
    }

    void setLightDirection(float x, float y, float z) {
        glUniform3f(uLightDirectionLocation, x, y, z);
    }

    void setDisplacementMap(int displacementMapID) {
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, displacementMapID);
        glUniform1i(uDisplacementMapUnitLocation, 0);
    }

    void setTexture(int textureID) {
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, textureID);
        glUniform1i(uTextureUnitLocation, 1);
    }

    void setShadowMap(int shadowMapID) {
        glActiveTexture(GL_TEXTURE2);
        glBindTexture(GL_TEXTURE_2D, shadowMapID);
        glUniform1i(uShadowMapUnitLocation, 2);
    }

    void setSeaLevel(float seaLevel) {
        glUniform1f(uSeaLevelLocation, seaLevel);
    }

    void setTerrainScale(float terrainScale) {
        glUniform1f(uTerrainScaleLocation, terrainScale);
    }
}
