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
    app.setPalette(QApplication.style().standardPalette())

    config = Config()
    file_repository = FileRepository(config)

    window = MainWindow(file_repository)
    window.show()
    sys.exit(app.exec())

if __name__ == "__main__":
    main()