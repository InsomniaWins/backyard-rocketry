package wins.insomnia.backyardrocketry.render;

import java.util.ArrayList;

public class Renderer {

    public final ArrayList<IRenderable> renderables = new ArrayList<>();

    public void render() {

        for (IRenderable renderable : renderables) {

            renderable.render();

        }

    }



}
