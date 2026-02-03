package saga.coordination;

import saga.state.sstore.BoundaryVertexInfo;

import java.util.Objects;

/**
 * Conflict resolution policy based on partition priority.
 *
 * Updates originating from lower-numbered partitions
 * are preferred over those from higher-numbered partitions.
 */
public final class PartitionPriorityPolicy implements ConflictResolutionPolicy {

    private static final long serialVersionUID = 1L;

    @Override
    public boolean accept(BoundaryVertexInfo boundaryInfo, int localPartitionId) {
        Objects.requireNonNull(boundaryInfo, "BoundaryVertexInfo cannot be null");

        /*
         * Accept the update if this partition has the smallest
         * identifier among all replicas.
         */
        for (int replicaPartition : boundaryInfo.getReplicaPartitions()) {
            if (replicaPartition < localPartitionId) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "PartitionPriorityPolicy";
    }
}
