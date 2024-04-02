#version 330 core

layout (location = 0) in vec2 vs_vertexPosition;
layout (location = 1) in vec2 vs_textureCoordinates;

out vec2 fs_textureCoordinates;

uniform mat4 vs_projectionMatrix;

void main()
{
    gl_Position = vs_projectionMatrix * vec4(vs_vertexPosition.xy, -0.5, 1.0);
    gl_Position.y = -gl_Position.y;
    fs_textureCoordinates = vs_textureCoordinates;
}