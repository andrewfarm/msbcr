package com.andrewofarm.msbcr.objects;

import com.andrewofarm.msbcr.programs.GlobeShaderProgram;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Created by Andrew on 7/4/17.
 */
class GlobeTile extends Object3D {

    private static final int POSITION_COMPONENT_COUNT = 3;
    private static final int NORMAL_COMPONENT_COUNT = 3;
    private static final int TEXTURE_COMPONENT_COUNT = 2;

    private static final int TOTAL_COMPONENT_COUNT =
            POSITION_COMPONENT_COUNT +
                    NORMAL_COMPONENT_COUNT +
                    TEXTURE_COMPONENT_COUNT;

    private final int face;
    private final float radius;
    private final float offsetX, offsetY;
    private final float size;
    private final int resolution;

    public GlobeTile(int face, float radius, float offsetX, float offsetY, float size, int resolution) {
        super(TOTAL_COMPONENT_COUNT * BYTES_PER_FLOAT);
        this.face = face;
        this.radius = radius;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.size = size;
        this.resolution = resolution;

        vertexCount = ObjectBuilder.getTileVertexCount(resolution);
        vertexBuf = newFloatBuffer(vertexCount * TOTAL_COMPONENT_COUNT);
        ObjectBuilder.buildTileVertices((FloatBuffer) vertexBuf, face, radius, offsetX, offsetY, size, resolution);
    }
}
