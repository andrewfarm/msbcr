package com.andrewofarm.msbcr.objects;

import com.andrewofarm.msbcr.programs.AuroraShaderProgram;

import java.nio.FloatBuffer;

/**
 * Created by Andrew on 7/1/17.
 */
public class Aurora extends Object3D {

    private static final int POSITION_COMPONENT_COUNT = 2;
    private static final int TEXTURE_COMPONENT_COUNT = 1;

    private static final int TOTAL_COMPONENT_COUNT =
            POSITION_COMPONENT_COUNT +
                    TEXTURE_COMPONENT_COUNT;

    private final float lowerBound;
    private final float outerRadius;

    public Aurora(int segments, float lowerBound, float outerRadius) {
        super(TOTAL_COMPONENT_COUNT * BYTES_PER_FLOAT);
        this.lowerBound = lowerBound;
        this.outerRadius = outerRadius;

        vertexCount = ObjectBuilder.getAuroraVertexCount(segments);
        vertexBuf = newFloatBuffer(vertexCount * TOTAL_COMPONENT_COUNT);
        ObjectBuilder.buildAuroraVertices((FloatBuffer) vertexBuf, lowerBound, outerRadius, segments);
    }

    public float getLowerBound() {
        return lowerBound;
    }

    public float getOuterRadius() {
        return outerRadius;
    }

    public void draw(AuroraShaderProgram shaderProgram) {
        setDataOffset(0);
        bindFloatAttribute(shaderProgram.aPositionLocation, POSITION_COMPONENT_COUNT);
        bindFloatAttribute(shaderProgram.aTextureCoordsLocation, TEXTURE_COMPONENT_COUNT);
        drawArrays(MODE_TRIANGLE_STRIP);
    }
}
