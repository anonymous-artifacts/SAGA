package saga.api;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a dynamic update to the graph.
 *
 * A GraphUpdate captures structural changes such as
 * edge insertions and deletions. These updates are
 * processed incrementally by SAGA to maintain
 * algorithm-specific graph properties.
 */
public final class GraphUpdate implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Type of graph update.
     */
    public enum UpdateType {
        EDGE_ADD,
        EDGE_REMOVE
    }

    private final UpdateType type;
    private final long source;
    private final long target;

    /**
     * Create a new graph update.
     *
     * @param type   update type (add or remove)
     * @param source source vertex identifier
     * @param target target vertex identifier
     */
    public GraphUpdate(UpdateType type, long source, long target) {
        this.type = Objects.requireNonNull(type, "Update type cannot be null");
        this.source = source;
        this.target = target;
    }

    public UpdateType getType() {
        return type;
    }

    public long getSource() {
        return source;
    }

    public long getTarget() {
        return target;
    }

    /**
     * Returns true if this update represents an insertion.
     */
    public boolean isAddition() {
        return type == UpdateType.EDGE_ADD;
    }

    /**
     * Returns true if this update represents a deletion.
     */
    public boolean isRemoval() {
        return type == UpdateType.EDGE_REMOVE;
    }

    @Override
    public String toString() {
        return "GraphUpdate{" +
                "type=" + type +
                ", source=" + source +
                ", target=" + target +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GraphUpdate)) return false;
        GraphUpdate that = (GraphUpdate) o;
        return source == that.source &&
               target == that.target &&
               type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, source, target);
    }
}
