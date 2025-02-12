package wins.insomnia.backyardrocketry.render.texture;

import org.lwjgl.stb.STBImage;
import wins.insomnia.backyardrocketry.BackyardRocketry;

import java.net.URL;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class TextureManager {

    private static final HashMap<String, Texture> TEXTURE_HASH_MAP = new HashMap<>();


    public TextureManager() {
        registerTextures();
    }

    private static void registerTextures() {

        registerTexture("block_atlas", new BlockAtlasTexture());

        registerTexture("placeholder_inventory", "gui/inventory/placeholder_inventory.png");
        registerTexture("font", "font.png");
        registerTexture("debug_font", "debug_font.png");
        registerTexture("block_outline", "block_outline.png");
        registerTexture("crosshair", "gui/crosshair.png");
        registerTexture("hotbar", "gui/hotbar.png");
        registerTexture("selected_hotbar_slot", "gui/selected_hotbar_slot.png");
        registerTexture("break_progress_bar_under", "gui/break_progress_bar_under.png");
        registerTexture("break_progress_bar_progress", "gui/break_progress_bar_progress.png");
        registerTexture("waila", "gui/waila.png");
        registerTexture("text_button", "gui/buttons/text_button.png");
        registerTexture("text_button_hovered", "gui/buttons/text_button_hovered.png");
        registerTexture("text_button_pressed", "gui/buttons/text_button_pressed.png");
        registerTexture("menu_background", "gui/background.png");
        registerTexture("dropped_item", "gui/notification_icons/dropped_item.png");
        registerTexture("line_edit", "gui/line_edits/line_edit.png");
        registerTexture("line_edit_hovered", "gui/line_edits/line_edit_hovered.png");
        registerTexture("line_edit_pressed", "gui/line_edits/line_edit_pressed.png");
        registerTexture("world_preview_crosshair", "world_preview_crosshair.png");
        registerTexture("buffering", "gui/buffering.png");
    }

    public static Texture getTexture(String textureId) {
        return TEXTURE_HASH_MAP.get(textureId);
    }

    public static Texture registerTexture(String textureFilePath) {
        return registerTexture(textureFilePath, textureFilePath);
    }

    public static void unregisterTexture(String textureId) {
        Texture texture = TEXTURE_HASH_MAP.get(textureId);

        if (texture != null && !texture.isClean()) {
            texture.clean();
        }

        TEXTURE_HASH_MAP.remove(textureId);
    }


    public static Texture registerTexture(String textureId, String textureFilePath) {
        Texture texture = new Texture(textureFilePath);
        TEXTURE_HASH_MAP.put(textureId, texture);
        return texture;
    }

    public static Texture registerTexture(String textureId, Texture texture) {
        TEXTURE_HASH_MAP.put(textureId, texture);
        return texture;
    }

    public static void clean() {
        for (Map.Entry<String, Texture> textureEntry : TEXTURE_HASH_MAP.entrySet()) {
            textureEntry.getValue().clean();
        }
        TEXTURE_HASH_MAP.clear();
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

    public static TextureManager get() {

        return BackyardRocketry.getInstance().getRenderer().getTextureManager();

    }

}
