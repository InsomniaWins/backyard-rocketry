#version 330 core
layout (location = 0) in vec3 vs_vertexPosition;
layout (location = 1) in vec2 vs_textureCoordinates;

out vec2 fs_textureCoordinates;

void main()
{
    gl_Position = vec4(vs_vertexPosition, 1.0);
    fs_textureCoordinates = vs_textureCoordinates;
}