package wins.insomnia.backyardrocketry.render.texture;

import de.matthiasmann.twl.utils.PNGDecoder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_MAX_LEVEL;
import static org.lwjgl.opengl.GL12.GL_TEXTURE_MAX_LOD;

public class Texture {

    private String textureName;
    private int textureIndex;
    private boolean isClean;
    private int width;
    private int height;


    public Texture(ByteBuffer textureData, int width, int height) {

        init(textureData, width, height);

    }

    public Texture(String textureName, boolean repeat) {
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

        init(buffer, decoder.getWidth(), decoder.getHeight(), repeat);
    }

    public Texture(String textureName) {
        this(textureName, true);
    }

    private void init(ByteBuffer textureData, int textureWidth, int textureHeight, boolean repeat) {

        textureIndex = glGenTextures();

        glBindTexture(GL_TEXTURE_2D, textureIndex);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);


        if (repeat) {
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        } else {
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);
        }

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LOD, 0);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);


        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, textureWidth, textureHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, textureData);

        isClean = false;

        this.width = textureWidth;
        this.height = textureHeight;

    }

    private void init(ByteBuffer textureData, int textureWidth, int textureHeight) {

        init(textureData, textureWidth, textureHeight, true);

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