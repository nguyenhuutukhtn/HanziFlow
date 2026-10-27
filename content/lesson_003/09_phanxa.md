# 09 — Phản xạ (mode file)

Lesson 3 · **午饭点错菜** · Author AI A (Qwen), step 9, mode `phanxa`.
Reads: `09_spec.md` (Rules for all modes + Phản xạ), `05_story.md` (the script),
`04b_facts.md` (F1–F19, canonical short answers), `03_core.md` §1–§2.

Conventions:

- Line format `ref | speaker | 中文 | pinyin | Tiếng Việt`; refs `q1`…`q48`, gap rows `qN.g`,
  short answer `qNa`, full answer `qNb`. Chinese-only mode: `narrator` and `gap` only, no
  `coach_vi` row (spec rules 3, 4).
- Answers follow `04b_facts.md`: the 16 canonical short answers are reused verbatim (only
  punctuation normalized to full-width, spec rule 3). No Nghe-only V variants here.
- One correct answer and one clear subject per question; names 张静 / 刘洋 instead of bare
  他 / 她 in every full answer.
- Easy → hard, in story order: 点单 → 付钱 → 到货 → 打开 → 辣 → 订单/谁错 → 换 → 面到 → 钱 → 结尾.
- Pinyin as pronounced (sandhi 一/不, neutral 吗呢吧的, A-not-A reduced 不: `là bu là`), per
  `03_core.md` doubt 1.

