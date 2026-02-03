package saga.algorithms;

import saga.api.GraphUpdate;
import saga.api.QueryResponse;
import saga.state.sstore.SStoreEntry;
import saga.state.sstore.SStoreUpdateResult;
import saga.state.vertex.VertexState;

import java.io.Serializable;

/**
 * Common interface for state-aware graph algorithms in SAGA.
 *
 * Implementations of this interface define how a particular
 * combinatorial property is incrementally maintained under
 * dynamic graph updates.
 */
public interface SagaAlgorithm extends Serializable {

    /**
     * Initialize algorithm-specific state for a vertex.
     *
     * This method is invoked when a vertex is first created
     * or when an algorithm is selected for execution.
     *
     * @param vertexState logical vertex state
     */
    void initialize(VertexState vertexState);

    /**
     * Apply a graph update within the context of an S-Store entry.
     *
     * @param entry  state-aware working context
     * @param update graph update to process
     * @return result indicating commit or abort
     */
    SStoreUpdateResult applyUpdate(SStoreEntry entry, GraphUpdate update);

    /**
     * Answer a read-only query from the current vertex state.
     *
     * This method must not mutate any state.
     *
     * @param vertexState authoritative vertex state
     * @return query response
     */
    QueryResponse query(VertexState vertexState);
}
