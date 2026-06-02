#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Mon Jun  1 20:48:44 2026

@author: pjs
"""

from __future__ import annotations

from dataclasses import dataclass, field
from typing import List
import json


@dataclass
class Node:
    nodeName: str
    fileName: str
    children: List["Node"] = field(default_factory=list)

    def to_dict(self) -> dict:
        return {
            "nodeName": self.nodeName,
            "fileName": self.fileName,
            "children": [child.to_dict() for child in self.children],
        }

    @classmethod
    def from_dict(cls, data: dict) -> "Node":
        return cls(
            nodeName=data["nodeName"],
            fileName=data["fileName"],
            children=[
                cls.from_dict(child)
                for child in data.get("children", [])
            ],
        )