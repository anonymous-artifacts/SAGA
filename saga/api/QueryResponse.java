package saga.api;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents the response to a read-only query.
 *
 * A QueryResponse encapsulates algorithm-specific information
 * retrieved from the current vertex state. The response is
 * immutable and does not affect ongoing update processing.
 */
public final class QueryResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private final AlgorithmType algorithm;
    private final long vertexId;

    /*
     * Algorithm-specific fields.
     * Only the relevant fields are populated depending on
     * the queried algorithm.
     */
    private final Boolean booleanResult;
    private final Integer integerResult;
    private final Long vertexResult;

    private QueryResponse(AlgorithmType algorithm,
                          long vertexId,
                          Boolean booleanResult,
                          Integer integerResult,
                          Long vertexResult) {
        this.algorithm = algorithm;
        this.vertexId = vertexId;
        this.booleanResult = booleanResult;
        this.integerResult = integerResult;
        this.vertexResult = vertexResult;
    }

    /**
     * Create a boolean-valued query response.
     *
     * Used for queries such as MIS membership or
     * matched/unmatched status.
     */
    public static QueryResponse booleanResponse(AlgorithmType algorithm,
                                                long vertexId,
                                                boolean value) {
        return new QueryResponse(algorithm, vertexId, value, null, null);
    }

    /**
     * Create an integer-valued query response.
     *
     * Used for queries such as vertex color.
     */
    public static QueryResponse integerResponse(AlgorithmType algorithm,
                                                long vertexId,
                                                int value) {
        return new QueryResponse(algorithm, vertexId, null, value, null);
    }

    /**
     * Create a vertex-valued query response.
     *
     * Used for queries such as matched partner.
     */
    public static QueryResponse vertexResponse(AlgorithmType algorithm,
                                               long vertexId,
                                               long value) {
        return new QueryResponse(algorithm, vertexId, null, null, value);
    }

    public AlgorithmType getAlgorithm() {
        return algorithm;
    }

    public long getVertexId() {
        return vertexId;
    }

    public Optional<Boolean> getBooleanResult() {
        return Optional.ofNullable(booleanResult);
    }

    public Optional<Integer> getIntegerResult() {
        return Optional.ofNullable(integerResult);
    }

    public Optional<Long> getVertexResult() {
        return Optional.ofNullable(vertexResult);
    }

    @Override
    public String toString() {
        return "QueryResponse{" +
                "algorithm=" + algorithm +
                ", vertexId=" + vertexId +
                ", booleanResult=" + booleanResult +
                ", integerResult=" + integerResult +
                ", vertexResult=" + vertexResult +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QueryResponse)) return false;
        QueryResponse that = (QueryResponse) o;
        return vertexId == that.vertexId &&
               algorithm == that.algorithm &&
               Objects.equals(booleanResult, that.booleanResult) &&
               Objects.equals(integerResult, that.integerResult) &&
               Objects.equals(vertexResult, that.vertexResult);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                algorithm, vertexId,
                booleanResult, integerResult, vertexResult
        );
    }
}
