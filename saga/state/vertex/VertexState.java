package saga.state.vertex;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents the authoritative logical state of a vertex.
 *
 * VertexState maintains structural information (adjacency)
 * and a reference to algorithm-specific state. It does not
 * encode transient or speculative update context; those are
 * handled by S-Store entries.
 */
public final class VertexState implements Serializable {

    private static final long serialVersionUID = 1L;

    private final VertexId vertexId;

    /**
     * Adjacency list of this vertex.
     *
     * This set contains the identifiers of neighboring vertices.
     * Structural updates modify this set incrementally.
     */
    private final Set<VertexId> neighbors;

    /**
     * Algorithm-specific state associated with this vertex.
     */
    private AlgorithmState algorithmState;

    /**
     * Create a new vertex state with no neighbors and no
     * algorithm-specific state attached.
     *
     * @param vertexId identifier of the vertex
     */
    public VertexState(VertexId vertexId) {
        this.vertexId = Objects.requireNonNull(
                vertexId, "VertexId cannot be null"
        );
        this.neighbors = new HashSet<>();
        this.algorithmState = null;
    }

    public VertexId getVertexId() {
        return vertexId;
    }

    /**
     * Returns an unmodifiable view of the adjacency list.
     */
    public Set<VertexId> getNeighbors() {
        return Collections.unmodifiableSet(neighbors);
    }

    /**
     * Add a neighbor to this vertex.
     *
     * @param neighbor neighboring vertex identifier
     * @return true if the neighbor was added, false if it already existed
     */
    public boolean addNeighbor(VertexId neighbor) {
        Objects.requireNonNull(neighbor, "Neighbor cannot be null");
        return neighbors.add(neighbor);
    }

    /**
     * Remove a neighbor from this vertex.
     *
     * @param neighbor neighboring vertex identifier
     * @return true if the neighbor was removed, false otherwise
     */
    public boolean removeNeighbor(VertexId neighbor) {
        Objects.requireNonNull(neighbor, "Neighbor cannot be null");
        return neighbors.remove(neighbor);
    }

    /**
     * Returns the algorithm-specific state associated with this vertex.
     */
    public AlgorithmState getAlgorithmState() {
        return algorithmState;
    }

    /**
     * Attach or replace algorithm-specific state.
     *
     * This method is typically invoked during initialization
     * or when switching algorithms.
     */
    public void setAlgorithmState(AlgorithmState algorithmState) {
        this.algorithmState = algorithmState;
    }

    @Override
    public String toString() {
        return "VertexState{" +
                "vertexId=" + vertexId +
                ", neighbors=" + neighbors.size() +
                ", algorithmState=" +
                (algorithmState != null
                        ? algorithmState.getClass().getSimpleName()
                        : "null") +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof VertexState)) return false;
        VertexState other = (VertexState) obj;
        return vertexId.equals(other.vertexId);
    }

    @Override
    public int hashCode() {
        return vertexId.hashCode();
    }
}
