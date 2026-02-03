package saga.state.vertex;

import java.io.Serializable;

/**
 * Strongly-typed identifier for a vertex in the graph.
 *
 * This abstraction avoids scattering raw primitive identifiers
 * across the codebase and provides a single point of control
 * for equality and hashing semantics.
 */
public final class VertexId implements Serializable {

    private static final long serialVersionUID = 1L;

    private final long id;

    public VertexId(long id) {
        this.id = id;
    }

    public long asLong() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof VertexId)) return false;
        VertexId other = (VertexId) obj;
        return this.id == other.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public String toString() {
        return "VertexId{" + id + '}';
    }
}
