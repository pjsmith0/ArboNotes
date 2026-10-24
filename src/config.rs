use std::env;
use std::fs;
use std::path::{Component, Path, PathBuf};

use serde::Serialize;

use crate::model::TreeHierarchyData;

pub const DEFAULT_ROOT_DIR: &str = "arbonote_data";
pub const DEFAULT_TREE_FILE: &str = "tree.json";
pub const DEFAULT_FILE_EXTENSION: &str = ".html";
pub const ROOT_PATH_ENV: &str = "ARBONOTES_ROOT";

/// Application configuration.
///
/// Mirrors the original Java `Config`, except that the data directory is
/// created on first launch instead of making the application exit when it is
/// missing.
#[derive(Debug, Clone)]
pub struct Config {
    pub root_path: PathBuf,
    pub tree_file_name: String,
    pub file_extension: String,
}

#[derive(Debug, Clone, Serialize)]
#[serde(rename_all = "camelCase")]
pub struct ConfigDto {
    pub root_path: String,
    pub tree_file_name: String,
    pub file_extension: String,
}

impl Config {
    /// Uses `ARBONOTES_ROOT` when set (handy for tests and portable installs),
    /// otherwise `~/arbonote_data`, matching the Java application.
    pub fn from_environment() -> Result<Self, String> {
        let root_path = match env::var_os(ROOT_PATH_ENV) {
            Some(value) if !value.is_empty() => PathBuf::from(value),
            _ => dirs::home_dir()
                .ok_or_else(|| "Could not determine the user's home directory".to_string())?
                .join(DEFAULT_ROOT_DIR),
        };

        Ok(Self {
            root_path,
            tree_file_name: DEFAULT_TREE_FILE.to_string(),
            file_extension: DEFAULT_FILE_EXTENSION.to_string(),
        })
    }

    /// For tests and embedders that want an explicit data directory.
    pub fn new(root_path: impl Into<PathBuf>) -> Self {
        Self {
            root_path: root_path.into(),
            tree_file_name: DEFAULT_TREE_FILE.to_string(),
            file_extension: DEFAULT_FILE_EXTENSION.to_string(),
        }
    }

    pub fn dto(&self) -> ConfigDto {
        ConfigDto {
            root_path: self.root_path.display().to_string(),
            tree_file_name: self.tree_file_name.clone(),
            file_extension: self.file_extension.clone(),
        }
    }

    pub fn tree_path(&self) -> PathBuf {
        self.root_path.join(&self.tree_file_name)
    }

    /// Creates the data directory and the default `tree.json` when missing.
    pub fn ensure_initialized(&self) -> Result<(), String> {
        fs::create_dir_all(&self.root_path).map_err(|error| {
            format!(
                "Could not create data directory {}: {error}",
                self.root_path.display()
            )
        })?;

        let tree_path = self.tree_path();
        if !tree_path.exists() {
            let root = TreeHierarchyData::new("Home", "Root.html");
            let json = serde_json::to_string_pretty(&root)
                .map_err(|error| format!("Could not serialize the default tree: {error}"))?;
            fs::write(&tree_path, json)
                .map_err(|error| format!("Could not write {}: {error}", tree_path.display()))?;
        }

        Ok(())
    }

    /// Resolves a note file name inside the data directory.
    ///
    /// Absolute paths and `..` components are rejected so that a hand-edited
    /// `tree.json` cannot make the application read or write outside its data
    /// directory.
    pub fn note_path(&self, file_name: &str) -> Result<PathBuf, String> {
        if file_name.trim().is_empty() {
            return Err("The note file name is empty".to_string());
        }

        let relative = Path::new(file_name);
        if relative.is_absolute()
            || relative.components().any(|component| {
                matches!(
                    component,
                    Component::ParentDir | Component::RootDir | Component::Prefix(_)
                )
            })
        {
            return Err(format!("Invalid note file name: {file_name}"));
        }

        Ok(self.root_path.join(relative))
    }
}
