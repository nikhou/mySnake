package com.nikho.oriens;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.io.IOException;
import java.nio.*;

import java.util.Objects;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Main {

    private long window;

    Model testModel;
    Model testSquare;
    Model head;
    Vector2i headPosition;
    Direction direction = Direction.RIGHT;
    int points = 5;

    public void run() {
        System.out.println("Hello LWJGL " + Version.getVersion() + "! "+"Java version: "+System.getProperty("java.version"));

        init();

        try {
            testModel = new Model("triangle.json");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RuntimeException e){
            System.out.println(e.getMessage());
        }

        try {
            testSquare = new Model("square.json");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            head = new Model("head.json");
        } catch (Exception e) {
            e.printStackTrace();
        }

        testSquare.scale.div(4);
        head.scale.div(8);
        loop();

        Model.cleanUp();

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        glfwTerminate();

        //glfwSetErrorCallback(null).free();
        Objects.requireNonNull(glfwSetErrorCallback(null)).free();
    }

    private void init() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if ( !glfwInit() )
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure GLFW
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

        // Create the window
        window = glfwCreateWindow(300, 300, "Hello World!", NULL, NULL);
        if ( window == NULL )
            throw new RuntimeException("Failed to create the GLFW window");

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
            if (key == GLFW_KEY_UP & direction!= Direction.UP)
                direction = Direction.UP;
            if (key == GLFW_KEY_DOWN & direction!= Direction.DOWN)
                direction = Direction.DOWN;
            if (key == GLFW_KEY_LEFT & direction!= Direction.LEFT)
                direction = Direction.LEFT;
            if (key == GLFW_KEY_RIGHT & direction!= Direction.RIGHT)
                direction = Direction.RIGHT;
        });

        // Get the thread stack and push a new frame
        try ( MemoryStack stack = stackPush() ) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            assert vidMode != null;
            glfwSetWindowPos(
                    window,
                    (vidMode.width() - pWidth.get(0)) / 2,
                    (vidMode.height() - pHeight.get(0)) / 2
            );
        } // the stack frame is popped automatically

        //scaling while resizing
        //glfwSetFramebufferSizeCallback(window, (window, width, height)->{glViewport(0,0,width, height);});
        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(1);
        // Make the window visible
        glfwShowWindow(window);

        // Create the OpenGL bindings available for use
        GL.createCapabilities();
    }

    private void loop() {
        int [][] field = new int[8][8];

        //set up snakes head position
        headPosition = new Vector2i(4, 4);
        // This line is critical for LWJGL's interoperation with GLFW'
        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        glClearColor(0.2f, 0.3f, 0.3f, 1.0f);

        while ( !glfwWindowShouldClose(window) ) {
            glClear(GL_COLOR_BUFFER_BIT); // clear the framebuffer

            float timeValue = (float) glfwGetTime();
            int vertexColorLocation = glGetUniformLocation(testSquare.program, "inColour");
            glUseProgram(testSquare.program);

            //rendering stuff
            for (int i = 0; i<field.length; i++){
                int x = i - field.length/2;
                for (int j = 0; j<field[i].length; j++){
                    int y = j - field.length/2;
                    Vector3f pos = new Vector3f((float) x/4,(float) y/4, 0);
                    if (i==headPosition.x && j==headPosition.y) {
                        head.rotation = direction.rotation;
                        glUniform4f(vertexColorLocation, 0.2f, 1f, 0.2f, 1.0f);
                        //fixing position for scale;
                        head.position = pos.sub(new Vector3f(-1.0f / 8.0f));
                        head.draw();
                    }
                    else if(field[i][j]>0){
                        glUniform4f(vertexColorLocation, 0.2f, 1f, 0.2f, 1.0f);
                        testSquare.position= pos;
                        testSquare.draw();
                    }
                    else if(((i+j)&1)==1) {
                        glUniform4f(vertexColorLocation, 0.2f, 0.2f, 0.2f, 1.0f);
                        testSquare.position= pos;
                        testSquare.draw();
                    }
                }
            }

            //tick
            if(glfwGetTime()>1){
                glfwSetTime(0);
                for (int i = 0; i<field.length; i++){
                    for (int j = 0; j<field[i].length;j++){
                        if (field[i][j]>0) field[i][j]--;
                    }
                }
                field[headPosition.x][headPosition.y]=points;
                headPosition.add(direction.value);
                headPosition.x = headPosition.x&7;
                headPosition.y = headPosition.y&7;
                if (field[headPosition.x][headPosition.y]>0)
                    break;
            }

            glfwSwapBuffers(window); // swap the color buffers

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();
        }
    }

    public static void main(String[] args) {
        new Main().run();
    }
}
