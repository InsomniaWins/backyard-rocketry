#version 330 core
out vec4 FragColor;

in vec2 fs_textureCoordinates;

uniform sampler2D fs_texture;

void main()
{

    vec4 color = texture(fs_texture, vec2(fs_textureCoordinates.x, fs_textureCoordinates.y));

    if (color.a == 0.0)
    {
        discard;
    }

    FragColor = color;
}