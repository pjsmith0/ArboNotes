#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Mon Jun  1 20:56:15 2026

@author: pjs
"""


EDITOR_HTML = """<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<style>
  * { box-sizing: border-box; margin: 0; padding: 0; }
  body {
    font-family: SansSerif, Arial, sans-serif;
    font-size: 14px;
    background: #ffffff;
    color: #000000;
    display: flex;
    flex-direction: column;
    height: 100vh;
    overflow: hidden;
  }

  /* ── Toolbar ── */
  #toolbar {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 2px;
    padding: 4px 6px;
    background: #f0f0f0;
    border-bottom: 1px solid #ccc;
    user-select: none;
    flex-shrink: 0;
  }

  .tb-group {
    display: flex;
    align-items: center;
    gap: 1px;
    margin-right: 4px;
  }

  .tb-sep {
    width: 1px;
    height: 22px;
    background: #aaa;
    margin: 0 4px;
  }

  select, .font-size-box {
    height: 26px;
    border: 1px solid #aaa;
    background: #fff;
    font-size: 12px;
    padding: 0 4px;
    cursor: pointer;
    border-radius: 2px;
  }

  #fontFamily { width: 120px; }
  #fontFormat { width: 110px; }
  #fontSize   { width: 52px; }

  .tb-btn {
    width: 28px;
    height: 26px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    border: 1px solid transparent;
    background: transparent;
    cursor: pointer;
    border-radius: 2px;
    font-size: 12px;
    font-weight: bold;
    color: #333;
    transition: background 0.1s, border-color 0.1s;
    white-space: nowrap;
    flex-shrink: 0;
  }
  .tb-btn:hover  { background: #ddd; border-color: #aaa; }
  .tb-btn:active { background: #bbb; }
  .tb-btn.active { background: #c8d8f0; border-color: #7a9fcc; }

  /* ── Title row ── */
  #title-row {
    padding: 10px 16px 0;
    flex-shrink: 0;
  }
  #note-title {
    font-size: 22px;
    font-weight: bold;
    outline: none;
    border: none;
    width: 100%;
    font-family: inherit;
    color: #111;
  }
  #note-title:empty::before {
    content: attr(data-placeholder);
    color: #aaa;
    pointer-events: none;
  }
  #title-divider {
    height: 1px;
    background: #ccc;
    margin: 6px 16px 0;
    flex-shrink: 0;
  }

  /* ── Editor ── */
  #editor {
    flex: 1;
    padding: 12px 16px;
    outline: none;
    overflow-y: auto;
    line-height: 1.6;
    word-wrap: break-word;
  }
  #editor:empty::before {
    content: "Start typing…";
    color: #bbb;
    pointer-events: none;
  }
  #editor pre, #editor code {
    background: #f5f5f5;
    padding: 2px 4px;
    border-radius: 3px;
    font-family: monospace;
  }
  #editor blockquote {
    border-left: 3px solid #ccc;
    padding-left: 10px;
    color: #555;
  }
</style>
</head>
<body>

