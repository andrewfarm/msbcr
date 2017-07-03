package com.andrewofarm.msbcr.objects;

import com.andrewofarm.msbcr.programs.GlobeShaderProgram;
import com.andrewofarm.msbcr.programs.ShadowMapShaderProgram;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Created by Andrew on 7/1/17.
 */
public class HierarchialGlobe extends Globe {

    private static final int POSITION_COMPONENT_COUNT = 3;
    private static final int NORMAL_COMPONENT_COUNT = 3;
    private static final int TEXTURE_COMPONENT_COUNT = 2;

    private static final int TOTAL_COMPONENT_COUNT =
            POSITION_COMPONENT_COUNT +
                    NORMAL_COMPONENT_COUNT +
                    TEXTURE_COMPONENT_COUNT;

    private static final int MAX_DETAIL_LEVELS = 2;

    public HierarchialGlobe(float radius, int meridians, int parallels) {
        super(radius, meridians, parallels);

        vertexCount = ObjectBuilder.getSphereVertexCount(meridians, parallels) * MAX_DETAIL_LEVELS;
        indexCount = ObjectBuilder.getSphereIndexCount(meridians, parallels) * MAX_DETAIL_LEVELS;
        vertexBuf = newFloatBuffer(vertexCount * TOTAL_COMPONENT_COUNT);
        indexBuf = newIntBuffer(indexCount);
        ObjectBuilder.buildSphere((FloatBuffer) vertexBuf, (IntBuffer) indexBuf,
                radius, meridians, parallels, true);
    }
}
