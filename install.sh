#!/bin/sh
set -e

# DevLog installer — downloads the latest release binary for your platform.

REPO="vikey89/DevLog"
INSTALL_DIR="/usr/local/bin"
BINARY_NAME="devlog"

main() {
    check_dependencies

    os="$(detect_os)"
    arch="$(detect_arch)"
    artifact="$(get_artifact_name "$os" "$arch")"

    printf "Detected: %s/%s\n" "$os" "$arch"
    printf "Artifact: %s\n" "$artifact"

    latest_tag="$(get_latest_tag)"
    printf "Latest release: %s\n" "$latest_tag"

    download_url="https://github.com/${REPO}/releases/download/${latest_tag}/${artifact}"

    tmpdir="$(mktemp -d)"
    trap 'rm -rf "$tmpdir"' EXIT

    printf "Downloading %s...\n" "$download_url"
    curl -fSL -o "${tmpdir}/${BINARY_NAME}" "$download_url"
    chmod +x "${tmpdir}/${BINARY_NAME}"

    install_binary "${tmpdir}/${BINARY_NAME}"

    printf "\ndevlog %s installed successfully!\n" "$latest_tag"
    printf "Run 'devlog --help' to get started.\n"
}

remove_quarantine() {
    if [ "$(uname -s)" = "Darwin" ]; then
        xattr -d com.apple.quarantine "$1" 2>/dev/null || true
    fi
}

check_dependencies() {
    if ! command -v curl >/dev/null 2>&1; then
        printf "Error: curl is required but not installed.\n" >&2
        exit 1
    fi
}

detect_os() {
    case "$(uname -s)" in
        Linux*)  printf "linux" ;;
        Darwin*) printf "macos" ;;
        MINGW*|MSYS*|CYGWIN*)
            printf "Error: Windows is not supported by the install script.\n" >&2
            printf "Download the binary manually from https://github.com/%s/releases\n" "$REPO" >&2
            exit 1
            ;;
        *)
            printf "Error: unsupported operating system '%s'.\n" "$(uname -s)" >&2
            exit 1
            ;;
    esac
}

detect_arch() {
    case "$(uname -m)" in
        x86_64|amd64)  printf "amd64" ;;
        arm64|aarch64) printf "arm64" ;;
        *)
            printf "Error: unsupported architecture '%s'.\n" "$(uname -m)" >&2
            exit 1
            ;;
    esac
}

get_artifact_name() {
    os="$1"
    arch="$2"

    case "${os}-${arch}" in
        linux-amd64)  printf "devlog-linux-amd64" ;;
        macos-arm64)  printf "devlog-macos-arm64" ;;
        *)
            printf "Error: no pre-built binary for %s/%s.\n" "$os" "$arch" >&2
            printf "You can build from source: ./gradlew nativeCompile\n" >&2
            exit 1
            ;;
    esac
}

get_latest_tag() {
    tag="$(curl -fsSL "https://api.github.com/repos/${REPO}/releases/latest" \
        | grep '"tag_name"' \
        | sed -E 's/.*"tag_name": *"([^"]+)".*/\1/')"

    if [ -z "$tag" ]; then
        printf "Error: could not determine latest release.\n" >&2
        printf "Check https://github.com/%s/releases\n" "$REPO" >&2
        exit 1
    fi

    printf "%s" "$tag"
}

install_binary() {
    src="$1"

    if [ -w "$INSTALL_DIR" ]; then
        cp "$src" "${INSTALL_DIR}/${BINARY_NAME}"
        remove_quarantine "${INSTALL_DIR}/${BINARY_NAME}"
        printf "Installed to %s/%s\n" "$INSTALL_DIR" "$BINARY_NAME"
    elif command -v sudo >/dev/null 2>&1; then
        printf "Installing to %s (requires sudo)...\n" "$INSTALL_DIR"
        sudo cp "$src" "${INSTALL_DIR}/${BINARY_NAME}"
        sudo xattr -d com.apple.quarantine "${INSTALL_DIR}/${BINARY_NAME}" 2>/dev/null || true
        printf "Installed to %s/%s\n" "$INSTALL_DIR" "$BINARY_NAME"
    else
        fallback_dir="${HOME}/.local/bin"
        mkdir -p "$fallback_dir"
        cp "$src" "${fallback_dir}/${BINARY_NAME}"
        remove_quarantine "${fallback_dir}/${BINARY_NAME}"
        printf "Installed to %s/%s\n" "$fallback_dir" "$BINARY_NAME"
        case ":$PATH:" in
            *":${fallback_dir}:"*) ;;
            *)
                printf "\nWARNING: %s is not in your PATH.\n" "$fallback_dir"
                printf "Add it with: export PATH=\"%s:\$PATH\"\n" "$fallback_dir"
                ;;
        esac
    fi
}

main
