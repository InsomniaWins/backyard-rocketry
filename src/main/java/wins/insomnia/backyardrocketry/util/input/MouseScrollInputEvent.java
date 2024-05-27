package wins.insomnia.backyardrocketry.util.input;

public class MouseScrollInputEvent extends InputEvent {

	private double offsetX;
	private double offsetY;


	private boolean consumed = false;

	public MouseScrollInputEvent(double offsetX, double offsetY) {
		this.offsetX = offsetX;
		this.offsetY = offsetY;
	}

	public double getOffsetX() {
		return offsetX;
	}

	public double getOffsetY() {
		return offsetY;
	}


	public boolean isConsumed() {
		return consumed;
	}

	public void consume() {
		consumed = true;
	}

}
