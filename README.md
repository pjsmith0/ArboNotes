# ArboNotes (Rust)

A Rust/Tauri rewrite of the JavaFX [ArboNotes](../ArboNotes) desktop note-taking
application. It keeps the same note format and data directory, so the two
applications can be pointed at the same data.

For architecture, behavior details, development conventions, and the
append-only request/feature history, see [`AGENTS.md`](AGENTS.md).

## Features

- **Hierarchical notes** – a tree of notes stored in `tree.json`, with
  create, rename, delete, expand/collapse, and drag-and-drop reordering
  (including dropping onto a node to make it a child).
- **Rich-text editor** – [Quill.js](https://quilljs.com/) with the Snow theme,
  including headings, lists, tables, links, images, code blocks, and syntax
  highlighting.
- **Daily notes calendar** – month/year navigation, highlighting of days that
  have notes, and create/open/delete from a context menu. Daily notes use the
  `yyyyMMdd.html` naming convention.
- **Search** – case-insensitive full-text search across all notes, with results
  shown in a dialog.
- **Autosave** – edits are saved shortly after typing stops, and the current
  note is flushed when the window is closed.
- **Plain-file storage** – every note is a raw HTML fragment, so notes remain
  easy to inspect, back up, and edit by hand.

## Requirements

- Rust **1.90+** (stable toolchain)
- A Linux desktop with WebKitGTK, or the platform equivalent for Tauri 2
  (WebView2 on Windows, WKWebView on macOS)
- Build dependencies for Tauri 2 (see the
  [Tauri prerequisites](https://v2.tauri.app/start/prerequisites/))

On Debian/Ubuntu, the usual packages are:

```bash
sudo apt install libwebkit2gtk-4.1-dev build-essential curl wget file \
  libxdo-dev libssl-dev libayatana-appindicator3-dev librsvg2-dev
```

## Run during development

```bash
cargo run
```

The frontend is embedded at compile time from `frontend/`, so after changing
HTML/CSS/JS, rebuild before relaunching. `cargo tauri dev` is not required; the
`tauri-cli` is only needed for packaging.

## Build

```bash
cargo build --release
```

The binary is written to `target/release/arbonotes`.

To build installers/packages (`.deb`, `.rpm`, `.AppImage`, `.msi`, `.dmg`, …),
install the Tauri CLI and run:

```bash
cargo install tauri-cli --version "^2"
cargo tauri build
```

Bundle metadata (product name, identifier, icons, window defaults) lives in
`tauri.conf.json`; platform permissions live in `capabilities/default.json`.

## Prebuilt executables (GitHub Actions)

`.github/workflows/build.yml` builds release executables for Linux and Windows
on every push and pull request targeting `main` (and manually via
**Actions → Build Rust executables → Run workflow**). Each run:

1. installs the Tauri 2 system dependencies (WebKitGTK, GTK, etc.) on Linux,
2. checks formatting with `cargo fmt --check`,
3. runs `cargo test --locked`,
4. builds with `cargo build --release --locked`,
5. uploads the executable as a workflow artifact.

| Artifact | Contents |
| --- | --- |
| `ArboNotes-linux` | `ArboNotes-linux-x86_64.tar.gz` containing `arbonotes` |
| `ArboNotes-windows` | `ArboNotes-windows-x86_64.zip` containing `arbonotes.exe` |

The Linux tarball keeps the executable bit; extract it with
`tar -xzf ArboNotes-linux-x86_64.tar.gz` and run `./arbonotes`. The Linux
binary still needs the WebKitGTK runtime libraries listed under
[Requirements](#requirements).

## Data location and compatibility

By default, data is stored in:

```
~/arbonote_data/
├── tree.json          # note hierarchy
├── <uuid>.html        # regular notes
└── yyyyMMdd.html      # daily notes
```

This is the same layout used by the Java application, and `tree.json` keeps the
same shape:

```json
{
  "nodeName": "Home",
  "fileName": "Root.html",
  "children": [
    { "nodeName": "Projects", "fileName": "0f8c….html", "children": [] }
  ]
}
```

Set `ARBONOTES_ROOT` to use a different data directory (useful for testing or a
portable install):

```bash
ARBONOTES_ROOT=/path/to/data cargo run
```

If the directory or `tree.json` does not exist, ArboNotes creates them on
startup. The Java version exits in that case.

## Differences from the Java version

- Data directory and default tree are created automatically.
- Note file names are validated; paths outside the data directory are rejected.
- Search results are de-duplicated.
- The root node cannot be deleted.
- Right-clicking a tree node selects it before opening the context menu.
- Edits are autosaved and flushed when the window closes.
- The UI is rendered with the system WebView instead of JavaFX WebView.

## Tests

```bash
cargo test
```

Unit tests cover tree/note persistence, path-traversal rejection, daily-note
discovery, case-insensitive chunked search, and Java-compatible name
sanitizing/heading generation.

## License

Apache License 2.0, the same as the original ArboNotes project. See
[`LICENSE`](LICENSE).
