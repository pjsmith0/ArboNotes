# ArboNote

A JavaFX desktop note-taking app built around a tree of notes:
- **Left panel** – a `TreeView` of your notes (loaded from `tree.json` in `~/arbonote_data/`). Each node is an HTML note saved under `~/arbonote_data/`.
- **Right panel** – a rich-text editor (Quill.js running in `WebView`) with formatting, tables, images, and search.

Notes and their HTML content are stored as plain files on disk under `~/arbonote_data/`, so your data is easy to back up or edit by hand.

## Requirements

- JDK **17** or newer (JDK 17+ ships `jpackage`, used for native packages)
- Maven **3.8+**
- Internet access on first build (Maven downloads JavaFX and plugins)
- To build the Linux `.deb` locally you need Debian/Ubuntu (`fakeroot` + `dpkg`; Arch/other distros can't build it because jpackage validates Debian packages via `dpkg -s`). On non-Debian systems `build-linux.sh` skips the `.deb` automatically — pass `-Dskip.deb=true` to `mvn package -Ppackage-linux` to do the same manually. The `.deb` is always built on the Ubuntu CI runner.
- To build the Linux portable `.AppImage`: [appimagetool](https://github.com/AppImage/AppImageKit) (see `build-appimage.sh`)
- The Windows **portable `.exe`** needs Java **25+** installed on the machine that runs it (it embeds the jar but no JRE)

## Run during development

```bash
mvn javafx:run
```

## Build a runnable jar (any OS)

```bash
mvn clean package
java -jar target/ArboNote-1.0-SNAPSHOT-all.jar
```

## Build a native executable

`jpackage` always builds for the OS it's running on — there is no cross-compiling a Windows `.exe` from Linux or vice versa. Two Maven profiles and two convenience scripts are provided, one per platform.

**Linux** (outputs under `target/installer/`):
```bash
./build-linux.sh
# or directly:
mvn clean package -Ppackage-linux
```
Produces:
- `ArboNote-*.AppImage` — a **single-file portable executable** (bundled JRE, no Java or install needed; `chmod +x` and run)
- `ArboNote-*.deb` — an installer for Debian/Ubuntu
- `target/installer/ArboNote/` — the self-contained app image folder

To build just the AppImage from an existing app image: `APPIMAGE=/path/to/appimagetool ./build-appimage.sh`.

**Windows** (outputs under `target\` and `target\installer\`):
```bat
build-windows.bat
:: or directly:
mvn clean package -Ppackage-windows
```
Produces:
- `target\ArboNote-portable.exe` — a **single-file portable exe** (embeds the jar; no install, but the target machine needs Java **25+**)
- `target\installer\ArboNote\` — a **self-contained portable app** (bundled JRE, no Java needed; run `ArboNote.exe`)

## Building both automatically (CI)

`.github/workflows/build.yml` runs on GitHub Actions with a matrix of `ubuntu-latest` and `windows-latest`, building on every push to `main`:
- **Linux**: `.deb` installer + single-file portable `.AppImage`
- **Windows**: single-file portable `.exe` (Launch4j) + self-contained app image zip

All artifacts are uploaded as workflow artifacts. Pushing a version tag (e.g. `git tag v1.0.0 && git push origin v1.0.0`) also creates a **GitHub Release** with them attached automatically.

## Project layout

```
pom.xml                          Maven build (JavaFX deps + jpackage profiles)
src/main/java/module-info.java   JavaFX module descriptor
src/main/java/com/pjs/App.java   Entry point
src/main/java/com/pjs/ui/…       Tree UI, web editor, dialogs, context menu
src/main/resources/com/pjs/…     Quill.js editor assets, styles
src/main/resources/icons/…     Application icons (arbonote.png)
build-linux.sh / build-windows.bat               One-shot native build scripts
build-appimage.sh               Wraps the app image into a portable AppImage
.github/workflows/build.yml      CI matrix build for both OSes + auto-release
```
