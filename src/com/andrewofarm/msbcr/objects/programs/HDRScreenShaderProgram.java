package com.andrewofarm.msbcr.objects.programs;

/**
 * Created by Andrew Farm on 7/22/17.
 */
public class HDRScreenShaderProgram extends ScreenShaderProgram {
    public HDRScreenShaderProgram() {
        super(TextResourceReader.readFile("src/shaders/screen_vertex_shader.glsl"),
                TextResourceReader.readFile("src/shaders/hdr_screen_fragment_shader.glsl"));
    }
}
