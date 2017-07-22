package com.andrewofarm.msbcr.objects.programs;

import static org.lwjgl.opengl.GL20.glGetAttribLocation;

/**
 * Created by Andrew Farm on 7/22/17.
 */
public abstract class ScreenShaderProgram extends ShaderProgram {

    private static final String A_POSITION = "a_Position";
    private static final String A_TEXTURE_COORDS = "a_TextureCoords";

    public final int aPositionLocation;
    public final int aTextureCoordsLocation;

    public ScreenShaderProgram(String vertexShaderSource, String fragmentShaderSource) {
        super(vertexShaderSource, fragmentShaderSource);

        aPositionLocation = glGetAttribLocation(programID, A_POSITION);
        aTextureCoordsLocation = glGetAttribLocation(programID, A_TEXTURE_COORDS);
    }
}
