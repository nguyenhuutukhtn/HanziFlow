# 09 — Five modes (lesson 3: 午饭点错菜)

Joined by the PM script from the per-mode files (09_<mode>.md, authored by Qwen); spec: 09_spec.md.

## Hiểu


Lesson 3 · 午饭点错菜 · Author AI A (Qwen), step 9 (mode: Hiểu).
Reads: `09_spec.md` (Rules for all modes + Hiểu), `05_story.md` (script, copied verbatim),
`04b_facts.md` (F1–F19), `03_core.md` §1–2 (P1–P15, tiers).

Refs: `h1`–`h4` intro · `hs…` story line + `hs…v` Vietnamese gloss · `hp1`–`hp15` phrase gloss
(`hp…v` = Vietnamese meaning, `hp…s` = story line that uses it) · `hr0` + `hr…` story once more,
Chinese only. Narrator rows use the token `narrator` (`modes.py` maps it to `coach_zh`).

```
h1  | coach_vi   | — | — | Bây giờ mình nghe hiểu câu chuyện nha: hai đồng nghiệp gọi đồ ăn trưa, quán giao lộn món.
h2  | coach_vi   | — | — | Mình nghe từng câu tiếng Trung, sau mỗi câu có phần giải thích bằng tiếng Việt.
h3  | coach_vi   | — | — | Bạn để ý ba chuyện: người ta hỏi và gọi món thế nào, câu "giao nhầm" nói ra sao, với hai người trả tiền cho nhau kiểu gì.
h4  | narrator   | 我们听故事。 | Wǒmen tīng gùshi. | Mình nghe câu chuyện.
hs1   | narrator   | 张静和刘洋是同一家公司的同事。 | Zhāng Jìng hé Liú Yáng shì tóng yì jiā gōngsī de tóngshì. | Tĩnh và Lưu Dương là đồng nghiệp ở cùng một công ty.
hs1v  | coach_vi   | — | — | Hai người này làm chung một công ty, gọi nhau là "mình" với "bạn".
hs1b  | narrator   | 现在十一点半。 | Xiànzài shíyī diǎn bàn. | Bây giờ là mười một rưỡi.
hs1bv | coach_vi   | — | — | Mười một rưỡi, sắp tới bữa trưa rồi — mấy phút nữa là gọi đồ ăn.
hs2   | liu_yang   | 我来点外卖吧，午饭你想吃什么？ | Wǒ lái diǎn wàimài ba, wǔfàn nǐ xiǎng chī shénme? | Để mình đặt đồ ăn nhé, trưa bạn muốn ăn gì?
hs2v  | coach_vi   | — | — | Lưu Dương xung phong đặt đồ ăn, rồi hỏi trưa nay Tĩnh muốn ăn gì. "Ngoại mại" là đồ ăn đặt người ta giao tới.
hs3   | zhang_jing | 随便，你点吧，麻烦你了。 | Suíbiàn, nǐ diǎn ba, máfan nǐ le. | Gì cũng được, bạn đặt đi, phiền bạn rồi.
hs3v  | coach_vi   | — | — | Cô ấy nói gì cũng được, bạn muốn gọi gì thì gọi, rồi cảm ơn một câu — "ma phiền" là phiền bạn rồi, người ta hay nói khi nhờ vả.
hs4   | liu_yang   | 你吃米饭还是吃面？ | Nǐ chī mǐfàn háishi chī miàn? | Bạn ăn cơm hay ăn mì?
hs4v  | coach_vi   | — | — | Anh ấy đưa ra hai lựa chọn: cơm hay mì. Chữ ở giữa hai lựa chọn là "hoàn thị".
hs5   | zhang_jing | 我今天想吃面。 | Wǒ jīntiān xiǎng chī miàn. | Hôm nay mình muốn ăn mì.
hs5v  | coach_vi   | — | — | Cô ấy chọn mì. Nói là hôm nay muốn ăn mì, không phải lúc nào cũng vậy.
hs6   | liu_yang   | 要不要来一份牛肉面？ | Yào bu yào lái yí fèn niúròu miàn? | Có muốn một phần mì bò không?
hs6v  | coach_vi   | — | — | Hỏi có muốn một phần mì bò không. "Mì bò" là món mì với thịt bò, Hán-Việt là "ngưu nhục".
hs7   | zhang_jing | 好啊，我要牛肉面。 | Hǎo a, wǒ yào niúròu miàn. | Được đó, mình gọi mì bò.
hs7v  | coach_vi   | — | — | Đồng ý luôn: mình lấy mì bò. Hai chữ đầu là kiểu đáp rất cửa miệng, gần như "được đó".
hs8   | liu_yang   | 那我点红烧肉饭，二十六块。 | Nà wǒ diǎn hóngshāoròu fàn, èrshíliù kuài. | Vậy mình đặt cơm thịt kho, hai mươi sáu tệ.
hs8v  | coach_vi   | — | — | Còn anh ấy lấy cơm thịt kho, hai mươi sáu tệ. "Khối" là cách nói thường ngày của "tệ" (đồng nhân dân tệ).
hs9   | zhang_jing | 一共多少钱？ | Yígòng duōshǎo qián? | Tổng cộng bao nhiêu tiền?
hs9v  | coach_vi   | — | — | Tĩnh hỏi tổng cộng bao nhiêu tiền — câu này dùng được mỗi khi mua chung.
hs10  | liu_yang   | 一共四十八块，我已经用手机付了。 | Yígòng sìshíbā kuài, wǒ yǐjīng yòng shǒujī fù le. | Tổng cộng bốn mươi tám tệ, mình đã trả bằng điện thoại rồi.
hs10v | coach_vi   | — | — | Bốn mươi tám tệ. Anh ấy trả bằng điện thoại xong rồi, không đưa tiền mặt, không quét mã.
hs11  | narrator   | 三十分钟以后，十二点了。 | Sānshí fēnzhōng yǐhòu, shí'èr diǎn le. | Ba mươi phút sau, đã mười hai giờ.
hs11v | coach_vi   | — | — | Ba mươi phút sau, đã mười hai giờ trưa. Chờ lâu đó.
hs12  | liu_yang   | 外卖到了，我去拿。 | Wàimài dào le, wǒ qù ná. | Đồ ăn tới rồi, mình đi lấy đây.
hs12v | coach_vi   | — | — | Đồ ăn tới rồi, anh ấy nói để anh đi lấy. Người giao không lên tận phòng.
hs13  | narrator   | 刘洋拿回来两个盒子，他们打开一看。 | Liú Yáng ná huílái liǎng ge hézi, tāmen dǎkāi yí kàn. | Lưu Dương mang hai cái hộp về, hai người mở ra xem.
hs13v | coach_vi   | — | — | Anh ấy mang về hai cái hộp, hai người mở ra xem — cùng một quán giao tới.
hs14  | liu_yang   | 我的是红烧肉饭，不辣。 | Wǒ de shì hóngshāoròu fàn, bú là. | Hộp của mình là cơm thịt kho, không cay.
hs14v | coach_vi   | — | — | Hộp của anh là cơm thịt kho, không cay — đúng phần anh gọi.
hs15  | zhang_jing | 我这个是麻婆豆腐饭，不是牛肉面。 | Wǒ zhège shì mápó dòufu fàn, bú shì niúròu miàn. | Hộp của mình là cơm đậu hũ ma bà, không phải mì bò.
hs15v | coach_vi   | — | — | Còn hộp của cô là cơm đậu hũ ma bà, một món cay của Tứ Xuyên — không phải mì bò. Nhầm rồi!
hs16  | narrator   | 张静吃了一口。 | Zhāng Jìng chī le yì kǒu. | Tĩnh ăn một miếng.
hs16v | coach_vi   | — | — | Tĩnh ăn thử một miếng thôi — "nhất khẩu" là một miếng, một miếng cắn.
hs17  | zhang_jing | 这个菜太辣了！我要喝水。 | Zhège cài tài là le! Wǒ yào hē shuǐ. | Món này cay quá! Mình phải uống nước.
hs17v | coach_vi   | — | — | Cay quá trời, cô ấy đòi uống nước. "Thái" ở đây là quá mức, than một cái là xong.
hs18  | liu_yang   | 有那么辣吗？ | Yǒu nàme là ma? | Cay đến vậy luôn hả?
hs18v | coach_vi   | — | — | Lưu Dương không tin: cay cỡ đó luôn hả?
hs19  | zhang_jing | 我不吃辣，你知道的。 | Wǒ bù chī là, nǐ zhīdào de. | Mình không ăn cay, bạn biết mà.
hs19v | coach_vi   | — | — | Cô ấy nhắc: mình đâu có ăn cay, bạn biết rồi còn gì. Đây là thói quen lâu nay, không phải hôm nay mới vậy.
hs20  | zhang_jing | 刘洋，你点的是什么？ | Liú Yáng, nǐ diǎn de shì shénme? | Lưu Dương, bạn đặt món gì vậy?
hs20v | coach_vi   | — | — | Rồi cô ấy gọi tên anh ấy, hỏi lại: bạn đặt món gì vậy? Lúc này cô ấy nghĩ là anh ấy gọi lầm.
hs21  | liu_yang   | 我给你点的是牛肉面，我自己要红烧肉饭。 | Wǒ gěi nǐ diǎn de shì niúròu miàn, wǒ zìjǐ yào hóngshāoròu fàn. | Mình đặt mì bò cho bạn, còn mình lấy cơm thịt kho.
hs21v | coach_vi   | — | — | Anh ấy nói: mì bò là gọi cho bạn, còn phần của mình là cơm thịt kho — anh ấy đâu có gọi món cay.
hs22  | liu_yang   | 可能送错了，我看看订单。 | Kěnéng sòng cuò le, wǒ kànkan dìngdān. | Có lẽ giao nhầm rồi, để mình xem đơn hàng.
hs22v | coach_vi   | — | — | Anh ấy đoán là giao nhầm rồi, để xem lại đơn hàng. Hai chữ cuối là "đính đơn", tức là cái đơn mình đặt.
hs23  | liu_yang   | 订单上写的是牛肉面，备注写的是“不要辣”。 | Dìngdān shàng xiě de shì niúròu miàn, bèizhù xiě de shì “bú yào là”. | Trên đơn ghi là mì bò, ghi chú là “không cho cay”.
hs23v | coach_vi   | — | — | Trên đơn ghi đúng là mì bò, phần ghi chú còn ghi không cho cay — tức là Lưu Dương gọi đúng, không sai chỗ nào.
hs23b | liu_yang   | 是商家送错了。 | Shì shāngjiā sòng cuò le. | Là quán giao nhầm rồi.
hs23bv | coach_vi  | — | — | Vậy là tại quán giao nhầm. "Thương gia" là cái quán bán trên ứng dụng.
hs24  | zhang_jing | 那我的面呢？能不能换一份？ | Nà wǒ de miàn ne? Néng bu néng huàn yí fèn? | Vậy mì của mình đâu? Đổi một phần khác được không?
hs24v | coach_vi   | — | — | Vậy mì của mình đâu? Đổi một phần khác được không? "Hoán" là đổi — đây là câu bạn cần khi nhận sai món.
hs25  | liu_yang   | 可以，我给商家打个电话。 | Kěyǐ, wǒ gěi shāngjiā dǎ ge diànhuà. | Được, để mình gọi cho quán.
hs25v | coach_vi   | — | — | Được, để anh gọi cho quán một cú. Anh ấy chỉ gọi có một cuộc điện thoại thôi.
hs26  | liu_yang   | 商家说，十五分钟后送过来，你等一下。 | Shāngjiā shuō, shíwǔ fēnzhōng hòu sòng guòlai, nǐ děng yíxià. | Quán nói mười lăm phút nữa sẽ giao tới, bạn đợi một chút.
hs26v | coach_vi   | — | — | Quán nói mười lăm phút nữa giao qua, bảo cô ấy đợi chút. Phần này là anh ấy thuật lại lời quán, quán không tự nói.
hs27  | narrator   | 十二点十五，商家又送来一个盒子。 | Shí'èr diǎn shíwǔ, shāngjiā yòu sòng lái yí ge hézi. | Mười hai giờ mười lăm, quán lại giao tới một cái hộp.
hs27v | coach_vi   | — | — | Mười hai giờ mười lăm, quán giao thêm một hộp nữa — lần này chỉ có một hộp.
hs28  | zhang_jing | 是牛肉面！太好了，我饿死了！ | Shì niúròu miàn! Tài hǎo le, wǒ è sǐ le! | Là mì bò! Tuyệt quá, mình đói chết mất!
hs28v | coach_vi   | — | — | Đúng mì bò rồi! Cô ấy mừng, nói đói muốn chết luôn.
hs29  | liu_yang   | 这次不用再付钱，是商家重新送的。 | Zhè cì bú yòng zài fù qián, shì shāngjiā chóngxīn sòng de. | Lần này không cần trả thêm tiền, quán giao lại mà.
hs29v | coach_vi   | — | — | Lần này không phải trả thêm tiền — quán giao lại mà, tại họ làm sai.
hs30  | narrator   | 张静终于吃上了牛肉面，味道不错。 | Zhāng Jìng zhōngyú chī shàng le niúròu miàn, wèidào búcuò. | Cuối cùng Tĩnh cũng được ăn mì bò, vị khá ngon.
hs30v | coach_vi   | — | — | Cuối cùng Tĩnh cũng được ăn mì bò. Vị khá ngon — cô ấy không than gì thêm.
hs31  | zhang_jing | 我原来的面二十二块，我转你。 | Wǒ yuánlái de miàn èrshí'èr kuài, wǒ zhuǎn nǐ. | Phần mì ban đầu của mình hai mươi hai tệ, mình chuyển cho bạn.
hs31v | coach_vi   | — | — | Cô ấy nói phần mì của mình hai mươi hai tệ, để chuyển tiền cho bạn. Hai người này chuyển tiền qua điện thoại, không đưa tiền mặt.
hs32  | liu_yang   | 今天我请你，别客气。 | Jīntiān wǒ qǐng nǐ, bié kèqi. | Hôm nay mình mời, đừng khách sáo.
hs32v | coach_vi   | — | — | Anh ấy gạt đi: hôm nay mình mời, đừng khách sáo. "Thỉnh" ở đây là mời ăn, tức là anh ấy trả tiền luôn. Tĩnh không phải trả đồng nào.
hs33  | zhang_jing | 那个麻婆豆腐饭很辣，你吃吗？ | Nàge mápó dòufu fàn hěn là, nǐ chī ma? | Hộp cơm đậu hũ ma bà kia cay lắm, bạn có ăn không?
hs33v | coach_vi   | — | — | Cô ấy hỏi: hộp đậu hũ cay kìa, bạn có ăn không? Cay đó, cô ấy ăn không được.
hs34  | liu_yang   | 吃啊，我最喜欢吃辣的。 | Chī a, wǒ zuì xǐhuan chī là de. | Có chứ, mình thích ăn cay nhất.
hs34v | coach_vi   | — | — | Anh ấy ăn chứ — anh thích ăn cay nhất, nên hộp đó không bỏ đi.
hs35  | narrator   | 刘洋吃完红烧肉饭，又吃了那盒麻婆豆腐饭。 | Liú Yáng chī wán hóngshāoròu fàn, yòu chī le nà hé mápó dòufu fàn. | Lưu Dương ăn xong cơm thịt kho, rồi lại ăn hộp cơm đậu hũ ma bà kia.
hs35v | coach_vi   | — | — | Lưu Dương ăn xong cơm thịt kho, rồi ăn luôn hộp đậu hũ cay, không bỏ phí.
hp1   | narrator   | 你想吃什么？ | Nǐ xiǎng chī shénme? | Bạn muốn ăn gì?
hp1v  | coach_vi   | — | — | Câu hỏi quen nhất khi đi ăn: bạn muốn ăn gì? Đổi "bạn" thành "mình" là thành câu trả lời.
hp1s  | narrator   | 我来点外卖吧，午饭你想吃什么？ | Wǒ lái diǎn wàimài ba, wǔfàn nǐ xiǎng chī shénme? | Để mình đặt đồ ăn nhé, trưa bạn muốn ăn gì?
hp2   | narrator   | 我来点外卖吧。 | Wǒ lái diǎn wàimài ba. | Để mình đặt đồ ăn giao tới nhé.
hp2v  | coach_vi   | — | — | "Ngoại mại" là đồ ăn đặt người ta giao tới. Câu này để giành phần việc về mình: để tôi đặt cho.
hp2s  | narrator   | 我来点外卖吧，午饭你想吃什么？ | Wǒ lái diǎn wàimài ba, wǔfàn nǐ xiǎng chī shénme? | Để mình đặt đồ ăn nhé, trưa bạn muốn ăn gì?
hp3   | narrator   | 随便，你点吧。 | Suíbiàn, nǐ diǎn ba. | Gì cũng được, bạn đặt đi.
hp3v  | coach_vi   | — | — | "Tùy tiện" — cái gì cũng được. Người Trung hay trả lời vậy cho nhanh, rồi nhường người kia chọn.
hp3s  | narrator   | 随便，你点吧，麻烦你了。 | Suíbiàn, nǐ diǎn ba, máfan nǐ le. | Gì cũng được, bạn đặt đi, phiền bạn rồi.
hp4   | narrator   | 要不要来一份牛肉面？ | Yào bu yào lái yí fèn niúròu miàn? | Có muốn một phần mì bò không?
hp4v  | coach_vi   | — | — | Có muốn ... không? Ghép thêm món vào sau là mời được ngay. Muốn hỏi lịch sự hơn thì thêm "một phần" ở giữa.
hp4s  | narrator   | 要不要来一份牛肉面？ | Yào bu yào lái yí fèn niúròu miàn? | Có muốn một phần mì bò không?
hp5   | narrator   | 米饭还是面？ | Mǐfàn háishi miàn? | Cơm hay là mì?
hp5v  | coach_vi   | — | — | Dùng khi đưa ra hai lựa chọn: cái này hay là cái kia. Nói tắt được, không cần động từ cũng hiểu.
hp5s  | narrator   | 你吃米饭还是吃面？ | Nǐ chī mǐfàn háishi chī miàn? | Bạn ăn cơm hay ăn mì?
hp6   | narrator   | 外卖到了。 | Wàimài dào le. | Đồ ăn tới rồi.
hp6v  | coach_vi   | — | — | Đồ ăn tới rồi — câu để báo cho cả phòng biết. Thêm "tôi đi lấy" là xong một lượt.
hp6s  | narrator   | 外卖到了，我去拿。 | Wàimài dào le, wǒ qù ná. | Đồ ăn tới rồi, mình đi lấy đây.
hp7   | narrator   | 你点的是什么？ | Nǐ diǎn de shì shénme? | Bạn đặt món gì?
hp7v  | coach_vi   | — | — | Hỏi lại xem người ta gọi món nào. Thêm tên người vào đầu câu thì tự nhiên hơn.
hp7s  | narrator   | 刘洋，你点的是什么？ | Liú Yáng, nǐ diǎn de shì shénme? | Lưu Dương, bạn đặt món gì vậy?
hp8   | narrator   | 我点的是牛肉面。 | Wǒ diǎn de shì niúròu miàn. | Mình đặt mì bò.
hp8v  | coach_vi   | — | — | Trả lời: mình gọi là món đó. Cái phần đứng trước "là" chính là thứ mình đặt.
hp8s  | narrator   | 我给你点的是牛肉面，我自己要红烧肉饭。 | Wǒ gěi nǐ diǎn de shì niúròu miàn, wǒ zìjǐ yào hóngshāoròu fàn. | Mình đặt mì bò cho bạn, còn mình lấy cơm thịt kho.
hp9   | narrator   | 送错了。 | Sòng cuò le. | Giao nhầm rồi.
hp9v  | coach_vi   | — | — | Giao nhầm rồi — Hán-Việt là "tống thác": tống là giao, thác là sai. Nói về món, về hàng, về tiền đều dùng được.
hp9s  | narrator   | 可能送错了，我看看订单。 | Kěnéng sòng cuò le, wǒ kànkan dìngdān. | Có lẽ giao nhầm rồi, để mình xem đơn hàng.
hp10  | narrator   | 太辣了！ | Tài là le! | Cay quá!
hp10v | coach_vi   | — | — | Cay quá! "Thái" là quá mức, ghép sau tính từ là thành câu than: cay quá, nhiều quá, đắt quá.
hp10s | narrator   | 这个菜太辣了！我要喝水。 | Zhège cài tài là le! Wǒ yào hē shuǐ. | Món này cay quá! Mình phải uống nước.
hp11  | narrator   | 我不吃辣。 | Wǒ bù chī là. | Mình không ăn cay.
hp11v | coach_vi   | — | — | Mình không ăn cay — nói thói quen của mình, không phải chuyện hôm nay. Muốn nhắc người khác thì thêm "bạn biết mà".
hp11s | narrator   | 我不吃辣，你知道的。 | Wǒ bù chī là, nǐ zhīdào de. | Mình không ăn cay, bạn biết mà.
hp12  | narrator   | 换一份吧。 | Huàn yí fèn ba. | Đổi một phần khác đi.
hp12v | coach_vi   | — | — | "Hoán" là đổi. Hỏi cho lịch sự thì nói: có đổi được không? — thêm "năng bu năng" phía trước.
hp12s | narrator   | 那我的面呢？能不能换一份？ | Nà wǒ de miàn ne? Néng bu néng huàn yí fèn? | Vậy mì của mình đâu? Đổi một phần khác được không?
hp13  | narrator   | 我给商家打电话。 | Wǒ gěi shāngjiā dǎ diànhuà. | Mình gọi cho quán đây.
hp13v | coach_vi   | — | — | "Thương gia" là quán bán trên ứng dụng. Nói gọi cho quán, người ta hiểu là gọi điện chứ không phải tới nơi.
hp13s | narrator   | 可以，我给商家打个电话。 | Kěyǐ, wǒ gěi shāngjiā dǎ ge diànhuà. | Được, để mình gọi cho quán.
hp14  | narrator   | 一共多少钱？ | Yígòng duōshǎo qián? | Tổng cộng bao nhiêu tiền?
hp14v | coach_vi   | — | — | Hỏi tổng cộng bao nhiêu tiền — dùng khi hai người ăn chung, chia tiền. Hán-Việt là "nhất cộng đa thiểu tiền".
hp14s | narrator   | 一共四十八块，我已经用手机付了。 | Yígòng sìshíbā kuài, wǒ yǐjīng yòng shǒujī fù le. | Tổng cộng bốn mươi tám tệ, mình đã trả bằng điện thoại rồi.
hp15  | narrator   | 今天我请你。 | Jīntiān wǒ qǐng nǐ. | Hôm nay mình mời.
hp15v | coach_vi   | — | — | "Thỉnh" là mời — nói vậy là mình trả tiền cho cả hai. Người kia hay gạt đi một câu: đừng khách sáo.
hp15s | narrator   | 今天我请你，别客气。 | Jīntiān wǒ qǐng nǐ, bié kèqi. | Hôm nay mình mời, đừng khách sáo.
hr0 | narrator | 再听一遍。 | Zài tīng yí biàn. | Nghe lại một lần nữa.
hr1   | narrator   | 张静和刘洋是同一家公司的同事。 | Zhāng Jìng hé Liú Yáng shì tóng yì jiā gōngsī de tóngshì. | Tĩnh và Lưu Dương là đồng nghiệp ở cùng một công ty.
hr1b  | narrator   | 现在十一点半。 | Xiànzài shíyī diǎn bàn. | Bây giờ là mười một rưỡi.
hr2   | liu_yang   | 我来点外卖吧，午饭你想吃什么？ | Wǒ lái diǎn wàimài ba, wǔfàn nǐ xiǎng chī shénme? | Để mình đặt đồ ăn nhé, trưa bạn muốn ăn gì?
hr3   | zhang_jing | 随便，你点吧，麻烦你了。 | Suíbiàn, nǐ diǎn ba, máfan nǐ le. | Gì cũng được, bạn đặt đi, phiền bạn rồi.
hr4   | liu_yang   | 你吃米饭还是吃面？ | Nǐ chī mǐfàn háishi chī miàn? | Bạn ăn cơm hay ăn mì?
hr5   | zhang_jing | 我今天想吃面。 | Wǒ jīntiān xiǎng chī miàn. | Hôm nay mình muốn ăn mì.
hr6   | liu_yang   | 要不要来一份牛肉面？ | Yào bu yào lái yí fèn niúròu miàn? | Có muốn một phần mì bò không?
hr7   | zhang_jing | 好啊，我要牛肉面。 | Hǎo a, wǒ yào niúròu miàn. | Được đó, mình gọi mì bò.
hr8   | liu_yang   | 那我点红烧肉饭，二十六块。 | Nà wǒ diǎn hóngshāoròu fàn, èrshíliù kuài. | Vậy mình đặt cơm thịt kho, hai mươi sáu tệ.
hr9   | zhang_jing | 一共多少钱？ | Yígòng duōshǎo qián? | Tổng cộng bao nhiêu tiền?
hr10  | liu_yang   | 一共四十八块，我已经用手机付了。 | Yígòng sìshíbā kuài, wǒ yǐjīng yòng shǒujī fù le. | Tổng cộng bốn mươi tám tệ, mình đã trả bằng điện thoại rồi.
hr11  | narrator   | 三十分钟以后，十二点了。 | Sānshí fēnzhōng yǐhòu, shí'èr diǎn le. | Ba mươi phút sau, đã mười hai giờ.
hr12  | liu_yang   | 外卖到了，我去拿。 | Wàimài dào le, wǒ qù ná. | Đồ ăn tới rồi, mình đi lấy đây.
hr13  | narrator   | 刘洋拿回来两个盒子，他们打开一看。 | Liú Yáng ná huílái liǎng ge hézi, tāmen dǎkāi yí kàn. | Lưu Dương mang hai cái hộp về, hai người mở ra xem.
hr14  | liu_yang   | 我的是红烧肉饭，不辣。 | Wǒ de shì hóngshāoròu fàn, bú là. | Hộp của mình là cơm thịt kho, không cay.
hr15  | zhang_jing | 我这个是麻婆豆腐饭，不是牛肉面。 | Wǒ zhège shì mápó dòufu fàn, bú shì niúròu miàn. | Hộp của mình là cơm đậu hũ ma bà, không phải mì bò.
hr16  | narrator   | 张静吃了一口。 | Zhāng Jìng chī le yì kǒu. | Tĩnh ăn một miếng.
hr17  | zhang_jing | 这个菜太辣了！我要喝水。 | Zhège cài tài là le! Wǒ yào hē shuǐ. | Món này cay quá! Mình phải uống nước.
hr18  | liu_yang   | 有那么辣吗？ | Yǒu nàme là ma? | Cay đến vậy luôn hả?
hr19  | zhang_jing | 我不吃辣，你知道的。 | Wǒ bù chī là, nǐ zhīdào de. | Mình không ăn cay, bạn biết mà.
hr20  | zhang_jing | 刘洋，你点的是什么？ | Liú Yáng, nǐ diǎn de shì shénme? | Lưu Dương, bạn đặt món gì vậy?
hr21  | liu_yang   | 我给你点的是牛肉面，我自己要红烧肉饭。 | Wǒ gěi nǐ diǎn de shì niúròu miàn, wǒ zìjǐ yào hóngshāoròu fàn. | Mình đặt mì bò cho bạn, còn mình lấy cơm thịt kho.
hr22  | liu_yang   | 可能送错了，我看看订单。 | Kěnéng sòng cuò le, wǒ kànkan dìngdān. | Có lẽ giao nhầm rồi, để mình xem đơn hàng.
hr23  | liu_yang   | 订单上写的是牛肉面，备注写的是“不要辣”。 | Dìngdān shàng xiě de shì niúròu miàn, bèizhù xiě de shì “bú yào là”. | Trên đơn ghi là mì bò, ghi chú là “không cho cay”.
hr23b | liu_yang   | 是商家送错了。 | Shì shāngjiā sòng cuò le. | Là quán giao nhầm rồi.
hr24  | zhang_jing | 那我的面呢？能不能换一份？ | Nà wǒ de miàn ne? Néng bu néng huàn yí fèn? | Vậy mì của mình đâu? Đổi một phần khác được không?
hr25  | liu_yang   | 可以，我给商家打个电话。 | Kěyǐ, wǒ gěi shāngjiā dǎ ge diànhuà. | Được, để mình gọi cho quán.
hr26  | liu_yang   | 商家说，十五分钟后送过来，你等一下。 | Shāngjiā shuō, shíwǔ fēnzhōng hòu sòng guòlai, nǐ děng yíxià. | Quán nói mười lăm phút nữa sẽ giao tới, bạn đợi một chút.
hr27  | narrator   | 十二点十五，商家又送来一个盒子。 | Shí'èr diǎn shíwǔ, shāngjiā yòu sòng lái yí ge hézi. | Mười hai giờ mười lăm, quán lại giao tới một cái hộp.
hr28  | zhang_jing | 是牛肉面！太好了，我饿死了！ | Shì niúròu miàn! Tài hǎo le, wǒ è sǐ le! | Là mì bò! Tuyệt quá, mình đói chết mất!
hr29  | liu_yang   | 这次不用再付钱，是商家重新送的。 | Zhè cì bú yòng zài fù qián, shì shāngjiā chóngxīn sòng de. | Lần này không cần trả thêm tiền, quán giao lại mà.
hr30  | narrator   | 张静终于吃上了牛肉面，味道不错。 | Zhāng Jìng zhōngyú chī shàng le niúròu miàn, wèidào búcuò. | Cuối cùng Tĩnh cũng được ăn mì bò, vị khá ngon.
hr31  | zhang_jing | 我原来的面二十二块，我转你。 | Wǒ yuánlái de miàn èrshí'èr kuài, wǒ zhuǎn nǐ. | Phần mì ban đầu của mình hai mươi hai tệ, mình chuyển cho bạn.
hr32  | liu_yang   | 今天我请你，别客气。 | Jīntiān wǒ qǐng nǐ, bié kèqi. | Hôm nay mình mời, đừng khách sáo.
hr33  | zhang_jing | 那个麻婆豆腐饭很辣，你吃吗？ | Nàge mápó dòufu fàn hěn là, nǐ chī ma? | Hộp cơm đậu hũ ma bà kia cay lắm, bạn có ăn không?
hr34  | liu_yang   | 吃啊，我最喜欢吃辣的。 | Chī a, wǒ zuì xǐhuan chī là de. | Có chứ, mình thích ăn cay nhất.
hr35  | narrator   | 刘洋吃完红烧肉饭，又吃了那盒麻婆豆腐饭。 | Liú Yáng chī wán hóngshāoròu fàn, yòu chī le nà hé mápó dòufu fàn. | Lưu Dương ăn xong cơm thịt kho, rồi lại ăn hộp cơm đậu hũ ma bà kia.
```


