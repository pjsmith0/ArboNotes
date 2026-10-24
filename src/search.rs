use std::fs::File;
use std::io::{self, BufRead, BufReader};
use std::path::Path;

const BUFFER_SIZE: usize = 8192;

/// Case-insensitive substring search over a file, reading it in chunks.
///
/// This is a port of `ChunkedFileSearch.searchInChunksIgnoreCase`: a tail of
/// the previous chunk is carried over so matches spanning chunk boundaries are
/// still found.
pub fn search_in_chunks_ignore_case(path: &Path, search_text: &str) -> io::Result<bool> {
    let needle = search_text.to_lowercase();
    if needle.is_empty() {
        return Ok(true);
    }

    let file = File::open(path)?;
    let mut reader = BufReader::with_capacity(BUFFER_SIZE, file);
    let mut line = String::new();
    let mut overlap = String::new();
    let keep = needle.chars().count().saturating_sub(1);

    loop {
        line.clear();
        let read = reader.read_line(&mut line)?;
        if read == 0 {
            break;
        }

        let chunk = format!("{overlap}{line}");
        let lowered = chunk.to_lowercase();
        if lowered.contains(&needle) {
            return Ok(true);
        }

        overlap = tail_chars(&lowered, keep);
    }

    Ok(false)
}

fn tail_chars(value: &str, count: usize) -> String {
    if count == 0 {
        return String::new();
    }
    let chars: Vec<char> = value.chars().collect();
    chars[chars.len().saturating_sub(count)..].iter().collect()
}

#[cfg(test)]
mod tests {
    use super::*;
    use std::io::Write;

    #[test]
    fn finds_text_case_insensitively() {
        let mut file = tempfile::NamedTempFile::new().unwrap();
        writeln!(file, "The quick brown fox").unwrap();
        assert!(search_in_chunks_ignore_case(file.path(), "QUICK BROWN").unwrap());
        assert!(search_in_chunks_ignore_case(file.path(), "brown fox").unwrap());
        assert!(!search_in_chunks_ignore_case(file.path(), "lazy dog").unwrap());
    }

    #[test]
    fn finds_matches_across_chunk_boundaries() {
        let mut file = tempfile::NamedTempFile::new().unwrap();
        let prefix = "x".repeat(BUFFER_SIZE - 5);
        writeln!(file, "{prefix}needle-across-boundary").unwrap();
        assert!(search_in_chunks_ignore_case(file.path(), "needle-across").unwrap());
    }

    #[test]
    fn empty_search_matches_everything() {
        let mut file = tempfile::NamedTempFile::new().unwrap();
        writeln!(file, "anything").unwrap();
        assert!(search_in_chunks_ignore_case(file.path(), "").unwrap());
    }
}
