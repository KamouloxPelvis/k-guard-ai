#!/usr/bin/env bash
set -euo pipefail

# ---------------------------------------------------------
# K-Guard AI root installer
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

# --- Basic dependency checks ---
for cmd in git go kubectl; do
  if ! command -v "${cmd}" >/dev/null 2>&1; then
    echo "[kguard-ai] ERROR: '${cmd}' is not installed or not in PATH."
    echo "[kguard-ai] Please install '${cmd}' and retry."
    exit 1
  fi
done

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
  echo "[kguard-ai] WARNING: 'check' failed. You can still run the installer,"
  echo "but cluster access issues must be resolved."
}
echo

echo "============================================"
echo "K-Guard AI Installer"
echo "============================================"
echo "You can now choose:"
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