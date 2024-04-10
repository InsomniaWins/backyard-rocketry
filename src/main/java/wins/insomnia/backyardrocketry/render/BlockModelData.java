package wins.insomnia.backyardrocketry.render;

import java.util.HashMap;

public class BlockModelData {

    private static final HashMap<Integer, HashMap<String, Object>> BLOCK_MODEL_MAP = new HashMap<>();
    static {
        loadBlockModels();
    }

    public static HashMap<String, Object> getBlockModel(int block) {
        return BLOCK_MODEL_MAP.get(block);
    }

    private static void loadBlockModels() {



        



    }
}
