package com.nikho.oriens;

import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import javax.annotation.Nonnull;
import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class GlwgTest {
    String getResource(String resourceName) throws IOException {
        if (!resourceName.contains("."))
            resourceName+=".json";
        InputStream resource = getClass().getClassLoader().getResourceAsStream(resourceName);
        if (resource==null) throw new IOException("Resource doesn't exist or resources name is wrong: "+resourceName);
        return new String(resource.readAllBytes());
    }

    String getResource(String location, String resourceName) throws IOException {
        return getResource(location+(location.endsWith("/")?"":"/")+resourceName);
    }

    static long window;

    @BeforeAll
    static void setUp(){
        if (!glfwInit())
        {
            System.out.println("Error while GLFW initialization");
            System.exit(1);
        }
        glfwSetErrorCallback((err, desc)-> System.out.println("GLFW Error "+err+": "+desc));
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        window = glfwCreateWindow(640, 480, "TestWindow", NULL, NULL);
        assertFalse(()-> 0==window);
        glfwMakeContextCurrent(window);
        GL.createCapabilities();

    }

    @Test
    void modelProgramTest() throws IOException {
        Model testModel = new Model("models/triangle.json");
        //testModel.addShaderProgram("basic_shader_program");
        assertNotEquals(0, testModel.program);
    }

    @Test
    void resourceTest() throws IOException {
        int i = 7;
        System.out.println((float) 7/2);
    }

    @AfterAll
    static void cleanUp(){
        glfwDestroyWindow(window);
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }
}
