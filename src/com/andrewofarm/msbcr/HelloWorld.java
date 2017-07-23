package com.andrewofarm.msbcr;

import com.andrewofarm.msbcr.objects.geom.*;
import com.andrewofarm.msbcr.objects.programs.*;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.Version;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.GL_VERTEX_PROGRAM_POINT_SIZE;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * Created by Andrew Farm on 6/7/17.
 */
@SuppressWarnings("DefaultFileTemplate")
public class HelloWorld {

    private long window;

    private int windowWidth = 800;
    private int windowHeight = 600;

    private static final float GLOBE_RADIUS = 1.0f;
    private static final float ATMOSPHERE_WIDTH = 0.2f;
    private static final float ATMOSPHERE_CEILING = GLOBE_RADIUS + ATMOSPHERE_WIDTH;
    private static final float SEA_LEVEL = 0.5f;
    private static final float TERRAIN_SCALE = 0.75f;

    private float lightX = -1, lightY = 0, lightZ = 0;

    private static final float LOOK_SPEED = 0.02f;
    private boolean up, down, left, right;
    private float camLookAzimuth = 0, camLookElev = 0;
    private float camAzimuth = 0, camElev = 0;
    private float camDist = 4;
    private float globeAzimuth = 0;
    private Vector3f camPos = new Vector3f();
    private Vector3f camPosModelSpace = new Vector3f();
    private Vector4f camPos4 = new Vector4f();
    private Vector4f camPosModelSpace4 = new Vector4f();
    private static final float FOV = (float) Math.PI / 4;
    private static final float TWO_TAN_HALF_FOV = (float) (2 * Math.tan(FOV / 2));

    private float auroraNoisePhase = 0;
    private static final float AURORA_NOISE_PHASE_INCREMENT = 20f;
    private float cloudsNoisePhase = 0;
    private static final float CLOUDS_NOISE_PHASE_INCREMENT = 5f;

    private boolean drawRings = false;

    private boolean dragging = false;
    private double prevX, prevY;

    private float timePassage = 0.005f;
    private static final float TIME_MOD = 1.1f;
    private boolean speedUp, slowDown;
    private boolean geostationary = true;

    private Matrix4f modelMatrix = new Matrix4f();
    private Matrix4f inverseModelMatrix = new Matrix4f();

    private static final float GEOMAGNETIC_POLE_LATITUDE = 0.3033f;
    private static final float GEOMAGNETIC_POLE_LONGITUDE = 0.1681f;
    private static final float AURORA_POLAR_ANGLE = 0.3f;
    private static final float AURORA_LOWER_BOUND = GLOBE_RADIUS + 0.01f;
    private static final float AURORA_UPPER_BOUND = GLOBE_RADIUS + 0.075f;

    private Matrix4f auroraModelMatrix = new Matrix4f();

    private Matrix4f viewMatrix = new Matrix4f();
    private Matrix4f projectionMatrix = new Matrix4f();
    private Matrix4f mvpMatrix = new Matrix4f();

    private Matrix4f vpRotationMatrix = new Matrix4f();

    private Matrix4f lightViewMatrix = new Matrix4f().lookAt(
            new Vector3f(lightX, lightY, lightZ).normalize().mul(2),
            new Vector3f(0, 0, 0),
            new Vector3f(0, 1, 0));
    private Matrix4f lightProjectionMatrix = new Matrix4f().ortho(-2, 2, -2, 2, 0, 6);
    private Matrix4f lightBiasMatrix = new Matrix4f(
            0.5f, 0.0f, 0.0f, 0.0f,
            0.0f, 0.5f, 0.0f, 0.0f,
            0.0f, 0.0f, 0.5f, 0.0f,
            0.5f, 0.5f, 0.5f, 1.0f);
    private Matrix4f lightMvpMatrix = new Matrix4f();
    private Matrix4f lightBiasMvpMatrix = new Matrix4f();

    private static final int MERIDIANS = 512;
    private static final int PARALLELS = 256;

