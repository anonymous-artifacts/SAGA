package saga.runtime.operators;

import saga.algorithms.SagaAlgorithm;
import saga.algorithms.gc.SagaGC;
import saga.algorithms.mis.SagaMIS;
import saga.algorithms.mm.SagaMM;
import saga.api.AlgorithmType;
import saga.api.GraphUpdate;
import saga.coordination.OmegaCoordinator;
import saga.coordination.PartitionPriorityPolicy;
import saga.coordination.PrepareCommitProtocol;
import saga.coordination.CoordinationResult;
import saga.state.sstore.BoundaryVertexInfo;
import saga.state.sstore.SStoreEntry;
import saga.state.sstore.SStoreUpdateResult;
import saga.state.vertex.VertexId;
import saga.state.vertex.VertexState;

import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

import java.util.Objects;

/**
 * Flink operator that applies graph updates to per-vertex state
 * using state-aware SAGA algorithms.
 *
 * Each instance of this operator is keyed by vertex identifier
 * and maintains authoritative vertex state.
 */
public final class VertexUpdateOperator
        extends KeyedProcessFunction<Long, String, Void> {

    private static final long serialVersionUID = 1L;

    private final AlgorithmType algorithmType;
    private transient SagaAlgorithm algorithm;

    private transient ValueState<VertexState> vertexState;

    private transient OmegaCoordinator coordinator;

    public VertexUpdateOperator(AlgorithmType algorithmType) {
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

        this.coordinator = new OmegaCoordinator(
                new PrepareCommitProtocol(
                        new PartitionPriorityPolicy()
                )
        );
    }

    @Override
    public void processElement(String value,
                               Context context,
                               Collector<Void> out) throws Exception {

        GraphUpdate update = parseUpdate(value);

        VertexId vertexId = new VertexId(update.getSource());

        VertexState currentState = vertexState.value();
        if (currentState == null) {
            currentState = new VertexState(vertexId);
            algorithm.initialize(currentState);
        }

        /*
         * Create state-aware working context (S-Store entry).
         */
        SStoreEntry entry = new SStoreEntry(vertexId, currentState);

        /*
         * Apply algorithm-specific update logic.
         */
        SStoreUpdateResult localResult =
                algorithm.applyUpdate(entry, update);

        if (localResult.isAbort()) {
            return;
        }

        /*
         * Coordination step for boundary vertices.
         *
         * In this implementation, boundary detection is simplified
         * and driven by runtime configuration.
         */
        BoundaryVertexInfo boundaryInfo =
                new BoundaryVertexInfo(vertexId);

        CoordinationResult coordResult =
                coordinator.coordinate(
                        entry,
                        boundaryInfo,
                        getRuntimeContext().getIndexOfThisSubtask()
                );

        if (coordResult.isCommit()) {
            vertexState.update(entry.getStateAfter());
        }
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
     * Parse a graph update from input text.
     *
     * Expected format:
     *   ADD u v
     *   DEL u v
     */
    private GraphUpdate parseUpdate(String line) {
        String[] parts = line.trim().split("\\s+");
        if (parts.length != 3) {
            throw new IllegalArgumentException(
                    "Invalid update format: " + line
            );
        }

        GraphUpdate.UpdateType type =
                parts[0].equalsIgnoreCase("ADD")
                        ? GraphUpdate.UpdateType.EDGE_ADD
                        : GraphUpdate.UpdateType.EDGE_REMOVE;

        long src = Long.parseLong(parts[1]);
        long dst = Long.parseLong(parts[2]);

        return new GraphUpdate(type, src, dst);
    }
}
