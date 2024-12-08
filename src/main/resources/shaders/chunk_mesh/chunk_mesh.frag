#version 330 core
out vec4 FragColor;

in vec2 fs_textureCoordinates;
in vec3 fs_normal;

uniform sampler2D fs_texture;

// transparency
float alphaThreshold = 0.25;

// lighting
vec3 lightDirection = normalize(-vec3(0.5, -0.9, 0.5));
vec3 lightColor = vec3(1.0, 1.0, 1.0);
float ambientLightStrength = 0.7;

void main() {

    vec4 fragmentColor = texture(fs_texture, vec2(fs_textureCoordinates.x, fs_textureCoordinates.y));

    // transparency
    if (fragmentColor.a < alphaThreshold) {
        discard;
    }

    // lighting
    vec3 ambientLighting = ambientLightStrength * lightColor;

    fragmentColor.rgb = fragmentColor.rgb * ambientLighting;

    vec3 diffuseLighting = lightColor * (max(dot(normalize(fs_normal), lightDirection), 0.0));

    fragmentColor.rgb = (ambientLighting + diffuseLighting) * fragmentColor.rgb;

    FragColor = fragmentColor;
}