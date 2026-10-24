package com.pjs.ui;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.concurrent.Worker;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

/**
 * Open-source rich text editor (Quill.js, BSD-3-Clause) rendered inside a
 * JavaFX {@link WebView}. Serves as a drop-in replacement for the built-in
 * {@code HTMLEditor}, exposing the same {@link #getHtmlText()} /
 * {@link #setHtmlText(String)} contract.
 *
 * <p>Provides a right-click context menu (Cut / Copy / Paste) that operates on
 * the operating-system clipboard, so text exchanged with native applications
 * (e.g. Notepad on Windows, gedit on Linux) works in both directions.</p>
 */
public class WebRichTextEditor extends Region {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final boolean IS_LINUX =
            System.getProperty("os.name", "").toLowerCase(java.util.Locale.ROOT).startsWith("linux");

    private final WebView webView = new WebView();
    private final WebEngine engine = webView.getEngine();

    private final ContextMenu contextMenu = new ContextMenu();
    private final MenuItem cutItem = new MenuItem("Cut");
    private final MenuItem copyItem = new MenuItem("Copy");
    private final MenuItem pasteItem = new MenuItem("Paste");

    private boolean loaded = false;
    private String pendingHtml;

    // Selection state captured when the context menu is requested; the popup
    // steals focus from the WebView, so the live DOM selection can no longer
    // be queried reliably once a menu item is clicked.
    private Integer cachedIndex;
    private int cachedLength;
    private String cachedText;
    private String cachedHtml;

