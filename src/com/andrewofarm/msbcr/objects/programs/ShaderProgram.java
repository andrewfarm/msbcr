package com.andrewofarm.msbcr.objects.programs;

import static org.lwjgl.opengl.GL20.*;

/**
 * Created by Andrew on 6/7/17.
 */
public abstract class ShaderProgram {

    public final int programID;

    ShaderProgram(String vertexShaderSource, String fragmentShaderSource) {
        programID = buildProgram(vertexShaderSource, fragmentShaderSource);
    }

    public void useProgram() {
        glUseProgram(programID);
    }

    private static int compileShader(int type, String shaderSource) {
        int shaderID = glCreateShader(type);

        //check for errors
        if (shaderID == 0) {
            System.err.println("could not create shader");
            return 0;
        }

        //upload and compile source
        glShaderSource(shaderID, shaderSource);
        glCompileShader(shaderID);

        //check that the shader was successfully compiled
        final int[] compileStatus = new int[1];
        glGetShaderiv(shaderID, GL_COMPILE_STATUS, compileStatus);
        if (compileStatus[0] == 0) {
            System.err.println("shader compilation failed");
            System.err.println(glGetShaderInfoLog(shaderID));
            glDeleteShader(shaderID);
            return 0;
        }

        return shaderID;
    }

    private static int linkProgram(int vertexShaderID, int fragmentShaderID) {
        final int programID = glCreateProgram();

        if (programID == 0) {
            System.err.println("could not create shader program");
            System.err.println(glGetProgramInfoLog(programID));
            return 0;
        }

        glAttachShader(programID, vertexShaderID);
        glAttachShader(programID, fragmentShaderID);
        glLinkProgram(programID);

        final int[] linkStatus = new int[1];
        glGetProgramiv(programID, GL_LINK_STATUS, linkStatus);
        if (linkStatus[0] == 0) {
            System.err.println("shader program linking failed");
            System.err.println(glGetProgramInfoLog(programID));
        }

        return programID;
    }

    private static void validateProgram(int programID) {
        glValidateProgram(programID);
        final int[] validateStatus = new int[1];
        glGetProgramiv(programID, GL_VALIDATE_STATUS, validateStatus);

        if (validateStatus[0] == 0) {
            System.err.println("shader program validation failed");
            System.err.println(glGetProgramInfoLog(programID));
        }
    }

    private static int buildProgram(String vertexShaderSource, String fragmentShaderSource) {
        System.out.println("compiling vertex shader");
        int vertexShaderID = compileShader(GL_VERTEX_SHADER, vertexShaderSource);
        System.out.println("compiling fragment shader");
        int fragmentShaderID = compileShader(GL_FRAGMENT_SHADER, fragmentShaderSource);
        System.out.println("linking shader program");
        int programID = linkProgram(vertexShaderID, fragmentShaderID);
        //for debugging
        //System.out.println("validating shader program");
        //validateProgram(programID);
        return programID;
    }
}
