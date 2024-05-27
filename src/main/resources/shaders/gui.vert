#version 330 core

layout (location = 0) in vec2 vs_vertexPosition;
layout (location = 1) in vec2 vs_textureCoordinates;

out vec2 fs_textureCoordinates;

uniform int vs_scale;
uniform int vs_textureSizeX;
uniform int vs_textureSizeY;
uniform int vs_posX = 0;
uniform int vs_posY = 0;
uniform mat4 vs_projectionMatrix;

void main()
{
    gl_Position = vs_projectionMatrix * vec4(
        vec2(
            vs_vertexPosition.x * vs_scale * vs_textureSizeX + vs_posX * vs_scale,
            vs_vertexPosition.y * vs_scale * vs_textureSizeY + vs_posY * vs_scale
        ),
        -0.5,
        1.0
    );

    gl_Position.y = -gl_Position.y;

    fs_textureCoordinates = vs_textureCoordinates;
}