<div id="toolbar">
  <!-- Font family -->
  <select id="fontFamily" onchange="execCmd('fontName', this.value)">
    <option>SansSerif</option>
    <option>Serif</option>
    <option>Monospace</option>
    <option>Arial</option>
    <option>Times New Roman</option>
    <option>Courier New</option>
    <option>Georgia</option>
    <option>Verdana</option>
    <option>Tahoma</option>
  </select>

  <!-- Paragraph format -->
  <select id="fontFormat" onchange="applyFormat(this.value); this.value=''">
    <option value="">Formatting</option>
    <option value="p">Normal</option>
    <option value="h1">Heading 1</option>
    <option value="h2">Heading 2</option>
    <option value="h3">Heading 3</option>
    <option value="h4">Heading 4</option>
    <option value="pre">Preformatted</option>
    <option value="blockquote">Blockquote</option>
  </select>

  <!-- Font size -->
  <select id="fontSize" onchange="execCmd('fontSize', this.value)">
    <option value="1">8</option>
    <option value="2">10</option>
    <option value="3">12</option>
    <option value="4" selected>14</option>
    <option value="5">18</option>
    <option value="6">24</option>
    <option value="7">36</option>
  </select>

  <div class="tb-sep"></div>

  <!-- Table buttons -->
  <div class="tb-group">
    <button class="tb-btn" title="Insert Table"        onclick="insertTable()">⊞</button>
    <button class="tb-btn" title="Insert Row"          onclick="insertTableRow()">⊟</button>
    <button class="tb-btn" title="Delete Row"          onclick="deleteTableRow()">✕</button>
    <button class="tb-btn" title="Insert Column"       onclick="insertTableCol()">⊡</button>
    <button class="tb-btn" title="Delete Column"       onclick="deleteTableCol()">⊠</button>
    <button class="tb-btn" title="Merge Cells"         onclick="mergeCells()">⊟</button>
    <button class="tb-btn" title="Split Cells"         onclick="splitCells()">⊞</button>
  </div>

  <div class="tb-sep"></div>

  <!-- Undo / Redo / HTML -->
  <div class="tb-group">
    <button class="tb-btn" title="Undo (Ctrl+Z)"       onclick="execCmd('undo')">↩</button>
    <button class="tb-btn" title="Redo (Ctrl+Y)"       onclick="execCmd('redo')">↪</button>
    <button class="tb-btn" title="View HTML"           onclick="viewHTML()" style="font-size:10px;width:36px">html</button>
  </div>

  <!-- Second row -->
  <div style="width:100%;height:0;flex-basis:100%"></div>

  <div class="tb-group">
    <button class="tb-btn" id="btn-bold"   title="Bold (Ctrl+B)"   onclick="toggleFmt('bold','btn-bold')"><b>B</b></button>
    <button class="tb-btn" id="btn-italic" title="Italic (Ctrl+I)" onclick="toggleFmt('italic','btn-italic')"><i>I</i></button>
    <button class="tb-btn" id="btn-uline"  title="Underline (Ctrl+U)" onclick="toggleFmt('underline','btn-uline')"><u>U</u></button>
    <button class="tb-btn" title="Highlight" onclick="execCmd('hiliteColor','#FFFF88')">🖍</button>
  </div>

  <div class="tb-sep"></div>

  <div class="tb-group">
    <button class="tb-btn" title="Align Left"    onclick="execCmd('justifyLeft')">≡</button>
    <button class="tb-btn" title="Align Center"  onclick="execCmd('justifyCenter')">≡</button>
    <button class="tb-btn" title="Blockquote"    onclick="execCmd('formatBlock','blockquote')">"</button>
    <button class="tb-btn" title="Ordered List"  onclick="execCmd('insertOrderedList')">1.</button>
    <button class="tb-btn" title="Unordered List" onclick="execCmd('insertUnorderedList')">•</button>
  </div>

  <div class="tb-sep"></div>

  <div class="tb-group">
    <button class="tb-btn" title="Insert Link"       onclick="insertLink()">🔗</button>
    <button class="tb-btn" title="Remove Link"       onclick="execCmd('unlink')">⛓</button>
    <button class="tb-btn" title="Insert Image"      onclick="insertImage()">🖼</button>
    <button class="tb-btn" title="Horizontal Rule"   onclick="execCmd('insertHorizontalRule')">—</button>
  </div>

  <div class="tb-sep"></div>

  <div class="tb-group">
    <button class="tb-btn" title="Align Left"      onclick="execCmd('justifyLeft')">⬅</button>
    <button class="tb-btn" title="Align Center"    onclick="execCmd('justifyCenter')">↔</button>
    <button class="tb-btn" title="Align Right"     onclick="execCmd('justifyRight')">➡</button>
    <button class="tb-btn" title="Justify"         onclick="execCmd('justifyFull')">⬌</button>
  </div>

  <div class="tb-sep"></div>

  <div class="tb-group">
    <button class="tb-btn" title="Indent"   onclick="execCmd('indent')">→|</button>
    <button class="tb-btn" title="Outdent"  onclick="execCmd('outdent')">|←</button>
    <button class="tb-btn" title="Code"     onclick="insertCode()" style="font-size:10px">&lt;/&gt;</button>
  </div>
</div>

<!-- Title -->
<div id="title-row">
  <div id="note-title" contenteditable="true" data-placeholder="Untitled note"
       oninput="titleChanged(this.innerText)"></div>
</div>
<div id="title-divider"></div>

<!-- Body -->
<div id="editor" contenteditable="true" spellcheck="true"></div>

<script>
/* ── helpers ── */
function execCmd(cmd, val) {
  document.getElementById('editor').focus();
  document.execCommand(cmd, false, val || null);
}

function applyFormat(tag) {
  if (!tag) return;
  execCmd('formatBlock', tag);
}

function toggleFmt(cmd, btnId) {
  execCmd(cmd);
  document.getElementById(btnId).classList.toggle('active',
    document.queryCommandState(cmd));
}

function insertLink() {
  const url = prompt('URL:', 'https://');
  if (url) execCmd('createLink', url);
}

function insertImage() {
  const url = prompt('Image URL:', 'https://');
  if (url) execCmd('insertImage', url);
}

