use tauri::{AppHandle, State};

use crate::config::{Config, ConfigDto};
use crate::model::{SearchTarget, TreeHierarchyData};
use crate::storage;
use crate::utils;

#[tauri::command]
pub fn get_config(config: State<'_, Config>) -> ConfigDto {
    config.dto()
}

#[tauri::command]
pub fn load_tree(config: State<'_, Config>) -> Result<TreeHierarchyData, String> {
    storage::load_tree(&config)
}

#[tauri::command]
pub fn save_tree(config: State<'_, Config>, tree: TreeHierarchyData) -> Result<(), String> {
    storage::save_tree(&config, &tree)
}

#[tauri::command]
pub fn load_note(config: State<'_, Config>, file_name: String) -> Result<String, String> {
    storage::load_note(&config, &file_name)
}

#[tauri::command]
pub fn save_note(config: State<'_, Config>, file_name: String, html: String) -> Result<(), String> {
    storage::save_note(&config, &file_name, &html)
}

#[tauri::command]
pub fn note_exists(config: State<'_, Config>, file_name: String) -> Result<bool, String> {
    storage::note_exists(&config, &file_name)
}

#[tauri::command]
pub fn create_note(config: State<'_, Config>) -> Result<String, String> {
    storage::create_note(&config)
}

#[tauri::command]
pub fn delete_note(config: State<'_, Config>, file_name: String) -> Result<(), String> {
    storage::delete_note(&config, &file_name)
}

#[tauri::command]
pub fn dates_with_notes(
    config: State<'_, Config>,
    year: i32,
    month: u32,
) -> Result<Vec<String>, String> {
    storage::dates_with_notes(&config, year, month)
}

#[tauri::command]
pub fn search_notes(
    config: State<'_, Config>,
    targets: Vec<SearchTarget>,
    search_text: String,
) -> Vec<SearchTarget> {
    storage::search_notes(&config, &targets, &search_text)
}

#[tauri::command]
pub fn sanitize_name(raw: String) -> String {
    utils::sanitize(&raw)
}

#[tauri::command]
pub fn heading_for_name(raw: String) -> String {
    utils::wrap_in_h1(&raw)
}

#[tauri::command]
pub fn exit_app(app: AppHandle) {
    app.exit(0);
}
