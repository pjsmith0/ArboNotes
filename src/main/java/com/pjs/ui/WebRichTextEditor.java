package com.pjs.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.concurrent.Worker;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

/**
 * Open-source rich text editor (Quill.js, BSD-3-Clause) rendered inside a
 * JavaFX {@link WebView}. Serves as a drop-in replacement for the built-in
 * {@code HTMLEditor}, exposing the same {@link #getHtmlText()} /
 * {@link #setHtmlText(String)} contract.
 */
public class WebRichTextEditor extends Region {

    private final WebView webView = new WebView();
    private final WebEngine engine = webView.getEngine();

    private boolean loaded = false;
    private String pendingHtml;

    public WebRichTextEditor() {
        webView.prefWidthProperty().bind(widthProperty());
        webView.prefHeightProperty().bind(heightProperty());
        webView.setContextMenuEnabled(false);
        getChildren().add(webView);

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

    public WebEngine getEngine() {
        return engine;
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

    private static String toJsStringLiteral(String value) {
        try {
            return new ObjectMapper().writeValueAsString(value);
        } catch (Exception e) {
            return "\"\"";
        }
    }
}
