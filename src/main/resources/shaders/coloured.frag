#version 330

uniform vec4 inColour;

out vec4 fragColor;

void main()
{
    fragColor = vec4(inColour);
}