## Từ vựng


Author AI A (Qwen), step 9 mode `tuvung`. Chinese only, no `coach_vi` rows.
Reads: `09_spec.md` (Rules for all modes + Từ vựng), `05_story.md`, `04b_facts.md`, `03_core.md` §1–2.

Conventions:

- Refs `v1`…`v108`, gap rows `<ref>.g`. 16 items in story order, one per core word/phrase.
- Per item: narrator says it → says it twice → explains in very simple spoken Chinese → 1–2 example
  lines (story lines keep their original speaker) → `<word>。你说。` + gap.
- Pinyin as pronounced (sandhi for 一/不, neutral tones), full-width punctuation in 中文.
- Every line checked against `04b_facts.md` F1–F19; no **V** variants (V is Nghe-only).

### Rows

```
v1    | narrator   | 今天我们听几个词。 | Jīntiān wǒmen tīng jǐ ge cí. | Hôm nay chúng ta nghe mấy từ nhé.
v2    | narrator   | 外卖。 | Wàimài. | Đồ ăn đặt qua app, giao tới nơi.
v3    | narrator   | 外卖，外卖。 | Wàimài, wàimài. | Đồ ăn đặt giao, đồ ăn đặt giao.
v4    | narrator   | 外卖就是你在手机上点的吃的。 | Wàimài jiùshì nǐ zài shǒujī shàng diǎn de chī de. | “外卖” là đồ ăn bạn đặt trên điện thoại.
v5    | narrator   | 商家做好了，外卖小哥送过来。 | Shāngjiā zuò hǎo le, wàimài xiǎogē sòng guòlai. | Quán nấu xong, anh giao hàng mang qua.
v6    | narrator   | 中午不想出去吃，我们就点外卖。 | Zhōngwǔ bù xiǎng chūqù chī, wǒmen jiù diǎn wàimài. | Trưa không muốn ra ngoài ăn thì mình đặt “外卖”.
v7    | liu_yang   | 我来点外卖吧，午饭你想吃什么？ | Wǒ lái diǎn wàimài ba, wǔfàn nǐ xiǎng chī shénme? | Để mình đặt đồ ăn nhé, trưa bạn muốn ăn gì?
v8    | narrator   | 外卖。你说。 | Wàimài. Nǐ shuō. | “外卖”. Bạn nói đi.
v8.g  | gap        | — | — | —
v9    | narrator   | 你想吃什么？ | Nǐ xiǎng chī shénme? | Bạn muốn ăn gì?
v10   | narrator   | 你想吃什么？你想吃什么？ | Nǐ xiǎng chī shénme? Nǐ xiǎng chī shénme? | Bạn muốn ăn gì? Bạn muốn ăn gì?
v11   | narrator   | 问他想吃什么，就说这一句。 | Wèn tā xiǎng chī shénme, jiù shuō zhè yí jù. | Muốn hỏi anh ấy ăn gì thì nói câu này.
v12   | narrator   | 他说“随便”，就是吃什么都可以，你来选。 | Tā shuō “suíbiàn”, jiùshì chī shénme dōu kěyǐ, nǐ lái xuǎn. | Anh ấy nói “随便” nghĩa là ăn gì cũng được, bạn chọn đi.
v13   | liu_yang   | 张静，中午你想吃什么？ | Zhāng Jìng, zhōngwǔ nǐ xiǎng chī shénme? | Tĩnh ơi, trưa nay bạn muốn ăn gì?
v14   | zhang_jing | 随便，你点吧。 | Suíbiàn, nǐ diǎn ba. | Gì cũng được, bạn đặt đi.
v15   | narrator   | 你想吃什么。你说。 | Nǐ xiǎng chī shénme. Nǐ shuō. | “Bạn muốn ăn gì?” Bạn nói đi.
v15.g | gap        | — | — | —
v16   | narrator   | 随便，你点吧。 | Suíbiàn, nǐ diǎn ba. | Gì cũng được, bạn đặt đi.
v17   | narrator   | 随便，你点吧。随便，你点吧。 | Suíbiàn, nǐ diǎn ba. Suíbiàn, nǐ diǎn ba. | Gì cũng được, bạn đặt đi. Gì cũng được, bạn đặt đi.
v18   | narrator   | 吃什么都行，你就说“随便”。 | Chī shénme dōu xíng, nǐ jiù shuō “suíbiàn”. | Ăn gì cũng được thì bạn nói “随便”.
v19   | narrator   | 他帮你点，你就加一句“麻烦你了”。 | Tā bāng nǐ diǎn, nǐ jiù jiā yí jù “máfan nǐ le”. | Anh ấy đặt giúp thì bạn thêm câu “麻烦你了”.
v20   | zhang_jing | 随便，你点吧，麻烦你了。 | Suíbiàn, nǐ diǎn ba, máfan nǐ le. | Gì cũng được, bạn đặt đi, phiền bạn rồi.
v21   | narrator   | 随便，你点吧。你说。 | Suíbiàn, nǐ diǎn ba. Nǐ shuō. | “Gì cũng được, bạn đặt đi”. Bạn nói đi.
v21.g | gap        | — | — | —
v22   | narrator   | 还是。 | Háishi. | Hay là (chọn một trong hai).
v23   | narrator   | 还是，还是。 | Háishi, háishi. | Hay là, hay là.
v24   | narrator   | 让他选一个，中间就说“还是”。 | Ràng tā xuǎn yí ge, zhōngjiān jiù shuō “háishi”. | Cho anh ấy chọn một, ở giữa thì nói “还是”.
v25   | liu_yang   | 你吃米饭还是吃面？ | Nǐ chī mǐfàn háishi chī miàn? | Bạn ăn cơm hay ăn mì?
v26   | zhang_jing | 我今天想吃面。 | Wǒ jīntiān xiǎng chī miàn. | Hôm nay mình muốn ăn mì.
v27   | narrator   | 米饭还是面？你说。 | Mǐfàn háishi miàn? Nǐ shuō. | “Cơm hay mì?” Bạn nói đi.
v27.g | gap        | — | — | —
v28   | narrator   | 要不要？ | Yào bu yào? | Có muốn không?
v29   | narrator   | 要不要？要不要？ | Yào bu yào? Yào bu yào? | Có muốn không? Có muốn không?
v30   | narrator   | 你想给他来一份，就先问他“要不要”。 | Nǐ xiǎng gěi tā lái yí fèn, jiù xiān wèn tā “yào bu yào”. | Muốn lấy cho anh ấy một phần thì hỏi trước “có muốn không”.
v31   | liu_yang   | 要不要来一份牛肉面？ | Yào bu yào lái yí fèn niúròu miàn? | Có muốn một phần mì bò không?
v32   | zhang_jing | 好啊，我要牛肉面。 | Hǎo a, wǒ yào niúròu miàn. | Được đó, mình lấy mì bò.
v33   | narrator   | 要不要来一份？你说。 | Yào bu yào lái yí fèn? Nǐ shuō. | “Có muốn một phần không?” Bạn nói đi.
v33.g | gap        | — | — | —
v34   | narrator   | 一共多少钱？ | Yígòng duōshǎo qián? | Tổng cộng bao nhiêu tiền?
v35   | narrator   | 一共多少钱？一共多少钱？ | Yígòng duōshǎo qián? Yígòng duōshǎo qián? | Tổng cộng bao nhiêu tiền? Tổng cộng bao nhiêu tiền?
v36   | narrator   | 两三个菜一起付钱，你就问“一共多少钱”。 | Liǎng sān ge cài yìqǐ fù qián, nǐ jiù wèn “yígòng duōshǎo qián”. | Hai ba món trả chung một lần thì bạn hỏi “tổng cộng bao nhiêu tiền”.
v37   | zhang_jing | 一共多少钱？ | Yígòng duōshǎo qián? | Tổng cộng bao nhiêu tiền?
v38   | liu_yang   | 一共四十八块，我已经用手机付了。 | Yígòng sìshíbā kuài, wǒ yǐjīng yòng shǒujī fù le. | Tổng cộng bốn mươi tám tệ, mình trả bằng điện thoại rồi.
v39   | narrator   | 一共多少钱。你说。 | Yígòng duōshǎo qián. Nǐ shuō. | “Tổng cộng bao nhiêu tiền?” Bạn nói đi.
v39.g | gap        | — | — | —
v40   | narrator   | 外卖到了。 | Wàimài dào le. | Đồ ăn tới rồi.
v41   | narrator   | 外卖到了。外卖到了。 | Wàimài dào le. Wàimài dào le. | Đồ ăn tới rồi. Đồ ăn tới rồi.
v42   | narrator   | 外卖小哥送到了，你就说“外卖到了”。 | Wàimài xiǎogē sòng dào le, nǐ jiù shuō “wàimài dào le”. | Anh giao hàng mang tới rồi thì bạn nói “đồ ăn tới rồi”.
v43   | liu_yang   | 外卖到了，我去拿。 | Wàimài dào le, wǒ qù ná. | Đồ ăn tới rồi, mình đi lấy đây.
v44   | narrator   | 刘洋拿回来两个盒子，他们打开一看。 | Liú Yáng ná huílái liǎng ge hézi, tāmen dǎkāi yí kàn. | Lưu Dương mang hai cái hộp về, hai người mở ra xem.
v45   | narrator   | 外卖到了。你说。 | Wàimài dào le. Nǐ shuō. | “Đồ ăn tới rồi”. Bạn nói đi.
v45.g | gap        | — | — | —
v46   | narrator   | 不吃辣。 | Bù chī là. | Không ăn cay.
v47   | narrator   | 不吃辣。不吃辣。 | Bù chī là. Bù chī là. | Không ăn cay. Không ăn cay.
v48   | narrator   | 辣就是辣椒的那个辣。 | Là jiùshì làjiāo de nàge là. | “辣” là cay, cái cay của ớt.
v49   | narrator   | 你吃不了辣，就跟他说“我不吃辣”。 | Nǐ chī bu liǎo là, jiù gēn tā shuō “wǒ bù chī là”. | Bạn không ăn được cay thì nói với anh ấy “我不吃辣”.
v50   | narrator   | 吃了一口，太辣了，就说“太辣了”。 | Chī le yì kǒu, tài là le, jiù shuō “tài là le”. | Ăn một miếng thấy cay quá thì nói “太辣了”.
v51   | zhang_jing | 这个菜太辣了！我要喝水。 | Zhège cài tài là le! Wǒ yào hē shuǐ. | Món này cay quá! Mình phải uống nước.
v52   | zhang_jing | 我不吃辣，你知道的。 | Wǒ bù chī là, nǐ zhīdào de. | Mình không ăn cay, bạn biết mà.
v53   | narrator   | 不吃辣。你说。 | Bù chī là. Nǐ shuō. | “Không ăn cay”. Bạn nói đi.
v53.g | gap        | — | — | —
v54   | narrator   | 你点的是什么？ | Nǐ diǎn de shì shénme? | Bạn đặt món gì vậy?
v55   | narrator   | 你点的是什么？你点的是什么？ | Nǐ diǎn de shì shénme? Nǐ diǎn de shì shénme? | Bạn đặt món gì vậy? Bạn đặt món gì vậy?
v56   | narrator   | 你想知道他点了哪个菜，就这么问。 | Nǐ xiǎng zhīdào tā diǎn le nǎge cài, jiù zhème wèn. | Muốn biết anh ấy đặt món nào thì hỏi vậy.
v57   | narrator   | 他回答的时候，就说“我点的是……”。 | Tā huídá de shíhou, jiù shuō “wǒ diǎn de shì……”. | Lúc trả lời thì nói “我点的是……”.
v58   | zhang_jing | 刘洋，你点的是什么？ | Liú Yáng, nǐ diǎn de shì shénme? | Lưu Dương, bạn đặt món gì vậy?
v59   | liu_yang   | 我给你点的是牛肉面，我自己要红烧肉饭。 | Wǒ gěi nǐ diǎn de shì niúròu miàn, wǒ zìjǐ yào hóngshāoròu fàn. | Mình đặt mì bò cho bạn, còn mình lấy cơm thịt kho.
v60   | narrator   | 你点的是什么。你说。 | Nǐ diǎn de shì shénme. Nǐ shuō. | “Bạn đặt món gì?” Bạn nói đi.
v60.g | gap        | — | — | —
v61   | narrator   | 送错了。 | Sòng cuò le. | Giao nhầm rồi.
v62   | narrator   | 送错了。送错了。 | Sòng cuò le. Sòng cuò le. | Giao nhầm rồi. Giao nhầm rồi.
v63   | narrator   | 送，就是把东西送到你这儿；错，就是不对。 | Sòng, jiùshì bǎ dōngxi sòng dào nǐ zhèr; cuò, jiùshì bú duì. | “送” là mang đồ tới chỗ bạn; “错” là sai.
v64   | narrator   | 你点的是牛肉面，商家送来的是麻婆豆腐饭，这就是送错了。 | Nǐ diǎn de shì niúròu miàn, shāngjiā sòng lái de shì mápó dòufu fàn, zhè jiùshì sòng cuò le. | Bạn đặt mì bò, quán giao cơm đậu hũ ma bà, thế là giao nhầm.
v65   | liu_yang   | 可能送错了，我看看订单。 | Kěnéng sòng cuò le, wǒ kànkan dìngdān. | Có lẽ giao nhầm rồi, để mình xem đơn hàng.
v66   | liu_yang   | 是商家送错了。 | Shì shāngjiā sòng cuò le. | Là quán giao nhầm rồi.
v67   | narrator   | 送错了。你说。 | Sòng cuò le. Nǐ shuō. | “Giao nhầm rồi”. Bạn nói đi.
v67.g | gap        | — | — | —
v68   | narrator   | 订单。 | Dìngdān. | Đơn hàng.
v69   | narrator   | 订单，订单。 | Dìngdān, dìngdān. | Đơn hàng, đơn hàng.
v70   | narrator   | 你在手机上点了菜，手机上就有一个订单。 | Nǐ zài shǒujī shàng diǎn le cài, shǒujī shàng jiù yǒu yí ge dìngdān. | Bạn đặt món trên điện thoại thì trên đó có một đơn hàng.
v71   | narrator   | 订单上写着你要的菜，还有一共多少钱。 | Dìngdān shàng xiězhe nǐ yào de cài, háiyǒu yígòng duōshǎo qián. | Trên đơn ghi món bạn gọi và tổng cộng bao nhiêu tiền.
v72   | narrator   | 订单上还可以写一句话，那句话叫备注。 | Dìngdān shàng hái kěyǐ xiě yí jù huà, nà jù huà jiào bèizhù. | Trên đơn còn viết thêm được một câu, câu đó gọi là ghi chú.
v73   | narrator   | 张静不吃辣，刘洋就在备注里写“不要辣”。 | Zhāng Jìng bù chī là, Liú Yáng jiù zài bèizhù lǐ xiě “bú yào là”. | Tĩnh không ăn cay nên Lưu Dương ghi chú “đừng cho cay”.
v74   | liu_yang   | 订单上写的是牛肉面，备注写的是“不要辣”。 | Dìngdān shàng xiě de shì niúròu miàn, bèizhù xiě de shì “bú yào là”. | Trên đơn ghi là mì bò, ghi chú là “không cho cay”.
v75   | narrator   | 订单。你说。 | Dìngdān. Nǐ shuō. | “Đơn hàng”. Bạn nói đi.
v75.g | gap        | — | — | —
v76   | narrator   | 换一份。 | Huàn yí fèn. | Đổi một phần khác.
v77   | narrator   | 换一份。换一份。 | Huàn yí fèn. Huàn yí fèn. | Đổi một phần khác. Đổi một phần khác.
v78   | narrator   | 这个不对，你想换成那个，这就叫换。 | Zhège bú duì, nǐ xiǎng huàn chéng nàge, zhè jiù jiào huàn. | Cái này không đúng, bạn muốn đổi thành cái kia, thế gọi là đổi.
v79   | narrator   | 菜送错了，你就问“能不能换一份”。 | Cài sòng cuò le, nǐ jiù wèn “néng bu néng huàn yí fèn”. | Món bị giao nhầm thì bạn hỏi “đổi một phần khác được không”.
v80   | zhang_jing | 那我的面呢？能不能换一份？ | Nà wǒ de miàn ne? Néng bu néng huàn yí fèn? | Vậy mì của mình đâu? Đổi một phần khác được không?
v81   | narrator   | 换一份。你说。 | Huàn yí fèn. Nǐ shuō. | “Đổi một phần khác”. Bạn nói đi.
v81.g | gap        | — | — | —
v82   | narrator   | 给商家打电话。 | Gěi shāngjiā dǎ diànhuà. | Gọi điện cho quán.
v83   | narrator   | 给商家打电话。给商家打电话。 | Gěi shāngjiā dǎ diànhuà. Gěi shāngjiā dǎ diànhuà. | Gọi điện cho quán. Gọi điện cho quán.
v84   | narrator   | 商家就是做饭、卖饭的那家店。 | Shāngjiā jiùshì zuò fàn, mài fàn de nà jiā diàn. | “商家” là cái quán nấu và bán đồ ăn đó.
v85   | narrator   | 菜送错了，就给商家打个电话，让他们再送一次。 | Cài sòng cuò le, jiù gěi shāngjiā dǎ ge diànhuà, ràng tāmen zài sòng yí cì. | Giao nhầm thì gọi cho quán một cú, để họ giao lại lần nữa.
v86   | liu_yang   | 可以，我给商家打个电话。 | Kěyǐ, wǒ gěi shāngjiā dǎ ge diànhuà. | Được, để mình gọi cho quán.
v87   | liu_yang   | 商家说，十五分钟后送过来，你等一下。 | Shāngjiā shuō, shíwǔ fēnzhōng hòu sòng guòlai, nǐ děng yíxià. | Quán nói mười lăm phút nữa giao tới, bạn đợi một chút.
v88   | narrator   | 给商家打电话。你说。 | Gěi shāngjiā dǎ diànhuà. Nǐ shuō. | “Gọi điện cho quán”. Bạn nói đi.
v88.g | gap        | — | — | —
v89   | narrator   | 饿死了。 | È sǐ le. | Đói chết mất.
v90   | narrator   | 饿死了。饿死了。 | È sǐ le. È sǐ le. | Đói chết mất. Đói chết mất.
v91   | narrator   | 十二点了还没吃饭，就会很饿。 | Shí'èr diǎn le hái méi chī fàn, jiù huì hěn è. | Mười hai giờ rồi mà chưa ăn thì sẽ rất đói.
v92   | narrator   | 很饿很饿，就说“我饿死了”。 | Hěn è hěn è, jiù shuō “wǒ è sǐ le”. | Đói lắm lắm thì nói “我饿死了”.
v93   | zhang_jing | 是牛肉面！太好了，我饿死了！ | Shì niúròu miàn! Tài hǎo le, wǒ è sǐ le! | Là mì bò! Tuyệt quá, mình đói chết mất!
v94   | narrator   | 张静终于吃上了牛肉面，味道不错。 | Zhāng Jìng zhōngyú chī shàng le niúròu miàn, wèidào búcuò. | Cuối cùng Tĩnh cũng được ăn mì bò, vị khá ngon.
v95   | narrator   | 味道不错，就是挺好吃的。 | Wèidào búcuò, jiùshì tǐng hǎochī de. | “味道不错” nghĩa là khá ngon.
v96   | narrator   | 我饿死了。你说。 | Wǒ è sǐ le. Nǐ shuō. | “Mình đói chết mất”. Bạn nói đi.
v96.g | gap        | — | — | —
v97   | narrator   | 我转你。 | Wǒ zhuǎn nǐ. | Mình chuyển tiền cho bạn.
v98   | narrator   | 我转你。我转你。 | Wǒ zhuǎn nǐ. Wǒ zhuǎn nǐ. | Mình chuyển tiền cho bạn. Mình chuyển tiền cho bạn.
v99   | narrator   | 转就是在手机上把钱给他。 | Zhuǎn jiùshì zài shǒujī shàng bǎ qián gěi tā. | “转” là chuyển tiền cho ai đó trên điện thoại.
v100  | narrator   | 他帮你付了钱，你要还他，就说“我转你”。 | Tā bāng nǐ fù le qián, nǐ yào huán tā, jiù shuō “wǒ zhuǎn nǐ”. | Anh ấy trả giúp, bạn muốn trả lại thì nói “我转你”.
v101  | zhang_jing | 我原来的面二十二块，我转你。 | Wǒ yuánlái de miàn èrshí'èr kuài, wǒ zhuǎn nǐ. | Phần mì ban đầu của mình hai mươi hai tệ, mình chuyển cho bạn.
v102  | narrator   | 我转你。你说。 | Wǒ zhuǎn nǐ. Nǐ shuō. | “Mình chuyển tiền cho bạn”. Bạn nói đi.
v102.g | gap       | — | — | —
v103  | narrator   | 我请你。 | Wǒ qǐng nǐ. | Mình mời bạn.
v104  | narrator   | 我请你。我请你。 | Wǒ qǐng nǐ. Wǒ qǐng nǐ. | Mình mời bạn. Mình mời bạn.
v105  | narrator   | 我请你，就是这顿饭我付钱，你不用付。 | Wǒ qǐng nǐ, jiùshì zhè dùn fàn wǒ fù qián, nǐ bú yòng fù. | “我请你” nghĩa là bữa này mình trả tiền, bạn không phải trả.
v106  | narrator   | 他说“别客气”，就是真不用你还，你别推了。 | Tā shuō “bié kèqi”, jiùshì zhēn bú yòng nǐ huán, nǐ bié tuī le. | Anh ấy nói “đừng khách sáo”, tức là bạn không cần trả lại, đừng từ chối nữa.
v107  | liu_yang   | 今天我请你，别客气。 | Jīntiān wǒ qǐng nǐ, bié kèqi. | Hôm nay mình mời, đừng khách sáo.
v108  | narrator   | 今天我请你。你说。 | Jīntiān wǒ qǐng nǐ. Nǐ shuō. | “Hôm nay mình mời”. Bạn nói đi.
v108.g | gap       | — | — | —
```

