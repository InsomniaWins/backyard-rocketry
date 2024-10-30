package wins.insomnia.backyardrocketry.render;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import wins.insomnia.backyardrocketry.util.OpenSimplex2;
import wins.insomnia.backyardrocketry.world.block.Block;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;


public class BlockModelData {

    private HashMap<String, String> textures = null;
    private HashMap<String, Object> faces = null;
    private String parent = null;


    private static final HashMap<Byte, HashMap<String, Object>> BLOCK_STATE_MODEL_MAP = new HashMap();
    private static final HashMap<String, BlockModelData> MODEL_MAP = new HashMap<>();
    private static final HashMap<Byte, Mesh> BLOCK_MESH_MAP = new HashMap<>();

    public static void init() {
        loadBlockModels();
        loadBlockStates();
        registerBlockMeshes();
    }

    public static void registerBlockMeshes() {

        for (Byte block : Block.getBlocks()) {
            registerBlockMesh(block);
        }



    }

    public static void clean() {
        cleanBlockMeshes();
    }

    public static BlockModelData getBlockModelFromBlock(byte block, int x, int y, int z) {

        /*
        int block = BitHelper.getBlockIdFromBlockState(blockState);
        BlockProperties blockProperties = Block.getBlockPropertiesFromBlockState(blockState);

        String blockModelName = blockProperties.getBlockModelName(blockState);

        Object model = BLOCK_STATE_MODEL_MAP.get(block).get(blockModelName);
        if (model == null) model = BLOCK_STATE_MODEL_MAP.get(block).get("default");
        */
        HashMap<String, Object> modelStates = BLOCK_STATE_MODEL_MAP.get(block);

        if (modelStates == null) return null;

        Object model = modelStates.get("default");
        if (model instanceof ArrayList modelList) {

            int randomPosNum = getRandomBlockNumberBasedOnBlockPosition(x, y, z);

            int randomIndex = randomPosNum % modelList.size();
            String modelName = (String) modelList.get(randomIndex);

            return MODEL_MAP.get(modelName);
        }

        return MODEL_MAP.get(
                (String) model
        );

    }

    public static BlockModelData getBlockModel(byte block, int blockX, int blockY, int blockZ) {

        String currentBlockStateName = "default";
        HashMap<String, Object> blockStateData = BLOCK_STATE_MODEL_MAP.get(block);

        if (blockStateData == null) return null;

        Object model = blockStateData.get(currentBlockStateName);

        if (model instanceof ArrayList modelList) {
            int randomIndex = getRandomBlockNumberBasedOnBlockPosition(blockX, blockY, blockZ) % modelList.size();
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



    private static void fixModelUvs(BlockModelData blockModelData) {

        int[] atlasCoordinates;

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

                        coordinateValue += TextureManager.BLOCK_SCALE_ON_ATLAS * atlasCoordinates[1];

                    }

                    faceVertexArray.set(i, coordinateValue);
                }

            }

