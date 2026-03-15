#!/bin/bash
set -e

# Creates fake git repos with realistic commits for recording a demo GIF.
# Usage: ./scripts/setup-demo.sh
# Cleanup: ./scripts/setup-demo.sh --cleanup

DEMO_DIR="/tmp/devlog-demo"
CONFIG_BACKUP="$HOME/.devlog/config.yml.bak"
CONFIG_FILE="$HOME/.devlog/config.yml"

cleanup() {
    echo "Cleaning up demo data..."
    rm -rf "$DEMO_DIR"
    if [ -f "$CONFIG_BACKUP" ]; then
        mv "$CONFIG_BACKUP" "$CONFIG_FILE"
        echo "Restored original config."
    fi
    echo "Done."
}

if [ "$1" = "--cleanup" ]; then
    cleanup
    exit 0
fi

# Backup existing config
if [ -f "$CONFIG_FILE" ]; then
    cp "$CONFIG_FILE" "$CONFIG_BACKUP"
    echo "Original config backed up to $CONFIG_BACKUP"
fi

mkdir -p "$DEMO_DIR"

# Helper: create a repo and add commits
create_repo() {
    local repo_name="$1"
    local repo_dir="$DEMO_DIR/$repo_name"
    mkdir -p "$repo_dir"
    cd "$repo_dir"
    git init -q
    git checkout -q -b main
    # Set author for consistent output
    git config user.name "demo"
    git config user.email "demo@devlog.dev"
}

# Helper: create a commit with fake file changes
fake_commit() {
    local msg="$1"
    local days_ago="${2:-0}"
    local num_files="${3:-2}"
    local lines="${4:-30}"

    local date
    date=$(date -v-"${days_ago}"d "+%Y-%m-%dT12:%M:%S" 2>/dev/null || date -d "$days_ago days ago" "+%Y-%m-%dT12:%M:%S")

    for i in $(seq 1 "$num_files"); do
        local file="src/$(echo "$msg" | tr ' ' '-' | tr '[:upper:]' '[:lower:]')-${i}.kt"
        mkdir -p "$(dirname "$file")"
        # Generate realistic-looking content
        head -c "$lines" /dev/urandom | base64 | head -n "$lines" > "$file"
    done

    git add -A
    GIT_AUTHOR_DATE="$date" GIT_COMMITTER_DATE="$date" \
        git commit -q -m "$msg"
}

echo "Creating demo repos in $DEMO_DIR..."

# ── Repo 1: mobile-app (Android project) ──
create_repo "mobile-app"
git checkout -q -b feat/onboarding

fake_commit "feat: add onboarding welcome screen" 6 4 80
fake_commit "feat: implement email validation" 5 2 40
fake_commit "fix: keyboard overlaps input on small screens" 4 1 15
fake_commit "feat: add biometric auth toggle" 3 3 60
fake_commit "test: add onboarding flow unit tests" 2 3 45
fake_commit "fix: back navigation skips splash" 1 1 12
fake_commit "feat: add analytics events for onboarding" 0 2 35

echo "  ✓ mobile-app (7 commits)"

# ── Repo 2: api-gateway (backend) ──
create_repo "api-gateway"
git checkout -q -b main

fake_commit "feat: add rate limiting middleware" 5 3 90
fake_commit "feat: implement JWT refresh token rotation" 4 4 120
fake_commit "fix: race condition in session cache" 3 1 25
fake_commit "refactor: remove deprecated v1 auth endpoints" 2 2 55
fake_commit "fix: CORS preflight returns 500 on OPTIONS" 0 1 18

echo "  ✓ api-gateway (5 commits)"

# ── Write demo config ──
mkdir -p "$(dirname "$CONFIG_FILE")"
cat > "$CONFIG_FILE" <<EOF
workspaces:
  - $DEMO_DIR
provider: anthropic
model: claude-haiku-4-5-20251001
apiKeyEnv: ANTHROPIC_API_KEY
language: english
EOF

echo ""
echo "Demo setup complete!"
echo "  Repos: $DEMO_DIR/{mobile-app,api-gateway}"
echo "  Config: $CONFIG_FILE (original backed up)"
echo ""
echo "Now record the GIF:"
echo "  ./gradlew installDist"
echo "  vhs demo.tape"
echo ""
echo "When done, restore your config:"
echo "  ./scripts/setup-demo.sh --cleanup"
