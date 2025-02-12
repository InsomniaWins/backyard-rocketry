package wins.insomnia.backyardrocketry.gui.elements;

import org.joml.Vector2f;
import wins.insomnia.backyardrocketry.audio.AudioBuffer;
import wins.insomnia.backyardrocketry.audio.AudioManager;
import wins.insomnia.backyardrocketry.render.Color;
import wins.insomnia.backyardrocketry.render.Renderer;
import wins.insomnia.backyardrocketry.render.Window;
import wins.insomnia.backyardrocketry.render.text.TextRenderer;
import wins.insomnia.backyardrocketry.render.texture.Texture;
import wins.insomnia.backyardrocketry.render.texture.TextureManager;
import wins.insomnia.backyardrocketry.render.texture.TextureRenderer;
import wins.insomnia.backyardrocketry.util.input.*;
import wins.insomnia.backyardrocketry.util.update.Updater;


import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;

public class LineEdit extends GuiElement {
	public enum HorizontalTextAlignment {
		LEFT,
		CENTER,
		RIGHT,
	}
	private static final ArrayList<Character> DEFAULT_ALLOWED_CHARACTERS = new ArrayList<>();
	static {

		for (int i = 0; i < 26; i++) {
			DEFAULT_ALLOWED_CHARACTERS.add((char) (i + (int) 'a'));
			DEFAULT_ALLOWED_CHARACTERS.add((char) (i + (int) 'A'));
		}

		for (int i = 0; i < 10; i++) {
			DEFAULT_ALLOWED_CHARACTERS.add((char) (i + (int) '0'));
		}

		DEFAULT_ALLOWED_CHARACTERS.add(' ');

	}
	private Texture normalTexture;
	private Texture hoveredTexture;
	private Texture pressedTexture;
	private boolean enabled = true;
	private boolean hovered = false;
	private boolean pressed = false;
	public boolean visible = true;
	private String text = "";
	private String label = "";
	private final ArrayList<Character> ALLOWED_CHARACTERS = new ArrayList<>();
	private int maximumCharacters = 100;
	private int caretBlinkSpeed = 10;
	private int caretBlinkValue = 0;
	private boolean caretVisible = true;
	private final Color CARET_COLOR = new Color(133, 133, 133);
	private HorizontalTextAlignment horizontalTextAlignment = HorizontalTextAlignment.LEFT;
	private final ArrayList<TextListener> TEXT_LISTENERS = new ArrayList<>();

	public LineEdit(String label) {
		normalTexture = TextureManager.getTexture("line_edit");
		hoveredTexture = TextureManager.getTexture("line_edit_hovered");
		pressedTexture = TextureManager.getTexture("line_edit_pressed");

		ALLOWED_CHARACTERS.addAll(DEFAULT_ALLOWED_CHARACTERS);

		this.label = label;
	}

	public LineEdit() {
		this("");
	}

	public void registerTextListener(TextListener listener) {
		TEXT_LISTENERS.add(listener);
	}

	public void unregisterTextListener(TextListener listener) {
		TEXT_LISTENERS.remove(listener);
	}

	public void setHorizontalTextAlignment(HorizontalTextAlignment alignment) {
		horizontalTextAlignment = alignment;
	}

	public HorizontalTextAlignment getHorizontalTextAlignment() {
		return horizontalTextAlignment;
	}

	public int getMaximumCharacters() {
		return maximumCharacters;
	}

