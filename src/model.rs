use serde::{Deserialize, Serialize};

/// On-disk representation of the note tree (`tree.json`).
///
/// Field names intentionally match the Java/Jackson JSON:
/// `{ "nodeName": ..., "fileName": ..., "children": [...] }`.
#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct TreeHierarchyData {
    pub node_name: String,
    pub file_name: String,
    #[serde(default)]
    pub children: Vec<TreeHierarchyData>,
}

impl TreeHierarchyData {
    pub fn new(node_name: impl Into<String>, file_name: impl Into<String>) -> Self {
        Self {
            node_name: node_name.into(),
            file_name: file_name.into(),
            children: Vec::new(),
        }
    }
}

/// A note that the frontend wants to search. The same shape is returned for
/// matches, which keeps the IPC contract small.
#[derive(Debug, Clone, PartialEq, Eq, Serialize, Deserialize)]
#[serde(rename_all = "camelCase")]
pub struct SearchTarget {
    pub node_name: String,
    pub file_name: String,
}
