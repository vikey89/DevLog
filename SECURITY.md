# Security Policy

## Reporting a Vulnerability

If you discover a security vulnerability in DevLog, please report it responsibly.

**Do NOT open a public GitHub issue.**

Instead, email: **vincenzo.provenza89@gmail.com**

You should receive a response within 48 hours. We will work with you to understand the issue and coordinate a fix before any public disclosure.

## Scope

DevLog handles:
- Git repository data (local filesystem access)
- LLM API keys (read from environment variables, never stored)
- Cached reports (stored in `~/.devlog/cache/`)

If you find a way that DevLog leaks API keys, exposes filesystem contents, or has any other security concern, please report it.
