# DevLog

[![CI](https://github.com/vikey89/devlog/actions/workflows/ci.yml/badge.svg)](https://github.com/vikey89/devlog/actions/workflows/ci.yml)
[![Release](https://github.com/vikey89/devlog/actions/workflows/release.yml/badge.svg)](https://github.com/vikey89/devlog/releases)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

**AI-powered dev diary from your git history. One command, zero effort.**

![demo](demo.gif)

DevLog scans your git repos, collects today's commits, and asks an LLM to write a human-readable summary. Daily reports, weekly digests, standup updates — all generated in seconds.

## Quick start

```bash
curl -fsSL https://raw.githubusercontent.com/vikey89/devlog/main/install.sh | sh
devlog init
devlog today
```

## What it does

```
$ devlog today

┌──────────────┬────────┬─────────┬────────────┬───────┐
│ Repo         │ Branch │ Commits │ +/-        │ Files │
├──────────────┼────────┼─────────┼────────────┼───────┤
│ my-api       │ main   │ 5       │ +342 -128  │ 12    │
│ mobile-app   │ feat/  │ 3       │ +89 -14    │ 6     │
└──────────────┴────────┴─────────┴────────────┴───────┘

Today I focused on two areas. In the API, I added the new /users endpoint
with pagination support and wrote integration tests covering edge cases.
On the mobile side, I started the profile screen with avatar upload.
```

## LLM providers

| Provider | Example model | Config `provider` | Env variable | Models list |
|----------|--------------|-------------------|--------------|-------------|
| Anthropic | `claude-sonnet-4-6` | `anthropic` | `ANTHROPIC_API_KEY` | [docs.anthropic.com/en/docs/about-claude/models](https://docs.anthropic.com/en/docs/about-claude/models) |
| OpenAI | `gpt-4o-mini` | `openai` | `OPENAI_API_KEY` | [platform.openai.com/docs/models](https://platform.openai.com/docs/models) |
| Google | `gemini-2.0-flash` | `gemini` | `GEMINI_API_KEY` | [ai.google.dev/gemini-api/docs/models](https://ai.google.dev/gemini-api/docs/models) |

> Copy the **model ID** from the provider's docs and paste it in `model` in your `config.yml`.

## Installation

### Install script (Linux / macOS)

```bash
curl -fsSL https://raw.githubusercontent.com/vikey89/devlog/main/install.sh | sh
```

### GitHub Release

Download the binary for your platform from [Releases](https://github.com/vikey89/devlog/releases).

### From source

```bash
git clone https://github.com/vikey89/devlog.git
cd devlog
./gradlew nativeCompile
# Binary at: build/native/nativeCompile/devlog
```

Fat JAR fallback (requires JDK 21):

```bash
./gradlew shadowJar
java -jar build/libs/devlog-0.1.0-all.jar --help
```

## Configuration

Run `devlog init` for interactive setup, or create `~/.devlog/config.yml` manually:

```yaml
workspaces:
  - /home/user/workspace
  - /home/user/projects
provider: anthropic
model: claude-haiku-4-5-20251001
apiKeyEnv: ANTHROPIC_API_KEY
language: english
author: "Your Name"    # optional — git author name (git config user.name)
```

Make sure the API key is set in your environment:

```bash
export ANTHROPIC_API_KEY=sk-ant-...
```

## Commands

| Command | Description |
|---------|-------------|
| `devlog init` | Interactive setup |
| `devlog today` | What you did today |
| `devlog yesterday` | What you did yesterday |
| `devlog week` | Weekly summary |
| `devlog standup` | Quick standup update |
| `devlog report --from 2026-03-01 --to 2026-03-05` | Custom date range |

### Common options

All generation commands support:

| Option | Description |
|--------|-------------|
| `--raw` | Show stats table only, skip LLM |
| `--format terminal\|markdown\|json\|slack` | Output format (default: terminal) |
| `--copy` | Copy narrative to clipboard |
| `--output FILE` | Save output to file |
| `--workspace PATH` | Override workspace path |
| `--no-cache` | Skip cached results |

### Examples

```bash
# Quick standup, copy to clipboard
devlog standup --copy

# Weekly report in Markdown, save to file
devlog week --format markdown --output weekly.md

# JSON output for scripting
devlog today --format json

# Slack-formatted output
devlog today --format slack

# Stats only, no LLM call
devlog today --raw

# Custom date range
devlog report --from 2026-03-01 --to 2026-03-05
```

## Architecture

Clean Architecture with strict layer separation:

```
presentation/  CLI commands (Clikt) + renderers (Mordant)
    domain/    Use cases + models + repository interfaces
      data/    Git CLI, LLM HTTP clients (Ktor), file cache
```

- Domain layer has zero external dependencies
- All LLM calls use Ktor Client directly (no SDK wrappers)
- DI via Koin

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md). Adding a new LLM provider or output format is a one-file change.

## License

[MIT](LICENSE)