```
q0   | narrator | 我来问，你来回答。 | Wǒ lái wèn, nǐ lái huídá. | Để tôi hỏi, bạn trả lời nhé.
q0b  | narrator | 听问题，马上回答，不用想很久。 | Tīng wèntí, mǎshàng huídá, bú yòng xiǎng hěn jiǔ. | Nghe câu hỏi thì trả lời ngay, đừng nghĩ lâu.
q1   | narrator | 张静和刘洋是什么关系？ | Zhāng Jìng hé Liú Yáng shì shénme guānxi? | Tĩnh và Lưu Dương quan hệ gì?
q1.g | gap | — | — | —
q1a  | narrator | 同事。 | Tóngshì. | Đồng nghiệp.
q1b  | narrator | 他们是同一家公司的同事。 | Tāmen shì tóng yì jiā gōngsī de tóngshì. | Hai người là đồng nghiệp cùng một công ty.
q2   | narrator | 他们在哪儿吃午饭？ | Tāmen zài nǎr chī wǔfàn? | Hai người ăn trưa ở đâu?
q2.g | gap | — | — | —
q2a  | narrator | 公司。 | Gōngsī. | Ở công ty.
q2b  | narrator | 他们在公司吃午饭。 | Tāmen zài gōngsī chī wǔfàn. | Họ ăn trưa ở công ty.
q3   | narrator | 谁点的外卖？ | Shéi diǎn de wàimài? | Ai đặt đồ ăn vậy?
q3.g | gap | — | — | —
q3a  | narrator | 刘洋点的。 | Liú Yáng diǎn de. | Lưu Dương đặt.
q3b  | narrator | 外卖是刘洋点的。 | Wàimài shì Liú Yáng diǎn de. | Đồ ăn là Lưu Dương đặt.
q4   | narrator | 刘洋什么时候点的外卖？ | Liú Yáng shénme shíhou diǎn de wàimài? | Lưu Dương đặt đồ ăn lúc nào?
q4.g | gap | — | — | —
q4a  | narrator | 十一点半。 | Shíyī diǎn bàn. | Mười một rưỡi.
q4b  | narrator | 刘洋是十一点半点的。 | Liú Yáng shì shíyī diǎn bàn diǎn de. | Lưu Dương đặt lúc mười một rưỡi.
q5   | narrator | 刘洋用什么点的外卖？ | Liú Yáng yòng shénme diǎn de wàimài? | Lưu Dương đặt bằng cái gì?
q5.g | gap | — | — | —
q5a  | narrator | 手机。 | Shǒujī. | Điện thoại.
q5b  | narrator | 刘洋用手机点的外卖。 | Liú Yáng yòng shǒujī diǎn de wàimài. | Lưu Dương đặt bằng điện thoại.
q6   | narrator | 张静有没有说要吃什么？ | Zhāng Jìng yǒu méi yǒu shuō yào chī shénme? | Tĩnh có nói muốn ăn gì không?
q6.g | gap | — | — | —
q6a  | narrator | 没有。 | Méi yǒu. | Không.
q6b  | narrator | 她没说，她说随便，你点吧。 | Tā méi shuō, tā shuō suíbiàn, nǐ diǎn ba. | Bạn ấy không nói, chỉ nói gì cũng được, bạn đặt đi.
q7   | narrator | 张静要吃米饭还是吃面？ | Zhāng Jìng yào chī mǐfàn háishi chī miàn? | Tĩnh ăn cơm hay ăn mì?
q7.g | gap | — | — | —
q7a  | narrator | 面。 | Miàn. | Mì.
q7b  | narrator | 张静今天想吃面。 | Zhāng Jìng jīntiān xiǎng chī miàn. | Hôm nay Tĩnh muốn ăn mì.
q8   | narrator | 张静点的是什么？ | Zhāng Jìng diǎn de shì shénme? | Tĩnh đặt món gì?
q8.g | gap | — | — | —
q8a  | narrator | 牛肉面。 | Niúròu miàn. | Mì bò.
q8b  | narrator | 张静点的是牛肉面。 | Zhāng Jìng diǎn de shì niúròu miàn. | Tĩnh đặt mì bò.
q9   | narrator | 刘洋自己点的是什么？ | Liú Yáng zìjǐ diǎn de shì shénme? | Còn Lưu Dương đặt món gì?
q9.g | gap | — | — | —
q9a  | narrator | 红烧肉饭。 | Hóngshāoròu fàn. | Cơm thịt kho.
q9b  | narrator | 刘洋自己点的是红烧肉饭。 | Liú Yáng zìjǐ diǎn de shì hóngshāoròu fàn. | Lưu Dương tự đặt cơm thịt kho.
q10  | narrator | 刘洋点的是不是红烧肉饭？ | Liú Yáng diǎn de shì bu shì hóngshāoròu fàn? | Lưu Dương đặt cơm thịt kho phải không?
q10.g | gap | — | — | —
q10a | narrator | 是。 | Shì. | Đúng.
q10b | narrator | 是的，刘洋点的是红烧肉饭。 | Shì de, Liú Yáng diǎn de shì hóngshāoròu fàn. | Đúng, Lưu Dương đặt cơm thịt kho.
q11  | narrator | 一共多少钱？ | Yígòng duōshǎo qián? | Tổng cộng bao nhiêu tiền?
q11.g | gap | — | — | —
q11a | narrator | 四十八块。 | Sìshíbā kuài. | Bốn mươi tám tệ.
q11b | narrator | 一共四十八块。 | Yígòng sìshíbā kuài. | Tổng cộng bốn mươi tám tệ.
q12  | narrator | 牛肉面多少钱？ | Niúròu miàn duōshǎo qián? | Mì bò bao nhiêu?
q12.g | gap | — | — | —
q12a | narrator | 二十二块。 | Èrshí'èr kuài. | Hai mươi hai tệ.
q12b | narrator | 牛肉面是二十二块。 | Niúròu miàn shì èrshí'èr kuài. | Mì bò hai mươi hai tệ.
q13  | narrator | 红烧肉饭多少钱？ | Hóngshāoròu fàn duōshǎo qián? | Cơm thịt kho bao nhiêu?
q13.g | gap | — | — | —
q13a | narrator | 二十六块。 | Èrshíliù kuài. | Hai mươi sáu tệ.
q13b | narrator | 红烧肉饭二十六块。 | Hóngshāoròu fàn èrshíliù kuài. | Cơm thịt kho hai mươi sáu tệ.
q14  | narrator | 谁付的钱？ | Shéi fù de qián? | Ai trả tiền?
q14.g | gap | — | — | —
q14a | narrator | 刘洋，在手机上付的。 | Liú Yáng, zài shǒujī shàng fù de. | Lưu Dương, trả trên điện thoại.
q14b | narrator | 钱是刘洋付的，他在手机上付的。 | Qián shì Liú Yáng fù de, tā zài shǒujī shàng fù de. | Tiền là Lưu Dương trả, bạn ấy trả trên điện thoại.
q15  | narrator | 外卖几点到的？ | Wàimài jǐ diǎn dào de? | Đồ ăn tới lúc mấy giờ?
q15.g | gap | — | — | —
q15a | narrator | 十二点。 | Shí'èr diǎn. | Mười hai giờ.
q15b | narrator | 外卖十二点到的。 | Wàimài shí'èr diǎn dào de. | Đồ ăn tới lúc mười hai giờ.
q16  | narrator | 他们等了多久？ | Tāmen děng le duō jiǔ? | Họ đợi bao lâu?
q16.g | gap | — | — | —
q16a | narrator | 三十分钟。 | Sānshí fēnzhōng. | Ba mươi phút.
q16b | narrator | 他们等了三十分钟。 | Tāmen děng le sānshí fēnzhōng. | Họ đợi ba mươi phút.
q17  | narrator | 谁去拿的外卖？ | Shéi qù ná de wàimài? | Ai đi lấy đồ ăn?
q17.g | gap | — | — | —
q17a | narrator | 刘洋去拿的。 | Liú Yáng qù ná de. | Lưu Dương đi lấy.
q17b | narrator | 刘洋自己去拿，外卖小哥没上来。 | Liú Yáng zìjǐ qù ná, wàimài xiǎogē méi shànglai. | Một mình Lưu Dương xuống lấy, anh giao hàng không lên lầu.
q18  | narrator | 第一次来了几个盒子？ | Dì yí cì lái le jǐ ge hézi? | Lần đầu tới mấy hộp?
q18.g | gap | — | — | —
q18a | narrator | 两个。 | Liǎng ge. | Hai hộp.
q18b | narrator | 第一次来了两个盒子。 | Dì yí cì lái le liǎng ge hézi. | Lần đầu tới hai hộp.
q19  | narrator | 刘洋的盒子里是什么？ | Liú Yáng de hézi li shì shénme? | Trong hộp của Lưu Dương là gì?
q19.g | gap | — | — | —
q19a | narrator | 红烧肉饭。 | Hóngshāoròu fàn. | Cơm thịt kho.
q19b | narrator | 刘洋的盒子里是红烧肉饭。 | Liú Yáng de hézi li shì hóngshāoròu fàn. | Hộp của Lưu Dương là cơm thịt kho.
q20  | narrator | 刘洋那份辣不辣？ | Liú Yáng nà fèn là bu là? | Phần của Lưu Dương có cay không?
q20.g | gap | — | — | —
q20a | narrator | 不辣。 | Bú là. | Không cay.
q20b | narrator | 刘洋的红烧肉饭不辣。 | Liú Yáng de hóngshāoròu fàn bú là. | Cơm thịt kho của Lưu Dương không cay.
q21  | narrator | 张静的盒子里是什么？ | Zhāng Jìng de hézi li shì shénme? | Trong hộp của Tĩnh là gì?
q21.g | gap | — | — | —
q21a | narrator | 麻婆豆腐饭。 | Mápó dòufu fàn. | Cơm đậu hũ ma bà.
q21b | narrator | 张静的盒子里是麻婆豆腐饭。 | Zhāng Jìng de hézi li shì mápó dòufu fàn. | Hộp của Tĩnh là cơm đậu hũ ma bà.
q22  | narrator | 张静的盒子里是不是牛肉面？ | Zhāng Jìng de hézi li shì bu shì niúròu miàn? | Hộp của Tĩnh có phải mì bò không?
q22.g | gap | — | — | —
q22a | narrator | 不是。 | Bú shì. | Không phải.
q22b | narrator | 不是牛肉面，是麻婆豆腐饭。 | Bú shì niúròu miàn, shì mápó dòufu fàn. | Không phải mì bò, là cơm đậu hũ ma bà.
q23  | narrator | 那盒菜辣不辣？ | Nà hé cài là bu là? | Hộp đó cay không?
q23.g | gap | — | — | —
q23a | narrator | 很辣。 | Hěn là. | Cay lắm.
q23b | narrator | 那盒菜很辣。 | Nà hé cài hěn là. | Món đó cay lắm.
q24  | narrator | 张静吃了几口？ | Zhāng Jìng chī le jǐ kǒu? | Tĩnh ăn mấy miếng?
q24.g | gap | — | — | —
q24a | narrator | 一口。 | Yì kǒu. | Một miếng.
q24b | narrator | 张静就吃了一口。 | Zhāng Jìng jiù chī le yì kǒu. | Tĩnh ăn có một miếng.
q25  | narrator | 吃完那一口，她要什么？ | Chī wán nà yì kǒu, tā yào shénme? | Ăn xong miếng đó, bạn ấy cần gì?
q25.g | gap | — | — | —
q25a | narrator | 水。 | Shuǐ. | Nước.
q25b | narrator | 她要喝水。 | Tā yào hē shuǐ. | Bạn ấy phải uống nước.
q26  | narrator | 张静吃辣吗？ | Zhāng Jìng chī là ma? | Tĩnh có ăn cay không?
q26.g | gap | — | — | —
q26a | narrator | 不吃，她不吃辣。 | Bú chī, tā bú chī là. | Không, bạn ấy không ăn cay.
q26b | narrator | 张静不吃辣。 | Zhāng Jìng bú chī là. | Tĩnh không ăn cay.
q27  | narrator | 刘洋知不知道她不吃辣？ | Liú Yáng zhī bu zhīdào tā bù chī là? | Lưu Dương có biết bạn ấy không ăn cay không?
q27.g | gap | — | — | —
q27a | narrator | 知道。 | Zhīdào. | Biết chứ.
q27b | narrator | 刘洋知道她不吃辣。 | Liú Yáng zhīdào tā bù chī là. | Lưu Dương biết Tĩnh không ăn cay.
q28  | narrator | 张静有没有再吃那盒饭？ | Zhāng Jìng yǒu méi yǒu zài chī nà hé fàn? | Tĩnh có ăn lại hộp đó không?
q28.g | gap | — | — | —
q28a | narrator | 没有。 | Méi yǒu. | Không.
q28b | narrator | 她没再吃，就吃了那一口。 | Tā méi zài chī, jiù chī le nà yì kǒu. | Bạn ấy không ăn nữa, chỉ ăn đúng một miếng đó.
q29  | narrator | 刘洋吃辣吗？ | Liú Yáng chī là ma? | Lưu Dương có ăn cay không?
q29.g | gap | — | — | —
q29a | narrator | 吃，他喜欢吃辣。 | Chī, tā xǐhuan chī là de. | Có, bạn ấy thích ăn cay.
q29b | narrator | 刘洋喜欢吃辣。 | Liú Yáng xǐhuan chī là. | Lưu Dương thích ăn cay.
q30  | narrator | 订单上写的是不是牛肉面？ | Dìngdān shàng xiě de shì bu shì niúròu miàn? | Trên đơn ghi có phải mì bò không?
q30.g | gap | — | — | —
q30a | narrator | 是。 | Shì. | Đúng.
q30b | narrator | 订单上写的是牛肉面。 | Dìngdān shàng xiě de shì niúròu miàn. | Trên đơn ghi là mì bò.
q31  | narrator | 备注写的是什么？ | Bèizhù xiě de shì shénme? | Phần ghi chú viết gì?
q31.g | gap | — | — | —
q31a | narrator | 不要辣。 | Bú yào là. | Không cho cay.
q31b | narrator | 备注写的是不要辣。 | Bèizhù xiě de shì bú yào là. | Ghi chú viết là không cho cay.
q32  | narrator | 刘洋点错了没有？ | Liú Yáng diǎn cuò le méi yǒu? | Lưu Dương có đặt nhầm không?
q32.g | gap | — | — | —
q32a | narrator | 没有。 | Méi yǒu. | Không.
q32b | narrator | 刘洋点对了，他点的是牛肉面。 | Liú Yáng diǎn duì le, tā diǎn de shì niúròu miàn. | Lưu Dương đặt đúng, bạn ấy đặt mì bò.
q33  | narrator | 谁送错了？ | Shéi sòng cuò le? | Ai giao nhầm vậy?
q33.g | gap | — | — | —
q33a | narrator | 商家送错了。 | Shāngjiā sòng cuò le. | Quán giao nhầm.
q33b | narrator | 是商家送错了。 | Shì shāngjiā sòng cuò le. | Là quán giao nhầm rồi.
q34  | narrator | 第一次有没有送来牛肉面？ | Dì yí cì yǒu méi yǒu sòng lái niúròu miàn? | Lần đầu có mì bò tới không?
q34.g | gap | — | — | —
q34a | narrator | 没有。 | Méi yǒu. | Không.
q34b | narrator | 第一次没有牛肉面，来的是麻婆豆腐饭。 | Dì yí cì méi yǒu niúròu miàn, lái de shì mápó dòufu fàn. | Lần đầu không có mì bò, tới là cơm đậu hũ ma bà.
q35  | narrator | 张静能不能换一份？ | Zhāng Jìng néng bu néng huàn yí fèn? | Tĩnh có đổi được phần khác không?
q35.g | gap | — | — | —
q35a | narrator | 能。 | Néng. | Được.
q35b | narrator | 可以，张静要换一份牛肉面。 | Kěyǐ, Zhāng Jìng yào huàn yí fèn niúròu miàn. | Được, Tĩnh muốn đổi một phần mì bò.
q36  | narrator | 刘洋怎么办？ | Liú Yáng zěnme bàn? | Lưu Dương làm sao?
q36.g | gap | — | — | —
q36a | narrator | 给商家打电话。 | Gěi shāngjiā dǎ diànhuà. | Gọi điện cho quán.
q36b | narrator | 刘洋给商家打了个电话。 | Liú Yáng gěi shāngjiā dǎ le ge diànhuà. | Lưu Dương gọi cho quán một cú.
q37  | narrator | 商家说几分钟送过来？ | Shāngjiā shuō jǐ fēnzhōng sòng guòlai? | Quán nói mấy phút giao tới?
q37.g | gap | — | — | —
q37a | narrator | 十五分钟。 | Shíwǔ fēnzhōng. | Mười lăm phút.
q37b | narrator | 商家说十五分钟送过来。 | Shāngjiā shuō shíwǔ fēnzhōng sòng guòlai. | Quán nói mười lăm phút giao tới.
q38  | narrator | 牛肉面几点到的？ | Niúròu miàn jǐ diǎn dào de? | Mì bò tới lúc mấy giờ?
q38.g | gap | — | — | —
q38a | narrator | 十二点十五。 | Shí'èr diǎn shíwǔ. | Mười hai giờ mười lăm.
q38b | narrator | 牛肉面十二点十五到的。 | Niúròu miàn shí'èr diǎn shíwǔ dào de. | Mì bò tới lúc mười hai giờ mười lăm.
q39  | narrator | 第二次送来了几个盒子？ | Dì èr cì sòng lái le jǐ ge hézi? | Lần hai giao tới mấy hộp?
q39.g | gap | — | — | —
q39a | narrator | 一个。 | Yí ge. | Một hộp.
q39b | narrator | 第二次商家送来一个盒子。 | Dì èr cì shāngjiā sòng lái yí ge hézi. | Lần hai quán giao tới một hộp.
q40  | narrator | 这次还要不要付钱？ | Zhè cì hái yào bu yào fù qián? | Lần này có phải trả tiền không?
q40.g | gap | — | — | —
q40a | narrator | 不用。 | Bú yòng. | Khỏi.
q40b | narrator | 这次不用再付钱，是商家重新送的。 | Zhè cì bú yòng zài fù qián, shì shāngjiā chóngxīn sòng de. | Lần này không tốn thêm tiền, quán giao lại mà.
q41  | narrator | 张静饿不饿？ | Zhāng Jìng è bu è? | Tĩnh có đói không?
q41.g | gap | — | — | —
q41a | narrator | 饿。 | È. | Đói chứ.
q41b | narrator | 张静饿死了，她等了十五分钟。 | Zhāng Jìng è sǐ le, tā děng le shíwǔ fēnzhōng. | Tĩnh đói chết mất, bạn ấy đợi những mười lăm phút.
q42  | narrator | 最后张静吃什么？ | Zuìhòu Zhāng Jìng chī shénme? | Cuối cùng Tĩnh ăn gì?
q42.g | gap | — | — | —
q42a | narrator | 牛肉面。 | Niúròu miàn. | Mì bò.
q42b | narrator | 张静终于吃上了牛肉面。 | Zhāng Jìng zhōngyú chī shàng le niúròu miàn. | Cuối cùng Tĩnh cũng được ăn mì bò.
q43  | narrator | 牛肉面好吃吗？ | Niúròu miàn hǎochī ma? | Mì bò có ngon không?
q43.g | gap | — | — | —
q43a | narrator | 好吃。 | Hǎochī. | Ngon.
q43b | narrator | 好吃，味道不错。 | Hǎochī, wèidào búcuò. | Ngon, vị khá ổn.
q44  | narrator | 张静说要转钱给谁？ | Zhāng Jìng shuō yào zhuǎn qián gěi shéi? | Tĩnh nói chuyển tiền cho ai?
q44.g | gap | — | — | —
q44a | narrator | 刘洋。 | Liú Yáng. | Lưu Dương.
q44b | narrator | 张静说她的面二十二块，她要转给刘洋。 | Zhāng Jìng shuō tā de miàn èrshí'èr kuài, tā yào zhuǎn gěi Liú Yáng. | Tĩnh nói phần mì hai mươi hai tệ, bạn ấy muốn chuyển cho Lưu Dương.
q45  | narrator | 刘洋怎么说？ | Liú Yáng zěnme shuō? | Lưu Dương nói sao?
q45.g | gap | — | — | —
q45a | narrator | 今天我请你，别客气。 | Jīntiān wǒ qǐng nǐ, bié kèqi. | Hôm nay mình mời, đừng khách sáo.
q45b | narrator | 刘洋说，今天我请你，别客气。 | Liú Yáng shuō, jīntiān wǒ qǐng nǐ, bié kèqi. | Lưu Dương nói hôm nay mình mời, đừng khách sáo.
q46  | narrator | 张静最后付钱了吗？ | Zhāng Jìng zuìhòu fù qián le ma? | Rốt cuộc Tĩnh có trả tiền không?
q46.g | gap | — | — | —
q46a | narrator | 不付，刘洋请她。 | Bú fù, Liú Yáng qǐng tā. | Không trả, Lưu Dương mời bạn ấy.
q46b | narrator | 张静没付钱，今天刘洋请她。 | Zhāng Jìng méi fù qián, jīntiān Liú Yáng qǐng tā. | Tĩnh không trả tiền, hôm nay Lưu Dương mời.
q47  | narrator | 最后刘洋吃什么？ | Zuìhòu Liú Yáng chī shénme? | Cuối cùng Lưu Dương ăn gì?
q47.g | gap | — | — | —
q47a | narrator | 红烧肉饭和麻婆豆腐饭。 | Hóngshāoròu fàn hé mápó dòufu fàn. | Cơm thịt kho và cơm đậu hũ ma bà.
q47b | narrator | 刘洋吃完红烧肉饭，又吃了那盒麻婆豆腐饭。 | Liú Yáng chī wán hóngshāoròu fàn, yòu chī le nà hé mápó dòufu fàn. | Lưu Dương ăn xong cơm thịt kho, rồi ăn luôn hộp đậu hũ ma bà.
q48  | narrator | 那盒辣菜最后是谁吃的？ | Nà hé là cài zuìhòu shì shéi chī de? | Hộp cay đó rốt cuộc ai ăn?
q48.g | gap | — | — | —
q48a | narrator | 刘洋吃的。 | Liú Yáng chī de. | Lưu Dương ăn.
q48b | narrator | 那盒辣菜是刘洋吃的，他喜欢吃辣。 | Nà hé là cài shì Liú Yáng chī de, tā xǐhuan chī là. | Hộp đó Lưu Dương ăn, bạn ấy thích ăn cay mà.
```

