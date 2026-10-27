# 04 — Core review (step 4)

Reviewer: DeepSeek (different vendor from the author, Qwen). Reads: `01_brief.md`, `03_core.md`,
`04b_facts.md`, `workflow.md` ("Rules for every step", "Independent lessons", "Vocabulary
balance", Step 4 prompt). Reviews only — no rewrites here.

Yardstick: what two colleagues in a mainland office actually say in 2026. Naturalness outranks
character budget and word bands (workflow, rule 1). "Block" = step 5 would copy a defect;
"fix" = should change before step 5; "note" = judgement or optional polish.

## Judgement on the six phrases the PM asked about

| Phrase | Verdict | Why (finding no.) |
|---|---|---|
| 我给你钱 | ✗ change | Cash-era phrasing. Settling a 22-yuan lunch is 我转你 / 我微信转你; the VN gloss already says "gửi tiền". (1, 2) |
| 外卖员 | △ change | News/official register. Speech: 外卖小哥; app text: 骑手. (3) |
| 红烧肉饭 | ✓ keep | Heard, but a delivery-app dish is normally 红烧肉盖饭. (15) |
| 麻婆豆腐饭 | ✓ keep | Same — 麻婆豆腐盖饭 is the menu string; parallelism is worth more than the 盖. (16) |
| 备注 不要辣 | ✓ keep | Exactly what people type in the app. Cost is exposure, not naturalness. (17) |
| 有那么辣吗？ | ✓ keep | Very natural, and it quietly carries F6. Don't touch. (19) |
| 给商家打电话 | ✓ keep | Natural; 给老板/店家打电话 is a touch more colloquial. (18) |

## Findings

| # | ref | severity | problem | suggested fix |
|---|---|---|---|---|
| 1 | p15a | block | 不用不用,我给你钱 — 给 + 钱 reads as handing cash. Nobody carries cash for a 48-yuan office lunch in 2026; the move is a WeChat/Alipay transfer. The VN already translates it as "gửi tiền cho bạn", so line and gloss disagree. | 不用不用,我转你。 (or 我微信转你) |
| 2 | F14 (+ V9, F7, 04b "Never allowed") | block | The fact table *forbids* 转 and V9 spells out 我给你二十二块 — i.e. it hard-codes the one form that is unnatural, and F7 has already established a cashless world. A tier rule ("转 is in no tier") is deciding a naturalness question. | F14 → 张静 offers 我转你二十二块; delete 转 from the forbidden list; V9 → 我转你二十二块; add 转 (zhuǎn, chuyển khoản) to Core words — it is shorter than 给…钱 and high-frequency. |
| 3 | tier 2.2 外卖员 (+ F16/F19) | fix | 外卖员 is the term TV news uses. Colleagues say 外卖小哥 ("小哥到楼下了"), and the platforms themselves say 骑手. F16 additionally bans 骑手, which is the app's own word. | Use 外卖小哥 once (he barely appears) and gloss 小哥; or cut the word entirely — 外卖 already covers it. Un-ban 骑手. |
| 4 | p11b | fix | 我知道,你点的是牛肉面 — telling her what she ordered is odd as a defence, and it implies she placed the order, contradicting F2 (刘洋 ordered for both). | 我知道,我给你点的是牛肉面。 |
| 5 | p14b | fix | Dialogue dead end: 张静 asks the total (p14), 刘洋 answers 四十八 (p14a), then she repeats the same number from the order. The beat that should follow (paying him back) is missing — and with 转 it becomes the natural close of the scene. | 对,我转你二十二。 (or drop p14b) |
| 6 | p6b | fix | 太好了,我太饿了 — two 太 in one short line is textbook, and this is a core line the learner will hear 10–30 times. | 太好了,我饿死了。 (add 死 to the Function list, or 我都饿死了) |
| 7 | p9a | fix | 刘洋 states 他们送错了 before anyone looks at the order, while F12 says he half-believes *he* tapped wrong and F13 says the order reading is what produces 送错了. Step 5 will inherit the contradiction. | 可能送错了。 … after the 订单: 是他们送错了。 |
| 8 | F6 | fix | 刘洋喜欢吃辣 is a permanent trait the whole ending rests on (he eats the spicy box), but no line in `03_core.md` ever says it — the only trace is the sceptical 有那么辣吗. Unmotivated in the story. | Step 5 must give it one explicit line (我吃辣的 / 我最喜欢吃辣) and treat 吃辣 as a chunk; otherwise drop F15's ending to the documented fallback. |
| 9 | tier 2.2 (bands) | fix | Measured in `03_core.md`'s own example lines: 14 of 35 Core words appear 0× (午饭 麻婆豆腐 水 喝 付钱 打开 盒子 备注 外卖员 手机 公司 同事 好吃 等) and 6 more only 1× (菜 订单 分钟 拿 饿 麻烦). Several (打开 好吃 公司 同事 等) have no natural 10-hearing slot in a lunch story. The wrong dish 麻婆豆腐 never appears in a single core line. | Cut or demote 打开 好吃 公司 同事 午饭 手机 等; keep 水 喝 盒子 订单 备注 商家 only if step 5 gives them 3+ natural slots (我喝口水, 给你倒杯水). Target ~28 Core words. |
| 10 | tier list | fix | 面 is a content word used standalone 4× (米饭还是面, 想吃面, 那我的面呢) and sits in no tier; so do 过来 (p13b), 以后 and 半 (facts timeline), and the measure words 份 / 个. | 面 → Core words (miàn, mì); 过来 以后 半 份 个 → Function. |
| 11 | 03 §3 | fix | Section 3 is literally `PLACEHOLDER_CHARACTERS` — the distinct-character list the step-3 prompt requires (and the PM counts) is missing. | Fill it, or run the counting script and paste its output. |
| 12 | all lines | fix | Punctuation is half-width: 15 lines use ASCII `,`, 14 use ASCII `?`, zero full-width `，？`. Looks unprofessional in the app. | Global replace to ，？ ！ inside the Chinese column only (not the pinyin column). |
| 13 | tier 2.2 付钱 | fix | Hán-Việt wrong: 付 = **phó**, not phụ (cf. 付款 phó khoản). | phó tiền. |
| 14 | tier 2.2 吃 | fix | "ngật (cật) ?" — 吃 is ngật only; 	cật is not a reading of it and the question mark will confuse step 10. | ngật. |
| 15 | p7b / F4 | note | 红烧肉饭 is heard but a delivery menu says 红烧肉盖饭; 盖饭 is the dish category. Naturalness cost is small, consistency cost is not. | Keep 饭 *only if* 麻婆豆腐饭 also keeps it (they must match); otherwise 盖饭 for both (+2 chars). |
| 16 | F10 | note | 麻婆豆腐饭 is understood and on real menus, but 麻婆豆腐盖饭 is the standard string. | Keep the 饭 pair for parallelism; never mix 饭 / 盖饭 in one lesson. |
| 17 | tier 2.2 备注 (+ F8) | note | 备注 is authentic as an app field, but as a Core word it can only be heard ~1–2×, and on a 牛肉面 the note is redundant — so F8's "proves 刘洋 ordered right" actually rests on the item list, not the note. | Keep the fact (F8), drop 备注 from Core words, or accept 1–2 hearings and say so in step 9's table. |
| 18 | p13 | note | 给商家打电话 is fine; 商家 is app jargon but universally understood. | Optional: 给老板打个电话 / 我联系一下商家 (the app's own button wording). |
| 19 | p10b | note | 有那么辣吗？ is exactly right — sceptical, spoken, and it seeds F6. No change. | Keep. |
| 20 | p13 / p13a | note | 我给商家打电话 and 我给商家打个电话 are the same sentence twice; 15 phrase slots shouldn't spend two on one form. | Use one slot for 我打电话问问 / 我联系一下商家. |
| 21 | p12b | note | 可以,我打电话问一下 — 可以 is acceptable but 行/好 is what people answer in speech; 问一下 is good. | 行,我打个电话问问。 |
| 22 | 04b V-table | note | The stated reason "请客 (客 off-tier)" is wrong: 客 is already on the Function list via 别客气. 请客 is very common. | Either allow 请客 (add to Function) or drop the bogus reason. |
| 23 | F15 / F17 | note | 15 minutes for a remake *and* redelivery is fast. | One clause makes it credible (商家就在楼下 / 很近), no number change. |
| 24 | tier 2.2 麻烦 | note | 麻烦 is a Core word and 麻烦你了 is also on the Function list — same word in two tiers, against "exactly one tier". | Keep 麻烦你了 in Function, drop 麻烦 from Core. |

## Pinyin / tone

- Tone sandhi convention: **yes, keep it**. `bú shì`, `yí fèn`, `yí ge`, `yígòng`, `búyòng`,
  `yào bu yào`, `dǎ ge diànhuà`, `kèqi`, `guòlai` are all correct as pronounced, and
  as-pronounced pinyin is *better* for an ears-first learner because it matches the audio.
  Two conditions: (a) 03, 05 and 09 must use the identical convention; (b) write `bú yòng`
  with a space like every other word (p15a has `Búyòng búyòng`).
- One character-level nit: the table literal "ngật (cật) ?" should be just ngật (finding 14).
- No tone errors found in the 45 sentence lines or the tier tables. 四十八 sìshíbā, 十五 shíwǔ,
  一共 yígòng, 一份 yí fèn, 一个 yí ge are all right.
- If 转 is added: 我转你 **zhuǎn** (not zhuàn); 外卖小哥 **wàimài xiǎogē**.

## Vietnamese

- p15a: "mình gửi tiền cho bạn" does not match 我给你钱 (hand money) — it matches 我转你. Fixing
  finding 1 fixes this line too.
- tier 外卖 "đồ ăn đặt giao (takeaway/delivery)" is clumsy Vietnamese: use "đồ ăn giao tới" /
  "đồ ăn đặt ngoài".
- p13a "gọi cho quán một cú" — colloquial but odd; "gọi cho quán một lát" is smoother.
- p8a/p8b gloss 这个 as "Hộp này" (the Chinese has no 盒). Understandable in context; change to
  "Cái này không phải của mình" if you want the gloss to match word for word.
- Rest of the glosses (红烧肉 = thịt kho, 麻婆豆腐 = đậu hũ ma bà, 块 = tệ, 送错了 = giao nhầm)
  are correct.

## Facts vs core (04b ↔ 03)

- **F14 vs p15a/F7**: the cashless world of F7 and the cash handover of F14 cannot both be
  true. Findings 1–2.
- **F12/F13 vs p9a**: 刘洋 cannot already know 他们送错了 at the moment he still suspects
  himself. Finding 7.
- **F2 vs p11b**: p11b has 张静 as the one who ordered. Finding 4.
- **F6 has no line**: the ending is unmotivated. Finding 8.
- **F15's 15 minutes** is tight for remake + redelivery (note 23); everything else in the
  timeline (11:30 → 12:00 → 12:15, 22 + 26 = 48, 两个盒子 → 一个盒子) is internally consistent
  and answers cleanly in Phản xạ.
- **F9/F19** (rider never comes up, never speaks) is realistic for an office building, and it
  neatly keeps the voice count at three.

## Tier outliers (only clear ones)

- No count problem: 15 phrases (10–15 ✓), 35 Core words (top of 20–35), 6 Coach (≤6 ✓).
- Clear outlier is *usage*, not count: 14 Core words at 0× and 6 at 1× in `03_core.md`
  (finding 9). That is 20 of 35 — a vocabulary contract the story cannot honour.
- Content words in no tier: 面, 过来, 以后, 半, 份, 个 (finding 10).
- 转 is missing on purpose but is the natural form the lesson now needs (finding 2).

## Answers to `03_core.md` Open doubts

1. Pinyin as pronounced: yes, keep it — matches the audio, better for an ears-first beginner; only make `Búyòng` → `bú yòng` and apply the same rule in 05/09.
2. 吃 = ngật (not cật); 面 "noodle" = miến (麵), diện is the surface/face reading merged by simplification; 豆腐 = đậu hủ (đậu phụ is a fine northern variant) — step 10 settles, but 付 is phó, not phụ.
3. 盖饭 is the menu form; 红烧肉饭 is heard, so keep it only if you also keep 麻婆豆腐饭 — the pair must match, and +2 chars for 盖饭 ×2 is affordable.
4. Keep 麻婆豆腐饭 for parallelism (both boxes are 饭 from the same 商家); never mix 饭 and 盖饭.
5. Keep 备注 不要辣 as a *fact* (authentic, it is a real app field), but drop 备注 from Core words — it cannot reach 10 hearings; F8's proof is the item list.
6. 转 to 我转你 — yes, do it: naturalness outranks the one extra character, and 转 replaces 4 chars with 3.
7. 外卖小哥 in speech, 骑手 in app wording; 外卖员 is too official for two colleagues — change, or drop the word since he appears once.
8. Bands are guidance, but 20 of 35 Core words at ≤1 use is not a small miss — see finding 9.

## Answers to `04b_facts.md` Open doubts

1. Confirmed: as-pronounced pinyin, keep.
2. Settled above: ngật / miến / đậu hủ; fix 付 → phó.
3. Switch to 外卖小哥 (or drop the word); un-ban 骑手 in F16.
4. Keep 刘洋 eating the tasted box — normal between close colleagues; the fallback stays documented but unused.
5. Yes, include one softener (不好意思 / 我看看) — 张静 just implicitly blamed him in p11a, so the beat is natural and costs nothing.

## Summary

- Findings: **2 block, 12 fix, 10 note** (24 total).
- The material is usable and the facts are mostly consistent; the two blocks are surgical (我转你 in p15a/F14/V9; the 转 ban).
- Verdict: **ready for step 5 after fixes** — do findings 1–2, 4, 7, 8 and 9 before writing the story.
