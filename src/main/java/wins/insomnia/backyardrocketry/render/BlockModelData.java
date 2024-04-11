package wins.insomnia.backyardrocketry.render;

import com.fasterxml.jackson.databind.ObjectMapper;
import wins.insomnia.backyardrocketry.world.Block;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class BlockModelData {

    private HashMap<String, String> textures;
    private HashMap<String, Object> faces;


    private static final HashMap<Integer, BlockModelData> BLOCK_MODEL_MAP = new HashMap<>();
    public static BlockModelData getBlockModel(int block) {
        return BLOCK_MODEL_MAP.get(block);
    }


    private static void fixModelUvs(BlockModelData blockModelData) {

        int[] atlasCoordinates = new int[2];

        for (Map.Entry<String, Object> faceEntry : blockModelData.getFaces().entrySet()) {
            HashMap<String, Object> faceData = (HashMap<String, Object>) faceEntry.getValue();
            ArrayList<Double> faceVertexArray = (ArrayList<Double>) faceData.get("vertices");

            String faceTextureName = blockModelData.textures.get((String) faceData.get("texture"));
            atlasCoordinates = TextureManager.get().getBlockAtlasCoordinates(faceTextureName);


            // modify uv coordinates to fit texture atlas
            for (int i = 0; i < faceVertexArray.size(); i++) {

                int vertexDataIndex = i % 5;
                if (vertexDataIndex == 3 || vertexDataIndex == 4) {

                    double coordinateValue = faceVertexArray.get(i);

                    coordinateValue *= TextureManager.BLOCK_SCALE_ON_ATLAS;

                    if (vertexDataIndex == 3) {

                        coordinateValue += TextureManager.BLOCK_SCALE_ON_ATLAS * atlasCoordinates[0];

                    } else {

                        coordinateValue -= TextureManager.BLOCK_SCALE_ON_ATLAS * atlasCoordinates[1] + TextureManager.BLOCK_SCALE_ON_ATLAS;

                    }

                    faceVertexArray.set(i, coordinateValue);
                }

            }

            faceData.put("vertices", faceVertexArray);
        }

    }

    private static void loadBlockModel(ObjectMapper mapper, int block, String modelPath) throws IOException {

        URL src = BlockModelData.class.getResource(modelPath);

        if (src == null) {

            throw new RuntimeException("Failed to load block model: " + modelPath);

        }

        BlockModelData blockModelData = mapper.readValue(src, BlockModelData.class);
        fixModelUvs(blockModelData);
        BLOCK_MODEL_MAP.put(block, blockModelData);

    }

    public static void loadBlockModels() {

        ObjectMapper mapper = new ObjectMapper();

        try {
            loadBlockModel(mapper, Block.GRASS, "/models/blocks/grass_block.json");
            loadBlockModel(mapper, Block.COBBLESTONE, "/models/blocks/cobblestone.json");
        } catch (IOException e) {
            e.printStackTrace();
        }


    }




    public HashMap<String, String> getTextures() {
        return textures;
    }

    public HashMap<String, Object> getFaces() {
        return faces;
    }

    public void setTextures(HashMap<String, String> textures) {
        this.textures = textures;
    }

    public void setFaces(HashMap<String, Object> faces) {
        this.faces = faces;
    }

}
