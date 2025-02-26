#version 330 core
in vec2 fs_textureCoordinates;
out vec4 color;

uniform vec3 fs_colorModulation;
uniform float fs_alpha = 1.0;
uniform sampler2D fs_texture;

void main()
{
    color = texture(fs_texture, fs_textureCoordinates);
    color.rgb = color.rgb * fs_colorModulation;

    color.a *= fs_alpha;
}