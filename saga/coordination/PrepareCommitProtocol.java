package saga.coordination;

import saga.state.sstore.BoundaryVertexInfo;

import java.io.Serializable;
import java.util.Objects;

/**
 * Lightweight prepare-commit protocol for coordinating
 * updates on boundary vertices.
 *
 * This protocol is invoked when multiple partitions may
 * attempt to modify replicated vertex state.
 */
public final class PrepareCommitProtocol implements Serializable {

    private static final long serialVersionUID = 1L;

    private final ConflictResolutionPolicy resolutionPolicy;

    public PrepareCommitProtocol(ConflictResolutionPolicy resolutionPolicy) {
        this.resolutionPolicy = Objects.requireNonNull(
                resolutionPolicy, "ConflictResolutionPolicy cannot be null"
        );
    }

    /**
     * Execute the prepare-commit decision for a boundary vertex update.
     *
     * @param boundaryInfo metadata for the boundary vertex
     * @param localPartitionId identifier of the local partition
     * @return coordination result indicating commit or abort
     */
    public CoordinationResult prepareAndDecide(BoundaryVertexInfo boundaryInfo,
                                               int localPartitionId) {
        Objects.requireNonNull(boundaryInfo, "BoundaryVertexInfo cannot be null");

        boolean accepted = resolutionPolicy.accept(
                boundaryInfo, localPartitionId
        );

        if (accepted) {
            return CoordinationResult.commit();
        } else {
            return CoordinationResult.abort(
                    "Rejected by conflict resolution policy"
            );
        }
    }

    @Override
    public String toString() {
        return "PrepareCommitProtocol{" +
                "policy=" + resolutionPolicy +
                '}';
    }
}
