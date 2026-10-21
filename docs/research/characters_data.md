# Character data sources — license research
_Date: 2026-10-21. Commercial use in a paid app (data bundled in packages)._

---

## 1. Hán-Việt readings

| Source | URL | License | Commercial | Obligations | Coverage | Format | Updated | 妈学谢 |
|--------|-----|---------|------------|-------------|----------|--------|---------|--------|
| Unihan kVietnamese | https://unicode.org/charts/unihan.html | Unicode License | ✓ | Include copyright notice | ~3000+ chars with field | TSV (UCD) | Annual | 媽=mã ✓, 學=học ✓, 謝=tạ ✓ |

**Gap:** `kVietnamese` is indexed by Unicode codepoint (works per codepoint, not limited to traditional). However, simplified-only chars (e.g. 们 U+4EEC) may lack the field if the character has no classical reading; check at pipeline time. Unihan `kTraditionalVariant` (same Unicode License) bridges simplified → traditional when needed.

**Recommendation:** Unihan kVietnamese + kTraditionalVariant fallback. Author gaps (<5% of HSK 1–6 set expected).

**NOTICE text:** "Contains data from the Unicode® Character Database, © Unicode, Inc. Used under the Unicode, Inc. License Agreement (https://www.unicode.org/license.txt)."

---

## 2. Decomposition / IDS with semantic/phonetic roles

| Source | URL | License | Commercial | Obligations | Coverage | Format | Updated | 妈学谢 |
|--------|-----|---------|------------|-------------|----------|--------|---------|--------|
| Make Me a Hanzi `dictionary.txt` | https://github.com/skishore/makemeahanzi | LGPL 3+ (https://github.com/skishore/makemeahanzi/blob/master/COPYING) | ✓ | Ship LGPL notice; data modifications must be LGPL3; app code unaffected | ~9000 simplified chars | JSONL | 2017 (stable) | 妈=⿰女马(phn:马,sem:女) ✓, 学=⿱⿱⺍冖子 ✓, 谢=⿰讠射(phn:射,sem:讠) ✓ |
| cjkvi-ids `ids.txt` | https://github.com/cjkvi/cjkvi-ids | GPLv2 | ✗ risk | GPLv2 copyleft could reach pipeline code | ~80k chars | TSV IDS | 2022 | ✓ but no semantic/phonetic roles |

**Recommendation:** Make Me a Hanzi `dictionary.txt`. Has IDS decomposition + `etymology` field with `type` (pictophonetic/ideographic), `semantic`, `phonetic`, `hint`. All 6 sample chars present with roles. LGPL3 does not affect `kmp/` code.

**NOTICE:** "Contains data from Make Me a Hanzi (https://github.com/skishore/makemeahanzi), licensed under LGPL 3.0."

---

## 3. Stroke-order paths

| Source | URL | License | Commercial | Obligations | Coverage | Format | Updated | 妈学谢 |
|--------|-----|---------|------------|-------------|----------|--------|---------|--------|
| Hanzi Writer data | https://github.com/chanind/hanzi-writer-data | Arphic Public License (ARPHICPL.TXT in repo) | ✓ | Bundle APL license file; unmodified use OK; modifications to stroke data must be made freely available | ~9000 chars | JSON per char (SVG paths + strokes) | 2023 | all 6 ✓ |
| AnimCJK | https://github.com/parsimonhi/animCJK | APL (graphics) + LGPL3 (other) | ✓ | Same as above + LGPL for non-graphics parts | ~36000 chars | SVG files + text | 2026 | ✓ |

**Recommendation:** Hanzi Writer data (JSON per char, easy to integrate). Arphic PL copyleft scope is the "Font" (stroke data) only — does not reach `kmp/` code. No modification intended, so only obligation is to bundle the APL license file.

**NOTICE:** "Stroke data from Hanzi Writer (https://github.com/chanind/hanzi-writer-data), derived from Arphic Technology fonts, licensed under the Arphic Public License (https://ftp.gnu.org/non-gnu/chinese-fonts-truetype/LICENSE)."

---

## 4. Frequency + HSK level lists

| Source | URL | License | Commercial | Obligations | Coverage | Format | Updated | 妈学谢 |
|--------|-----|---------|------------|-------------|----------|--------|---------|--------|
| ivankra/hsk30 | https://github.com/ivankra/hsk30 | MIT | ✓ | Include MIT copyright notice | HSK 3.0 (2021), 3000 chars | CSV | 2023 | 妈 HSK1 ✓, 学 HSK1 ✓, 谢 HSK2 ✓ |
| SUBTLEX-CH | https://pmc.ncbi.nlm.nih.gov/articles/pmid/20532192 | Informal (email permission for non-academic use, CC BY-SA-like) | ⚠️ | No formal license for commercial bundling; contact Brysbaert | ~97k words | TSV | 2010 | ✓ |
| Jun Da | https://lingua.mtsu.edu/chinese-computing/old-version1/ | Research/teaching only | ✗ | No commercial use without written permission | ~9933 chars | HTML/download | 2004 | ✓ |

