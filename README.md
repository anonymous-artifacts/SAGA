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

## 7. Query Semantics

* Queries are **read-only**
* Queries do **not block updates**
* Queries read directly from **live keyed vertex state**
* Query latency depends only on state access and Flink scheduling

---

## 8. Coordination and Consistency Model

* Updates are processed **locally and incrementally**
* Boundary (replicated) vertices may trigger coordination via Ω
* Conflict resolution is **deterministic** (partition-priority policy)
* The system prioritizes **localized repair and throughput**

This implementation is a **research prototype** and does not claim full transactional or adversarial consistency guarantees.

---

## 9. State Management and Fault Tolerance

* Vertex state is maintained using **Flink keyed state**
* Periodic checkpoints provide fault tolerance
* State backend configuration is explicit and configurable

---

## 10. Anonymity Notice

This repository is released **anonymously** for peer review.
No identifying information is included in the code or documentation.

---
