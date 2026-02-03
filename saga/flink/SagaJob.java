package saga.runtime.flink;

import saga.api.AlgorithmType;
import saga.runtime.operators.VertexUpdateOperator;
import saga.runtime.operators.QueryOperator;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;

/**
 * Main Flink job for executing SAGA.
 *
 * This job wires together the update stream, query stream,
 * and state-aware operators to incrementally maintain
 * combinatorial graph properties.
 */
public final class SagaJob {

    private SagaJob() {
        // Utility class
    }

    public static void main(String[] args) throws Exception {

        ParameterTool params = ParameterTool.fromArgs(args);

        AlgorithmType algorithm = AlgorithmType.fromString(
                params.getRequired("algorithm")
        );

        String updatesPath = params.getRequired("updates");
        String queriesPath = params.get("queries", null);

        StreamExecutionEnvironment env =
                StreamExecutionEnvironment.getExecutionEnvironment();

        env.getConfig().setGlobalJobParameters(params);
        env.enableCheckpointing(10_000);

        /*
         * Build update stream.
         */
        DataStream<String> updateLines =
                env.readTextFile(updatesPath)
                   .assignTimestampsAndWatermarks(
                           WatermarkStrategy.noWatermarks()
                   );

        SingleOutputStreamOperator<?> updateStream =
                updateLines.process(
                        new VertexUpdateOperator(algorithm)
                );

        /*
         * Optional query stream.
         */
        if (queriesPath != null) {
            DataStream<String> queryLines =
                    env.readTextFile(queriesPath)
                       .assignTimestampsAndWatermarks(
                               WatermarkStrategy.noWatermarks()
                       );

            queryLines.process(
                    new QueryOperator(algorithm)
            );
        }

        env.execute("SAGA - State-Aware Graph Analytics");
    }
}
