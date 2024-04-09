package wins.insomnia.backyardrocketry.world;

import java.util.ArrayList;

public class World {

    ArrayList<Chunk> chunks;

    public World() {

        chunks = new ArrayList<>();
        chunks.add(new Chunk(0, 0, 0));

    }
}
