package com.andrewofarm.msbcr.objects;

import org.joml.Vector3f;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Created by Andrew on 6/8/17.
 */
public abstract class ObjectBuilder {

    static int getSphereVertexCount(int meridians, int parallels) {
        return (parallels + 1) * (meridians + 1);
    }

    static int getSphereIndexCount(int meridians, int parallels) {
        return ((parallels + 1) * 2 + 2) * (meridians + 1) - 2;
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

    static void buildHierarchialSphere(FloatBuffer vertexBuf, IntBuffer indexBuf, float radius,
        int meridians, int parallels, boolean textured) {

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

            for (int row = 0; row < parallels + 1; row++) {

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


    private static void generateSphereVertices(FloatBuffer vertexBuf, float radius,
        int meridians, int parallels,
        int copies, boolean textured) {

        generateSphereVertices(vertexBuf, radius, meridians, parallels,
                0, 2 * Math.PI, 0, Math.PI,
                copies, textured);
    }

    private static void generateSphereVertices(FloatBuffer vertexBuf, float radius, int meridians, int parallels,
        double startAzimuth, double endAzimuth,
        double startPolarAngle, double endPolarAngle,
        int copies, boolean textured) {

        final double azimuthSpan = endAzimuth - startAzimuth;
        final double polarAngleSpan = startPolarAngle - endPolarAngle;
        final double azimuthInterval = azimuthSpan / meridians;
        final double polarAngleInterval = polarAngleSpan / parallels;
        double azimuthFraction;
        double azimuth;
        double polarAngleFraction;
        double polarAngle;
        float x1, y1, z1;
        float x, y, z;
        Vector3f position = new Vector3f();
        Vector3f normal = new Vector3f();
        for (int col = 0; col <= meridians; col++) {
            azimuth = col * azimuthInterval + startAzimuth;
            azimuthFraction = azimuth / (2 * Math.PI);
            x1 = radius * (float) Math.sin(azimuth);
            y1 = radius;
            z1 = radius * (float) Math.cos(azimuth);

            for (int row = 0; row <= parallels; row++) {
                polarAngle = row * polarAngleInterval + startPolarAngle;
                polarAngleFraction = polarAngle / Math.PI;
                float sin = (float) Math.sin(polarAngle);
                x = x1 * sin;
                y = y1 * (float) Math.cos(polarAngle);
                z = z1 * sin;
                position.set(x, y, z);
                normal.set(x, y, z);
                normal.normalize();
                if (textured) {
                    for (int i = 0; i < copies; i++) {
                        putVertex(vertexBuf, position, normal, (float) azimuthFraction, 1 - (float) polarAngleFraction);
                    }
                } else {
                    for (int i = 0; i < copies; i++) {
                        putVertex(vertexBuf, position, normal);
                    }
                }
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
