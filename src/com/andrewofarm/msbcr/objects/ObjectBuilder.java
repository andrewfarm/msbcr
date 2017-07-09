package com.andrewofarm.msbcr.objects;

import org.joml.Vector3f;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Created by Andrew on 6/8/17.
 */
public abstract class ObjectBuilder {

    static final int CUBE_RIGHT = 0;
    static final int CUBE_LEFT = 1;
    static final int CUBE_TOP = 2;
    static final int CUBE_BOTTOM = 3;
    static final int CUBE_FRONT = 4;
    static final int CUBE_BACK = 5;

    static int getSphereVertexCount(int meridians, int parallels) {
        return (parallels + 1) * (meridians + 1);
    }

    static int getSphereIndexCount(int meridians, int parallels) {
        return ((parallels + 1) * 2 + 2) * (meridians + 1) - 2;
    }

    static int getTileVertexCount(int resolution) {
        return (resolution + 1) * (resolution + 1);
    }

    static int getTileIndexCount(int resolution) {
        return ((resolution + 1) * 2 + 2) * resolution - 2;
    }

    static int getWireframeTileIndexCount(int resolution) {
        return resolution * resolution * 8;
    }

    static int getFacetedSphereVertexCount(int meridians, int parallels) {
        return getSphereVertexCount(meridians, parallels) * 4;
    }

    static int getFacetedSphereIndexCount(int meridians, int parallels) {
        return meridians * (parallels + 1) * 6;
    }

    static int getRingsVertexCount(int segments) {
        return (segments + 1) * 2;
    }

    static int getSkyboxVertexCount() {
        return 24;
    }

    static int getSkyboxIndexCount() {
        return 36;
    }


    static void buildSphere(FloatBuffer vertexBuf, IntBuffer indexBuf, float radius, int meridians, int parallels, boolean textured) {
        generateSphereVertices(vertexBuf, radius, meridians, parallels, 1, textured);

        int col1, col2;
        int col1StartIndex, col2StartIndex;
        for (col1 = 0; col1 < meridians; col1++) {
            col2 = col1 + 1;
            col1StartIndex = col1 * (parallels + 1);
            col2StartIndex = col2 * (parallels + 1);

            for (int row = 0; row < parallels + 1; row++) {
                indexBuf.put(col2StartIndex + row);
                indexBuf.put(col1StartIndex + row);
            }

            //degenerate vertices
            if (col2 < meridians) {
                indexBuf.put(col1StartIndex + parallels + 1);
                indexBuf.put(col2StartIndex + parallels + 2);
            }
        }
    }

    static void buildFacetedSphere(FloatBuffer vertexBuf, IntBuffer indexBuf, float radius, int meridians, int parallels, boolean textured) {
        generateSphereVertices(vertexBuf, radius, meridians, parallels, 4, textured);

        final int[] faceVertexIndices = new int[4];
        int col1, col2;
        int col1StartIndex, col2StartIndex;
        for (col1 = 0; col1 < meridians; col1++) {
            col2 = col1 + 1;
            col1StartIndex = col1 * (parallels + 1);
            col2StartIndex = col2 * (parallels + 1);

            for (int row = 0; row < parallels + 1; row ++) {

                faceVertexIndices[0] = (col2StartIndex + row) * 4 + 3;
                faceVertexIndices[1] = (col1StartIndex + row) * 4 + 2;
                faceVertexIndices[2] = (col2StartIndex + row + 1) * 4 + 1;
                faceVertexIndices[3] = (col1StartIndex + row + 1) * 4;

                Vector3f faceNormal = new Vector3f();
                for (int faceVertexIndex : faceVertexIndices) {
                    faceNormal.add(new Vector3f(
                            vertexBuf.get(faceVertexIndex * 8 + 3),
                            vertexBuf.get(faceVertexIndex * 8 + 4),
                            vertexBuf.get(faceVertexIndex * 8 + 5))
                            .normalize());
                }
                faceNormal.normalize();
                for (int faceVertexIndex : faceVertexIndices) {
                    vertexBuf.put(faceVertexIndex * 8 + 3, faceNormal.x);
                    vertexBuf.put(faceVertexIndex * 8 + 4, faceNormal.y);
                    vertexBuf.put(faceVertexIndex * 8 + 5, faceNormal.z);
                }

//                float texU = 0, texV = 0;
//                for (int faceVertexIndex : faceVertexIndices) {
//                    texU += vertexBuf.get(faceVertexIndex * 8 + 6);
//                    texV += vertexBuf.get(faceVertexIndex * 8 + 7);
//                }
//                texU /= faceVertexIndices.length;
//                texV /= faceVertexIndices.length;
//                for (int faceVertexIndex : faceVertexIndices) {
//                    vertexBuf.put(faceVertexIndex * 8 + 6, texU);
//                    vertexBuf.put(faceVertexIndex * 8 + 7, texV);
//                }

                indexBuf.put(faceVertexIndices[0]);
                indexBuf.put(faceVertexIndices[1]);
                indexBuf.put(faceVertexIndices[2]);
                indexBuf.put(faceVertexIndices[3]);
                indexBuf.put(faceVertexIndices[2]);
                indexBuf.put(faceVertexIndices[1]);
            }
        }
    }

