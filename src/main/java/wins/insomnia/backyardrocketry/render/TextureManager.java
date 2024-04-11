package wins.insomnia.backyardrocketry.render;

import org.lwjgl.stb.STBImage;
import wins.insomnia.backyardrocketry.BackyardRocketry;
import wins.insomnia.backyardrocketry.world.Block;

import java.net.URL;
import java.nio.ByteBuffer;

public class TextureManager {

    private final Texture FONT_TEXTURE;
    private final Texture BLOCK_ATLAS;
    public static final float BLOCK_SCALE_ON_ATLAS = 16f / 256f;


    public TextureManager() {

        FONT_TEXTURE = new Texture("font.png");
        BLOCK_ATLAS = new Texture("blockAtlas.png");

    }

    public Texture getFontTexture() {
        return FONT_TEXTURE;
    }

    public void clean() {

        FONT_TEXTURE.clean();

    }

    private Texture makeBlockAtlas() {

        String[] textureNames = {
                "blocks/cobblestone.png"
        };

        byte[][] textureAtlasData;



        for (int i = 0; i < textureNames.length; i++) {
            byte[] textureData = loadTextureData("blocks/cobblestone.png");


        }


        //Texture texture = new Texture(textureDataBuffer, 256, 256);




        //STBImage.stbi_image_free(textureDataBuffer);

        return null;
        //return texture;
    }

    public int[] getBlockAtlasCoordinates(String blockTextureName) {
        switch (blockTextureName) {
            case "cobblestone" -> {
                return new int[] {0, 0};
            }
            case "stone" -> {
                return new int[] {1, 0};
            }
            case "dirt" -> {
                return new int[] {2, 0};
            }
            case "grass_block_side" -> {
                return new int[] {3, 0};
            }
            case "grass_block_top" -> {
                return new int[] {4, 0};
            }
        }
        return new int[] {0, 0};
    }

    private static byte[] loadTextureData(String textureName) {
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

        byte[] textureData = new byte[buffer.remaining()];
        buffer.get(textureData);

        STBImage.stbi_image_free(buffer);

        return textureData;
    }

    public Texture getBlockAtlasTexture() {
        return BLOCK_ATLAS;
    }

    public static TextureManager get() {

        return BackyardRocketry.getInstance().getRenderer().getTextureManager();

    }

}
