#version 330 core
out vec4 FragColor;

in vec2 fs_textureCoordinates;

uniform sampler2D fs_texture;

void main()
{
    FragColor = texture(fs_texture, fs_textureCoordinates);
}