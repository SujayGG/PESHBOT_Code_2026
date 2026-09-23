# The `ftc` MCP server

[`ftc-mcp`](https://github.com/jackulau/ftcMCP) is an MCP server that feeds an AI
assistant ~9,500 lines of verified FTC documentation: the full hardware API,
Pedro Pathing 2.1 (**outdated — this repo uses Pedro 3; don't trust the MCP for Pedro**), FTC Dashboard, Panels, Road Runner, FTCLib, and Gradle
coordinates for every common library.

It matters because models' training data for these libraries is out of date —
Pedro Pathing v1 APIs, wrong import paths, `@Config` misuse. The MCP server
gives the assistant the current API instead of a remembered one.

It provides three tools — `scan_project`, `search_knowledge`, `validate_ftc_code`
— plus 49 documentation resources and 11 workflow prompts
(`create-autonomous`, `create-subsystem`, `tune-pid`, `setup-gradle`, …).

## Requirement

[Node.js 18 or newer](https://nodejs.org). Check with `node --version`. Nothing
else to install — `npx` fetches the server on first use and caches it.

## Claude Code — already done, for everyone

This repo contains a [`.mcp.json`](../.mcp.json) at the root:

```json
{
  "mcpServers": {
    "ftc": {
      "command": "npx",
      "args": ["-y", "ftc-mcp"]
    }
  }
}
```

That is *project scope*: anyone who clones this repo and runs `claude` in it
gets the server, with no setup of their own. [`.claude/settings.json`](../.claude/settings.json)
pre-approves it, so Claude Code won't prompt for confirmation on first run.

Verify it inside Claude Code with:

```
/mcp
```

You should see `ftc` listed as connected.

### About `claude mcp add ftc -- npx ftc-mcp`

That command registers the server in **local scope** — your machine, your user,
and by default only in the directory where you ran it. It won't reach the rest
of the team, which is why the checked-in `.mcp.json` exists. The two can
coexist; if you'd rather not have the duplicate, remove yours:

```bash
claude mcp remove ftc
claude mcp list          # confirm what's registered and in which scope
```

The `.mcp.json` in the repo keeps working either way.

## Other editors

MCP is an open protocol, so the same server works anywhere. It is not
Claude-only, and it is not tied to the Claude CLI.

**VS Code (Copilot)** — already configured in [`.vscode/mcp.json`](../.vscode/mcp.json);
just open the repo.

**Cursor** — add to `~/.cursor/mcp.json`, or `.cursor/mcp.json` in the repo:

```json
{ "mcpServers": { "ftc": { "command": "npx", "args": ["-y", "ftc-mcp"] } } }
```

**Windsurf / Zed / other MCP clients** — same `mcpServers` block, in that
client's MCP config file.

**Android Studio (Gemini)** — Studio's own assistant supports MCP from the
Narwhal releases onward. Enable it under
**Settings → Tools → AI → MCP Servers → Enable MCP Servers**, then add the same
`mcpServers` block. Studio reads it from `mcp.json` in its configuration
directory:

- macOS: `~/Library/Application Support/Google/AndroidStudio<version>/mcp.json`
- Windows: `%APPDATA%\Google\AndroidStudio<version>\mcp.json`
- Linux: `~/.config/Google/AndroidStudio<version>/mcp.json`

Whether your Studio build accepts a `command`/`args` (stdio) server or only a
remote `httpUrl` endpoint depends on the version — check the MCP Servers
settings page after pasting it. See
[Google's docs](https://developer.android.com/studio/gemini/add-mcp-server).
This is optional: our AI workflow runs through Claude Code in a terminal, and
Android Studio is for Gradle syncs, deploys, and logcat.

## Using it well

Start a session with a scan so the assistant knows what's actually installed:

> Scan the project and tell me what libraries and OpModes exist.

Then work in plain language. Good prompts for this project:

- "Create a field-centric mecanum TeleOp with slow mode on the right bumper."
- "Add a lift subsystem with dashboard-tunable target positions."
- "Set up Pedro Pathing and FTC Dashboard in the Gradle files."
- "My loop times are ~40 ms — what should I change?"
- "Validate `TeleOpMain.java` against common FTC mistakes."

Ask it to run `validate_ftc_code` on anything new, and always finish with
`./gradlew assembleDebug` before you take code to the field.

## Troubleshooting

| Symptom | Fix |
|---|---|
| `/mcp` shows nothing or `ftc` failed | Run `node --version`; needs 18+ |
| Hangs on first use | `npx` is downloading the package — give it a minute |
| Still stuck | `npx -y ftc-mcp` in a terminal; it should start and wait on stdin (Ctrl-C to exit) |
| Suggested APIs look wrong | Ask it to `search_knowledge` before writing code — it may be answering from memory |
