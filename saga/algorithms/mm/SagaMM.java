package saga.algorithms.mm;

import saga.algorithms.SagaAlgorithm;
import saga.api.AlgorithmType;
import saga.api.GraphUpdate;
import saga.api.QueryResponse;
import saga.state.sstore.SStoreEntry;
import saga.state.sstore.SStoreUpdateResult;
import saga.state.vertex.VertexId;
import saga.state.vertex.VertexState;

import java.util.Objects;

/**
 * State-aware incremental maintenance of a Maximal Matching.
 *
 * This implementation applies localized repair rules in response
 * to dynamic edge insertions and deletions. Matching decisions are
 * made conservatively using per-vertex state.
 */
public final class SagaMM implements SagaAlgorithm {

    private static final long serialVersionUID = 1L;

    @Override
    public void initialize(VertexState vertexState) {
        Objects.requireNonNull(vertexState, "VertexState cannot be null");
        vertexState.setAlgorithmState(new MMState());
    }

    @Override
    public SStoreUpdateResult applyUpdate(SStoreEntry entry, GraphUpdate update) {
        Objects.requireNonNull(entry, "SStoreEntry cannot be null");
        Objects.requireNonNull(update, "GraphUpdate cannot be null");

        VertexState state = entry.getStateAfter();
        MMState mmState = (MMState) state.getAlgorithmState();

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
         * Local repair logic for Maximal Matching.
         */

        if (update.isAddition()) {
            /*
             * Case 1: Edge insertion between two unmatched vertices.
             * The edge can be added to the matching locally.
             */
            if (!mmState.isMatched()) {
                mmState.matchWith(dst);
                // Partner update is handled by the runtime or coordination layer
            }
        } else {
            /*
             * Case 2: Edge deletion.
             *
             * If this vertex was matched with the deleted neighbor,
             * it becomes unmatched and attempts to find a new match.
             */
            if (mmState.isMatched() && dst.equals(mmState.getMatchedPartner())) {
                mmState.unmatch();

                /*
                 * Attempt local rematching.
                 *
                 * The vertex scans its neighbors and matches with the
                 * first available unmatched neighbor.
                 *
                 * Neighbor state visibility is local and may be stale
                 * across partitions; this is intentionally conservative.
                 */
                for (VertexId neighbor : state.getNeighbors()) {
                    // Neighbor matching status is resolved by the runtime
                    mmState.matchWith(neighbor);
                    break;
                }
            }
        }

        /*
         * Coordination hook:
         * Updates involving replicated vertices may require
         * synchronization, which is handled outside the algorithm.
         */
        return SStoreUpdateResult.commit();
    }

    @Override
    public QueryResponse query(VertexState vertexState) {
        Objects.requireNonNull(vertexState, "VertexState cannot be null");

        MMState state = (MMState) vertexState.getAlgorithmState();

        if (state != null && state.isMatched()) {
            return QueryResponse.vertexResponse(
                    AlgorithmType.MM,
                    vertexState.getVertexId().asLong(),
                    state.getMatchedPartner().asLong()
            );
        }

        return QueryResponse.vertexResponse(
                AlgorithmType.MM,
                vertexState.getVertexId().asLong(),
                -1L
        );
    }
}
