package com.andrewofarm.msbcr.objects.geom;

import com.andrewofarm.msbcr.objects.programs.RingsShaderProgram;

import java.nio.FloatBuffer;

/**
 * Created by Andrew on 7/1/17.
 */
public class RingsGeometry extends Geometry {

    private static final int POSITION_COMPONENT_COUNT = 3;
    private static final int TEXTURE_COMPONENT_COUNT = 1;

    private static final int TOTAL_COMPONENT_COUNT =
            POSITION_COMPONENT_COUNT +
                    TEXTURE_COMPONENT_COUNT;

    private final float innerRadius;
    private final float outerRadius;

    public RingsGeometry(int segments, float innerRadius, float outerRadius) {
        super(TOTAL_COMPONENT_COUNT * BYTES_PER_FLOAT);
        this.innerRadius = innerRadius;
        this.outerRadius = outerRadius;

        vertexCount = ObjectBuilder.getRingsVertexCount(segments);
        vertexBuf = newFloatBuffer(vertexCount * TOTAL_COMPONENT_COUNT);
        ObjectBuilder.buildTexturedRings((FloatBuffer) vertexBuf, innerRadius, outerRadius, segments);
    }

    public float getInnerRadius() {
        return innerRadius;
    }

    public float getOuterRadius() {
        return outerRadius;
    }

    public void draw(RingsShaderProgram shaderProgram) {
        setDataOffset(0);
        bindFloatAttribute(shaderProgram.aPositionLocation, POSITION_COMPONENT_COUNT);
        bindFloatAttribute(shaderProgram.aTexCoordsLocation, TEXTURE_COMPONENT_COUNT);
        drawArrays(MODE_TRIANGLE_STRIP);
    }
}
