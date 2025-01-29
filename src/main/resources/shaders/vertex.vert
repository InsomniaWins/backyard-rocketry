#version 330 core
layout (location = 0) in vec3 vs_vertexPosition;
layout (location = 1) in vec2 vs_textureCoordinates;

out vec2 fs_textureCoordinates;

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
    gl_Position = vs_projectionMatrix * vs_viewMatrix * vs_modelMatrix * vec4(vs_vertexPosition, 1.0);

    gl_Position = snap(gl_Position, vec2(320, 240));

    fs_textureCoordinates = vs_textureCoordinates;
}