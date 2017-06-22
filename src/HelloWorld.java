import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.Version;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Optional;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;
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
                    NORMAL_COMPONENT_COUNT) * 4;

    private static final int STRIDE_TEXTURED =
            (POSITION_COMPONENT_COUNT +
                    NORMAL_COMPONENT_COUNT +
                    TEXTURE_COMPONENT_COUNT) * 4;

    private long window;

    private int windowWidth = 800;
    private int windowHeight = 600;

//    private float[] vertices = {
//            0.5f, 0.5f, 1f, 0f, 0f,
//            -0.5f, -0.5f, 0f, 1f, 0f,
//            0.5f, -0.5f, 0f, 0f, 1f,
//    };
    private FloatBuffer globeVertexBuffer;
    private IntBuffer globeIndexBuffer;

    private FloatBuffer oceanVertexBuffer;
    private IntBuffer oceanIndexBuffer;

    private FloatBuffer skyboxVertexBuffer;
    private ByteBuffer skyboxIndexBuffer;

    private static final float GLOBE_RADIUS = 1;
    private static final float SEA_LEVEL = 0f;
    private static final float TERRAIN_SCALE = 0.075f;

    private float lightX = -1, lightY = 0, lightZ = 0;

    private static final float LOOK_SPEED = 0.02f;
    private boolean up, down, left, right;
    private float camLookAzimuth = 0, camLookElev = 0;
    private float camAzimuth = 0, camElev = 0;
    private float camDist = 4;
    private float globeAzimuth = 0;

    private boolean dragging = false;
    private double prevX, prevY;

    private Matrix4f modelMatrix = new Matrix4f();

    private Matrix4f viewMatrix = new Matrix4f();
    private Matrix4f projectionMatrix = new Matrix4f();
    private Matrix4f mvpMatrix = new Matrix4f();

    private Matrix4f lightViewMatrix = new Matrix4f().lookAt(
            new Vector3f(lightX, lightY, lightZ).normalize().mul(2),
            new Vector3f(0, 0, 0),
            new Vector3f(0, 1, 0));
    private Matrix4f lightProjectionMatrix = new Matrix4f().ortho(-2, 2, -2, 2, 0, 4);
    private Matrix4f lightBiasMatrix = new Matrix4f(
            0.5f, 0.0f, 0.0f, 0.0f,
            0.0f, 0.5f, 0.0f, 0.0f,
            0.0f, 0.0f, 0.5f, 0.0f,
            0.5f, 0.5f, 0.5f, 1.0f);
    private Matrix4f lightMvpMatrix = new Matrix4f();
    private Matrix4f lightBiasMvpMatrix = new Matrix4f();

    private GlobeShaderProgram globeShaderProgram;
    private OceanShaderProgram oceanShaderProgram;
    private StarfieldShaderProgram starfieldShaderProgram;
    private ShadowMapShaderProgram shadowMapShaderProgram;

    private int globeTexture;
    private int displacementMap;
    private int normalMap;
    private int starfieldTexture;

    private int shadowMapWidth = 2048;
    private int shadowMapHeight = 2048;

    private int shadowMapFramebuffer;
    private int shadowMapDepthTexture;

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
        window = glfwCreateWindow(windowWidth, windowHeight, "Hello World!", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("failed to create GLFW window");
        }

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE ) {
                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
            } else if (action == GLFW_PRESS) {
                switch (key) {
                    case GLFW_KEY_UP:
                        up = true;
                        break;
                    case GLFW_KEY_DOWN:
                        down = true;
                        break;
                    case GLFW_KEY_LEFT:
                        left = true;
                        break;
                    case GLFW_KEY_RIGHT:
                        right = true;
                        break;
                }
            } else if (action == GLFW_RELEASE) {
                switch (key) {
                    case GLFW_KEY_UP:
                        up = false;
                        break;
                    case GLFW_KEY_DOWN:
                        down = false;
                        break;
                    case GLFW_KEY_LEFT:
                        left = false;
                        break;
                    case GLFW_KEY_RIGHT:
                        right = false;
                        break;
                }
            }
        });

        glfwSetWindowSizeCallback(window, (window1, width, height) -> {
            windowWidth = width;
            windowHeight = height;
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
                float scale = camDist - GLOBE_RADIUS;
                camAzimuth -= (xpos - prevX) * scale * 0.0015f;
                camElev += (ypos - prevY) * scale * 0.0015f;
                camElev = Math.min(Math.max(camElev, (float) -Math.PI / 2), (float) Math.PI / 2);
                updateViewMatrix();
                prevX = xpos;
                prevY = ypos;
            }
        });

        glfwSetScrollCallback(window, (long window, double xoffset, double yoffset) -> {
            camDist -= yoffset * (camDist - GLOBE_RADIUS) * 0.005;
            camDist = Math.min(Math.max(camDist, 1.01f), 15);
            camLookElev = (float) (1.2 * Math.pow(10, -(camDist - GLOBE_RADIUS)));
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

        int meridians = 2048;
        int parallels = 1024;

        globeVertexBuffer = ByteBuffer.allocateDirect(ObjectBuilder.getSphereVertexCount(meridians, parallels) * STRIDE_TEXTURED)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        globeIndexBuffer = ByteBuffer.allocateDirect(ObjectBuilder.getSphereIndexCount(meridians, parallels) * 4)
                .order(ByteOrder.nativeOrder())
                .asIntBuffer();

        oceanVertexBuffer = ByteBuffer.allocateDirect(ObjectBuilder.getSphereVertexCount(meridians, parallels) * STRIDE)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        oceanIndexBuffer = ByteBuffer.allocateDirect(ObjectBuilder.getSphereIndexCount(meridians, parallels) * 4)
                .order(ByteOrder.nativeOrder())
                .asIntBuffer();

        skyboxVertexBuffer = ByteBuffer.allocateDirect(24 * STRIDE_TEXTURED)
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

        globeVertexBuffer.position(0);
        globeIndexBuffer.position(0);
        ObjectBuilder.buildSphere(globeVertexBuffer, globeIndexBuffer, GLOBE_RADIUS, meridians, parallels, true);

        oceanVertexBuffer.position(0);
        oceanIndexBuffer.position(0);
        ObjectBuilder.buildSphere(oceanVertexBuffer, oceanIndexBuffer, GLOBE_RADIUS, meridians, parallels, false);

        globeShaderProgram = new GlobeShaderProgram();
        oceanShaderProgram = new OceanShaderProgram();
        starfieldShaderProgram = new StarfieldShaderProgram();
        shadowMapShaderProgram = new ShadowMapShaderProgram();

        globeTexture = TextureLoader.loadTexture2D("res/earth-nasa.jpg");
        displacementMap = TextureLoader.loadTexture2D("res/elevation-usgs.png");
        normalMap = TextureLoader.loadTexture2D("res/normalmap.png");
        starfieldTexture = TextureLoader.loadTextureCube(new String[] {
                "res/starmap_8k_4.png",
                "res/starmap_8k_3.png",
                "res/starmap_8k_6.png",
                "res/starmap_8k_5.png",
                "res/starmap_8k_2.png",
                "res/starmap_8k_1.png"
        });

        Optional<TextureLoader.ShadowMap> shadowMap = TextureLoader.createShadowMap(shadowMapWidth, shadowMapHeight);
        shadowMap.ifPresent(shadowMap1 -> {
            shadowMapFramebuffer = shadowMap1.frameBufferID;
            shadowMapDepthTexture = shadowMap1.depthTextureID;
        });

        modelMatrix.identity();
        viewMatrix.setTranslation(0, 0, -camDist).rotate(camAzimuth, 0, 1, 0).rotate(camElev, 1, 0, 0);

        updateLightMatrices();

        // Set the clear color
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
        glEnable(GL_CULL_FACE);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

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

        if (up) {
            camLookElev += LOOK_SPEED;
        }
        if (down) {
            camLookElev -= LOOK_SPEED;
        }
        if (left) {
            camLookAzimuth += LOOK_SPEED;
        }
        if (right) {
            camLookAzimuth -= LOOK_SPEED;
        }

        globeAzimuth += 0.005f;
        updateModelMatrix();
        camAzimuth += 0.005f;
        updateViewMatrix();
        updateMvpMatrix();
        updateLightMatrices();

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

        //render to shadow map

        glBindFramebuffer(GL_FRAMEBUFFER, shadowMapFramebuffer);
        glCullFace(GL_FRONT);

        shadowMapShaderProgram.useProgram();
        shadowMapShaderProgram.setLightMvpMatrix(lightMvpMatrix);
        shadowMapShaderProgram.setDisplacementMap(displacementMap);
        shadowMapShaderProgram.setSeaLevel(SEA_LEVEL);
        shadowMapShaderProgram.setTerrainScale(TERRAIN_SCALE);

        int dataOffset = 0;

        glClear(GL_DEPTH_BUFFER_BIT);
        glViewport(0, 0, shadowMapWidth, shadowMapHeight);

        globeVertexBuffer.position(dataOffset);
        glVertexAttribPointer(shadowMapShaderProgram.aPositionLocation, POSITION_COMPONENT_COUNT,
                GL_FLOAT, false, STRIDE_TEXTURED, globeVertexBuffer);
        glEnableVertexAttribArray(shadowMapShaderProgram.aPositionLocation);
        dataOffset += POSITION_COMPONENT_COUNT;
        dataOffset += NORMAL_COMPONENT_COUNT;

        globeVertexBuffer.position(dataOffset);
        glVertexAttribPointer(shadowMapShaderProgram.aTextureCoordsLocation, TEXTURE_COMPONENT_COUNT,
                GL_FLOAT, false, STRIDE_TEXTURED, globeVertexBuffer);
        glEnableVertexAttribArray(shadowMapShaderProgram.aTextureCoordsLocation);

        globeIndexBuffer.position(0);
//        glDrawElements(GL_TRIANGLE_STRIP, globeIndexBuffer);

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glCullFace(GL_BACK);

        //draw globe

        globeShaderProgram.useProgram();
        globeShaderProgram.setMvpMatrix(mvpMatrix);
        globeShaderProgram.setModelMatrix(modelMatrix);
        globeShaderProgram.setLightBiasMvpMatrix(lightBiasMvpMatrix);
        globeShaderProgram.setLightDirection(lightX, lightY, lightZ);
        globeShaderProgram.setDisplacementMap(displacementMap);
        globeShaderProgram.setTexture(globeTexture);
        globeShaderProgram.setNormalMap(normalMap);
        globeShaderProgram.setShadowMap(shadowMapDepthTexture);
        globeShaderProgram.setSeaLevel(SEA_LEVEL);
        globeShaderProgram.setTerrainScale(TERRAIN_SCALE);

        glViewport(0, 0, windowWidth * 2, windowHeight * 2); //TODO check for retina display

        dataOffset = 0;

        globeVertexBuffer.position(dataOffset);
        glVertexAttribPointer(globeShaderProgram.aPositionLocation, POSITION_COMPONENT_COUNT,
                GL_FLOAT, false, STRIDE_TEXTURED, globeVertexBuffer);
        glEnableVertexAttribArray(globeShaderProgram.aPositionLocation);
        dataOffset += POSITION_COMPONENT_COUNT;

        globeVertexBuffer.position(dataOffset);
        glVertexAttribPointer(globeShaderProgram.aNormalLocation, NORMAL_COMPONENT_COUNT,
                GL_FLOAT, false, STRIDE_TEXTURED, globeVertexBuffer);
        glEnableVertexAttribArray(globeShaderProgram.aNormalLocation);
        dataOffset += NORMAL_COMPONENT_COUNT;

        globeVertexBuffer.position(dataOffset);
        glVertexAttribPointer(globeShaderProgram.aTextureCoordsLocation, TEXTURE_COMPONENT_COUNT,
                GL_FLOAT, false, STRIDE_TEXTURED, globeVertexBuffer);
        glEnableVertexAttribArray(globeShaderProgram.aTextureCoordsLocation);

        globeIndexBuffer.position(0);
        glDrawElements(GL_TRIANGLE_STRIP, globeIndexBuffer);

        //draw ocean

        oceanShaderProgram.useProgram();
        oceanShaderProgram.setMvpMatrix(mvpMatrix);
        oceanShaderProgram.setModelMatrix(modelMatrix);
        oceanShaderProgram.setLightDirection(lightX, lightY, lightZ);
        oceanShaderProgram.setCamPos(
                (float) (camDist * Math.sin(camAzimuth) * Math.cos(camElev)),
                (float) (camDist * Math.sin(camElev)),
                (float) (camDist * Math.cos(camAzimuth) * Math.cos(camElev)));

        dataOffset = 0;

        oceanVertexBuffer.position(dataOffset);
        glVertexAttribPointer(oceanShaderProgram.aPositionLocation, POSITION_COMPONENT_COUNT,
                GL_FLOAT, false, STRIDE, oceanVertexBuffer);
        glEnableVertexAttribArray(oceanShaderProgram.aPositionLocation);
        dataOffset += POSITION_COMPONENT_COUNT;

        oceanVertexBuffer.position(dataOffset);
        glVertexAttribPointer(oceanShaderProgram.aNormalLocation, NORMAL_COMPONENT_COUNT,
                GL_FLOAT, false, STRIDE, oceanVertexBuffer);
        glEnableVertexAttribArray(oceanShaderProgram.aNormalLocation);

        oceanIndexBuffer.position(0);
        glDrawElements(GL_TRIANGLE_STRIP, oceanIndexBuffer);

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
        viewMatrix
                .rotate(-camLookElev, 1, 0, 0)
                .rotate(-camLookAzimuth, 0, 1, 0)
                .translate(0, 0, -camDist)
                .rotate(camElev, 1, 0, 0)
                .rotate(-camAzimuth, 0, 1, 0);
    }

    private void updateModelMatrix() {
        modelMatrix.identity();
        modelMatrix.rotate(globeAzimuth, 0, 1, 0);
    }

    private void updateLightMatrices() {
        lightMvpMatrix.set(lightProjectionMatrix).mul(lightViewMatrix).mul(modelMatrix);
        lightBiasMvpMatrix.set(lightBiasMatrix).mul(lightMvpMatrix);
    }

    private void updateMvpMatrix() {
        mvpMatrix.set(projectionMatrix).mul(viewMatrix).mul(modelMatrix);
    }

    public static void main(String[] args) {
        new HelloWorld().run();
    }
}
