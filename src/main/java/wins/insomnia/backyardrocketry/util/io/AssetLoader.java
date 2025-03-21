package wins.insomnia.backyardrocketry.util.io;

import java.util.LinkedList;
import java.util.Queue;

public class AssetLoader {
	private boolean processingTask = false;
	private int progress = 0;
	private int totalProgress = 0;
	private final Queue<LoadTask> LOAD_TASKS;

	public AssetLoader() {
		LOAD_TASKS = new LinkedList<>();
	}

	public void addLoadTask(LoadTask task) {
		LOAD_TASKS.add(task);
		totalProgress++;
	}

	public boolean areAssetsCurrentlyLoading() {
		return progress > 0 && !areAssetsLoaded();
	}

	public boolean areAssetsLoaded() {
		return progress >= totalProgress;
	}

	public LoadTask peekTask() {
		return LOAD_TASKS.peek();
	}

	public void loadNextAsset() {
		if (LOAD_TASKS.isEmpty()) return;

		processingTask = true;

		LoadTask task = LOAD_TASKS.poll();
		task.task().run();

		progress += 1;
		processingTask = false;

	}

	public int getTotalProgress() {
		return totalProgress;
	}

	public int getProgress() {
		return progress;
	}

	public void reset() {

		totalProgress = 0;
		progress = 0;
		LOAD_TASKS.clear();

	}

	public boolean isProcessingTask() {
		return processingTask;
	}
}
