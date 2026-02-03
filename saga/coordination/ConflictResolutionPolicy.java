package saga.coordination;

import saga.state.sstore.BoundaryVertexInfo;

import java.io.Serializable;

/**
 * Defines a policy for resolving conflicts on boundary vertices.
 *
 * Conflict resolution is invoked when multiple partitions
 * attempt to commit incompatible updates to the same
 * replicated vertex.
 */
public interface ConflictResolutionPolicy extends Serializable {

    /**
     * Decide whether the local update should be accepted.
     *
     * @param boundaryInfo metadata describing the boundary vertex
     * @param localPartitionId identifier of the local partition
     * @return true if the local update should be accepted, false otherwise
     */
    boolean accept(BoundaryVertexInfo boundaryInfo, int localPartitionId);
}
