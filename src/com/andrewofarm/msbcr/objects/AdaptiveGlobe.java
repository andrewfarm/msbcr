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

    private static final boolean WIREFRAME = false;
    private static final boolean DIAGONALS = true;
    private static final boolean DRAW_CORNERS = false;

    private final float radius;

    private static final float BASE_GEOMETRIC_ERROR = 0.005f; //TODO calculate programatically
    private static final float SCREEN_SPACE_ERROR_TOLERANCE = 0.005f;

    private Set<QuadTree<GlobeTile>> tiles = new HashSet<>();

    public AdaptiveGlobe(float radius, int tileResolution) {
        super(GlobeTile.TOTAL_COMPONENT_COUNT * BYTES_PER_FLOAT);
        this.radius = radius;

        tiles.add(new QuadTree<>(new GlobeTile(CUBE_RIGHT,  radius, 0, 0, 1, tileResolution)));
        tiles.add(new QuadTree<>(new GlobeTile(CUBE_LEFT,   radius, 0, 0, 1, tileResolution)));
        tiles.add(new QuadTree<>(new GlobeTile(CUBE_TOP,    radius, 0, 0, 1, tileResolution)));
        tiles.add(new QuadTree<>(new GlobeTile(CUBE_BOTTOM, radius, 0, 0, 1, tileResolution)));
        tiles.add(new QuadTree<>(new GlobeTile(CUBE_FRONT,  radius, 0, 0, 1, tileResolution)));
        tiles.add(new QuadTree<>(new GlobeTile(CUBE_BACK,   radius, 0, 0, 1, tileResolution)));

        if (WIREFRAME) {
            indexCount = ObjectBuilder.getWireframeTileIndexCount(tileResolution, DIAGONALS);
            indexBuf = newIntBuffer(indexCount);
            ObjectBuilder.buildWireframeTileIndices((IntBuffer) indexBuf, tileResolution, DIAGONALS);
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

    private static float getGeometricError(int level) {
        return BASE_GEOMETRIC_ERROR / (float) Math.pow(2, level);
    }

    public void update(Vector3f viewpointModelSpace, float twoTanHalfFOV) {
        for (QuadTree<GlobeTile> tileTree : tiles) {
            updateTiles(viewpointModelSpace, twoTanHalfFOV, tileTree, 0);
        }
    }

    private void updateTiles(Vector3f viewpointModelSpace, float twoTanHalfFOV, QuadTree<GlobeTile> tileTree, int level) {
        GlobeTile tile = tileTree.getValue();
        if (getScreenSpaceError(getGeometricError(level), viewpointModelSpace, twoTanHalfFOV, tile) < SCREEN_SPACE_ERROR_TOLERANCE) {
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
                //recurse
                updateTiles(viewpointModelSpace, twoTanHalfFOV, tileTree.getTopLeft(), level);
                updateTiles(viewpointModelSpace, twoTanHalfFOV, tileTree.getTopRight(), level);
                updateTiles(viewpointModelSpace, twoTanHalfFOV, tileTree.getBottomLeft(), level);
                updateTiles(viewpointModelSpace, twoTanHalfFOV, tileTree.getBottomRight(), level);
            }
        }
    }

    public void draw(ShaderProgram shaderProgram, Vector3f viewpointModelSpace, float twoTanHalfFOV) {
        for (QuadTree<GlobeTile> tileTree : tiles) {
            drawTiles(shaderProgram, tileTree, viewpointModelSpace, twoTanHalfFOV, 0);
        }
    }

    private void drawTiles(ShaderProgram shaderProgram, QuadTree<GlobeTile> tileTree, Vector3f viewpointModelSpace, float twoTanHalfFOV, int level) {
        if (tileTree.isLeaf()) {
            if (shaderProgram instanceof GlobeShaderProgram) {
                drawTile((GlobeShaderProgram) shaderProgram, tileTree.getValue(), viewpointModelSpace, twoTanHalfFOV, level);
            } else if (!WIREFRAME && (shaderProgram instanceof ShadowMapShaderProgram)) {
                drawTile((ShadowMapShaderProgram) shaderProgram, tileTree.getValue(), viewpointModelSpace, twoTanHalfFOV, level);
            }
        } else {
            level++;
            drawTiles(shaderProgram, tileTree.getTopLeft(),     viewpointModelSpace, twoTanHalfFOV, level);
            drawTiles(shaderProgram, tileTree.getTopRight(),    viewpointModelSpace, twoTanHalfFOV, level);
            drawTiles(shaderProgram, tileTree.getBottomLeft(),  viewpointModelSpace, twoTanHalfFOV, level);
            drawTiles(shaderProgram, tileTree.getBottomRight(), viewpointModelSpace, twoTanHalfFOV, level);
        }
    }

    private void drawTile(GlobeShaderProgram shaderProgram, GlobeTile tile, Vector3f viewpointModelSpace, float twoTanHalfFOV, int level) {
        FloatBuffer tileBuf = (FloatBuffer) tile.vertexBuf;
        setDataOffset(0);
        bindFloatAttribute(shaderProgram.aPositionLocation, GlobeTile.POSITION_COMPONENT_COUNT, tileBuf);
        bindFloatAttribute(shaderProgram.aNormalLocation, GlobeTile.NORMAL_COMPONENT_COUNT, tileBuf);
        bindFloatAttribute(shaderProgram.aTextureCoordsLocation, GlobeTile.TEXTURE_COMPONENT_COUNT, tileBuf);
        bindFloatAttribute(shaderProgram.aTextureCoordsAdj1Location, GlobeTile.TEXTURE_COMPONENT_COUNT, tileBuf);
        bindFloatAttribute(shaderProgram.aTextureCoordsAdj2Location, GlobeTile.TEXTURE_COMPONENT_COUNT, tileBuf);
        float screenSpaceError = getScreenSpaceError(getGeometricError(level), viewpointModelSpace, twoTanHalfFOV, tile);
        float morph = Math.min(Math.max(2 * screenSpaceError / SCREEN_SPACE_ERROR_TOLERANCE - 1, 0), 1);
        shaderProgram.setMorph(morph);
        //noinspection ConstantConditions
        drawElements(WIREFRAME ? MODE_LINES : MODE_TRIANGLE_STRIP);

        if (DRAW_CORNERS) {
            FloatBuffer cornerVertexBuf = tile.getCornerVertexBuffer();
            setDataOffset(0);
            bindFloatAttribute(shaderProgram.aPositionLocation, GlobeTile.POSITION_COMPONENT_COUNT, cornerVertexBuf);
            bindFloatAttribute(shaderProgram.aNormalLocation, GlobeTile.NORMAL_COMPONENT_COUNT, cornerVertexBuf);
            bindFloatAttribute(shaderProgram.aTextureCoordsLocation, GlobeTile.TEXTURE_COMPONENT_COUNT, cornerVertexBuf);
            bindFloatAttribute(shaderProgram.aTextureCoordsAdj1Location, GlobeTile.TEXTURE_COMPONENT_COUNT, null);
            bindFloatAttribute(shaderProgram.aTextureCoordsAdj2Location, GlobeTile.TEXTURE_COMPONENT_COUNT, null);
            drawArrays(MODE_POINTS, 0, 4);
        }
    }

    private void drawTile(ShadowMapShaderProgram shaderProgram, GlobeTile tile, Vector3f viewpointModelSpace, float twoTanHalfFOV, int level) {
        FloatBuffer tileBuf = (FloatBuffer) tile.vertexBuf;
        setDataOffset(0);
        bindFloatAttribute(shaderProgram.aPositionLocation, GlobeTile.POSITION_COMPONENT_COUNT, tileBuf);
        skipAttributes(GlobeTile.NORMAL_COMPONENT_COUNT);
        bindFloatAttribute(shaderProgram.aTextureCoordsLocation, GlobeTile.TEXTURE_COMPONENT_COUNT, tileBuf);
        bindFloatAttribute(shaderProgram.aTextureCoordsAdj1Location, GlobeTile.TEXTURE_COMPONENT_COUNT, tileBuf);
        bindFloatAttribute(shaderProgram.aTextureCoordsAdj2Location, GlobeTile.TEXTURE_COMPONENT_COUNT, tileBuf);
        float screenSpaceError = getScreenSpaceError(getGeometricError(level), viewpointModelSpace, twoTanHalfFOV, tile);
        float morph = Math.min(Math.max(2 * screenSpaceError / SCREEN_SPACE_ERROR_TOLERANCE - 1, 0), 1);
        shaderProgram.setMorph(morph);
        drawElements(MODE_TRIANGLE_STRIP);
    }
}