### Item list (16)

| # | item | tier | refs | lifts (low-exposure items from `05_story.md` doubt 3) |
|---|---|---|---|---|
| 1 | 外卖 | Core word / P2 | v2–v8 | 午饭 (v7), 手机 (v4), 商家 (v5), 外卖小哥 (v5) |
| 2 | 你想吃什么 | P1 | v9–v15 | 说 (Coach), 问 → item 9/12 |
| 3 | 随便，你点吧 | P3 | v16–v21 | 麻烦 (v19, v20), 帮 (v19) |
| 4 | 还是 | P5 | v22–v27 | 米饭 (v25, v27) |
| 5 | 要不要 | P4 | v28–v33 | 份 (v30, v33), 问 (v30) |
| 6 | 一共多少钱 | P14 | v34–v39 | 付钱 (v36), 手机 (v38), 块 (v38) |
| 7 | 外卖到了 | P6 | v40–v45 | 拿 (v43, v44), 打开 / 盒子 (v44) |
| 8 | 不吃辣 + 太辣了 | P11 + P10 | v46–v53 | 菜 / 喝 / 水 (v51), 一口 (v50) |
| 9 | 你点的是什么 + 我点的是… | P7 + P8 | v54–v60 | 回答 (Coach, v57), 看 (v56) |
| 10 | 送错了 | P9 | v61–v67 | 送 / 错 (v63, v64), 商家 (v64, v66) |
| 11 | 订单 + 备注 | Core words | v68–v75 | 备注 (v72, v73, v74), 手机 (v70) |
| 12 | 换一份 | P12 | v76–v81 | 换 (v78, v79, v80, v81) |
| 13 | 给商家打电话 | P13 | v82–v88 | 打电话 (v82–v88), 等 (v87), 分钟 (v87), 说 (v87) |
| 14 | 饿死了 | Core word 饿 | v89–v96 | 饿 (v89–v93, v96), 好吃 (v95) |
| 15 | 我转你 | Core word 转 | v97–v102 | 转 (v97–v102), 付钱 (v100), 请 → item 16 |
| 16 | 我请你 + 别客气 | P15 / Function | v103–v108 | 请 (v103–v108) |

