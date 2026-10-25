# AGENTS.md

Instructions and context for AI agents working on **ArboNotes (Rust)**.

## 1. What this application is

ArboNotes is a desktop note-taking application built around a hierarchy of
notes:

- **Left panel** – a tree of notes (`tree.json`), with create, rename, delete,
  expand/collapse, and drag-and-drop reordering.
- **Left panel (bottom)** – a month calendar for daily notes. Days with notes
  are highlighted; right-clicking a day opens, deletes, or copies its date.
- **Right panel** – a Quill.js rich-text editor (headings, lists, tables,
  links, colors, alignment, code blocks, syntax highlighting).
- **Search** – case-insensitive full-text search across the selected subtree.
- **Storage** – plain files under `~/arbonote_data`: `tree.json`, one raw HTML
  fragment per note, and `yyyyMMdd.html` files for daily notes.

This is a Rust/Tauri 2 rewrite of the original JavaFX application at
`/home/pjs/code/ArboNotes`. The Java project is the behavioral reference and
**must not be modified** as part of work on the Rust version. The Rust project
lives in `/home/pjs/code/ArboNotes-Rust`.

## 2. Request log (append-only)

> **Mandatory documentation rule**
>
> Every new feature, behavior change, or user-visible fix must be documented in
> this file in the same change that implements it. Add a new entry to the
> **Request log** below containing:
>
> 1. The user's request, quoted verbatim.
> 2. The date.
> 3. A short summary of what was implemented.
> 4. Any files, commands, data-format changes, or migration notes an agent needs.
>
> Then update the relevant sections of this file (feature inventory,
> architecture, data format, commands, known constraints) so the document stays
> accurate. Do this automatically as part of the change; do not wait to be
> asked and do not treat documentation as optional follow-up work. A feature is
> not complete until this file is updated. Never delete or rewrite old request
> entries; the log is the permanent history of why the application exists and
> what it is expected to do. This ensures any agent can immediately recover the
> original requests and the full feature history.
>
> Template for new entries:
>
> ```markdown
> ### R<N> — <short title> (<YYYY-MM-DD>)
>
> > "<user request, quoted verbatim>"
>
> **Implemented:** <what changed, where, and any migration or testing notes>
> ```

### R1 — Initial conversion request (2026-10-24)

> "can you convert this java application into a rust one? Use a different
> directory to rebuild everything"

**Implemented:** A complete Rust/Tauri 2 rewrite in
`/home/pjs/code/ArboNotes-Rust`, preserving the Java application's behavior and
on-disk format while leaving the Java project untouched. The initial scope was
feature parity with the Java app (derived from its source code and UI):

- hierarchical notes tree loaded from `~/arbonote_data/tree.json`
- Quill.js rich-text editor using raw HTML note fragments
- daily-note calendar with `yyyyMMdd.html` files
- case-insensitive full-text search
- tree drag-and-drop (reparent and reorder)
- tree and calendar context menus
- File/Help menus and About dialog
- status bar, resizable split panes, autosave, close-time save flush
- compatibility with the Java application's data directory and JSON shape

Improvements made during the rewrite:

- creates the data directory and default `tree.json` when missing (Java exited)
- rejects absolute paths and `..` in note file names (path-traversal guard)
- de-duplicates search results
- disables deletion of the root node
- selects the right-clicked tree node before showing its context menu
- autosaves editor changes and flushes them when the window closes

### R2 — Documentation request (2026-10-24)

> "Can you write an AGENTS.md file to describe the app that you just wrote?
> Also add in this file that any additional features must be automatically
> documented in this file so that any agent can immediately get the original
> requests that described this app."

**Implemented:** Added this `AGENTS.md`, including the app overview,
architecture, feature inventory, development commands, constraints, the
append-only request log above, and the mandatory documentation rule.

### R3 — Do not create empty daily notes on calendar click (2026-10-24)

> "I noticed that by clicking on various dates in the calendar, new empty
> files gets generated. This should not be the case. When clicking on a date
> in the calendar, check if a file exists and load it if there is one, if none
> exist, do not create one that is empty. Only create a new file when clicking
> away from a date and the user typed in some text."

**Implemented:** `openDailyNote` in `frontend/app.js` now calls the
`note_exists` IPC command first. An existing `yyyyMMdd.html` is loaded as
before; a missing one is shown as an empty editor without touching the disk.
The file is only created when the editor has non-empty text and the normal
save path runs (autosave, note switch, `Ctrl/Cmd+S`, or window close). Opening
tree notes is unchanged: new tree notes are still created explicitly through
`create_note`.

### R4 — GitHub Actions build for Windows and Linux (2026-10-24)

> "Can you create a github workflows file that will build executables for both
> windows and linux OS's ? See: /home/pjs/code/ArboNotes/ for ideas."

