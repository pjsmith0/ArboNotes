from notedata import NoteData
from treeitemdata import TreeItemData


def map_notedata_to_treeitemdata(nd: NoteData):
    return TreeItemData(nodeName=nd.nodeName, fileName=nd.fileName)