Not lifted here: 公司, 同事 (1 hearing each, only in `s1`) — see Open doubts 5.


## Nghe


Author AI A (Qwen). Reads: `09_spec.md` (Rules for all modes + section Nghe), `05_story.md` (story v2,
step 8), `04b_facts.md`, `03_core.md` §1–2. Chinese only (no `coach_vi` rows); the Tiếng Việt column is
the on-screen transcript. Refs `l1`…`l119`.

Shape (per spec, Nghe): 1 opener → 2 story once (`l3`–`l39`, = `s1`–`s35` verbatim) → 3 narrator
third-person retelling, no dialogue (`l40`–`l57`) → 4 four retellings that each change ONE detail and
announce it (`l58`–`l80`) → 5 story once more (`l83`–`l119`). Canonical retelling keeps F17's numbers
and uses `04b`'s V items (V1 `l41`, V2 `l44`, V3 `l45`, V4 `l57`, V6 `l51`, V7 `l53`, V8 `l54`,
V9/V11 `l56`, V10 `l47`).

```
l1  | narrator     | 我们听故事。 | Wǒmen tīng gùshi. | Chúng ta nghe câu chuyện nhé.
l2  | narrator     | 先听一遍，听懂就好，不用回答。 | Xiān tīng yí biàn, tīng dǒng jiù hǎo, bú yòng huídá. | Cứ nghe một lần, hiểu được là tốt rồi, không cần trả lời.
l3  | narrator     | 张静和刘洋是同一家公司的同事。 | Zhāng Jìng hé Liú Yáng shì tóng yì jiā gōngsī de tóngshì. | Tĩnh và Lưu Dương là đồng nghiệp ở cùng một công ty.
l4  | narrator     | 现在十一点半。 | Xiànzài shíyī diǎn bàn. | Bây giờ là mười một rưỡi.
l5  | liu_yang     | 我来点外卖吧，午饭你想吃什么？ | Wǒ lái diǎn wàimài ba, wǔfàn nǐ xiǎng chī shénme? | Để mình đặt đồ ăn nhé, trưa bạn muốn ăn gì?
l6  | zhang_jing   | 随便，你点吧，麻烦你了。 | Suíbiàn, nǐ diǎn ba, máfan nǐ le. | Gì cũng được, bạn đặt đi, phiền bạn rồi.
l7  | liu_yang     | 你吃米饭还是吃面？ | Nǐ chī mǐfàn háishi chī miàn? | Bạn ăn cơm hay ăn mì?
l8  | zhang_jing   | 我今天想吃面。 | Wǒ jīntiān xiǎng chī miàn. | Hôm nay mình muốn ăn mì.
l9  | liu_yang     | 要不要来一份牛肉面？ | Yào bu yào lái yí fèn niúròu miàn? | Có muốn một phần mì bò không?
l10 | zhang_jing   | 好啊，我要牛肉面。 | Hǎo a, wǒ yào niúròu miàn. | Được đó, mình gọi mì bò.
l11 | liu_yang     | 那我点红烧肉饭，二十六块。 | Nà wǒ diǎn hóngshāoròu fàn, èrshíliù kuài. | Vậy mình đặt cơm thịt kho, hai mươi sáu tệ.
l12 | zhang_jing   | 一共多少钱？ | Yígòng duōshǎo qián? | Tổng cộng bao nhiêu tiền?
l13 | liu_yang     | 一共四十八块，我已经用手机付了。 | Yígòng sìshíbā kuài, wǒ yǐjīng yòng shǒujī fù le. | Tổng cộng bốn mươi tám tệ, mình đã trả bằng điện thoại rồi.
l14 | narrator     | 三十分钟以后，十二点了。 | Sānshí fēnzhōng yǐhòu, shí'èr diǎn le. | Ba mươi phút sau, đã mười hai giờ.
l15 | liu_yang     | 外卖到了，我去拿。 | Wàimài dào le, wǒ qù ná. | Đồ ăn tới rồi, mình đi lấy đây.
l16 | narrator     | 刘洋拿回来两个盒子，他们打开一看。 | Liú Yáng ná huílái liǎng ge hézi, tāmen dǎkāi yí kàn. | Lưu Dương mang hai cái hộp về, hai người mở ra xem.
l17 | liu_yang     | 我的是红烧肉饭，不辣。 | Wǒ de shì hóngshāoròu fàn, bú là. | Hộp của mình là cơm thịt kho, không cay.
l18 | zhang_jing   | 我这个是麻婆豆腐饭，不是牛肉面。 | Wǒ zhège shì mápó dòufu fàn, bú shì niúròu miàn. | Hộp của mình là cơm đậu hũ ma bà, không phải mì bò.
l19 | narrator     | 张静吃了一口。 | Zhāng Jìng chī le yì kǒu. | Tĩnh ăn một miếng.
l20 | zhang_jing   | 这个菜太辣了！我要喝水。 | Zhège cài tài là le! Wǒ yào hē shuǐ. | Món này cay quá! Mình phải uống nước.
l21 | liu_yang     | 有那么辣吗？ | Yǒu nàme là ma? | Cay đến vậy luôn hả?
l22 | zhang_jing   | 我不吃辣，你知道的。 | Wǒ bù chī là, nǐ zhīdào de. | Mình không ăn cay, bạn biết mà.
l23 | zhang_jing   | 刘洋，你点的是什么？ | Liú Yáng, nǐ diǎn de shì shénme? | Lưu Dương, bạn đặt món gì vậy?
l24 | liu_yang     | 我给你点的是牛肉面，我自己要红烧肉饭。 | Wǒ gěi nǐ diǎn de shì niúròu miàn, wǒ zìjǐ yào hóngshāoròu fàn. | Mình đặt mì bò cho bạn, còn mình lấy cơm thịt kho.
l25 | liu_yang     | 可能送错了，我看看订单。 | Kěnéng sòng cuò le, wǒ kànkan dìngdān. | Có lẽ giao nhầm rồi, để mình xem đơn hàng.
l26 | liu_yang     | 订单上写的是牛肉面，备注写的是“不要辣”。 | Dìngdān shàng xiě de shì niúròu miàn, bèizhù xiě de shì “bú yào là”. | Trên đơn ghi là mì bò, ghi chú là “không cho cay”.
l27 | liu_yang     | 是商家送错了。 | Shì shāngjiā sòng cuò le. | Là quán giao nhầm rồi.
l28 | zhang_jing   | 那我的面呢？能不能换一份？ | Nà wǒ de miàn ne? Néng bu néng huàn yí fèn? | Vậy mì của mình đâu? Đổi một phần khác được không?
l29 | liu_yang     | 可以，我给商家打个电话。 | Kěyǐ, wǒ gěi shāngjiā dǎ ge diànhuà. | Được, để mình gọi cho quán.
l30 | liu_yang     | 商家说，十五分钟后送过来，你等一下。 | Shāngjiā shuō, shíwǔ fēnzhōng hòu sòng guòlai, nǐ děng yíxià. | Quán nói mười lăm phút nữa sẽ giao tới, bạn đợi một chút.
l31 | narrator     | 十二点十五，商家又送来一个盒子。 | Shí'èr diǎn shíwǔ, shāngjiā yòu sòng lái yí ge hézi. | Mười hai giờ mười lăm, quán lại giao tới một cái hộp.
l32 | zhang_jing   | 是牛肉面！太好了，我饿死了！ | Shì niúròu miàn! Tài hǎo le, wǒ è sǐ le! | Là mì bò! Tuyệt quá, mình đói chết mất!
l33 | liu_yang     | 这次不用再付钱，是商家重新送的。 | Zhè cì bú yòng zài fù qián, shì shāngjiā chóngxīn sòng de. | Lần này không cần trả thêm tiền, quán giao lại mà.
l34 | narrator     | 张静终于吃上了牛肉面，味道不错。 | Zhāng Jìng zhōngyú chī shàng le niúròu miàn, wèidào búcuò. | Cuối cùng Tĩnh cũng được ăn mì bò, vị khá ngon.
l35 | zhang_jing   | 我原来的面二十二块，我转你。 | Wǒ yuánlái de miàn èrshí'èr kuài, wǒ zhuǎn nǐ. | Phần mì ban đầu của mình hai mươi hai tệ, mình chuyển cho bạn.
l36 | liu_yang     | 今天我请你，别客气。 | Jīntiān wǒ qǐng nǐ, bié kèqi. | Hôm nay mình mời, đừng khách sáo.
l37 | zhang_jing   | 那个麻婆豆腐饭很辣，你吃吗？ | Nàge mápó dòufu fàn hěn là, nǐ chī ma? | Hộp cơm đậu hũ ma bà kia cay lắm, bạn có ăn không?
l38 | liu_yang     | 吃啊，我最喜欢吃辣的。 | Chī a, wǒ zuì xǐhuan chī là de. | Có chứ, mình thích ăn cay nhất.
l39 | narrator     | 刘洋吃完红烧肉饭，又吃了那盒麻婆豆腐饭。 | Liú Yáng chī wán hóngshāoròu fàn, yòu chī le nà hé mápó dòufu fàn. | Lưu Dương ăn xong cơm thịt kho, rồi lại ăn hộp cơm đậu hũ ma bà kia.
l40 | narrator     | 现在我再说一遍这个故事，你听。 | Xiànzài wǒ zài shuō yí biàn zhège gùshi, nǐ tīng. | Bây giờ mình kể lại câu chuyện này một lần nữa, bạn nghe nhé.
l41 | narrator     | 今天十一点半，刘洋在手机上给两个人点了外卖。 | Jīntiān shíyī diǎn bàn, Liú Yáng zài shǒujī shàng gěi liǎng ge rén diǎn le wàimài. | Hôm nay mười một rưỡi, Lưu Dương đặt đồ ăn trên điện thoại cho cả hai người.
l42 | narrator     | 张静说随便，她想吃面，所以刘洋给她点了一份牛肉面。 | Zhāng Jìng shuō suíbiàn, tā xiǎng chī miàn, suǒyǐ Liú Yáng gěi tā diǎn le yí fèn niúròu miàn. | Tĩnh nói gì cũng được, cô ấy muốn ăn mì, nên Lưu Dương đặt cho cô ấy một phần mì bò.
l43 | narrator     | 刘洋自己要了红烧肉饭，一共四十八块，他在手机上付了钱。 | Liú Yáng zìjǐ yào le hóngshāoròu fàn, yígòng sìshíbā kuài, tā zài shǒujī shàng fù le qián. | Lưu Dương tự lấy cơm thịt kho, tổng cộng bốn mươi tám tệ, anh ấy trả tiền trên điện thoại.
l44 | narrator     | 三十分钟以后，十二点，外卖来了，刘洋下去拿，外卖小哥在楼下等他。 | Sānshí fēnzhōng yǐhòu, shí'èr diǎn, wàimài lái le, Liú Yáng xiàqù ná, wàimài xiǎogē zài lóuxià děng tā. | Ba mươi phút sau, mười hai giờ, đồ ăn tới, Lưu Dương đi xuống lấy, anh giao hàng đợi anh ấy dưới lầu.
l45 | narrator     | 他拿回来两个盒子，一个是刘洋的，一个是张静的。 | Tā ná huílái liǎng ge hézi, yí ge shì Liú Yáng de, yí ge shì Zhāng Jìng de. | Anh ấy mang về hai cái hộp, một cái của Lưu Dương, một cái của Tĩnh.
l46 | narrator     | 刘洋的那个是红烧肉饭，不辣，没有错。 | Liú Yáng de nàge shì hóngshāoròu fàn, bú là, méiyǒu cuò. | Hộp của Lưu Dương là cơm thịt kho, không cay, không nhầm.
l47 | narrator     | 可是张静的盒子里不是面，是麻婆豆腐饭，很辣。 | Kěshì Zhāng Jìng de hézi lǐ bú shì miàn, shì mápó dòufu fàn, hěn là. | Nhưng trong hộp của Tĩnh không phải mì, mà là cơm đậu hũ ma bà, rất cay.
l48 | narrator     | 张静吃了一口，太辣了，她马上喝水。 | Zhāng Jìng chī le yì kǒu, tài là le, tā mǎshàng hē shuǐ. | Tĩnh ăn một miếng, cay quá, cô ấy uống nước ngay.
l49 | narrator     | 她不吃辣，所以这个菜她不能吃。 | Tā bù chī là, suǒyǐ zhège cài tā bù néng chī. | Cô ấy không ăn cay, nên món này cô ấy không ăn được.
l50 | narrator     | 刘洋看订单，订单上写的是牛肉面，备注是“不要辣”。 | Liú Yáng kàn dìngdān, dìngdān shàng xiě de shì niúròu miàn, bèizhù shì “bú yào là”. | Lưu Dương xem đơn hàng, trên đơn ghi mì bò, ghi chú là “không cho cay”.
l51 | narrator     | 刘洋点对了，是商家送错了。 | Liú Yáng diǎn duì le, shì shāngjiā sòng cuò le. | Lưu Dương đặt đúng rồi, là quán giao nhầm.
l52 | narrator     | 张静的面没有来，她想换一份。 | Zhāng Jìng de miàn méiyǒu lái, tā xiǎng huàn yí fèn. | Mì của Tĩnh không tới, cô ấy muốn đổi một phần khác.
l53 | narrator     | 刘洋打电话问商家，商家说，十五分钟送过来。 | Liú Yáng dǎ diànhuà wèn shāngjiā, shāngjiā shuō, shíwǔ fēnzhōng sòng guòlai. | Lưu Dương gọi điện hỏi quán, quán nói mười lăm phút nữa giao qua.
l54 | narrator     | 十二点十五，面来了，就一个盒子，不用再付钱。 | Shí'èr diǎn shíwǔ, miàn lái le, jiù yí ge hézi, bú yòng zài fù qián. | Mười hai giờ mười lăm, mì tới, chỉ một cái hộp, không phải trả thêm tiền.
l55 | narrator     | 张静很饿，等了一会儿，终于吃上了牛肉面。 | Zhāng Jìng hěn è, děng le yíhuìr, zhōngyú chī shàng le niúròu miàn. | Tĩnh đói lắm, đợi một lúc, cuối cùng cũng được ăn mì bò.
l56 | narrator     | 她要转钱给刘洋，刘洋说今天他请她，让她别客气。 | Tā yào zhuǎn qián gěi Liú Yáng, Liú Yáng shuō jīntiān tā qǐng tā, ràng tā bié kèqi. | Cô ấy muốn chuyển tiền cho Lưu Dương, Lưu Dương nói hôm nay anh ấy mời, bảo cô ấy đừng khách sáo.
l57 | narrator     | 那个辣的盒子，刘洋自己吃了，因为他最喜欢吃辣。 | Nàge là de hézi, Liú Yáng zìjǐ chī le, yīnwèi tā zuì xǐhuan chī là. | Cái hộp cay kia, Lưu Dương tự ăn, vì anh ấy thích ăn cay nhất.
l58 | narrator     | 接着听，下面几个版本不一样，都不是真的故事。 | Jiēzhe tīng, xiàmiàn jǐ ge bǎnběn bù yíyàng, dōu bú shì zhēn de gùshi. | Nghe tiếp nhé, các bản dưới đây khác nhau và đều không phải câu chuyện thật.
l59 | narrator     | 第一个版本，这次张静的菜不一样。 | Dì yí ge bǎnběn, zhè cì Zhāng Jìng de cài bù yíyàng. | Bản thứ nhất, lần này món của Tĩnh khác.
l60 | narrator     | 这次张静不想吃面，刘洋也给她点了红烧肉饭。 | Zhè cì Zhāng Jìng bù xiǎng chī miàn, Liú Yáng yě gěi tā diǎn le hóngshāoròu fàn. | Lần này Tĩnh không muốn ăn mì, Lưu Dương cũng đặt cơm thịt kho cho cô ấy.
l61 | narrator     | 两个盒子都是红烧肉饭，都不辣。 | Liǎng ge hézi dōu shì hóngshāoròu fàn, dōu bú là. | Cả hai hộp đều là cơm thịt kho, đều không cay.
l62 | narrator     | 商家没有送错，谁也没有打电话。 | Shāngjiā méiyǒu sòng cuò, shéi yě méiyǒu dǎ diànhuà. | Quán không giao nhầm, chẳng ai gọi điện thoại.
l63 | narrator     | 两个人吃得很开心，张静也没有多等。 | Liǎng ge rén chī de hěn kāixīn, Zhāng Jìng yě méiyǒu duō děng. | Hai người ăn rất vui, Tĩnh cũng không phải đợi thêm.
l64 | narrator     | 第二个版本，这次刘洋给自己点的是麻婆豆腐饭。 | Dì èr ge bǎnběn, zhè cì Liú Yáng gěi zìjǐ diǎn de shì mápó dòufu fàn. | Bản thứ hai, lần này Lưu Dương tự gọi cho mình cơm đậu hũ ma bà.
l65 | narrator     | 刘洋打开一看，自己的是麻婆豆腐饭，很辣。 | Liú Yáng dǎkāi yí kàn, zìjǐ de shì mápó dòufu fàn, hěn là. | Lưu Dương mở ra xem, hộp của mình là cơm đậu hũ ma bà, rất cay.
l66 | narrator     | 张静的盒子里是牛肉面，一点也没有错。 | Zhāng Jìng de hézi lǐ shì niúròu miàn, yìdiǎn yě méiyǒu cuò. | Trong hộp của Tĩnh là mì bò, chẳng nhầm chút nào.
l67 | narrator     | 刘洋最喜欢吃辣，所以他吃得很快，也不喝水。 | Liú Yáng zuì xǐhuan chī là, suǒyǐ tā chī de hěn kuài, yě bù hē shuǐ. | Lưu Dương thích ăn cay nhất, nên anh ấy ăn rất nhanh, cũng chẳng uống nước.
l68 | narrator     | 这个版本里，谁也不用打电话，谁也不用等。 | Zhège bǎnběn lǐ, shéi yě bú yòng dǎ diànhuà, shéi yě bú yòng děng. | Trong bản này, chẳng ai phải gọi điện, chẳng ai phải đợi.
l69 | narrator     | 第三个版本，这次商家说的话不一样。 | Dì sān ge bǎnběn, zhè cì shāngjiā shuō de huà bù yíyàng. | Bản thứ ba, lần này lời quán nói thì khác.
l70 | narrator     | 张静的盒子里还是麻婆豆腐饭，还是那么辣。 | Zhāng Jìng de hézi lǐ háishi mápó dòufu fàn, háishi nàme là. | Trong hộp của Tĩnh vẫn là cơm đậu hũ ma bà, vẫn cay như thế.
l71 | narrator     | 刘洋打电话问商家，商家说，二十分钟送过来。 | Liú Yáng dǎ diànhuà wèn shāngjiā, shāngjiā shuō, èrshí fēnzhōng sòng guòlai. | Lưu Dương gọi điện hỏi quán, quán nói hai mươi phút nữa giao qua.
l72 | narrator     | 十二点二十，面才来，张静多等了五分钟。 | Shí'èr diǎn èrshí, miàn cái lái, Zhāng Jìng duō děng le wǔ fēnzhōng. | Mười hai giờ hai mươi mì mới tới, Tĩnh phải đợi thêm năm phút.
l73 | narrator     | 她更饿了，面一来，她吃得很高兴。 | Tā gèng è le, miàn yì lái, tā chī de hěn gāoxìng. | Cô ấy càng đói hơn, mì vừa tới là cô ấy ăn rất vui.
l74 | narrator     | 别的都一样，那盒面也不用付钱。 | Biéde dōu yíyàng, nà hé miàn yě bú yòng fù qián. | Mọi thứ khác đều như cũ, hộp mì đó cũng không phải trả tiền.
l75 | narrator     | 最后一个版本，这次张静变了。 | Zuìhòu yí ge bǎnběn, zhè cì Zhāng Jìng biàn le. | Bản cuối cùng, lần này Tĩnh thay đổi.
l76 | narrator     | 盒子里还是麻婆豆腐饭，张静吃了一口。 | Hézi lǐ háishi mápó dòufu fàn, Zhāng Jìng chī le yì kǒu. | Trong hộp vẫn là cơm đậu hũ ma bà, Tĩnh ăn một miếng.
l77 | narrator     | 这次她不怕辣，她挺喜欢吃辣的。 | Zhè cì tā bú pà là, tā tǐng xǐhuan chī là de. | Lần này cô ấy không sợ cay, cô ấy khá là thích ăn cay.
l78 | narrator     | 所以她把那盒麻婆豆腐饭全吃完了。 | Suǒyǐ tā bǎ nà hé mápó dòufu fàn quán chī wán le. | Nên cô ấy ăn hết sạch hộp cơm đậu hũ ma bà đó.
l79 | narrator     | 她不要牛肉面了，刘洋也没有打电话。 | Tā bú yào niúròu miàn le, Liú Yáng yě méiyǒu dǎ diànhuà. | Cô ấy không lấy mì bò nữa, Lưu Dương cũng không gọi điện.
l80 | narrator     | 刘洋吃自己的红烧肉饭，张静吃那个辣的饭。 | Liú Yáng chī zìjǐ de hóngshāoròu fàn, Zhāng Jìng chī nàge là de fàn. | Lưu Dương ăn phần cơm thịt kho của mình, Tĩnh ăn hộp cơm cay kia.
l81 | narrator     | 好，这几个版本都不是真的故事。 | Hǎo, zhè jǐ ge bǎnběn dōu bú shì zhēn de gùshi. | Rồi, mấy bản này đều không phải câu chuyện thật.
l82 | narrator     | 再听一遍，这次听真的故事。 | Zài tīng yí biàn, zhè cì tīng zhēn de gùshi. | Nghe lại một lần nữa, lần này nghe câu chuyện thật.
l83 | narrator     | 张静和刘洋是同一家公司的同事。 | Zhāng Jìng hé Liú Yáng shì tóng yì jiā gōngsī de tóngshì. | Tĩnh và Lưu Dương là đồng nghiệp ở cùng một công ty.
l84 | narrator     | 现在十一点半。 | Xiànzài shíyī diǎn bàn. | Bây giờ là mười một rưỡi.
l85 | liu_yang     | 我来点外卖吧，午饭你想吃什么？ | Wǒ lái diǎn wàimài ba, wǔfàn nǐ xiǎng chī shénme? | Để mình đặt đồ ăn nhé, trưa bạn muốn ăn gì?
l86 | zhang_jing   | 随便，你点吧，麻烦你了。 | Suíbiàn, nǐ diǎn ba, máfan nǐ le. | Gì cũng được, bạn đặt đi, phiền bạn rồi.
l87 | liu_yang     | 你吃米饭还是吃面？ | Nǐ chī mǐfàn háishi chī miàn? | Bạn ăn cơm hay ăn mì?
l88 | zhang_jing   | 我今天想吃面。 | Wǒ jīntiān xiǎng chī miàn. | Hôm nay mình muốn ăn mì.
l89 | liu_yang     | 要不要来一份牛肉面？ | Yào bu yào lái yí fèn niúròu miàn? | Có muốn một phần mì bò không?
l90 | zhang_jing   | 好啊，我要牛肉面。 | Hǎo a, wǒ yào niúròu miàn. | Được đó, mình gọi mì bò.
l91 | liu_yang     | 那我点红烧肉饭，二十六块。 | Nà wǒ diǎn hóngshāoròu fàn, èrshíliù kuài. | Vậy mình đặt cơm thịt kho, hai mươi sáu tệ.
l92 | zhang_jing   | 一共多少钱？ | Yígòng duōshǎo qián? | Tổng cộng bao nhiêu tiền?
l93 | liu_yang     | 一共四十八块，我已经用手机付了。 | Yígòng sìshíbā kuài, wǒ yǐjīng yòng shǒujī fù le. | Tổng cộng bốn mươi tám tệ, mình đã trả bằng điện thoại rồi.
l94 | narrator     | 三十分钟以后，十二点了。 | Sānshí fēnzhōng yǐhòu, shí'èr diǎn le. | Ba mươi phút sau, đã mười hai giờ.
l95 | liu_yang     | 外卖到了，我去拿。 | Wàimài dào le, wǒ qù ná. | Đồ ăn tới rồi, mình đi lấy đây.
l96 | narrator     | 刘洋拿回来两个盒子，他们打开一看。 | Liú Yáng ná huílái liǎng ge hézi, tāmen dǎkāi yí kàn. | Lưu Dương mang hai cái hộp về, hai người mở ra xem.
l97 | liu_yang     | 我的是红烧肉饭，不辣。 | Wǒ de shì hóngshāoròu fàn, bú là. | Hộp của mình là cơm thịt kho, không cay.
l98 | zhang_jing   | 我这个是麻婆豆腐饭，不是牛肉面。 | Wǒ zhège shì mápó dòufu fàn, bú shì niúròu miàn. | Hộp của mình là cơm đậu hũ ma bà, không phải mì bò.
l99 | narrator     | 张静吃了一口。 | Zhāng Jìng chī le yì kǒu. | Tĩnh ăn một miếng.
l100 | zhang_jing  | 这个菜太辣了！我要喝水。 | Zhège cài tài là le! Wǒ yào hē shuǐ. | Món này cay quá! Mình phải uống nước.
l101 | liu_yang    | 有那么辣吗？ | Yǒu nàme là ma? | Cay đến vậy luôn hả?
l102 | zhang_jing  | 我不吃辣，你知道的。 | Wǒ bù chī là, nǐ zhīdào de. | Mình không ăn cay, bạn biết mà.
l103 | zhang_jing  | 刘洋，你点的是什么？ | Liú Yáng, nǐ diǎn de shì shénme? | Lưu Dương, bạn đặt món gì vậy?
l104 | liu_yang    | 我给你点的是牛肉面，我自己要红烧肉饭。 | Wǒ gěi nǐ diǎn de shì niúròu miàn, wǒ zìjǐ yào hóngshāoròu fàn. | Mình đặt mì bò cho bạn, còn mình lấy cơm thịt kho.
l105 | liu_yang    | 可能送错了，我看看订单。 | Kěnéng sòng cuò le, wǒ kànkan dìngdān. | Có lẽ giao nhầm rồi, để mình xem đơn hàng.
l106 | liu_yang    | 订单上写的是牛肉面，备注写的是“不要辣”。 | Dìngdān shàng xiě de shì niúròu miàn, bèizhù xiě de shì “bú yào là”. | Trên đơn ghi là mì bò, ghi chú là “không cho cay”.
l107 | liu_yang    | 是商家送错了。 | Shì shāngjiā sòng cuò le. | Là quán giao nhầm rồi.
l108 | zhang_jing  | 那我的面呢？能不能换一份？ | Nà wǒ de miàn ne? Néng bu néng huàn yí fèn? | Vậy mì của mình đâu? Đổi một phần khác được không?
l109 | liu_yang    | 可以，我给商家打个电话。 | Kěyǐ, wǒ gěi shāngjiā dǎ ge diànhuà. | Được, để mình gọi cho quán.
l110 | liu_yang    | 商家说，十五分钟后送过来，你等一下。 | Shāngjiā shuō, shíwǔ fēnzhōng hòu sòng guòlai, nǐ děng yíxià. | Quán nói mười lăm phút nữa sẽ giao tới, bạn đợi một chút.
l111 | narrator    | 十二点十五，商家又送来一个盒子。 | Shí'èr diǎn shíwǔ, shāngjiā yòu sòng lái yí ge hézi. | Mười hai giờ mười lăm, quán lại giao tới một cái hộp.
l112 | zhang_jing  | 是牛肉面！太好了，我饿死了！ | Shì niúròu miàn! Tài hǎo le, wǒ è sǐ le! | Là mì bò! Tuyệt quá, mình đói chết mất!
l113 | liu_yang    | 这次不用再付钱，是商家重新送的。 | Zhè cì bú yòng zài fù qián, shì shāngjiā chóngxīn sòng de. | Lần này không cần trả thêm tiền, quán giao lại mà.
l114 | narrator    | 张静终于吃上了牛肉面，味道不错。 | Zhāng Jìng zhōngyú chī shàng le niúròu miàn, wèidào búcuò. | Cuối cùng Tĩnh cũng được ăn mì bò, vị khá ngon.
l115 | zhang_jing  | 我原来的面二十二块，我转你。 | Wǒ yuánlái de miàn èrshí'èr kuài, wǒ zhuǎn nǐ. | Phần mì ban đầu của mình hai mươi hai tệ, mình chuyển cho bạn.
l116 | liu_yang    | 今天我请你，别客气。 | Jīntiān wǒ qǐng nǐ, bié kèqi. | Hôm nay mình mời, đừng khách sáo.
l117 | zhang_jing  | 那个麻婆豆腐饭很辣，你吃吗？ | Nàge mápó dòufu fàn hěn là, nǐ chī ma? | Hộp cơm đậu hũ ma bà kia cay lắm, bạn có ăn không?
l118 | liu_yang    | 吃啊，我最喜欢吃辣的。 | Chī a, wǒ zuì xǐhuan chī là de. | Có chứ, mình thích ăn cay nhất.
l119 | narrator    | 刘洋吃完红烧肉饭，又吃了那盒麻婆豆腐饭。 | Liú Yáng chī wán hóngshāoròu fàn, yòu chī le nà hé mápó dòufu fàn. | Lưu Dương ăn xong cơm thịt kho, rồi lại ăn hộp cơm đậu hũ ma bà kia.
```


