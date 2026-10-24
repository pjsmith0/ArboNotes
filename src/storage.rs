use std::fs;
use std::io;
use std::path::Path;
use std::sync::OnceLock;

use chrono::{Datelike, NaiveDate};
use regex::Regex;
use uuid::Uuid;

use crate::config::Config;
use crate::model::{SearchTarget, TreeHierarchyData};
use crate::search::search_in_chunks_ignore_case;

fn date_file_pattern() -> &'static Regex {
    static RE: OnceLock<Regex> = OnceLock::new();
    RE.get_or_init(|| Regex::new(r"(?i)^(\d{8})\.html$").expect("valid date file regex"))
}

pub fn load_tree(config: &Config) -> Result<TreeHierarchyData, String> {
    let path = config.tree_path();
    let json = fs::read_to_string(&path)
        .map_err(|error| format!("Could not read {}: {error}", path.display()))?;
    serde_json::from_str(&json)
        .map_err(|error| format!("Could not parse {}: {error}", path.display()))
}

pub fn save_tree(config: &Config, tree: &TreeHierarchyData) -> Result<(), String> {
    let path = config.tree_path();
    let json = serde_json::to_string_pretty(tree)
        .map_err(|error| format!("Could not serialize the note tree: {error}"))?;
    fs::write(&path, json).map_err(|error| format!("Could not write {}: {error}", path.display()))
}

/// Loads a note, creating an empty file when it does not exist yet. This
/// matches `FileSystemManager.loadData`.
pub fn load_note(config: &Config, file_name: &str) -> Result<String, String> {
    let path = config.note_path(file_name)?;
    if !path.exists() {
        create_parent_and_file(&path)?;
    }
    fs::read_to_string(&path).map_err(|error| format!("Could not read {}: {error}", path.display()))
}

pub fn save_note(config: &Config, file_name: &str, html: &str) -> Result<(), String> {
    let path = config.note_path(file_name)?;
    if let Some(parent) = path.parent() {
        fs::create_dir_all(parent)
            .map_err(|error| format!("Could not create {}: {error}", parent.display()))?;
    }
    fs::write(&path, html).map_err(|error| format!("Could not write {}: {error}", path.display()))
}

pub fn note_exists(config: &Config, file_name: &str) -> Result<bool, String> {
    Ok(config.note_path(file_name)?.is_file())
}

/// Creates a new, empty note with a UUID-based file name and returns that name.
pub fn create_note(config: &Config) -> Result<String, String> {
    let file_name = format!("{}{}", Uuid::new_v4(), config.file_extension);
    let path = config.note_path(&file_name)?;
    create_parent_and_file(&path)?;
    Ok(file_name)
}

pub fn delete_note(config: &Config, file_name: &str) -> Result<(), String> {
    let path = config.note_path(file_name)?;
    match fs::remove_file(&path) {
        Ok(()) => Ok(()),
        Err(error) if error.kind() == io::ErrorKind::NotFound => Ok(()),
        Err(error) => Err(format!("Could not delete {}: {error}", path.display())),
    }
}

/// Returns the `yyyyMMdd` keys of all daily-note files in the requested month.
pub fn dates_with_notes(config: &Config, year: i32, month: u32) -> Result<Vec<String>, String> {
    let mut dates = Vec::new();
    collect_dates(&config.root_path, year, month, &mut dates)?;
    dates.sort();
    dates.dedup();
    Ok(dates)
}

fn collect_dates(
    directory: &Path,
    year: i32,
    month: u32,
    dates: &mut Vec<String>,
) -> Result<(), String> {
    let entries = match fs::read_dir(directory) {
        Ok(entries) => entries,
        Err(error) if error.kind() == io::ErrorKind::NotFound => return Ok(()),
        Err(error) => {
            return Err(format!(
                "Could not read directory {}: {error}",
                directory.display()
            ))
        }
    };

    for entry in entries {
        let entry = match entry {
            Ok(entry) => entry,
            Err(_) => continue,
        };
        let path = entry.path();

        if path.is_dir() {
            collect_dates(&path, year, month, dates)?;
            continue;
        }

        let Some(file_name) = path.file_name().and_then(|name| name.to_str()) else {
            continue;
        };
        let Some(captures) = date_file_pattern().captures(file_name) else {
            continue;
        };
        let Some(date_string) = captures.get(1).map(|value| value.as_str()) else {
            continue;
        };
        let Ok(date) = NaiveDate::parse_from_str(date_string, "%Y%m%d") else {
            continue;
        };

        if date.year() == year && date.month() == month {
            dates.push(date.format("%Y%m%d").to_string());
        }
    }

    Ok(())
}

