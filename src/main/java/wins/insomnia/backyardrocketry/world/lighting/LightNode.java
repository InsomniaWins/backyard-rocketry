package wins.insomnia.backyardrocketry.world.lighting;

import wins.insomnia.backyardrocketry.world.chunk.Chunk;

public class LightNode {

	public byte x;
	public byte y;
	public byte z;
	public Chunk chunk;

	public LightNode(byte x, byte y, byte z, Chunk chunk) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.chunk = chunk;
	}

}
