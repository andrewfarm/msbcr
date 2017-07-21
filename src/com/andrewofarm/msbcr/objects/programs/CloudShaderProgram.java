package com.andrewofarm.msbcr.objects.programs;

import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glUniform1f;

/**
 * Created by Andrew Farm on 7/21/17.
 */
public class CloudShaderProgram extends ShaderProgram {

    private static final String U_MVP_MATRIX = "u_MvpMatrix";
    private static final String U_MODEL_MATRIX = "u_ModelMatrix";
    private static final String U_LIGHT_BIAS_MVP_MATRIX = "u_LightBiasMvpMatrix";
    private static final String U_CLOUD_COVER_UNIT = "u_CloudCoverUnit";
    private static final String U_NOISE_PHASE = "u_NoisePhase";
    private static final String U_LIGHT_DIRECTION = "u_LightDirection";
    private static final String U_CAM_POS = "u_CamPos";
    private static final String U_GLOBE_RADIUS = "u_GlobeRadius";
    private static final String U_ATMOSPHERE_WIDTH = "u_AtmosphereWidth";
    private static final String A_POSITION = "a_Position";
    private static final String A_NORMAL = "a_Normal";
    private static final String A_TEXTURE_COORDS = "a_TextureCoords";

    public final int uMvpMatrixLocation;
    public final int uModelMatrixLocation;
    public final int uLightBiasMvpMatrixLocation;
    public final int uCloudCoverUnitLocation;
    public final int uNoisePhaseLocation;
    public final int uLightDirectionLocation;
    public final int uCamPosLocation;
    public final int uGlobeRadiusLocation;
    public final int uAtmosphereWidthLocation;
    public final int aPositionLocation;
    public final int aNormalLocation;
    public final int aTextureCoordsLocation;

    public CloudShaderProgram() {
        super(TextResourceReader.readFile("src/shaders/cloud_vertex_shader.glsl"),
                TextResourceReader.readFile("src/shaders/atmospheric_scattering.glsl") +
                        TextResourceReader.readFile("src/shaders/webgl-noise/src/classicnoise4D.glsl") +
                        TextResourceReader.readFile("src/shaders/cloud_fragment_shader.glsl"));

        uMvpMatrixLocation = glGetUniformLocation(programID, U_MVP_MATRIX);
        uModelMatrixLocation = glGetUniformLocation(programID, U_MODEL_MATRIX);
        uLightBiasMvpMatrixLocation = glGetUniformLocation(programID, U_LIGHT_BIAS_MVP_MATRIX);
        uCloudCoverUnitLocation = glGetUniformLocation(programID, U_CLOUD_COVER_UNIT);
        uNoisePhaseLocation = glGetUniformLocation(programID, U_NOISE_PHASE);
        uLightDirectionLocation = glGetUniformLocation(programID, U_LIGHT_DIRECTION);
        uCamPosLocation = glGetUniformLocation(programID, U_CAM_POS);
        uGlobeRadiusLocation = glGetUniformLocation(programID, U_GLOBE_RADIUS);
        uAtmosphereWidthLocation = glGetUniformLocation(programID, U_ATMOSPHERE_WIDTH);
        aPositionLocation = glGetAttribLocation(programID, A_POSITION);
        aNormalLocation = glGetAttribLocation(programID, A_NORMAL);
        aTextureCoordsLocation = glGetAttribLocation(programID, A_TEXTURE_COORDS);
    }

    public void setMvpMatrix(Matrix4f m) {
        glUniformMatrix4fv(uMvpMatrixLocation, false, m.get(new float[16]));
    }

    public void setModelMatrix(Matrix4f m) {
        glUniformMatrix4fv(uModelMatrixLocation, false, m.get(new float[16]));
    }

    public void setLightBiasMvpMatrix(Matrix4f m) {
        glUniformMatrix4fv(uLightBiasMvpMatrixLocation, false, m.get(new float[16]));
    }

    public void setCloudCoverUnit(int textureID) {
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textureID);
        glUniform1i(uCloudCoverUnitLocation, 0);
    }

    public void setNoisePhase(float noisePhase) {
        glUniform1f(uNoisePhaseLocation, noisePhase);
    }

    public void setLightDirection(float x, float y, float z) {
        glUniform3f(uLightDirectionLocation, x, y, z);
    }

    public void setCamPos(float x, float y, float z) {
        glUniform3f(uCamPosLocation, x, y, z);
    }

    public void setGlobeRadius(float globeRadius) {
        glUniform1f(uGlobeRadiusLocation, globeRadius);
    }

    public void setAtmosphereWidth(float atmosphereWidth) {
        glUniform1f(uAtmosphereWidthLocation, atmosphereWidth);
    }
}
