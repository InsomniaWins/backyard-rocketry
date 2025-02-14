package wins.insomnia.backyardrocketry.world;

import org.joml.Vector3i;

import java.util.Objects;

public class ChunkPosition {

    private int x;
    private int y;
    private int z;
    private int hashCode;

    private final boolean CAN_MODIFY;

    public ChunkPosition(ChunkPosition other) {
        this.x = other.getX();
        this.y = other.getY();
        this.z = other.getZ();
        this.hashCode = other.hashCode;
        CAN_MODIFY = other.CAN_MODIFY;
    }


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

    public ChunkPosition add(int x, int y, int z) {

        if (!CAN_MODIFY) {
            System.out.println("Tried modifying unmodifiable chunk-position: " + this);
            return this;
        }

        this.x += x;
        this.y += y;
        this.z += z;
        return this;
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

    public int getBlockX() {
        return x * Chunk.SIZE_X;
    }

    public int getBlockY() {
        return y * Chunk.SIZE_Y;
    }

    public int getBlockZ() {
        return z * Chunk.SIZE_Z;
    }

    public static int getBlockX(int chunkX) {
        return chunkX * Chunk.SIZE_X;
    }

    public static int getBlockY(int chunkY) {
        return chunkY * Chunk.SIZE_Y;
    }

    public static int getBlockZ(int chunkZ) {
        return chunkZ * Chunk.SIZE_Z;
    }

    public Vector3i getVector() {
        return new Vector3i(x, y, z);
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
        generateHashCode();
        return hashCode;
    }

    @Override
    public String toString() {
        return "ChunkPosition: {" + x + ", " + y + ", " + z + "}";
    }
}
