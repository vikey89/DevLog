# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.1.0] - 2026-03-15

### Added
- Daily, yesterday, weekly, standup, and custom date range reports
- LLM providers: Anthropic, OpenAI, Google Gemini
- Output formats: terminal (colored ASCII), markdown, JSON, Slack
- File-based caching to avoid redundant LLM calls
- Clipboard support (`--copy`)
- File output (`--output FILE`)
- Raw mode (`--raw`) for stats-only without LLM
- Interactive setup via `devlog init`
- GraalVM native image support (Linux, macOS, Windows)
- Install script for Linux and macOS
