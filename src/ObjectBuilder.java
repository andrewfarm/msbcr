import org.joml.Vector3f;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Created by Andrew on 6/8/17.
 */
public abstract class ObjectBuilder {

    static int getSphereVertexCount(int meridians, int parallels) {
        return (parallels + 2) * (meridians + 1);
    }

    static int getSphereIndexCount(int meridians, int parallels) {
        return ((parallels + 2) * 2 + 2) * (meridians + 1) - 2;
    }

    static int getFacetedSphereVertexCount(int meridians, int parallels) {
        return getSphereVertexCount(meridians, parallels) * 4;
    }

    static int getFacetedSphereIndexCount(int meridians, int parallels) {
        return meridians * (parallels + 1) * 6;
    }

    static void buildSphere(FloatBuffer vertexBuf, IntBuffer indexBuf, float radius, int meridians, int parallels, boolean textured) {
        generateSphereVertices(vertexBuf, radius, meridians, parallels, 1, textured);

        int col1, col2;
        int col1StartIndex, col2StartIndex;
        for (col1 = 0; col1 < meridians; col1++) {
            col2 = col1 + 1;
            col1StartIndex = col1 * (parallels + 2);
            col2StartIndex = col2 * (parallels + 2);

            for (int row = 0; row < parallels + 2; row++) {
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
            col1StartIndex = col1 * (parallels + 2);
            col2StartIndex = col2 * (parallels + 2);

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
        final double polarAngleInterval = Math.PI / (parallels + 1);
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

            for (int row = 0; row < parallels + 2; row++) {
                polarAngleFraction = (double) row / (parallels + 1);
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
