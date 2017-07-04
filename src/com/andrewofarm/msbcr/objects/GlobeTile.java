package com.andrewofarm.msbcr.objects;

import org.joml.Vector3f;

import java.nio.FloatBuffer;

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

    private FloatBuffer cornerVertexBuf;
    private final Vector3f[] corners;

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
        FloatBuffer floatVertexBuf = (FloatBuffer) vertexBuf;
        ObjectBuilder.buildTileVertices(floatVertexBuf, face, radius, offsetX, offsetY, size, resolution);

        int[] cornerIndices = {
                0,
                resolution * TOTAL_COMPONENT_COUNT,
                (resolution + 1) * resolution * TOTAL_COMPONENT_COUNT,
                ((resolution + 1) * resolution + resolution) * TOTAL_COMPONENT_COUNT
        };
        cornerVertexBuf = newFloatBuffer(4 * TOTAL_COMPONENT_COUNT).put(new float[] {
                floatVertexBuf.get(cornerIndices[0]),
                floatVertexBuf.get(cornerIndices[0] + 1),
                floatVertexBuf.get(cornerIndices[0] + 2),
                1, 1, 1, 1, 1,
                floatVertexBuf.get(cornerIndices[1]),
                floatVertexBuf.get(cornerIndices[1] + 1),
                floatVertexBuf.get(cornerIndices[1] + 2),
                1, 1, 1, 1, 1,
                floatVertexBuf.get(cornerIndices[2]),
                floatVertexBuf.get(cornerIndices[2] + 1),
                floatVertexBuf.get(cornerIndices[2] + 2),
                1, 1, 1, 1, 1,
                floatVertexBuf.get(cornerIndices[3]),
                floatVertexBuf.get(cornerIndices[3] + 1),
                floatVertexBuf.get(cornerIndices[3] + 2),
                1, 1, 1, 1, 1,
        });
        corners = new Vector3f[] {
                new Vector3f(
                        cornerVertexBuf.get(0),
                        cornerVertexBuf.get(1),
                        cornerVertexBuf.get(2)),
                new Vector3f(
                        cornerVertexBuf.get(TOTAL_COMPONENT_COUNT),
                        cornerVertexBuf.get(TOTAL_COMPONENT_COUNT + 1),
                        cornerVertexBuf.get(TOTAL_COMPONENT_COUNT + 1)),
                new Vector3f(
                        cornerVertexBuf.get(TOTAL_COMPONENT_COUNT * 2),
                        cornerVertexBuf.get(TOTAL_COMPONENT_COUNT * 2 + 1),
                        cornerVertexBuf.get(TOTAL_COMPONENT_COUNT * 2 + 2)),
                new Vector3f(
                        cornerVertexBuf.get(TOTAL_COMPONENT_COUNT * 3),
                        cornerVertexBuf.get(TOTAL_COMPONENT_COUNT * 3 + 1),
                        cornerVertexBuf.get(TOTAL_COMPONENT_COUNT * 3 + 2)),
        };
    }

    public float getOffsetX() {
        return offsetX;
    }

    public float getOffsetY() {
        return offsetY;
    }

    public float getSize() {
        return size;
    }

    public Vector3f[] getCorners() {
        return corners;
    }

    public FloatBuffer getCornerVertexBuffer() {
        return cornerVertexBuf;
    }
}
