package wins.insomnia.backyardrocketry.render.texture;

import org.joml.Vector2i;
import wins.insomnia.backyardrocketry.util.update.Updater;

import java.util.HashMap;

public class BlockAtlasTexture extends Texture {

	public static final float BLOCK_SCALE_ON_ATLAS = 16f / 512f;
	public static final int BLOCK_AMOUNT = 28;
	private final HashMap<String, BlockTexture> BLOCK_TEXTURE_MAP;

	public BlockAtlasTexture() {
		super("block_atlas.png");

		BLOCK_TEXTURE_MAP = new HashMap<>();
		registerBlockTexture("cobblestone", new BlockTextureFrames(new Vector2i(0, 0)));
		registerBlockTexture("stone", new BlockTextureFrames(new Vector2i(1, 0)));
		registerBlockTexture("dirt", new BlockTextureFrames(new Vector2i(2, 0)));
		registerBlockTexture("grass_bottom", new BlockTextureFrames(new Vector2i(3, 0)));
		registerBlockTexture("grass_top", new BlockTextureFrames(new Vector2i(4, 0)));
		registerBlockTexture("log_top", new BlockTextureFrames(new Vector2i(0, 1)));
		registerBlockTexture("log_side", new BlockTextureFrames(new Vector2i(1, 1)));
		registerBlockTexture("leaves", new BlockTextureFrames(new Vector2i(2, 1)));
		registerBlockTexture("wooden_planks", new BlockTextureFrames(new Vector2i(3, 1)));
		registerBlockTexture("glass", new BlockTextureFrames(new Vector2i(4, 1)));
		registerBlockTexture("bricks", new BlockTextureFrames(new Vector2i(5, 1)));
		registerBlockTexture("wood", new BlockTextureFrames(new Vector2i(0, 2)));
		registerBlockTexture("water", new BlockTextureFrames(
				BlockTextureFrames.toTicksPerFrame(1),
				4,
				new Vector2i(6, 0))
		);

	}

	private void registerBlockTexture(String textureName, BlockTextureFrames textureFrames) {
		BLOCK_TEXTURE_MAP.put(textureName, new BlockTexture(textureName, textureFrames));
	}

	public BlockTexture getBlockTexture(String textureName) {
		return BLOCK_TEXTURE_MAP.get(textureName);
	}


	public static int[] getBlockAtlasCoordinates(String blockTextureName) {

		BlockTexture blockTexture = get().getBlockTexture(blockTextureName);

		if (blockTexture == null) return new int[] {0, 0};

		return blockTexture.getFrames().getFramePosition(0);
	}


	public static BlockAtlasTexture get() {
		return (BlockAtlasTexture) TextureManager.getTexture("block_atlas");
	}


	public static class BlockTexture {

		private final String NAME;
		private final BlockTextureFrames FRAMES;

		public BlockTexture(String textureName, BlockTextureFrames frames) {
			NAME = textureName;
			FRAMES = frames;
		}

		public String getName() {
			return NAME;
		}

		public BlockTextureFrames getFrames() {
			return FRAMES;
		}

	}

	public static class BlockTextureFrames {

		private final int TICKS_PER_FRAME;
		private final int FRAME_AMOUNT;
		private final int[] FRAME_POSITION;

		public BlockTextureFrames(Vector2i framePosition) {
			this(0, 1, framePosition);
		}

		public BlockTextureFrames(int ticksPerFrame, int frameAmount, Vector2i framePosition) {

			TICKS_PER_FRAME = ticksPerFrame;
			FRAME_AMOUNT = frameAmount;

			FRAME_POSITION = new int[FRAME_AMOUNT * 2];

			FRAME_POSITION[0] = framePosition.x;
			FRAME_POSITION[1] = framePosition.y;

		}

		public int[] getFramePosition(int frameIndex) {
			int[] frame = new int[2];

			frame[0] = FRAME_POSITION[0];
			frame[1] = FRAME_POSITION[1];

			for (int i = 0; i < frameIndex; i++) {
				frame[0] += 1;

				if (frame[0] == BLOCK_AMOUNT) {
					frame[0] = 0;
					frame[1] += 1;
				}
			}

			return frame;
		}

		public int[] getCurrentFramePosition() {
			return getFramePosition(getCurrentFrameIndex());
		}

		public int getCurrentFrameIndex() {

			double currentTime = Updater.getCurrentTime();
			double fps = getFramesPerSecond();

			double frame = fps * currentTime;

			return ((int) frame) % getFrameAmount();

		}

		public int getFrameAmount() {
			return FRAME_AMOUNT;
		}

		public double getFramesPerSecond() {
			return Updater.FIXED_UPDATES_PER_SECOND / (double) TICKS_PER_FRAME;
		}

		public int getTicksPerFrame() {
			return TICKS_PER_FRAME;
		}

		public static int toTicksPerFrame(float fps) {

			return (int) (Updater.FIXED_UPDATES_PER_SECOND / fps);

		}

	}


}
