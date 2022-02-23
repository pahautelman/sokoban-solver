import java.util.Objects;

class Position {
    private int x;
    private int y;

    Position(int x, int y) {
        this.x = x; this.y = y;
    }

    public int x() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int y() {
        return this.y;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.x, this.y);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof Position) {
            var other = (Position) obj;
            return this.x == other.x() && this.y == other.y();
        }
        return false;
    }
}