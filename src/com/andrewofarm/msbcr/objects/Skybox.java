package com.andrewofarm.msbcr.objects;

import com.andrewofarm.msbcr.programs.SkyboxShaderProgram;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

/**
 * Created by Andrew on 7/1/17.
 */
public class Skybox extends Object3D {

    private static final int POSITION_COMPONENT_COUNT = 3;

    private static final int TOTAL_COMPONENT_COUNT =
            POSITION_COMPONENT_COUNT;

    public Skybox() {
        super(TOTAL_COMPONENT_COUNT * BYTES_PER_FLOAT);

        vertexCount = ObjectBuilder.getSkyboxVertexCount();
        indexCount = ObjectBuilder.getSkyboxIndexCount();
        vertexBuf = newFloatBuffer(vertexCount * TOTAL_COMPONENT_COUNT);
        indexBuf = newByteBuffer(indexCount);
        ObjectBuilder.buildSkybox((FloatBuffer) vertexBuf, (ByteBuffer) indexBuf);
    }

    public void draw(SkyboxShaderProgram shaderProgram) {
        setDataOffset(0);
        bindFloatAttribute(shaderProgram.aPositionLocation, POSITION_COMPONENT_COUNT);
        drawElements(MODE_TRIANGLES);
    }
}
