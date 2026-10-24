use std::collections::HashSet;
use std::sync::OnceLock;

use regex::Regex;
use unicode_normalization::UnicodeNormalization;

const WINDOWS_RESERVED: &[&str] = &[
    "CON", "PRN", "AUX", "NUL", "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8",
    "COM9", "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9",
];

fn marks() -> &'static Regex {
    static RE: OnceLock<Regex> = OnceLock::new();
    RE.get_or_init(|| Regex::new(r"\p{M}").expect("valid mark regex"))
}

fn invalid_characters() -> &'static Regex {
    static RE: OnceLock<Regex> = OnceLock::new();
    RE.get_or_init(|| Regex::new(r#"[\\/:*?"<>|]"#).expect("valid invalid-character regex"))
}

fn control_characters() -> &'static Regex {
    static RE: OnceLock<Regex> = OnceLock::new();
    RE.get_or_init(|| Regex::new(r"\p{Cc}").expect("valid control-character regex"))
}

fn trailing_dots_or_spaces() -> &'static Regex {
    static RE: OnceLock<Regex> = OnceLock::new();
    RE.get_or_init(|| Regex::new(r"[. ]+$").expect("valid trailing-character regex"))
}

fn unsafe_characters() -> &'static Regex {
    static RE: OnceLock<Regex> = OnceLock::new();
    RE.get_or_init(|| Regex::new(r"[^a-zA-Z0-9._-]").expect("valid safe-character regex"))
}

/// Port of `com.pjs.util.Utils.sanitize`.
///
/// Produces a filesystem-friendly name: accents are removed, unsafe characters
/// are replaced with underscores, trailing dots/spaces are dropped, and Windows
/// reserved device names are prefixed with an underscore.
pub fn sanitize(input: &str) -> String {
    if input.trim().is_empty() {
        return "dir".to_string();
    }

    let decomposed: String = input.nfkd().collect();
    let without_marks = marks().replace_all(&decomposed, "").into_owned();

    let name = invalid_characters()
        .replace_all(&without_marks, "_")
        .into_owned();
    let name = control_characters().replace_all(&name, "_").into_owned();
    let name = trailing_dots_or_spaces()
        .replace_all(&name, "")
        .into_owned();
    let name = unsafe_characters().replace_all(&name, "_").into_owned();

    let mut name = if name.is_empty() {
        "dir".to_string()
    } else {
        name
    };

    let reserved: HashSet<&str> = WINDOWS_RESERVED.iter().copied().collect();
    if reserved.contains(name.to_uppercase().as_str()) {
        name = format!("_{name}");
    }

    name
}

pub fn capitalize_first_char(value: &str) -> String {
    let mut chars = value.chars();
    match chars.next() {
        None => String::new(),
        Some(first) => {
            let mut result: String = first.to_uppercase().collect();
            result.push_str(chars.as_str());
            result
        }
    }
}

pub fn wrap_in_h1(value: &str) -> String {
    format!("<h1>{}</h1>", capitalize_first_char(value))
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn sanitizes_names_like_the_java_application() {
        assert_eq!(sanitize("Hello World!"), "Hello_World_");
        assert_eq!(sanitize("a/b\\c:d*e?f\"g<h>i|j"), "a_b_c_d_e_f_g_h_i_j");
        assert_eq!(sanitize("Café notes"), "Cafe_notes");
        assert_eq!(sanitize("   "), "dir");
        assert_eq!(sanitize(""), "dir");
        assert_eq!(sanitize("name...  "), "name");
        assert_eq!(sanitize("CON"), "_CON");
        assert_eq!(sanitize("con"), "_con");
        assert_eq!(sanitize("already_safe-1.2"), "already_safe-1.2");
    }

    #[test]
    fn wraps_names_in_a_heading() {
        assert_eq!(wrap_in_h1("my note"), "<h1>My note</h1>");
        assert_eq!(wrap_in_h1("Already"), "<h1>Already</h1>");
        assert_eq!(wrap_in_h1(""), "<h1></h1>");
    }

    #[test]
    fn capitalizes_only_the_first_character() {
        assert_eq!(capitalize_first_char("hello"), "Hello");
        assert_eq!(capitalize_first_char("Hello"), "Hello");
        assert_eq!(capitalize_first_char(""), "");
        assert_eq!(capitalize_first_char("éclair"), "Éclair");
    }
}
