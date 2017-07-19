package com.andrewofarm.msbcr.objects.programs;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL20.*;

/**
 * Created by Andrew on 7/2/17.
 */
public class SunShaderProgram extends ShaderProgram {

    private static final String U_VP_MATRIX = "u_VpMatrix";
    private static final String U_SIZE = "u_Size";
    private static final String U_COLOR = "u_Color";
    private static final String U_TEXTURE_UNIT = "u_TextureUnit";
    private static final String A_POSITION = "a_Position";

    public final int uVpMatrixLocation;
    public final int uSizeLocation;
    public final int uColorLocation;
    public final int uTextureUnitLocation;
    public final int aPositionLocation;

    public SunShaderProgram() {
        super(TextResourceReader.readFile("src/shaders/sun_vertex_shader.glsl"),
                TextResourceReader.readFile("src/shaders/sun_fragment_shader.glsl"));

        uVpMatrixLocation = glGetUniformLocation(programID, U_VP_MATRIX);
        uSizeLocation = glGetUniformLocation(programID, U_SIZE);
        uColorLocation = glGetUniformLocation(programID, U_COLOR);
        uTextureUnitLocation = glGetUniformLocation(programID, U_TEXTURE_UNIT);
        aPositionLocation = glGetAttribLocation(programID, A_POSITION);
    }

    public void setVpMatrix(Matrix4f m) {
        glUniformMatrix4fv(uVpMatrixLocation, false, m.get(new float[16]));
    }

    public void setSize(float size) {
        glUniform1f(uSizeLocation, size);
    }

    public void setColor(Vector3f color) {
        glUniform3f(uColorLocation, color.get(0), color.get(1), color.get(2));
    }

    public void setTexture(int textureID) {
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textureID);
        glUniform1i(uTextureUnitLocation, 0);
    }
}
