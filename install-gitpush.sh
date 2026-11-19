#!/usr/bin/env sh
# Instala o comando "gitpush" (Termux ou Linux/macOS/Git Bash)
set -e
DIR="$(cd "$(dirname "$0")" && pwd)"
if [ -n "$PREFIX" ] && [ -d "$PREFIX/bin" ]; then DEST="$PREFIX/bin"; else DEST="/usr/local/bin"; fi
ln -sf "$DIR/gitpush.sh" "$DEST/gitpush" 2>/dev/null || sudo ln -sf "$DIR/gitpush.sh" "$DEST/gitpush"
echo "Pronto. Agora use:  gitpush \"mensagem\""
