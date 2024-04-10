package wins.insomnia.backyardrocketry.render;

import com.fasterxml.jackson.databind.ObjectMapper;
import wins.insomnia.backyardrocketry.world.Block;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


public class BlockModelData {

    private HashMap<String, String> textures;
    private HashMap<String, ?> faces;


    private static final HashMap<Integer, BlockModelData> BLOCK_MODEL_MAP = new HashMap<>();
    public static BlockModelData getBlockModel(int block) {
        return BLOCK_MODEL_MAP.get(block);
    }


    public static void loadBlockModels() {

        ObjectMapper mapper = new ObjectMapper();

        try {
            BlockModelData cobblestoneModel = mapper.readValue(BlockModelData.class.getResource("/models/blocks/cobblestone.json"), BlockModelData.class);
            BLOCK_MODEL_MAP.put(Block.GRASS, cobblestoneModel);



        } catch (IOException e) {

            throw new RuntimeException(e);

        }


    }




    public HashMap<String, String> getTextures() {
        return textures;
    }

    public HashMap<String, ?> getFaces() {
        return faces;
    }

    public void setTextures(HashMap<String, String> textures) {
        this.textures = textures;
    }

    public void setFaces(HashMap<String, ?> faces) {
        this.faces = faces;
    }

}
