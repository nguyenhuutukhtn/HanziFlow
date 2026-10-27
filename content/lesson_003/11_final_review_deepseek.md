# 11 — Final review (DeepSeek, second reviewer)

Lesson 3 · 午饭点错菜 · workflow step 11, reviewer 2. Review only: no source file was edited.

Read: `content/workflow.md` (§Rules for every step, §Independent lessons), `09_spec.md`,
`04b_facts.md`, `05_story.md`, `09_modes.md` (all five modes). Not read: `11_final_review_claude.md`,
`03_core.md`, `10_hanzi_*`, the per-mode `09_*.md` files.

Mechanical checks run with a script (row cells only, markdown prose excluded):

- `09_modes.md`: 55 `coach_vi` rows, **all in Hiểu**, Chinese column `—`, 0 CJK in the Vietnamese;
  duplicate refs 0; half-width punctuation in 中文 0; every `gap` row uses the `—` form.
- Hiểu: 37/37 story rows + 15/15 phrases have a `…v` gloss; the `hr…` pass has 37 rows.
- Nghe: story twice (37 + 37 rows), 18-row third-person retelling (no dialogue), 4 variants.
- Phản xạ: 48 questions (spec 40–50), each with gap + short + full answer.
- Nói: 29/29 story dialogue lines shadowed (12 张静 + 17 刘洋, no narrator rows), 9 chunk build-ups.
- Pinyin: identical 中文 → identical pinyin everywhere (one `里` exception, §5); spot-checked
  sandhi is correct (`yí ge`, `yí fèn`, `yì kǒu`, `yì jiā`, `shíyī`, `shí'èr`, `bù chī`,
  `bú là`, `bú yòng`, `bù zhīdào`, `chī bu liǎo`, `shì bu shì`).
- Exposure, substring count over all five modes' 中文 cells: 懂 1, 回答 4, 外卖小哥 3, 打开 6,
  麻烦 8, 同事 6, 公司 7; 辣 97, 面 116, 吃 151 (compound-inflated, see §6).

Overall the Chinese is genuinely spoken, not textbook: Hiểu, Nghe and Nói have no stuffed or
translated-sounding line. The exceptions are listed below.

## 1. Facts (04b) and the Nghe retellings

`l71–l72` | **block** | Variant 3 uses 二十分钟 / 十二点二十 / 多等了五分钟. 04b's
"Never allowed" list names 二十分钟 by name ("any changed number") and F17 fixes 十五分钟. The
arithmetic is consistent, but as 04b stands this is a contradiction. | Drop variant 3 (3 variants
still meet "3–4"), or add a "variant changes" clause to 04b that authorises this one.

`l76–l78` | **block** | Variant 4 has 张静 liking spicy food and finishing the box. F16:
"Not used, never introduce: … 张静 eating the spicy dish"; F5 calls 不吃辣 a permanent trait. It is
announced, so it is a fake version, but 04b bans the content outright. | Same choice as above:
authorise it in 04b, or replace the detail (e.g. 这次刘洋先吃辣的盒子).

`l59–l63` | fix | Variant 1 is announced as "张静的菜不一样" but changes three facts: her dish
(F3 牛肉面), 商家 没有送错 (F8/F13), and nobody phones (F15). "Exactly one detail" is not met, and
`l63` ("张静一点也没有等") ignores that delivery still takes thirty minutes. | Keep the dish change
and let the other lines read as its consequence ("所以商家没有送错，两个人也没有打电话"), or declare the
cascade in 04b.

`l64–l68` | fix | Variant 2 moves the spicy box to 刘洋 (F10) and drops the phone call (F15) without
saying why, although 刘洋 still received a dish he did not order. `l68` therefore reads as a second,
unannounced change. | Add one line: "刘洋的饭也送错了，可是他不在乎，所以谁也不用打电话。"

`l75` | fix | The announcement is vague ("这次张静变了"); the other three variants name the changed
detail. A learner started cold cannot tell what to listen for. | "这次张静爱吃辣了。"

