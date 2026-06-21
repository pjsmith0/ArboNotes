import os
from idlelib.tree import TreeItem

from config import Config
import uuid
import json

from treeitemdata import TreeItemData


class FileRepository:
    def __init__(self, config: Config):
        self._config = config

    def get_tree_data(self):
        tree_file_path = self._config.get_data_dir() / "tree.json"
        with open(tree_file_path, "r", encoding="utf-8") as f:
            json_node = json.load(f)
            return json_node

    def save_tree_data(self, text):
        tree_file_path = self._config.get_data_dir() / "tree.json"
        with open(tree_file_path, "w") as file:
            file.write(text)

    def create_file(self, text: str):
        new_file_name = str(f"{uuid.uuid4()}.html")
        new_file_path = self._config.get_data_dir() / new_file_name

        if not new_file_path.exists():
            new_file_path.parent.mkdir(exist_ok=True, parents=True)

        with open(new_file_path, "w") as file:
            file.write(text)

        return new_file_name

    def load_file(self, file_name: str):
        file_content = None
        file_path = self._config.get_data_dir() / file_name
        with open(file_path, "r") as file:
            file_content = file.read()

        return file_content

    def save(self, tid: TreeItemData, data: str):
        tree_file_path = self._config.get_data_dir() / tid.fileName
        with open(tree_file_path, "w") as file:
            file.write(data)

    def delete_file(self, tid: TreeItemData):
        tree_file_path = self._config.get_data_dir() / tid.fileName
        if os.path.exists(tree_file_path):
            os.remove(tree_file_path)