## Phản xạ


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
q6   | narrator | 刘洋问她吃什么，张静一开始怎么说？ | Liú Yáng wèn tā chī shénme, Zhāng Jìng yì kāishǐ zěnme shuō? | Lưu Dương hỏi Tĩnh ăn gì, lúc đầu Tĩnh nói sao?
q6.g | gap | — | — | —
q6a  | narrator | 随便。 | Suíbiàn. | Gì cũng được.
q6b  | narrator | 她说随便，让刘洋点。 | Tā shuō suíbiàn, ràng Liú Yáng diǎn. | Bạn ấy nói gì cũng được, để Lưu Dương đặt.
q7   | narrator | 张静要吃米饭还是吃面？ | Zhāng Jìng yào chī mǐfàn háishi chī miàn? | Tĩnh ăn cơm hay ăn mì?
q7.g | gap | — | — | —
q7a  | narrator | 面。 | Miàn. | Mì.
q7b  | narrator | 张静今天想吃面。 | Zhāng Jìng jīntiān xiǎng chī miàn. | Hôm nay Tĩnh muốn ăn mì.
q8   | narrator | 刘洋给张静点的是什么？ | Liú Yáng gěi Zhāng Jìng diǎn de shì shénme? | Lưu Dương đặt món gì cho Tĩnh?
q8.g | gap | — | — | —
q8a  | narrator | 牛肉面。 | Niúròu miàn. | Mì bò.
q8b  | narrator | 刘洋给张静点的是牛肉面。 | Liú Yáng gěi Zhāng Jìng diǎn de shì niúròu miàn. | Lưu Dương đặt mì bò cho Tĩnh.
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
q16  | narrator | 第一份外卖等了多久？ | Dì yí fèn wàimài děng le duō jiǔ? | Phần đồ ăn đầu tiên phải đợi bao lâu?
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
q22  | narrator | 第一次张静的盒子里是什么？ | Dì yí cì Zhāng Jìng de hézi lǐ shì shénme? | Lần đầu trong hộp của Tĩnh là gì?
q22.g | gap | — | — | —
q22a | narrator | 麻婆豆腐饭。 | Mápó dòufu fàn. | Cơm đậu hũ ma bà.
q22b | narrator | 第一次是麻婆豆腐饭。 | Dì yí cì shì mápó dòufu fàn. | Lần đầu là cơm đậu hũ ma bà.
q23  | narrator | 张静那盒辣不辣？ | Zhāng Jìng nà hé là bu là? | Hộp của Tĩnh có cay không?
q23.g | gap | — | — | —
q23a | narrator | 很辣。 | Hěn là. | Cay lắm.
q23b | narrator | 张静那盒很辣。 | Zhāng Jìng nà hé hěn là. | Hộp của Tĩnh cay lắm.
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
q46  | narrator | 张静要付钱吗？ | Zhāng Jìng yào fù qián ma? | Tĩnh có phải trả tiền không?
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


