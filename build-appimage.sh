#!/usr/bin/env bash
# Wrap the jpackage app image (target/installer/ArboNotes) into a single-file
# portable AppImage -- the Linux equivalent of the Windows portable .exe.
# No installation needed: make it executable and run it anywhere.
#
# Prerequisites:
#   1. Build the app image first:  mvn clean package -Ppackage-linux
#   2. Have appimagetool available, e.g.:
#        wget -q https://github.com/AppImage/AppImageKit/releases/download/continuous/appimagetool-x86_64.AppImage
#        chmod +x appimagetool-x86_64.AppImage
#
# Usage:
#   APPIMAGE=/path/to/appimagetool ./build-appimage.sh
#   APPIMAGE="/path/to/appimagetool --appimage-extract-and-run" ./build-appimage.sh   (no FUSE systems)
set -euo pipefail

APP_NAME="ArboNotes"
APP_VERSION="${APP_VERSION:-1.0.0}"
ARCH="${ARCH:-$(uname -m)}"
APP_IMAGE="target/installer/${APP_NAME}"
APPDIR="${APP_NAME}.AppDir"
TOOL="${APPIMAGE:-appimagetool}"

if [ ! -d "$APP_IMAGE" ]; then
  echo "App image not found at $APP_IMAGE." >&2
  echo "Run 'mvn clean package -Ppackage-linux' first." >&2
  exit 1
fi

echo "== Building AppImage from $APP_IMAGE =="
rm -rf "$APPDIR"
mkdir -p "$APPDIR"

# Payload: the self-contained jpackage app image (bundled JRE + fat jar).
cp -a "$APP_IMAGE" "$APPDIR/usr"

# AppImage entry point.
cat > "$APPDIR/AppRun" <<EOF
#!/usr/bin/env bash
exec "\$APPDIR/usr/bin/${APP_NAME}" "\$@"
EOF
chmod +x "$APPDIR/AppRun"

# Desktop entry and icon (required by the AppImage format).
cat > "$APPDIR/${APP_NAME}.desktop" <<EOF
[Desktop Entry]
Type=Application
Name=${APP_NAME}
Exec=AppRun
Icon=${APP_NAME}
Comment=ArboNotes tree notes app
Categories=Utility;
EOF
cp src/main/resources/icons/arbonotes.png "$APPDIR/${APP_NAME}.png"

# Build the AppImage (APPIMAGE may include extra runtime flags, e.g. --appimage-extract-and-run).
# shellcheck disable=SC2086
$TOOL "$APPDIR"

mv "${APP_NAME}"-*.AppImage "target/installer/${APP_NAME}-${APP_VERSION}-${ARCH}.AppImage"
rm -rf "$APPDIR"
echo
echo "Done: target/installer/${APP_NAME}-${APP_VERSION}-${ARCH}.AppImage"
echo "Run it with:  ./target/installer/${APP_NAME}-${APP_VERSION}-${ARCH}.AppImage"
