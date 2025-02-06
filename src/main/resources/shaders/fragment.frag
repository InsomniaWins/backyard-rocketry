#version 330 core
out vec4 FragColor;

in vec2 fs_textureCoordinates;
in vec3 fs_normal;

uniform sampler2D fs_texture;
uniform bool fs_lightingEnabled = true;
uniform vec4 fs_color = vec4(1.0, 1.0, 1.0, 1.0);

// lighting
vec3 lightDirection = -normalize(vec3(-0.7, -0.9, -0.45));
vec3 lightColor = vec3(1.0, 1.0, 1.0);
float ambientLightStrength = 0.7;

void main() {
    vec4 fragmentColor = texture(fs_texture, vec2(fs_textureCoordinates.x, fs_textureCoordinates.y));

    // transparency
    if (fragmentColor.a == 0.0) {
        discard;
    }

    fragmentColor = fragmentColor * fs_color;

    if (fs_lightingEnabled) {
        // lighting
        vec3 ambientLighting = ambientLightStrength * lightColor;

        fragmentColor.rgb = fragmentColor.rgb * ambientLighting;

        vec3 diffuseLighting = lightColor * (max(dot(normalize(fs_normal), lightDirection), 0.0));

        fragmentColor.rgb = (ambientLighting + diffuseLighting) * fragmentColor.rgb;
    }

    FragColor = fragmentColor;
}