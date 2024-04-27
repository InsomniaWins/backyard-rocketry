package wins.insomnia.backyardrocketry.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class World {

    private final ArrayList<Chunk> CHUNKS;
    public static final Random RANDOM = new Random();

    public World() {

        CHUNKS = new ArrayList<>();

        for (int x = 0; x < 3; x++) {
            for (int z = 0; z < 3; z++) {
                CHUNKS.add(new Chunk(x * 16, 0, z * 16));
            }
        }

    }

    public List<Chunk> getChunks() {
        return CHUNKS;
    }
}
