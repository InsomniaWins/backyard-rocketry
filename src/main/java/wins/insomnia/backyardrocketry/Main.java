package wins.insomnia.backyardrocketry;

import org.joml.Math;
import wins.insomnia.backyardrocketry.util.BitHelper;
import wins.insomnia.backyardrocketry.world.block.blockstate.property.PropertyBoolean;
import wins.insomnia.backyardrocketry.world.chunk.ChunkData;

public class Main {

    private static BackyardRocketry game;
    public static final Thread MAIN_THREAD = Thread.currentThread();

    public static void main(String[] args) {

        game = new BackyardRocketry();
        game.run();

    }

    public BackyardRocketry getGame() {
        return game;
    }

}