    private SkyboxGeometry skybox = new SkyboxGeometry();
    private SunGeometry sun = new SunGeometry(lightX, lightY, lightZ);
    private AdaptiveGlobeGeometry globe = new AdaptiveGlobeGeometry(1.0f, 64);
    private RingsGeometry rings = new RingsGeometry(128, 1.5f, 3.0f);
    private OceanGeometry ocean = new OceanGeometry(1.0f, MERIDIANS, PARALLELS);
    private OceanGeometry cloudLayer1 = new OceanGeometry(GLOBE_RADIUS + 0.02f, MERIDIANS, PARALLELS);
    private AtmosphereCeilingGeometry atmCeiling = new AtmosphereCeilingGeometry(ATMOSPHERE_CEILING, 64, 32);
    private AuroraGeometry aurora = new AuroraGeometry(512, AURORA_LOWER_BOUND, AURORA_UPPER_BOUND);

    private AdaptiveGlobeShadowMapShaderProgram shadowMapShaderProgram;
    private SkyboxShaderProgram skyboxShaderProgram;
    private SunShaderProgram sunShaderProgram;
    private AdaptiveGlobeShaderProgram adaptiveGlobeShaderProgram;
    private RingsShaderProgram ringsShaderProgram;
    private OceanShaderProgram oceanShaderProgram;
    private CloudShaderProgram cloudShaderProgram;
    private AtmosphereCeilingShaderProgram atmosphereCeilingShaderProgram;
    private AuroraShaderProgram auroraShaderProgram;

    private ScreenGeometry screenGeometry = new ScreenGeometry();
    private ScreenShaderProgram screenShaderProgram;

    private boolean hdr = true;
    private SimpleScreenShaderProgram simpleScreenShaderProgram;
    private HDRScreenShaderProgram hdrScreenShaderProgram;

    private int starfieldTexture;
    private int sunTexture;
    private int globeTexture;
    private int displacementMap;
    private int normalMap;
    private int ringsTexture;
    private int auroraTexture;

    private int shadowMapWidth = 4096;
    private int shadowMapHeight = 4096;

    private int shadowMapFramebuffer;
    private int shadowMapDepthTexture;

    private int screenFramebuffer;
    private int screenTexture;

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

        glfwWindowHint(GLFW_FOCUSED, GLFW_TRUE);

