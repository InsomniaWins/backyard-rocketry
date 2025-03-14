package wins.insomnia.backyardrocketry.world.chunk;

import wins.insomnia.backyardrocketry.Main;
import wins.insomnia.backyardrocketry.audio.AudioManager;
import wins.insomnia.backyardrocketry.audio.AudioPlayer;
import wins.insomnia.backyardrocketry.render.ChunkMesh;
import wins.insomnia.backyardrocketry.render.Color;
import wins.insomnia.backyardrocketry.render.Renderer;
import wins.insomnia.backyardrocketry.util.debug.DebugInfo;
import wins.insomnia.backyardrocketry.util.debug.DebugOutput;
import wins.insomnia.backyardrocketry.util.io.ChunkIO;
import wins.insomnia.backyardrocketry.util.update.Updater;
import wins.insomnia.backyardrocketry.world.ChunkPosition;
import wins.insomnia.backyardrocketry.world.ClientWorld;
import wins.insomnia.backyardrocketry.world.World;
import wins.insomnia.backyardrocketry.world.block.Blocks;
import wins.insomnia.backyardrocketry.world.block.BlockAudio;
import wins.insomnia.backyardrocketry.world.lighting.ChunkLighting;

import java.util.ConcurrentModificationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientChunk extends Chunk {

	public static final ExecutorService chunkMeshGenerationExecutorService = Executors.newFixedThreadPool(4, r -> new Thread(r, "chunk-mesh-generation-thread"));
	private final AtomicBoolean SHOULD_REGENERATE_MESH = new AtomicBoolean(false);
	private final AtomicBoolean SHOULD_INSTANTLY_REGENERATE_MESH = new AtomicBoolean(false);
	private final ChunkMesh CHUNK_MESH;
	private final ChunkMesh TRANSPARENT_CHUNK_MESH;

	public ClientChunk(World world, ChunkPosition chunkPosition) {
		super(world, chunkPosition);


		SHOULD_REGENERATE_MESH.set(true);

		CHUNK_MESH = new ChunkMesh(this, false);
		TRANSPARENT_CHUNK_MESH = new ChunkMesh(this, true);

		updateNeighborChunkMeshes(true);
	}

	public ClientChunk(ClientWorld world, ChunkData chunkData) {
		this(world, chunkData.getChunkPosition(world));
		gotChunkDataFromServer(chunkData);
	}

	public void setBlock(int x, int y, int z, byte block, byte blockState, boolean regenerateMesh, boolean instantly) {

		if (Thread.currentThread() != Main.MAIN_THREAD) {

			throw new ConcurrentModificationException("Tried to \"setBlock\" on thread other than main thread!");

		}


		chunkData.setBlock(x,y,z, block);
		chunkData.setBlockState(x, y, z, blockState);

		short lightColor = Blocks.getBlockMinimumLightLevel(block);
		if (lightColor != 0) {
			ChunkLighting.setLight(this, x, y, z, lightColor);
		} else {
			ChunkLighting.removeLight(this, x, y, z);
		}



        setShouldRegenerateMesh(regenerateMesh, instantly);

        if (regenerateMesh) {
            updateNeighborChunkMeshesIfBlockIsOnBorder(toGlobalX(x), toGlobalY(y), toGlobalZ(z), instantly);
        }

	}

	public void setBlock(int x, int y, int z, byte block, byte blockState, boolean regenerateMesh) {

		setBlock(x, y, z, block, blockState, regenerateMesh, false);

	}

	public void breakBlock(int x, int y, int z, boolean regenerateMesh) {
		byte blockBroken = getBlock(x, y, z);
		setBlock(x, y, z, Blocks.AIR, (byte) 0, regenerateMesh, true);

		BlockAudio blockAudio = Blocks.getBlockAudio(blockBroken);
		if (blockAudio != null) {
			AudioPlayer audioPlayer = AudioManager.get().playAudioSpatial(blockAudio.getBreakAudio(), false, false, true);
			audioPlayer.setPosition(toGlobalX(x) + 0.5f, toGlobalY(y) + 0.5f, toGlobalZ(z) + 0.5f);
			audioPlayer.setPitch(World.RANDOM.nextFloat(0.9f, 1.1f));
		}

	}

	public void placeBlock(int x, int y, int z, byte block, byte blockState) {
		setBlock(x, y, z, block, blockState, true, true);

		BlockAudio blockAudio = Blocks.getBlockAudio(block);
		if (blockAudio != null) {
			AudioPlayer audioPlayer = AudioManager.get().playAudioSpatial(blockAudio.getPlaceAudio(), false, false, true);
			audioPlayer.setPosition(toGlobalX(x) + 0.5f, toGlobalY(y) + 0.5f, toGlobalZ(z) + 0.5f);
			audioPlayer.setPitch(World.RANDOM.nextFloat(0.9f, 1.1f));
		}

	}

	public void gotChunkDataFromServer(ChunkData chunkData) {
		this.chunkData = chunkData;
		SHOULD_REGENERATE_MESH.set(true);
	}


	public void updateNeighborChunkMeshes(boolean instantly, boolean includeCorners) {
		if (Thread.currentThread() != Main.MAIN_THREAD) {

			DebugOutput.outputText(new DebugOutput.DebugText[]{
					new DebugOutput.DebugText("Tried updating neighboring chunk meshes from thread other than the main thread!", Color.RED)
			});

			return;
		}



		for (Chunk chunk : getNeighborChunks(includeCorners)) {
			if (!(chunk instanceof ClientChunk clientChunk)) {
				continue;
			}

			clientChunk.SHOULD_INSTANTLY_REGENERATE_MESH.set(instantly);
			clientChunk.SHOULD_REGENERATE_MESH.set(true);
		}
	}

	public void updateNeighborChunkMeshes(boolean instantly) {

		updateNeighborChunkMeshes(instantly, false);
	}

	private void updateNeighborChunkMeshesIfBlockIsOnBorder(int x, int y, int z, boolean instantly) {

		if (isBlockOnChunkBorder(x, y, z)) {

			updateNeighborChunkMeshes(instantly);

		}

	}

	private void updateNeighborChunkMeshesIfBlockIsOnBorder(int x, int y, int z) {
		updateNeighborChunkMeshesIfBlockIsOnBorder(x, y, z, false);
	}

	public ChunkData getChunkData() {
		return chunkData;
	}

	private void generateMesh(boolean isDelayed) {

		SHOULD_REGENERATE_MESH.set(false);

		try {

			CHUNK_MESH.generateMesh(chunkData.getBlocks(), chunkData.getBlockStates(), isDelayed);
			TRANSPARENT_CHUNK_MESH.generateMesh(chunkData.getBlocks(), chunkData.getBlockStates(), isDelayed);

		} catch (Exception e) {

			e.printStackTrace();

		}

	}

	public void clean() {
		SHOULD_REGENERATE_MESH.set(false);

		Renderer.get().removeRenderable(CHUNK_MESH);
		Renderer.get().removeRenderable(TRANSPARENT_CHUNK_MESH);

		CHUNK_MESH.destroy();
		TRANSPARENT_CHUNK_MESH.destroy();

	}

	public void setShouldRegenerateMesh(boolean value, boolean instantly) {
		SHOULD_INSTANTLY_REGENERATE_MESH.set(instantly);
		SHOULD_REGENERATE_MESH.set(value);
	}

	public void setShouldRegenerateMesh(boolean value) {
		SHOULD_INSTANTLY_REGENERATE_MESH.set(false);
		SHOULD_REGENERATE_MESH.set(value);
	}

	private void generateMesh() {
		generateMesh(true);
	}

	public ChunkMesh getChunkMesh() {
		return CHUNK_MESH;
	}

	public ChunkMesh getTransparentChunkMesh() {
		return TRANSPARENT_CHUNK_MESH;
	}

	@Override
	public void update(double deltaTime) {
		super.update(deltaTime);

		if (SHOULD_REGENERATE_MESH.get()) {

			if (!CHUNK_MESH.isGenerating() && !TRANSPARENT_CHUNK_MESH.isGenerating()) {


				SHOULD_REGENERATE_MESH.set(false);

				CHUNK_MESH.setGenerating(true);
				TRANSPARENT_CHUNK_MESH.setGenerating(true);

				boolean instantly = SHOULD_INSTANTLY_REGENERATE_MESH.getAndSet(false);

				chunkMeshGenerationExecutorService.submit(() -> generateMesh(instantly));
			}
		}


		//<editor-fold desc="create chunk meshes">
		// Moving this block to fixedUpdate would stop the block outline from looking delayed when placing/breaking
		// a block; however, it also destabilizes FPS
		if (CHUNK_MESH.isReadyToCreateOpenGLMeshData()) {
			CHUNK_MESH.createOpenGLMeshData();
		}

		if (TRANSPARENT_CHUNK_MESH.isReadyToCreateOpenGLMeshData()) {
			TRANSPARENT_CHUNK_MESH.createOpenGLMeshData();
		}
		//</editor-fold>
	}


	@Override
	public void registeredFixedUpdateListener() {

		Renderer.get().addRenderable(CHUNK_MESH);
		Renderer.get().addRenderable(TRANSPARENT_CHUNK_MESH);

	}

	@Override
	public void unregisteredFixedUpdateListener() {
		clean();

		chunkMeshGenerationExecutorService.submit(() -> {
			ChunkIO.saveChunk(this, chunkData);
		});

	}


}
