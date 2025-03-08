package wins.insomnia.backyardrocketry.util.debug;

import org.w3c.dom.Text;
import wins.insomnia.backyardrocketry.Main;
import wins.insomnia.backyardrocketry.render.Color;
import wins.insomnia.backyardrocketry.render.text.TextRenderer;
import wins.insomnia.backyardrocketry.render.texture.FontTexture;
import wins.insomnia.backyardrocketry.render.texture.TextureManager;
import wins.insomnia.backyardrocketry.util.update.Updater;

import java.util.ArrayList;

public class DebugOutput {


	public static final ArrayList<DebugText[]> DEBUG_TEXT_LIST = new ArrayList<>();



	public static void render() {

		for (int i = 0; i < DEBUG_TEXT_LIST.size(); i++) {

			StringBuilder text = new StringBuilder();

			for (DebugText debugText : DEBUG_TEXT_LIST.get(i)) {

				TextRenderer.setFontColor(debugText.color);
				TextRenderer.drawText(debugText.text, TextRenderer.getTextPixelWidth(text.toString()), i * TextRenderer.getTextPixelHeight(1), TextureManager.getTexture("debug_font"));
				text.append(debugText.text);

			}


		}

		TextRenderer.setFontColor(Color.WHITE);
	}


	public static void outputText(DebugText[] text) {

		if (Thread.currentThread() != Main.MAIN_THREAD) {
			Updater.get().queueMainThreadInstruction(() -> outputText(text));
			return;
		}

		DEBUG_TEXT_LIST.add(text);

		while (DEBUG_TEXT_LIST.size() > 5) {
			DEBUG_TEXT_LIST.remove(0);
		}

	}



	public static class DebugText {

		public String text;
		public Color color;

		public DebugText(String text, Color color) {

			this.text = text;
			this.color = color;

		}

		public DebugText(String text) {
			this(text, Color.WHITE);
		}
	}
}
