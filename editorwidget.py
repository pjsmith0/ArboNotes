#!/usr/bin/env python3
from __future__ import annotations

from os.path import expanduser

from PySide6.QtCore import Signal, Qt
from PySide6.QtGui import (
    QAction, QTextCharFormat, QTextCursor, QFont, QIcon,
    QTextListFormat, QTextTableFormat, QTextLength, QColor,
    QTextBlockFormat,
)
from PySide6.QtWidgets import (
    QWidget,
    QTextEdit,
    QGridLayout,
    QToolBar,
    QComboBox,
    QPushButton,
    QDialog,
    QDialogButtonBox,
    QFormLayout,
    QSpinBox,
    QLabel,
    QLineEdit,
    QApplication,
    QInputDialog,
    QMessageBox,
)
from bs4 import BeautifulSoup

from filerepository import FileRepository


class InsertTableDialog(QDialog):
    """Simple dialog to ask for row/column count when inserting a table."""

    def __init__(self, parent=None):
        super().__init__(parent)

        self.setWindowTitle("Insert Table")

        layout = QFormLayout(self)

        self.rows_spin = QSpinBox()
        self.rows_spin.setRange(1, 50)
        self.rows_spin.setValue(3)
        layout.addRow("Rows:", self.rows_spin)

        self.cols_spin = QSpinBox()
        self.cols_spin.setRange(1, 20)
        self.cols_spin.setValue(3)
        layout.addRow("Columns:", self.cols_spin)

        buttons = QDialogButtonBox(
            QDialogButtonBox.StandardButton.Ok | QDialogButtonBox.StandardButton.Cancel
        )
        buttons.accepted.connect(self.accept)
        buttons.rejected.connect(self.reject)
        layout.addRow(buttons)

    @property
    def rows(self):
        return self.rows_spin.value()

    @property
    def cols(self):
        return self.cols_spin.value()


class InsertLinkDialog(QDialog):
    """Dialog for inserting a hyperlink."""

    def __init__(self, selected_text: str = "", parent=None):
        super().__init__(parent)
        self.setWindowTitle("Insert Link")
        self.setMinimumWidth(380)

        layout = QFormLayout(self)

        self.text_edit = QLineEdit(selected_text)
        layout.addRow("Display text:", self.text_edit)

        self.url_edit = QLineEdit("https://")
        layout.addRow("URL:", self.url_edit)

        buttons = QDialogButtonBox(
            QDialogButtonBox.StandardButton.Ok | QDialogButtonBox.StandardButton.Cancel
        )
        buttons.accepted.connect(self.accept)
        buttons.rejected.connect(self.reject)
        layout.addRow(buttons)

    @property
    def display_text(self):
        return self.text_edit.text()

    @property
    def url(self):
        return self.url_edit.text()


class UnformattedTextDialog(QDialog):
    """Dialog for entering/pasting unformatted / preformatted text."""

    def __init__(self, title: str = "Enter Text", parent=None):
        super().__init__(parent)
        self.setWindowTitle(title)
        self.setMinimumSize(500, 300)

        layout = QGridLayout(self)

        self._edit = QTextEdit()
        self._edit.setFontFamily("Courier New")
        layout.addWidget(QLabel("Paste or type plain text below:"), 0, 0)
        layout.addWidget(self._edit, 1, 0)

        buttons = QDialogButtonBox(
            QDialogButtonBox.StandardButton.Ok | QDialogButtonBox.StandardButton.Cancel
        )
        buttons.accepted.connect(self.accept)
        buttons.rejected.connect(self.reject)
        layout.addWidget(buttons, 2, 0)

    @property
    def plain_text(self):
        return self._edit.toPlainText()


# ---------------------------------------------------------------------------


