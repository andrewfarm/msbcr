package com.andrewofarm.msbcr.objects;

import org.lwjgl.opengl.GL11;

import java.nio.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;

/**
 * Created by Andrew on 7/1/17.
 */
public abstract class Object3D {

    public final int STRIDE; //vertex stride, in bytes

    static final int MODE_LINES = GL11.GL_LINES;
    static final int MODE_LINE_STRIP = GL11.GL_LINE_STRIP;
    static final int MODE_TRIANGLES = GL11.GL_TRIANGLES;
    static final int MODE_TRIANGLE_STRIP = GL11.GL_TRIANGLE_STRIP;
    static final int MODE_TRIANGLE_FAN = GL_TRIANGLE_FAN;
    static final int MODE_POINTS = GL_POINTS;

    static final int BYTES_PER_FLOAT = 4;
    static final int BYTES_PER_INT = 4;

    protected Buffer vertexBuf;
    protected Buffer indexBuf;
    protected int vertexCount;
    protected int indexCount;

    private int dataOffset;

    public Object3D(int stride) {
        STRIDE = stride;
    }

    public int getDataOffset() {
        return dataOffset;
    }

    public void setDataOffset(int dataOffset) {
        this.dataOffset = dataOffset;
    }

    public void skipAttributes(int skipComponents) {
        dataOffset += skipComponents;
    }

    protected void bindFloatAttribute(int index, int size) {
        vertexBuf.position(dataOffset);
        glVertexAttribPointer(index, size, GL_FLOAT, false, STRIDE, (FloatBuffer) vertexBuf);
        glEnableVertexAttribArray(index);
        dataOffset += size;
    }

    protected void bindFloatAttribute(int index, int size, FloatBuffer vertexBuf) {
        vertexBuf.position(dataOffset);
        glVertexAttribPointer(index, size, GL_FLOAT, false, STRIDE, vertexBuf);
        glEnableVertexAttribArray(index);
        dataOffset += size;
    }

    protected void drawArrays(int mode) {
        glDrawArrays(mode, 0, vertexCount);
    }

    protected void drawArrays(int mode, int first, int count) {
        glDrawArrays(mode, first, count);
    }

    protected void drawElements(int mode) {
        indexBuf.position(0);
        if (indexBuf instanceof IntBuffer) {
            glDrawElements(mode, (IntBuffer) indexBuf);
        } else if (indexBuf instanceof ShortBuffer) {
            glDrawElements(mode, (ShortBuffer) indexBuf);
        } else if (indexBuf instanceof ByteBuffer) {
            glDrawElements(mode, (ByteBuffer) indexBuf);
        } else {
            System.err.println("unknown index buffer type: " + indexBuf.getClass().getName());
        }
    }

    protected FloatBuffer newFloatBuffer(int elements) {
        return newNativeOrderedBuffer(elements * BYTES_PER_FLOAT).asFloatBuffer();
    }

    protected IntBuffer newIntBuffer(int elements) {
        return newNativeOrderedBuffer(elements * BYTES_PER_INT).asIntBuffer();
    }

    protected ByteBuffer newByteBuffer(int elements) {
        return ByteBuffer.allocateDirect(elements);
    }

    private ByteBuffer newNativeOrderedBuffer(int capacity) {
        return ByteBuffer.allocateDirect(capacity).order(ByteOrder.nativeOrder());
    }
}
