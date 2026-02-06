import os
import gzip
import random
import argparse
import json

UPDATE_SIZES = {
    "0.01K": 10,
    "0.1K": 100,
    "1K": 1000,
    "10K": 10000,
    "100K": 100000
}

def read_edges(path):
    edges = []
    with open(path, 'r') as f:
        for line in f:
            if line.startswith('#') or not line.strip():
                continue
            u, v = line.split()
            edges.append((int(u), int(v)))
    return edges

def write_gz(path, lines):
    with gzip.open(path, 'wt') as f:
        for line in lines:
            f.write(line + '\n')

def split_base_updates(edges, base_frac=0.9):
    random.shuffle(edges)
    split = int(len(edges) * base_frac)
    return edges[:split], edges[split:]

def generate_update_files(update_edges, out_dir):
    os.makedirs(out_dir, exist_ok=True)

    for label, size in UPDATE_SIZES.items():
        if len(update_edges) < size:
            raise ValueError(f"Not enough updates for {label}")

        subset = update_edges[:size]
        fname = f"updates_{label}.txt.gz"
        path = os.path.join(out_dir, fname)

        lines = [f"+ {u} {v}" for u, v in subset]
        write_gz(path, lines)

def generate_queries(edges, out_dir, num_queries=10000):
    os.makedirs(out_dir, exist_ok=True)

    vertices = list(set([u for u, v in edges] + [v for u, v in edges]))
    random.shuffle(vertices)
    queries = vertices[:num_queries]

    for algo in ["mis", "gc", "mm"]:
        with open(os.path.join(out_dir, f"queries_{algo}.txt"), "w") as f:
            for v in queries:
                f.write(f"{v}\n")

def write_metadata(graph_name, edges, base_edges, update_edges, out_dir):
    meta = {
        "name": graph_name,
        "num_vertices": len(set([u for u, v in edges] + [v for u, v in edges])),
        "num_edges": len(edges),
        "base_edges": len(base_edges),
        "update_edges_available": len(update_edges),
        "base_fraction": 0.9,
        "update_files": list(UPDATE_SIZES.keys()),
        "query_count": 10000,
        "algorithms": ["MIS", "GC", "MM"]
    }

    with open(os.path.join(out_dir, "metadata.json"), "w") as f:
        json.dump(meta, f, indent=2)

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--graph", required=True)
    parser.add_argument("--input", required=True)
    parser.add_argument("--out", required=True)
    args = parser.parse_args()

    random.seed(42)

    print(f"[+] Reading {args.graph}")
    edges = read_edges(args.input)

    print("[+] Splitting base / updates (90 / 10)")
    base_edges, update_edges = split_base_updates(edges)

    graph_dir = os.path.join(args.out, args.graph)
    base_dir = os.path.join(graph_dir, "base")
    upd_dir = os.path.join(graph_dir, "updates")
    qry_dir = os.path.join(graph_dir, "queries")

    os.makedirs(base_dir, exist_ok=True)

    write_gz(
        os.path.join(base_dir, "base_graph.txt.gz"),
        [f"{u} {v}" for u, v in base_edges]
    )

    print("[+] Writing 5 update files")
    generate_update_files(update_edges, upd_dir)

    print("[+] Writing queries")
    generate_queries(edges, qry_dir)

    print("[+] Writing metadata")
    write_metadata(args.graph, edges, base_edges, update_edges, graph_dir)

    print("[*] Dataset ready")

if __name__ == "__main__":
    main()
