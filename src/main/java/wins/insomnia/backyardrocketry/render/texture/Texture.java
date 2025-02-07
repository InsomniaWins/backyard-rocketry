package wins.insomnia.backyardrocketry.render.texture;

import de.matthiasmann.twl.utils.PNGDecoder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;

public class Texture {

    private String textureName;
    private int textureIndex;
    private boolean isClean;
    private int width;
    private int height;


    public Texture(ByteBuffer textureData, int width, int height) {

        textureIndex = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureIndex);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        //glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, textureData);

        // if mipmaps should be loaded
        //glGenerateMipmap(GL_TEXTURE_2D)

        isClean = false;

    }


    public Texture(String textureName) {
        this.textureName = textureName;

        PNGDecoder decoder = null;
        ByteBuffer buffer = null;

        try {
            InputStream inputStream = Texture.class.getResourceAsStream("/textures/" + textureName);

            if (inputStream == null) {
                System.err.println("Could not open texture: " + textureName);
                return;
            }

            decoder = new PNGDecoder(inputStream);

            buffer = ByteBuffer.allocateDirect(4 * decoder.getWidth() * decoder.getHeight());
            decoder.decode(buffer, decoder.getWidth() * 4, PNGDecoder.Format.RGBA);
            buffer.flip();

        } catch (IOException ioException) {

            System.err.println("Could not open texture: " + textureName);
            return;

        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        textureIndex = glGenTextures();

        glBindTexture(GL_TEXTURE_2D, textureIndex);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        //glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        //glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, decoder.getWidth(), decoder.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

        // if mipmaps should be loaded
        //glGenerateMipmap(GL_TEXTURE_2D);

        isClean = false;

        this.width = decoder.getWidth();
        this.height = decoder.getHeight();

    }

    public String getTextureName() {
        return textureName;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isClean() {
        return isClean;
    }

    public void clean() {

        isClean = true;
        glDeleteTextures(textureIndex);

    }

    public int getTextureHandle() {
        return textureIndex;
    }

}