        // Create the window
        window = glfwCreateWindow(windowWidth, windowHeight, "Hello World!", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("failed to create GLFW window");
        }

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (action == GLFW_PRESS) {
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
                    case GLFW_KEY_ESCAPE:
                        geostationary = !geostationary;
                        break;
                    case GLFW_KEY_H:
                        hdr = !hdr;
                        screenShaderProgram = hdr ? hdrScreenShaderProgram : simpleScreenShaderProgram;
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
            camDist = Math.min(Math.max(camDist, 1.005f), 15);
            camLookElev = (float) (1.6 * Math.pow(2, -4 * (camDist - GLOBE_RADIUS)));
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

        shadowMapShaderProgram = new AdaptiveGlobeShadowMapShaderProgram();
        skyboxShaderProgram = new SkyboxShaderProgram();
        sunShaderProgram = new SunShaderProgram();
        adaptiveGlobeShaderProgram = new AdaptiveGlobeShaderProgram();
        if (drawRings) ringsShaderProgram = new RingsShaderProgram();
        oceanShaderProgram = new OceanShaderProgram();
        cloudShaderProgram = new CloudShaderProgram();
        atmosphereCeilingShaderProgram = new AtmosphereCeilingShaderProgram();
        auroraShaderProgram = new AuroraShaderProgram();

        simpleScreenShaderProgram = new SimpleScreenShaderProgram();
        hdrScreenShaderProgram = new HDRScreenShaderProgram();
        screenShaderProgram = hdr ? hdrScreenShaderProgram : simpleScreenShaderProgram;

        globeTexture = TextureLoader.loadTexture2D("res/earth-nasa.jpg");
        displacementMap = TextureLoader.loadTexture2D("res/elevation.png");
        normalMap = TextureLoader.loadTexture2D("res/normalmap.png");
        sunTexture = TextureLoader.loadTexture2D("res/sun.jpg");
        starfieldTexture = TextureLoader.loadTextureCube(new String[] {
                "res/starmap_8k_4.png",
                "res/starmap_8k_3.png",
                "res/starmap_8k_6.png",
                "res/starmap_8k_5.png",
                "res/starmap_8k_2.png",
                "res/starmap_8k_1.png"
        });
        if (drawRings) ringsTexture = TextureLoader.loadTexture1D("res/rings.jpg");
        auroraTexture = TextureLoader.loadTexture1D("res/aurora.png");

        TextureLoader.TextureFramebuffer shadowMap = TextureLoader.createDepthTextureFrameBuffer(
                shadowMapWidth, shadowMapHeight, GL_FLOAT, GL_LINEAR);
        if (shadowMap != null) {
            shadowMapFramebuffer = shadowMap.frameBufferID;
            shadowMapDepthTexture = shadowMap.textureID;
        }

        TextureLoader.TextureFramebuffer screenBuffer = TextureLoader.createColorTextureFrameBuffer(
                windowWidth * 2, windowHeight * 2, GL_FLOAT, GL_NEAREST);
        if (screenBuffer != null) {
            screenFramebuffer = screenBuffer.frameBufferID;
            screenTexture = screenBuffer.textureID;
        }

        modelMatrix.identity();
        viewMatrix.setTranslation(0, 0, -camDist).rotate(camAzimuth, 0, 1, 0).rotate(camElev, 1, 0, 0);
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

        auroraNoisePhase += AURORA_NOISE_PHASE_INCREMENT * timePassage;
        cloudsNoisePhase += CLOUDS_NOISE_PHASE_INCREMENT * timePassage;

        globeAzimuth += timePassage;
        updateModelMatrix();
        if (geostationary) {
            camAzimuth += timePassage;
            updateViewMatrix();
        }
        updateMvpMatrix(modelMatrix);
        updateLightMatrices();
        updateVpRotationMatrix();
        updateCamPos();

        globe.update(camPosModelSpace, TWO_TAN_HALF_FOV);
    }

    private void render() {
        renderScene();

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, windowWidth * 2, windowHeight * 2);
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glDisable(GL_CULL_FACE);
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
        glClear(GL_COLOR_BUFFER_BIT);
        screenShaderProgram.useProgram();
        screenShaderProgram.setTexture(screenTexture);
        screenGeometry.draw(screenShaderProgram);

