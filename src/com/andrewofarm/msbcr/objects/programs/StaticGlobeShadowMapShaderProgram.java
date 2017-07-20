package com.andrewofarm.msbcr.objects.programs;

import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL20.*;

/**
 * Created by Andrew on 6/18/17.
 */
public class StaticGlobeShadowMapShaderProgram extends ShaderProgram {

    private static final String U_LIGHT_MVP_MATRIX = "u_LightMvpMatrix";
    private static final String U_DISPLACEMENT_MAP_UNIT = "u_DisplacementMapUnit";
    private static final String U_SEA_LEVEL = "u_SeaLevel";
    private static final String U_TERRAIN_SCALE = "u_TerrainScale";
    private static final String A_POSITION = "a_Position";
    private static final String A_TEXTURE_COORDS = "a_TextureCoords";

    public final int uLightMvpMatrixLocation;
    public final int uDisplacementMapUnitLocation;
    public final int uSeaLevelLocation;
    public final int uTerrainScaleLocation;
    public final int aPositionLocation;
    public final int aTextureCoordsLocation;

    public StaticGlobeShadowMapShaderProgram() {
        super(TextResourceReader.readFile("src/shaders/static_globe_shadowmap_vertex_shader.glsl"),
                TextResourceReader.readFile("src/shaders/shadowmap_fragment_shader.glsl"));

        uLightMvpMatrixLocation = glGetUniformLocation(programID, U_LIGHT_MVP_MATRIX);
        uDisplacementMapUnitLocation = glGetUniformLocation(programID, U_DISPLACEMENT_MAP_UNIT);
        uSeaLevelLocation = glGetUniformLocation(programID, U_SEA_LEVEL);
        uTerrainScaleLocation = glGetUniformLocation(programID, U_TERRAIN_SCALE);
        aPositionLocation = glGetAttribLocation(programID, A_POSITION);
        aTextureCoordsLocation = glGetAttribLocation(programID, A_TEXTURE_COORDS);
    }

    public void setLightMvpMatrix(Matrix4f m) {
        glUniformMatrix4fv(uLightMvpMatrixLocation, false, m.get(new float[16]));
    }

    public void setDisplacementMap(int displacementMapID) {
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, displacementMapID);
        glUniform1i(uDisplacementMapUnitLocation, 0);
    }

    public void setSeaLevel(float seaLevel) {
        glUniform1f(uSeaLevelLocation, seaLevel);
    }

    public void setTerrainScale(float terrainScale) {
        glUniform1f(uTerrainScaleLocation, terrainScale);
    }
}