/// Searches the given notes for `search_text` (case-insensitive). Missing or
/// unreadable files are skipped rather than aborting the whole search.
pub fn search_notes(
    config: &Config,
    targets: &[SearchTarget],
    search_text: &str,
) -> Vec<SearchTarget> {
    if search_text.is_empty() {
        return Vec::new();
    }

    targets
        .iter()
        .filter(|target| {
            let Ok(path) = config.note_path(&target.file_name) else {
                return false;
            };
            search_in_chunks_ignore_case(&path, search_text).unwrap_or(false)
        })
        .cloned()
        .collect()
}

fn create_parent_and_file(path: &Path) -> Result<(), String> {
    if let Some(parent) = path.parent() {
        fs::create_dir_all(parent)
            .map_err(|error| format!("Could not create {}: {error}", parent.display()))?;
    }
    fs::OpenOptions::new()
        .write(true)
        .create(true)
        .truncate(false)
        .open(path)
        .map(|_| ())
        .map_err(|error| format!("Could not create {}: {error}", path.display()))
}

#[cfg(test)]
mod tests {
    use super::*;

    fn test_config() -> (tempfile::TempDir, Config) {
        let dir = tempfile::tempdir().unwrap();
        let config = Config::new(dir.path().join("arbonote_data"));
        config.ensure_initialized().unwrap();
        (dir, config)
    }

    #[test]
    fn initializes_default_tree() {
        let (_dir, config) = test_config();
        let tree = load_tree(&config).unwrap();
        assert_eq!(tree.node_name, "Home");
        assert_eq!(tree.file_name, "Root.html");
        assert!(tree.children.is_empty());
        assert!(config.tree_path().exists());
    }

    #[test]
    fn round_trips_tree_json() {
        let (_dir, config) = test_config();
        let mut tree = load_tree(&config).unwrap();
        tree.children
            .push(TreeHierarchyData::new("Child", "child.html"));

        save_tree(&config, &tree).unwrap();
        let loaded = load_tree(&config).unwrap();
        assert_eq!(loaded, tree);
    }

    #[test]
    fn creates_loads_and_saves_notes() {
        let (_dir, config) = test_config();

        let file_name = create_note(&config).unwrap();
        assert!(file_name.ends_with(".html"));
        assert!(config.note_path(&file_name).unwrap().is_file());
        assert_eq!(load_note(&config, &file_name).unwrap(), "");

        save_note(&config, &file_name, "<p>Hello</p>").unwrap();
        assert_eq!(load_note(&config, &file_name).unwrap(), "<p>Hello</p>");

        delete_note(&config, &file_name).unwrap();
        assert!(!note_exists(&config, &file_name).unwrap());
        // Deleting a missing note is a no-op.
        delete_note(&config, &file_name).unwrap();
    }

    #[test]
    fn rejects_paths_outside_the_data_directory() {
        let (_dir, config) = test_config();
        assert!(config.note_path("../escape.html").is_err());
        assert!(config.note_path("/etc/passwd").is_err());
        assert!(config.note_path("").is_err());
        assert!(config.note_path("nested/note.html").is_ok());
    }

    #[test]
    fn finds_daily_notes_for_a_month() {
        let (_dir, config) = test_config();
        save_note(&config, "20260201.html", "one").unwrap();
        save_note(&config, "20260228.html", "two").unwrap();
        save_note(&config, "20260301.html", "three").unwrap();
        save_note(&config, "not-a-date.html", "four").unwrap();

        let dates = dates_with_notes(&config, 2026, 2).unwrap();
        assert_eq!(dates, vec!["20260201", "20260228"]);
    }

    #[test]
    fn searches_notes_case_insensitively() {
        let (_dir, config) = test_config();
        save_note(&config, "a.html", "The Quick Brown Fox").unwrap();
        save_note(&config, "b.html", "Nothing here").unwrap();

        let targets = vec![
            SearchTarget {
                node_name: "A".into(),
                file_name: "a.html".into(),
            },
            SearchTarget {
                node_name: "B".into(),
                file_name: "b.html".into(),
            },
            SearchTarget {
                node_name: "Missing".into(),
                file_name: "missing.html".into(),
            },
        ];

        let found = search_notes(&config, &targets, "quick brown");
        assert_eq!(found.len(), 1);
        assert_eq!(found[0].file_name, "a.html");
    }
}
