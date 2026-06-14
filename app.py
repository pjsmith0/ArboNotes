#!/usr/bin/env python3

import sys

from filerepository import FileRepository
from mainwindow import MainWindow
from config import Config
from PySide6.QtWidgets import (QApplication)

def main():
    app = QApplication.instance() or QApplication(sys.argv)
    app.setApplicationName("ArboNote")
    app.setStyle("Fusion")

    config = Config()
    filerepository = FileRepository(config)

    window = MainWindow(filerepository)
    window.show()
    sys.exit(app.exec())

if __name__ == "__main__":
    main()