**Implemented:** Added `.github/workflows/build.yml`, modeled on the Java
project's `.github/workflows/build.yml` matrix (Linux + Windows, `push`/`pull_request`
on `main`, `workflow_dispatch`, `fail-fast: false`, per-platform artifact
upload). Instead of Maven it installs the Tauri 2 Linux dependencies, sets up
stable Rust with `dtolnay/rust-toolchain@stable`, caches with
`Swatinem/rust-cache@v2`, runs `cargo fmt --check` (Linux only) and
`cargo test --locked`, builds with `cargo build --release --locked`, and uploads
`ArboNotes-linux-x86_64.tar.gz` (contains `arbonotes`; tar preserves the
executable bit) and `ArboNotes-windows-x86_64.zip` (contains `arbonotes.exe`).
The workflow needs no secrets and no Tauri CLI, because the frontend is embedded
at compile time from `frontend/`. README and AGENTS.md were updated with the CI
details. Note: the repo currently has no git remote, so the workflow only runs
once the project is pushed to GitHub with `main` as the default branch.

### R5 — Publish downloadable builds on a rolling GitHub Release (2026-10-25)

> "Have a look at this repo: https://github.com/pjsmith0/ArboNotes where can I
> download the builds (executables)?"

> "Yes, add that please" (add a stable, sign-in-free download link)

**Implemented:** `.github/workflows/build.yml` gained a `release` job that runs
after `build` on pushes to `main` and on manual dispatch (never on pull
requests). It downloads both platform artifacts, force-moves the `latest` tag to
`$GITHUB_SHA`, creates or updates the **Latest build** release, uploads
`ArboNotes-linux-x86_64.tar.gz` and `ArboNotes-windows-x86_64.zip` with
`--clobber`, marks it as the repository's latest release, and appends the two
download links to the run summary. Because the release is rolling, these URLs
are permanent and need no GitHub login:

- `https://github.com/pjsmith0/ArboNotes/releases/download/latest/ArboNotes-linux-x86_64.tar.gz`
- `https://github.com/pjsmith0/ArboNotes/releases/download/latest/ArboNotes-windows-x86_64.zip`

The pre-existing release pages (`v0.0.1`, `v0.0.2`) only carry auto-generated
source archives, which is why no executables were downloadable before.
Workflow artifacts remain in place for PR builds. The job requests
`permissions: contents: write` (the workflow default is `contents: read`) so it
can push the tag and write release assets. `README.md` now starts with a
"Download a build" section; the release notes and run summary are generated
from the commit subject, short SHA, UTC build time, and run URL. Versioned
releases stay manual and are unaffected. Manually validated by running the
release step's shell script with a stubbed `gh` for both the create and update
paths; YAML parses cleanly.

## 3. Technology and project layout

- **Backend:** Rust 2021, Tauri 2, Serde/Serde JSON, Chrono, Regex,
  Unicode-normalization, UUID, `dirs`.
- **Frontend:** plain HTML/CSS/JavaScript in a Tauri WebView. No bundler and no
  npm build step.
- **Editor:** Quill.js 2 (Snow theme) with the table and syntax modules.
- **Highlighting:** highlight.js.
- **Persistence:** direct filesystem access from Rust; the frontend never
  touches the filesystem.

```text
ArboNotes-Rust/
├── .github/workflows/build.yml  # CI: release executables for Linux + Windows
├── src/
│   ├── main.rs          # thin binary entry point
│   ├── lib.rs           # Tauri builder, state, commands, window-close hook
│   ├── commands.rs      # IPC surface exposed to the frontend
│   ├── config.rs        # data paths, initialization, path validation
│   ├── model.rs         # TreeHierarchyData and SearchTarget
│   ├── storage.rs       # tree/note persistence, daily-note discovery, search
│   ├── search.rs        # chunked case-insensitive file search
│   └── utils.rs         # Java-compatible name sanitizing and headings
├── frontend/
│   ├── index.html       # layout and Quill toolbar
│   ├── app.js           # entire UI controller (~1300 lines)
│   ├── styles.css       # application styling
│   ├── quill/           # vendored Quill.js + Snow CSS + license
│   └── highlight/       # vendored highlight.js + style + license
├── capabilities/default.json  # Tauri permissions (`core:default`)
├── icons/               # RGBA PNG/ICO application icons
├── tauri.conf.json      # window, asset, and bundle configuration
├── Cargo.toml
├── build.rs
└── README.md            # user-facing setup and packaging guide
```

## 4. Data model and compatibility

Default data directory: `~/arbonote_data`. Override it with the
`ARBONOTES_ROOT` environment variable (used for tests and portable installs).

```text
~/arbonote_data/
├── tree.json       # hierarchy; same shape as the Java/Jackson JSON
├── <uuid>.html     # regular note contents (raw HTML fragment)
└── yyyyMMdd.html   # daily notes
```

