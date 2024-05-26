package wins.insomnia.backyardrocketry;

import wins.insomnia.backyardrocketry.world.blockproperty.BlockProperties;

public class Main {

    private static BackyardRocketry game;
    public static final Thread MAIN_THREAD = Thread.currentThread();

    public static void main(String[] args) {


        int num = 0b00000001_10000000_00000000_00001101;

        System.out.println(Integer.toBinaryString(BlockProperties.getBlockPropertiesFromBlockState(num).getPropertiesInt()));

        game = new BackyardRocketry();
        game.run();

    }

    public BackyardRocketry getGame() {
        return game;
    }
}