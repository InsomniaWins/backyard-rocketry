#version 330 core

layout (location = 0) in vec3 vs_vertexPosition;
layout (location = 1) in vec2 vs_textureCoordinates;
layout (location = 2) in vec3 vs_normal;
layout (location = 3) in float vs_ambientOcclusionValue;

out vec2 fs_textureCoordinates;
out vec3 fs_normal;
out vec4 fs_eyeSpacePosition;
out float fs_ambientOcclusionValue;

uniform mat4 vs_modelMatrix;
uniform mat4 vs_viewMatrix;
uniform mat4 vs_projectionMatrix;
uniform vec2 vs_uvOffset;

void main() {
    vec4 vertexVector = vec4(vs_vertexPosition, 1.0);

    mat4 modelViewMatrix = vs_viewMatrix * vs_modelMatrix;
    mat4 modelViewProjectionMatrix = vs_projectionMatrix * modelViewMatrix;

    gl_Position = modelViewProjectionMatrix * vertexVector;

    fs_textureCoordinates = vs_textureCoordinates;
    fs_normal = vs_normal;
    fs_eyeSpacePosition = modelViewMatrix * vertexVector;
    fs_ambientOcclusionValue = vs_ambientOcclusionValue;
}