#version 330

layout (location=0) in vec2 position;

out vec3 colour;

void main()
{
    gl_Position = vec4(position,0.0, 1.0);
    colour=vec3(0.3, 0.4, 0.5);
}