    public WebRichTextEditor() {
        webView.prefWidthProperty().bind(widthProperty());
        webView.prefHeightProperty().bind(heightProperty());
        webView.setContextMenuEnabled(false);
        getChildren().add(webView);

        // On Linux the GTK backend reports the numeric keypad's Enter key as
        // KeyCode.ENTER with no text (the main Enter key carries "\r"), and
        // delivers no character, so Quill's keydown handler never sees it.
        // Replay those presses through the page, which dispatches a synthetic
        // Enter into Quill's own keyboard pipeline.
        webView.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            String text = e.getText();
            if (IS_LINUX && e.getCode() == KeyCode.ENTER
                    && (text == null || text.isEmpty())) {
                e.consume();
                execJs("window.insertNumpadEnter(" + e.isShiftDown() + ", "
                        + e.isControlDown() + ", " + e.isAltDown() + ", "
                        + e.isMetaDown() + ")");
                return;
            }
            handleClipboardShortcut(e);
        });

        cutItem.setOnAction(e -> copyToSystemClipboard(true));
        copyItem.setOnAction(e -> copyToSystemClipboard(false));
        pasteItem.setOnAction(e -> pasteFromSystemClipboard());
        contextMenu.getItems().addAll(cutItem, copyItem, pasteItem);

        webView.setOnContextMenuRequested(e -> {
            cacheSelectionState();
            cutItem.setDisable(cachedLength <= 0);
            copyItem.setDisable(cachedLength <= 0);
            Clipboard systemClipboard = Clipboard.getSystemClipboard();
            pasteItem.setDisable(!systemClipboard.hasString() && !systemClipboard.hasHtml());
            contextMenu.show(webView, e.getScreenX(), e.getScreenY());
            e.consume();
        });

        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                loaded = true;
                if (pendingHtml != null) {
                    String html = pendingHtml;
                    pendingHtml = null;
                    setHtmlText(html);
                }
            } else if (newState == Worker.State.FAILED) {
                System.err.println("Failed to load editor page: " + engine.getLoadWorker().getException());
            }
        });

        engine.load(getClass().getResource("/com/pjs/editor.html").toExternalForm());
    }

    public String getHtmlText() {
        if (!loaded) {
            return "";
        }
        Object result = engine.executeScript("window.getContent()");
        return result == null ? "" : result.toString();
    }

    public void setHtmlText(String html) {
        if (html == null) {
            return;
        }
        if (!loaded) {
            pendingHtml = html;
            return;
        }
        engine.executeScript("window.setContent(" + toJsStringLiteral(html) + ")");
    }

    /**
     * Handles Ctrl+C / Ctrl+X / Ctrl+V (Cmd on macOS) inside the Quill editor.
     * WebView's built-in clipboard integration is unreliable here, so these
     * shortcuts go through the same manual system-clipboard path as the
     * context menu. When focus is elsewhere (e.g. the link tooltip's input)
     * the event is left untouched so native text-field behaviour still works.
     */
    private void handleClipboardShortcut(KeyEvent event) {
        KeyCode code = event.getCode();
        if (code != KeyCode.C && code != KeyCode.X && code != KeyCode.V) {
            return;
        }
        if (!event.isShortcutDown() || event.isAltDown() || !isEditorFocused()) {
            return;
        }
        switch (code) {
            case C -> {
                cacheSelectionState();
                copyToSystemClipboard(false);
            }
            case X -> {
                cacheSelectionState();
                copyToSystemClipboard(true);
            }
            case V -> {
                cacheSelectionState();
                pasteFromSystemClipboard();
            }
            default -> {
                return;
            }
        }
        event.consume();
    }

    private boolean isEditorFocused() {
        return Boolean.TRUE.equals(evalJs("window.isEditorFocused()"));
    }

    /**
     * Reads the current Quill selection and the selected plain-text/HTML
     * content from the page. Must run while the WebView still has focus
     * (i.e. before the context menu popup is shown or a shortcut is handled).
     */
    private void cacheSelectionState() {
        cachedIndex = null;
        cachedLength = 0;
        cachedText = null;
        cachedHtml = null;
        if (!loaded) {
            return;
        }
        try {
            Object selJson = engine.executeScript("window.getSelectionJson()");
            if (!(selJson instanceof String json)) {
                return;
            }
            JsonNode node = MAPPER.readTree(json);
            cachedIndex = node.path("index").asInt(0);
            cachedLength = node.path("length").asInt(0);
            if (cachedLength > 0) {
                cachedText = asStringOrNull(engine.executeScript("window.getSelectedText()"));
                cachedHtml = asStringOrNull(engine.executeScript("window.getSelectedHtml()"));
            }
        } catch (Exception e) {
            cachedIndex = null;
            cachedLength = 0;
        }
    }

    private void copyToSystemClipboard(boolean deleteAfter) {
        if (cachedIndex == null || cachedLength <= 0) {
            return;
        }
        ClipboardContent content = new ClipboardContent();
        if (cachedText != null) {
            content.putString(cachedText);
        }
        if (cachedHtml != null) {
            content.putHtml(cachedHtml);
        }
        if (!content.isEmpty()) {
            Clipboard.getSystemClipboard().setContent(content);
        }
        if (deleteAfter) {
            execJs("window.deleteRange(" + cachedIndex + "," + cachedLength + ")");
        }
        cachedIndex = null;
        cachedLength = 0;
        cachedText = null;
        cachedHtml = null;
    }

    private void pasteFromSystemClipboard() {
        if (cachedIndex == null) {
            return;
        }
        int index = cachedIndex;
        int length = cachedLength;
        cachedIndex = null;
        Clipboard systemClipboard = Clipboard.getSystemClipboard();
        String html = systemClipboard.hasHtml() ? systemClipboard.getHtml() : null;
        if (html != null && !html.isBlank()) {
            execJs("window.pasteHtml(" + index + "," + length + ","
                    + toJsStringLiteral(html) + ")");
        } else if (systemClipboard.hasString()) {
            execJs("window.pasteText(" + index + "," + length + ","
                    + toJsStringLiteral(systemClipboard.getString()) + ")");
        }
    }

    private void execJs(String script) {
        evalJs(script);
    }

    private Object evalJs(String script) {
        if (!loaded) {
            return null;
        }
        try {
            return engine.executeScript(script);
        } catch (Exception e) {
            System.err.println("Editor script failed: " + e.getMessage());
            return null;
        }
    }

    private static String asStringOrNull(Object value) {
        return value instanceof String s ? s : null;
    }

    private static String toJsStringLiteral(String value) {
        try {
            return new ObjectMapper().writeValueAsString(value);
        } catch (Exception e) {
            return "\"\"";
        }
    }
}
