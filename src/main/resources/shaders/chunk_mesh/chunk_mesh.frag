#version 330 core
out vec4 FragColor;

in vec2 fs_textureCoordinates;
in float fs_ao_value;
in vec2 fs_localBlockTextureUV;

uniform sampler2D fs_texture;

float alphaThreshold = 0.25;

vec3 get_ao_color(float value)
{
    if (value == 0.0)
    {
        return vec3(1.0, 0.0, 0.0);
    }
    else if (value == 1.0)
    {
        return vec3(0.0, 1.0, 0.0);
    }
    else if (value == 2.0)
    {
        return vec3(0.0, 0.0, 1.0);
    }
    else
    {
        return vec3(1.0, 1.0, 1.0);
    }
}

void main()
{

    vec4 color = texture(fs_texture, vec2(fs_textureCoordinates.x, fs_textureCoordinates.y));

    if (color.a < alphaThreshold)
    {
        discard;
    }

    vec3 ao_color = get_ao_color(fs_ao_value);
    color.rgb = mix(color.rgb, vec3(0.5, 0.5, 0.5), 0.3 * ao_color * distance(fs_localBlockTextureUV, vec2(0.5)));

    FragColor = color;
}