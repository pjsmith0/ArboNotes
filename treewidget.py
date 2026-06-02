#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Mon Jun  1 20:52:07 2026

@author: pjs
"""

from PySide6.QtCore import Qt, Signal
from PySide6.QtWidgets import (    
    QTreeWidget, QTreeWidgetItem, QMenu, 
    QInputDialog, QMessageBox, QStyle
)

from notedata import NoteData

class NotesTreeWidget(QTreeWidget):
    """Tree widget with context menu for adding siblings/children."""
    nodeSelected = Signal(object)   # emits NoteData
    nodeRenamed  = Signal(object)   # emits NoteData

    def __init__(self, parent=None):
        super().__init__(parent)
        self.setHeaderHidden(True)
        self.setContextMenuPolicy(Qt.CustomContextMenu)
        self.customContextMenuRequested.connect(self._show_context_menu)
        self.currentItemChanged.connect(self._on_selection_changed)
        self._populate_defaults()

    # ── initial demo tree ──────────────────────────────────────────────
    def _populate_defaults(self):
        self._add_top("Home", is_folder=True)
        test_item = self._add_top("test", is_folder=True)

        for child_name in ("aaa", "postgres", "special_paste", "paste_1", "text_wrap"):
            child = QTreeWidgetItem(test_item, [child_name])
            nd = NoteData(child_name)
            child.setData(0, Qt.UserRole, nd)
            child.setIcon(0, self.style().standardIcon(QStyle.StandardPixmap.SP_FileIcon))

        test_item.setExpanded(True)
        self.expandAll()

    def _add_top(self, name: str, is_folder=False) -> QTreeWidgetItem:
        item = QTreeWidgetItem(self, [name])
        nd = NoteData(name)
        item.setData(0, Qt.UserRole, nd)
        if is_folder:
            item.setIcon(0, self.style().standardIcon(QStyle.StandardPixmap.SP_DirIcon))
        else:
            item.setIcon(0, self.style().standardIcon(QStyle.StandardPixmap.SP_FileIcon))
        return item

    # ── context menu ──────────────────────────────────────────────────
    def _show_context_menu(self, pos):
        item = self.itemAt(pos)
        menu = QMenu(self)

        act_add_child   = menu.addAction("Add Child Node")
        act_add_sibling = menu.addAction("Add Sibling Node")
        menu.addSeparator()
        act_rename  = menu.addAction("Rename")
        act_delete  = menu.addAction("Delete")

        action = menu.exec(self.mapToGlobal(pos))
        if action == act_add_child:
            self._add_child(item)
        elif action == act_add_sibling:
            self._add_sibling(item)
        elif action == act_rename and item:
            self._rename(item)
        elif action == act_delete and item:
            self._delete(item)

    def _add_child(self, parent_item):
        name, ok = QInputDialog.getText(self, "New Child", "Node name:")
        if not ok or not name.strip():
            return
        if parent_item is None:
            new_item = QTreeWidgetItem(self, [name])
        else:
            new_item = QTreeWidgetItem(parent_item, [name])
            parent_item.setExpanded(True)
        nd = NoteData(name)
        new_item.setData(0, Qt.UserRole, nd)
        new_item.setIcon(0, self.style().standardIcon(QStyle.StandardPixmap.SP_FileIcon))
        self.setCurrentItem(new_item)

    def _add_sibling(self, item):
        name, ok = QInputDialog.getText(self, "New Sibling", "Node name:")
        if not ok or not name.strip():
            return
        parent = item.parent() if item else None
        if parent:
            new_item = QTreeWidgetItem(parent, [name])
        else:
            new_item = QTreeWidgetItem(self, [name])
        nd = NoteData(name)
        new_item.setData(0, Qt.UserRole, nd)
        new_item.setIcon(0, self.style().standardIcon(QStyle.StandardPixmap.SP_FileIcon))
        self.setCurrentItem(new_item)

    def _rename(self, item):
        old = item.text(0)
        name, ok = QInputDialog.getText(self, "Rename", "New name:", text=old)
        if ok and name.strip():
            item.setText(0, name)
            nd: NoteData = item.data(0, Qt.UserRole)
            if nd:
                nd.name  = name
                nd.title = name
                self.nodeRenamed.emit(nd)

    def _delete(self, item):
        reply = QMessageBox.question(
            self, "Delete", f"Delete '{item.text(0)}'?",
            QMessageBox.Yes | QMessageBox.No)
        if reply == QMessageBox.Yes:
            parent = item.parent() or self.invisibleRootItem()
            parent.removeChild(item)

    def _on_selection_changed(self, current, _previous):
        if current:
            nd: NoteData = current.data(0, Qt.UserRole)
            if nd:
                self.nodeSelected.emit(nd)


