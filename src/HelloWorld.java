import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * Created by Andrew Farm on 6/7/17.
 */
@SuppressWarnings("DefaultFileTemplate")
public class HelloWorld {

    private static final int POSITION_COMPONENT_COUNT = 3;
    private static final int NORMAL_COMPONENT_COUNT = 3;
    private static final int TEXTURE_COMPONENT_COUNT = 2;

    private static final int STRIDE =
            (POSITION_COMPONENT_COUNT +
                    NORMAL_COMPONENT_COUNT +
                    TEXTURE_COMPONENT_COUNT) * 4;

    private long window;

//    private float[] vertices = {
//            0.5f, 0.5f, 1f, 0f, 0f,
//            -0.5f, -0.5f, 0f, 1f, 0f,
//            0.5f, -0.5f, 0f, 0f, 1f,
//    };
    private FloatBuffer vertexBuffer;
    private IntBuffer indexBuffer;

    private float camAzimuth = 0, camElev = 0;
    private float camDist = 2;

    private boolean dragging = false;
    private double prevX, prevY;

    private Matrix4f modelMatrix = new Matrix4f();
    private Matrix4f viewMatrix = new Matrix4f();
    private Matrix4f projectionMatrix = new Matrix4f();
    private Matrix4f mvpMatrix = new Matrix4f();

    private DefaultShaderProgram shaderProgram;

    private void run() {
        System.out.println("Hello LWJGL " + Version.getVersion() + "!");

        init();
        loop();
    }

    private void init() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        boolean success = glfwInit();
        if (!success) {
            throw new IllegalStateException("unable to initialize GLFW");
        }

        // Create the window
        window = glfwCreateWindow(800, 600, "Hello World!", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("failed to create GLFW window");
        }

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
        });

        glfwSetWindowSizeCallback(window, (window1, width, height) -> {
            if (GL.getCapabilities() != null) {
                updateProjectionMatrix(width, height);
                render();
            }
        });

        glfwSetMouseButtonCallback(window, (long window, int button, int action, int mods) -> {
            dragging = (action == GLFW_PRESS);
            double[] cursorX = new double[1];
            double[] cursorY = new double[1];
            glfwGetCursorPos(window, cursorX, cursorY);
            prevX = cursorX[0];
            prevY = cursorY[0];
        });

        glfwSetCursorPosCallback(window, (long window, double xpos, double ypos) -> {
            if (dragging) {
//                System.out.println("(" + prevX + ", " + prevY + ") -> (" + xpos + ", " + ypos + " )");
                camAzimuth += (xpos - prevX) * 0.005f;
                camElev += (ypos - prevY) * 0.005f;
                camElev = Math.min(Math.max(camElev, (float) -Math.PI / 2), (float) Math.PI / 2);
                updateViewMatrix();
                prevX = xpos;
                prevY = ypos;
            }
        });

        int[] pWidth = new int[1];
        int[] pHeight = new int[1];

        // Get the window size passed to glfwCreateWindow
        glfwGetWindowSize(window, pWidth, pHeight);

        updateProjectionMatrix(pWidth[0], pHeight[0]);

        // Get the resolution of the primary monitor
        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

        // Center the window
        glfwSetWindowPos(window,
                (vidmode.width() - pWidth[0]) / 2,
                (vidmode.height() - pHeight[0]) / 2);

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);
    }

    private void loop() {
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        final float radius = 1;
        int meridians = 64;
        int parallels = 31;

        vertexBuffer = ByteBuffer.allocateDirect(ObjectBuilder.getTexturedSphereVertexCount(meridians, parallels) * STRIDE)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        indexBuffer = ByteBuffer.allocateDirect(ObjectBuilder.getTexturedSphereIndexCount(meridians, parallels) * 4)
                .order(ByteOrder.nativeOrder())
                .asIntBuffer();

        ObjectBuilder.buildTexturedSphere(vertexBuffer, indexBuffer, radius, meridians, parallels);

        shaderProgram = new DefaultShaderProgram();

        modelMatrix.identity();
        viewMatrix.setTranslation(0, 0, -camDist).rotate(camAzimuth, 0, 1, 0).rotate(camElev, 1, 0, 0);

        // Set the clear color
        glClearColor(1.0f, 0.0f, 0.0f, 0.0f);

        glEnable(GL_DEPTH_TEST);

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while ( !glfwWindowShouldClose(window) ) {
            render();
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void render() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

        shaderProgram.useProgram();
        updateMvpMatrix();
        shaderProgram.setMvpMatrix(mvpMatrix);
        shaderProgram.setModelMatrix(modelMatrix);

        int dataOffset = 0;

        vertexBuffer.position(dataOffset);
        glVertexAttribPointer(shaderProgram.aPositionLocation, POSITION_COMPONENT_COUNT,
                GL_FLOAT, false, STRIDE, vertexBuffer);
        glEnableVertexAttribArray(shaderProgram.aPositionLocation);
        dataOffset += POSITION_COMPONENT_COUNT;

        vertexBuffer.position(dataOffset);
        glVertexAttribPointer(shaderProgram.aNormalLocation, NORMAL_COMPONENT_COUNT,
                GL_FLOAT, false, STRIDE, vertexBuffer);
        glEnableVertexAttribArray(shaderProgram.aNormalLocation);
        dataOffset += NORMAL_COMPONENT_COUNT;

        indexBuffer.position(0);
        glDrawElements(GL_TRIANGLE_STRIP, indexBuffer);

        glfwSwapBuffers(window); // swap the color buffers

        // Poll for window events. The key callback above will only be
        // invoked during this call.
        glfwPollEvents();
    }

    private void updateProjectionMatrix(int width, int height) {
        projectionMatrix.identity();
        projectionMatrix.perspective(90, (float) width / (float) height, 0.1f, 100f);
    }

    private void updateViewMatrix() {
        viewMatrix.identity();
        viewMatrix.translate(0, 0, -camDist).rotate(camAzimuth, 0, 1, 0).rotate(camElev, 1, 0, 0);
    }

    private void updateMvpMatrix() {
        mvpMatrix.set(projectionMatrix).mul(viewMatrix).mul(modelMatrix);
    }

    public static void main(String[] args) {
        new HelloWorld().run();
    }
}
