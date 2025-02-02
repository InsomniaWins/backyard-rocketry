package wins.insomnia.backyardrocketry.render;

public interface IRenderable {
    void render();
    boolean shouldRender();
    boolean isClean();
    void clean();
    int getRenderPriority();
    boolean hasTransparency();
}
