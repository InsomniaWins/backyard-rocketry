package wins.insomnia.backyardrocketry.world;

import java.util.Objects;

public class ChunkPosition {

    public final int X;
    public final int Y;
    public final int Z;
    private int hashCode;

    public ChunkPosition(int x, int y, int z) {
        this.X = x;
        this.Y = y;
        this.Z = z;
        hashCode = Objects.hash(x, y, z);
    }

    @Override
    public boolean equals(Object other) {

        if (other instanceof ChunkPosition chunkPosition) {

            return (X == chunkPosition.X && Y == chunkPosition.Y && Z == chunkPosition.Z);

        }

        return false;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return "ChunkPosition: {" + X + ", " + Y + ", " + Z + "}";
    }
}
