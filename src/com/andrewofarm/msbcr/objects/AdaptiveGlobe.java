package com.andrewofarm.msbcr.objects;

import com.andrewofarm.msbcr.programs.GlobeShaderProgram;
import com.andrewofarm.msbcr.programs.ShaderProgram;
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

    private int tileResolution = 16;

    private static final boolean WIREFRAME = true;
    private static final boolean DRAW_CORNERS = true;

    private final float radius;

    private Set<QuadTree<GlobeTile>> tiles = new HashSet<>();

    public AdaptiveGlobe(float radius, int tileResolution) {
        super(TOTAL_COMPONENT_COUNT * BYTES_PER_FLOAT);
        this.radius = radius;

        tiles.add(new QuadTree<>(new GlobeTile(CUBE_RIGHT,  radius, 0, 0, 1, tileResolution)));
        tiles.add(new QuadTree<>(new GlobeTile(CUBE_LEFT,   radius, 0, 0, 1, tileResolution)));
        tiles.add(new QuadTree<>(new GlobeTile(CUBE_TOP,    radius, 0, 0, 1, tileResolution)));
        tiles.add(new QuadTree<>(new GlobeTile(CUBE_BOTTOM, radius, 0, 0, 1, tileResolution)));
        tiles.add(new QuadTree<>(new GlobeTile(CUBE_FRONT,  radius, 0, 0, 1, tileResolution)));
        tiles.add(new QuadTree<>(new GlobeTile(CUBE_BACK,   radius, 0, 0, 1, tileResolution)));

        if (WIREFRAME) {
            indexCount = ObjectBuilder.getWireframeTileIndexCount(tileResolution);
            indexBuf = newIntBuffer(indexCount);
            ObjectBuilder.buildWireframeTileIndices((IntBuffer) indexBuf, tileResolution);
        } else {
            indexCount = ObjectBuilder.getTileIndexCount(tileResolution);
            indexBuf = newIntBuffer(indexCount);
            ObjectBuilder.buildTileIndices((IntBuffer) indexBuf, tileResolution);
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
        for (QuadTree<GlobeTile> tileTree : tiles) {
            //TODO
        }
    }

    public void draw(ShaderProgram shaderProgram) {
//        setDataOffset(0);
//        bindFloatAttribute(shaderProgram.aPositionLocation, POSITION_COMPONENT_COUNT);
//        bindFloatAttribute(shaderProgram.aNormalLocation, NORMAL_COMPONENT_COUNT);
//        bindFloatAttribute(shaderProgram.aTextureCoordsLocation, TEXTURE_COMPONENT_COUNT);
//        drawElements(MODE_TRIANGLE_STRIP);

        for (QuadTree<GlobeTile> tileTree : tiles) {
            drawTiles(shaderProgram, tileTree);
        }
    }

    private void drawTiles(ShaderProgram shaderProgram, QuadTree<GlobeTile> tileTree) {
        if (tileTree.isLeaf()) {
            if (shaderProgram instanceof GlobeShaderProgram) {
                drawTile((GlobeShaderProgram) shaderProgram, tileTree.getValue());
            } else if (shaderProgram instanceof ShadowMapShaderProgram) {
                drawTile((ShadowMapShaderProgram) shaderProgram, tileTree.getValue());
            }
        } else {
            drawTiles(shaderProgram, tileTree.getTopLeft());
            drawTiles(shaderProgram, tileTree.getTopRight());
            drawTiles(shaderProgram, tileTree.getBottomLeft());
            drawTiles(shaderProgram, tileTree.getBottomRight());
        }
    }

    private void drawTile(GlobeShaderProgram shaderProgram, GlobeTile tile) {
        FloatBuffer tileBuf = (FloatBuffer) tile.vertexBuf;
        setDataOffset(0);
        bindFloatAttribute(shaderProgram.aPositionLocation, POSITION_COMPONENT_COUNT, tileBuf);
        bindFloatAttribute(shaderProgram.aNormalLocation, NORMAL_COMPONENT_COUNT, tileBuf);
        bindFloatAttribute(shaderProgram.aTextureCoordsLocation, TEXTURE_COMPONENT_COUNT, tileBuf);
        drawElements(WIREFRAME ? MODE_LINES : MODE_TRIANGLE_STRIP);

        if (DRAW_CORNERS) {
            FloatBuffer cornerVertexBuf = tile.getCornerVertexBuffer();
            setDataOffset(0);
            bindFloatAttribute(shaderProgram.aPositionLocation, POSITION_COMPONENT_COUNT, cornerVertexBuf);
            bindFloatAttribute(shaderProgram.aNormalLocation, NORMAL_COMPONENT_COUNT, cornerVertexBuf);
            bindFloatAttribute(shaderProgram.aTextureCoordsLocation, TEXTURE_COMPONENT_COUNT, cornerVertexBuf);
            drawArrays(MODE_POINTS, 0, 4);
        }
    }

    public void drawTile(ShadowMapShaderProgram shaderProgram, GlobeTile tile) {
        FloatBuffer tileBuf = (FloatBuffer) tile.vertexBuf;
        setDataOffset(0);
        bindFloatAttribute(shaderProgram.aPositionLocation, POSITION_COMPONENT_COUNT, tileBuf);
        skipAttributes(NORMAL_COMPONENT_COUNT);
        bindFloatAttribute(shaderProgram.aTextureCoordsLocation, TEXTURE_COMPONENT_COUNT, tileBuf);
        drawElements(MODE_TRIANGLES);
    }
}
