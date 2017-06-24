import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL20.glGetAttribLocation;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;

/**
 * Created by Andrew on 6/23/17.
 */
public class AtmosphereRingShaderProgram extends ShaderProgram {

    private static final String U_ATMOSPHERE_RING_MVP_MATRIX = "u_AtmosphereRingMvpMatrix";
    private static final String A_POSITION = "a_Position";
    private static final String A_ALPHA = "a_Alpha";

    public final int uAtmosphereRingMvpMatrixLocation;
    public final int aPositionLocation;
    public final int aAlphaLocation;

    AtmosphereRingShaderProgram() {
        super(TextResourceReader.readFile("src/shaders/atmosphere_ring_vertex_shader.glsl"),
                TextResourceReader.readFile("src/shaders/atmosphere_ring_fragment_shader.glsl"));

        uAtmosphereRingMvpMatrixLocation = glGetUniformLocation(programID, U_ATMOSPHERE_RING_MVP_MATRIX);
        aPositionLocation = glGetAttribLocation(programID, A_POSITION);
        aAlphaLocation = glGetAttribLocation(programID, A_ALPHA);
    }

    void setAtmosphereMvpMatrix(Matrix4f m) {
        glUniformMatrix4fv(uAtmosphereRingMvpMatrixLocation, false, m.get(new float[16]));
    }
}