`tree.json` shape:

```json
{
  "nodeName": "Home",
  "fileName": "Root.html",
  "children": [
    { "nodeName": "Projects", "fileName": "0f8c….html", "children": [] }
  ]
}
```

Rules and compatibility notes:

- `nodeName` is the display name after Java-compatible sanitizing.
- `fileName` is relative to the data directory and normally UUID-based.
- Daily-note discovery is recursive and matches `^(\d{8})\.html$`
  case-insensitively; only dates in the requested year/month are returned.
- Note files contain an HTML fragment, not a full document.
- An empty editor is not written over an existing note.
- Merely opening a daily note never creates a file; the `yyyyMMdd.html` file is
  created by the first save that contains non-empty text.
- The frontend adds internal `_id` fields to the tree in memory only;
  `stripInternal` removes them before saving.
- If `tree.json` is missing, the app creates a default `Home` / `Root.html`
  tree. If the directory is missing, it is created.
- Do not change the JSON field names or daily-note naming without an explicit
  migration plan: the Java app must remain able to read the same data.

## 5. IPC command surface

All commands are registered in `src/lib.rs` and implemented in
`src/commands.rs`:

| Command | Purpose |
| --- | --- |
| `get_config` | Returns `rootPath`, `treeFileName`, `fileExtension` |
| `load_tree` | Reads and parses `tree.json` |
| `save_tree` | Serializes the hierarchy to `tree.json` |
| `load_note` | Reads a note; creates an empty file if absent |
| `save_note` | Writes raw HTML to a note file |
| `note_exists` | Checks whether a note file exists |
| `create_note` | Creates a UUID-named empty note, returns its file name |
| `delete_note` | Deletes a note file (missing files are ignored) |
| `dates_with_notes` | Returns `yyyyMMdd` keys for a month |
| `search_notes` | Searches a list of targets, returns matching targets |
| `sanitize_name` | Java-compatible display/file name sanitizing |
| `heading_for_name` | Builds the initial `<h1>Name</h1>` content |
| `exit_app` | Exits the application |

Tauri permissions are intentionally minimal (`core:default`). If a new
capability is needed, update `capabilities/default.json` and document why.

## 6. Feature inventory and behavior details

### Tree

- Root node cannot be dragged or deleted.
- Single click selects and opens a note; double click reopens it.
- Twisty toggles expansion; the root starts expanded.
- Context menu: **Create…**, **Rename**, **Delete**, **Search**.
- Create prompts for a name, creates a UUID note, sanitizes the name, inserts
  the node, saves the tree, opens the note, and writes an `<h1>` heading.
- Rename changes only `nodeName`; the file name stays stable.
- Delete confirms, removes the node, deletes the file, saves the tree, and
  selects the parent.
- Drag-and-drop:
  - drop on the middle of a row → make the dragged node a child
  - drop near the top/bottom edge → insert before/after that sibling
  - dropping onto the root's edges is ignored
  - dragging a node into its own descendant is ignored
- Tree state (expanded set) is in memory only and is not persisted.

### Editor

- Quill Snow theme; toolbar supports headings, bold/italic/underline/strike,
  text/background colors, alignment, ordered/bulleted lists, indent, blockquote,
  code block, link, clear formatting, and tables (insert/delete rows, columns,
  table).
- Syntax highlighting is provided by highlight.js.
- Autosave fires 600 ms after the last user edit.
- `Ctrl/Cmd+S` forces a save.
- Window blur flushes a pending autosave.
- Closing the window emits `request-close`; the frontend saves, then calls
  `exit_app`. Rust also has a 5-second fallback exit.
- A note is only saved when its stripped text is non-empty and the HTML differs
  from `state.lastSavedHtml`.
- Quill's internal `.ql-ui` elements are stripped before saving.

### Calendar

- Shows a 6×7 grid, locale-aware first day of week and weekday labels.
- Month title plus previous/next month, previous/next year, and **Today**.
- Days with notes get a visual indicator.
- Click/Enter/Space opens the daily note if it exists; otherwise the editor is
  cleared without creating a file. The file appears only after non-empty text
  is saved.
- Right-click menu: **Open note**, **Delete note** (disabled when absent),
  separator, **Copy date** (ISO `yyyy-MM-dd`).
- Deleting the currently open daily note clears the editor.

### Search

- Opened from a tree node's context menu; searches that node and its descendants.
- De-duplicates targets by file name.
- Case-insensitive, chunked file reading (8 KiB buffer) so large notes do not
  need to be loaded whole; matches across chunk boundaries are handled.
- Results show node names; double-click expands the path, closes the dialog,
  selects the node, and scrolls it into view.
- Empty search text does nothing; unreadable/missing files are skipped.

### Menus and dialogs