`05_story` word table, row `外卖小哥 1 · s13` | fix | Step 8 rewrote s13 as 刘洋拿回来两个盒子; the word
is no longer in any story line, so the story count is 0 (only Từ vựng v5/v42 and Phản xạ q17b say it,
3 hearings total). The same stale claim sits in the "Fact check while writing" line (`F9 s11, s12, s13`). |
Set the story row to 0 `—`, and either add one hearing in Nghe (e.g. `l44`) or accept 3.

`05_story` "Speaker split" | fix | Says coach_zh 7 / zhang_jing 12 / liu_yang 16. The story actually has
8 / 12 / 17 (37 rows). This is the number step 11 and Nói's "29 dialogue lines" rest on. | 8 / 12 / 17.

Announcement and consistency otherwise pass: l58, l59, l64, l69, l75 announce the variant block and
each variant, l81 closes it, l82 restores the real story; variant 3's arithmetic (12:00 + 20 min) and
variant 4's logic are internally sound.

## 2. Naturalness (spoken Chinese)

`l58` | fix | "再听一遍，下面几个版本不一样，不是真的故事。" — nothing is being repeated yet; the line
contradicts what follows. | "接着听，下面几个版本不一样，都不是真的故事。"

`l43` | note | 27 characters, three clauses in one Nghe line (刘洋自己要了红烧肉饭，一共四十八块，他在手机上
付了钱). Long for a beginner's first listen. | Split after 红烧肉饭。

`l63` | note | "两个人吃得很好" is written-style, and "一点也没有等" reads as if she never waited for
delivery at all. | "两个人吃得很开心，张静也没有多等。"

## 3. Vietnamese glosses (Hiểu) and Từ vựng explanations

`hs6v` | fix | "người Bắc gọi thịt bò là 'ngưu nhục'" invents a north/south difference: 牛肉 is the
word everywhere; 牛/肉 is simply Hán-Việt. | "'Ngưu nhục' là âm Hán-Việt của 牛肉."

`hs7v` | fix | "kiểu trả lời rất miệng" is not Vietnamese. | "kiểu đáp rất cửa miệng".

`h1` + `hs20v` | fix | Vietnamese variety is mixed: h1 "quán giao lộn món", hs20v "gọi lầm", while the
rest of the file uses "giao nhầm" (~20×). One TTS voice should not switch between lộn/lầm and nhầm. |
"giao nhầm món"; "anh ấy gọi nhầm".

`v12` | fix | "他说'随便'，就是他不知道，你给他选一个" teaches the wrong meaning: 随便 = 吃什么都行 /
你来定, not "I don't know". It also contradicts v18 in the same item. | "他说'随便'，就是吃什么都行，你给他选一个。"

`v71` | fix | "订单上写着你要的菜，还有多少钱。" — 还有多少钱 parses as "how much money is left"; the
point is the total. | "订单上写着你要的菜，还有一共多少钱。"

