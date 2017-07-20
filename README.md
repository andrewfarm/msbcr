# MSBCR

By Andrew Farm  
[http://www.andrewofarm.com/](http://www.andrewofarm.com/)

![Screenshot](MSBCR.png)

## Introduction

MSBCR is a real-time 3D world renderer. It is written in Java using
[LWJGL](http://lwjgl.org/).

The name is an acronym for _Mid Seier's Blatant Civilization Ripoff_. However,
the project goals have changed greatly since the beginning and is meant to be a
3D world renderer rather than a strategy game based on the works of the good
people at Firaxis.

## License

This work is published under the
[MIT liscense](https://choosealicense.com/licenses/mit/).

## Dependencies

MSBCR requires Java 8 and JDK 1.8.

MSBCR uses LWJGL 3.1.2 as a Java wrapper around OpenGL. The required LWJGL
binaries are included in this repository in the lwjgl/ directory.

If you for some strange reason choose instead to download LWJGL from
[lwjgl.org](http://lwjgl.org/), MSBCR uses these components:

  * LWJGL core
  * OpenGL bindings
  * stb bindings
  * JOML v1.9.3

## Installation

MSBCR has been tested on macOS Sierra 10.12.5 with a 1536MB Intel Iris 6100
GPU.

MSBCR can by run by running HelloWorld.java. First you must ensure the LWJGL
binaries are added to the classpath or as a user library. The way to do this
varies by IDE.

Whichever platform you build MSBCR on, you must add the following to the Java
VM arguments:

    -Dorg.lwjgl.librarypath=lwjgl

This specifies the location of the LWJGL binaries (the lwjgl/
directory).

When running from the command line, simply run the command with this argument.
In IntelliJ IDEA, the VM arguments can be edited under Run > Edit
Configurations.

### X Window System

On some platforms, GLFW uses the X Window System. If this is the case, you will
also need to add the following VM argument in order for GLFW to work properly:

    -XstartOnFirstThread

## Support

MSBCR has only been tested on macOS. If you encounter any problems building or
running MSBCR, please submit an issue detailing the problem so I can promptly
fix it and/or update this documentation.

## Issues

MSBCR currently has some known issues, including:

  * Texture split along prime meridian
  * Visible T-junctions along the edges of globe tiles at different levels of
detail
  * Poor performance rendering the atmosphere when zoomed in
  * Dark spots along coastlines
  * Subtle lighting artifacts and occasional shadow acne
