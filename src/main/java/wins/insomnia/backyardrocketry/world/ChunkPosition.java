package wins.insomnia.backyardrocketry.world;

import java.util.Objects;

public class ChunkPosition {

    private int x;
    private int y;
    private int z;
    private int hashCode;

    private final boolean CAN_MODIFY;

    public ChunkPosition(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        generateHashCode();
        CAN_MODIFY = true;
    }

    private void generateHashCode() {
        hashCode = Objects.hash(x, y, z);
    }

    public ChunkPosition(int x, int y, int z, boolean modifiable) {
        this.x = x;
        this.y = y;
        this.z = z;
        hashCode = Objects.hash(x, y, z);
        CAN_MODIFY = modifiable;
    }

    public void set(int x, int y, int z) {
        if (!CAN_MODIFY) {
            throw new RuntimeException("Attempted to modify constant ChunkPosition!");
        }
        this.x = x;
        this.y = y;
        this.z = z;
        generateHashCode();
    }

    public void setX(int x) {
        if (!CAN_MODIFY) {
            throw new RuntimeException("Attempted to modify constant ChunkPosition!");
        }
        this.x = x;
        generateHashCode();
    }

    public void setY(int y) {
        if (!CAN_MODIFY) {
            throw new RuntimeException("Attempted to modify constant ChunkPosition!");
        }
        this.y = y;
        generateHashCode();
    }

    public void setZ(int z) {
        if (!CAN_MODIFY) {
            throw new RuntimeException("Attempted to modify constant ChunkPosition!");
        }
        this.z = z;
        generateHashCode();
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    @Override
    public boolean equals(Object other) {

        if (other instanceof ChunkPosition chunkPosition) {

            return (x == chunkPosition.x && y == chunkPosition.y && z == chunkPosition.z);

        }

        return false;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return "ChunkPosition: {" + x + ", " + y + ", " + z + "}";
    }
}
