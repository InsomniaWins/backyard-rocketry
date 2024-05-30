#version 330 core
out vec4 FragColor;

in vec2 fs_textureCoordinates;

uniform sampler2D fs_texture;


float alphaThreshold = 0.25;

void main()
{

    vec4 color = texture(fs_texture, vec2(fs_textureCoordinates.x, fs_textureCoordinates.y));

    if (color.a < alphaThreshold)
    {
        discard;
    }

    FragColor = color;
}