class EditorWidget(QWidget):

    statusMessage = Signal(str)

    def __init__(self, file_repository: FileRepository, parent=None):
        super().__init__(parent)

        self._file_name = ""
        self._current_note = None

        self._file_repository = file_repository

        #
        # Two toolbar rows
        #
        self.toolbar1 = QToolBar()
        self.toolbar1.setStyleSheet("background:#f0f0f0;")

        self.toolbar2 = QToolBar()
        self.toolbar2.setStyleSheet("background:#f0f0f0;")

        self._text_edit = QTextEdit(self)
        #self._text_edit.setStyleSheet("background:#e8e8e8;")
        self._text_edit.setStyleSheet("""
            QTextEdit {
                background: #ffffff;
                color: #1a1a1a;
                font-family: Arial;
                font-size: 10pt;
                border: none;
                padding: 4px;
                selection-background-color: #b3d4f5;
                selection-color: #000000;
            }
        """)

        layout = QGridLayout(self)
        layout.setContentsMargins(0, 0, 0, 0)
        layout.setSpacing(0)

        layout.addWidget(self.toolbar1, 0, 0)
        layout.addWidget(self.toolbar2, 1, 0)
        layout.addWidget(self._text_edit, 2, 0)

        self._build_toolbar()

    def _build_toolbar(self):

        # ------------------------------------------------------------------
        # First toolbar row
        # ------------------------------------------------------------------

        self._font_combobox = QComboBox()
        self._font_combobox.addItems([
            "SansSerif",
            "Serif",
            "Monospaced",
            "Dialog",
            "DialogInput",
            "Arial",
            "Courier New",
            "Times New Roman",
        ])
        self._font_combobox.currentTextChanged.connect(self.font_combobox_changed)
        self.toolbar1.addWidget(self._font_combobox)

        self._style_combobox = QComboBox()
        self._style_combobox.addItems([
            "Paragraph",
            "Heading 1",
            "Heading 2",
            "Heading 3",
            "Preformatted",
        ])
        self._style_combobox.currentTextChanged.connect(self.style_combobox_changed)
        self.toolbar1.addWidget(self._style_combobox)

        self._font_size_combobox = QComboBox()
        self._font_size_combobox.addItems([
            "8", "9", "10", "11", "12", "14", "16",
            "18", "20", "24", "28", "32", "36",
        ])
        self._font_size_combobox.setCurrentText("12")
        self._font_size_combobox.currentTextChanged.connect(self.font_size_combobox_changed)
        self.toolbar1.addWidget(self._font_size_combobox)

        self.toolbar1.addSeparator()

        insert_table_button = QPushButton()
        insert_table_button.setIcon(QIcon("icons/table_16dp.png"))
        insert_table_button.setToolTip("Insert Table")
        insert_table_button.clicked.connect(self.insert_table)
        self.toolbar1.addWidget(insert_table_button)

        edit_table_button = QPushButton()
        edit_table_button.setIcon(QIcon("icons/table_edit_16dp.png"))
        edit_table_button.setToolTip("Edit Table (add/remove rows & columns)")
        edit_table_button.clicked.connect(self.edit_table)
        self.toolbar1.addWidget(edit_table_button)

        remove_table_button = QPushButton()
        remove_table_button.setIcon(QIcon("icons/table_remove_16dp.png"))
        remove_table_button.setToolTip("Remove Table")
        remove_table_button.clicked.connect(self.remove_table)
        self.toolbar1.addWidget(remove_table_button)

        self.toolbar1.addSeparator()

        undo_button = QPushButton()
        undo_button.setIcon(QIcon("icons/undo_16dp.png"))
        undo_button.setToolTip("Undo")
        undo_button.clicked.connect(self.undo)
        self.toolbar1.addWidget(undo_button)

        redo_button = QPushButton()
        redo_button.setIcon(QIcon("icons/redo_16dp.png"))
        redo_button.setToolTip("Redo")
        redo_button.clicked.connect(self.redo)
        self.toolbar1.addWidget(redo_button)

        self.toolbar1.addSeparator()

        toggle_html_button = QPushButton()
        toggle_html_button.setIcon(QIcon("icons/html_16dp.png"))
        toggle_html_button.setToolTip("Toggle HTML")
        toggle_html_button.clicked.connect(self.toggle_html)
        self.toolbar1.addWidget(toggle_html_button)

        # ------------------------------------------------------------------
        # Second toolbar row
        # ------------------------------------------------------------------

        bold_button = QPushButton()
        bold_button.setIcon(QIcon("icons/format_bold_16dp.png"))
        bold_button.setToolTip("Bold (Ctrl+B)")
        bold_button.clicked.connect(self.text_bold)
        self.toolbar2.addWidget(bold_button)

        italic_button = QPushButton()
        italic_button.setIcon(QIcon("icons/format_italic_16dp.png"))
        italic_button.setToolTip("Italic (Ctrl+I)")
        italic_button.clicked.connect(self.text_italic)
        self.toolbar2.addWidget(italic_button)

        underline_button = QPushButton()
        underline_button.setIcon(QIcon("icons/format_underlined_16dp.png"))
        underline_button.setToolTip("Underline (Ctrl+U)")
        underline_button.clicked.connect(self.text_underline)
        self.toolbar2.addWidget(underline_button)

        self.toolbar2.addSeparator()

        indent_decrease_button = QPushButton()
        indent_decrease_button.setIcon(QIcon("icons/format_indent_decrease_16dp.png"))
        indent_decrease_button.setToolTip("Decrease Indent")
        indent_decrease_button.clicked.connect(self.indent_decrease)
        self.toolbar2.addWidget(indent_decrease_button)

        indent_increase_button = QPushButton()
        indent_increase_button.setIcon(QIcon("icons/format_indent_increase_16dp.png"))
        indent_increase_button.setToolTip("Increase Indent")
        indent_increase_button.clicked.connect(self.indent_increase)
        self.toolbar2.addWidget(indent_increase_button)

        self.toolbar2.addSeparator()

        numbered_list_button = QPushButton()
        numbered_list_button.setIcon(QIcon("icons/format_list_numbered_16dp.png"))
        numbered_list_button.setToolTip("Numbered List")
        numbered_list_button.clicked.connect(self.numbered_list)
        self.toolbar2.addWidget(numbered_list_button)

        bullet_list_button = QPushButton()
        bullet_list_button.setIcon(QIcon("icons/list_16dp.png"))
        bullet_list_button.setToolTip("Bullet List")
        bullet_list_button.clicked.connect(self.bullet_list)
        self.toolbar2.addWidget(bullet_list_button)

        self.toolbar2.addSeparator()

        insert_link_button = QPushButton()
        insert_link_button.setIcon(QIcon("icons/link_16dp.png"))
        insert_link_button.setToolTip("Insert Link")
        insert_link_button.clicked.connect(self.insert_link)
        self.toolbar2.addWidget(insert_link_button)

        remove_link_button = QPushButton()
        remove_link_button.setIcon(QIcon("icons/link_off_16dp.png"))
        remove_link_button.setToolTip("Remove Link")
        remove_link_button.clicked.connect(self.remove_link)
        self.toolbar2.addWidget(remove_link_button)

        self.toolbar2.addSeparator()

        insert_horizontal_line_button = QPushButton()
        insert_horizontal_line_button.setIcon(QIcon("icons/horizontal_rule_16dp.png"))
        insert_horizontal_line_button.setToolTip("Insert Horizontal Rule")
        insert_horizontal_line_button.clicked.connect(self.insert_horizontal_line)
        self.toolbar2.addWidget(insert_horizontal_line_button)

        self.toolbar2.addSeparator()

        align_left_button = QPushButton()
        align_left_button.setIcon(QIcon("icons/format_align_left_16dp.png"))
        align_left_button.setToolTip("Align Left")
        align_left_button.clicked.connect(self.align_left)
        self.toolbar2.addWidget(align_left_button)

        align_center_button = QPushButton()
        align_center_button.setIcon(QIcon("icons/format_align_center_16dp.png"))
        align_center_button.setToolTip("Align Center")
        align_center_button.clicked.connect(self.align_center)
        self.toolbar2.addWidget(align_center_button)

        align_right_button = QPushButton()
        align_right_button.setIcon(QIcon("icons/format_align_right_16dp.png"))
        align_right_button.setToolTip("Align Right")
        align_right_button.clicked.connect(self.align_right)
        self.toolbar2.addWidget(align_right_button)

        align_justified_button = QPushButton()
        # reuse the same icon name you had; swap for a justified icon if you add one
        align_justified_button.setIcon(QIcon("icons/format_align_justify_16.png"))
        align_justified_button.setToolTip("Justify")
        align_justified_button.clicked.connect(self.align_justified)
        self.toolbar2.addWidget(align_justified_button)

        self.toolbar2.addSeparator()

        paste_preformatted_text_button = QPushButton()
        paste_preformatted_text_button.setIcon(QIcon("icons/chat_paste_go_16dp.png"))
        paste_preformatted_text_button.setToolTip("Paste as Preformatted Text")
        paste_preformatted_text_button.clicked.connect(self.paste_preformatted_text)
        self.toolbar2.addWidget(paste_preformatted_text_button)

        enter_unformatted_text_button = QPushButton()
        enter_unformatted_text_button.setIcon(QIcon("icons/code_16dp.png"))
        enter_unformatted_text_button.setToolTip("Enter Unformatted Text")
        enter_unformatted_text_button.clicked.connect(self.enter_unformatted_text)
        self.toolbar2.addWidget(enter_unformatted_text_button)

        self.toolbar1.setMovable(False)
        self.toolbar2.setMovable(False)

    # ------------------------------------------------------------------
    # Internal helpers
    # ------------------------------------------------------------------

    def _merge_format(self, fmt: QTextCharFormat):
        cursor = self._text_edit.textCursor()
        if not cursor.hasSelection():
            cursor.select(QTextCursor.SelectionType.WordUnderCursor)
        cursor.mergeCharFormat(fmt)
        self._text_edit.mergeCurrentCharFormat(fmt)

    def _set_block_alignment(self, alignment: Qt.AlignmentFlag):
        cursor = self._text_edit.textCursor()
        fmt = QTextBlockFormat()
        fmt.setAlignment(alignment)
        cursor.mergeBlockFormat(fmt)
        self._text_edit.setTextCursor(cursor)

    def _cursor_in_table(self) -> bool:
        return self._text_edit.textCursor().currentTable() is not None

    # ------------------------------------------------------------------
    # Formatting: bold / italic / underline
    # ------------------------------------------------------------------

    def text_bold(self):
        fmt = QTextCharFormat()
        current = self._text_edit.fontWeight()
        fmt.setFontWeight(
            QFont.Weight.Normal if current == QFont.Weight.Bold else QFont.Weight.Bold
        )
        self._merge_format(fmt)

    def text_italic(self):
        fmt = QTextCharFormat()
        fmt.setFontItalic(not self._text_edit.fontItalic())
        self._merge_format(fmt)

    def text_underline(self):
        fmt = QTextCharFormat()
        fmt.setFontUnderline(not self._text_edit.fontUnderline())
        self._merge_format(fmt)

    # ------------------------------------------------------------------
    # Indent
    # ------------------------------------------------------------------

    def indent_decrease(self):
        cursor = self._text_edit.textCursor()
        lst = cursor.currentList()
        if lst:
            fmt = lst.format()
            level = fmt.indent()
            if level > 1:
                fmt.setIndent(level - 1)
                lst.setFormat(fmt)
            else:
                # Remove from list entirely, restore as plain paragraph
                fmt_block = cursor.blockFormat()
                fmt_block.setIndent(0)
                cursor.setBlockFormat(fmt_block)
                cursor.setBlockCharFormat(QTextCharFormat())
        else:
            fmt = cursor.blockFormat()
            indent = max(0, fmt.indent() - 1)
            fmt.setIndent(indent)
            cursor.setBlockFormat(fmt)
        self._text_edit.setTextCursor(cursor)

    def indent_increase(self):
        cursor = self._text_edit.textCursor()
        lst = cursor.currentList()
        if lst:
            fmt = lst.format()
            fmt.setIndent(fmt.indent() + 1)
            lst.setFormat(fmt)
        else:
            fmt = cursor.blockFormat()
            fmt.setIndent(fmt.indent() + 1)
            cursor.setBlockFormat(fmt)
        self._text_edit.setTextCursor(cursor)

    # ------------------------------------------------------------------
    # Lists
    # ------------------------------------------------------------------

    def _toggle_list(self, style: QTextListFormat.Style):
        cursor = self._text_edit.textCursor()
        lst = cursor.currentList()

        if lst and lst.format().style() == style:
            # Already this list type → remove list formatting
            fmt = cursor.blockFormat()
            fmt.setIndent(0)
            cursor.setBlockFormat(fmt)
            cursor.setBlockCharFormat(QTextCharFormat())
        else:
            fmt = QTextListFormat()
            fmt.setStyle(style)
            fmt.setIndent(1)
            cursor.createList(fmt)

        self._text_edit.setTextCursor(cursor)

    def numbered_list(self):
        self._toggle_list(QTextListFormat.Style.ListDecimal)

    def bullet_list(self):
        self._toggle_list(QTextListFormat.Style.ListDisc)

    # ------------------------------------------------------------------
    # Links
    # ------------------------------------------------------------------

    def insert_link(self):
        cursor = self._text_edit.textCursor()
        selected = cursor.selectedText()

        dlg = InsertLinkDialog(selected_text=selected, parent=self)
        if dlg.exec() != QDialog.DialogCode.Accepted:
            return

        url = dlg.url.strip()
        text = dlg.display_text.strip() or url
        if not url:
            return

        fmt = QTextCharFormat()
        fmt.setAnchor(True)
        fmt.setAnchorHref(url)
        fmt.setForeground(QColor("blue"))
        fmt.setFontUnderline(True)

        if cursor.hasSelection():
            cursor.mergeCharFormat(fmt)
        else:
            cursor.insertText(text, fmt)

        # Reset char format so subsequent typing is plain
        reset = QTextCharFormat()
        self._text_edit.setCurrentCharFormat(reset)
        self._text_edit.setTextCursor(cursor)

    def remove_link(self):
        cursor = self._text_edit.textCursor()
        if not cursor.hasSelection():
            cursor.select(QTextCursor.SelectionType.WordUnderCursor)

        fmt = QTextCharFormat()
        fmt.setAnchor(False)
        fmt.setAnchorHref("")
        fmt.setForeground(self._text_edit.palette().text().color())
        fmt.setFontUnderline(False)

        cursor.mergeCharFormat(fmt)
        self._text_edit.mergeCurrentCharFormat(fmt)

    # ------------------------------------------------------------------
    # Horizontal rule
    # ------------------------------------------------------------------

    def insert_horizontal_line(self):
        """Insert an HTML <hr> represented as a full-width bottom-bordered paragraph."""
        cursor = self._text_edit.textCursor()

        # Move to end of current block
        cursor.movePosition(QTextCursor.MoveOperation.EndOfBlock)

        # Insert a new paragraph that acts as an HR via a bottom border in HTML
        cursor.insertHtml(
            "<hr style='border:none; border-top:1px solid #888; margin:6px 0;'/>"
        )
        self._text_edit.setTextCursor(cursor)

    # ------------------------------------------------------------------
    # Alignment
    # ------------------------------------------------------------------

    def align_left(self):
        self._set_block_alignment(Qt.AlignmentFlag.AlignLeft)

    def align_center(self):
        self._set_block_alignment(Qt.AlignmentFlag.AlignHCenter)

    def align_right(self):
        self._set_block_alignment(Qt.AlignmentFlag.AlignRight)

    def align_justified(self):
        self._set_block_alignment(Qt.AlignmentFlag.AlignJustify)

    # ------------------------------------------------------------------
    # Paste / enter unformatted text
    # ------------------------------------------------------------------

    def paste_preformatted_text(self):
        """Paste clipboard contents as preformatted (monospaced, no HTML) text."""
        clipboard = QApplication.clipboard()
        text = clipboard.text()
        if not text:
            return

        import html
        cursor = self._text_edit.textCursor()
        cursor.insertHtml(f"<pre>{html.escape(text)}</pre>")
        self._text_edit.setTextCursor(cursor)

    def enter_unformatted_text(self):
        """Open a dialog where the user can type/paste plain text to insert."""
        dlg = UnformattedTextDialog(title="Enter Unformatted Text", parent=self)
        if dlg.exec() != QDialog.DialogCode.Accepted:
            return

        text = dlg.plain_text
        if not text:
            return

        import html
        cursor = self._text_edit.textCursor()
        cursor.insertHtml(f"<pre>{html.escape(text)}</pre>")
        self._text_edit.setTextCursor(cursor)

    # ------------------------------------------------------------------
    # Font / style / size comboboxes
    # ------------------------------------------------------------------

    # Logical → Qt family mapping
    _FONT_MAP = {
        "SansSerif":   "Arial",
        "Serif":       "Times New Roman",
        "Monospaced":  "Courier New",
        "Dialog":      "Arial",
        "DialogInput": "Courier New",
    }

    def font_combobox_changed(self, text: str):
        family = self._FONT_MAP.get(text, text)
        fmt = QTextCharFormat()
        fmt.setFontFamily(family)
        self._merge_format(fmt)

    def style_combobox_changed(self, text: str):
        cursor = self._text_edit.textCursor()

        block_fmt = QTextBlockFormat()
        char_fmt = QTextCharFormat()

        if text == "Heading 1":
            char_fmt.setFontPointSize(24)
            char_fmt.setFontWeight(QFont.Weight.Bold)
            block_fmt.setHeadingLevel(1)

        elif text == "Heading 2":
            char_fmt.setFontPointSize(18)
            char_fmt.setFontWeight(QFont.Weight.Bold)
            block_fmt.setHeadingLevel(2)

        elif text == "Heading 3":
            char_fmt.setFontPointSize(14)
            char_fmt.setFontWeight(QFont.Weight.Bold)
            block_fmt.setHeadingLevel(3)

        elif text == "Preformatted":
            char_fmt.setFontFamily("Courier New")
            char_fmt.setFontFixedPitch(True)
            char_fmt.setFontPointSize(10)
            block_fmt.setHeadingLevel(0)

        else:  # "Paragraph"
            char_fmt.setFontPointSize(
                float(self._font_size_combobox.currentText())
            )
            char_fmt.setFontWeight(QFont.Weight.Normal)
            block_fmt.setHeadingLevel(0)

        cursor.mergeBlockFormat(block_fmt)
        cursor.mergeBlockCharFormat(char_fmt)
        self._text_edit.mergeCurrentCharFormat(char_fmt)
        self._text_edit.setTextCursor(cursor)

    def font_size_combobox_changed(self, font_size: str):
        try:
            size = float(font_size)
        except ValueError:
            return
        fmt = QTextCharFormat()
        fmt.setFontPointSize(size)
        self._merge_format(fmt)

    # ------------------------------------------------------------------
    # Table operations
    # ------------------------------------------------------------------

    def insert_table(self):
        dlg = InsertTableDialog(parent=self)
        if dlg.exec() != QDialog.DialogCode.Accepted:
            return

        rows, cols = dlg.rows, dlg.cols
        cursor = self._text_edit.textCursor()

        fmt = QTextTableFormat()
        fmt.setCellPadding(4)
        fmt.setCellSpacing(0)
        fmt.setBorderStyle(QTextTableFormat.BorderStyle.BorderStyle_Solid)
        fmt.setBorder(1)

        # Equal-width columns
        constraints = [
                          QTextLength(QTextLength.Type.PercentageLength, 100.0 / cols)
                      ] * cols
        fmt.setColumnWidthConstraints(constraints)

        cursor.insertTable(rows, cols, fmt)
        self._text_edit.setTextCursor(cursor)

    def edit_table(self):
        """Provide row/column add-remove actions for the table at cursor."""
        table = self._text_edit.textCursor().currentTable()
        if not table:
            QMessageBox.information(
                self, "No Table", "Place the cursor inside a table first."
            )
            return

        actions = [
            "Add row above",
            "Add row below",
            "Remove current row",
            "Add column before",
            "Add column after",
            "Remove current column",
        ]
        action, ok = QInputDialog.getItem(
            self, "Edit Table", "Choose action:", actions, 0, False
        )
        if not ok:
            return

        cursor = self._text_edit.textCursor()
        cell = table.cellAt(cursor)
        row = cell.row()
        col = cell.column()

        if action == "Add row above":
            table.insertRows(row, 1)
        elif action == "Add row below":
            table.insertRows(row + 1, 1)
        elif action == "Remove current row":
            if table.rows() > 1:
                table.removeRows(row, 1)
            else:
                QMessageBox.warning(self, "Cannot Remove", "Table must have at least one row.")
        elif action == "Add column before":
            table.insertColumns(col, 1)
        elif action == "Add column after":
            table.insertColumns(col + 1, 1)
        elif action == "Remove current column":
            if table.columns() > 1:
                table.removeColumns(col, 1)
            else:
                QMessageBox.warning(self, "Cannot Remove", "Table must have at least one column.")

    def remove_table(self):
        """Delete the entire table the cursor is inside."""
        cursor = self._text_edit.textCursor()
        table = cursor.currentTable()
        if not table:
            QMessageBox.information(
                self, "No Table", "Place the cursor inside a table first."
            )
            return

        # Select the whole table and delete it
        table_cursor = table.firstCursorPosition()
        table_cursor.setPosition(
            table.lastCursorPosition().position(),
            QTextCursor.MoveMode.KeepAnchor,
        )
        table_cursor.removeSelectedText()
        self._text_edit.setTextCursor(table_cursor)

    # ------------------------------------------------------------------
    # Undo / Redo
    # ------------------------------------------------------------------

    def undo(self):
        self._text_edit.undo()

    def redo(self):
        self._text_edit.redo()

    def toggle_html(self):
        if self._text_edit.acceptRichText():
            # Switch to HTML source view
            html = self._clean_text(self._text_edit.toHtml())
            self._text_edit.setAcceptRichText(False)
            self._text_edit.setPlainText(html)
        else:
            # Switch back to rich text view
            html = self._text_edit.toPlainText()
            self._text_edit.setAcceptRichText(True)
            self._text_edit.setHtml(html)

    def load_note(self, tree_item_data):
        self._current_note = tree_item_data
        text = self._file_repository.load_file(tree_item_data.fileName)
        self._text_edit.setHtml(text)

    def save_current(self, callback=None):
        self.statusMessage.emit("Note saved")

    def get_html(self) -> str:
        return self._clean_text(self._text_edit.toHtml())

    def editor(self) -> QTextEdit:
        return self._text_edit

    def _clean_text(self, text):
        soup = BeautifulSoup(text, 'html.parser')

        # Remove all script tags
        for script in soup.find_all(['head', 'script']):
            script.decompose()

        for tag in soup.find_all():
            tag.attrs.pop('style', None)

        return soup.prettify()