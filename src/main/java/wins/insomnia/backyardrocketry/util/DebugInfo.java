package wins.insomnia.backyardrocketry.util;

import org.joml.Vector3i;
import wins.insomnia.backyardrocketry.render.Renderer;
import wins.insomnia.backyardrocketry.world.Block;
import wins.insomnia.backyardrocketry.world.BlockState;

import java.text.DecimalFormat;

public class DebugInfo {

	public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");



	public static String getMemoryUsage() {
		return "Memory Usage: " +
				Runtime.getRuntime().freeMemory() / 1_048_576 + "MiB / " +
				Runtime.getRuntime().totalMemory() / 1_048_576 + "MiB";
	}

	public static String getFramesPerSecond() {
		return "FPS: " + Renderer.get().getFramesPerSecond();
	}

	public static String getFixedUpdatesPerSecond() {
		return "Fixed Updates: " + Updater.get().getUpdatesPerSecond();
	}

	public static String getPlayerBlockPosition(IPlayer player) {
		Vector3i playerBlockPosition = player.getBlockPosition();
		return "Block Position: " + FancyToString.toString(playerBlockPosition);
	}

	public static String getPlayerPosition(IPlayer player) {
		return "Position: <" + DECIMAL_FORMAT.format(player.getPosition().x) +
				", " + DECIMAL_FORMAT.format(player.getPosition().y) +
				", " + DECIMAL_FORMAT.format(player.getPosition().z) + ">";
	}

	public static String getPlayerRotation(IPlayer player) {
		return "Rotation: <" + DECIMAL_FORMAT.format(player.getTransform().getRotation().x) +
				", " + DECIMAL_FORMAT.format(player.getTransform().getRotation().y) +
				", " + DECIMAL_FORMAT.format(player.getTransform().getRotation().z) + ">";
	}

	public static String getRenderMode() {
		return "Render Mode: " +
		switch (Renderer.get().getRenderMode()) {
			case 0 -> "[Cull Back], [Fill]";
			case 1 -> "[Cull Back], [Wireframe]";
			default -> "[No Cull], [Fill]";
		};
	}

	public static String getPlayerTargetBlockInfo(TestPlayer player) {

		int targetBlockHealth = 0;
		int targetBlock = Block.NULL;
		if (player.getTargetBlock() != null) {
			BlockState blockState = (player.getWorld().getBlockState(
					player.getTargetBlock().getBlockX(),
					player.getTargetBlock().getBlockY(),
					player.getTargetBlock().getBlockZ()
			));

			if (blockState != null) {
				targetBlockHealth = (int) (blockState.getHealth() * 100f);
				targetBlock = blockState.getBlock();
			}
		}

		if (player.getTargetBlock() == null) {
			return "Targeted Block: NULL";
		} else {
			return "Targeted Block: " + targetBlock + " : <" +
					player.getTargetBlock().getBlockX() + ", " +
					player.getTargetBlock().getBlockY() + ", " +
					player.getTargetBlock().getBlockZ() + ">" +
					"\nTarget Block Health: " + targetBlockHealth;
		}

	}
}
