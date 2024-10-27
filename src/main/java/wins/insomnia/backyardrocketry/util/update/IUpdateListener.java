package wins.insomnia.backyardrocketry.util.update;

public interface IUpdateListener extends IGenericUpdateListener {

    void update(double deltaTime);
    void registeredUpdateListener();
    void unregisteredUpdateListener();
}
