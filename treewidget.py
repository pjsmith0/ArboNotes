#!/usr/bin/env python3
import os
from operator import concat

from PySide6.QtCore import Qt, Signal
from PySide6.QtWidgets import (    
    QTreeWidget, QTreeWidgetItem, QMenu, 
    QInputDialog, QMessageBox, QStyle
)
import json

from filerepository import FileRepository
from notedata import NoteData
from treeitemdata import TreeItemData
import utils
import datamapper

class NotesTreeWidget(QTreeWidget):
    node_deselected_signal = Signal(TreeItemData)
    node_selected_signal = Signal(TreeItemData)
    node_created_signal = Signal(str)

    def __init__(self, file_repository: FileRepository, parent=None):
        super().__init__(parent)

        self._file_repository = file_repository
        self.setHeaderHidden(True)
        self.setContextMenuPolicy(Qt.CustomContextMenu)
        self.customContextMenuRequested.connect(self._show_context_menu)
        self.currentItemChanged.connect(self._on_selection_changed)
        self._populate_defaults()
        self._set_topmost_tree_item()

    def _populate_defaults(self):
        tree_file_content = self._file_repository.get_tree_data()
        self._pretty_print(tree_file_content)
        tree_data = NoteData.from_dict(tree_file_content)
        self._add_item(None, tree_data)

    def _add_item(self, parent_item, note_data):
        tid = datamapper.map_notedata_to_treeitemdata(note_data)
        new_item = QTreeWidgetItem([tid.nodeName])
        new_item.setData(0, Qt.UserRole, tid)
        new_item.setExpanded(True)
        
        if parent_item is None:
            self.addTopLevelItem(new_item)
        else:
            parent_item.addChild(new_item)
        
        for child in note_data.children:
            self._add_item(new_item, child)

    # ── context menu ──────────────────────────────────────────────────
    def _show_context_menu(self, pos):
        item = self.itemAt(pos)
        menu = QMenu(self)

        if item and item.parent() is None:
            act_add_child   = menu.addAction("Add Child Node")
            menu.addSeparator()
            act_rename  = menu.addAction("Rename")

            action = menu.exec(self.mapToGlobal(pos))
            if action == act_add_child:
                self._add_child(item)
            elif action == act_rename and item:
                self._rename(item)

        else:
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

        text_header = self._generate_header_from_name(name)
        tree_item_data = TreeItemData(name, self._file_repository.create_file(text_header))
        new_item.setData(0, Qt.UserRole, tree_item_data)
        new_item.setIcon(0, self.style().standardIcon(QStyle.StandardPixmap.SP_FileIcon))
        self.setCurrentItem(new_item)
        self._file_repository.save_tree_data(self._serialize_tree())
        self.node_created_signal.emit(text_header)

    def _add_sibling(self, item):
        name, ok = QInputDialog.getText(self, "New Sibling", "Node name:")
        if not ok or not name.strip():
            return
        parent = item.parent() if item else None
        if parent:
            new_item = QTreeWidgetItem(parent, [name])
        else:
            new_item = QTreeWidgetItem(self, [name])

        tree_item_data = TreeItemData(name, utils.generate_file())
        new_item.setData(0, Qt.UserRole, tree_item_data)
        new_item.setIcon(0, self.style().standardIcon(QStyle.StandardPixmap.SP_FileIcon))
        self.setCurrentItem(new_item)
        self._file_repository.save_tree_data(self._serialize_tree())

    def _rename(self, item):
        old = item.text(0)
        name, ok = QInputDialog.getText(self, "Rename", "New name:", text=old)
        if ok and name.strip():
            item.setText(0, name)
            tree_item_data: TreeItemData = item.data(0, Qt.UserRole)
            if tree_item_data:
                tree_item_data.nodeName = name

        self._file_repository.save_tree_data(self._serialize_tree())

    def _delete(self, item):
        reply = QMessageBox.question(
            self, "Delete", f"Delete '{item.text(0)}'?",
            QMessageBox.Yes | QMessageBox.No)
        if reply == QMessageBox.Yes:
            parent = item.parent() or self.invisibleRootItem()
            parent.removeChild(item)
            tree_item_data: TreeItemData = item.data(0, Qt.UserRole)
            self._file_repository.delete_file(tree_item_data)

        self._file_repository.save_tree_data(self._serialize_tree())

    def _on_selection_changed(self, current, _previous):
        if current:
            tree_item_data: TreeItemData = current.data(0, Qt.UserRole)

            # save old data
            if _previous:
                self.node_deselected_signal.emit(_previous.data(0, Qt.UserRole))

            # set the content of editor
            self.node_selected_signal.emit(tree_item_data)

    def _item_to_dict(self, item: QTreeWidgetItem) -> dict:
        tree_item_data: TreeItemData = item.data(0, Qt.UserRole)
        return {
            "nodeName": tree_item_data.nodeName,
            "fileName": tree_item_data.fileName,
            "children": [
                self._item_to_dict(item.child(i))
                for i in range(item.childCount())
            ]
        }

    def _serialize_tree(self) -> str:
        root = self.topLevelItem(0)
        data = self._item_to_dict(root)
        return json.dumps(data, indent=2)
    
    @staticmethod
    def _pretty_print(json_object):
        json_formatted_str = json.dumps(json_object, indent=2)
        print(json_formatted_str)

    def _generate_header_from_name(self, name):
        text = self._capitilize_text(name)
        return f"<h1>{text}</h1><hr>"

    def _capitilize_text(self, text):
        if text[0].isupper():
            return concat(text[0] + text[1:].lower(), '\n')

        return text[0].upper() + text[1:].lower()

    def _set_topmost_tree_item(self):
        topmost_tree_item = self.topLevelItem(0)
        if topmost_tree_item:
            topmost_tree_item.setSelected(True)
            topmost_tree_item.setExpanded(True)


