package wins.insomnia.backyardrocketry.render;

import com.fasterxml.jackson.databind.ObjectMapper;
import wins.insomnia.backyardrocketry.util.BitHelper;
import wins.insomnia.backyardrocketry.util.OpenSimplex2;
import wins.insomnia.backyardrocketry.world.Block;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class BlockModelData {

    private HashMap<String, String> textures;
    private HashMap<String, Object> faces;


    private static final HashMap<Integer, HashMap<String, Object>> BLOCK_STATE_MODEL_MAP = new HashMap();
    private static final HashMap<String, BlockModelData> MODEL_MAP = new HashMap<>();

    private static final Mesh TARGET_BLOCK_OUTLINE_MESH = new Mesh(
            new float[] {
                    1.02f, 1.02f, 1.02f, 1.0f, 0.0f,
                    1.02f, 1.02f, -0.02f, 1.0f, 1.0f,
                    1.02f, -0.02f, 1.02f, 1.0f, 0.0f,
                    1.02f, -0.02f, -0.02f, 1.0f, 1.0f,
                    -0.02f, 1.02f, 1.02f, 1.0f, 0.0f,
                    -0.02f, 1.02f, -0.02f, 1.0f, 1.0f,
                    -0.02f, -0.02f, 1.02f, 1.0f, 0.0f,
                    -0.02f, -0.02f, -0.02f, 1.0f, 1.0f
            },
            new int[] {
                    4, 0,
                    4, 5,
                    5, 1,
                    0, 1,

                    6, 2,
                    6, 7,
                    7, 3,
                    2, 3,

                    4, 6,

                    0, 2,

                    1, 3,

                    5, 7

            }
    );

    public static Mesh getTargetBlockOutlineMesh() {
        return TARGET_BLOCK_OUTLINE_MESH;
    }

    public static void clean() {
        TARGET_BLOCK_OUTLINE_MESH.clean();
    }

    public static BlockModelData getBlockModel(int block, int blockX, int blockY, int blockZ) {

        String currentBlockStateName = "default";
        Object model = BLOCK_STATE_MODEL_MAP.get(block).get(currentBlockStateName);

        if (model instanceof ArrayList modelList) {
            int randomIndex = getRandomBlockNumberBasedOnBlockPosition(blockX, blockY, blockZ) % modelList.size();
            String modelName = (String) modelList.get(randomIndex);
            return MODEL_MAP.get(modelName);
        }

        return MODEL_MAP.get(
                (String) model
        );
    }

    public static BlockModelData getBlockModel(int block) {
        return getBlockModel(block, 0);
    }

    public static BlockModelData getBlockModel(int block, int blockModelIndex) {

        String currentBlockStateName = "default";
        Object model = BLOCK_STATE_MODEL_MAP.get(block).get(currentBlockStateName);

        if (model instanceof ArrayList modelList) {
            int randomIndex = blockModelIndex % modelList.size();
            String modelName = (String) modelList.get(randomIndex);
            return MODEL_MAP.get(modelName);
        }

        return MODEL_MAP.get(
                (String) model
        );
    }

    public static int getRandomBlockNumberBasedOnBlockPosition(int x, int y, int z) {
        return (int) (3f * (OpenSimplex2.noise3_ImproveXZ(1, x, y, z) * 0.5f + 1f));
    }

    public static BlockModelData getBlockModelFromBlockState(int blockState) {
        return getBlockModel(BitHelper.getBlockIdFromBlockState(blockState), 0);
        //getRandomBlockNumberBasedOnBlockPosition(blockState.getX(), blockState.getY(), blockState.getZ())
    }

    public static BlockModelData getBlockModelFromBlock(int block, int variant) {
        return getBlockModel(block, variant);
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
        HashMap<String, Object> blockStateModelMap = new HashMap<>();
        for (Map.Entry<Object, Object> entry : blockStateData.entrySet()) {

            String stateName = (String) entry.getKey();
            if (entry.getValue() instanceof String) {
                String modelName = (String) entry.getValue();
                blockStateModelMap.put(stateName, modelName);
            } else if (entry.getValue() instanceof ArrayList) {

                blockStateModelMap.put(stateName, entry.getValue());

            } else {
                throw new RuntimeException("Failed to load blockstate of " + blockStatePath + " with name of " + stateName);
            }



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
