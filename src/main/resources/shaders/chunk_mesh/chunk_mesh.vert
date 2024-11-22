#version 330 core
layout (location = 0) in vec3 vs_vertexPosition;
layout (location = 1) in vec2 vs_textureCoordinates;
layout (location = 2) in vec2 vs_localBlockTextureUV;

out vec2 fs_textureCoordinates;
out float fs_atlasBlockScale;
out vec2 fs_localBlockTextureUV;
out float fs_ao_value;

uniform float vs_atlasBlockScale;
uniform mat4 vs_modelMatrix;
uniform mat4 vs_viewMatrix;
uniform mat4 vs_projectionMatrix;
uniform vec2 vs_uvOffset;


float vertex_ao(int side_1, int side_2, float corner)
{
    if (side_1 != 0 && side_2 != 0)
    {
        return 0.0;
    }

    return 3.0 - (side_1 + side_2 + corner);
}

void main()
{
    gl_Position = vs_projectionMatrix * vs_viewMatrix * vs_modelMatrix * vec4(vs_vertexPosition, 1.0);
    fs_textureCoordinates = vs_textureCoordinates;
    fs_atlasBlockScale = vs_atlasBlockScale;
    fs_localBlockTextureUV = vs_localBlockTextureUV;
    fs_ao_value = 3.0;
}