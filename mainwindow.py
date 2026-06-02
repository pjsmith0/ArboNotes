#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Mon Jun  1 20:57:17 2026

@author: pjs
"""

from PySide6.QtWidgets import (
    QMainWindow, QWidget, QSplitter,
    QVBoxLayout, QHBoxLayout,
    QStatusBar, QLabel,
    QMessageBox
)
from PySide6.QtGui import (
    QFont, QKeySequence
)
from PySide6.QtCore import Qt

from notedata import NoteData
from treewidget import NotesTreeWidget
from editorwidget import EditorWidget

class MainWindow(QMainWindow):
    def __init__(self):
        super().__init__()
        self.setWindowTitle("ArboNote")
        self.resize(1024, 700)
        self._current_node: NoteData | None = None
        self._build_ui()
        self._build_menus()

    # ── UI construction ────────────────────────────────────────────────
    def _build_ui(self):
        # Central splitter
        splitter = QSplitter(Qt.Horizontal, self)
        self.setCentralWidget(splitter)

        # Left panel: tree
        left = QWidget()
        left_layout = QVBoxLayout(left)
        left_layout.setContentsMargins(0, 0, 0, 0)
        left_layout.setSpacing(0)

        # Mini toolbar above tree
        tree_toolbar = QWidget()
        tree_toolbar.setFixedHeight(28)
        tree_toolbar.setStyleSheet("background:#e8e8e8; border-bottom:1px solid #ccc;")
        #tree_toolbar.setStyleSheet("border-bottom:1px solid #ccc;")
        ttl = QHBoxLayout(tree_toolbar)
        ttl.setContentsMargins(4, 2, 4, 2)
        ttl.setSpacing(2)

        lbl = QLabel("Notes Tree")
        lbl.setFont(QFont("SansSerif", 9, QFont.Bold))
        ttl.addWidget(lbl)
        ttl.addStretch()

        left_layout.addWidget(tree_toolbar)

        self.tree = NotesTreeWidget()
        self.tree.setStyleSheet("""
            QTreeWidget {
                border: none;
                font-size: 13px;
                outline: none;
                background: #f7f7f7;
            }
            QTreeWidget::item { padding: 2px 4px; }
            QTreeWidget::item:selected {
                background: #3a7bd5;
                color: white;
            }
        """)
        left_layout.addWidget(self.tree)
        left.setMinimumWidth(180)
        left.setMaximumWidth(320)

        # Collapse button on the divider
        splitter.addWidget(left)

        # Right panel: editor area
        right = QWidget()
        right_layout = QVBoxLayout(right)
        right_layout.setContentsMargins(0, 0, 0, 0)
        right_layout.setSpacing(0)

        # Editor label strip
        editor_label = QWidget()
        editor_label.setFixedHeight(26)
        editor_label.setStyleSheet(
            "background:#f0f0f0; border-bottom:1px solid #ccc;")
        ell = QHBoxLayout(editor_label)
        ell.setContentsMargins(8, 2, 4, 2)
        self._editor_label = QLabel("Editor")
        self._editor_label.setFont(QFont("SansSerif", 9, QFont.Bold))
        ell.addWidget(self._editor_label)
        ell.addStretch()
        right_layout.addWidget(editor_label)

        self.editor = EditorWidget()
        right_layout.addWidget(self.editor)
        splitter.addWidget(right)

        splitter.setSizes([200, 800])
        splitter.setHandleWidth(4)

        # Status bar
        self.status = QStatusBar()
        self.status.setStyleSheet(
            "background:#f0f0f0;")
        self.setStatusBar(self.status)
        self._status_label = QLabel("No note selected")
        self.status.addWidget(self._status_label)

        # Signals
        self.tree.nodeSelected.connect(self._on_node_selected)
        self.tree.nodeRenamed.connect(self._on_node_renamed)
        #btn_add_child.clicked.connect(
        #    lambda: self.tree._add_child(self.tree.currentItem()))
        #btn_add_sibling.clicked.connect(
        #    lambda: self.tree._add_sibling(self.tree.currentItem()
        #                                   or self.tree.topLevelItem(0)))

    def _small_btn(self, text, tip):
        from PySide6.QtWidgets import QPushButton
        btn = QPushButton(text)
        btn.setToolTip(tip)
        btn.setFixedHeight(22)
        btn.setStyleSheet("""
            QPushButton {
                font-size: 11px;
                padding: 1px 6px;
                background: #ddd;
                border: 1px solid #bbb;
                border-radius: 3px;
            }
            QPushButton:hover { background: #ccc; }
        """)
        return btn

    # ── Menu bar ───────────────────────────────────────────────────────
    def _build_menus(self):
        mb = self.menuBar()
        mb.setStyleSheet("background:#f0f0f0;")

        # File
        file_menu = mb.addMenu("&File")
        file_menu.addAction("&New Note", self._new_note,
                            QKeySequence.New)
        file_menu.addAction("&Save",     self._save_note,
                            QKeySequence.Save)
        file_menu.addSeparator()
        file_menu.addAction("&Quit", self.close, QKeySequence.Quit)

        # Help
        help_menu = mb.addMenu("&Help")
        help_menu.addAction("About",
            lambda: QMessageBox.information(
                self, "ArboNote",
                "ArboNote – tree-based note taking.\nBuilt with PySide6."))

    # ── Slots ──────────────────────────────────────────────────────────
    def _on_node_selected(self, nd: NoteData):
        # Save previous note first
        if self._current_node and self._current_node is not nd:
            self.editor.save_current()

        self._current_node = nd
        self.editor.load_note(nd)
        self._status_label.setText(f"Selected: {nd.name}  >>  FileName: {nd.file_name}")

    def _on_node_renamed(self, nd: NoteData):
        if self._current_node is nd:
            self._status_label.setText(f"Selected: {nd.name}  >>  FileName: {nd.file_name}")

    def _new_note(self):
        self.tree._add_child(self.tree.currentItem())

    def _save_note(self):
        def _done(nd):
            self.status.showMessage(f"Saved: {nd.name}", 3000)
        self.editor.save_current(callback=_done)

    def closeEvent(self, event):
        self.editor.save_current()
        event.accept()






