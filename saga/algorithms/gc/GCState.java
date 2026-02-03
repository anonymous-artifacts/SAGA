package saga.algorithms.gc;

import saga.state.vertex.AlgorithmState;

/**
 * Algorithm-specific state for Graph Coloring.
 *
 * Each vertex maintains a single color assignment.
 * A value of -1 indicates that the vertex is currently uncolored.
 */
public final class GCState implements AlgorithmState {

    private static final long serialVersionUID = 1L;

    /**
     * Color assigned to this vertex.
     * A negative value indicates no color.
     */
    private int color;

    public GCState() {
        this.color = -1;
    }

    public int getColor() {
        return color;
    }

    public boolean isColored() {
        return color >= 0;
    }

    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return "GCState{" +
                "color=" + color +
                '}';
    }
}
