package com.pjs.ui;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.concurrent.Worker;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
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
     * Reads the current Quill selection and the selected plain-text/HTML
     * content from the page. Must run while the WebView still has focus
     * (i.e. before the context menu popup is shown).
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
        cachedIndex = null;
        Clipboard systemClipboard = Clipboard.getSystemClipboard();
        String html = systemClipboard.hasHtml() ? systemClipboard.getHtml() : null;
        if (html != null && !html.isBlank()) {
            execJs("window.pasteHtml(" + index + "," + toJsStringLiteral(html) + ")");
        } else if (systemClipboard.hasString()) {
            execJs("window.pasteText(" + index + "," + toJsStringLiteral(systemClipboard.getString()) + ")");
        }
    }

    private void execJs(String script) {
        if (!loaded) {
            return;
        }
        try {
            engine.executeScript(script);
        } catch (Exception e) {
            System.err.println("Editor script failed: " + e.getMessage());
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
