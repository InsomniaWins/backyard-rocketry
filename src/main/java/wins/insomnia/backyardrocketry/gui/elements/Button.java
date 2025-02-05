package wins.insomnia.backyardrocketry.gui.elements;

import org.w3c.dom.Text;
import wins.insomnia.backyardrocketry.audio.AudioBuffer;
import wins.insomnia.backyardrocketry.audio.AudioManager;
import wins.insomnia.backyardrocketry.audio.AudioPlayer;
import wins.insomnia.backyardrocketry.render.*;
import wins.insomnia.backyardrocketry.util.input.MouseInput;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

public class Button extends GuiElement {


	private Texture normalTexture;
	private Texture hoveredTexture;
	private Texture pressedTexture;

	private String buttonName;
	private Runnable pressedCallback;
	private boolean enabled = true;
	private boolean hovered = false;
	private boolean pressed = false;

	public boolean visible = true;
	public boolean onJustPressed = false;

	public Button(String buttonName, Runnable pressedCallback) {
		this.buttonName = buttonName;
		this.pressedCallback = pressedCallback;

		normalTexture = TextureManager.getTexture("text_button");
		hoveredTexture = TextureManager.getTexture("text_button_hovered");
		pressedTexture = TextureManager.getTexture("text_button_pressed");

	}

	public void setEnabled(boolean value) {
		enabled = value;
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

	public String getButtonName() {
		return buttonName;
	}

	public Runnable getPressedCallback() {
		return pressedCallback;
	}

	public void setPressedCallback(Runnable pressedCallback) {
		this.pressedCallback = pressedCallback;
	}

	public void setButtonName(String buttonName) {
		this.buttonName = buttonName;
	}


	@Override
	public boolean shouldRender() {
		return visible;
	}

	@Override
	public boolean hasTransparency() {
		return true;
	}

	@Override
	public void render() {

		Texture buttonTexture = normalTexture;

		if (hovered && hoveredTexture != null) {
			buttonTexture = hoveredTexture;
		}

		if (pressed && pressedTexture != null) {
			buttonTexture = pressedTexture;
		}


		Renderer.get().drawGuiTextureNineSlice(
				buttonTexture,
				(int) getXPosition(),
				(int) getYPosition(),
				(int) getWidth(),
				(int) getHeight(),
				5, true
		);

		if (buttonName != null) {

			double centerX = getXPosition() + getWidth() / 2.0;
			double centerY = getYPosition() + getHeight() / 2.0;

			int textX = (int) (centerX - TextRenderer.getTextPixelWidth(buttonName) / 2.0);
			int textY = (int) (centerY - TextRenderer.getTextPixelHeight(1) / 2.0);

			if (hovered) {
				TextRenderer.setFontColor(new Color(1, 0.7843137254901961f, 0.1450980392156863f));
			} else {
				TextRenderer.setFontColor(Color.WHITE);
			}

			TextRenderer.drawTextOutline(buttonName, textX, textY, 1, TextureManager.getTexture("font"));

			TextRenderer.setFontColor(Color.WHITE);
		}

	}


	private void playPressSound() {

		AudioManager audioManager = AudioManager.get();
		AudioBuffer hoverSound = audioManager.getAudioBuffer("button_hover");

		AudioPlayer audioPlayer = audioManager.playAudio(hoverSound, false, false, true);
		audioPlayer.setPitch(2f);

	}


	private void checkForPress() {

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
				if (onJustPressed) {
					playPressSound();
					pressedCallback.run();
				}
			} else if (mouseInput.isButtonJustReleased(GLFW_MOUSE_BUTTON_LEFT)) {
				pressed = false;
				if (!onJustPressed) {
					playPressSound();
					pressedCallback.run();
				}
			}
		} else {
			pressed = false;
		}

	}

	@Override
	public void fixedUpdate() {

		if (isEnabled()) {
			checkForPress();
		}

	}


}
