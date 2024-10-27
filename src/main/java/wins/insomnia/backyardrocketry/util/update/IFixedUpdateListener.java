package wins.insomnia.backyardrocketry.util.update;

public interface IFixedUpdateListener extends IGenericUpdateListener {

    void fixedUpdate();
    void registeredFixedUpdateListener();
    void unregisteredFixedUpdateListener();
}
