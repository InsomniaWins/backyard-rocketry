package wins.insomnia.backyardrocketry.entity.player;

import org.joml.Vector3d;
import org.joml.Vector3i;
import wins.insomnia.backyardrocketry.util.Transform;
import wins.insomnia.backyardrocketry.world.World;


public interface IPlayer {
    World getWorld();

    Vector3d getPosition();
    Vector3i getBlockPosition();
    Transform getTransform();
    Vector3d getInterpolatedPosition();
}
