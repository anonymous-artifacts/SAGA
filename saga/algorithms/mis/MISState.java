package saga.algorithms.mis;

import saga.state.vertex.AlgorithmState;

/**
 * Algorithm-specific state for Maximal Independent Set (MIS).
 *
 * Each vertex stores whether it currently belongs to the MIS.
 * All decisions are made using local neighborhood information.
 */
public final class MISState implements AlgorithmState {

    private static final long serialVersionUID = 1L;

    /**
     * Indicates whether this vertex is currently in the MIS.
     */
    private boolean inMIS;

    public MISState() {
        this.inMIS = false;
    }

    public boolean isInMIS() {
        return inMIS;
    }

    public void setInMIS(boolean inMIS) {
        this.inMIS = inMIS;
    }

    @Override
    public String toString() {
        return "MISState{" +
                "inMIS=" + inMIS +
                '}';
    }
}