function insertCode() {
  const sel = window.getSelection();
  const range = sel.getRangeAt(0);
  const code = document.createElement('code');
  code.textContent = sel.toString() || 'code';
  range.deleteContents();
  range.insertNode(code);
}

function insertTable() {
  const rows = parseInt(prompt('Rows:', '3')) || 3;
  const cols = parseInt(prompt('Columns:', '3')) || 3;
  let html = '<table border="1" cellpadding="4" cellspacing="0" style="border-collapse:collapse;width:100%"><tbody>';
  for (let r = 0; r < rows; r++) {
    html += '<tr>';
    for (let c = 0; c < cols; c++) html += '<td>&nbsp;</td>';
    html += '</tr>';
  }
  html += '</tbody></table><p><br></p>';
  execCmd('insertHTML', html);
}

function insertTableRow() { execCmd('insertParagraph'); /* placeholder */ }
function deleteTableRow()  { /* placeholder */ }
function insertTableCol()  { /* placeholder */ }
function deleteTableCol()  { /* placeholder */ }
function mergeCells()      { /* placeholder */ }
function splitCells()      { /* placeholder */ }

function viewHTML() {
  const editor = document.getElementById('editor');
  const current = editor.getAttribute('data-html-mode') === '1';
  if (!current) {
    const raw = document.createElement('pre');
    raw.id = 'html-view';
    raw.style.cssText = 'white-space:pre-wrap;padding:12px;outline:none;flex:1;overflow-y:auto;font-family:monospace;font-size:12px;background:#f9f9f9;';
    raw.contentEditable = true;
    raw.textContent = editor.innerHTML;
    editor.style.display = 'none';
    editor.parentNode.insertBefore(raw, editor.nextSibling);
    editor.setAttribute('data-html-mode', '1');
  } else {
    const raw = document.getElementById('html-view');
    if (raw) { editor.innerHTML = raw.textContent; raw.remove(); }
    editor.style.display = '';
    editor.setAttribute('data-html-mode', '0');
  }
}

function titleChanged(txt) {
  if (window.pyBridge) window.pyBridge.titleChanged(txt);
}

/* Keep format buttons in sync */
document.getElementById('editor').addEventListener('keyup', syncState);
document.getElementById('editor').addEventListener('mouseup', syncState);
function syncState() {
  ['bold','italic','underline'].forEach(cmd => {
    const map = {bold:'btn-bold', italic:'btn-italic', underline:'btn-uline'};
    const el = document.getElementById(map[cmd]);
    if (el) el.classList.toggle('active', document.queryCommandState(cmd));
  });
}

/* ── Python bridge API (called from Qt) ── */
function setTitle(t)   { document.getElementById('note-title').innerText = t; }
function setContent(h) { document.getElementById('editor').innerHTML = h; }
function getContent()  { return document.getElementById('editor').innerHTML; }
function getTitle()    { return document.getElementById('note-title').innerText; }
</script>
</body>
</html>
"""

from PySide6.QtWebEngineWidgets import QWebEngineView
from PySide6.QtWebEngineCore import QWebEngineSettings
from PySide6.QtCore import QUrl
from notedata import NoteData

class EditorWidget(QWebEngineView):
    """QWebEngineView wrapping the contenteditable HTML editor."""

    def __init__(self, parent=None):
        super().__init__(parent)
        settings = self.settings()
        settings.setAttribute(QWebEngineSettings.JavascriptEnabled, True)
        settings.setAttribute(QWebEngineSettings.LocalContentCanAccessRemoteUrls, True)
        self.setHtml(EDITOR_HTML, QUrl("qrc:/"))
        self._current_node: NoteData | None = None
        self.loadFinished.connect(self._on_load)

    def _on_load(self, ok):
        if ok and self._current_node:
            self._load_node(self._current_node)

    def load_note(self, nd: NoteData):
        self._current_node = nd
        self._load_node(nd)

    def _load_node(self, nd: NoteData):
        safe_title   = nd.title.replace("'", "\\'").replace("\n", "")
        safe_content = nd.content.replace("'", "\\'").replace("\n", "\\n")
        js = f"setTitle('{safe_title}'); setContent('{safe_content}');"
        self.page().runJavaScript(js)

    def save_current(self, callback=None):
        if self._current_node is None:
            return
        nd = self._current_node
        def _save_content(html):
            nd.content = html or ""
            def _save_title(title):
                if title:
                    nd.title = title
                if callback:
                    callback(nd)
            self.page().runJavaScript("getTitle()", _save_title)
        self.page().runJavaScript("getContent()", _save_content)