	public void setMaximumCharacters(int maximumCharacters) {
		this.maximumCharacters = Math.max(0, maximumCharacters);
		setText(getText().substring(0, Math.min(getText().length(), this.maximumCharacters)));
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setEnabled(boolean value) {
		enabled = value;

		if (!value) loseFocus();

	}

	public void enable() {
		setEnabled(true);
	}

	public void disable() {
		setEnabled(false);
	}

	public boolean isEnabled() {
		return enabled;
	}

	public boolean isDisabled() {
		return !isEnabled();
	}

	private void updateListeners() {
		for (TextListener listener : TEXT_LISTENERS) {

			listener.textChanged(getText());

		}
	}

	public void setText(String newText, boolean updateListeners) {
		text = newText;
		if (updateListeners) {
			updateListeners();
		}
	}

	public void setText(String newText) {
		setText(newText, true);
	}

	public void clearText() {
		text = "";
	}

	public String getText() {
		return text;
	}

	public ArrayList<Character> getAllowedCharacters() {
		return ALLOWED_CHARACTERS;
	}

	public void addAllowedCharacter(char c) {
		getAllowedCharacters().add(c);
	}

	public void removeAllowedCharacter(char c) {
		getAllowedCharacters().remove(c);
	}

	public void clearAllowedCharacters() {
		getAllowedCharacters().clear();
	}

	@Override
	public boolean shouldRender() {
		return visible;
	}

	@Override
	public void render() {

		Texture texture = normalTexture;
		if (hovered) texture = hoveredTexture;
		if (hasFocus()) texture = pressedTexture;

		TextureRenderer.drawGuiTextureNineSlice(
				texture,
				(int) getXPosition(),
				(int) getYPosition(),
				(int) getWidth(),
				(int) getHeight(),
				5, true
		);

		TextRenderer.drawText(
				label,
				(int) (getXPosition() + getWidth() / 2f - TextRenderer.getTextPixelWidth(label) / 2),
				(int) (getYPosition() - 12)
		);

		int textX = (int) getXPosition() + 4;

		if (getHorizontalTextAlignment() == HorizontalTextAlignment.CENTER) {
			textX = (int) (getXPosition() + getWidth() / 2f - TextRenderer.getTextPixelWidth(text) / 2f);
		} else if (getHorizontalTextAlignment() == HorizontalTextAlignment.RIGHT) {
			textX = (int) (getXPosition() + getWidth() - TextRenderer.getTextPixelWidth(text));
		}

		int textY = (int) (getYPosition() + getHeight() / 2 - TextRenderer.getTextPixelHeight(1) / 2);

		TextRenderer.drawText(text, textX, textY);

		if (caretVisible && hasFocus()) {
			TextRenderer.setFontColor(CARET_COLOR);
			TextRenderer.drawText("_", textX + TextRenderer.getTextPixelWidth(text), textY);
			TextRenderer.setFontColor(Color.WHITE);
		}

	}

	private void checkForPress() {

		if (!isEnabled()) return;


		MouseInput mouseInput = MouseInput.get();


		int mouseX = Window.get().getViewportMouseX();
		int mouseY = Window.get().getViewportMouseY();

		if (containsPoint(mouseX, mouseY)) {
			if (!hovered) {
				hovered = true;
				AudioManager audioManager = AudioManager.get();
				AudioBuffer hoverSound = audioManager.getAudioBuffer("button_hover");
				audioManager.playAudio(hoverSound, false, false, true);
			}
		} else {
			hovered = false;
		}


		if (hovered) {
			if (mouseInput.isButtonJustPressed(GLFW_MOUSE_BUTTON_LEFT)) {
				pressed = true;

				grabFocus();

			} else if (mouseInput.isButtonJustReleased(GLFW_MOUSE_BUTTON_LEFT)) {
				pressed = false;
			}
		} else {
			pressed = false;

			if (mouseInput.isButtonJustPressed(GLFW_MOUSE_BUTTON_LEFT)) {
				loseFocus();
			}

		}

	}

	@Override
	public void fixedUpdate() {

		checkForPress();
		caretBlinkValue++;
		if (caretBlinkValue > caretBlinkSpeed) {
			caretBlinkValue = 0;
			caretVisible = !caretVisible;
		}

	}

	private void onTypedCharacter(char typedCharacter) {

		if (getText().length() >= getMaximumCharacters()) return;

		setText(getText() + typedCharacter);

	}

	private void onBackspace() {

		if (getText().isEmpty()) return;

		setText(getText().substring(0, getText().length() - 1));

	}

	private boolean isAllowedCharacter(char character) {
		return getAllowedCharacters().contains(character);
	}

	private void inputEvent(InputEvent inputEvent) {

		if (!hasFocus()) return;
		if (!isEnabled()) return;

		if (inputEvent.isConsumed()) return;

		if (inputEvent instanceof TypeInputEvent typeInputEvent) {

			typeInputEvent.consume();

			char keyCharacter = typeInputEvent.getCharacter();
			if (isAllowedCharacter(keyCharacter)) {
				onTypedCharacter(keyCharacter);
			}

		} else if (inputEvent instanceof KeyboardInputEvent keyboardInputEvent) {

			keyboardInputEvent.consume();

			if (keyboardInputEvent.isJustPressed()) {

				switch (keyboardInputEvent.getKey()) {
					case GLFW_KEY_BACKSPACE -> onBackspace();
					case GLFW_KEY_ESCAPE, GLFW_KEY_ENTER -> {
						loseFocus();
						updateListeners();
					}
				}

			}
		}

	}


	@Override
	public void registeredUpdateListener() {

		KeyboardInput.get().registerInputCallback(this::inputEvent);

	}



	@Override
	public void unregisteredUpdateListener() {

		KeyboardInput.get().unregisterInputCallback(this::inputEvent);


	}

	public static interface TextListener {

		void textChanged(String text);

	}
}
