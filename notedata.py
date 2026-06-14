#!/usr/bin/env python3

from __future__ import annotations

from dataclasses import dataclass, field
from typing import List


@dataclass
class NoteData:
    nodeName: str
    fileName: str
    children: List["NoteData"] = field(default_factory=list)

    def to_dict(self) -> dict:
        return {
            "nodeName": self.nodeName,
            "fileName": self.fileName,
            "children": [child.to_dict() for child in self.children],
        }

    @classmethod
    def from_dict(cls, data: dict) -> "NoteData":
        return cls(
            nodeName=data["nodeName"],
            fileName=data["fileName"],
            children=[
                cls.from_dict(child)
                for child in data.get("children", [])
            ],
        )