package saga.algorithms.mm;

import saga.state.vertex.AlgorithmState;
import saga.state.vertex.VertexId;

/**
 * Algorithm-specific state for Maximal Matching.
 *
 * Each vertex tracks whether it is currently matched
 * and the identifier of its matched partner, if any.
 */
public final class MMState implements AlgorithmState {

    private static final long serialVersionUID = 1L;

    /**
     * Indicates whether this vertex is currently matched.
     */
    private boolean matched;

    /**
     * Identifier of the matched partner, if matched.
     */
    private VertexId matchedPartner;

    public MMState() {
        this.matched = false;
        this.matchedPartner = null;
    }

    public boolean isMatched() {
        return matched;
    }

    public VertexId getMatchedPartner() {
        return matchedPartner;
    }

    public void matchWith(VertexId partner) {
        this.matched = true;
        this.matchedPartner = partner;
    }

    public void unmatch() {
        this.matched = false;
        this.matchedPartner = null;
    }

    @Override
    public String toString() {
        return "MMState{" +
                "matched=" + matched +
                ", matchedPartner=" + matchedPartner +
                '}';
    }
}
