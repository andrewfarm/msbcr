import org.joml.Matrix4f;
import org.lwjgl.Version;
import org.lwjgl.glfw.*;
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

    private FloatBuffer skyboxVertexBuffer;
    private ByteBuffer skyboxIndexBuffer;

    private float globeRadius = 1;

    private float camAzimuth = 0, camElev = 0;
    private float camDist = 4;

    private boolean dragging = false;
    private double prevX, prevY;

    private Matrix4f modelMatrix = new Matrix4f();
    private Matrix4f viewMatrix = new Matrix4f();
    private Matrix4f projectionMatrix = new Matrix4f();
    private Matrix4f mvpMatrix = new Matrix4f();

    private DefaultShaderProgram shaderProgram;
    private StarfieldShaderProgram starfieldShaderProgram;

    private int globeTexture;
    private int starfieldTexture;

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
                glViewport(0, 0, width, height);
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
                float scale = camDist - globeRadius;
                camAzimuth += (xpos - prevX) * scale * 0.002f;
                camElev += (ypos - prevY) * scale * 0.002f;
                camElev = Math.min(Math.max(camElev, (float) -Math.PI / 2), (float) Math.PI / 2);
                updateViewMatrix();
                prevX = xpos;
                prevY = ypos;
            }
        });

        glfwSetScrollCallback(window, (long window, double xoffset, double yoffset) -> {
            camDist -= yoffset * (camDist - globeRadius) * 0.005;
            camDist = Math.min(Math.max(camDist, 1.01f), 15);
            updateViewMatrix();
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

        int meridians = 64;
        int parallels = 31;

        vertexBuffer = ByteBuffer.allocateDirect(ObjectBuilder.getTexturedFacetedSphereVertexCount(meridians, parallels) * STRIDE)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        indexBuffer = ByteBuffer.allocateDirect(ObjectBuilder.getTexturedFacetedSphereIndexCount(meridians, parallels) * 4)
                .order(ByteOrder.nativeOrder())
                .asIntBuffer();

        skyboxVertexBuffer = ByteBuffer.allocateDirect(24 * STRIDE)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(new float[] {
                       -1,  1,  1,
                        1,  1,  1,
                       -1, -1,  1,
                        1, -1,  1,
                       -1,  1, -1,
                        1,  1, -1,
                       -1, -1, -1,
                        1, -1, -1,});

        skyboxIndexBuffer = ByteBuffer.allocateDirect(36 * 4).put(new byte[] {
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

        vertexBuffer.position(0);
        indexBuffer.position(0);
        ObjectBuilder.buildTexturedFacetedSphere(vertexBuffer, indexBuffer, globeRadius, meridians, parallels);

        vertexBuffer.position(0);
        while (vertexBuffer.hasRemaining()) {
            System.out.println("Vertex: position(" + vertexBuffer.get() + ", " + vertexBuffer.get() + ", " + vertexBuffer.get() +
                    ") normal=(" + vertexBuffer.get() + ", " + vertexBuffer.get() + ", " + vertexBuffer.get() +
                    ") texture=(" + vertexBuffer.get() + ", " + vertexBuffer.get() + ")");
        }

        System.out.println("vertexBuffer.limit()=" + vertexBuffer.limit());
        indexBuffer.position(0);
        int maxIndex = indexBuffer.get();
        while (indexBuffer.hasRemaining()) {
            int index = indexBuffer.get();
            if (index > maxIndex) {
                maxIndex = index;
            }
        }
        System.out.println("max index: " + maxIndex);
        System.out.println("indexBuffer.limit()=" + indexBuffer.limit());


        shaderProgram = new DefaultShaderProgram();
        starfieldShaderProgram = new StarfieldShaderProgram();

        globeTexture = TextureLoader.loadTexture2D("res/earth-max2.jpeg");
        starfieldTexture = TextureLoader.loadTextureCube(new String[] {
                "res/dallasw_left.jpg",
                "res/dallasw_right.jpg",
                "res/dallasw_bottom.jpg",
                "res/dallasw_top.jpg",
                "res/dallasw_front.jpg",
                "res/dallasw_back.jpg"
        });

        modelMatrix.identity();
        viewMatrix.setTranslation(0, 0, -camDist).rotate(camAzimuth, 0, 1, 0).rotate(camElev, 1, 0, 0);

        // Set the clear color
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
        glEnable(GL_CULL_FACE);

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

        updateMvpMatrix();

        //draw starfield

        starfieldShaderProgram.useProgram();
        Matrix4f vpRotationMatrix = new Matrix4f(viewMatrix);
        vpRotationMatrix.m30(0);
        vpRotationMatrix.m31(0);
        vpRotationMatrix.m32(0);
        starfieldShaderProgram.setVpMatrix(projectionMatrix.mul(vpRotationMatrix, vpRotationMatrix));
        starfieldShaderProgram.setTexture(starfieldTexture);

        skyboxVertexBuffer.position(0);
        glVertexAttribPointer(starfieldShaderProgram.aPositionLocation, 3,
                GL_FLOAT, false, 12, skyboxVertexBuffer);
        glEnableVertexAttribArray(starfieldShaderProgram.aPositionLocation);

        skyboxIndexBuffer.position(0);
        glDrawElements(GL_TRIANGLES, skyboxIndexBuffer);

        //draw globe

        shaderProgram.useProgram();
        shaderProgram.setMvpMatrix(mvpMatrix);
        shaderProgram.setModelMatrix(modelMatrix);
        shaderProgram.setTexture(globeTexture);

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

        vertexBuffer.position(dataOffset);
        glVertexAttribPointer(shaderProgram.aTextureCoordsLocation, TEXTURE_COMPONENT_COUNT,
                GL_FLOAT, false, STRIDE, vertexBuffer);
        glEnableVertexAttribArray(shaderProgram.aTextureCoordsLocation);
        dataOffset += TEXTURE_COMPONENT_COUNT;

        indexBuffer.position(0);
        glDrawElements(GL_TRIANGLES, indexBuffer);

        glfwSwapBuffers(window); // swap the color buffers

        // Poll for window events. The key callback above will only be
        // invoked during this call.
        glfwPollEvents();
    }

    private void updateProjectionMatrix(int width, int height) {
        projectionMatrix.identity();
        projectionMatrix.perspective((float) Math.PI / 4, (float) width / (float) height, 0.01f, 20f);
    }

    private void updateViewMatrix() {
        viewMatrix.identity();
        viewMatrix.translate(0, 0, -camDist).rotate(camElev, 1, 0, 0).rotate(camAzimuth, 0, 1, 0);
    }

    private void updateMvpMatrix() {
        mvpMatrix.set(projectionMatrix).mul(viewMatrix).mul(modelMatrix);
    }

    public static void main(String[] args) {
        new HelloWorld().run();
    }
}
