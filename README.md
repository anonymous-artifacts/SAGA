# SAGA: State-Aware Graph Analytics on Dynamic Graphs

This repository contains the **research prototype implementation of SAGA (State-Aware Graph Analytics)**, a distributed framework for maintaining **combinatorial graph properties** on **dynamic graphs** under continuous updates and concurrent queries.

The implementation is **Flink-native** and is intended for **research evaluation and artifact review**, not as a production library.



## 1. Overview

SAGA is designed to incrementally maintain graph properties as the graph evolves, without global recomputation. The system emphasizes:

* **State-aware vertex management**
* **Localized, incremental repair**
* **Non-blocking query processing**
* **Streaming execution on Apache Flink**

The current implementation supports the following algorithms:

* **SAGA-MIS** — Maximal Independent Set
* **SAGA-GC** — Graph Coloring
* **SAGA-MM** — Maximal Matching

Each algorithm operates on **per-vertex state** and is integrated into a unified streaming runtime.



## 2. System Design (Code Perspective)

### Core Concepts

* **VertexState**
  Authoritative per-vertex logical state stored as Flink keyed state.

* **S-Store (State Store)**
  A state-aware working context that captures before/after vertex state during update processing.

* **Algorithms**
  MIS, GC, and MM are implemented as pluggable modules with minimal algorithm-specific state.

* **Ω (Coordination Engine)**
  A lightweight coordination layer used when updates affect boundary (replicated) vertices.

* **Flink Runtime**
  Apache Flink is used for streaming execution, fault tolerance, and state management.



## 3. Repository Structure

```text
saga/
├── api/                    # Update & query APIs
├── state/
│   ├── vertex/             # VertexState and algorithm state
│   └── sstore/             # State-aware working context
├── algorithms/
│   ├── mis/                # Maximal Independent Set
│   ├── gc/                 # Graph Coloring
│   └── mm/                 # Maximal Matching
├── runtime/
│   ├── flink/              # Flink job and state backend
│   └── operators/          # Flink operators
└── coordination/           # Ω coordination layer
```

The codebase is intentionally modular to allow reviewers to inspect algorithms, coordination, and runtime independently.



## 4. Build Requirements

### Software

* Java 17
* Apache Flink 1.17+ (tested with modern Flink releases)
* Maven 3.8+

### Hardware (recommended)

* Multi-core machine (≥16 cores recommended)
* ≥64 GB RAM for large graphs
* Distributed cluster recommended for scaling experiments



## 5. Building the Project

From the repository root:

```bash
mvn clean package
```

This produces a runnable JAR under:

```text
target/
```



## 6. Running SAGA

### 6.1 Input Format

#### Graph Updates

Each line represents a dynamic edge update:

```text
ADD u v
DEL u v
```

Where `u` and `v` are vertex identifiers.

#### Queries

Each line represents a read-only query:

```text
QUERY vertexId
```



### 6.2 Running the Flink Job

Example (Maximal Independent Set):

```bash
flink run \
  -c saga.runtime.flink.SagaJob \
  target/saga.jar \
  --algorithm MIS \
  --updates /path/to/updates.txt \
  --queries /path/to/queries.txt
```

Supported algorithm values:

* `MIS`
* `GC`
* `MM`

Queries are optional. If omitted, the system runs in update-only mode.



## 7. Datasets and Graph Preparation

SAGA is evaluated on a collection of large real-world graphs, referred to as **G1–G10**, which are treated as **dynamic graphs** by applying incremental update batches over an initial static base graph.



### 7.1 Graph Sources 

Table below summarizes the graphs used in evaluation. All graphs are publicly available benchmark datasets commonly used in large-scale graph processing and graph algorithm research.
| S. No. | Graph Name       | Vertices (|V|) | Edges (|E|) | Avg Degree (|E|/|V|) |
|--------|------------------|---------------:|------------:|--------------------:|
| G1  | citationCiteseer | 0.27M | 2.31M | 8.55 |
| G2  | amazon0601       | 0.40M | 4.89M | 12.22 |
| G3  | as-Skitter       | 1.69M | 22.19M | 13.14 |
| G4  | cit-Patents      | 3.77M | 33.04M | 8.80 |
| G5  | rmat22           | 4.19M | 65.66M | 15.67 |
| G6  | soc-LiveJournal1 | 4.85M | 85.70M | 17.67 |
| G7  | delaunay_n24     | 16.78M | 100.66M | 5.99 |
| G8  | kron_g500-logn21 | 2.09M | 182.08M | 87.11 |
| G9  | uk-2002          | 18.52M | 523.57M | 28.27 |
| G10 | com-Friendster   | 65.61M | 1.82B | 27.58 |


**Notes:**

* Graphs span **social, web, citation, and synthetic** categories.
* They cover a wide range of:

  * graph sizes,
  * degree distributions,
  * sparsity/density characteristics.
* This diversity is intentional to stress different aspects of SAGA’s design.

