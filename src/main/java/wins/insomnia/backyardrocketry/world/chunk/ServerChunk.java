package wins.insomnia.backyardrocketry.world.chunk;

import org.joml.Math;
import wins.insomnia.backyardrocketry.Main;
import wins.insomnia.backyardrocketry.controller.ServerController;
import wins.insomnia.backyardrocketry.item.Item;
import wins.insomnia.backyardrocketry.item.ItemStack;
import wins.insomnia.backyardrocketry.item.Items;
import wins.insomnia.backyardrocketry.network.entity.player.PacketPlayerBreakBlock;
import wins.insomnia.backyardrocketry.network.entity.player.PacketPlayerPlaceBlock;
import wins.insomnia.backyardrocketry.network.world.PacketLoadChunk;
import wins.insomnia.backyardrocketry.network.world.PacketUpdateBlock;
import wins.insomnia.backyardrocketry.util.update.Updater;
import wins.insomnia.backyardrocketry.world.ChunkPosition;
import wins.insomnia.backyardrocketry.world.ServerWorld;
import wins.insomnia.backyardrocketry.world.World;
import wins.insomnia.backyardrocketry.world.WorldGeneration;
import wins.insomnia.backyardrocketry.world.block.Block;
import wins.insomnia.backyardrocketry.world.block.Blocks;
import wins.insomnia.backyardrocketry.world.block.blockstate.BlockStateManager;
import wins.insomnia.backyardrocketry.world.block.loot.BlockLoot;

import java.util.*;

public class ServerChunk extends Chunk {

	public enum GenerationPass {
		UNLOADED,
		TERRAIN,
		DECORATION

	}
	private final Queue<SetBlockQueueElement> SET_BLOCK_QUEUE = new LinkedList<>();
	public static final GenerationPass[] GENERATION_PASS_VALUES = GenerationPass.values();
	private boolean finishedGenerationPass = false;
	private boolean startedGenerationPass = false;
	private int currentGenerationPass = GenerationPass.UNLOADED.ordinal();
	private int desiredGenerationPass = GenerationPass.UNLOADED.ordinal();


	public ServerChunk(World world, ChunkPosition chunkPosition) {
		super(world, chunkPosition);
		chunkData = new ChunkData(
				world.getSeed(),
				chunkPosition.getX(),
				chunkPosition.getY(),
				chunkPosition.getZ(),
				false,
				false
		);

		finishedGenerationPass = true;
		startedGenerationPass = false;

	}



	// must be run on main thread!
	private boolean beginPass(GenerationPass pass) {

		if (Thread.currentThread() != Main.MAIN_THREAD) {
			getWorld().logInfo("Tried to begin chunk pass on thread other than main thread! -> " + getChunkPosition());
			return false;
		}

		if (!finishedGenerationPass || startedGenerationPass) {
			return false;
		}

		// tried beginning pass out of order
		if (pass.ordinal() - 1 != currentGenerationPass) {
			return false;
		}


		// neighboring chunks not ready
		if (pass.ordinal() > GenerationPass.TERRAIN.ordinal()) {
			if (!areNeighboringChunksFinishedWithPass(GENERATION_PASS_VALUES[currentGenerationPass], true)) {
				return false;
			}
		}


		finishedGenerationPass = false;
		startedGenerationPass = true;
		currentGenerationPass = pass.ordinal();

		((ServerWorld) getWorld()).submitChunkTask(() -> {

			WorldGeneration.runChunkGenerationPass(
					this,
					chunkData,
					getCurrentGenerationPass(),
					() -> Updater.get().queueMainThreadInstruction(() -> {

						finishedGenerationPass = true;
						startedGenerationPass = false;

						if (hasFinishedPass(GenerationPass.DECORATION)) {
							ServerController.sendReliable(createLoadPacket());
						}

					})
			);

		});

		return true;

	}

	// must run on main thread
	public boolean areNeighboringChunksFinishedWithPass(GenerationPass pass, boolean loadPass) {

		for (int x = 0; x < 3; x++) {
			for (int y = 0; y < 3; y++) {
				for (int z = 0; z < 3; z++) {

					if (z == 0 && y == 0 && x == 0) {

						continue;

					}


					ServerWorld serverWorld = (ServerWorld) getWorld();
					ChunkPosition neighborPosition = new ChunkPosition(getChunkPosition().add(x, y, z));

					if (!serverWorld.isChunkPositionInWorldBorder(neighborPosition)) continue;

					ServerChunk chunk = (ServerChunk) serverWorld.getChunkSafe(neighborPosition);

					if (chunk == null) {
						if (loadPass) {
							serverWorld.queueChunkForLoading(neighborPosition, GenerationPass.TERRAIN);
						}
						return false;
					}

					if (!chunk.hasFinishedPass(pass)) {

						if (loadPass) {
							int desiredPass = Math.max(pass.ordinal(), chunk.desiredGenerationPass);
							chunk.setDesiredGenerationPass(GENERATION_PASS_VALUES[desiredPass]);

						}

						//System.err.println("chunk " + chunk.getChunkPosition() + " not finished with " + pass + ": " + GENERATION_PASS_VALUES[chunk.currentGenerationPass]);
						return false;
					}

				}
			}
		}


		return true;
	}

	public ChunkData getChunkData() {
		return chunkData;
	}

	public boolean hasFinishedPass(GenerationPass pass) {

		if (getCurrentGenerationPassOrdinal() > pass.ordinal()) return true;
		if (getCurrentGenerationPassOrdinal() < pass.ordinal()) return false;

		// pass is equal to current pass, not check progress
		return finishedGenerationPass;

	}

