# ArboNotes

A JavaFX desktop note-taking app built around a tree of notes:
- **Left panel** – a `TreeView` of your notes (loaded from `tree.json` in `~/arbonote_data/`). Each node is an HTML note saved under `~/arbonote_data/`.
- **Right panel** – a rich-text editor (Quill.js running in a JavaFX `WebView`) with formatting, tables, images, and search.

Notes and their HTML content are stored as plain files on disk under `~/arbonote_data/`, so your data is easy to back up or edit by hand.

## Requirements

- JDK **25** or newer (minimum version to compile and run)
- Maven **3.8+**
- Internet access on first build (Maven downloads JavaFX and other dependencies)

The build produces a single self-contained **uber jar** — no native installers or packages are built.

## Run during development

```bash
mvn javafx:run
```

## Build and run the uber jar

```bash
mvn clean package
java -jar target/ArboNotes-1.0-SNAPSHOT-all.jar
```

The jar bundles all dependencies (including the platform-correct JavaFX natives, resolved automatically for the OS the build runs on), but it does **not** embed a JRE — the target machine needs Java **25+** installed.

## CI

`.github/workflows/build.yml` runs on GitHub Actions with a matrix of `ubuntu-latest` and `windows-latest`, building the uber jar on every push to `main` and on pull requests. Each runner's jar is uploaded as a workflow artifact (`ArboNotes-linux` / `ArboNotes-win`).
