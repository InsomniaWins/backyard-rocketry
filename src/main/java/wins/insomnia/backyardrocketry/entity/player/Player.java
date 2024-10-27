package wins.insomnia.backyardrocketry.entity.player;

import org.joml.Vector3d;
import wins.insomnia.backyardrocketry.level.Level;
import wins.insomnia.backyardrocketry.util.IFixedUpdateListener;
import wins.insomnia.backyardrocketry.util.IUpdateListener;
import wins.insomnia.backyardrocketry.util.Transform;
import wins.insomnia.backyardrocketry.util.Updater;
import wins.insomnia.backyardrocketry.util.input.KeyboardInput;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;

public class Player implements IUpdateListener, IFixedUpdateListener {

    private static Player player;
    private final Transform TRANSFORM = new Transform();
    private final Level LEVEL;

    public Player(Level level) {
        this.LEVEL = level;
        Player.player = this;

        Updater.get().registerUpdateListener(this);
        Updater.get().registerFixedUpdateListener(this);
    }




    @Override
    public void update(double deltaTime) {



    }


    @Override
    public void fixedUpdate() {
        LEVEL.updateChunksAroundPlayer(this);


        if (KeyboardInput.get().isKeyJustPressed(GLFW_KEY_SPACE)) {

            getPosition().x += 1;

        }

    }





    public Level getLevel() {
        return LEVEL;
    }

    public Transform getTransform() {
        return TRANSFORM;
    }

    public Vector3d getPosition() {
        return getTransform().getPosition();
    }

    public static Player get() {
        return player;
    }
}
