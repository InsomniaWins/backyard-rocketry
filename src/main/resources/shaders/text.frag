#version 330 core
in vec2 fs_textureCoordinates;
out vec4 color;

uniform sampler2D fs_texture;
uniform vec3 textColor;

void main()
{
    vec4 sampled = vec4(1.0, 1.0, 1.0, texture(fs_texture, fs_textureCoordinates).r);
    color = vec4(textColor, 1.0) * sampled;
    color = sampled;
}