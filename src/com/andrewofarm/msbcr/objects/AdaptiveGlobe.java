package com.andrewofarm.msbcr.objects;

import com.andrewofarm.msbcr.programs.GlobeShaderProgram;
import com.andrewofarm.msbcr.programs.ShadowMapShaderProgram;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashSet;
import java.util.Set;

import static com.andrewofarm.msbcr.objects.ObjectBuilder.*;

/**
 * Created by Andrew on 7/1/17.
 */
public class AdaptiveGlobe extends Object3D {

    private static final int POSITION_COMPONENT_COUNT = 3;
    private static final int NORMAL_COMPONENT_COUNT = 3;
    private static final int TEXTURE_COMPONENT_COUNT = 2;

    private static final int TOTAL_COMPONENT_COUNT =
            POSITION_COMPONENT_COUNT +
                    NORMAL_COMPONENT_COUNT +
                    TEXTURE_COMPONENT_COUNT;

    private static final int TILE_RESOLUTION = 16;

    private static final boolean WIREFRAME = true;

    private final float radius;

    private Set<GlobeTile> tiles = new HashSet<>();

    public AdaptiveGlobe(float radius, int meridians, int parallels) {
        super(TOTAL_COMPONENT_COUNT * BYTES_PER_FLOAT);
        this.radius = radius;

        tiles.add(new GlobeTile(CUBE_RIGHT,  radius, 0, 0, 1, TILE_RESOLUTION));
        tiles.add(new GlobeTile(CUBE_LEFT,   radius, 0, 0, 1, TILE_RESOLUTION));
        tiles.add(new GlobeTile(CUBE_TOP,    radius, 0, 0, 1, TILE_RESOLUTION));
        tiles.add(new GlobeTile(CUBE_BOTTOM, radius, 0, 0, 1, TILE_RESOLUTION));
        tiles.add(new GlobeTile(CUBE_FRONT,  radius, 0, 0, 1, TILE_RESOLUTION));
        tiles.add(new GlobeTile(CUBE_BACK,   radius, 0, 0, 1, TILE_RESOLUTION));

        if (WIREFRAME) {
            indexCount = ObjectBuilder.getWireframeTileIndexCount(TILE_RESOLUTION);
            indexBuf = newIntBuffer(indexCount);
            ObjectBuilder.buildWireframeTileIndices((IntBuffer) indexBuf, TILE_RESOLUTION);
        } else {
            indexCount = ObjectBuilder.getTileIndexCount(TILE_RESOLUTION);
            indexBuf = newIntBuffer(indexCount);
            ObjectBuilder.buildTileIndices((IntBuffer) indexBuf, TILE_RESOLUTION);
        }

//        vertexCount = ObjectBuilder.getSphereVertexCount(meridians, parallels);
//        indexCount = ObjectBuilder.getSphereIndexCount(meridians, parallels);
//        vertexBuf = newFloatBuffer(vertexCount * TOTAL_COMPONENT_COUNT);
//        indexBuf = newIntBuffer(indexCount);
//        ObjectBuilder.buildSphere((FloatBuffer) vertexBuf, (IntBuffer) indexBuf,
//                radius, meridians, parallels, true);
    }

    public float getRadius() {
        return radius;
    }

    public void update(Matrix4f mvpMatrix) {
        for (GlobeTile tile : tiles) {
            //TODO
        }
    }

    public void draw(GlobeShaderProgram shaderProgram) {
//        setDataOffset(0);
//        bindFloatAttribute(shaderProgram.aPositionLocation, POSITION_COMPONENT_COUNT);
//        bindFloatAttribute(shaderProgram.aNormalLocation, NORMAL_COMPONENT_COUNT);
//        bindFloatAttribute(shaderProgram.aTextureCoordsLocation, TEXTURE_COMPONENT_COUNT);
//        drawElements(MODE_TRIANGLE_STRIP);

        FloatBuffer tileBuf;
        for (GlobeTile tile : tiles) {
            tileBuf = (FloatBuffer) tile.vertexBuf;
            setDataOffset(0);
            bindFloatAttribute(shaderProgram.aPositionLocation, POSITION_COMPONENT_COUNT, tileBuf);
            bindFloatAttribute(shaderProgram.aNormalLocation, NORMAL_COMPONENT_COUNT, tileBuf);
            bindFloatAttribute(shaderProgram.aTextureCoordsLocation, TEXTURE_COMPONENT_COUNT, tileBuf);
            drawElements(WIREFRAME ? MODE_LINES : MODE_TRIANGLE_STRIP);
        }
    }

    public void draw(ShadowMapShaderProgram shaderProgram) {
//        setDataOffset(0);
//        bindFloatAttribute(shaderProgram.aPositionLocation, POSITION_COMPONENT_COUNT);
//        skipAttributes(NORMAL_COMPONENT_COUNT);
//        bindFloatAttribute(shaderProgram.aTextureCoordsLocation, TEXTURE_COMPONENT_COUNT);
//        drawElements(MODE_TRIANGLE_STRIP);

        FloatBuffer tileBuf;
        for (GlobeTile tile : tiles) {
            tileBuf = (FloatBuffer) tile.vertexBuf;
            setDataOffset(0);
            bindFloatAttribute(shaderProgram.aPositionLocation, POSITION_COMPONENT_COUNT, tileBuf);
            skipAttributes(NORMAL_COMPONENT_COUNT);
            bindFloatAttribute(shaderProgram.aTextureCoordsLocation, TEXTURE_COMPONENT_COUNT, tileBuf);
            drawElements(MODE_TRIANGLES);
        }
    }
}
