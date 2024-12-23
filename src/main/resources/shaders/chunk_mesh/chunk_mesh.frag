#version 330 core

out vec4 FragColor;

in vec2 fs_textureCoordinates;
in vec3 fs_normal;
in vec4 fs_eyeSpacePosition;
in float fs_ambientOcclusionValue;

uniform sampler2D fs_texture;
uniform vec3 fs_fogColor;
uniform bool fs_fogEnabled = true;

// transparency
float alphaThreshold = 0.25;

// lighting
vec3 lightDirection = normalize(-vec3(0.7, -0.9, 0.45));
vec3 lightColor = vec3(1.0, 1.0, 1.0);
float ambientLightStrength = 0.7;

void main() {

    vec4 fragmentColor = texture(fs_texture, vec2(fs_textureCoordinates.x, fs_textureCoordinates.y));

    // transparency
    if (fragmentColor.a < alphaThreshold) {
        discard;
    }


    if (fs_fogEnabled) {

        // fog calculations
        float fogCoordinate = abs(fs_eyeSpacePosition.z / fs_eyeSpacePosition.w);

        float linearEnd = 200.0;
        float linearStart = 0.0;
        float fogLength = linearEnd - linearStart;
        float fogFactor = (linearEnd - fogCoordinate) / fogLength;
        fogFactor = 1.0 - clamp(fogFactor, 0.0, 1.0);

        if (fogFactor < 1.0) {

            // lighting
            vec3 ambientLighting = ambientLightStrength * lightColor;

            fragmentColor.rgb = fragmentColor.rgb * ambientLighting;

            vec3 diffuseLighting = lightColor * (max(dot(normalize(fs_normal), lightDirection), 0.0));

            fragmentColor.rgb = (ambientLighting + diffuseLighting) * fragmentColor.rgb;

            // ambient occlusion
            fragmentColor.rgb = fragmentColor.rgb * fs_ambientOcclusionValue;

            // apply fog
            fragmentColor = mix(fragmentColor, vec4(fs_fogColor, 1.0), fogFactor);

        } else {

            fragmentColor.rgb = fs_fogColor;

        }

    } else {

        // lighting
        vec3 ambientLighting = ambientLightStrength * lightColor;

        fragmentColor.rgb = fragmentColor.rgb * ambientLighting;

        vec3 diffuseLighting = lightColor * (max(dot(normalize(fs_normal), lightDirection), 0.0));

        fragmentColor.rgb = (ambientLighting + diffuseLighting) * fragmentColor.rgb;

        // ambient occlusion
        fragmentColor.rgb = fragmentColor.rgb * fs_ambientOcclusionValue;

    }





    FragColor = fragmentColor;
}