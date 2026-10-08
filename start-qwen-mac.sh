#!/bin/bash
# Local Qwen coding API for Apple Silicon. Compatible with macOS Bash 3.2.
# Default: mlx-community/Qwen3.8-27B-4bit (~16.1 GB download).
# Uses uv + isolated Python 3.12 + mlx-lm; no Homebrew or sudo needed.
# Sources checked 2026-10-07:
# https://docs.astral.sh/uv/getting-started/installation/
# https://github.com/ml-explore/mlx-lm
# https://huggingface.co/mlx-community/Qwen3.8-27B-4bit

set -euo pipefail

log() { printf '\n[qwen] %s\n' "$*"; }
die() { printf '\n[qwen] ERROR: %s\n' "$*" >&2; exit 1; }
trap 'printf "\n[qwen] Setup/start failed at line %s. See the error above.\n" "$LINENO" >&2' ERR

usage() {
  cat <<'HELP'
Usage: bash start-qwen-mac.sh [--small] [--install-only] [--upgrade]

  (no options)    Install missing tooling, download/reuse Qwen3.8-27B, start API.
  --small         Use Qwen3.5-9B 4-bit for a 16 GB or 24 GB Mac.
  --install-only  Install tooling and download model, then exit.
  --upgrade       Upgrade this script's Python packages before starting.
  --help          Show this help without installing anything.

Requirements: Apple Silicon, native ARM terminal, macOS 15+, internet on first run.
Default model: 32 GB+ memory recommended and required by this script.
Small model: 16 GB+ memory required. Close memory-heavy apps if necessary.
These are conservative checks; long prompts still consume additional memory.

Optional environment variables:
  QWEN_PORT=8080               Local server port.
  QWEN_MAX_TOKENS=8192         Default output budget, including thinking tokens.
  QWEN_APP_DIR=...             Tooling directory (default ~/.local/share/qwen-mac).
  HF_HOME=...                  Hugging Face model cache directory.

Examples:
  bash start-qwen-mac.sh
  bash start-qwen-mac.sh --small
  QWEN_PORT=8081 bash start-qwen-mac.sh

Client base URL: http://127.0.0.1:8080/v1
Model ID: mlx-community/Qwen3.8-27B-4bit (or mlx-community/Qwen3.5-9B-4bit)
API key: not required; use "local" if your client insists on a value.
The process stays in the foreground. Stop it with Ctrl+C.
The API serves text; this launcher does not enable image input.
Set your coding client's context budget to 16K-32K initially. The output-token
setting is NOT an input-context or total-memory limit.
HELP
}

qwen_small=0
qwen_install_only=0
qwen_upgrade=0
while [ "$#" -gt 0 ]; do
  case "$1" in
    --small) qwen_small=1 ;;
    --install-only) qwen_install_only=1 ;;
    --upgrade) qwen_upgrade=1 ;;
    --help|-h) usage; exit 0 ;;
    *) die "Unknown option: $1. Run with --help." ;;
  esac
  shift
done

[ "$(uname -s)" = Darwin ] || die "This script runs on macOS only."
[ "$(uname -m)" = arm64 ] || die "Apple Silicon in native ARM mode is required. If using Rosetta, run: arch -arm64 /bin/bash start-qwen-mac.sh"
[ "$(id -u)" -ne 0 ] || die "Run as your normal macOS user, without sudo."
qwen_macos=$(sw_vers -productVersion)
[ "${qwen_macos%%.*}" -ge 15 ] || die "This launcher requires macOS 15 or newer (found $qwen_macos)."

qwen_model='mlx-community/Qwen3.8-27B-4bit'
qwen_min_gib=32
if [ "$qwen_small" -eq 1 ]; then
  qwen_model='mlx-community/Qwen3.5-9B-4bit'
  qwen_min_gib=16
