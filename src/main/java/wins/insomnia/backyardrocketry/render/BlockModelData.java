package wins.insomnia.backyardrocketry.render;

import com.fasterxml.jackson.databind.ObjectMapper;
import wins.insomnia.backyardrocketry.world.Block;
import wins.insomnia.backyardrocketry.world.BlockState;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class BlockModelData {

    private HashMap<String, String> textures;
    private HashMap<String, Object> faces;


    private static final HashMap<Integer, HashMap<String, String>> BLOCK_STATE_MODEL_MAP = new HashMap();
    private static final HashMap<String, BlockModelData> MODEL_MAP = new HashMap<>();
    public static BlockModelData getBlockModel(int block) {

        String currentBlockStateName = "default";
        String modelName = BLOCK_STATE_MODEL_MAP.get(block).get(currentBlockStateName);

        return MODEL_MAP.get(
                modelName
        );
    }

    public static BlockModelData getBlockModelFromBlockState(BlockState blockState) {

        return MODEL_MAP.get(BLOCK_STATE_MODEL_MAP.get(blockState.getBlock()).get(blockState.getStateString()));

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

    private static void loadBlockModel(ObjectMapper mapper, String modelName) throws IOException {

        URL src = BlockModelData.class.getResource("/models/blocks/" + modelName + ".json");

        if (src == null) {

            throw new RuntimeException("Failed to load block model: " + modelName);

        }

        BlockModelData blockModelData = mapper.readValue(src, BlockModelData.class);
        fixModelUvs(blockModelData);
        MODEL_MAP.put(modelName, blockModelData);

        System.out.println("Loaded block model: " + modelName);
    }

    private static void loadBlockState(ObjectMapper mapper, int block, String blockStatePath) throws IOException {

        URL src = BlockModelData.class.getResource("/blockstates/" + blockStatePath + ".json");

        if (src == null) {

            throw new RuntimeException("Failed to load blockstate file: " + blockStatePath);

        }

        HashMap<Object, Object> blockStateData = mapper.readValue(src, HashMap.class);
        HashMap<String, String> blockStateModelMap = new HashMap<>();
        for (Map.Entry<Object, Object> entry : blockStateData.entrySet()) {

            String stateName = (String) entry.getKey();
            String modelName = (String) entry.getValue();

            blockStateModelMap.put(stateName, modelName);

        }

        BLOCK_STATE_MODEL_MAP.put(block, blockStateModelMap);
        System.out.println("Loaded blockstate: " + blockStatePath);

    }

    public static void loadBlockModels() {

        ObjectMapper mapper = new ObjectMapper();

        try {
            loadBlockModel(mapper, "grass_block");
            loadBlockModel(mapper, "grass_block_deep");
            loadBlockModel(mapper, "cobblestone");
            loadBlockModel(mapper, "dirt");
            loadBlockModel(mapper, "stone");
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public static void loadBlockStates() {

        ObjectMapper mapper = new ObjectMapper();

        try {
            loadBlockState(mapper, Block.GRASS, "grass_block");
            loadBlockState(mapper, Block.COBBLESTONE, "cobblestone");
            loadBlockState(mapper, Block.DIRT, "dirt");
            loadBlockState(mapper, Block.STONE, "stone");
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
