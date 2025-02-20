package wins.insomnia.backyardrocketry;

import wins.insomnia.backyardrocketry.world.block.blockstate.property.PropertyBoolean;

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