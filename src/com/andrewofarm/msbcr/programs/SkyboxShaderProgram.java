package com.andrewofarm.msbcr.programs;

import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL20.*;

/**
 * Created by Andrew on 6/10/17.
 */
public class SkyboxShaderProgram extends ShaderProgram {

    private static final String U_VP_MATRIX = "u_VpMatrix";
    private static final String U_TEXTURE_UNIT = "u_TextureUnit";
    private static final String A_POSITION = "a_Position";

    public final int uVpMatrixLocation;
    public final int uTextureUnitLocation;
    public final int aPositionLocation;

    public SkyboxShaderProgram() {
        super(TextResourceReader.readFile("src/shaders/starfield_vertex_shader.glsl"),
                TextResourceReader.readFile("src/shaders/starfield_fragment_shader.glsl"));

        uVpMatrixLocation = glGetUniformLocation(programID, U_VP_MATRIX);
        uTextureUnitLocation = glGetUniformLocation(programID, U_TEXTURE_UNIT);
        aPositionLocation = glGetAttribLocation(programID, A_POSITION);
    }

    public void setVpMatrix(Matrix4f m) {
        glUniformMatrix4fv(uVpMatrixLocation, false, m.get(new float[16]));
    }

    public void setTexture(int textureID) {
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_CUBE_MAP, textureID);
        glUniform1i(uTextureUnitLocation, 0);
    }
}
