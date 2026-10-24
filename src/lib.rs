mod commands;
mod config;
mod model;
mod search;
mod storage;
mod utils;

pub use config::Config;

use std::time::Duration;
use tauri::{Emitter, Manager};

/// Starts the ArboNotes desktop application.
pub fn run() {
    let config = match Config::from_environment() {
        Ok(config) => config,
        Err(error) => {
            eprintln!("ArboNotes: {error}");
            std::process::exit(1);
        }
    };

    if let Err(error) = config.ensure_initialized() {
        eprintln!("ArboNotes: {error}");
        std::process::exit(1);
    }

    tauri::Builder::default()
        .manage(config)
        .invoke_handler(tauri::generate_handler![
            commands::get_config,
            commands::load_tree,
            commands::save_tree,
            commands::load_note,
            commands::save_note,
            commands::note_exists,
            commands::create_note,
            commands::delete_note,
            commands::dates_with_notes,
            commands::search_notes,
            commands::sanitize_name,
            commands::heading_for_name,
            commands::exit_app,
        ])
        .on_window_event(|window, event| {
            // Give the frontend a chance to flush the editor before the
            // window disappears. The frontend saves and then calls
            // `exit_app`; the timeout is only a safety net.
            if let tauri::WindowEvent::CloseRequested { api, .. } = event {
                api.prevent_close();
                let _ = window.emit("request-close", ());
                let app = window.app_handle().clone();
                std::thread::spawn(move || {
                    std::thread::sleep(Duration::from_secs(5));
                    app.exit(0);
                });
            }
        })
        .run(tauri::generate_context!())
        .expect("error while running ArboNotes");
}
