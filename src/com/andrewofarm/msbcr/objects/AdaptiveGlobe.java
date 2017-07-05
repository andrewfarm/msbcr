package com.andrewofarm.msbcr.objects;

import com.andrewofarm.msbcr.programs.GlobeShaderProgram;
import com.andrewofarm.msbcr.programs.ShaderProgram;
import com.andrewofarm.msbcr.programs.ShadowMapShaderProgram;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

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

    private static final int MAX_DETAIL_LEVEL = 5;

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
    }

    public float getRadius() {
        return radius;
    }

    private static int getDesiredDetailLevel(float depth) {
        return (int) (1 / depth);
    }

    private static float getDepthOfClosestCorner(Matrix4f matrix, GlobeTile tile) {
        Vector3f[] corners = tile.getCorners();
        float minDepth = Float.POSITIVE_INFINITY;
        for (Vector3f corner : corners) {
            minDepth = Math.min(minDepth, Math.abs(
                    matrix.transformAffine(corner.get(0), corner.get(1), corner.get(2), 1.0f, new Vector4f()).get(2)));
        }
        return minDepth;
    }

    public void update(Matrix4f matrix) {
        for (QuadTree<GlobeTile> tileTree : tiles) {
            updateTiles(matrix, tileTree, 0);
        }
    }

    private void updateTiles(Matrix4f matrix, QuadTree<GlobeTile> tileTree, int level) {
        GlobeTile tile = tileTree.getValue();
        int desiredDetailLevel = getDesiredDetailLevel(getDepthOfClosestCorner(matrix, tile));
        if (desiredDetailLevel <= level) {
            //no further detail is needed
            tileTree.removeChildren();
        } else {
            if (tileTree.isLeaf()) {
                //split tile into quarters
                int face = tile.getFace();
                float radius = tile.getRadius();
                float offsetX = tile.getOffsetX();
                float offsetY = tile.getOffsetY();
                float newSize = tile.getSize() * 0.5f;
                int resolution = tile.getResolution();
                tileTree.addChildren(
                        new GlobeTile(face, radius, offsetX, offsetY, newSize, resolution),
                        new GlobeTile(face, radius, offsetX + newSize, offsetY, newSize, resolution),
                        new GlobeTile(face, radius, offsetX, offsetY + newSize, newSize, resolution),
                        new GlobeTile(face, radius, offsetX + newSize, offsetY + newSize, newSize, resolution));
            } else {
                level++;
                if (level < MAX_DETAIL_LEVEL) {
                    //recurse
                    updateTiles(matrix, tileTree.getTopLeft(), level);
                    updateTiles(matrix, tileTree.getTopRight(), level);
                    updateTiles(matrix, tileTree.getBottomLeft(), level);
                    updateTiles(matrix, tileTree.getBottomRight(), level);
                }
            }
        }
    }

    public void draw(ShaderProgram shaderProgram) {
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

    private void drawTile(ShadowMapShaderProgram shaderProgram, GlobeTile tile) {
        FloatBuffer tileBuf = (FloatBuffer) tile.vertexBuf;
        setDataOffset(0);
        bindFloatAttribute(shaderProgram.aPositionLocation, POSITION_COMPONENT_COUNT, tileBuf);
        skipAttributes(NORMAL_COMPONENT_COUNT);
        bindFloatAttribute(shaderProgram.aTextureCoordsLocation, TEXTURE_COMPONENT_COUNT, tileBuf);
        drawElements(MODE_TRIANGLES);
    }
}
