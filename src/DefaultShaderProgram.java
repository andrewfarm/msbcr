import static org.lwjgl.opengl.GL20.glGetAttribLocation;

/**
 * Created by Andrew on 6/8/17.
 */
public class DefaultShaderProgram extends ShaderProgram {

    private static final String A_POSITION = "a_Position";
    private static final String A_COLOR = "a_Color";

    public final int aPositionLocation;
    public final int aColorLocation;

    DefaultShaderProgram() {
        super(TextResourceReader.readFile("src/shaders/vertex_shader.glsl"),
                TextResourceReader.readFile("src/shaders/fragment_shader.glsl"));

        aPositionLocation = glGetAttribLocation(programID, A_POSITION);
        aColorLocation = glGetAttribLocation(programID, A_COLOR);
    }
}
