import os
import argparse
from collections import defaultdict

def read_edges(path):
    edges = []
    with open(path, "r") as f:
        for line in f:
            if line.startswith("#") or not line.strip():
                continue
            u, v = line.split()
            edges.append((int(u), int(v)))
    return edges

def build_csr(edges):
    adj = defaultdict(list)
    vertices = set()

    for u, v in edges:
        adj[u].append(v)
        adj[v].append(u)
        vertices.add(u)
        vertices.add(v)

    vertices = sorted(vertices)
    vid = {v: i for i, v in enumerate(vertices)}

    row_ptr = [0]
    col_idx = []

    for v in vertices:
        nbrs = sorted(adj[v])
        for n in nbrs:
            col_idx.append(vid[n])
        row_ptr.append(len(col_idx))

    return len(vertices), len(edges), row_ptr, col_idx

def write_single_csr(path, V, E, row_ptr, col_idx):
    with open(path, "w") as f:
        f.write(f"{V} {E}\n")
        f.write("indices ->\n")
        for x in row_ptr:
            f.write(f"{x}\n")
        f.write("list ->\n")
        for x in col_idx:
            f.write(f"{x}\n")

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--graph", required=True)
    parser.add_argument("--base", required=True)
    parser.add_argument("--out", required=True)
    args = parser.parse_args()

    print(f"[+] Reading base graph: {args.base}")
    edges = read_edges(args.base)

    print("[+] Building CSR")
    V, E, row_ptr, col_idx = build_csr(edges)

    csr_dir = os.path.join(args.out, args.graph, "csr")
    os.makedirs(csr_dir, exist_ok=True)

    out_file = os.path.join(csr_dir, "csr_graph.txt")
    write_single_csr(out_file, V, E, row_ptr, col_idx)

    print("[*] CSR file written")
    print(f"    Vertices : {V}")
    print(f"    Edges    : {E}")
    print(f"    Row ptr  : {len(row_ptr)}")
    print(f"    Col idx  : {len(col_idx)}")

if __name__ == "__main__":
    main()
