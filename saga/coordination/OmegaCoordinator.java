package saga.coordination;

import saga.state.sstore.BoundaryVertexInfo;
import saga.state.sstore.SStoreEntry;

import java.io.Serializable;
import java.util.Objects;

/**
 * Coordination engine (Ω) responsible for resolving conflicts
 * on boundary vertices.
 *
 * Ω is invoked by the runtime when a state-aware update affects
 * replicated vertex state and requires synchronization.
 */
public final class OmegaCoordinator implements Serializable {

    private static final long serialVersionUID = 1L;

    private final PrepareCommitProtocol protocol;

    public OmegaCoordinator(PrepareCommitProtocol protocol) {
        this.protocol = Objects.requireNonNull(
                protocol, "PrepareCommitProtocol cannot be null"
        );
    }

    /**
     * Coordinate an S-Store update entry.
     *
     * @param entry            state-aware update context
     * @param boundaryInfo     metadata for the boundary vertex
     * @param localPartitionId identifier of the local partition
     * @return coordination result
     */
    public CoordinationResult coordinate(SStoreEntry entry,
                                         BoundaryVertexInfo boundaryInfo,
                                         int localPartitionId) {

        Objects.requireNonNull(entry, "SStoreEntry cannot be null");
        Objects.requireNonNull(boundaryInfo, "BoundaryVertexInfo cannot be null");

        /*
         * If the entry does not require coordination, the update
         * can be committed immediately.
         */
        if (!entry.requiresCoordination()) {
            return CoordinationResult.commit();
        }

        /*
         * Execute prepare-commit protocol for boundary vertices.
         */
        return protocol.prepareAndDecide(
                boundaryInfo, localPartitionId
        );
    }

    @Override
    public String toString() {
        return "OmegaCoordinator{" +
                "protocol=" + protocol +
                '}';
    }
}
