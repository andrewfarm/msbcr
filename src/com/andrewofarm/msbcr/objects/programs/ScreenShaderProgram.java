package com.andrewofarm.msbcr.objects.programs;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL20.glGetAttribLocation;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1i;

/**
 * Created by Andrew Farm on 7/22/17.
 */
public abstract class ScreenShaderProgram extends ShaderProgram {

    private static final String U_TEXTURE_UNIT = "u_TextureUnit";
    private static final String A_POSITION = "a_Position";
    private static final String A_TEXTURE_COORDS = "a_TextureCoords";

    public final int uTextureUnitLocation;
    public final int aPositionLocation;
    public final int aTextureCoordsLocation;

    public ScreenShaderProgram(String vertexShaderSource, String fragmentShaderSource) {
        super(vertexShaderSource, fragmentShaderSource);

        uTextureUnitLocation = glGetUniformLocation(programID, U_TEXTURE_UNIT);
        aPositionLocation = glGetAttribLocation(programID, A_POSITION);
        aTextureCoordsLocation = glGetAttribLocation(programID, A_TEXTURE_COORDS);
    }

    public void setTexture(int textureID) {
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textureID);
        glUniform1i(uTextureUnitLocation, 0);
    }
}
