package com.andrewofarm.msbcr.objects.programs;

import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1f;

/**
 * Created by Andrew Farm on 7/22/17.
 */
public class ExperimentalScreenShaderProgram extends ScreenShaderProgram {

    private static final String U_NOISE_PHASE = "u_NoisePhase";

    private final int uNoisePhaseLocation;

    public ExperimentalScreenShaderProgram() {
        super(TextResourceReader.readFile("src/shaders/screen_vertex_shader.glsl"),
                TextResourceReader.readFile("src/shaders/experimental_screen_fragment_shader.glsl") +
                        TextResourceReader.readFile("src/shaders/webgl-noise/src/classicnoise3D.glsl"));

        uNoisePhaseLocation = glGetUniformLocation(programID, U_NOISE_PHASE);
    }

    public void setNoisePhase(float noisePhase) {
        glUniform1f(uNoisePhaseLocation, noisePhase);
    }
}
