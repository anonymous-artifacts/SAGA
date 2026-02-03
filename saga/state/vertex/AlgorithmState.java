package saga.state.vertex;

import java.io.Serializable;

/**
 * Marker interface for algorithm-specific vertex state.
 *
 * Implementations of this interface capture the minimal
 * per-vertex information required by a particular
 * combinatorial algorithm.
 */
public interface AlgorithmState extends Serializable {
    // Marker interface
}
