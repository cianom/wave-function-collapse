package cianom.lib;

public enum Boundary {

    LEFT(-1, 0),
    DOWN(0, 1),
    RIGHT(1, 0),
    UP(0, -1);

    public final int x;
    public final int y;

    Boundary(final int x, final int y) {
        this.x = x;
        this.y = y;
    }

    public Boundary opposite() {
        switch (this) {
            case LEFT:
                return RIGHT;
            case DOWN:
                return UP;
            case RIGHT:
                return LEFT;
            case UP:
                return DOWN;
            default:
                throw new IllegalStateException("Unknown Boundary " + this);
        }
    }


}