            faceData.put("vertices", faceVertexArray);
        }

    }

    private static BlockModelData loadBlockModelData(ObjectMapper mapper, String modelName) throws IOException {

        // get path to model
        URL src = BlockModelData.class.getResource("/models/blocks/" + modelName + ".json");

        // if could not find file
        if (src == null) {

            throw new RuntimeException("Failed to load block model: " + modelName);

        }

        // load model
        BlockModelData blockModelData = mapper.readValue(src, BlockModelData.class);

        if (blockModelData.parent != null) {

            BlockModelData parentBlockModelData = loadBlockModelData(mapper, blockModelData.parent);


            if (blockModelData.faces != null) {

                parentBlockModelData.faces = blockModelData.faces;

            }


            if (blockModelData.textures != null) {

                parentBlockModelData.textures = blockModelData.textures;

            }


            blockModelData = parentBlockModelData;

        }

        return blockModelData;
    }

    private static void loadBlockModel(ObjectMapper mapper, String modelName) throws IOException {

        // if model already loaded, return
        if (MODEL_MAP.get(modelName) != null) return;

        // configure mapper
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // load block model
        BlockModelData blockModelData = loadBlockModelData(mapper, modelName);
        // fix Uvs
        fixModelUvs(blockModelData);

        // put in model map
        MODEL_MAP.put(modelName, blockModelData);

        // debug info
        System.out.println("Loaded block model: " + modelName);
    }

    private static void loadBlockState(ObjectMapper mapper, byte block, String blockStatePath) throws IOException {

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

            URI uri = BlockModelData.class.getResource("/models/blocks/").toURI();

            Path myPath;
            if (uri.getScheme().equals("jar")) {
                FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
                myPath = fileSystem.getPath("/models/blocks/");
                fileSystem.close();
            } else {
                myPath = Paths.get(uri);
            }

            Stream<Path> walk = Files.walk(myPath, 1);
            for (Iterator<Path> it = walk.iterator(); it.hasNext();){

                String fileName = it.next().getFileName().toString();

                if (fileName.equals("blocks")) continue;

                if (!fileName.endsWith(".json")) continue;

                fileName = fileName.replace(".json", "");
                loadBlockModel(mapper, fileName);

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}


	}

    public static void loadBlockStates() {

        ObjectMapper mapper = new ObjectMapper();

        try {

            for (byte block : Block.getBlocks()) {

                String blockStateFileName = Block.getBlockStateName(block);

                if (blockStateFileName == null || blockStateFileName.isEmpty()) continue;

                loadBlockState(mapper, block, blockStateFileName);
            }

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

    public void setParent(String parent) {
        this.parent = parent;
    }

    public static HashMap<Byte, HashMap<String, Object>> getBlockStateModelMap() {
        return BLOCK_STATE_MODEL_MAP;
    }

    public static Mesh getMeshFromBlock(byte block) {
        return BLOCK_MESH_MAP.get(block);
    }

    private static void unregisterBlockMesh(byte block) {
        Mesh blockMesh = BLOCK_MESH_MAP.get(block);
        if (blockMesh != null && !blockMesh.isClean()) {
            blockMesh.clean();
        }
        BLOCK_MESH_MAP.remove(blockMesh);
    }

    private static void cleanBlockMeshes() {

        Iterator<Map.Entry<Byte, Mesh>> iterator = BLOCK_MESH_MAP.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Byte, Mesh> entry = iterator.next();
            Mesh blockMesh = entry.getValue();

            iterator.remove();

            if (blockMesh != null && !blockMesh.isClean()) {
                blockMesh.clean();
            }
        }

    }

    private static void registerBlockMesh(byte block) {

        BlockModelData blockModelData = BlockModelData.getBlockModelFromBlock(block, 0, 0, 0);
        if (blockModelData == null) return;

        ArrayList<Float> vertexArray = new ArrayList<>();
        ArrayList<Integer> indexArray = new ArrayList<>();

        int indexOffset = 0;

        for (Map.Entry<String, ?> faceEntry : blockModelData.getFaces().entrySet()) {

            HashMap<String, ?> faceData = (HashMap<String, ?>) faceEntry.getValue();

            ArrayList<Double> faceVertexArray = (ArrayList<Double>) faceData.get("vertices");
            ArrayList<Integer> faceIndexArray = (ArrayList<Integer>) faceData.get("indices");

            for (Double vertexValue : faceVertexArray) {
                vertexArray.add(vertexValue.floatValue());
            }

            int greatestIndexValue = 0;
            for (Integer indexValue : faceIndexArray) {
                indexArray.add(indexValue + indexOffset);
                if (indexValue > greatestIndexValue) {
                    greatestIndexValue = indexValue;
                }
            }
            indexOffset += greatestIndexValue + 1;
        }

        float[] primitiveVertexArray = new float[vertexArray.size()];
        for (int i = 0; i < primitiveVertexArray.length; i++) {
            primitiveVertexArray[i] = vertexArray.get(i);
        }

        int[] primitiveIndexArray = new int[indexArray.size()];
        for (int i = 0; i < primitiveIndexArray.length; i++) {
            primitiveIndexArray[i] = indexArray.get(i);
        }

        BLOCK_MESH_MAP.put(block, new Mesh(primitiveVertexArray, primitiveIndexArray));
    }
}
