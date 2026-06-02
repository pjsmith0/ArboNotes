#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Mon Jun  1 20:48:44 2026

@author: pjs
"""

import uuid

class NoteData:
    """Stores note metadata and content."""
    def __init__(self, name: str):
        self.name = name
        self.file_name = str(uuid.uuid4()) + ".html"
        self.content = ""
        self.title = name