## Nói

```
n1   | narrator    | 跟我说。 | Gēn wǒ shuō. | Nói theo tôi.
n2   | narrator    | 吃面。 | Chī miàn. | ăn mì
n2.g | gap         | — | — | —
n3   | narrator    | 想吃面。 | Xiǎng chī miàn. | muốn ăn mì
n3.g | gap         | — | — | —
n4   | narrator    | 我今天想吃面。 | Wǒ jīntiān xiǎng chī miàn. | Hôm nay mình muốn ăn mì.
n4.g | gap         | — | — | —
n5   | narrator    | 钱。 | Qián. | tiền
n5.g | gap         | — | — | —
n6   | narrator    | 多少钱？ | Duōshǎo qián? | bao nhiêu tiền?
n6.g | gap         | — | — | —
n7   | narrator    | 一共多少钱？ | Yígòng duōshǎo qián? | Tổng cộng bao nhiêu tiền?
n7.g | gap         | — | — | —
n8   | narrator    | 牛肉面。 | Niúròu miàn. | mì bò
n8.g | gap         | — | — | —
n9   | narrator    | 一份牛肉面。 | Yí fèn niúròu miàn. | một phần mì bò
n9.g | gap         | — | — | —
n10  | narrator    | 来一份牛肉面。 | Lái yí fèn niúròu miàn. | lấy một phần mì bò
n10.g | gap        | — | — | —
n11  | narrator    | 要不要来一份牛肉面？ | Yào bu yào lái yí fèn niúròu miàn? | Có muốn một phần mì bò không?
n11.g | gap        | — | — | —
n12  | narrator    | 吃什么？ | Chī shénme? | ăn gì?
n12.g | gap        | — | — | —
n13  | narrator    | 想吃什么？ | Xiǎng chī shénme? | muốn ăn gì?
n13.g | gap        | — | — | —
n14  | narrator    | 我来点外卖吧。 | Wǒ lái diǎn wàimài ba. | Để mình đặt đồ ăn nhé.
n14.g | gap        | — | — | —
n15  | narrator    | 我来点外卖吧，午饭你想吃什么？ | Wǒ lái diǎn wàimài ba, wǔfàn nǐ xiǎng chī shénme? | Để mình đặt đồ ăn nhé, trưa bạn muốn ăn gì?
n15.g | gap        | — | — | —
n16  | narrator    | 吃辣。 | Chī là. | ăn cay
n16.g | gap        | — | — | —
n17  | narrator    | 不吃辣。 | Bù chī là. | không ăn cay
n17.g | gap        | — | — | —
n18  | narrator    | 我不吃辣。 | Wǒ bù chī là. | Mình không ăn cay.
n18.g | gap        | — | — | —
n19  | narrator    | 我不吃辣，你知道的。 | Wǒ bù chī là, nǐ zhīdào de. | Mình không ăn cay, bạn biết mà.
n19.g | gap        | — | — | —
n20  | narrator    | 送错了。 | Sòng cuò le. | giao nhầm rồi
n20.g | gap        | — | — | —
n21  | narrator    | 商家送错了。 | Shāngjiā sòng cuò le. | quán giao nhầm rồi
n21.g | gap        | — | — | —
n22  | narrator    | 是商家送错了。 | Shì shāngjiā sòng cuò le. | Là quán giao nhầm rồi.
n22.g | gap        | — | — | —
n23  | narrator    | 换一份。 | Huàn yí fèn. | đổi một phần khác
n23.g | gap        | — | — | —
n24  | narrator    | 能不能换一份？ | Néng bu néng huàn yí fèn? | đổi một phần khác được không?
n24.g | gap        | — | — | —
n25  | narrator    | 那我的面呢？能不能换一份？ | Nà wǒ de miàn ne? Néng bu néng huàn yí fèn? | Vậy mì của mình đâu? Đổi một phần khác được không?
n25.g | gap        | — | — | —
n26  | narrator    | 打个电话。 | Dǎ ge diànhuà. | gọi điện một cú
n26.g | gap        | — | — | —
n27  | narrator    | 给商家打个电话。 | Gěi shāngjiā dǎ ge diànhuà. | gọi cho quán một cú
n27.g | gap        | — | — | —
n28  | narrator    | 可以，我给商家打个电话。 | Kěyǐ, wǒ gěi shāngjiā dǎ ge diànhuà. | Được, để mình gọi cho quán một cú.
n28.g | gap        | — | — | —
n29  | narrator    | 你说。 | Nǐ shuō. | Đến lượt bạn nói.
n30  | liu_yang    | 我来点外卖吧，午饭你想吃什么？ | Wǒ lái diǎn wàimài ba, wǔfàn nǐ xiǎng chī shénme? | Để mình đặt đồ ăn nhé, trưa bạn muốn ăn gì?
n30.g | gap        | — | — | —
n31  | zhang_jing  | 随便，你点吧，麻烦你了。 | Suíbiàn, nǐ diǎn ba, máfan nǐ le. | Gì cũng được, bạn đặt đi, phiền bạn rồi.
n31.g | gap        | — | — | —
n32  | liu_yang    | 你吃米饭还是吃面？ | Nǐ chī mǐfàn háishi chī miàn? | Bạn ăn cơm hay ăn mì?
n32.g | gap        | — | — | —
n33  | zhang_jing  | 我今天想吃面。 | Wǒ jīntiān xiǎng chī miàn. | Hôm nay mình muốn ăn mì.
n33.g | gap        | — | — | —
n34  | liu_yang    | 要不要来一份牛肉面？ | Yào bu yào lái yí fèn niúròu miàn? | Có muốn một phần mì bò không?
n34.g | gap        | — | — | —
n35  | zhang_jing  | 好啊，我要牛肉面。 | Hǎo a, wǒ yào niúròu miàn. | Được đó, mình gọi mì bò.
n35.g | gap        | — | — | —
n36  | liu_yang    | 那我点红烧肉饭，二十六块。 | Nà wǒ diǎn hóngshāoròu fàn, èrshíliù kuài. | Vậy mình đặt cơm thịt kho, hai mươi sáu tệ.
n36.g | gap        | — | — | —
n37  | zhang_jing  | 一共多少钱？ | Yígòng duōshǎo qián? | Tổng cộng bao nhiêu tiền?
n37.g | gap        | — | — | —
n38  | liu_yang    | 一共四十八块，我已经用手机付了。 | Yígòng sìshíbā kuài, wǒ yǐjīng yòng shǒujī fù le. | Tổng cộng bốn mươi tám tệ, mình đã trả bằng điện thoại rồi.
n38.g | gap        | — | — | —
n39  | liu_yang    | 外卖到了，我去拿。 | Wàimài dào le, wǒ qù ná. | Đồ ăn tới rồi, mình đi lấy đây.
n39.g | gap        | — | — | —
n40  | liu_yang    | 我的是红烧肉饭，不辣。 | Wǒ de shì hóngshāoròu fàn, bú là. | Hộp của mình là cơm thịt kho, không cay.
n40.g | gap        | — | — | —
n41  | zhang_jing  | 我这个是麻婆豆腐饭，不是牛肉面。 | Wǒ zhège shì mápó dòufu fàn, bú shì niúròu miàn. | Hộp của mình là cơm đậu hũ ma bà, không phải mì bò.
n41.g | gap        | — | — | —
n42  | zhang_jing  | 这个菜太辣了！我要喝水。 | Zhège cài tài là le! Wǒ yào hē shuǐ. | Món này cay quá! Mình phải uống nước.
n42.g | gap        | — | — | —
n43  | liu_yang    | 有那么辣吗？ | Yǒu nàme là ma? | Cay đến vậy luôn hả?
n43.g | gap        | — | — | —
n44  | zhang_jing  | 我不吃辣，你知道的。 | Wǒ bù chī là, nǐ zhīdào de. | Mình không ăn cay, bạn biết mà.
n44.g | gap        | — | — | —
n45  | zhang_jing  | 刘洋，你点的是什么？ | Liú Yáng, nǐ diǎn de shì shénme? | Lưu Dương, bạn đặt món gì vậy?
n45.g | gap        | — | — | —
n46  | liu_yang    | 我给你点的是牛肉面，我自己要红烧肉饭。 | Wǒ gěi nǐ diǎn de shì niúròu miàn, wǒ zìjǐ yào hóngshāoròu fàn. | Mình đặt mì bò cho bạn, còn mình lấy cơm thịt kho.
n46.g | gap        | — | — | —
n47  | liu_yang    | 可能送错了，我看看订单。 | Kěnéng sòng cuò le, wǒ kànkan dìngdān. | Có lẽ giao nhầm rồi, để mình xem đơn hàng.
n47.g | gap        | — | — | —
n48  | liu_yang    | 订单上写的是牛肉面，备注写的是“不要辣”。 | Dìngdān shàng xiě de shì niúròu miàn, bèizhù xiě de shì “bú yào là”. | Trên đơn ghi là mì bò, ghi chú là “không cho cay”.
n48.g | gap        | — | — | —
n49  | liu_yang    | 是商家送错了。 | Shì shāngjiā sòng cuò le. | Là quán giao nhầm rồi.
n49.g | gap        | — | — | —
n50  | zhang_jing  | 那我的面呢？能不能换一份？ | Nà wǒ de miàn ne? Néng bu néng huàn yí fèn? | Vậy mì của mình đâu? Đổi một phần khác được không?
n50.g | gap        | — | — | —
n51  | liu_yang    | 可以，我给商家打个电话。 | Kěyǐ, wǒ gěi shāngjiā dǎ ge diànhuà. | Được, để mình gọi cho quán.
n51.g | gap        | — | — | —
n52  | liu_yang    | 商家说，十五分钟后送过来，你等一下。 | Shāngjiā shuō, shíwǔ fēnzhōng hòu sòng guòlai, nǐ děng yíxià. | Quán nói mười lăm phút nữa sẽ giao tới, bạn đợi một chút.
n52.g | gap        | — | — | —
n53  | zhang_jing  | 是牛肉面！太好了，我饿死了！ | Shì niúròu miàn! Tài hǎo le, wǒ è sǐ le! | Là mì bò! Tuyệt quá, mình đói chết mất!
n53.g | gap        | — | — | —
n54  | liu_yang    | 这次不用再付钱，是商家重新送的。 | Zhè cì bú yòng zài fù qián, shì shāngjiā chóngxīn sòng de. | Lần này không cần trả thêm tiền, quán giao lại mà.
n54.g | gap        | — | — | —
n55  | zhang_jing  | 我原来的面二十二块，我转你。 | Wǒ yuánlái de miàn èrshí'èr kuài, wǒ zhuǎn nǐ. | Phần mì ban đầu của mình hai mươi hai tệ, mình chuyển cho bạn.
n55.g | gap        | — | — | —
n56  | liu_yang    | 今天我请你，别客气。 | Jīntiān wǒ qǐng nǐ, bié kèqi. | Hôm nay mình mời, đừng khách sáo.
n56.g | gap        | — | — | —
n57  | zhang_jing  | 那个麻婆豆腐饭很辣，你吃吗？ | Nàge mápó dòufu fàn hěn là, nǐ chī ma? | Hộp cơm đậu hũ ma bà kia cay lắm, bạn có ăn không?
n57.g | gap        | — | — | —
n58  | liu_yang    | 吃啊，我最喜欢吃辣的。 | Chī a, wǒ zuì xǐhuan chī là de. | Có chứ, mình thích ăn cay nhất.
n58.g | gap        | — | — | —
```


