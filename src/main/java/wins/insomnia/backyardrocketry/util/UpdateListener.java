package wins.insomnia.backyardrocketry.util;

public class UpdateListener implements IFixedUpdateListener, IUpdateListener{

    private boolean registered = false;




    @Override
    public void fixedUpdate() {

    }

    @Override
    public void update(double deltaTime) {

    }


    public boolean isRegistered() {
        return registered;
    }

    public void register() {

        Updater.get().registerManualUpdateListener(this);
        registered = true;

    }

    public void unregister() {

        Updater.get().unregisterManualUpdateListener(this);
        registered = false;

    }
}
