package wins.insomnia.backyardrocketry.entity.component;

import org.joml.Math;
import wins.insomnia.backyardrocketry.entity.Entity;
import wins.insomnia.backyardrocketry.entity.IBoundingBoxEntity;
import wins.insomnia.backyardrocketry.physics.BoundingBox;
import wins.insomnia.backyardrocketry.physics.Collision;
import wins.insomnia.backyardrocketry.world.Chunk;
import wins.insomnia.backyardrocketry.world.World;

import java.util.ArrayList;
import java.util.List;

public class ComponentGenericVelocityMovement extends Component {

	private final Entity ENTITY;

	public ComponentGenericVelocityMovement(Entity entity) {
		ENTITY = entity;
	}

	public void move() {

		// get bounding boxes of blocks near entity

		boolean hasBoundingBox = ENTITY instanceof IBoundingBoxEntity;

		if (!hasBoundingBox) {
			ENTITY.getTransform().getPosition().add(ENTITY.getVelocity());
			return;
		}

		IBoundingBoxEntity boundingBoxEntity = (IBoundingBoxEntity) ENTITY;

		BoundingBox broadPhaseBoundingBox = new BoundingBox(boundingBoxEntity.getBoundingBox()).grow(ENTITY.getVelocity().length() * 2);

		List<Chunk> broadPhaseChunks = Collision. getChunksTouchingBoundingBox(World.getServerWorld(), broadPhaseBoundingBox);
		List<BoundingBox> blockBoundingBoxesNearPlayer = new ArrayList<>();

		if (!broadPhaseChunks.isEmpty()) {
			for (Chunk chunk : broadPhaseChunks) {
				List<BoundingBox> boundingBoxes = chunk.getBlockBoundingBoxes(broadPhaseBoundingBox);
				blockBoundingBoxesNearPlayer.addAll(boundingBoxes);
			}
		}



		// move and collide
		if (ENTITY.getVelocity().x != 0f) {
			for (BoundingBox boundingBox : blockBoundingBoxesNearPlayer) {
				ENTITY.getVelocity().x = boundingBox.collideX(boundingBoxEntity.getBoundingBox(), ENTITY.getVelocity().x);
			}

			ENTITY.getPosition().x += ENTITY.getVelocity().x;
		}

		if (ENTITY.getVelocity().y != 0f) {
			for (BoundingBox boundingBox : blockBoundingBoxesNearPlayer) {
				ENTITY.getVelocity().y = boundingBox.collideY(boundingBoxEntity.getBoundingBox(), ENTITY.getVelocity().y);
			}

			ENTITY.getPosition().y += ENTITY.getVelocity().y;
		}

		if (ENTITY.getVelocity().z != 0f) {
			for (BoundingBox boundingBox : blockBoundingBoxesNearPlayer) {
				ENTITY.getVelocity().z = boundingBox.collideZ(boundingBoxEntity.getBoundingBox(), ENTITY.getVelocity().z);
			}

			ENTITY.getPosition().z += ENTITY.getVelocity().z;
		}

	}

}
