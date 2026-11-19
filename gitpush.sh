#!/usr/bin/env sh
# gitpush — envia tudo desta pasta para https://github.com/hackproff23-max/FIF-EDIT-
# Uso:  gitpush "mensagem do commit"      (a mensagem é opcional)
set -e
REPO="https://github.com/hackproff23-max/FIF-EDIT-.git"
BRANCH="main"
cd "$(dirname "$(readlink -f "$0" 2>/dev/null || echo "$0")")"

command -v git >/dev/null 2>&1 || { echo "git não instalado (Termux: pkg install git)"; exit 1; }
[ -d .git ] || { git init -q; git checkout -q -B "$BRANCH"; }
git remote get-url origin >/dev/null 2>&1 || git remote add origin "$REPO"
git add -A
if git diff --cached --quiet; then
  echo "Nada novo para commitar."
else
  git commit -q -m "${1:-atualização $(date '+%d/%m/%Y %H:%M')}"
fi
git push -u origin "$BRANCH"
echo "Enviado para $REPO"
