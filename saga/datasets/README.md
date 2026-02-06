# Datasets and Utilities

This directory contains graph datasets and preprocessing utilities used in the experiments.

Due to repository size constraints, we include **only one graph dataset** in this repository:

* **amazon0601** — provided in compressed (`.gz`) format

All other datasets used in the paper can be downloaded from public sources (e.g., social, web, and citation graphs).
Links can be added as needed.

## Included Dataset

### amazon0601

```text
datasets/amazon0601/
├── base/
│   └── base_graph.txt.gz
├── updates/
│   └── updates_*.txt.gz
```

* Base graph and update files are stored in **compressed format**.
* This keeps the repository lightweight while preserving reproducibility.

To unzip the dataset:

```bash
python unzipDataset.py --datasets datasets
```

This converts `.txt.gz` files in `base/` and `updates/` to plain `.txt` files while keeping the original compressed files.

## Utilities

All preprocessing scripts are located in:

```text
datasets/utilities/
```

### 1. Converting Base Graph to CSR

The `baseToCSR.py` utility converts a base graph into **CSR (Compressed Sparse Row)** format, producing a single CSR file suitable for efficient neighborhood access.

Example:

```bash
python baseToCSR.py \
  --graph amazon0601 \
  --base datasets/amazon0601/base/base_graph.txt \
  --out datasets
```

This creates a CSR representation under:

```text
datasets/amazon0601/csr/
```

### 2. Creating Update Batches and Queries

The `createUpdateBatches.py` utility generates:

* Fixed-size update batches:

  * `0.01K`, `0.1K`, `1K`, `10K`, `100K`
* Query workloads for:

  * **MIS** (Maximal Independent Set)
  * **MM** (Maximal Matching)
  * **GC** (Graph Coloring)

These outputs are derived from the base graph and are directly consumable by SAGA and baseline systems.

## Additional Datasets

Other datasets used in the evaluation (e.g., social networks, citation graphs, web graphs) are **not included** due to size constraints.
They can be downloaded from public repositories and processed using the same utilities provided here.



