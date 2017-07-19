package com.andrewofarm.msbcr.objects.programs;
import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_1D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.GL_TEXTURE1;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL20.*;

/**
 * Created by Andrew on 7/1/17.
 */
public class RingsShaderProgram extends ShaderProgram {

    private static final String U_MVP_MATRIX = "u_MvpMatrix";
    private static final String U_LIGHT_BIAS_MVP_MATRIX = "u_LightBiasMvpMatrix";
    private static final String U_TEXTURE_UNIT = "u_TextureUnit";
    private static final String U_SHADOW_MAP_UNIT = "u_ShadowMapUnit";
    private static final String A_POSITION = "a_Position";
    private static final String A_TEX_COORDS = "a_TexCoords";

    public final int uMvpMatrixLocation;
    public final int uLightBiasMvpMatrixLocation;
    public final int uTextureUnitLocation;
    public final int uShadowMapUnitLocation;
    public final int aPositionLocation;
    public final int aTexCoordsLocation;

    public RingsShaderProgram() {
        super(TextResourceReader.readFile("src/shaders/rings_vertex_shader.glsl"),
                TextResourceReader.readFile("src/shaders/rings_fragment_shader.glsl"));

        uMvpMatrixLocation = glGetUniformLocation(programID, U_MVP_MATRIX);
        uLightBiasMvpMatrixLocation = glGetUniformLocation(programID, U_LIGHT_BIAS_MVP_MATRIX);
        uTextureUnitLocation = glGetUniformLocation(programID, U_TEXTURE_UNIT);
        uShadowMapUnitLocation = glGetUniformLocation(programID, U_SHADOW_MAP_UNIT);
        aPositionLocation = glGetAttribLocation(programID, A_POSITION);
        aTexCoordsLocation = glGetAttribLocation(programID, A_TEX_COORDS);
    }

    public void setMvpMatrix(Matrix4f m) {
        glUniformMatrix4fv(uMvpMatrixLocation, false, m.get(new float[16]));
    }

    public void setLightBiasMvpMatrixMatrix(Matrix4f m) {
        glUniformMatrix4fv(uLightBiasMvpMatrixLocation, false, m.get(new float[16]));
    }

    public void setTexture(int textureID) {
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_1D, textureID);
        glUniform1i(uTextureUnitLocation, 0);
    }

    public void setShadowMap(int shadowMapID) {
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, shadowMapID);
        glUniform1i(uShadowMapUnitLocation, 1);
    }
}
