package wins.insomnia.backyardrocketry.render;

import org.lwjgl.stb.STBImage;
import wins.insomnia.backyardrocketry.BackyardRocketry;

import java.net.URL;
import java.nio.ByteBuffer;
import java.util.HashMap;

public class TextureManager {
    public final Texture FONT_TEXTURE;
    public final Texture DEBUG_FONT_TEXTURE;
    public final Texture BLOCK_ATLAS;
    public final Texture BLOCK_OUTLINE_TEXTURE;
    public final Texture CROSSHAIR_TEXTURE;
    public final Texture HOTBAR_TEXTURE;
    public final Texture HOTBAR_SLOT_TEXTURE;
    public final Texture BREAK_PROGRESS_BAR_UNDER_TEXTURE;
    public final Texture BREAK_PROGRESS_BAR_PROGRESS_TEXTURE;
    public final Texture WAILA_BACKGROUND_TEXTURE;

    public static final float BLOCK_SCALE_ON_ATLAS = 16f / 256f;


    public TextureManager() {

        FONT_TEXTURE = new Texture("font.png");
        DEBUG_FONT_TEXTURE = new Texture("debug_font.png");
        BLOCK_ATLAS = new Texture("block_atlas.png");
        BLOCK_OUTLINE_TEXTURE = new Texture("block_outline.png");
        CROSSHAIR_TEXTURE = new Texture("gui/crosshair.png");
        HOTBAR_TEXTURE = new Texture("gui/hotbar.png");
        HOTBAR_SLOT_TEXTURE = new Texture("gui/selected_hotbar_slot.png");
        BREAK_PROGRESS_BAR_UNDER_TEXTURE = new Texture("gui/break_progress_bar_under.png");
        BREAK_PROGRESS_BAR_PROGRESS_TEXTURE = new Texture("gui/break_progress_bar_progress.png");
        WAILA_BACKGROUND_TEXTURE = new Texture("gui/waila.png");
    }

    public Texture getBlockOutlineTexture() {
        return BLOCK_OUTLINE_TEXTURE;
    }

    public Texture getFontTexture() {
        return FONT_TEXTURE;
    }

    public Texture getCrosshairTexture() {
        return CROSSHAIR_TEXTURE;
    }

    public Texture getDebugFontTexture() {
        return DEBUG_FONT_TEXTURE;
    }

    public void clean() {

        BREAK_PROGRESS_BAR_PROGRESS_TEXTURE.clean();
        BREAK_PROGRESS_BAR_UNDER_TEXTURE.clean();
        FONT_TEXTURE.clean();
        DEBUG_FONT_TEXTURE.clean();
        CROSSHAIR_TEXTURE.clean();
        HOTBAR_TEXTURE.clean();
        HOTBAR_SLOT_TEXTURE.clean();
        WAILA_BACKGROUND_TEXTURE.clean();
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
            case "grass_block_deep_side" -> {
                return new int[] {5, 0};
            }
            case "log_top" -> {
                return new int[] {0, 1};
            }
            case "log_side" -> {
                return new int[] {1, 1};
            }
            case "leaves" -> {
                return new int[] {2, 1};
            }
            case "wooden_planks" -> {
                return new int[] {3, 1};
            }
            case "glass" -> {
                return new int[] {4, 1};
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
