package wins.insomnia.backyardrocketry.util.datastructure;

import wins.insomnia.backyardrocketry.world.World;

import java.util.LinkedList;

public class ChunkManagementQueue {

	public final int SIZE;

	private final LinkedList<World.ChunkManagementData> LIST = new LinkedList<>();



	public ChunkManagementQueue(int size) {
		SIZE = size;
	}


	public void add(World.ChunkManagementData element) {

		if (LIST.size() >= SIZE) return;

		LIST.offer(element);

	}

	public World.ChunkManagementData poll() {

		return LIST.poll();

	}


	public int getSize() {
		return SIZE;
	}
}
