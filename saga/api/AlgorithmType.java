package saga.api;

/**
 * Enumeration of graph algorithms supported by SAGA.
 *
 * This type is used by the runtime to select the appropriate
 * state-aware maintenance logic for incoming updates and queries.
 */
public enum AlgorithmType {

    /**
     * Maximal Independent Set maintenance.
     */
    MIS,

    /**
     * Graph Coloring maintenance.
     */
    GC,

    /**
     * Maximal Matching maintenance.
     */
    MM;

    /**
     * Parse an algorithm type from a string.
     *
     * This method is intended for use by configuration
     * and command-line argument parsing.
     *
     * @param value string representation of the algorithm
     * @return corresponding AlgorithmType
     * @throws IllegalArgumentException if the value is unknown
     */
    public static AlgorithmType fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Algorithm type cannot be null");
        }
        switch (value.toUpperCase()) {
            case "MIS":
                return MIS;
            case "GC":
                return GC;
            case "MM":
                return MM;
            default:
                throw new IllegalArgumentException(
                        "Unknown algorithm type: " + value +
                        ". Supported values are MIS, GC, MM."
                );
        }
    }
}