The collection includes both real-world graphs and synthetic generators (e.g., RMAT and Kronecker graphs) to evaluate behavior across diverse structural characteristics.

### 7.2 Data Cleaning and Preprocessing

Before graph construction, each dataset undergoes the following preprocessing steps:

1. **Removal of self-loops**
   Edges of the form `(u, u)` are discarded.

2. **Duplicate edge elimination**
   Multiple occurrences of the same edge are collapsed into a single edge.

3. **Undirected normalization**
   For directed inputs, edges are symmetrized so that `(u, v)` implies `(v, u)`.

4. **Vertex ID normalization**
   Vertex identifiers are remapped to a dense range `[0, n)` to improve memory locality.

These steps ensure that all graphs conform to a consistent structure suitable for combinatorial graph algorithms.



### 7.3 CSR Construction

Internally, SAGA represents graphs using the **Compressed Sparse Row (CSR)** format.

For each graph:

* an adjacency list is constructed after preprocessing,
* neighbors are stored contiguously in memory,
* an offset array indexes the start of each vertex’s adjacency.

Conceptually, the CSR representation consists of:

* a **row pointer array** of size `|V| + 1`, and
* a **column index array** of size `|E|`.

This representation:

* enables efficient neighbor traversal,
* improves cache locality,
* minimizes memory overhead,
* aligns with high-performance graph processing practices.

The CSR structure serves as the **initial base graph state** before dynamic updates are applied. 
While updates are provided as a stream of `ADD/DEL` operations, SAGA internally maintains vertex adjacency using CSR-derived structures that are incrementally updated during execution.

CSR is chosen to align with high-performance graph processing practices and to ensure efficient neighborhood access during incremental updates.



### 7.4 Dynamic Update Stream Generation

To model graph evolution, a subset of edges from each CSR graph is converted into a **stream of dynamic updates**.

Each update is encoded textually as:

```text
ADD u v
DEL u v
```

where `u` and `v` are vertex identifiers consistent with the CSR mapping.

Updates are applied incrementally on top of the base CSR graph during execution.



### 7.5 Update Batch Sizes

To evaluate scalability and responsiveness under varying workloads, update streams are divided into **batches of increasing size**.

The following batch sizes are used throughout the evaluation:

| Batch Size | Number of Updates |
| ---------- | ----------------- |
| 0.01K      | 10                |
| 0.1K       | 100               |
| 1K         | 1,000             |
| 10K        | 10,000            |
| 100K       | 100,000           |

Each batch is processed independently to measure:

* update processing time,
* throughput,
* query latency under concurrent updates.



### 7.6 Batch Generation Methodology

Given a preprocessed CSR graph, update batches are generated as follows:

1. Select a subset of edges from the CSR representation.
2. Randomly partition the selected edges into batches of the desired size.
3. Emit each batch as a sequence of `ADD` or `DEL` operations.

This approach:

* preserves the original graph topology,
* avoids synthetic graph generation,
* reflects realistic incremental graph evolution.



### 7.7 Queries

SAGA supports **read-only vertex queries** that can be issued during or after update processing.

#### Query Format

Each query is encoded as:

```text
QUERY vertexId
```

where `vertexId` refers to a vertex in the current graph.



#### Query Semantics

* Queries are **read-only**
* Queries **do not modify** graph or algorithm state
* Queries **do not block** update processing
* Queries are served directly from **live keyed vertex state**

Depending on the selected algorithm:

* **MIS**: query returns whether the vertex is in the independent set
* **GC**: query returns the current color of the vertex
* **MM**: query returns the matched partner (or −1 if unmatched)



#### Queries During Updates

Queries may be issued:

* concurrently with update batches, or
* after a batch has completed.

This allows evaluation of **query latency under active graph evolution**, which is a core design goal of SAGA.



#### Query Cost Model

Query latency depends on:

* keyed state access,
* Flink task scheduling,
* operator execution overhead.

No log replay, joins, or recomputation is required.



## 8.  Logging and Debugging

SAGA relies on **Apache Flink’s runtime logging and metrics infrastructure** rather than fine-grained per-update application logging.

#### Logging Design

* SAGA does **not log individual graph updates or per-vertex decisions by default**.
* This design avoids excessive overhead during high-throughput streaming execution.
* Instead, logging focuses on **runtime-level events**, including:

  * job startup and termination
  * operator initialization
  * checkpoint creation and completion
  * failures and recovery
  * backpressure and task warnings

All such events are handled by Flink’s logging subsystem.

#### Log Locations

When running SAGA on a Flink cluster, logs are written to:

```text
$FLINK_HOME/log/
```

Typical log files include:

* `flink-*-jobmanager-*.log`
* `flink-*-taskexecutor-*.log`

These logs provide visibility into:

* state backend initialization
* operator execution
* checkpointing and recovery
* runtime errors, if any



## 9. Anonymity Notice

This repository is released **anonymously** for peer review.
No identifying information is included in the code or documentation.


