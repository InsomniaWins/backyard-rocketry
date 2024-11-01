#version 330 core
layout (location = 0) in vec3 vs_vertexPosition;
layout (location = 1) in vec2 vs_textureCoordinates;

out vec2 fs_textureCoordinates;

uniform mat4 vs_modelMatrix;
uniform mat4 vs_viewMatrix;
uniform mat4 vs_projectionMatrix;
uniform vec2 vs_uvOffset;

void main()
{
    gl_Position = vs_projectionMatrix * vs_viewMatrix * vs_modelMatrix * vec4(vs_vertexPosition, 1.0);
    fs_textureCoordinates = vs_textureCoordinates;
}