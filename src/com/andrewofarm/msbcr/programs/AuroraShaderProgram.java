package com.andrewofarm.msbcr.programs;

import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;

/**
 * Created by Andrew on 7/1/17.
 */
public class AuroraShaderProgram extends ShaderProgram {

    private static final String U_MVP_MATRIX = "u_MvpMatrix";
    private static final String U_TEXTURE_UNIT = "u_TextureUnit";
    private static final String U_POLAR_ANGLE = "u_PolarAngle";
    private static final String U_NOISE_PHASE = "u_NoisePhase";
    private static final String A_POSITION = "a_Position";
    private static final String A_TEXTURE_COORDS = "a_TextureCoords";

    public final int uMvpMatrixLocation;
    public final int uTextureUnitLocation;
    public final int uPolarAngleLocation;
    public final int uNoisePhaseLocation;
    public final int aPositionLocation;
    public final int aTextureCoordsLocation;

    public AuroraShaderProgram() {
        super(TextResourceReader.readFile("src/shaders/aurora_vertex_shader.glsl") +
                        TextResourceReader.readFile("src/shaders/classicnoise3D.glsl"),
                TextResourceReader.readFile("src/shaders/aurora_fragment_shader.glsl") +
                        TextResourceReader.readFile("src/shaders/classicnoise3D.glsl"));

        uMvpMatrixLocation = glGetUniformLocation(programID, U_MVP_MATRIX);
        uTextureUnitLocation = glGetUniformLocation(programID, U_TEXTURE_UNIT);
        uPolarAngleLocation = glGetUniformLocation(programID, U_POLAR_ANGLE);
        uNoisePhaseLocation = glGetUniformLocation(programID, U_NOISE_PHASE);
        aPositionLocation = glGetAttribLocation(programID, A_POSITION);
        aTextureCoordsLocation = glGetAttribLocation(programID, A_TEXTURE_COORDS);
    }

    public void setMvpMatrix(Matrix4f m) {
        glUniformMatrix4fv(uMvpMatrixLocation, false, m.get(new float[16]));
    }

    public void setTexture(int textureID) {
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_1D, textureID);
        glUniform1i(uTextureUnitLocation, 0);
    }

    public void setPolarAngle(float polarAngle) {
        glUniform1f(uPolarAngleLocation, polarAngle);
    }

    public void setNoisePhase(float noisePhase) {
        glUniform1f(uNoisePhaseLocation, noisePhase);
    }
}
