#!/usr/bin/env python3

from PySide6.QtWidgets import (
    QMainWindow, QWidget, QSplitter,
    QVBoxLayout, QHBoxLayout,
    QStatusBar, QLabel,
    QMessageBox, QAbstractItemView,
    QPushButton
)
from PySide6.QtGui import (
    QFont, QKeySequence
)
from PySide6.QtCore import Qt, Signal

from treeitemdata import TreeItemData
from treewidget import NotesTreeWidget
from editorwidget import EditorWidget
from filerepository import FileRepository

class MainWindow(QMainWindow):

    def __init__(self, file_repository: FileRepository):
        super().__init__()
        self._file_repository = file_repository
        self.setWindowTitle("ArboNote")
        self.resize(1024, 700)
        self._current_node: TreeItemData | None = None
        self._build_ui()
        self._configure_signals()
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
        ttl = QHBoxLayout(tree_toolbar)
        ttl.setContentsMargins(4, 2, 4, 2)
        ttl.setSpacing(2)

        lbl = QLabel("Notes Tree")
        lbl.setFont(QFont("SansSerif", 9, QFont.Bold))
        ttl.addWidget(lbl)
        ttl.addStretch()

        left_layout.addWidget(tree_toolbar)

        self.tree = NotesTreeWidget(self._file_repository)
        self.tree.setSelectionMode(QAbstractItemView.SingleSelection)
        self.tree.setDragEnabled(True)
        self.tree.setAcceptDrops(True)
        self.tree.setDropIndicatorShown(True)
        
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
        ell = QHBoxLayout(editor_label)
        ell.setContentsMargins(8, 2, 4, 2)

        _editor_label = QLabel("Editor")
        _editor_label.setFont(QFont("SansSerif", 9, QFont.Bold))
        ell.addWidget(_editor_label)
        ell.addStretch()

        right_layout.addWidget(editor_label)

        self.editor = EditorWidget(self._file_repository)
        right_layout.addWidget(self.editor)
        splitter.addWidget(right)

        splitter.setSizes([200, 800])
        splitter.setHandleWidth(4)

        # Status bar
        self.status = QStatusBar()
        self.setStatusBar(self.status)
        self._status_label = QLabel("No note selected")
        self.status.addWidget(self._status_label)

    def _configure_signals(self):
        # Signals
        self.tree.node_deselected_signal.connect(self.node_deselected)
        self.tree.node_selected_signal.connect(self.editor.load_note)
        self.tree.node_created_signal.connect(self.editor._text_edit.setHtml)

    def _small_btn(self, text, tip):
        btn = QPushButton(text)
        btn.setToolTip(tip)
        btn.setFixedHeight(22)
        return btn

    # ── Menu bar ───────────────────────────────────────────────────────
    def _build_menus(self):
        mb = self.menuBar()

        # File
        file_menu = mb.addMenu("&File")
        file_menu.addAction("&New Note", self._new_note, QKeySequence.New)
        file_menu.addAction("&Save",     self._save_note, QKeySequence.Save)
        file_menu.addSeparator()
        file_menu.addAction("&Quit", self.close, QKeySequence.Quit)

        # Help
        help_menu = mb.addMenu("&Help")
        help_menu.addAction("About",
            lambda: QMessageBox.information(
                self, "ArboNote",
                "ArboNote – tree-based note taking.\nBuilt with PySide6."))

    # ── Slots ──────────────────────────────────────────────────────────
    def _on_node_selected(self, tree_item_data: TreeItemData):
        # Save previous note first
        if self._current_node and self._current_node is not tree_item_data:
            self.editor.save_current()

        self._current_node = tree_item_data
        #self.editor.load_note(tree_item_data.nodeName)
        #self._status_label.setText(f"Selected: {tree_item_data.nodeName}  >>  FileName: {tree_item_data.fileName}")

    def _on_node_renamed(self, tree_item_data: TreeItemData):
        if self._current_node is tree_item_data:
            self._status_label.setText(f"Selected: {tree_item_data.nodeName}  >>  FileName: {tree_item_data.fileName}")

    def _new_note(self):
        self.tree._add_child(self.tree.currentItem())

    def _save_note(self):
        def _done(nd):
            self.status.showMessage(f"Saved: {nd.name}", 3000)
        self.editor.save_current(callback=_done)

    def closeEvent(self, event):
        self.editor.save_current()
        event.accept()

    def node_deselected(self, tid: TreeItemData):
        self._file_repository.save(tid, self.editor.get_html())




