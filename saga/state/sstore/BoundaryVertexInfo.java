package saga.state.sstore;

import saga.state.vertex.VertexId;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Metadata describing a boundary (replicated) vertex.
 *
 * Boundary vertices are those whose state may be mirrored
 * across multiple partitions. Updates affecting such vertices
 * may require coordination before committing.
 */
public final class BoundaryVertexInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private final VertexId vertexId;

    /**
     * Identifiers of partitions that hold replicas of this vertex.
     */
    private final Set<Integer> replicaPartitions;

    public BoundaryVertexInfo(VertexId vertexId) {
        this.vertexId = Objects.requireNonNull(
                vertexId, "VertexId cannot be null"
        );
        this.replicaPartitions = new HashSet<>();
    }

    public VertexId getVertexId() {
        return vertexId;
    }

    /**
     * Register a replica partition for this vertex.
     *
     * @param partitionId identifier of the partition
     */
    public void addReplicaPartition(int partitionId) {
        replicaPartitions.add(partitionId);
    }

    /**
     * Remove a replica partition.
     */
    public void removeReplicaPartition(int partitionId) {
        replicaPartitions.remove(partitionId);
    }

    /**
     * Returns true if this vertex is replicated.
     */
    public boolean isBoundaryVertex() {
        return !replicaPartitions.isEmpty();
    }

    /**
     * Returns an unmodifiable view of replica partitions.
     */
    public Set<Integer> getReplicaPartitions() {
        return Collections.unmodifiableSet(replicaPartitions);
    }

    @Override
    public String toString() {
        return "BoundaryVertexInfo{" +
                "vertexId=" + vertexId +
                ", replicas=" + replicaPartitions +
                '}';
    }
}
