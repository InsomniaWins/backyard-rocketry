package wins.insomnia.backyardrocketry.render;

import wins.insomnia.backyardrocketry.BackyardRocketry;

public class TextureManager {

    private final Texture FONT_TEXTURE;
    private final Texture BLOCK_ATLAS;


    public TextureManager() {

        FONT_TEXTURE = new Texture("font.png");
        BLOCK_ATLAS = makeBlockAtlas();

    }

    public Texture getFontTexture() {
        return FONT_TEXTURE;
    }

    public void clean() {

        FONT_TEXTURE.clean();

    }

    private Texture makeBlockAtlas() {

        Texture texture = new Texture();



        return texture;
    }

    public static TextureManager get() {

        return BackyardRocketry.getInstance().getRenderer().getTextureManager();

    }

}
