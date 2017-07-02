package com.andrewofarm.msbcr;

import com.andrewofarm.msbcr.objects.Globe;
import com.andrewofarm.msbcr.objects.Ocean;
import com.andrewofarm.msbcr.objects.Rings;
import com.andrewofarm.msbcr.objects.Skybox;
import com.andrewofarm.msbcr.programs.*;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.Version;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Optional;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
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

    private static final float GLOBE_RADIUS = 1;
    private static final float SEA_LEVEL = 0.5f;
    private static final float TERRAIN_SCALE = 0.75f;

    private float lightX = -1, lightY = 0, lightZ = 0;

    private static final float LOOK_SPEED = 0.02f;
    private boolean up, down, left, right;
    private float camLookAzimuth = 0, camLookElev = 0;
    private float camAzimuth = 0, camElev = 0;
    private float camDist = 4;
    private float globeAzimuth = 0;

    private boolean dragging = false;
    private double prevX, prevY;

    private static float timePassage = 0.005f;
    private static final float TIME_MOD = 1.1f;
    private boolean speedUp, slowDown;

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

    private static final int MERIDIANS = 1024;
    private static final int PARALLELS = 512;

    private Skybox skybox = new Skybox();
    private Globe globe = new Globe(1.0f, MERIDIANS, PARALLELS);
    private Rings rings = new Rings(128, 1.5f, 3.0f);
    private Ocean ocean = new Ocean(1.0f, MERIDIANS, PARALLELS);

    private ShadowMapShaderProgram shadowMapShaderProgram;
    private SkyboxShaderProgram skyboxShaderProgram;
    private GlobeShaderProgram globeShaderProgram;
    private RingsShaderProgram ringsShaderProgram;
    private OceanShaderProgram oceanShaderProgram;

    private int starfieldTexture;
    private int globeTexture;
    private int displacementMap;
    private int normalMap;
    private int ringsTexture;

    private int shadowMapWidth = 4096;
    private int shadowMapHeight = 4096;

    private int shadowMapFramebuffer;
    private int shadowMapDepthTexture;

    private void run() {
        System.out.println("Hello LWJGL " + Version.getVersion() + "!");

        initWindow();
        initScene();
        loop();
    }

    private void initWindow() {
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
                    case GLFW_KEY_EQUAL:
                        speedUp = true;
                        break;
                    case GLFW_KEY_MINUS:
                        slowDown = true;
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
                    case GLFW_KEY_EQUAL:
                        speedUp = false;
                        break;
                    case GLFW_KEY_MINUS:
                        slowDown = false;
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

    private void initScene() {
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        shadowMapShaderProgram = new ShadowMapShaderProgram();
        skyboxShaderProgram = new SkyboxShaderProgram();
        globeShaderProgram = new GlobeShaderProgram();
        ringsShaderProgram = new RingsShaderProgram();
        oceanShaderProgram = new OceanShaderProgram();

        globeTexture = TextureLoader.loadTexture2D("res/earth-nasa.jpg");
        displacementMap = TextureLoader.loadTexture2D("res/elevation.png");
        normalMap = TextureLoader.loadTexture2D("res/normalmap.png");
        starfieldTexture = TextureLoader.loadTextureCube(new String[] {
                "res/starmap_8k_4.png",
                "res/starmap_8k_3.png",
                "res/starmap_8k_6.png",
                "res/starmap_8k_5.png",
                "res/starmap_8k_2.png",
                "res/starmap_8k_1.png"
        });
        ringsTexture = TextureLoader.loadTexture1D("res/rings.jpg");

        TextureLoader.ShadowMap shadowMap = TextureLoader.createShadowMap(shadowMapWidth, shadowMapHeight);
        if (shadowMap != null) {
            shadowMapFramebuffer = shadowMap.frameBufferID;
            shadowMapDepthTexture = shadowMap.depthTextureID;
        }

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
    }

    private void loop() {
        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while (!glfwWindowShouldClose(window)) {
            update();
            render();
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            glfwPollEvents();
        }
    }

    private void update() {
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
        if (speedUp) {
            timePassage *= TIME_MOD;
        }
        if (slowDown) {
            timePassage /= TIME_MOD;
        }

        globeAzimuth += timePassage;
        updateModelMatrix();
        camAzimuth += timePassage;
        updateViewMatrix();
        updateMvpMatrix();
        updateLightMatrices();
    }

    private void render() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

        //render to shadow map

        glBindFramebuffer(GL_FRAMEBUFFER, shadowMapFramebuffer);
        glClear(GL_DEPTH_BUFFER_BIT);
        glViewport(0, 0, shadowMapWidth, shadowMapHeight);

        shadowMapShaderProgram.useProgram();
        shadowMapShaderProgram.setLightMvpMatrix(lightMvpMatrix);
        shadowMapShaderProgram.setDisplacementMap(displacementMap);
        shadowMapShaderProgram.setSeaLevel(SEA_LEVEL);
        shadowMapShaderProgram.setTerrainScale(TERRAIN_SCALE);
        globe.draw(shadowMapShaderProgram);

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, windowWidth * 2, windowHeight * 2); //TODO check for retina display

        //draw starfield

        skyboxShaderProgram.useProgram();
        Matrix4f vpRotationMatrix = new Matrix4f(viewMatrix);
        vpRotationMatrix.m30(0);
        vpRotationMatrix.m31(0);
        vpRotationMatrix.m32(0);
        skyboxShaderProgram.setVpMatrix(projectionMatrix.mul(vpRotationMatrix, vpRotationMatrix));
        skyboxShaderProgram.setTexture(starfieldTexture);
        skybox.draw(skyboxShaderProgram);

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
        globe.draw(globeShaderProgram);

        //daw rings

        glDisable(GL_CULL_FACE);
        ringsShaderProgram.useProgram();
        ringsShaderProgram.setMvpMatrix(mvpMatrix);
        ringsShaderProgram.setTexture(ringsTexture);
        rings.draw(ringsShaderProgram);

        //draw ocean

        oceanShaderProgram.useProgram();
        oceanShaderProgram.setMvpMatrix(mvpMatrix);
        oceanShaderProgram.setModelMatrix(modelMatrix);
        oceanShaderProgram.setLightDirection(lightX, lightY, lightZ);
        oceanShaderProgram.setCamPos(
                (float) (camDist * Math.sin(camAzimuth) * Math.cos(camElev)),
                (float) (camDist * Math.sin(camElev)),
                (float) (camDist * Math.cos(camAzimuth) * Math.cos(camElev)));
        ocean.draw(oceanShaderProgram);

        glfwSwapBuffers(window); // swap the color buffers
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
