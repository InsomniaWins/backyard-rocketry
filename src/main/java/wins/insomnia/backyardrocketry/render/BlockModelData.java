package wins.insomnia.backyardrocketry.render;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.w3c.dom.Text;
import wins.insomnia.backyardrocketry.world.Block;

import java.io.File;
import java.io.IOException;
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


    public static void loadBlockModels() {

        ObjectMapper mapper = new ObjectMapper();

        try {
            BlockModelData cobblestoneModel = mapper.readValue(BlockModelData.class.getResource("/models/blocks/cobblestone.json"), BlockModelData.class);


            int[] atlasCoordinates = new int[2];

            for (Map.Entry<String, Object> faceEntry : cobblestoneModel.getFaces().entrySet()) {
                HashMap<String, Object> faceData = (HashMap<String, Object>) faceEntry.getValue();
                ArrayList<Double> faceVertexArray = (ArrayList<Double>) faceData.get("vertices");

                String faceTextureName = cobblestoneModel.textures.get((String) faceData.get("texture"));
                atlasCoordinates = TextureManager.get().getBlockAtlasCoordinates(faceTextureName);

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

            BLOCK_MODEL_MAP.put(Block.GRASS, cobblestoneModel);



        } catch (IOException e) {

            throw new RuntimeException(e);

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
