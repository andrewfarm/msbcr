package com.andrewofarm.msbcr.objects.geom;

import com.andrewofarm.msbcr.objects.programs.CloudShaderProgram;
import com.andrewofarm.msbcr.objects.programs.OceanShaderProgram;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Created by Andrew on 7/1/17.
 */
public class OceanGeometry extends Geometry {
    private static final int POSITION_COMPONENT_COUNT = 3;
    private static final int NORMAL_COMPONENT_COUNT = 3;
    private static final int TEXTURE_COMPONENT_COUNT = 2;


    private static final int TOTAL_COMPONENT_COUNT =
            POSITION_COMPONENT_COUNT +
                NORMAL_COMPONENT_COUNT +
                TEXTURE_COMPONENT_COUNT;

    private final float radius;

    public OceanGeometry(float radius, int meridians, int parallels) {
        super(TOTAL_COMPONENT_COUNT * BYTES_PER_FLOAT);
        this.radius = radius;

        vertexCount = ObjectBuilder.getSphereVertexCount(meridians, parallels);
        indexCount = ObjectBuilder.getSphereIndexCount(meridians, parallels);
        vertexBuf = newFloatBuffer(vertexCount * TOTAL_COMPONENT_COUNT);
        indexBuf = newIntBuffer(indexCount);
        ObjectBuilder.buildSphere((FloatBuffer) vertexBuf, (IntBuffer) indexBuf,
                radius, meridians, parallels, true, true);
    }

    public float getRadius() {
        return radius;
    }

    public void draw(OceanShaderProgram shaderProgram) {
        setDataOffset(0);
        bindFloatAttribute(shaderProgram.aPositionLocation, POSITION_COMPONENT_COUNT);
        bindFloatAttribute(shaderProgram.aNormalLocation, NORMAL_COMPONENT_COUNT);
        bindFloatAttribute(shaderProgram.aTextureCoordsLocation, TEXTURE_COMPONENT_COUNT);
        drawElements(MODE_TRIANGLE_STRIP);
    }

    public void draw(CloudShaderProgram shaderProgram) {
        setDataOffset(0);
        bindFloatAttribute(shaderProgram.aPositionLocation, POSITION_COMPONENT_COUNT);
        bindFloatAttribute(shaderProgram.aNormalLocation, NORMAL_COMPONENT_COUNT);
        bindFloatAttribute(shaderProgram.aTextureCoordsLocation, TEXTURE_COMPONENT_COUNT);
        drawElements(MODE_TRIANGLE_STRIP);
    }
}
