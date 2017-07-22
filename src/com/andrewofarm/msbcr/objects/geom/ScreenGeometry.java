package com.andrewofarm.msbcr.objects.geom;

import com.andrewofarm.msbcr.objects.programs.ScreenShaderProgram;
import com.andrewofarm.msbcr.objects.programs.SkyboxShaderProgram;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

/**
 * Created by Andrew on 7/1/17.
 */
public class ScreenGeometry extends Geometry {

    private static final float[] vertices = {
         1,  1,  1,  1,
        -1,  1,  0,  1,
         1, -1,  1,  0,
        -1, -1,  0,  0,
    };

    private static final int POSITION_COMPONENT_COUNT = 2;
    private static final int TEXTURE_COMPONENT_COUNT = 2;

    private static final int TOTAL_COMPONENT_COUNT =
            POSITION_COMPONENT_COUNT +
            TEXTURE_COMPONENT_COUNT;

    public ScreenGeometry() {
        super(TOTAL_COMPONENT_COUNT * BYTES_PER_FLOAT);

        vertexCount = 4;
        vertexBuf = newFloatBuffer(vertexCount * TOTAL_COMPONENT_COUNT).put(vertices);
    }

    public void draw(ScreenShaderProgram shaderProgram) {
        setDataOffset(0);
        bindFloatAttribute(shaderProgram.aPositionLocation, POSITION_COMPONENT_COUNT);
        bindFloatAttribute(shaderProgram.aTextureCoordsLocation, TEXTURE_COMPONENT_COUNT);
        drawArrays(MODE_TRIANGLE_STRIP);
    }
}
