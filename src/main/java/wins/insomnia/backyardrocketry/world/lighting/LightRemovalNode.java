package wins.insomnia.backyardrocketry.world.lighting;

import wins.insomnia.backyardrocketry.world.chunk.Chunk;

public class LightRemovalNode {

	public Chunk chunk;
	public byte x;
	public byte y;
	public byte z;
	public int colorChannel = -1;
	public short lightValue;

	public LightRemovalNode(byte x, byte y, byte z, short lightValue, Chunk chunk) {

		this.x = x;
		this.y = y;
		this.z = z;

		this.lightValue = lightValue;
		this.chunk = chunk;
	}

	public LightRemovalNode setColorChannel(int colorChannel) {
		this.colorChannel = colorChannel;
		return this;
	}

}