`v95` | fix | "味道不错，就是很好吃。" and the gloss "nghĩa là rất ngon" oversell 不错 (不赖/挺好, "quite
good"), and 就是很好吃 is a stiff equation. | "味道不错，就是挺好吃的。" · "…nghĩa là khá ngon."

`v91` | fix | "十二点了，你还没吃饭，你就很饿。" — "你就很饿" is redundant and unnatural. |
"十二点了还没吃饭，就会很饿。"

`hs8v` | note | "tức là đồng tiền mình" muddles 块; the learner needs the unit, not "our currency". |
"'Khối' là cách nói tắt của 元 — tệ."

`hs31v` | note | "Hai người này chuyển tiền qua điện thoại" implies a transfer happened; F14 has 张静
pay nothing. | "Cô ấy định chuyển khoản, nhưng anh ấy không lấy."

`v106` | note | "'别客气'，就是你不用谢他，也不用还钱。" — 客气 is not 谢; the gloss teaches the wrong
paraphrase. | "他说'别客气'，就是真不用你还，你别推了。"

`v63` | note | "送是把东西给你，错是不对。" is clipped ("错是不对" needs 就). | "送，就是把东西给你；错，就是不对。"

`v78` | note | "这个不对，你要那个，就叫换。" jumps between clauses. | "这个不对，你想换成那个，这就叫换。"

`v57` | note | "他回答的时候，就说'我点的是……'" — the learner is the one answering, so the subject
shifts mid-sentence. | "你回答的时候，就说'我点的是……'。"

`v30` | note | "你想给他来一份，就先问他'要不要'。" misses 也 and reads like ordering for a third person. |
"你想给他也来一份，就先问他'要不要'。"

`v13` + `v14` | note | Từ vựng invents a 刘洋→张静 exchange; v14 gives 张静 "我不知道，你呢？", which is a
line she never says and clashes with her 随便 / 我今天想吃面. | Make v14 "随便，你点吧。" (reuses P3) or
mark the pair as an example, not the story.

`v70` | note | "你在手机上点了菜，手机上就有一个订单。" repeats 手机上 and 就 awkwardly. |
"你在手机上点了菜，就会有一个订单。"

No Từ vựng explanation is too hard for the level; the longest (v85, 22 chars; v64, 27 chars) are still
spoken. The comprehension bugs are v12 and v71 above, not length.

## 4. Phản xạ

`q6` | **block** | "张静有没有说要吃什么？" → "没有" contradicts F2: she does name 牛肉面 as soon as
刘洋 offers it (s6–s7), and q8 later asks exactly that. With no time frame the question has a false
answer. q6b ("她没说…") is wrong for the same reason. | "一开始刘洋问她，她怎么说？" → "她说随便，让刘洋点。"

`q22` | fix | "张静的盒子里是不是牛肉面？" has two correct answers: 不是 at 12:00, 是 at 12:15. |
"第一次张静的盒子里是什么？" → 麻婆豆腐饭。

`q16` | fix | "他们等了多久？" is 30 min for the first delivery and 15 more for the replacement — two
answers, and the short answer 三十分钟 only fits one reading. | "第一份外卖等了多久？" → 三十分钟。

`q46` | fix | "张静最后付钱了吗？" is answered "不付，刘洋请她。" — 了吗 needs 没付/没有, so the
canonical answer does not fit its own question. | Ask "张静要付钱吗？" — the canonical answer then fits
unchanged.

`q32` | note | "刘洋点错了没有？" voices the false belief. The answer (没有，点对了) is correct, so F12
is not broken, but the drill plants 点错了 as a proposition. | Optional: "刘洋点对了没有？"

`q47` + `q48` | note | Both ask about 刘洋 eating the spicy box (same F6/F15 fact). |
Keep both (q48 drills the 是谁…的 cleft) or drop q48.

`q4b` | note | "刘洋是十一点半点的。" has 点 twice in a row and no object, which is hard to catch by
ear. | "刘洋是十一点半点的外卖。"

One correct answer and a clear subject otherwise hold for all 48 questions; the canonical short answers
are reused verbatim (punctuation only).

## 5. Mode rules and pinyin

`q0` | note | Phản xạ opens with two lines ("我来问，你来回答。" + "听问题，马上回答，不用想很久。");
rule 6 asks for one fixed short line naming the mode, and rule 7's fixed line is 你回答。 The line does
name the activity, so this is a variant, not a break. | Swap q0 to 你回答。 and keep q0b, or accept as is.

`q19`/`q19b`/`q21`/`q21b`/`q22` | fix | 盒子里 is written `hézi li` (neutral) here, but `hézi lǐ` in
v73, l47, l66, l68, l70, l76. Same string, two pinyin forms. | Normalise to `hézi lǐ` everywhere.

`n2–n28` | fix | Nói has 9 chunk build-ups (spec 6–8) and two of them have only 2 steps (n12–n13
吃什么/想吃什么; n14–n15 我来点外卖吧): n13 is a fragment, not a line the learner can use. |
Drop n12–n13 and extend n14–n15 to 点外卖 → 我来点外卖 → 我来点外卖吧 → full line.

`n1–n58` | note | 115 rows (58 spoken + 57 gaps) is ~9–10 min against the 5–8 min band. |
If the PM measures it long, shadow only the 张静 lines.

`v1–v108` | note | 16 items against "~12–15". | If 15 is required, cut item 14 (饿死了, v89–v96).

Từ vựng item list, row 9 | fix | "lifts: … 看 (v56)" — v56 is 你想知道他点了哪个菜，就这么问, no 看. |
Change to "问 (v56)" or drop the 看 claim (看 is lifted in Hiểu hs13/hs22 and Nghe only).

Passes: every Chinese-only mode opens with a mode-naming line (l1 我们听故事 / v1 今天我们听几个词 /
q0 我来问，你来回答 / n1 跟我说); coach_vi appears only in Hiểu (55 rows) with `—` in 中文 and no CJK in
Vietnamese; Hiểu glosses all 37 story rows and P1–P15; Nói shadows all 29 dialogue lines with a gap
each and no narrator rows.

## 6. Exposure

Whole file | **block** | The spec's closing requirement — "End with an exposure table: every
Core/Coach word × count per mode × total, flag anything outside its band" — is missing. Từ vựng has an
item/lift table and the Open doubts discuss exposure in prose, but there is no per-mode count and no
band flag, so step 11 and the PM cannot check bands from this file. | Generate the table with the PM's
counting script and append it before TTS.

`懂` (1) and `回答` (4: v57, l2, q0, q0b) | fix | Coach words are supposed to be heard ≥ 8. 懂 occurs
only inside l2 听懂就好; 回答 only in the two openers, l2 and v57. | Add 懂 to a Hiểu or Nghe line (你听懂了吗？) and
回答 to two Phản xạ prompts, or move both out of the Coach tier.

`外卖小哥` (3: v5, v42, q17b) | fix | Core word at 3 hearings against 10–40, and the story no longer
contains it (§1). | Add two natural hearings in Nghe (l44 刘洋下去拿，外卖小哥在楼下等他) or accept the low
count explicitly.

`同事` 6, `公司` 7, `打开` 6, `麻烦` 8 | note | All below the 10–40 band. Bands are guidance, and each is
used naturally, so this is a PM sign-off item, not a defect. | Accept, or lift 打开/麻烦 once each.

`辣` 97, `面` 116, `吃` 151 | note | Raw substring counts exceed the ~60 cap, but they are inflated by
compounds (牛肉面, 吃辣, 好吃, 不吃辣). | No change unless the PM's standalone-token count also reads > 60.

## Open doubts

1. 04b vs `09_spec` on Nghe variants: the spec names 二十分钟 and 张静 eating spicy as allowed
   changed details, 04b's "Never allowed" list bans both. PM decides which document wins (my
   blocks in §1 assume 04b).
2. My exposure figures are substring counts over the 中文 cells only (so compounds inflate them and
   prose is excluded). The PM's script should confirm 懂 / 回答 / 外卖小哥 and the 辣 / 吃 / 面 totals
   before the table is written.
3. `03_core.md` was not read; the Từ vựng item list labels 我请你 as "P15 / Function" while
   `05_story.md` counts 请 as a Core word. One of the two needs correcting.

## Summary

Blocks 4 · Fixes 21 · Notes 19 (44 findings). Facts, mode rules and pinyin are otherwise clean (0 coach_vi violations, 1 answer contradiction, 2 ambiguous questions); naturalness is good, Từ vựng needs the v12/v71/v95/v91 corrections, and Hiểu needs the hs6v/hs7v and lộn/lầm Vietnamese fixes.
Nghe variants 3–4 and the missing exposure table need a PM decision (04b vs 09_spec), not another writer round.
Verdict: not ready for TTS — clear the 4 blocks and the 21 fixes, then it passes.
