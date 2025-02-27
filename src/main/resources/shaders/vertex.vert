#version 330 core

layout (location = 0) in vec3 vs_vertexPosition;
layout (location = 1) in vec2 vs_textureCoordinates;
layout (location = 2) in vec3 vs_normal;

out vec2 fs_textureCoordinates;
out vec3 fs_normal;

uniform bool vs_vertexSnapping = false;
uniform mat4 vs_modelMatrix;
uniform mat4 vs_viewMatrix;
uniform mat4 vs_projectionMatrix;
uniform vec2 vs_uvOffset;

// taken from u/PixelbearGames on reddit
vec4 snap(vec4 vertex, vec2 resolution) {
    vec4 snappedPos = vertex;
    snappedPos.xyz = vertex.xyz / vertex.w; // convert to normalised device coordinates (NDC)
    snappedPos.xy = floor(resolution * snappedPos.xy) / resolution; // snap the vertex to the lower-resolution grid
    snappedPos.xyz *= vertex.w; // convert back to projection-space
    return snappedPos;
}

void main() {
    mat4 mvMatrix = vs_viewMatrix * vs_modelMatrix;
    mat4 mvpMatrix = vs_projectionMatrix * mvMatrix;
    gl_Position = mvpMatrix * vec4(vs_vertexPosition, 1.0);

    mat3 normalMatrix = mat3(vs_projectionMatrix * vs_modelMatrix);
    fs_normal = normalMatrix * vs_normal;
    fs_normal.z *= -1;

    if (vs_vertexSnapping) {
        gl_Position = snap(gl_Position, vec2(320 * 0.25, 240 * 0.25));
    }

    fs_textureCoordinates = vs_textureCoordinates;

}