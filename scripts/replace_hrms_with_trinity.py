#!/usr/bin/env python3
"""
Safe bulk replace script: replace 'from hrms' and 'import hrms' with 'from trinity' / 'import trinity'
Only operates on .py files inside the repo. Creates a .bak copy before changing each file.
"""
import io
import os
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

PY_PATTERN = re.compile(r"^(\s*)(from|import)\s+hrms(\b.*)$")

def process_file(path: Path):
    text = path.read_text(encoding="utf-8")
    changed = False
    new_lines = []
    for line in text.splitlines(keepends=True):
        m = PY_PATTERN.match(line)
        if m:
            indent, kind, rest = m.groups()
            new_line = f"{indent}{kind} trinity{rest}"
            if new_line != line:
                changed = True
                new_lines.append(new_line)
                continue
        new_lines.append(line)

    if changed:
        bak = path.with_suffix(path.suffix + ".bak")
        if not bak.exists():
            path.rename(bak)
            bak.write_text(text, encoding="utf-8")
        path.write_text(''.join(new_lines), encoding="utf-8")
        print(f"Updated: {path.relative_to(ROOT)}")

def main():
    print(f"Scanning {ROOT} for .py files...")
    for p in ROOT.rglob("*.py"):
        # skip virtual envs or hidden dirs
        if "/.venv" in str(p) or "/env" in str(p):
            continue
        process_file(p)

if __name__ == "__main__":
    main()
