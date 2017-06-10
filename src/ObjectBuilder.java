import org.joml.Vector3f;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Created by Andrew on 6/8/17.
 */
public abstract class ObjectBuilder {

    static int getTexturedSphereVertexCount(int meridians, int parallels) {
        return (parallels + 2) * meridians;
    }

    static int getTexturedSphereIndexCount(int meridians, int parallels) {
        return ((parallels + 2) * 2 + 2) * meridians;
    }

    static int getTexturedFacetedSphereVertexCount(int meridians, int parallels) {
        return meridians * (parallels + 2) * 4;
    }

    static int getTexturedFacetedSphereIndexCount(int meridians, int parallels) {
        return meridians * (parallels * 4 + 2);
    }

    static void buildTexturedSphere(FloatBuffer vertexBuf, IntBuffer indexBuf, float radius, int meridians, int parallels) {
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
        for (int col = 0; col < meridians; col++) {
            azimuthFraction = (double) col / meridians;
            azimuth = col * azimuthInterval;
            x1 = radius * (float) Math.cos(azimuth);
            y1 = radius;
            z1 = radius * (float) Math.sin(azimuth);

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
                putVertex(vertexBuf, position, normal, (float) azimuthFraction, 1 - (float) polarAngleFraction);
            }
        }

        int col1, col2;
        int col1StartIndex, col2StartIndex;
        for (col1 = 0; col1 < meridians; col1++) {
            col2 = (col1 + 1) % meridians;
            col1StartIndex = col1 * (parallels + 2);
            col2StartIndex = col2 * (parallels + 2);

            for (int row = 0; row < parallels + 2; row++) {
                indexBuf.put(col2StartIndex + row);
                indexBuf.put(col1StartIndex + row);
            }

            //degenerate vertices
            indexBuf.put((col2StartIndex == 0) ? (col2StartIndex + (parallels + 2) * meridians - 1) : (col2StartIndex - 1));
            indexBuf.put(col2StartIndex);
        }
    }

    static void builtTexturedFacetedSphere(FloatBuffer vertexBuf, IntBuffer indexBuf, float radius, int meridians, int parallels) {

    }

    private static void putVertex(FloatBuffer vertexBuf, Vector3f position, Vector3f normal, float tu, float tv) {
        vertexBuf.put(position.x);
        vertexBuf.put(position.y);
        vertexBuf.put(position.z);
        vertexBuf.put(normal.x);
        vertexBuf.put(normal.y);
        vertexBuf.put(normal.z);
        vertexBuf.put(tu);
        vertexBuf.put(tv);
    }
}
