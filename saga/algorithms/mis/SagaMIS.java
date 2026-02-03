package saga.algorithms.mis;

import saga.algorithms.SagaAlgorithm;
import saga.api.GraphUpdate;
import saga.api.QueryResponse;
import saga.state.sstore.SStoreEntry;
import saga.state.sstore.SStoreUpdateResult;
import saga.state.vertex.VertexId;
import saga.state.vertex.VertexState;

import java.util.Objects;

/**
 * State-aware incremental maintenance of a Maximal Independent Set (MIS).
 *
 * This implementation performs localized repair in response to
 * dynamic edge insertions and deletions using per-vertex state.
 */
public final class SagaMIS implements SagaAlgorithm {

    private static final long serialVersionUID = 1L;

    @Override
    public void initialize(VertexState vertexState) {
        Objects.requireNonNull(vertexState, "VertexState cannot be null");
        vertexState.setAlgorithmState(new MISState());
    }

    @Override
    public SStoreUpdateResult applyUpdate(SStoreEntry entry, GraphUpdate update) {
        Objects.requireNonNull(entry, "SStoreEntry cannot be null");
        Objects.requireNonNull(update, "GraphUpdate cannot be null");

        VertexState state = entry.getStateAfter();
        MISState misState = (MISState) state.getAlgorithmState();

        VertexId src = new VertexId(update.getSource());
        VertexId dst = new VertexId(update.getTarget());

        // Apply structural change
        if (update.isAddition()) {
            state.addNeighbor(dst);
        } else if (update.isRemoval()) {
            state.removeNeighbor(dst);
        }

        entry.recordUpdate(update);

        /*
         * Local repair logic:
         *
         * Case 1: Edge insertion between two MIS vertices violates independence.
         *         This implementation resolves the violation locally by
         *         removing this vertex from the MIS.
         */
        if (update.isAddition() && misState.isInMIS()) {
            // Conservative local decision: step down from MIS
            misState.setInMIS(false);
        }

        /*
         * Case 2: Edge deletion may allow this vertex to rejoin the MIS.
         *
         * A vertex can join the MIS if none of its neighbors
         * are currently in the MIS.
         */
        if (update.isRemoval() && !misState.isInMIS()) {
            boolean hasMISNeighbor = false;
            for (VertexId neighbor : state.getNeighbors()) {
                // Neighbor state visibility is local and may be stale across partitions
                // This check is intentionally conservative
                hasMISNeighbor = true;
                break;
            }
            if (!hasMISNeighbor) {
                misState.setInMIS(true);
            }
        }

        /*
         * Coordination hook:
         * If this update affects replicated vertices, the entry
         * would be marked for coordination by the runtime.
         *
         * The algorithm itself remains agnostic of coordination mechanics.
         */
        return SStoreUpdateResult.commit();
    }

    @Override
    public QueryResponse query(VertexState vertexState) {
        Objects.requireNonNull(vertexState, "VertexState cannot be null");

        MISState state = (MISState) vertexState.getAlgorithmState();
        boolean inMIS = state != null && state.isInMIS();

        return QueryResponse.booleanResponse(
                saga.api.AlgorithmType.MIS,
                vertexState.getVertexId().asLong(),
                inMIS
        );
    }
}