- Menu bar: **File → Exit**, **Help → About**.
- About shows the version, data directory, and technology stack.
- Modal dialogs: alert, error, confirm (Yes/No/Cancel), prompt, and search.
- `Escape` closes a modal, context menu, or open menu.

### Layout

- Two splitters: tree/calendar vertical split and left/right horizontal split.
- Status bar shows the selected note and file name, or the daily note name.

## 7. Build, run, and test

```bash
cd /home/pjs/code/ArboNotes-Rust
cargo run                 # debug build and launch
cargo build               # rebuild embedded frontend + binary
cargo build --release     # optimized binary at target/release/arbonotes
cargo test                # Rust unit tests
cargo fmt                 # format
cargo fmt --check         # verify formatting
```

Useful test launch:

```bash
ARBONOTES_ROOT=/tmp/arbonotes-test cargo run
```

Packaging (requires the Tauri CLI, not needed for normal development):

```bash
cargo install tauri-cli --version "^2"
cargo tauri build
```

### Continuous integration

`.github/workflows/build.yml` builds release executables on GitHub-hosted
runners and runs on pushes/PRs to `main` plus manual dispatch:

| Runner | Artifact | Contents |
| --- | --- | --- |
| `ubuntu-latest` | `ArboNotes-linux` | `ArboNotes-linux-x86_64.tar.gz` → `arbonotes` |
| `windows-latest` | `ArboNotes-windows` | `ArboNotes-windows-x86_64.zip` → `arbonotes.exe` |

Each job installs the Tauri 2 Linux packages (Linux only), sets up stable Rust,
restores the Cargo cache, runs `cargo fmt --check` (Linux only), `cargo test
--locked`, then `cargo build --release --locked`. Keep the workflow's Linux
package list in sync with the README's Debian/Ubuntu prerequisites, and keep
`--locked` working by committing `Cargo.lock` updates.

A second `release` job (`needs: build`, skipped for pull requests) then
publishes both archives on the rolling **Latest build** release and force-moves
the `latest` tag to the built commit, so these public, login-free URLs always
serve the newest successful `main` build:

- `https://github.com/pjsmith0/ArboNotes/releases/download/latest/ArboNotes-linux-x86_64.tar.gz`
- `https://github.com/pjsmith0/ArboNotes/releases/download/latest/ArboNotes-windows-x86_64.zip`

That job needs `permissions: contents: write` for the tag push and asset
uploads; do not lower it, and do not switch it to `--prerelease`, because
`/releases/latest` would stop resolving to it. Versioned releases (`v0.0.1`,
`v0.0.2`, …) remain manual. Workflow artifacts are kept for PR builds; they
require a signed-in GitHub account and expire after 90 days, unlike release
assets.

## 8. Development constraints and gotchas

- **The frontend is embedded at compile time** from `frontend/` via
  `tauri.conf.json` (`frontendDist`). After editing HTML/CSS/JS you must run
  `cargo build` (or `cargo run`) before relaunching. `cargo tauri dev` is not
  installed in this environment.
- **Rust owns the filesystem.** Add new file operations as Tauri commands in
  `commands.rs`/`storage.rs`; do not use browser filesystem APIs.
- **Preserve the Java data format.** Keep `tree.json` field names, raw HTML
  fragments, and `yyyyMMdd.html` daily-note names unless a migration is
  explicitly requested.
- **Validate note paths.** All note access must go through
  `Config::note_path`, which rejects absolute paths and `..`.
- **Keep errors user-facing.** Commands return `Result<_, String>` and the
  frontend displays errors through `showError`.
- **Serialize backend operations.** The frontend uses an operation chain
  (`enqueue`/`queueSave`) so saves and note switches cannot race.
- **Do not reintroduce source-map comments** in vendored Quill assets; they were
  removed to avoid WebView console warnings.
- **Icons must be RGBA** PNGs; Tauri rejects some other color types.
- **Do not modify the Java project** at `/home/pjs/code/ArboNotes`; read it only
  as a reference for behavior or assets.
- **Vendored assets:** Quill and highlight.js are checked in under
  `frontend/quill` and `frontend/highlight` with their licenses. Avoid adding
  an npm build pipeline unless the task explicitly requires it.
- **Tests:** `cargo test` covers storage, path traversal, daily-note discovery,
  chunked search, and sanitizing/headings. UI behavior is currently verified
  manually against `ARBONOTES_ROOT=/tmp/arbonotes-test`.
- **Window close test:** send `WM_DELETE_WINDOW` rather than killing the
  process, then confirm the app exits and pending editor text was saved.

## 9. Documentation expectations

- Update `README.md` for user-facing setup, build, packaging, or data-format
  changes.
- Update this `AGENTS.md` for architecture, behavior, conventions, commands, and
  every new feature/request. The request log is mandatory and append-only.
- When a feature changes existing behavior, update the relevant section instead
  of only adding a request entry, so the document never contradicts the code.
