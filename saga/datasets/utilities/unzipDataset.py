import os
import gzip
import shutil
import argparse

TARGET_DIRS = ["base", "updates"]

def unzip_gz_file(gz_path):
    txt_path = gz_path[:-3]  # remove .gz
    if os.path.exists(txt_path):
        print(f"[=] Skipping (already exists): {txt_path}")
        return

    with gzip.open(gz_path, 'rb') as f_in:
        with open(txt_path, 'wb') as f_out:
            shutil.copyfileobj(f_in, f_out)

    print(f"[+] Unzipped: {txt_path}")

def process_graph(graph_dir):
    for sub in TARGET_DIRS:
        subdir = os.path.join(graph_dir, sub)
        if not os.path.isdir(subdir):
            continue

        for fname in os.listdir(subdir):
            if fname.endswith(".txt.gz"):
                gz_path = os.path.join(subdir, fname)
                unzip_gz_file(gz_path)

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--datasets",
        required=True,
        help="Path to datasets directory (e.g., datasets/)"
    )
    args = parser.parse_args()

    datasets_root = args.datasets

    for graph in os.listdir(datasets_root):
        graph_dir = os.path.join(datasets_root, graph)
        if not os.path.isdir(graph_dir):
            continue

        print(f"\n=== Processing graph: {graph} ===")
        process_graph(graph_dir)

    print("\n[*] All done")

if __name__ == "__main__":
    main()