	@Override
	public void fixedUpdate() {
		super.fixedUpdate();

		if (getCurrentGenerationPassOrdinal() < desiredGenerationPass) {

			int nextPass = Math.signum(desiredGenerationPass - getCurrentGenerationPassOrdinal());

			if (nextPass == 1) {
				beginPass(GENERATION_PASS_VALUES[getCurrentGenerationPassOrdinal() + 1]);
			}

		}

		/*
		try {
			Iterator<SetBlockQueueElement> iterator = SET_BLOCK_QUEUE.iterator();
			while (iterator.hasNext()) {
				SetBlockQueueElement element = iterator.next();
				iterator.remove();

				setBlock(
						element.localX,
						element.localY,
						element.localZ,
						element.block,
						element.blockState,
						element.updateClients
				);
			}
		} catch (ConcurrentModificationException e) {
			e.printStackTrace();
		}*/




	}

	public GenerationPass getDesiredGenerationPass() {
		return GENERATION_PASS_VALUES[desiredGenerationPass];
	}

	public int getDesiredGenerationPassOrdinal() {
		return desiredGenerationPass;
	}

	public void setDesiredGenerationPass(GenerationPass pass) {
		desiredGenerationPass = pass.ordinal();
	}


	public int getCurrentGenerationPassOrdinal() {
		return currentGenerationPass;
	}

	public GenerationPass getCurrentGenerationPass() {
		return GENERATION_PASS_VALUES[currentGenerationPass];
	}

	public PacketLoadChunk createLoadPacket() {

		return new PacketLoadChunk(chunkData);

	}



	public void setBlock(int x, int y, int z, byte block, byte blockState, boolean updateClients) {

		if (Thread.currentThread() != Main.MAIN_THREAD) {
			Updater.get().queueMainThreadInstruction(() -> {
				setBlock(x, y, z, block, blockState, updateClients);
			});
			return;
		}


		if (!hasFinishedPass(GenerationPass.TERRAIN)) {

			SET_BLOCK_QUEUE.add(new SetBlockQueueElement(
					x, y, z,
					block,
					blockState,
					updateClients
			));

			return;
		}


		super.setBlock(x, y, z, block, blockState);

		if (updateClients) {
			ServerController.sendReliable(
					new PacketUpdateBlock()
							.setWorldX(toGlobalX(x))
							.setWorldY(toGlobalY(y))
							.setWorldZ(toGlobalZ(z))
							.setBlock(getBlock(x, y, z))
							.setBlockState(blockState)
			);
		}

	}

	public void setBlock(int x, int y, int z, byte block, byte blockState) {
		setBlock(x, y, z, block, blockState, true);
	}


	public void placeBlock(int x, int y, int z, byte block, Blocks.Face face) {

		Block blockObject = Blocks.getBlock(block);

		if (blockObject == null) return;

		byte blockState = blockObject.onPlace(x ,y, z, face);


		setBlock(x, y, z, block, blockState, false);

		ServerController.sendReliable(
				new PacketPlayerPlaceBlock()
						.setWorldX(toGlobalX(x))
						.setWorldY(toGlobalY(y))
						.setWorldZ(toGlobalZ(z))
						.setBlock(block)
						.setBlockState(blockState)
		);

	}


	public void breakBlock(int x, int y, int z, boolean dropLoot) {

		if (!isBlockInBoundsLocal(x, y, z)) return;

		byte blockBroken = getBlock(x, y, z);
		byte blockState = getBlockState(x, y, z);

		if (Blocks.getBlockStrength(blockBroken) < 0) return;

		setBlock(x, y, z, Blocks.AIR, (byte) 0, false);

		if (blockBroken == Blocks.LOG) {

			System.err.println(Blocks.AIR);
			System.err.println(blockBroken + ":" + blockState + " = " + BlockStateManager.parseBlockState(blockBroken, blockState).getStateString());

		}

		if (dropLoot) {

			dropBlockLoot(
					blockBroken,
					getX() + x + 0.5f,
					getY() + y + 0.5f,
					getZ() + z + 0.5f
			);

		}

		ServerController.sendReliable(
				new PacketPlayerBreakBlock()
						.setWorldX(toGlobalX(x))
						.setWorldY(toGlobalY(y))
						.setWorldZ(toGlobalZ(z))
		);

	}

	public void dropBlockLoot(byte block, double x, double y, double z) {
		BlockLoot blockLoot = BlockLoot.getBlockLoot(block);

		if (blockLoot == null) {
			return;
		}

		ArrayList<ArrayList<Object>> defaultLoot = blockLoot.getLootOfType("default");
		for (ArrayList<Object> lootEntry : defaultLoot) {

			String itemSynonym = (String) lootEntry.get(0);
			int volume = (Integer) lootEntry.get(1);

			Item item = Items.getItem(itemSynonym);
			ItemStack itemStack = new ItemStack(item, volume);

			ServerWorld serverWorld = ServerWorld.getServerWorld();
			if (serverWorld != null) {

				serverWorld.dropItem(
						itemStack,
						x, y, z,
						0.0,
						0.0,
						0.0
				);

			}

		}
	}


	public boolean hasFinishedDesiredGenerationPass() {
		return hasFinishedPass(getDesiredGenerationPass());
	}


	public record SetBlockQueueElement(
		int localX, int localY, int localZ,
		byte block,
		byte blockState,
		boolean updateClients
	) {}



}
