/**
 * Created by Andrew on 12/30/16.
 */

public class Vector3f {

    public float x, y, z;

    public Vector3f() {
        set(0f, 0f, 0f);
    }

    public Vector3f(float x, float y, float z) {
        set(x, y, z);
    }

    public Vector3f(Vector3f v) {
        set(v);
    }

    public Vector3f(float[] coords) {
        set(coords);
    }

    public void set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void set(Vector3f v) {
        set(v.x, v.y, v.z);
    }

    public void set(float[] coords) {
        set(coords[0], coords[1], coords[2]);
    }

    public void setZero() {
        set(0, 0, 0);
    }

    public void add(float x, float y, float z) {
        this.x += x;
        this.y += y;
        this.z += z;
    }

    public void add(Vector3f v) {
        add(v.x, v.y, v.z);
    }

    public void add(float[] coords) {
        add(coords[0], coords[1], coords[2]);
    }

    public void sub(float x, float y, float z) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
    }

    public void sub(Vector3f v) {
        sub(v.x, v.y, v.z);
    }

    public void sub(float[] coords) {
        sub(coords[0], coords[1], coords[2]);
    }

    public void scale(float f) {
        x *= f;
        y *= f;
        z *= f;
    }

    public void setLength(float f) {
        scale(f / length());
    }

    public void negate() {
        x = -x;
        y = -y;
        z = -z;
    }

    public void normalize() {
        scale(1f / length());
    }

    public float lengthSquared() {
        return  x * x +
                y * y +
                z * z;
    }

    public float length() {
        return (float) Math.sqrt(lengthSquared());
    }

    public float dot(Vector3f v) {
        return  x * v.x +
                y * v.y +
                z * v.z;
    }

    public void cross(Vector3f v) {
        float newX = (y * v.z) - (z * v.y);
        float newY = (z * v.x) - (x * v.z);
        float newZ = (x * v.y) - (y * v.x);

        x = newX;
        y = newY;
        z = newZ;
    }

    public float[] toArray() {
        return new float[] {x, y, z};
    }

    public float[] toArray4() {
        return new float[] {x, y, z, 1f};
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ", " + z + ")";
    }
}
