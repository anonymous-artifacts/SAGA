package saga.state.sstore;

import java.io.Serializable;

/**
 * Represents the outcome of applying updates within an S-Store entry.
 *
 * This result is used by the runtime and coordination layer
 * to decide whether tentative changes should be committed
 * to authoritative vertex state or discarded.
 */
public final class SStoreUpdateResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Possible outcomes of update processing.
     */
    public enum Status {
        COMMIT,
        ABORT
    }

    private final Status status;
    private final String reason;

    private SStoreUpdateResult(Status status, String reason) {
        this.status = status;
        this.reason = reason;
    }

    /**
     * Create a commit result.
     */
    public static SStoreUpdateResult commit() {
        return new SStoreUpdateResult(Status.COMMIT, null);
    }

    /**
     * Create an abort result with a reason.
     *
     * @param reason explanation for aborting the update
     */
    public static SStoreUpdateResult abort(String reason) {
        return new SStoreUpdateResult(Status.ABORT, reason);
    }

    public Status getStatus() {
        return status;
    }

    public boolean isCommit() {
        return status == Status.COMMIT;
    }

    public boolean isAbort() {
        return status == Status.ABORT;
    }

    /**
     * Optional explanation for aborting an update.
     */
    public String getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return "SStoreUpdateResult{" +
                "status=" + status +
                (reason != null ? ", reason='" + reason + '\'' : "") +
                '}';
    }
}
