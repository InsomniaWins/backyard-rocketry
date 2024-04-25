package wins.insomnia.backyardrocketry.physics;

public interface ICollisionBody {

    BoundingBox getBoundingBox();
    boolean isBodyStatic();
}
