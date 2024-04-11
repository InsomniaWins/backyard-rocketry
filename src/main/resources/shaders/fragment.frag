#version 330 core
out vec4 FragColor;

in vec2 fs_textureCoordinates;

uniform sampler2D fs_texture;

void main()
{
    FragColor = texture(fs_texture, vec2(fs_textureCoordinates.x, fs_textureCoordinates.y));
}