## Changes (step 11)

- q6: 张静有没有说要吃什么？→刘洋问她吃什么，张静一开始怎么说？ (Claude q6, DeepSeek q6)
- q6a: 没有。→随便。 (Claude q6, DeepSeek q6)
- q6b: 她没说，她说随便，你点吧。→她说随便，让刘洋点。 (Claude q6, DeepSeek q6)
- q8/q8b: 张静点的是什么？/张静点的是牛肉面。→刘洋给张静点的是什么？/刘洋给张静点的是牛肉面。 (Claude q8, DeepSeek q8)
- q16: 他们等了多久？→第一份外卖等了多久？ (Claude q16, DeepSeek q16)
- q22/q22a/q22b: ambiguous box question and answer→第一次张静的盒子里是什么？/麻婆豆腐饭。/第一次是麻婆豆腐饭。 (Claude q22, DeepSeek q22)
- q23/q23b: 那盒菜辣不辣？/那盒菜很辣。→张静那盒辣不辣？/张静那盒很辣。 (Claude q23, DeepSeek q23)
- q46: 张静最后付钱了吗？→张静要付钱吗？ (DeepSeek q46)
- v12: 他说“随便”，就是他不知道，你给他选一个。→他说“随便”，就是吃什么都可以，你来选。 (Claude v12, DeepSeek v12)
- v14: 我不知道，你呢？→随便，你点吧。 (Claude v13–v14, DeepSeek v13–v14)
- v63: 送是把东西给你，错是不对。→送，就是把东西送到你这儿；错，就是不对。 (DeepSeek v63 note applied as typo/naturalness fix)
- v71: 订单上写着你要的菜，还有多少钱。→订单上写着你要的菜，还有一共多少钱。 (DeepSeek v71)
- v78: 这个不对，你要那个，就叫换。→这个不对，你想换成那个，这就叫换。 (DeepSeek v78)
- v91: 十二点了，你还没吃饭，你就很饿。→十二点了还没吃饭，就会很饿。 (DeepSeek v91)
- v95: 味道不错，就是很好吃。→味道不错，就是挺好吃的。 (DeepSeek v95)
- v106: 他说“别客气”，就是你不用谢他，也不用还钱。→他说“别客气”，就是真不用你还，你别推了。 (Claude v106, DeepSeek v106 note)
- hs1v: 删除“ ngang tuổi nhau”。 (Claude hs1v)
- hs6v/hs7v/hs8v/hs18v/hs35v: corrected Hán-Việt, Vietnamese naturalness, and premature spicy-food cue. (Claude/DeepSeek)
- hp4v/hp12v/hp15v: corrected Vietnamese explanations. (Claude/DeepSeek)
- l40/l58: repeat announcement→接着听，下面几个版本不一样，都不是真的故事。 (Claude l58, DeepSeek l58)
- l60: 她也点了红烧肉饭。→刘洋也给她点了红烧肉饭。 (Claude l60)
- l63: natural retelling and wait wording corrected. (DeepSeek l63 note, Claude l63 note)
- l73: 可是→面一来。 (Claude l73)
- l44: added 外卖小哥 hearing. (DeepSeek exposure fix)
- 04b: added PM authorization for announced Nghe detail changes. (PM instruction)
- l64 (PM): 这次那个辣的盒子是刘洋的。→这次刘洋给自己点的是麻婆豆腐饭。 (Claude l64–l68 block; Codex missed it)
- Not applied: DeepSeek l71–l72 and l76–l78 blocks, per PM decision to keep announced variants; DeepSeek exposure-table block, per PM script/counting decision.

