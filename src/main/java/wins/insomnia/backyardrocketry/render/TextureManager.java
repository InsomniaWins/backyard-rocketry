package wins.insomnia.backyardrocketry.render;

public class TextureManager {

    private final Texture FONT_TEXTURE;

    public TextureManager() {

        FONT_TEXTURE = new Texture("font.png");

    }

    public Texture getFontTexture() {
        return FONT_TEXTURE;
    }

    public void clean() {

        FONT_TEXTURE.clean();

    }

}
