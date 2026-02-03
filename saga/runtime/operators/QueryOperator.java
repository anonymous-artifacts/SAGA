package saga.runtime.operators;

import saga.algorithms.SagaAlgorithm;
import saga.algorithms.gc.SagaGC;
import saga.algorithms.mis.SagaMIS;
import saga.algorithms.mm.SagaMM;
import saga.api.AlgorithmType;
import saga.api.QueryRequest;
import saga.api.QueryResponse;
import saga.state.vertex.VertexId;
import saga.state.vertex.VertexState;

import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

import java.util.Objects;

/**
 * Flink operator that serves read-only queries over live vertex state.
 *
 * Queries are processed without blocking update execution and
 * do not modify any state.
 */
public final class QueryOperator
        extends KeyedProcessFunction<Long, String, QueryResponse> {

    private static final long serialVersionUID = 1L;

    private final AlgorithmType algorithmType;
    private transient SagaAlgorithm algorithm;

    private transient ValueState<VertexState> vertexState;

    public QueryOperator(AlgorithmType algorithmType) {
        this.algorithmType = Objects.requireNonNull(
                algorithmType, "AlgorithmType cannot be null"
        );
    }

    @Override
    public void open(org.apache.flink.configuration.Configuration parameters) {

        ValueStateDescriptor<VertexState> descriptor =
                new ValueStateDescriptor<>(
                        "vertexState",
                        VertexState.class
                );

        this.vertexState = getRuntimeContext().getState(descriptor);
        this.algorithm = createAlgorithm(algorithmType);
    }

    @Override
    public void processElement(String value,
                               Context context,
                               Collector<QueryResponse> out) throws Exception {

        QueryRequest request = parseQuery(value);

        VertexId vertexId = new VertexId(request.getVertexId());

        VertexState state = vertexState.value();
        if (state == null) {
            return;
        }

        QueryResponse response = algorithm.query(state);
        out.collect(response);
    }

    /**
     * Instantiate the selected algorithm.
     */
    private SagaAlgorithm createAlgorithm(AlgorithmType type) {
        switch (type) {
            case MIS:
                return new SagaMIS();
            case GC:
                return new SagaGC();
            case MM:
                return new SagaMM();
            default:
                throw new IllegalArgumentException(
                        "Unsupported algorithm: " + type
                );
        }
    }

    /**
     * Parse a query request from input text.
     *
     * Expected format:
     *   QUERY vertexId
     */
    private QueryRequest parseQuery(String line) {
        String[] parts = line.trim().split("\\s+");
        if (parts.length != 2 || !parts[0].equalsIgnoreCase("QUERY")) {
            throw new IllegalArgumentException(
                    "Invalid query format: " + line
            );
        }

        long vertexId = Long.parseLong(parts[1]);
        return new QueryRequest(algorithmType, vertexId);
    }
}
