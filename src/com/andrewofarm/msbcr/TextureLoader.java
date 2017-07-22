package com.andrewofarm.msbcr;

import org.lwjgl.stb.STBImage;

import java.io.File;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL14.GL_DEPTH_COMPONENT16;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL32.glFramebufferTexture;

/**
 * Created by Andrew on 6/10/17.
 */
public abstract class TextureLoader {

    static class TextureFramebuffer {
        int frameBufferID;
        int textureID;

        TextureFramebuffer(int frameBufferID, int textureID) {
            this.frameBufferID = frameBufferID;
            this.textureID = textureID;
        }
    }

    static int loadTexture2D(String imgPath) {
        return loadTexture(imgPath, true);
    }

    static int loadTexture1D(String imgPath) {
        return loadTexture(imgPath, false);
    }

    private static int loadTexture(String imgPath, boolean is2D) {
        int[] imgWidth = new int[1];
        int[] imgHeight = new int[1];
        @SuppressWarnings("unused") int[] channels = new int[1];
        System.out.println("loading " + imgPath);
        ByteBuffer buf = STBImage.stbi_load(imgPath, imgWidth, imgHeight, channels, 3);

        if (buf == null) {
            if (!new File(imgPath).exists()) {
                System.err.println(imgPath + " not found");
            } else {
                System.err.println("error reading " + imgPath);
            }
            return 0;
        }

        System.out.println("image loaded");

        //create texture object
        final int[] textureObjectIDs = new int[1];
        glGenTextures(textureObjectIDs);

        //check for errors
        if (textureObjectIDs[0] == 0) {
            System.err.println("could not create texture");
            return 0;
        }

        final int TARGET = is2D ? GL_TEXTURE_2D : GL_TEXTURE_1D;

        //bind texture to TARGET
        glBindTexture(TARGET, textureObjectIDs[0]);

        //specify texture filtering (scaling) methods
        glTexParameteri(TARGET, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glTexParameteri(TARGET, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        //load texture data into OpenGL
        buf.position(0);
        if (is2D) {
            glTexImage2D(TARGET, 0, GL_RGB, imgWidth[0], imgHeight[0], 0, GL_RGB, GL_UNSIGNED_BYTE, buf);
        } else {
            glTexImage1D(TARGET, 0, GL_RGB, imgWidth[0], 0, GL_RGB, GL_UNSIGNED_BYTE, buf);
        }

        //generate mipmaps
        glGenerateMipmap(TARGET);

        //unbind texture from TARGET
        glBindTexture(TARGET, 0);

        return textureObjectIDs[0];
    }

    static int loadTextureCube(String[] imgPaths) {
        //create texture object
        final int[] textureObjectIDs = new int[1];
        glGenTextures(textureObjectIDs);

        //check for errors
        if (textureObjectIDs[0] == 0) {
            System.err.println("could not create texture");
            return 0;
        }

        final ByteBuffer[] bufs = new ByteBuffer[6];

        int[] imgWidth = new int[1];
        int[] imgHeight = new int[1];
        @SuppressWarnings("unused") int[] channels = new int[1];
        for (int i = 0; i < 6; i++) {
            System.out.println("loading " + imgPaths[i]);
            bufs[i] = STBImage.stbi_load(imgPaths[i], imgWidth, imgHeight, channels, 3);

            if (bufs[i] == null) {
                if (!new File(imgPaths[i]).exists()) {
                    System.err.println(imgPaths[i] + " not found");
                } else {
                    System.err.println("error reading " + imgPaths[i]);
                }
                return 0;
            }

            System.out.println("image loaded");
        }

        glBindTexture(GL_TEXTURE_CUBE_MAP, textureObjectIDs[0]);

        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        glTexImage2D(GL_TEXTURE_CUBE_MAP_NEGATIVE_X, 0, GL_RGB, imgWidth[0], imgHeight[0], 0, GL_RGB, GL_UNSIGNED_BYTE, bufs[0]);
        glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0, GL_RGB, imgWidth[0], imgHeight[0], 0, GL_RGB, GL_UNSIGNED_BYTE, bufs[1]);
        glTexImage2D(GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0, GL_RGB, imgWidth[0], imgHeight[0], 0, GL_RGB, GL_UNSIGNED_BYTE, bufs[2]);
        glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0, GL_RGB, imgWidth[0], imgHeight[0], 0, GL_RGB, GL_UNSIGNED_BYTE, bufs[3]);
        glTexImage2D(GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0, GL_RGB, imgWidth[0], imgHeight[0], 0, GL_RGB, GL_UNSIGNED_BYTE, bufs[4]);
        glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0, GL_RGB, imgWidth[0], imgHeight[0], 0, GL_RGB, GL_UNSIGNED_BYTE, bufs[5]);

        glBindTexture(GL_TEXTURE_CUBE_MAP, 0);

        return textureObjectIDs[0];
    }

    static TextureFramebuffer createColorTextureFrameBuffer(int width, int height, int type, int filter) {
        return createTextureFramebuffer(GL_RGB, GL_RGB, type, filter, width, height, GL_COLOR_ATTACHMENT0, GL_COLOR_ATTACHMENT0);
    }

    static TextureFramebuffer createDepthTextureFrameBuffer(int width, int height, int type, int filter) {
        return createTextureFramebuffer(GL_DEPTH_COMPONENT16, GL_DEPTH_COMPONENT, type, filter, width, height, GL_DEPTH_ATTACHMENT, GL_NONE);
    }

    private static TextureFramebuffer createTextureFramebuffer(int internalFormat, int format, int type, int filter,
                                                               int width, int height, int attachment, int buffer) {
        System.out.println("creating textured framebuffer");
        int[] frameBufferIDs = new int[1];
        glGenFramebuffers(frameBufferIDs);

        int[] textureIDs = new int[1];
        glGenTextures(textureIDs);
        glBindTexture(GL_TEXTURE_2D, textureIDs[0]);

        glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, width, height, 0,
                format, type, 0);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, filter);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, filter);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        glBindFramebuffer(GL_FRAMEBUFFER, frameBufferIDs[0]);
        glFramebufferTexture(GL_FRAMEBUFFER, attachment, textureIDs[0], 0);
        glDrawBuffer(buffer); // No color buffer is drawn to.
        glReadBuffer(buffer);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        // Always check that our framebuffer is ok
        int status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        if(status == GL_FRAMEBUFFER_COMPLETE) {
            System.out.println("framebuffer creation successful");
        } else {
            System.err.println("error creating framebuffer (status: " + status + ")");
            return null;
        }

        return new TextureFramebuffer(frameBufferIDs[0], textureIDs[0]);
    }
}
