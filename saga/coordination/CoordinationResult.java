package saga.coordination;

import java.io.Serializable;

/**
 * Represents the result of a coordination decision.
 *
 * Coordination is required when updates affect boundary
 * vertices replicated across partitions.
 */
public final class CoordinationResult implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Status {
        COMMIT,
        ABORT
    }

    private final Status status;
    private final String reason;

    private CoordinationResult(Status status, String reason) {
        this.status = status;
        this.reason = reason;
    }

    /**
     * Create a successful coordination result.
     */
    public static CoordinationResult commit() {
        return new CoordinationResult(Status.COMMIT, null);
    }

    /**
     * Create an aborted coordination result.
     *
     * @param reason explanation for aborting
     */
    public static CoordinationResult abort(String reason) {
        return new CoordinationResult(Status.ABORT, reason);
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

    public String getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return "CoordinationResult{" +
                "status=" + status +
                (reason != null ? ", reason='" + reason + '\'' : "") +
                '}';
    }
}
