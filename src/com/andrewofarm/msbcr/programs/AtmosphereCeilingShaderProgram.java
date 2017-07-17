package com.andrewofarm.msbcr.programs;

import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL20.*;

/**
 * Created by Andrew on 6/18/17.
 */
public class AtmosphereCeilingShaderProgram extends ShaderProgram {

    private static final String U_MVP_MATRIX = "u_MvpMatrix";
    private static final String U_MODEL_MATRIX = "u_ModelMatrix";
    private static final String U_LIGHT_DIRECTION = "u_LightDirection";
    private static final String U_CAM_POS = "u_CamPos";
    private static final String U_GLOBE_RADIUS = "u_GlobeRadius";
    private static final String U_ATMOSPHERE_WIDTH = "u_AtmosphereWidth";
    private static final String A_POSITION = "a_Position";

    public final int uMvpMatrixLocation;
    public final int uModelMatrixLocation;
    public final int uLightDirectionLocation;
    public final int uCamPosLocation;
    public final int uGlobeRadiusLocation;
    public final int uAtmosphereWidthLocation;
    public final int aPositionLocation;

    public AtmosphereCeilingShaderProgram() {
        super(TextResourceReader.readFile("src/shaders/atmosphere_ceiling_vertex_shader.glsl"),
                TextResourceReader.readFile("src/shaders/atmospheric_scattering.glsl") +
                        TextResourceReader.readFile("src/shaders/atmosphere_ceiling_fragment_shader.glsl"));

        uMvpMatrixLocation = glGetUniformLocation(programID, U_MVP_MATRIX);
        uModelMatrixLocation = glGetUniformLocation(programID, U_MODEL_MATRIX);
        uLightDirectionLocation = glGetUniformLocation(programID, U_LIGHT_DIRECTION);
        uCamPosLocation = glGetUniformLocation(programID, U_CAM_POS);
        uGlobeRadiusLocation = glGetUniformLocation(programID, U_GLOBE_RADIUS);
        uAtmosphereWidthLocation = glGetUniformLocation(programID, U_ATMOSPHERE_WIDTH);
        aPositionLocation = glGetAttribLocation(programID, A_POSITION);
    }
    
    public void setMvpMatrix(Matrix4f m) {
        glUniformMatrix4fv(uMvpMatrixLocation, false, m.get(new float[16]));
    }

    public void setModelMatrix(Matrix4f m) {
        glUniformMatrix4fv(uModelMatrixLocation, false, m.get(new float[16]));
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
