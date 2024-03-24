package wins.insomnia.backyardrocketry.world;

import wins.insomnia.backyardrocketry.render.IRenderable;

import java.util.ArrayList;

public class World implements IRenderable {

    ArrayList<Chunk> chunks;

    public World() {

        chunks = new ArrayList<>();

        chunks.add(new Chunk(0, 0, 0));

    }

    @Override
    public void render() {

        for (Chunk chunk : chunks) {
            chunk.render();
        }

    }
}