**Recommendation:** ivankra/hsk30 for HSK level (MIT, clean). For raw frequency rank: SUBTLEX-CH has no formal commercial license — either skip frequency rank or derive from a permissively-licensed corpus. Mark as author-gap.

**NOTICE:** "HSK 3.0 data from https://github.com/ivankra/hsk30 (MIT), © 2023 Ivan Krasilnikov et al."

---

## 5. Meanings

### English

| Source | URL | License | Commercial | Obligations | Coverage | Format | Updated | 妈学谢 |
|--------|-----|---------|------------|-------------|----------|--------|---------|--------|
| CC-CEDICT | https://cc-cedict.org/ | CC BY-SA 4.0 | ✓ | Attribution required; adaptations (modified entries) must be shared CC BY-SA; unmodified extraction OK; app code unaffected | ~120k entries | UTF-8 text | Monthly | 妈=母,妈妈 ✓, 学 ✓, 谢=感谢 ✓ |

**Recommendation:** CC-CEDICT. Share-alike scope is the dictionary data, not `kmp/` code. Standard practice for commercial apps.

**NOTICE:** "Dictionary data from CC-CEDICT (https://cc-cedict.org/), © MDBG, licensed under CC BY-SA 4.0."

### Vietnamese

| Source | URL | License | Commercial | Obligations | Coverage | Format | Updated | notes |
|--------|-----|---------|------------|-------------|----------|--------|---------|-------|
| Wiktionary vi | https://vi.wiktionary.org/ | CC BY-SA 3.0 | ✓ | Attribution + share-alike (data only) | Partial; ~2000 CJK entries | HTML scrape / XML dump | Ongoing | Inconsistent structure |
| Thiều Chửu 1942 | — | Unclear (2011 publisher edition) | ✗ | Publisher copyright not confirmed expired | ~10k chars | Book scan | 2011 reprint | Not machine-readable |

**Recommendation:** Author Vietnamese meanings with AI + human review. Use Wiktionary vi XML dump as a reference (CC BY-SA 3.0 — extract readings for internal reference, attribute if published). Hán-Việt readings from Unihan (need 1) cover the most important Vietnamese data point.

---

## 6. Component names in Vietnamese (bộ thủ)

| Source | URL | License | Commercial | Notes |
|--------|-----|---------|------------|-------|
| None found | — | — | — | No structured digital list for simplified components in Vietnamese |

**Recommendation:** Author it. ~214 Kangxi radicals + ~30 simplified-only components (氵讠扌钅饣纟糹讠). ~250 entries total. AI-generate Vietnamese names (e.g. 氵→ "ba chấm thủy", 讠→ "ngôn đứng") + native speaker review. ~2 hours.

---

## Sample records

### 妈 (mā)
- **Pinyin:** mā | **Hán-Việt:** mã (Unihan: 媽 kVietnamese)
- **Decomposition:** ⿰女马 | **Semantic:** 女 (woman) | **Phonetic:** 马 (mǎ)
- **English:** mother, mama (CC-CEDICT)
- **Vietnamese:** mẹ [authored]
- **Stroke data:** ✓ (Hanzi Writer data)

### 谢 (xiè)
- **Pinyin:** xiè | **Hán-Việt:** tạ (Unihan: 謝 kVietnamese)
- **Decomposition:** ⿰讠射 | **Semantic:** 讠 (speech) | **Phonetic:** 射 (shè)
- **English:** to thank; to refuse politely (CC-CEDICT)
- **Vietnamese:** cảm ơn, từ chối [authored]
- **Stroke data:** ✓ (Hanzi Writer data)

---

## Combined NOTICE text (app settings / about screen)

```
Dictionary data: CC-CEDICT (cc-cedict.org) © MDBG — CC BY-SA 4.0
Decomposition: Make Me a Hanzi (github.com/skishore/makemeahanzi) — LGPL 3.0
Stroke data: Hanzi Writer (github.com/chanind/hanzi-writer-data), derived from
  Arphic Technology fonts — Arphic Public License
HSK data: github.com/ivankra/hsk30 © 2023 Ivan Krasilnikov et al. — MIT
Character data: Unicode® Character Database © Unicode, Inc. — unicode.org/license.txt
```

---

## Risks

| Risk | Impact | Mitigation |
|------|--------|------------|
| LGPL3 (dictionary.txt) | Data file must ship with LGPL notice; data modifications must be LGPL | Bundle unmodified; include notice; `kmp/` code unaffected |
| Arphic PL (stroke data) | Modifications to stroke SVG/JSON must be made freely available | Bundle unmodified; include APL file |
| CC BY-SA 4.0 (CC-CEDICT) | Modified entries must be re-shared | Don't modify entries; use as-is |
| Hán-Việt gaps | ~5% of HSK chars may lack kVietnamese | Author gaps (small set, AI+review) |
| No frequency license | SUBTLEX-CH / Jun Da not commercially clear | Use HSK level only; skip absolute frequency rank |
| Vietnamese meanings | No ready source | Author all with AI+human review (pipeline step) |
| Bundled size | MMAH dict ~3 MB, HW stroke ~15 MB, CEDICT ~10 MB compressed | ~28 MB total; acceptable for lesson packages |
