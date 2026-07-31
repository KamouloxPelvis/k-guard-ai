#!/usr/bin/env bash
set -euo pipefail

# ---------------------------------------------------------
# K-Guard AI root installer (v0.8.0)
# Usage:
#   git clone https://github.com/KamouloxPelvis/K-Guard-AI.git
#   cd K-Guard-AI
#   ./install.sh
# ---------------------------------------------------------

REPO_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
INSTALLER_DIR="${REPO_DIR}/installer"
BINARY="${INSTALLER_DIR}/kguard-ai-installer"

echo "[kguard-ai] Root installer starting..."
echo "[kguard-ai] Repository directory: ${REPO_DIR}"
echo

# --- Basic dependency checks (with Go auto-install) ---

# Check git
if ! command -v git >/dev/null 2>&1; then
  echo "[kguard-ai] ERROR: 'git' is not installed or not in PATH."
  echo "[kguard-ai] Please install 'git' (e.g. sudo apt-get install -y git) and retry."
  exit 1
fi

# Check kubectl
if ! command -v kubectl >/dev/null 2>&1; then
  echo "[kguard-ai] ERROR: 'kubectl' is not installed or not in PATH."
  echo "[kguard-ai] K-Guard AI installer expects a reachable k3s/Kubernetes cluster through kubectl."
  echo "[kguard-ai] Please install 'kubectl' and configure your kubeconfig, then retry."
  exit 1
fi

# Check Go, auto-install if missing (Debian/Ubuntu)
if ! command -v go >/dev/null 2>&1; then
  echo "[kguard-ai] 'go' is not installed. Attempting to install Go via apt..."

  if command -v apt-get >/dev/null 2>&1; then
    sudo apt-get update
    sudo apt-get install -y golang

    if ! command -v go >/dev/null 2>&1; then
      echo "[kguard-ai] ERROR: 'go' is still not available after installation."
      echo "[kguard-ai] Please check your Go installation and PATH."
      exit 1
    fi

    echo "[kguard-ai] Go installed successfully."
  } else {
    echo "[kguard-ai] ERROR: 'apt-get' not found. Automatic Go installation is only supported on Debian/Ubuntu."
    echo "[kguard-ai] Please install Go manually (version 1.22+ recommended) and retry."
    exit 1
  fi
fi

echo "[kguard-ai] Dependencies OK (git, go, kubectl)."
echo

# --- Ensure Go modules and build installer ---
echo "[kguard-ai] Preparing Go installer..."
cd "${INSTALLER_DIR}"

echo "[kguard-ai] Running 'go mod tidy'..."
go mod tidy

echo "[kguard-ai] Building 'kguard-ai-installer'..."
go build -o "${BINARY}" .

echo "[kguard-ai] Build completed: ${BINARY}"
echo

# --- Run initial check via TUI/CLI installer ---
echo "[kguard-ai] Checking kubectl and cluster access via installer..."
"${BINARY}" check || {
  echo "[kguard-ai] WARNING: 'check' failed. K-Guard AI installer expects a reachable k3s/Kubernetes cluster through kubectl."
  echo "[kguard-ai] You can still run the installer, but cluster access issues must be resolved."
}
echo

echo "============================================"
echo "K-Guard AI Installer (v0.8.0)"
echo "============================================"
echo "You can now choose (in the current k3s/Kubernetes cluster):"
echo "  - TUI mode  : ${BINARY}"
echo "  - CLI mode  : ${BINARY} install"
echo "  - Status    : ${BINARY} status"
echo

read -r -p "Start TUI installer now? [y/N]: " ANSWER

ANSWER_LOWER=$(echo "${ANSWER}" | tr '[:upper:]' '[:lower:]')
if [ "${ANSWER_LOWER}" = "y" ] || [ "${ANSWER_LOWER}" = "yes" ]; then
  echo "[kguard-ai] Starting TUI installer..."
  "${BINARY}"
else
  echo "[kguard-ai] Skipping TUI. You can run later:"
  echo "  cd ${INSTALLER_DIR}"
  echo "  ./kguard-ai-installer install"
fi

echo
echo "[kguard-ai] Root installation flow finished."