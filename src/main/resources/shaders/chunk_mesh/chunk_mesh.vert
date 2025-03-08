#version 330 core

layout (location = 0) in vec3 vs_vertexPosition;
layout (location = 1) in vec2 vs_textureCoordinates;
layout (location = 2) in vec3 vs_normal;
layout (location = 3) in float vs_ambientOcclusionValue;
layout (location = 4) in float vs_framesPerSecond;
layout (location = 5) in float vs_frameAmount;
layout (location = 6) in float vs_waveStrength;
layout (location = 7) in vec4 vs_lightValue;

out vec2 fs_textureCoordinates;
out vec3 fs_normal;
out vec4 fs_eyeSpacePosition;
out float fs_ambientOcclusionValue;
out float fs_affine;
out float fs_framesPerSecond;
out int fs_frameAmount;
out vec3 fs_fragmentPosition;
out vec3 fs_lightValue;

uniform float vs_time;
uniform bool vs_vertexSnapping = true;
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
    vec4 vertexVector = vec4(vs_vertexPosition, 1.0);

    vec4 worldPosition = vs_modelMatrix * vertexVector;
    // TODO: implement vector waving (not just vertical per face)
    //vertexVector.x += sin(worldPosition.x + worldPosition.z + vs_time) * vs_waveStrength;
    vertexVector.y += sin(worldPosition.x + worldPosition.z + vs_time) * vs_waveStrength;
    //vertexVector.z += sin(worldPosition.x + worldPosition.z + vs_time) * vs_waveStrength;

    mat4 modelViewMatrix = vs_viewMatrix * vs_modelMatrix;
    mat4 modelViewProjectionMatrix = vs_projectionMatrix * modelViewMatrix;

    gl_Position = modelViewProjectionMatrix * vertexVector;

    if (vs_vertexSnapping) {
        gl_Position = snap(gl_Position, vec2(320 * 0.25, 240 * 0.25));
    }

    fs_textureCoordinates = vs_textureCoordinates;
    fs_normal = normalize(vs_normal);

    fs_eyeSpacePosition = modelViewMatrix * vertexVector;
    fs_ambientOcclusionValue = vs_ambientOcclusionValue;
    fs_framesPerSecond = vs_framesPerSecond;
    fs_frameAmount = int(vs_frameAmount);
    fs_fragmentPosition = vec3(vs_modelMatrix * vec4(vs_vertexPosition, 1.0));

    fs_lightValue = vs_lightValue.xyz / 16.0;
}