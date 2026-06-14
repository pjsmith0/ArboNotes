from pathlib import Path

class Config:

    def __init__(self):
        self._data_dir = Path.home() / "arbonote_data"

    def get_data_dir(self):
        return self._data_dir