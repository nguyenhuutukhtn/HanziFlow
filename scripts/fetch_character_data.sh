#!/usr/bin/env bash
set -euo pipefail

# Pinned snapshots: source URLs, commit/UCDB versions, and SHA-256 values are
# intentionally kept here so a fresh machine gets the same character inputs.
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"
TMP="$(mktemp -d "${TMPDIR:-/tmp}/hanziflow-character-data.XXXXXX")"
trap 'rm -rf "$TMP"' EXIT

fetch() {
  local url="$1" target="$2" expected="$3" temp
  if [[ -f "$target" ]] && [[ "$(shasum -a 256 "$target" | awk '{print $1}')" == "$expected" ]]; then
    return
  fi
  temp="$TMP/$(basename "$target")"
  curl -fL --retry 2 --connect-timeout 20 -o "$temp" "$url"
  [[ "$(shasum -a 256 "$temp" | awk '{print $1}')" == "$expected" ]] || { echo "sha256 mismatch: $url" >&2; exit 1; }
  mkdir -p "$(dirname "$target")"
  mv "$temp" "$target"
}

fetch_archive() {
  local url="$1" archive_hash="$2" marker="$3" extract_dir="$4" temp
  if [[ -f "$marker" ]] && [[ "$(cat "$marker")" == "$archive_hash" ]] && [[ -f "$extract_dir/all.json" ]]; then
    return
  fi
  temp="$TMP/source.tar.gz"
  curl -fL --retry 2 --connect-timeout 20 -o "$temp" "$url"
  [[ "$(shasum -a 256 "$temp" | awk '{print $1}')" == "$archive_hash" ]] || { echo "sha256 mismatch: $url" >&2; exit 1; }
  rm -rf "$TMP/hanzi-writer-data" "$extract_dir"
  tar -xzf "$temp" -C "$TMP"
  mkdir -p "$extract_dir"
  cp -R "$TMP/hanzi-writer-data-ad1a9905cada18d07630acc27d438b070d753ec0/data/." "$extract_dir/"
  printf '%s\n' "$archive_hash" > "$marker"
}

fetch 'https://raw.githubusercontent.com/skishore/makemeahanzi/bddc96d41bef78427ed0e034e9f7e31d71fd1b92/dictionary.txt' data/mmah/dictionary.txt 744bb05d5b0742e9ee35c37791f94d56a173349b3367569e7ca11e510364d203
fetch 'https://raw.githubusercontent.com/skishore/makemeahanzi/bddc96d41bef78427ed0e034e9f7e31d71fd1b92/COPYING' data/mmah/COPYING 26fa6b699b32401de4b75a4af574c2e0a99b3df6cb12df9313d257ee4634359c
fetch 'https://raw.githubusercontent.com/ivankra/hsk30/4ff9e3915ce87baaecd7ebe263085573a4ea3192/hsk30.csv' data/hsk30/hsk30.csv 8c2b73f74776240bcf154624730fc6fb2c42c254d5c5d0f88943878b8e575b9b
fetch 'https://raw.githubusercontent.com/ivankra/hsk30/4ff9e3915ce87baaecd7ebe263085573a4ea3192/LICENSE' data/hsk30/LICENSE 9cd70c000ea23c899e71b2cbe7c28e18c60937f6e74d1b344d2923f6bddae44a

mkdir -p data/unihan
if [[ ! -f data/unihan/Unihan_Readings.txt ]] || [[ "$(shasum -a 256 data/unihan/Unihan_Readings.txt | awk '{print $1}')" != c307b7358c73ca9d17c75c6b75a462f1797067b0ab5eccf0d7bed724bcd86ce6 || ! -f data/unihan/Unihan_Variants.txt ]] || [[ "$(shasum -a 256 data/unihan/Unihan_Variants.txt | awk '{print $1}')" != b3afc19d33c44b6b62a3189b9cb2c94b89b166c79763e98ef40c2ca05b876f7d ]]; then
  fetch 'https://www.unicode.org/Public/16.0.0/ucd/Unihan.zip' "$TMP/Unihan.zip" b8f000df69de7828d21326a2ffea462b04bc7560022989f7cc704f10521ef3e0
  unzip -p "$TMP/Unihan.zip" Unihan_Readings.txt > "$TMP/Unihan_Readings.txt"
  mv "$TMP/Unihan_Readings.txt" data/unihan/Unihan_Readings.txt
  unzip -p "$TMP/Unihan.zip" Unihan_Variants.txt > "$TMP/Unihan_Variants.txt"
  mv "$TMP/Unihan_Variants.txt" data/unihan/Unihan_Variants.txt
fi
fetch 'https://www.unicode.org/license.txt' data/unihan/UNICODE-LICENSE.txt e7a93b009565cfce55919a381437ac4db883e9da2126fa28b91d12732bc53d96
fetch 'https://www.unicode.org/Public/16.0.0/ucd/CJKRadicals.txt' data/unihan/CJKRadicals.txt 4a96507f4644bbbb11b423edb436e05cee84dadd57413e7571b5af67321474f1

# CC-CEDICT snapshot published 2026-09-25; its content hash pins the export.
mkdir -p data/cedict
if [[ ! -f data/cedict/cedict_ts.u8 ]] || [[ "$(shasum -a 256 data/cedict/cedict_ts.u8 | awk '{print $1}')" != f8d5912ffa25149a24a229e75fb0691ffce5462770d270ba06c3e7757d6622bf ]]; then
  fetch 'https://www.mdbg.net/chinese/export/cedict/cedict_1_0_ts_utf-8_mdbg.txt.gz' "$TMP/cedict.txt.gz" ee4a029f3a608ccc998b63e73ef933753bf3dce6ec59925aeaf8e69a490b46a1
  gzip -dc "$TMP/cedict.txt.gz" > "$TMP/cedict_ts.u8"
  mv "$TMP/cedict_ts.u8" data/cedict/cedict_ts.u8
fi
fetch 'https://creativecommons.org/licenses/by-sa/4.0/legalcode.txt' data/cedict/CC-CEDICT-LICENSE.txt 28a9529c7d0bb4dc51f4bf5c116a3d16ef247a052f7591466768ddf563fd1cf5

fetch_archive 'https://github.com/chanind/hanzi-writer-data/archive/ad1a9905cada18d07630acc27d438b070d753ec0.tar.gz' 88144aa2e9b4d0647c13a54e0af02f8dfa46a348125e23f6f66a7b2062f76727 data/hanzi_writer/.archive.sha256 data/hanzi_writer
fetch 'https://raw.githubusercontent.com/chanind/hanzi-writer-data/ad1a9905cada18d07630acc27d438b070d753ec0/ARPHICPL.TXT' data/hanzi_writer/ARPHICPL.TXT 5590533436c70f10f2f524ee61456238c290175c6662fbe1c700b5f038a6d328

echo "Character data ready (MMAH, HSK 3.0, Unihan 16.0.0, CC-CEDICT, Hanzi Writer)."
