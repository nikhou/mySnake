package com.nikho.oriens;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.json.JSONArray;
import org.json.JSONObject;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class Model {
    private static final Set<Integer> buffers= new HashSet<Integer>();
    private static final Set<Integer> vertexArrays= new HashSet<Integer>();
    private static final Map<String,Integer> programs= new HashMap<>();

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

    public int program;
    int vertexArrayObject;
    int arrayCount;
    int mode = GL_TRIANGLES;

    public Vector3f position = new Vector3f();
    public Vector3f scale = new Vector3f(1);
    public Quaternionf rotation = new Quaternionf();

    Model(String source) throws IOException {
        JSONObject modelInfo = new JSONObject(getResource("models",source));

        mode = modelInfo.getInt("mode");
        //loading vertices
        float[] vertices;
        JSONArray verticesData = modelInfo.getJSONArray("vertices");
        vertices = new float[verticesData.length()];
        for(int i = 0; i<verticesData.length(); i++)
            vertices[i]=verticesData.getFloat(i);

        //get program if already exist
        String programName = modelInfo.getString("program");
        if(programs.containsKey(programName))
            program=programs.get(programName);
        else
            addShaderProgram(programName);

        //Sending data for shader
        vertexArrayObject = glGenVertexArrays();
        vertexArrays.add(vertexArrayObject);
        int vertexBufferObject = glGenBuffers();
        buffers.add(vertexBufferObject);

        glBindVertexArray(vertexBufferObject);
        glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObject);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        int stride = new JSONObject(getResource("programs", programName)).getJSONObject("vertex").getInt("data");
        arrayCount = vertices.length/stride;
        glVertexAttribPointer(  0,2,GL_FLOAT,false,
                                stride*Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        glBindVertexArray(0);
    }

    public void addShaderProgram(String name) throws IOException {
        int vertexShader;
        int fragmentShader;
        JSONObject shaderProgramInfo = new JSONObject(getResource("programs", name));
        //load vertex shader
        vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, getResource("shaders",shaderProgramInfo.getJSONObject("vertex").getString("name")+".vert"));
        glCompileShader(vertexShader);
        //checking shader status
        {int success;
            success=glGetShaderi(vertexShader, GL_COMPILE_STATUS);
            if (success==0)throw new GLShaderError(vertexShader);}
        //loading fragment shader
        fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, getResource("shaders/"+shaderProgramInfo.getJSONObject("fragment").getString("name")+".frag"));
        glCompileShader(fragmentShader);
        //checking shader status
        {   int success;
            success=glGetShaderi(vertexShader, GL_COMPILE_STATUS);
            if (success==0)throw new GLShaderError(fragmentShader);}
        //linking program
        program=glCreateProgram();
        programs.put(name, program);
        glAttachShader(program, vertexShader);
        glAttachShader(program, fragmentShader);
        glLinkProgram(program);

        //test program status
        {   int success;
            success=glGetProgrami(program, GL_LINK_STATUS);
            if (success==0)throw new GLProgramError(program);}

        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
    }

    public void draw(){
        Matrix4f transform = new Matrix4f()
                .translate(position)
                .scale(scale)
                .rotate(rotation);
        int transformLocation = glGetUniformLocation(program, "transform");
        glUniformMatrix4fv(transformLocation, false, transform.get(new float[16]));

        glUseProgram(program);
        glBindVertexArray(vertexArrayObject);
        glDrawArrays(mode,0, arrayCount);
    }

    void draw(Vector3f position, Vector3f scale, Quaternionf rotation){
        Matrix4f transform = new Matrix4f()
                .translate(position)
                .scale(scale)
                .rotate(rotation);
        int transformLocation = glGetUniformLocation(program, "transform");
        glUniformMatrix4fv(transformLocation, false, transform.get(new float[16]));

        glUseProgram(program);
        glBindVertexArray(vertexArrayObject);
        glDrawArrays(mode,0, arrayCount);
    }

    static void cleanUp(){
        vertexArrays.forEach(GL30::glDeleteVertexArrays);
        buffers.forEach(GL15::glDeleteBuffers);
        programs.values().forEach(GL15::glDeleteBuffers);
    }
}
