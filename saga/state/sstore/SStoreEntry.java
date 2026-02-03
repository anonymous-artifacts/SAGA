package saga.state.sstore;

import saga.api.GraphUpdate;
import saga.state.vertex.VertexId;
import saga.state.vertex.VertexState;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a state-aware local working context for a vertex.
 *
 * An SStoreEntry encapsulates the before/after state of a vertex
 * while processing one or more graph updates. Changes are applied
 * tentatively and committed only after coordination (if required).
 */
public final class SStoreEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    private final VertexId vertexId;

    /**
     * Snapshot of the authoritative state before applying updates.
     */
    private final VertexState stateBefore;

    /**
     * Working copy of the vertex state after applying updates.
     */
    private final VertexState stateAfter;

    /**
     * List of graph updates applied to this entry.
     */
    private final List<GraphUpdate> appliedUpdates;

    /**
     * Flag indicating whether this entry affects boundary vertices
     * and therefore requires coordination.
     */
    private boolean requiresCoordination;

    /**
     * Create a new S-Store entry for a vertex.
     *
     * @param vertexId     identifier of the vertex
     * @param stateBefore  authoritative vertex state before updates
     */
    public SStoreEntry(VertexId vertexId, VertexState stateBefore) {
        this.vertexId = Objects.requireNonNull(
                vertexId, "VertexId cannot be null"
        );
        this.stateBefore = Objects.requireNonNull(
                stateBefore, "StateBefore cannot be null"
        );

        /*
         * Create a shallow working copy.
         * Structural and algorithm-specific updates are applied
         * to stateAfter without mutating stateBefore.
         */
        this.stateAfter = new VertexState(stateBefore.getVertexId());
        this.stateAfter.setAlgorithmState(stateBefore.getAlgorithmState());

        stateBefore.getNeighbors()
                   .forEach(this.stateAfter::addNeighbor);

        this.appliedUpdates = new ArrayList<>();
        this.requiresCoordination = false;
    }

    public VertexId getVertexId() {
        return vertexId;
    }

    public VertexState getStateBefore() {
        return stateBefore;
    }

    public VertexState getStateAfter() {
        return stateAfter;
    }

    public List<GraphUpdate> getAppliedUpdates() {
        return appliedUpdates;
    }

    /**
     * Record an update applied to this entry.
     */
    public void recordUpdate(GraphUpdate update) {
        appliedUpdates.add(
                Objects.requireNonNull(update, "Update cannot be null")
        );
    }

    /**
     * Mark this entry as requiring coordination.
     *
     * This is typically invoked when an update affects
     * replicated or boundary vertices.
     */
    public void markRequiresCoordination() {
        this.requiresCoordination = true;
    }

    /**
     * Returns true if this entry requires coordination
     * before committing.
     */
    public boolean requiresCoordination() {
        return requiresCoordination;
    }

    @Override
    public String toString() {
        return "SStoreEntry{" +
                "vertexId=" + vertexId +
                ", updates=" + appliedUpdates.size() +
                ", requiresCoordination=" + requiresCoordination +
                '}';
    }
}
