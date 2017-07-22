package com.andrewofarm.msbcr.objects.programs;

/**
 * Created by Andrew Farm on 7/22/17.
 */
public class SimpleScreenShaderProgram extends ScreenShaderProgram {
    public SimpleScreenShaderProgram() {
        super(TextResourceReader.readFile("src/shaders/screen_vertex_shader.glsl"),
                TextResourceReader.readFile("src/shaders/simple_screen_fragment_shader.glsl"));
    }
}
