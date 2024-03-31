package wins.insomnia.backyardrocketry.render;

import org.lwjgl.stb.STBImage;
import java.net.URL;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;

public class Texture {

    private int textureIndex;

    public Texture(String textureName) {

        URL url = Texture.class.getResource("/textures/" + textureName);
        if (url == null) {
            throw new RuntimeException("Could not locate texture resource: resources/textures/" + textureName);
        }

        String filePath = url.getPath();

        ByteBuffer buffer;

        int[] width = new int[1];
        int[] height = new int[1];
        int[] channels = new int[1];


        // pass file path without the first character '/' because this doesn't work with '/' as the first char?????
        STBImage.stbi_set_flip_vertically_on_load(true);
        buffer = STBImage.stbi_load(filePath.substring(1), width, height, channels, 4);
        if (buffer == null) {
            throw new RuntimeException("Cannot load texture: \"" + textureName + "\" " + STBImage.stbi_failure_reason());
        }

        textureIndex = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureIndex);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        //glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width[0], height[0], 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

        // if mipmaps should be loaded
            //glGenerateMipmap(GL_TEXTURE_2D)

        STBImage.stbi_image_free(buffer);

    }

    public void clean() {

        glDeleteTextures(textureIndex);

    }

    public int getTextureHandle() {
        return textureIndex;
    }

}