    private static void generateSphereVertices(FloatBuffer vertexBuf, float radius, int meridians, int parallels, int copies, boolean textured) {
        final double azimuthInterval = 2 * Math.PI / meridians;
        final double polarAngleInterval = Math.PI / parallels;
        double azimuthFraction;
        double azimuth;
        double polarAngleFraction;
        double polarAngle;
        float x1, y1, z1;
        float x, y, z;
        Vector3f position = new Vector3f();
        Vector3f normal = new Vector3f();
        for (int col = 0; col <= meridians; col++) {
            azimuthFraction = (double) col / meridians;
            azimuth = col * azimuthInterval;
            x1 = radius * (float) Math.sin(azimuth);
            y1 = radius;
            z1 = radius * (float) Math.cos(azimuth);

            for (int row = 0; row <= parallels; row++) {
                polarAngleFraction = (double) row / parallels;
                polarAngle = row * polarAngleInterval;
                float sin = (float) Math.sin(polarAngle);
                x = x1 * sin;
                y = y1 * (float) Math.cos(polarAngle);
                z = z1 * sin;
                position.set(x, y, z);
                normal.set(x, y, z);
                normal.normalize();
                if (textured) {
                    for (int i = 0; i < copies; i++) {
                        putVertex(vertexBuf, position, normal, (float) azimuthFraction, (float) polarAngleFraction);
                    }
                } else {
                    for (int i = 0; i < copies; i++) {
                        putVertex(vertexBuf, position, normal);
                    }
                }
            }
        }
    }

