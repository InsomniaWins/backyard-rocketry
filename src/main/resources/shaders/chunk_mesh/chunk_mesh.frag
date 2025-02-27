#version 330 core

out vec4 FragColor;

in vec2 fs_textureCoordinates;
in vec3 fs_normal;
in vec4 fs_eyeSpacePosition;
in float fs_ambientOcclusionValue;
in float fs_framesPerSecond;
in vec3 fs_fragmentPosition;
flat in int fs_frameAmount;

uniform vec3 fs_viewPosition;
uniform sampler2D fs_texture;
uniform sampler2D fs_heightMap;
uniform float fs_time;

// fog
uniform vec3 fs_fogColor;
uniform bool fs_fogEnabled = true;
uniform float fs_fogStart = 0.0;
uniform float fs_fogEnd = 1.0;

// lighting
vec3 lightDirection = normalize(vec3(-0.7, -0.8, -0.45));
vec3 lightColor = vec3(1.0, 1.0, 1.0);
float ambientLightStrength = 0.7;

void main() {

    float sizeOfBlockOnAtlas = 18.0 / 512.0;

    vec2 texCoord = vec2(fs_textureCoordinates.x, fs_textureCoordinates.y);
    int frameIndex = (int(fs_time * fs_framesPerSecond) % fs_frameAmount);
    int nextFrameIndex = (frameIndex + 1) % fs_frameAmount;

    vec2 currentFrameTexCoord = vec2(texCoord.x, texCoord.y);

    for (int i = 0; i < int(frameIndex); i++) {
        currentFrameTexCoord.x += sizeOfBlockOnAtlas;

        if (currentFrameTexCoord.x > 1.0 - sizeOfBlockOnAtlas) {
            currentFrameTexCoord.x = 0.0;
            currentFrameTexCoord.y += sizeOfBlockOnAtlas;
        }
    }

    vec2 nextFrameTexCoord = vec2(texCoord.x, texCoord.y);

    for (int i = 0; i < int(nextFrameIndex); i++) {
        nextFrameTexCoord.x += sizeOfBlockOnAtlas;

        if (nextFrameTexCoord.x > 1.0 - sizeOfBlockOnAtlas) {
            nextFrameTexCoord.x = 0.0;
            nextFrameTexCoord.y += sizeOfBlockOnAtlas;
        }
    }
    vec4 currentFrameFragmentColor = texture(fs_texture, vec2(currentFrameTexCoord.x, currentFrameTexCoord.y));;
    vec4 nextFrameFragmentColor = texture(fs_texture, vec2(nextFrameTexCoord.x, nextFrameTexCoord.y));

    vec4 fragmentColor = mix(currentFrameFragmentColor, nextFrameFragmentColor, mod(fs_time * fs_framesPerSecond, 1));


    // transparency
    if (fragmentColor.a == 0.0) {
        discard;
    }

    //specular lighting
    vec3 viewDirection = normalize(fs_viewPosition - fs_fragmentPosition);
    vec3 reflectDirection = reflect(lightDirection, fs_normal);
    float specular = pow(max(dot(viewDirection, reflectDirection), 0.0), 4);
    vec3 specularLighting = (texture(fs_heightMap, currentFrameTexCoord).r) * specular * lightColor;




    if (fs_fogEnabled) {

        // fog calculations
        float fogCoordinate = abs(fs_eyeSpacePosition.z / fs_eyeSpacePosition.w);

        float fogLength = fs_fogEnd - fs_fogStart;
        float fogFactor = (fs_fogEnd - fogCoordinate) / fogLength;
        fogFactor = 1.0 - clamp(fogFactor, 0.0, 1.0);



        if (fogFactor < 1.0) {

            // directional lighting
            vec3 ambientLighting = ambientLightStrength * lightColor;

            fragmentColor.rgb = fragmentColor.rgb * ambientLighting;

            vec3 diffuseLighting = lightColor * (max(dot(-fs_normal, lightDirection), 0.0));

            fragmentColor.rgb = (ambientLighting + diffuseLighting + specularLighting) * fragmentColor.rgb;

            // ambient occlusion
            fragmentColor.rgb = fragmentColor.rgb * fs_ambientOcclusionValue;

            // apply fog
            fragmentColor = mix(fragmentColor, vec4(fs_fogColor, 1.0), fogFactor);

        } else {

            fragmentColor.rgb = fs_fogColor;

        }

    } else {

        // directional lighting
        vec3 ambientLighting = ambientLightStrength * lightColor;

        fragmentColor.rgb = fragmentColor.rgb * ambientLighting;

        vec3 diffuseLighting = lightColor * (max(dot(normalize(fs_normal), lightDirection), 0.0));

        fragmentColor.rgb = (ambientLighting + diffuseLighting + specularLighting) * fragmentColor.rgb;

        // ambient occlusion
        fragmentColor.rgb = fragmentColor.rgb * fs_ambientOcclusionValue;

    }





    FragColor = fragmentColor;
}