## Open doubts

1. **Opener wording vs spec rule 6.** The mode has no tier word for "phản xạ"; I opened with
   我来问，你来回答。+ 听问题，马上回答，不用想很久。(naming the activity with Coach words 问 /
   回答 / 听). If the PM wants the fixed line verbatim, replace `q0` with 你回答。 and keep `q0b`.
2. **New everyday words the drill needs** — 问题, 关系, 时候, 多久, 马上, 自己 (s21), 完 (s35),
   终于 / 味道 (s30), 最后 and 怎么办 (both in `04b`'s canonical answers), 哪儿, 知不知道. None
   is a content word of the story; all are simple spoken items. Belong in `03_core.md` §2.5
   Function (like 问 / 回答)? Step 10 to add or strip.
3. **Canonical answers, punctuation only.** `04b` writes them with half-width commas
   (不吃,她不吃辣); spec rule 3 forces full-width, so the Chinese is 不吃，她不吃辣。 Word-for-word
   otherwise unchanged, including 刘洋，在手机上付的。 and 十五分钟。 (I dropped 04b's parenthetical
   "(十二点十五)" — `q38` already asks that).
4. **`q32` states the false belief as a question** (刘洋点错了没有？→ 没有，刘洋点对了). F12 says
   刘洋点错了 is never a fact; this is the correction beat and matches F8's wording 点对了. If the
   PM would rather not hear 点错了 at all, `q32` → 刘洋点对了没有？ — same answer, weaker drill.
5. **`q23` / `q48` use 那盒… / 辣菜**, a compress of s35's 那盒麻婆豆腐饭 and s15's name. 菜 gets
   its first extra hearings here (story 1). Confirm the short form is allowed in Phản xạ (V4 is
   Nghe-only, so 麻婆豆腐饭 is always spelled out in the answer rows; the short form appears only
   in the question, after `q21`/`q23b` have given the full name).
6. **`q47` and `q48` overlap** (both about 刘洋 eating the spicy box, F6/F15). Kept because the
   second one drills 谁…吃的的 cleft form; drop `q48` if the PM wants exactly 47 questions.
7. **Exposure lift (story open doubt 3).** This file gives second+ hearings to 午饭 菜 米饭 水 喝
   拿 打开 备注 外卖小哥 手机 公司 同事 饿 好吃 等 请 转 换 打电话 and to Coach 听 问 回答;
   麻烦 is the one story-singleton left untouched — a "what did 张静 say to be polite" question read
   as a memory test, not a reflex. Step 10 counts.
8. Pinyin convention still open (`03_core` doubt 1): A-not-A written with reduced 不 (`shì bu shì`
   q10/q22/q30, `là bu là` q20/q23, `zhī bu zhīdào` q27, `hái yào bu yào` q40), statements with
   bú shì / bú là / bú yòng, per story s14/s15/s29.