    static void buildTileVertices(FloatBuffer vertexBuf, int face, float radius,
                                  float offsetX, float offsetY, float size, int resolution) {

        final float interval = size / resolution;
        float tileX, tileY;
        Vector3f position = new Vector3f();
        for (int i = 0; i <= resolution; i++) {
            tileX = (i * interval + offsetX) * 2.0f - 1.0f;
            for (int j = 0; j <= resolution; j++) {
                tileY = (j * interval + offsetY) * 2.0f - 1.0f;

                switch (face) {
                    case CUBE_RIGHT:
                        position.set(1, -tileY, -tileX);
                        break;
                    case CUBE_LEFT:
                        position.set(-1, -tileY, tileX);
                        break;
                    case CUBE_TOP:
                        position.set(tileX, 1, tileY);
                        break;
                    case CUBE_BOTTOM:
                        position.set(tileX, -1, -tileY);
                        break;
                    case CUBE_FRONT:
                        position.set(tileX, -tileY, 1);
                        break;
                    case CUBE_BACK:
                        position.set(-tileX, -tileY, -1);
                        break;
                }
                position.normalize().mul(radius);
//                putVertex(vertexBuf, position, position, tileX * 0.5f + 0.5f, tileY * 0.5f + 0.5f);
                putVertex(vertexBuf, position, position,
                        (float) (Math.atan2(position.get(0) / radius, position.get(2) / radius) / (2 * Math.PI)),
                        (float) (Math.acos(position.get(1) / radius) / Math.PI));

                vertexBuf.position(vertexBuf.position() + 4);
            }
        }

        /*
        if (adjTexCoords) {
            int floatIndex;
            final int TOTAL_COMPONENT_COUNT = 12;

            //calculate adjacent texture coords for old vertices (from coarser chunk)
            for (int col = 0; col <= resolution; col += 2) {
                for (int row = 0; row <= resolution; row += 2) {
                    floatIndex = (col * (resolution + 1) + row) * TOTAL_COMPONENT_COUNT;
                    vertexBuf.put(floatIndex + 8, vertexBuf.get(floatIndex + 6));
                    vertexBuf.put(floatIndex + 9, vertexBuf.get(floatIndex + 7));
                    vertexBuf.put(floatIndex + 10, vertexBuf.get(floatIndex + 6));
                    vertexBuf.put(floatIndex + 11, vertexBuf.get(floatIndex + 7));
                }
            }

            //calculate adjacent texture coords for new horizontal edge vertices
            final int COL_FLOAT_STRIDE = (resolution + 1) * TOTAL_COMPONENT_COUNT;
            for (int col = 1; col <= resolution; col += 2) {
                for (int row = 0; row <= resolution; row += 2) {
                    floatIndex = (col * (resolution + 1) + row) * TOTAL_COMPONENT_COUNT;
                    vertexBuf.put(floatIndex + 8, vertexBuf.get(floatIndex - COL_FLOAT_STRIDE + 6));
                    vertexBuf.put(floatIndex + 9, vertexBuf.get(floatIndex - COL_FLOAT_STRIDE + 7));
                    vertexBuf.put(floatIndex + 9, vertexBuf.get(floatIndex + COL_FLOAT_STRIDE + 6));
                    vertexBuf.put(floatIndex + 9, vertexBuf.get(floatIndex + COL_FLOAT_STRIDE + 7));
                }
            }

            //calculate adjacent texture coords for new vertical edge vertices
            for (int col = 0; col <= resolution; col += 2) {
                for (int row = 1; row <= resolution; row += 2) {
                    floatIndex = (col * (resolution + 1) + row) * TOTAL_COMPONENT_COUNT;
                    vertexBuf.put(floatIndex + 8, vertexBuf.get(floatIndex - TOTAL_COMPONENT_COUNT + 6));
                    vertexBuf.put(floatIndex + 9, vertexBuf.get(floatIndex - TOTAL_COMPONENT_COUNT + 7));
                    vertexBuf.put(floatIndex + 9, vertexBuf.get(floatIndex + TOTAL_COMPONENT_COUNT + 6));
                    vertexBuf.put(floatIndex + 9, vertexBuf.get(floatIndex + TOTAL_COMPONENT_COUNT + 7));
                }
            }

            //calculate adjacent texture coords for new center vertices
            for (int col = 1; col <= resolution; col += 2) {
                for (int row = 1; row <= resolution; row += 2) {
                    floatIndex = (col * (resolution + 1) + row) * TOTAL_COMPONENT_COUNT;
                    vertexBuf.put(floatIndex + 8, vertexBuf.get(floatIndex - COL_FLOAT_STRIDE - TOTAL_COMPONENT_COUNT + 6));
                    vertexBuf.put(floatIndex + 9, vertexBuf.get(floatIndex - COL_FLOAT_STRIDE - TOTAL_COMPONENT_COUNT + 7));
                    vertexBuf.put(floatIndex + 9, vertexBuf.get(floatIndex + COL_FLOAT_STRIDE + TOTAL_COMPONENT_COUNT + 6));
                    vertexBuf.put(floatIndex + 9, vertexBuf.get(floatIndex + COL_FLOAT_STRIDE + TOTAL_COMPONENT_COUNT + 7));
                }
            }
        }*/
    }

