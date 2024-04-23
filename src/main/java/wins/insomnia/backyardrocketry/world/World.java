package wins.insomnia.backyardrocketry.world;

import java.util.ArrayList;
import java.util.Random;

public class World {

    ArrayList<Chunk> chunks;
    public static final Random RANDOM = new Random();

    public World() {

        chunks = new ArrayList<>();
        chunks.add(new Chunk(0, 0, 0));

    }
}
