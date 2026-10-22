# 03 — Core script (lesson 2: 你在哪儿？)

Source: ChatGPT draft (chosen over Grok and Gemini by PM, 2026-10-22: avg 12.1 chars/line,
max 18, 45 new chars by script, no 了). PM edits: p4 adds 接; p15 uses the draft's own
了-free variant; 是不是 kept as a fixed chunk. For this dialogue topic the core script is
also the story: step 5 is skipped, steps 6–7 review this file.

## Script

```
n1  | narrator  | 王芳今天来开会。她在一座楼的门口，进不去。 | Wáng Fāng jīntiān lái kāihuì. Tā zài yí zuò lóu de ménkǒu, jìn bu qù. | Hôm nay Vương Phương đến họp. Cô ấy đứng ở cửa một toà nhà, không vào được.
p1  | wang_fang | 喂，陈东，你在八楼吗？ | Wéi, Chén Dōng, nǐ zài bā lóu ma? | A lô, Trần Đông, anh đang ở tầng 8 à?
p2  | chen_dong | 对，我在八楼。你在哪儿？ | Duì, wǒ zài bā lóu. Nǐ zài nǎr? | Đúng, tôi đang ở tầng 8. Cô đang ở đâu?
p3  | wang_fang | 我在楼下，门口这里。我没有卡，进不去。 | Wǒ zài lóu xià, ménkǒu zhèlǐ. Wǒ méiyǒu kǎ, jìn bu qù. | Tôi ở dưới toà nhà, ngay cửa đây. Tôi không có thẻ, không vào được.
p4  | chen_dong | 好，你等一下，我马上下楼接你。 | Hǎo, nǐ děng yíxià, wǒ mǎshàng xià lóu jiē nǐ. | Được, cô đợi một chút, tôi xuống đón cô ngay.
n2  | narrator  | 陈东从八楼下来，在大厅找王芳。 | Chén Dōng cóng bā lóu xiàlái, zài dàtīng zhǎo Wáng Fāng. | Trần Đông từ tầng 8 đi xuống, tìm Vương Phương ở sảnh.
p5  | chen_dong | 我在大厅，你在哪个门口？ | Wǒ zài dàtīng, nǐ zài nǎ ge ménkǒu? | Tôi đang ở sảnh, cô ở cửa nào?
p6 | wang_fang | 我在门口啊。你能看见我吗？ | Wǒ zài ménkǒu a. Nǐ néng kànjiàn wǒ ma? | Tôi ở ngay cửa mà. Anh có thấy tôi không?
p7 | chen_dong | 看不见。我在这里，你在哪儿？ | Kàn bu jiàn. Wǒ zài zhèlǐ, nǐ zài nǎr? | Không thấy. Tôi đang ở đây, cô ở đâu?
p8  | wang_fang | 你旁边有什么？我旁边有一家咖啡店。 | Nǐ pángbiān yǒu shénme? Wǒ pángbiān yǒu yì jiā kāfēidiàn. | Bên cạnh anh có gì? Bên cạnh tôi có một quán cà phê.
n3 | narrator | 陈东看看旁边，没有咖啡店。 | Chén Dōng kànkan pángbiān, méiyǒu kāfēidiàn. | Trần Đông nhìn quanh, bên cạnh không có quán cà phê nào.
p9  | chen_dong | 咖啡店在你旁边？你在哪个楼的门口？ | Kāfēidiàn zài nǐ pángbiān? Nǐ zài nǎ ge lóu de ménkǒu? | Quán cà phê ở cạnh cô à? Cô đang ở cửa toà nào?
p10 | wang_fang | 对，咖啡店在我旁边。我在A座门口等你啊。 | Duì, kāfēidiàn zài wǒ pángbiān. Wǒ zài A zuò ménkǒu děng nǐ a. | Đúng, quán cà phê ở cạnh tôi. Tôi đang đợi anh ở cửa toà A mà.
p11 | chen_dong | 等一下，A座旁边没有咖啡店。你是不是在B座？ | Děng yíxià, A zuò pángbiān méiyǒu kāfēidiàn. Nǐ shì bu shì zài B zuò? | Đợi chút, cạnh toà A không có quán cà phê. Có phải cô đang ở toà B không?
p12 | wang_fang | 啊？这里不是A座吗？我以为我在A座。怎么办？ | Á? Zhèlǐ bú shì A zuò ma? Wǒ yǐwéi wǒ zài A zuò. Zěnme bàn? | Hả? Đây không phải toà A à? Tôi cứ tưởng mình ở toà A. Giờ làm sao đây?
p13 | chen_dong | 别急，A座在B座对面。你马上过来，我在门口等你。 | Bié jí, A zuò zài B zuò duìmiàn. Nǐ mǎshàng guòlái, wǒ zài ménkǒu děng nǐ. | Đừng vội, toà A ở đối diện toà B. Cô sang đây ngay đi, tôi đợi cô ở cửa.
n4  | narrator  | 几分钟后，王芳走到A座门口。 | Jǐ fēnzhōng hòu, Wáng Fāng zǒu dào A zuò ménkǒu. | Vài phút sau, Vương Phương đi đến cửa toà A.
p14 | wang_fang | 陈东，我在这里！你能看见我吗？ | Chén Dōng, wǒ zài zhèlǐ! Nǐ néng kànjiàn wǒ ma? | Trần Đông, tôi ở đây! Anh có thấy tôi không?
p15 | chen_dong | 看见你了！好，我们一起进去开会。 | Kànjiàn nǐ le! Hǎo, wǒmen yìqǐ jìnqù kāihuì. | Thấy cô rồi! Được, chúng ta cùng vào họp.
```

## New words (from the draft; Hán-Việt to be cross-checked in step 10)

喂 · 八楼 · 哪儿 · 楼下 · 门口 · 卡 · 进不去 · 等一下 · 马上 · 下楼 · 接 · 大厅 · 看见 ·
旁边 · 咖啡店 · 对面 · 座 · 以为 · 怎么办 · 别急 · 过来 · 几分钟 · 开会 · 进去 ·
also in lines: 来, 从…下来, 找, 什么, 哪个, 开始, 她, 啊, 后

## Lesson 1 words used

对 · 这里 · 一起 · 今天 · 没有 · 别 · 看 · 问 · 也 · 是 · 有 · 你/我.
Weak lesson 1 words (还是, 谁, 知道, 名字, 同事, 问题) are not forced into the dialogue;
step 9 (Phản xạ questions, narration) recycles them where natural.

## New characters (script, PM)

45 (after review round 1): 下个为么什从以会儿八几分办卡厅去口咖哪啊啡喂在大她店座开怎急找接旁来楼等见走边过进钟门面马. Names: 王芳陈东.
Lines avg 13.2 chars, max 18, no 了.

## Review round 1 (PM, 2026-10-22)

Applied: 06 p10 (empty clue → coffee-shop logic: A has none, so she is at B), 06 p12 (+这里不是A座吗; 以为 kept: natural without 了, 06's "了 required" claim rejected), 07 "hard to track who is where" (n3/p9–p13). Rejected: 座→栋, 看见→看到 (both natural), 07 "Hán-Việt doesn't help" for 对面/大厅/开会/以为 (wrong). 07 "new words heard once" → step 9 exposure bands.

## Open doubts

- 是不是 (p11) kept as a chunk; alternative 你在B座吗？.
- p15 "对，我看见你" is 了-free but less natural than 看见你了 (draft's own note).
- 咖啡店 vs 咖啡馆: both fine; keep 咖啡店.
