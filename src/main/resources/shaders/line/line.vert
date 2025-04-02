#version 330 core

layout (location = 0) in vec3 vs_vertexPosition;

uniform mat4 vs_modelMatrix;
uniform mat4 vs_viewMatrix;
uniform mat4 vs_projectionMatrix;


void main() {
    mat4 mvMatrix = vs_viewMatrix * vs_modelMatrix;
    mat4 mvpMatrix = vs_projectionMatrix * mvMatrix;
    gl_Position = mvpMatrix * vec4(vs_vertexPosition, 1.0);
}