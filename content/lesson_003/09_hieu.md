# 09 hieu — Hiểu mode

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
hs1v  | coach_vi   | — | — | Hai người này làm chung một công ty, ngang tuổi nhau, gọi nhau là "mình" với "bạn".
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
hs6v  | coach_vi   | — | — | Hỏi có muốn một phần mì bò không. "Mì bò" là món mì với thịt bò, người Bắc gọi thịt bò là "ngưu nhục".
hs7   | zhang_jing | 好啊，我要牛肉面。 | Hǎo a, wǒ yào niúròu miàn. | Được đó, mình gọi mì bò.
hs7v  | coach_vi   | — | — | Đồng ý luôn: mình lấy mì bò. Hai chữ đầu là kiểu trả lời rất miệng, gần như "được đó".
hs8   | liu_yang   | 那我点红烧肉饭，二十六块。 | Nà wǒ diǎn hóngshāoròu fàn, èrshíliù kuài. | Vậy mình đặt cơm thịt kho, hai mươi sáu tệ.
hs8v  | coach_vi   | — | — | Còn anh ấy lấy cơm thịt kho, hai mươi sáu tệ. Chữ này đọc là "khối", tức là đồng tiền mình, nói tắt cho nhanh.
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
hs18v | coach_vi   | — | — | Lưu Dương không tin: cay cỡ đó luôn hả? Anh ấy ăn cay giỏi mà.
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
hs35v | coach_vi   | — | — | Lưu Dương ăn xong cơm thịt kho, rồi ăn luôn hộp đậu hũ cay — hai người cùng công ty, chia nhau một hộp là bình thường.
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

## Open doubts

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
