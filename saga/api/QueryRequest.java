package saga.api;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a read-only query over the current graph state.
 *
 * Queries are processed against live vertex state and are
 * decoupled from update execution. They do not modify
 * the graph or algorithm-specific state.
 */
public final class QueryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private final AlgorithmType algorithm;
    private final long vertexId;

    /**
     * Create a new query request.
     *
     * @param algorithm algorithm to query (MIS, GC, MM)
     * @param vertexId  vertex identifier
     */
    public QueryRequest(AlgorithmType algorithm, long vertexId) {
        this.algorithm = Objects.requireNonNull(
                algorithm, "Algorithm type cannot be null"
        );
        this.vertexId = vertexId;
    }

    public AlgorithmType getAlgorithm() {
        return algorithm;
    }

    public long getVertexId() {
        return vertexId;
    }

    @Override
    public String toString() {
        return "QueryRequest{" +
                "algorithm=" + algorithm +
                ", vertexId=" + vertexId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QueryRequest)) return false;
        QueryRequest that = (QueryRequest) o;
        return vertexId == that.vertexId &&
               algorithm == that.algorithm;
    }

    @Override
    public int hashCode() {
        return Objects.hash(algorithm, vertexId);
    }
}
