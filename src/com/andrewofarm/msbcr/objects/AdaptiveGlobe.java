package com.andrewofarm.msbcr.objects;

import com.andrewofarm.msbcr.programs.GlobeShaderProgram;
import com.andrewofarm.msbcr.programs.ShaderProgram;
import com.andrewofarm.msbcr.programs.ShadowMapShaderProgram;
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

    private static final int MAX_DETAIL_LEVEL = 6;

    private static final boolean WIREFRAME = false;
    private static final boolean DRAW_CORNERS = false;

    private final float radius;

    private static final float BASE_GEOMETRIC_ERROR = 0.25f; //TODO calculate programatically
    private static final float SCREEN_SPACE_ERROR_TOLERANCE = 0.25f;

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

    private static float getDistanceOfClosestCorner(Vector3f viewpointModelSpace, GlobeTile tile) {
        Vector3f[] corners = tile.getCorners();
        Vector3f distVector = new Vector3f();
        float distSq;
        float minDistSq = Float.POSITIVE_INFINITY;
        for (Vector3f corner : corners) {
            distVector.set(corner);
            distVector.sub(viewpointModelSpace);
            distSq = distVector.lengthSquared();
            if (distSq < minDistSq) {
                minDistSq = distSq;
            }
        }
        return (float) Math.sqrt(minDistSq);
    }

    private static float getScreenSpaceError(float geomError, Vector3f viewpointModelSpace, float twoTanHalfFOV, GlobeTile tile) {
        return geomError / (getDistanceOfClosestCorner(viewpointModelSpace, tile) * twoTanHalfFOV);
    }

    public void update(Vector3f viewpointModelSpace, float twoTanHalfFOV) {
        for (QuadTree<GlobeTile> tileTree : tiles) {
            updateTiles(viewpointModelSpace, twoTanHalfFOV, tileTree, 0);
        }
    }

    private void updateTiles(Vector3f viewpointModelSpace, float twoTanHalfFOV, QuadTree<GlobeTile> tileTree, int level) {
        GlobeTile tile = tileTree.getValue();
        float geomError = BASE_GEOMETRIC_ERROR / (level + 1);
        if (getScreenSpaceError(geomError, viewpointModelSpace, twoTanHalfFOV, tile) < SCREEN_SPACE_ERROR_TOLERANCE) {
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
                    updateTiles(viewpointModelSpace, twoTanHalfFOV, tileTree.getTopLeft(), level);
                    updateTiles(viewpointModelSpace, twoTanHalfFOV, tileTree.getTopRight(), level);
                    updateTiles(viewpointModelSpace, twoTanHalfFOV, tileTree.getBottomLeft(), level);
                    updateTiles(viewpointModelSpace, twoTanHalfFOV, tileTree.getBottomRight(), level);
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