fi
qwen_memory_bytes=$(sysctl -n hw.memsize)
qwen_memory_gib=$((qwen_memory_bytes / 1073741824))
[ "$qwen_memory_gib" -ge "$qwen_min_gib" ] || die "Found ${qwen_memory_gib} GiB RAM; this model's launcher requires ${qwen_min_gib} GiB. On a 16/24 GB Mac, rerun with --small."

qwen_port=${QWEN_PORT:-8080}
qwen_max_tokens=${QWEN_MAX_TOKENS:-8192}
case "$qwen_port" in ''|*[!0-9]*) die 'QWEN_PORT must be an integer.' ;; esac
case "$qwen_max_tokens" in ''|*[!0-9]*) die 'QWEN_MAX_TOKENS must be an integer.' ;; esac
[ "${#qwen_port}" -le 5 ] || die 'QWEN_PORT must be between 1024 and 65535.'
[ "${#qwen_max_tokens}" -le 6 ] || die 'QWEN_MAX_TOKENS must be between 1 and 131072.'
qwen_port=$((10#$qwen_port))
qwen_max_tokens=$((10#$qwen_max_tokens))
[ "$qwen_port" -ge 1024 ] && [ "$qwen_port" -le 65535 ] || die 'QWEN_PORT must be between 1024 and 65535.'
[ "$qwen_max_tokens" -ge 1 ] && [ "$qwen_max_tokens" -le 131072 ] || die 'QWEN_MAX_TOKENS must be between 1 and 131072.'

qwen_app_dir=${QWEN_APP_DIR:-"$HOME/.local/share/qwen-mac"}
qwen_venv="$qwen_app_dir/venv"
mkdir -p "$qwen_app_dir/bin"
log "macOS $qwen_macos / Apple Silicon / ${qwen_memory_gib} GiB RAM"

# Reuse uv from PATH or the standard installer locations before installing it.
qwen_uv=$(command -v uv || true)
if [ -z "$qwen_uv" ]; then
  for qwen_candidate in "$HOME/.local/bin/uv" /opt/homebrew/bin/uv "$qwen_app_dir/bin/uv"; do
    if [ -x "$qwen_candidate" ]; then qwen_uv="$qwen_candidate"; break; fi
  done
fi
if [ -z "$qwen_uv" ]; then
  log 'Installing uv from the official Astral installer...'
  qwen_installer=$(mktemp -t qwen-uv)
  trap 'rm -f "$qwen_installer"' EXIT
  curl --fail --show-error --location --retry 3 --connect-timeout 20 \
    https://astral.sh/uv/install.sh -o "$qwen_installer"
  UV_INSTALL_DIR="$qwen_app_dir/bin" UV_NO_MODIFY_PATH=1 sh "$qwen_installer"
  rm -f "$qwen_installer"
  trap - EXIT
  qwen_uv="$qwen_app_dir/bin/uv"
fi
"$qwen_uv" --version

if [ ! -x "$qwen_venv/bin/python" ]; then
  log 'Creating an isolated Python 3.12 environment (Python is downloaded if needed)...'
  "$qwen_uv" venv --python 3.12 --managed-python "$qwen_venv"
fi
qwen_python="$qwen_venv/bin/python"

# This check does not contact PyPI. Existing working installs are reused.
if [ "$qwen_upgrade" -eq 1 ] || ! "$qwen_python" - <<'PY' >/dev/null 2>&1
from importlib.metadata import version
from packaging.version import Version
assert Version(version('mlx-lm')) >= Version('0.32.0')
import mlx.core
import mlx_lm.server
import mlx_lm.models.qwen3_5
import huggingface_hub
PY
then
  log 'Installing/updating MLX tooling in the isolated environment...'
  "$qwen_uv" pip install --python "$qwen_python" --upgrade 'mlx-lm>=0.32.0' packaging
fi

"$qwen_python" - <<'PY'
import platform
from importlib.metadata import version
import mlx.core as mx
if platform.machine() != 'arm64':
    raise SystemExit('Python must be an ARM64 build, not an Intel/Rosetta build.')
if not mx.metal.is_available():
    raise SystemExit('MLX cannot access Metal on this Mac. Check macOS and Python architecture.')
print(f"MLX {version('mlx')} / mlx-lm {version('mlx-lm')} / Python {platform.python_version()}")
PY

if [ "$qwen_install_only" -eq 0 ]; then
  "$qwen_python" - "$qwen_port" <<'PY'
import socket, sys
port = int(sys.argv[1])
try:
    with socket.socket() as sock:
        sock.bind(('127.0.0.1', port))
except OSError as exc:
    raise SystemExit(f'Cannot use port {port}: {exc}. Try QWEN_PORT=8081.')
PY
fi

# Check actual CLI capabilities before downloading a large model.
qwen_help=$("$qwen_python" -m mlx_lm.server --help)
for qwen_flag in --model --host --port --max-tokens --chat-template-args; do
  case "$qwen_help" in *"$qwen_flag"*) ;; *) die "Installed mlx-lm lacks $qwen_flag. Rerun with --upgrade." ;; esac
done

log "Downloading/reusing $qwen_model. First run can take a while."
"$qwen_python" - "$qwen_model" <<'PY'
import os, shutil, sys
from pathlib import Path
from huggingface_hub import snapshot_download
from huggingface_hub.constants import HF_HUB_CACHE
cache = Path(HF_HUB_CACHE).expanduser()
cache.mkdir(parents=True, exist_ok=True)
free_gib = shutil.disk_usage(cache).free / 2**30
if free_gib < 22:
    print(f'Available disk space: {free_gib:.1f} GiB. A fresh 27B download needs '
          'about 16.1 GB plus tooling/headroom; cached downloads reuse existing files.', flush=True)
snapshot_download(
    repo_id=sys.argv[1],
    allow_patterns=['*.json', '*.safetensors', '*.jinja', '*.model', '*.txt', '*.tiktoken'],
)
print('Model files are cached.')
PY

if [ "$qwen_install_only" -eq 1 ]; then
  log 'Installation and model download complete. Rerun without --install-only to start.'
  exit 0
fi

qwen_args=(--model "$qwen_model" --host 127.0.0.1 --port "$qwen_port"
  --max-tokens "$qwen_max_tokens"
  --chat-template-args '{"enable_thinking":true,"preserve_thinking":true}')
# Limit retained prompt caches and concurrent generations when supported.
# These limits do not cap the active request's input context or total RAM.
case "$qwen_help" in *--prompt-cache-size*) qwen_args+=(--prompt-cache-size 1) ;; esac
case "$qwen_help" in *--prompt-cache-bytes*) qwen_args+=(--prompt-cache-bytes 1073741824) ;; esac
case "$qwen_help" in *--decode-concurrency*) qwen_args+=(--decode-concurrency 1) ;; esac
case "$qwen_help" in *--prompt-concurrency*) qwen_args+=(--prompt-concurrency 1) ;; esac
case "$qwen_help" in *--prefill-step-size*) qwen_args+=(--prefill-step-size 512) ;; esac
case "$qwen_help" in *--temp*) qwen_args+=(--temp 1.0) ;; esac
case "$qwen_help" in *--top-p*) qwen_args+=(--top-p 0.95) ;; esac
case "$qwen_help" in *--top-k*) qwen_args+=(--top-k 20) ;; esac

cat <<INFO

Starting the model. Wait for the server's listening message before sending requests.
Base URL: http://127.0.0.1:$qwen_port/v1
Model ID: $qwen_model
API key:  local (placeholder; the server does not require authentication)
Stop:     Ctrl+C

Test in another terminal (the first response may take a while):
curl -N http://127.0.0.1:$qwen_port/v1/chat/completions \\
  -H 'Content-Type: application/json' \\
  -d '{"model":"$qwen_model","messages":[{"role":"user","content":"Write a Java method that checks whether a string is a palindrome."}],"max_tokens":2048,"stream":true}'

INFO
exec "$qwen_python" -m mlx_lm.server "${qwen_args[@]}"
