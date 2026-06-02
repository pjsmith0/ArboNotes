#!/usr/bin/env python3
"""
TreeNotes - A hierarchical note-taking app with rich text editing.
Requires: PyQt6
Install:  pip install PyQt6
"""

import sys
from mainwindow import MainWindow

from PySide6.QtWidgets import (QApplication)

def main():
    #app = QApplication(sys.argv)
    app = QApplication.instance() or QApplication(sys.argv)
    app.setApplicationName("ArboNote")
    app.setStyle("Fusion")

    window = MainWindow()
    window.show()
    #sys.exit(app.exec())
    app.exec()


if __name__ == "__main__":
    main()