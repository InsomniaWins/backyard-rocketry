#version 330 core
in vec2 fs_textureCoordinates;
out vec4 color;

uniform sampler2D fs_texture;

void main()
{
    color = texture(fs_texture, fs_textureCoordinates);
}