# SAGA: State-Aware Graph Analytics on Dynamic Graphs

This repository contains the **research prototype implementation of SAGA (State-Aware Graph Analytics)**, a distributed framework for maintaining **combinatorial graph properties** on **dynamic graphs** under continuous updates and concurrent queries.

The implementation is **Flink-native** and is intended for **research evaluation and artifact review**, not as a production library.

---

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

---

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

---

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

---

## 4. Build Requirements

### Software

* Java 17
* Apache Flink 1.17+ (tested with modern Flink releases)
* Maven 3.8+

### Hardware (recommended)

* Multi-core machine (≥16 cores recommended)
* ≥64 GB RAM for large graphs
* Distributed cluster recommended for scaling experiments

---

## 5. Building the Project

From the repository root:

```bash
mvn clean package
```

This produces a runnable JAR under:

```text
target/
```

---

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

---

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

---

## 7. Datasets

SAGA is evaluated on **dynamic graphs**, where a static base graph is incrementally modified by a stream of updates. This section describes how datasets are prepared and how update batches are generated.

#### Base Graph

Each experiment starts from a **static base graph**, provided as an edge list:

```text
u v
u v
u v
```

where `u` and `v` denote vertex identifiers.

The base graph is loaded into SAGA as the initial state before any dynamic updates are applied.

#### Update Stream Construction

To model graph evolution, a subset of edges from the dataset is converted into a **stream of dynamic updates**.

Each update is represented in text form as:

```text
ADD u v
DEL u v
```

where:

* `ADD u v` inserts an edge between vertices `u` and `v`
* `DEL u v` removes an edge between vertices `u` and `v`

These updates are processed incrementally by SAGA’s streaming runtime.

---

#### Update Batch Sizes

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

* update processing time
* throughput
* query latency under concurrent updates

#### Batch Generation Methodology

Given an input dataset, update batches are generated as follows:

1. Load the full edge list of the dataset.
2. Randomly sample a subset of edges.
3. Split the sampled edges into batches of the desired size.
4. Emit updates sequentially as `ADD` or `DEL` operations.

This methodology:

* preserves the original graph structure
* avoids synthetic graph generation
* reflects realistic incremental graph evolution

#### Queries During Updates

Queries may be issued:

* concurrently with update batches, or
* after a batch has completed

Queries are encoded as:

```text
QUERY vertexId
```

All queries are served from **live per-vertex state** and do not block update processing.
* Queries are **read-only**
* Queries do **not block updates**
* Queries read directly from **live keyed vertex state**
* Query latency depends only on state access and Flink scheduling

---

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

---

## 9. Anonymity Notice

This repository is released **anonymously** for peer review.
No identifying information is included in the code or documentation.

---
