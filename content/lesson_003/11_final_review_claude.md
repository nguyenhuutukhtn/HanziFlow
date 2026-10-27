# 11 — Final review (Claude) · lesson 3 午饭点错菜

Scope: `09_modes.md` vs `04b_facts.md`, `05_story.md`, `09_spec.md`. Findings by ref; nothing rewritten.
Story lines (s1–s35) were treated as approved; doubts about them are in section 6.

## 1. Story logic and facts

| ref | level | problem | suggested fix |
|---|---|---|---|
| q6, q6a, q6b | block | 张静有没有说要吃什么？→ 没有. At the end she does say 我今天想吃面 (s5) and 我要牛肉面 (s7), so "没有" is wrong without a time anchor (F2 only says she names no dish *before* 刘洋 offers one). Two correct answers. | q6 → 刘洋问她吃什么，张静一开始怎么说？ a: 随便。 b: 她说随便，你点吧。 |
| l64–l68 (Nghe V2) | block | The spicy box is 刘洋's, but he ordered 红烧肉饭 (F4). l65 gives him 麻婆豆腐饭 with no explanation, so it is a wrong delivery, yet l68 says nobody calls or waits. Not internally consistent, and it changes two details (who has the box, what 刘洋 ordered). | Add to l64/l65: 这次刘洋点的是麻婆豆腐饭 (he ordered it himself), then the box is correct and l68 holds. |
| q8, q8b | fix | 张静点的是什么？ clashes with q3 (谁点的外卖→刘洋). 张静 did not order; 刘洋 ordered for her. Unclear subject. | q8 → 刘洋给张静点的是什么？ (or 张静要的是什么？); q8b to match. 04b's canonical wording has the same problem; PM may adjust it. |
| q16 | fix | 他们等了多久？ → 三十分钟. After the whole story the wait was 30 + 15 min, so the answer depends on the moment. | q16 → 外卖等了多久才到？ / 点了以后，外卖等了多久？ |
| q23, q48, q48a/b | fix | 那盒菜 / 那盒辣菜 for a rice box is vague (q23 follows q20 about 刘洋's box) and 辣菜 is not natural. q48 duplicates q47. | q23 → 张静那盒辣不辣？ q48 → 那盒辣的饭最后谁吃了？ or drop q48 (47 questions is enough). |
| q25 | fix | 她要什么？ → 水 is odd (要 what?). | 她想喝什么？ / 她要喝什么？ |
| l60 | fix | 她也点了红烧肉饭: 张静 does not order, 刘洋 does (F2, F12). | 刘洋也给她点了红烧肉饭。 |
| l58 | fix | 再听一遍 announces a repeat, but what follows are new versions. | 下面是几个不一样的版本，不是真的故事。 |
| l61–l62 (V1) | note | "商家没有送错" is a second changed fact; it is a logical consequence, so acceptable. | none |
| l75–l80 (V4), l71 (V3) | note | 张静 eats the spicy dish (F16) and 二十分钟 (04b "never allowed"). Both are in the spec's variant list and announced as fake (l58, l69, l75, l81). Internally consistent (12:00 + 20 = 12:20, "多等了五分钟" correct). | keep |
| hs35v | note | "chia nhau một hộp là bình thường": 刘洋 eats the whole box, he does not share it. | → "Lưu Dương ăn hộp đó, không bỏ phí." |
| hs1v | note | "ngang tuổi nhau" is not in the story. | drop it |
| hs18v | note | "Anh ấy ăn cay giỏi mà" gives away F6 before s34. | drop it |
| q2 | note | The answer 公司 is only implied by s1; the story never states the place. | acceptable; or drop the question |

Everything else checked clean: 22/26/48, 11:30/12:00/12:15, 两个/一个, 刘洋 fetched the food, 商家送错了, 我转你 + 今天我请你, and 刘洋 eats the spicy box. The canonical retelling (l40–l57) keeps all numbers and uses V1–V11 only.

## 2. Naturalness

| ref | level | problem | suggested fix |
|---|---|---|---|
| v12 | fix | 他说"随便"，就是他不知道 is the wrong meaning. 随便 = "anything is fine, you pick". | 就是吃什么都可以，你来选。 |
| v13–v14 | fix | A new exchange given to 刘洋 and 张静. 张静 says 我不知道，你呢？, which contradicts the story, where she says 随便 (F2). The voices are heard as the story's. | Use the narrator voice or drop the pair; keep the story line v7 as the example. |
| v106 | fix | 别客气 does not mean 不用还钱 (that comes from 我请你). | 别客气，就是你不用不好意思。 |
| v95 | note | 味道不错 explained as 很好吃 (Vi: "rất ngon"); it means "tasty / not bad". | 就是挺好吃；Vi "khá ngon" |
| v63 | note | 送是把东西给你 is weak. | 送是把东西送到你这儿。 |
| q4b | note | 十一点半点的: "半点…" sounds like a different phrase. | 刘洋十一点半点的外卖。 |
| l73 | fix | 她更饿了，可是面来了，她还是吃得很高兴: 可是 is illogical. | 她更饿了，面一来，她吃得很高兴。 |
| l63 | note | 张静一点也没有等 (she did wait 30 min for the delivery). | 张静不用再等。 |
| l78 | note | 全吃完 is redundant. | 都吃完了。 |
| n12–n15 | note | The back-chain is broken: 想吃什么 → 我来点外卖吧 → full line. | 想吃什么 → 午饭你想吃什么 → full line. |
| q0 | note | Opener differs from the fixed 你回答。 (rule 7). Acceptable, since it names the mode. | keep |

The repetition of 吃/面/辣/点 is natural throughout. I flag no forced passage.

## 3. Gloss accuracy

| ref | level | problem | suggested fix |
|---|---|---|---|
| hs6v | fix | "người Bắc gọi thịt bò là ngưu nhục" is wrong (it implies a dialect). | "thịt bò, Hán-Việt là ngưu nhục" |
| hs8v | fix | "đồng tiền mình, nói tắt" is garbled. | "khối: cách nói thường ngày của 'tệ' (đồng nhân dân tệ)" |
| hp4v | fix | "thêm 'một phần' ở giữa" is wrong, since 一份 is already in the sentence. | "Muốn lịch sự hơn thì thêm 'có muốn không' trước tên món." |
| hp12v | fix | "năng bu năng" mixes Hán-Việt with pinyin "bu" and the Vietnamese voice can't read it. | "thêm 'có thể… không' phía trước" |
| hp15v | fix | "Người kia hay gạt đi: đừng khách sáo". In the story the one who invites says 别客气. | "Người mời hay nói thêm: đừng khách sáo." |
| hp8, hp8s | note | 我点的是牛肉面 is not in the story (s21 is 我给你点的是…). In narration it is half true for 刘洋 (he ordered both dishes). | use 你点的是什么？/ 我给你点的是牛肉面 for P8, or accept it |
| hs7 / v32 | note | Vi differs: "gọi" vs "lấy" mì bò. | unify |
| hs10v | note | "không quét mã" is odd, because phone payment usually is a scan; F7 only rules out cash and paying at the door. | "trả xong trên ứng dụng rồi" |
| hs3v, hs4v, hs9v, hs15v–hs17v, hs22v–hs26v, hs31v–hs32v | ok | Hán-Việt (ma phiền, hoàn thị, tống thác, nhất khẩu, thái, đính đơn, hoán, thương gia, thỉnh) correct. | none |

`coach_vi` rows hold no Chinese characters. Pinyin tone sandhi: 一/不 are applied correctly, but third-tone sandhi (你点 ní diǎn, 可以 kéyǐ, 我给你 wǒ gěi ní) is not marked. The spec says "as pronounced", so the convention is inconsistent (note, pipeline-wide; either mark it or state "no 3rd-tone sandhi").

## 4. Phản xạ

Each question has one answer and a clear subject, except for the findings above (q6 block; q8, q16, q23, q25, q48 fix). q32 (刘洋点错了没有？→ 没有) is a correction beat and is acceptable; 刘洋点错了吗？ is slightly more natural. The 16 canonical answers are present and match 04b, including q47 (红烧肉饭和麻婆豆腐饭) and q46 (不付，刘洋请她). The count is 48.

## 5. Mode rules

- coach_vi appears only in Hiểu. Nothing leaks into the other four modes.
- Openers: v1 今天我们听几个词, l1 我们听故事, q0 我来问，你来回答, n1 跟我说. All are short and name the mode. Hiểu opens with coach_vi (h1) and has 我们听故事 as h4.
- Pinyin matches the Chinese in all rows checked (一/不 sandhi, shí'èr, yí ge, yíxià, kèqi).
- Từ vựng has 16 items against the spec's 12–15, and Nói is about 115 rows (over its band). Both were flagged by the writer; PM decision.

## 6. Lines for an outside native reader (max 10)

1. s31 我原来的面二十二块，我转你。 Is 原来的面 natural after the replacement came? Perhaps 面二十二块，我转你吧。
2. s33 那个麻婆豆腐饭很辣，你吃吗？ Does a colleague offer a box she already bit? And does 刘洋 eating from it sound normal?
3. s20 刘洋，你点的是什么？ Would 张静 not say 你是不是点错了？ / 你点的不是牛肉面吗？
4. s24 能不能换一份？ Is 换 the right word, or 能不能重新送一份？
5. s29 这次不用再付钱，是商家重新送的。 Textbooky? 重新送 vs 重送.
6. s30 / v94 张静终于吃上了牛肉面，味道不错。 Is 吃上了 natural in narration?
7. s3 随便，你点吧，麻烦你了。 Is 麻烦你了 too heavy between colleagues?
8. l41 刘洋在手机上给两个人点了外卖 and l44 刘洋下去拿 (下去 = downstairs).
9. v14 / q4b / l73: replacement wording listed in sections 1–2.
10. s26 十五分钟后送过来 vs 十五分钟后到; s23b 是商家送错了 as a separate line.

## Summary

- 2 block, 17 fix, 14 note (counts include the table rows above, grouped).
- Facts, prices, times and box counts are consistent in all five modes; the blocks are one Phản xạ question (q6) and one Nghe version (V2, l64–l68).
- Verdict: ready for TTS after fixes.
