package saga.runtime.flink;

import org.apache.flink.runtime.state.StateBackend;
import org.apache.flink.runtime.state.hashmap.HashMapStateBackend;
import org.apache.flink.runtime.state.storage.FileSystemCheckpointStorage;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.Objects;

/**
 * Factory for configuring Flink state backends used by SAGA.
 *
 * This class centralizes state backend configuration to make
 * execution semantics explicit and auditable.
 */
public final class FlinkStateBackendFactory {

    private FlinkStateBackendFactory() {
        // Utility class
    }

    /**
     * Configure the state backend and checkpoint storage.
     *
     * @param env Flink execution environment
     * @param checkpointDir directory for checkpoint storage
     */
    public static void configure(StreamExecutionEnvironment env,
                                 String checkpointDir) {

        Objects.requireNonNull(env, "StreamExecutionEnvironment cannot be null");

        StateBackend backend = new HashMapStateBackend();
        env.setStateBackend(backend);

        if (checkpointDir != null) {
            env.getCheckpointConfig().setCheckpointStorage(
                    new FileSystemCheckpointStorage(checkpointDir)
            );
        }
    }
}