## Open doubts

### Hiểu
1. **Speaker token.** Rule 3 writes `narrator`, `05_story.md` writes `coach_zh`. `story/modes.py`
   only accepts `narrator` (and maps it to `coach_zh`), so every narrator row here says
   `narrator`. But `modes.py:SPEAKERS` is still the lesson-2 set `{narrator, wang_fang,
   chen_dong, coach_vi, gap}` — it will reject `zhang_jing` and `liu_yang`, so the importer needs
   lesson-3 names (or a per-lesson set) before `09_modes.md` parses.
2. **No `## Hiểu` heading in this file**, because the spec says each mode file is "one ``` block
   of rows, then `## Open doubts`" and `parse_modes` keys sections on `## <Mode>`. If the join
   script copies blocks rather than adding headings, it must insert `## Hiểu` before these rows.
3. **Length: 161 rows** (3 intro + 1 + 74 story/gloss + 45 phrase + 1 + 37 re-listen) — at the
   6–8 min ceiling or slightly over, since 37 story lines get a Vietnamese gloss each. If the
   PM's duration estimate is over 8 min, the cheapest cuts are: drop the `hr…` re-listen pass
   (−38 rows), or gloss only the hard lines (`hs1`, `hs13`–`hs15`, `hs20`–`hs26`, `hs31`–`hs32`)
   and leave the short ones (`hs5`, `hs7`, `hs11`, `hs16`) as Chinese only (−33 rows).
4. **No `gap` rows.** Rule 3 defines the gap form and the speaker list includes `gap`, but the
   Hiểu plan (intro → story line by line → phrase gloss → story again) names no place to answer,
   so none were added. If you want a comprehension check, the natural slot is after `hs35v`:
   `hp0 | narrator | 你回答。 | Nǐ huídá. | Bạn trả lời.` + `hp0.g | gap | — | — | —`, then 2–3
   questions taken from the `04b` canonical short answers.
5. **`hp1s` and `hp2s` are both s2** — P1 (你想吃什么) and P2 (我来点外卖) only occur together in
   that one story line, so s2 is heard four times in this file. Alternative if that is wasteful:
   P2's repeat line → s3 (随便，你点吧，麻烦你了。) and give P1 the full s2.
6. **One Chinese word outside the tiers, in an instruction line only**: 故事 (gùshi) in 我们听故事。
   and 再听一遍。 — both are rule 7's fixed lines, so they were used as written; 听 is Coach, 遍
   and 故事 are not in any tier. Counted once each here (2 hearings), flagging per `05` doubt 3.
7. **Vietnamese rows carry no Chinese characters** (rule 5), so dish names appear as "mì bò",
   "cơm thịt kho", "đậu hũ ma bà" and the Chinese word is referred to as "câu này / chữ này" or by
   Hán-Việt (tống thác, ngoại mại, thương gia, tùy tiện, hoán, thỉnh, nhất khẩu, đính đơn, khối,
   nhất cộng đa thiểu tiền, hoàn thị, ma phiền). Hán-Việt follows `03_core.md` §2.2, but `04b`
   doubt 2 (吃 ngật/cật, 面 miến/diện, 豆腐 đậu hủ/đậu phụ) is still open — the prose here uses
   "ăn / mì / đậu hũ"; switch those three if step 10 decides otherwise.
8. **The `hr…` pass fills the Tiếng Việt column as on-screen text** although the audio is
   Chinese-only (rule 4 read as applying to the column, not the audio). Set those cells to `—` if
   the re-listen pass must be transcript-free.
9. **Pinyin as pronounced** throughout, copied from the story (`bú shì`, `yí ge`, `yíxià`,
   `kèqi`, `guòlai`) — `03_core` doubt 1 / `04b` doubt 1 still open; a switch to underlying tones
   touches only the pinyin column.
10. **Facts checked against `04b`**: every gloss keeps 刘洋点对了 / 商家送错了 (F8, F12 — the
    Vietnamese at `hs20v` says 张静 *believes* he got it wrong, never that he did); payment is
    app-only (F7, `hs10v`, `hs31v`); one phone call, one free box (F15, `hs25v`, `hs29v`); box
    count 两个 then 一个 (F18, `hs13v`, `hs27v`); only the shop's reported words, nobody else
    speaks (F19, `hs26v`); 刘洋 eats the spicy box (F6, F15, `hs34v`, `hs35v`).
### Từ vựng
1. **16 items, spec says ~12–15.** P7/P8 (你点的是什么 / 我点的是…) could not be dropped: that is the
   beat where the wrong dish is found (`s20`–`s21`, F8, F12), so it is one merged item. If the PM
   needs 15, the cheapest cut is item 14 (饿死了, v89–v96, 8 rows) — it loses the lift of 饿 and
   好吃; merging items 1–2 (both come from `s2`) is the other option.
2. **Two merges hide a drill.** Item 8 teaches P11 不吃辣 and P10 太辣了 but drills only 不吃辣;
   item 11 teaches 订单 and 备注 but drills only 订单; item 9 drills only 你点的是什么, not
   我点的是…; item 16 drills only 今天我请你, not 别客气. Splitting any of them costs 4–6 rows.
3. **Pinyin is as-pronounced** (`yí fèn`, `bú duì`, `chī bu liǎo`, `yào bu yào`, `děng yíxià`,
   `shí'èr diǎn`, `hézi`, `máfan`, `háishi`, `guòlai`), matching `05_story.md`. `03_core` doubt 1 is
   still open: if the pipeline wants underlying tones, only the pinyin column changes here.
4. **New words added by the explanations** (rule 1 allows them, all everyday, none enters the
   tiers, none appears in a story example line): 出去 句 选 让 中间 辣椒 跟 东西 哪个 的时候 卖 店
   次 帮 还 谢 顿. Step 10 needs Hán-Việt only if they are counted; my call is not to count them.
5. **公司 and 同事 stay at 1 hearing** (story `s1`). No item here needs them naturally; Nghe
   (`s1` retelling) or Phản xạ (谁和谁是同事?) should lift them, or the PM accepts them as
   lesson-wide low items. Coach words 听 / 看 / 问 / 回答 / 说 are now all present (v1, v11, v30,
   v56, v57, v87); 懂 is still 0 — it belongs to Hiểu.
6. **v12 forward-references 随便** before item 3 teaches it, on purpose: it tells the learner how to
   react to the answer, and it keeps item 2 self-contained. If the review prefers strict order, move
   v12 into item 3 (after v18) — no other row changes.
7. **v94–v95 keep 味道** (off-tier in the story, `s30`) and bridge it to 好吃, which was at 1
   hearing. If step 10 flags 味道 as stray, v94 can be replaced by 张静吃上了牛肉面，很好吃。 — but
   then it no longer matches the approved `s30` wording.
8. **Length:** 108 spoken rows + 16 gaps ≈ 6.5–7 min at ~3.3 s/row and ~3 s per gap, inside the
   6–10 min band but near the bottom. If the TTS runs fast and the PM's duration check comes under
   6 min, the cheap fix is a second gap row after each drill (`<ref>.g2`), not more items.
### Nghe
1. **二十分钟 in version 3 (`l71`–`l72`) vs `04b` "Never allowed: any changed number".** The Nghe
   spec names "the shop says 二十分钟" as a changed-detail idea, so it is used **only inside version 3**,
   which `l69` and `l58`/`l81` announce as not the real story; the arithmetic stays consistent
   (十二点 + 二十分钟 = 十二点二十, 五分钟 more than F17's 十五分钟). The canonical retelling (`l40`–`l57`)
   keeps every F17 number. Version 1 states no total price, to avoid inventing a new sum for two
   红烧肉饭. Drop version 3 (or renumber it to 十五分钟 with a different detail) if the PM wants `04b`
   read strictly.
2. **Version 4 (`l75`–`l80`) has 张静 eating the spicy dish**, which F16 bans *in the story*; kept
   because the Nghe spec names it as a changed-detail idea and the version is announced as fake.
   Delete `l75`–`l80` if F16 is meant to cover Nghe variants too.
3. **外卖小哥 is now at 0 in the story.** The step-8 rewrite of `s13` (`刘洋拿回来两个盒子`) removed it,
   but `05_story.md`'s word table still counts it once at `s13`. I did not reintroduce it here (F9 only
   needs "he goes down to get it", `l44`); Từ vựng / Phản xạ should carry it, or the PM fixes the table.
4. **Pinyin convention still open** (`04b` doubt 1, `05_story` doubt 1): written as pronounced
   (`yí biàn`, `bú yòng`, `yíhuìr`, `shí'èr diǎn`). If the pipeline wants underlying tones, only the
   pinyin column changes.
5. **Everyday words outside the tiers**, used only in narration and variants: 所以 可是 因为 马上 一会儿
   更 挺 全 怕 变 真的 版本 主人 (dropped) 下去 才 别的 一点. All are simple spoken-Chinese connectives
   needed to retell; flag if step 10 counts them as stray.
6. **Runtime.** Two full stories (37 rows each) + 18-row retelling + 4 versions ≈ 8 min at normal
   coach pace, inside the 6–10 min band. If the PM measures longer, cut version 3 first, then the second
   story (`l83`–`l119`).
7. **Low-exposure Core words lifted here** (`05_story` doubt 3): 手机 拿 打开 盒子 订单 备注 等 转 请
   付钱 换 饿 水 喝 好吃/味道 each get 1–3 extra hearings in `l40`–`l80`. Still low after Nghe:
   午饭 米饭 菜 麻烦 公司 同事 外卖小哥 — Phản xạ and Nói need to carry those.
### Phản xạ
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
### Nói
1. **Length.** 115 rows (58 spoken + 57 gaps). The spec's shadowing rule is "every zhang_jing /
   liu_yang story line" = 29 lines (12 张静 + 17 刘洋, counting the step-8 additions s1b/s23b —
   s1b is narrator, so 29 dialogue lines), plus 8 build-ups at 3–4 steps. With a 3 s gap that is
   ~9–10 min, above the 5–8 min band. Cheapest trim: cut the four shortest build-ups (n5–n7 钱,
   n12–n13) and shadow only the 张静 lines, keeping 刘洋's in the story pass. PM to decide; I did
   not trim because the spec names both blocks.
2. **打电话 is drilled only as the story's split form.** s25 says 我给商家打个电话, so the tail chunk
   is 打个电话 (n26), not the tier's base 打电话 (`03_core.md` 2.2). The Core word therefore never
   gets a clean isolated hearing in this mode; if the PM wants the base form heard, n26 → 打电话。
   and n27 must become 给商家打电话 → 可以，我给商家打个电话 (4 steps, one extra row).
3. **Chunk rows are not story lines.** n2–n28 build sentences from tails (吃面 → 想吃面 → 我今天
   想吃面), so their 中文 cells are substrings, not the verbatim rows the pipeline may expect. The
   full-line rows (n4, n11, n15, n19, n22, n25, n28) and the whole shadowing block are copied
   character-for-character from `05_story.md`. If a row must match a script line, the chunk rows
   need a flag in the joiner.
4. **Which 8 lines got the build-up** — P5 answer (s5), P14 (s9), P4 (s6), P1+P2 (s2), P11 (s19),
   P9 (s23b), P12 (s24), P13 (s25): the lines a learner has to *produce* to order lunch and fix a
   wrong delivery. Not built up: P3 随便，你点吧, P6 外卖到了, P7/P8 你点的是什么/我点的是, P10 太辣了,
   P15 我请你 — each still gets one shadow rep (s3, s12, s20/s21, s17, s32), so their story hearing
   count stays 1 and the low-item problem in `05_story.md` doubt 3 is not solved by this mode.
5. **Opener.** Rule 6 wants a fixed line naming the mode; rule 7's list gives 跟我说。, used as n1.
   The mid-file bridge 你说。 (n29) is also from rule 7. If the joiner expects a longer mode-naming
   line (e.g. 我们来说一说。), n1 is the only row to change.
6. **s23 is the hardest shadow** — 18 characters with the quoted 备注 (“不要辣”), and `05_story.md`
   doubt 4 already questions whether it is repeatable. It is kept here because F8's evidence line is
   worth speaking; drop it if the learner stumbles on every pass.
7. **转 (s31) and 请 (s32) only appear once each here.** Both are Core words with story count 1
   (`05_story.md` doubt 3); this mode could lift them with a ninth build-up (我转你 → 不用不用，我转你)
   if the PM wants the band met. Not added — the spec caps the block at 8 lines.