        glfwSwapBuffers(window); // swap the color buffers
    }

    private void renderScene() {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
        glEnable(GL_CULL_FACE);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_VERTEX_PROGRAM_POINT_SIZE);

        //render to shadow map

        glBindFramebuffer(GL_FRAMEBUFFER, shadowMapFramebuffer);
        glClear(GL_DEPTH_BUFFER_BIT);
        glViewport(0, 0, shadowMapWidth, shadowMapHeight);

        shadowMapShaderProgram.useProgram();
        shadowMapShaderProgram.setLightMvpMatrix(lightMvpMatrix);
        shadowMapShaderProgram.setDisplacementMap(displacementMap);
        shadowMapShaderProgram.setSeaLevel(SEA_LEVEL);
        shadowMapShaderProgram.setTerrainScale(TERRAIN_SCALE);
        globe.draw(shadowMapShaderProgram, camPosModelSpace, TWO_TAN_HALF_FOV);

        glBindFramebuffer(GL_FRAMEBUFFER, screenFramebuffer);
        glViewport(0, 0, windowWidth * 2, windowHeight * 2); //TODO check for retina display

        //draw starfield

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        skyboxShaderProgram.useProgram();
        skyboxShaderProgram.setVpMatrix(projectionMatrix.mul(vpRotationMatrix, vpRotationMatrix));
        skyboxShaderProgram.setTexture(starfieldTexture);
        skybox.draw(skyboxShaderProgram);

        //draw sun

        sunShaderProgram.useProgram();
        sunShaderProgram.setVpMatrix(vpRotationMatrix);
        sunShaderProgram.setSize(500);
        sunShaderProgram.setColor(new Vector3f(1.0f, 0.95f, 0.9f));
        sunShaderProgram.setTexture(sunTexture);
        sun.draw(sunShaderProgram);

        //draw globe

        adaptiveGlobeShaderProgram.useProgram();
        adaptiveGlobeShaderProgram.setMvpMatrix(mvpMatrix);
        adaptiveGlobeShaderProgram.setModelMatrix(modelMatrix);
        adaptiveGlobeShaderProgram.setLightBiasMvpMatrix(lightBiasMvpMatrix);
        adaptiveGlobeShaderProgram.setLightDirection(lightX, lightY, lightZ);
        adaptiveGlobeShaderProgram.setDisplacementMap(displacementMap);
        adaptiveGlobeShaderProgram.setTexture(globeTexture);
        adaptiveGlobeShaderProgram.setNormalMap(normalMap);
        adaptiveGlobeShaderProgram.setShadowMap(shadowMapDepthTexture);
        adaptiveGlobeShaderProgram.setSeaLevel(SEA_LEVEL);
        adaptiveGlobeShaderProgram.setTerrainScale(TERRAIN_SCALE);
        adaptiveGlobeShaderProgram.setCamPos(camPos.get(0), camPos.get(1), camPos.get(2));
        adaptiveGlobeShaderProgram.setGlobeRadius(GLOBE_RADIUS);
        adaptiveGlobeShaderProgram.setAtmosphereWidth(ATMOSPHERE_WIDTH);
        globe.draw(adaptiveGlobeShaderProgram, camPosModelSpace, TWO_TAN_HALF_FOV);

        //draw rings

        if (drawRings) {
            glDisable(GL_CULL_FACE);
            ringsShaderProgram.useProgram();
            ringsShaderProgram.setMvpMatrix(mvpMatrix);
            ringsShaderProgram.setLightBiasMvpMatrixMatrix(lightBiasMvpMatrix);
            ringsShaderProgram.setTexture(ringsTexture);
            ringsShaderProgram.setShadowMap(shadowMapDepthTexture);
            rings.draw(ringsShaderProgram);
        }

        //draw ocean

        glEnable(GL_CULL_FACE);
        oceanShaderProgram.useProgram();
        oceanShaderProgram.setMvpMatrix(mvpMatrix);
        oceanShaderProgram.setModelMatrix(modelMatrix);
        oceanShaderProgram.setLightDirection(lightX, lightY, lightZ);
        oceanShaderProgram.setCamPos(camPos.get(0), camPos.get(1), camPos.get(2));
        oceanShaderProgram.setGlobeRadius(GLOBE_RADIUS);
        oceanShaderProgram.setElevationMap(displacementMap);
        oceanShaderProgram.setSeaLevel(SEA_LEVEL);
        oceanShaderProgram.setAtmosphereWidth(ATMOSPHERE_WIDTH);
        ocean.draw(oceanShaderProgram);

        //draw atmosphere ceiling

        glEnable(GL_CULL_FACE);
        glCullFace(GL_FRONT);
        glBlendFunc(GL_ONE, GL_ONE);
        atmosphereCeilingShaderProgram.useProgram();
        atmosphereCeilingShaderProgram.setMvpMatrix(mvpMatrix);
        atmosphereCeilingShaderProgram.setModelMatrix(modelMatrix);
        atmosphereCeilingShaderProgram.setLightDirection(lightX, lightY, lightZ);
        atmosphereCeilingShaderProgram.setCamPos(camPos.get(0), camPos.get(1), camPos.get(2));
        atmosphereCeilingShaderProgram.setGlobeRadius(GLOBE_RADIUS);
        atmosphereCeilingShaderProgram.setAtmosphereWidth(ATMOSPHERE_WIDTH);
        atmCeiling.draw(atmosphereCeilingShaderProgram);

        //draw cloud layers

        glDisable(GL_CULL_FACE);
        glDepthMask(false); //perform depth tests, but don't write to the depth buffer
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        cloudShaderProgram.useProgram();
        cloudShaderProgram.setMvpMatrix(mvpMatrix);
        cloudShaderProgram.setModelMatrix(modelMatrix);
        cloudShaderProgram.setLightBiasMvpMatrix(lightBiasMvpMatrix);
        cloudShaderProgram.setCloudCoverUnit(0); //TODO
        cloudShaderProgram.setShadowMapUnit(shadowMapDepthTexture);
        cloudShaderProgram.setLightDirection(lightX, lightY, lightZ);
        cloudShaderProgram.setCamPos(camPos.get(0), camPos.get(1), camPos.get(2));
        cloudShaderProgram.setGlobeRadius(GLOBE_RADIUS);
        cloudShaderProgram.setAtmosphereWidth(ATMOSPHERE_WIDTH);
        cloudShaderProgram.setNoisePhase(cloudsNoisePhase);
        cloudLayer1.draw(cloudShaderProgram);
        glDepthMask(true);

        //draw aurora

        glDisable(GL_CULL_FACE);
        glDepthMask(false); //perform depth tests, but don't write to the depth buffer
        glBlendFunc(GL_ONE, GL_ONE);
        updateMvpMatrix(auroraModelMatrix);
        auroraShaderProgram.useProgram();
        auroraShaderProgram.setMvpMatrix(mvpMatrix);
        auroraShaderProgram.setTexture(auroraTexture);
        auroraShaderProgram.setNoisePhase(auroraNoisePhase);
        auroraShaderProgram.setPolarAngle(AURORA_POLAR_ANGLE);
        aurora.draw(auroraShaderProgram);
        auroraShaderProgram.setPolarAngle((float) Math.PI + AURORA_POLAR_ANGLE);
        aurora.draw(auroraShaderProgram);

        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glDepthMask(true);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    private void updateProjectionMatrix(int width, int height) {
        projectionMatrix.identity();
        projectionMatrix.perspective(FOV, (float) width / (float) height, 0.001f, 20f);
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

        modelMatrix.invert(inverseModelMatrix);

        auroraModelMatrix.set(modelMatrix)
                .rotate(-GEOMAGNETIC_POLE_LATITUDE, 1, 0, 0)
                .rotate(GEOMAGNETIC_POLE_LONGITUDE, 0, 1, 0);
    }

    private void updateLightMatrices() {
        lightMvpMatrix.set(lightProjectionMatrix).mul(lightViewMatrix).mul(modelMatrix);
        lightBiasMvpMatrix.set(lightBiasMatrix).mul(lightMvpMatrix);
    }

    private void updateMvpMatrix(Matrix4f modelMatrix) {
        mvpMatrix.set(projectionMatrix).mul(viewMatrix).mul(modelMatrix);
    }

    private void updateVpRotationMatrix() {
        vpRotationMatrix.set(viewMatrix);
        vpRotationMatrix.m30(0);
        vpRotationMatrix.m31(0);
        vpRotationMatrix.m32(0);
    }

    private void updateCamPos() {
        camPos.set(
                (float) (camDist * Math.sin(camAzimuth) * Math.cos(camElev)),
                (float) (camDist * Math.sin(camElev)),
                (float) (camDist * Math.cos(camAzimuth) * Math.cos(camElev)));
        camPos4.set(camPos, 1.0f);
        inverseModelMatrix.transform(camPos4, camPosModelSpace4);
        camPosModelSpace.set(
                camPosModelSpace4.get(0),
                camPosModelSpace4.get(1),
                camPosModelSpace4.get(2));
    }

    public static void main(String[] args) {
        new HelloWorld().run();
    }
}
