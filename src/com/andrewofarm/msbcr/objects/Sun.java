package com.andrewofarm.msbcr.objects;

import com.andrewofarm.msbcr.programs.SunShaderProgram;

import java.nio.FloatBuffer;

/**
 * Created by Andrew on 7/2/17.
 */
public class Sun extends Object3D {

    private static final int POSITION_COMPONENT_COUNT = 3;

    private static final int TOTAL_COMPONENT_COUNT =
            POSITION_COMPONENT_COUNT;

    public Sun(float x, float y, float z) {
        super(TOTAL_COMPONENT_COUNT * BYTES_PER_FLOAT);

        vertexCount = 1;
        vertexBuf = newFloatBuffer(vertexCount * TOTAL_COMPONENT_COUNT);
        ((FloatBuffer) vertexBuf).put(new float[] {x, y, z});
    }

    public void draw(SunShaderProgram shaderProgram) {
        setDataOffset(0);
        bindFloatAttribute(shaderProgram.aPositionLocation, POSITION_COMPONENT_COUNT);
        drawArrays(MODE_POINTS);
    }
}
