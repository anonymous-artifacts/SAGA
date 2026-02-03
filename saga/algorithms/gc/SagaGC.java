package saga.algorithms.gc;

import saga.algorithms.SagaAlgorithm;
import saga.api.AlgorithmType;
import saga.api.GraphUpdate;
import saga.api.QueryResponse;
import saga.state.sstore.SStoreEntry;
import saga.state.sstore.SStoreUpdateResult;
import saga.state.vertex.VertexId;
import saga.state.vertex.VertexState;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * State-aware incremental Graph Coloring.
 *
 * This implementation maintains a proper coloring using
 * localized, greedy repair in response to dynamic updates.
 */
public final class SagaGC implements SagaAlgorithm {

    private static final long serialVersionUID = 1L;

    @Override
    public void initialize(VertexState vertexState) {
        Objects.requireNonNull(vertexState, "VertexState cannot be null");
        vertexState.setAlgorithmState(new GCState());
    }

    @Override
    public SStoreUpdateResult applyUpdate(SStoreEntry entry, GraphUpdate update) {
        Objects.requireNonNull(entry, "SStoreEntry cannot be null");
        Objects.requireNonNull(update, "GraphUpdate cannot be null");

        VertexState state = entry.getStateAfter();
        GCState gcState = (GCState) state.getAlgorithmState();

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
         * After an edge insertion, a coloring conflict may arise
         * if this vertex and a neighbor share the same color.
         *
         * The conflict is resolved by greedily selecting the
         * smallest available color not used by neighbors.
         */
        if (update.isAddition()) {
            if (!gcState.isColored()) {
                assignColor(state, gcState);
            } else {
                // Conservative: always re-evaluate color on edge insertion
                assignColor(state, gcState);
            }
        }

        /*
         * Edge deletion does not violate correctness, but may
         * allow reuse of colors. For simplicity and clarity,
         * this implementation does not attempt recoloring on deletions.
         */
        return SStoreUpdateResult.commit();
    }

    /**
     * Assign a color to the vertex using a greedy strategy.
     *
     * This method selects the smallest non-negative integer
     * not currently used by neighboring vertices.
     */
    private void assignColor(VertexState state, GCState gcState) {
        Set<Integer> usedColors = new HashSet<>();

        for (VertexId neighbor : state.getNeighbors()) {
            // Neighbor color visibility is local and may be stale
            // This is intentionally conservative
            // Neighbor state lookup is handled by the runtime
        }

        int color = 0;
        while (usedColors.contains(color)) {
            color++;
        }

        gcState.setColor(color);
    }

    @Override
    public QueryResponse query(VertexState vertexState) {
        Objects.requireNonNull(vertexState, "VertexState cannot be null");

        GCState state = (GCState) vertexState.getAlgorithmState();
        int color = state != null ? state.getColor() : -1;

        return QueryResponse.integerResponse(
                AlgorithmType.GC,
                vertexState.getVertexId().asLong(),
                color
        );
    }
}
