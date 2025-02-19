package wins.insomnia.backyardrocketry.physics;

import org.joml.Vector3d;
import wins.insomnia.backyardrocketry.entity.IBoundingBoxEntity;

public class BoundingBoxRaycastResult {

	private BoundingBox collidingBoundingBox;
	private double[] collisionPoint;
	private IBoundingBoxEntity entity;

	public BoundingBoxRaycastResult() {
		collidingBoundingBox = null;
		collisionPoint = new double[3];
		entity = null;
	}

	public boolean isColliding() {
		return collidingBoundingBox != null;
	}

	public BoundingBox getCollidingBoundingBox() {
		return collidingBoundingBox;
	}

	public Vector3d getCollisionPoint() {
		return new Vector3d(collisionPoint[0], collisionPoint[1], collisionPoint[2]);
	}

	public void setEntity(IBoundingBoxEntity entity) {
		this.entity = entity;
	}

	public IBoundingBoxEntity getEntity() {
		return entity;
	}

	public void setCollidingBoundingBox(BoundingBox boundingBox) {
		collidingBoundingBox = boundingBox;
	}

	public void setCollisionPoint(double x, double y, double z) {
		collisionPoint[0] = x;
		collisionPoint[1] = y;
		collisionPoint[2] = z;
	}

	public boolean hasEntity() {
		return entity != null;
	}

}
