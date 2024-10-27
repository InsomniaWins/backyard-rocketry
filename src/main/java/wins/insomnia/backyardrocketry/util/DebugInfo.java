package wins.insomnia.backyardrocketry.util;

import org.joml.Vector3i;
import wins.insomnia.backyardrocketry.entity.player.IPlayer;
import wins.insomnia.backyardrocketry.entity.player.TestPlayer;
import wins.insomnia.backyardrocketry.render.Renderer;
import wins.insomnia.backyardrocketry.util.update.Updater;
import wins.insomnia.backyardrocketry.world.ChunkPosition;
import wins.insomnia.backyardrocketry.world.block.Block;

import java.text.DecimalFormat;

public class DebugInfo {

	public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");

	public static String getMemoryUsage() {

		long usedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

		return "Memory Usage: \n  " +
				usedMemory / 1_048_576 + "MiB / " +
				Runtime.getRuntime().totalMemory() / 1_048_576 + "MiB";
	}

	public static String getFramesPerSecond() {
		return "FPS: \n  " + Renderer.get().getFramesPerSecond();
	}

	public static String getFixedUpdatesPerSecond() {
		return "Fixed Updates: \n  " + Updater.get().getUpdatesPerSecond();
	}

	public static String getPlayerChunkPosition(IPlayer player) {
		Vector3i playerBlockPosition = player.getBlockPosition();
		ChunkPosition chunkPosition = player.getWorld().getChunkPositionFromBlockPosition(playerBlockPosition.x, playerBlockPosition.y, playerBlockPosition.z);
		return "Chunk Position: \n  " + (chunkPosition != null ? FancyToString.toString(chunkPosition) : "NULL");
	}
	public static String getPlayerBlockPosition(IPlayer player) {
		Vector3i playerBlockPosition = player.getBlockPosition();
		return "Block Position: \n  " + FancyToString.toString(playerBlockPosition);
	}

	public static String getPlayerPosition(IPlayer player) {
		return "Camera Position: \n  <" + DECIMAL_FORMAT.format(player.getPosition().x) +
				", " + DECIMAL_FORMAT.format(player.getPosition().y) +
				", " + DECIMAL_FORMAT.format(player.getPosition().z) + ">";
	}

	public static String getPlayerRotation(IPlayer player) {
		return "Camera Rotation: \n  <" + DECIMAL_FORMAT.format(player.getTransform().getRotation().x) +
				", " + DECIMAL_FORMAT.format(player.getTransform().getRotation().y) +
				", " + DECIMAL_FORMAT.format(player.getTransform().getRotation().z) + ">";
	}

	public static String getRenderMode() {
		return "Render Mode: \n  " +
		switch (Renderer.get().getRenderMode()) {
			case 0 -> "[Cull Back], [Fill]";
			case 1 -> "[Cull Back], [Wireframe]";
			default -> "[No Cull], [Fill]";
		};
	}

	public static String getPlayerTargetBlockInfo(TestPlayer player) {

		int targetBlockHealth = 0;
		int targetBlock = Block.NULL;
		int targetBlockState = Block.NULL;

		if (player.getTargetBlock() != null) {
			int blockState = (player.getWorld().getBlockState(
					player.getTargetBlock().getBlockX(),
					player.getTargetBlock().getBlockY(),
					player.getTargetBlock().getBlockZ()
			));

			if (BitHelper.getBlockIdFromBlockState(blockState) != Block.NULL) {
				targetBlockHealth = 0;
				targetBlockState = blockState;
				targetBlock = BitHelper.getBlockIdFromBlockState(targetBlockState);
			}
		}

		if (player.getTargetBlock() == null) {
			return "Targeted Block: \n  NULL";
		} else {
			return "Targeted Block: \n  " + targetBlock + " : <" +
					player.getTargetBlock().getBlockX() + ", " +
					player.getTargetBlock().getBlockY() + ", " +
					player.getTargetBlock().getBlockZ() + ">" +
					"\n  " + String.format("%32s", Integer.toBinaryString(targetBlockState)).replace(' ', '0') +
					"\n  " + Block.getBlockName(targetBlock);
		}

	}
}