    static void buildTileIndices(IntBuffer indexBuf, int resolution) {
        int col1, col2;
        int col1StartIndex, col2StartIndex;
        for (col1 = 0; col1 < resolution; col1++) {
            col2 = col1 + 1;
            col1StartIndex = col1 * (resolution + 1);
            col2StartIndex = col2 * (resolution + 1);

            for (int row = 0; row <= resolution; row++) {
                indexBuf.put(col2StartIndex + row);
                indexBuf.put(col1StartIndex + row);
            }

            //degenerate vertices
            if (col2 < resolution) {
                indexBuf.put(col1StartIndex + resolution);
                indexBuf.put(col2StartIndex + resolution + 1);
            }
        }
    }

    static void buildWireframeTileIndices(IntBuffer indexBuf, int resolution) {
        int col1, col2;
        int col1StartIndex, col2StartIndex;
        int row;
        int i1, i2, i3, i4;
        for (col1 = 0; col1 < resolution; col1++) {
            col2 = col1 + 1;
            col1StartIndex = col1 * (resolution + 1);
            col2StartIndex = col2 * (resolution + 1);

            for (row = 0; row < resolution; row++) {
                i1 = col1StartIndex + row;
                i2 = col2StartIndex + row;
                i3 = i2 + 1;
                i4 = i1 + 1;

                indexBuf.put(i1);
                indexBuf.put(i2);

                indexBuf.put(i2);
                indexBuf.put(i3);

                indexBuf.put(i3);
                indexBuf.put(i4);

                indexBuf.put(i4);
                indexBuf.put(i1);
            }
        }
    }

    static void buildTexturedRings(FloatBuffer vertexBuf, float innerRadius, float outerRadius, int segments) {
        final double angleInterval = 2 * Math.PI / segments;
        double angle;
        float sin, cos;
        for (int i = 0; i <= segments; i++) {
            angle = i * angleInterval;
            sin = (float) Math.sin(angle);
            cos = (float) Math.cos(angle);

            vertexBuf.put(cos * innerRadius);
            vertexBuf.put(0.0f);
            vertexBuf.put(sin * innerRadius);
            vertexBuf.put(0.0f);

            vertexBuf.put(cos * outerRadius);
            vertexBuf.put(0.0f);
            vertexBuf.put(sin * outerRadius);
            vertexBuf.put(1.0f);
        }
    }

    static void buildSkybox(FloatBuffer vertexBuf, ByteBuffer indexBuf) {
        vertexBuf.put(new float[] {
                -1,  1,  1,
                1,  1,  1,
                -1, -1,  1,
                1, -1,  1,
                -1,  1, -1,
                1,  1, -1,
                -1, -1, -1,
                1, -1, -1,
        });

        indexBuf.put(new byte[] {
                //Front
                1, 3, 0,
                0, 3, 2,

                //Back
                4, 6, 5,
                5, 6, 7,

                //Left
                0, 2, 4,
                4, 2, 6,

                //Right
                5, 7, 1,
                1, 7, 3,

                //Top
                5, 1, 4,
                4, 1, 0,

                //Bottom
                6, 2, 7,
                7, 2, 3,
        });
    }

    private static void putVertex(FloatBuffer vertexBuf, Vector3f position, Vector3f normal) {
        vertexBuf.put(position.x);
        vertexBuf.put(position.y);
        vertexBuf.put(position.z);
        vertexBuf.put(normal.x);
        vertexBuf.put(normal.y);
        vertexBuf.put(normal.z);
    }

    private static void putVertex(FloatBuffer vertexBuf, Vector3f position, Vector3f normal, float tu, float tv) {
        putVertex(vertexBuf, position, normal);
        vertexBuf.put(tu);
        vertexBuf.put(tv);
    }
}
