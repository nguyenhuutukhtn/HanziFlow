# 09 — Nghe mode (lesson 3 · 午饭点错菜)

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
l44 | narrator     | 三十分钟以后，十二点，外卖来了，刘洋下去拿。 | Sānshí fēnzhōng yǐhòu, shí'èr diǎn, wàimài lái le, Liú Yáng xiàqù ná. | Ba mươi phút sau, mười hai giờ, đồ ăn tới, Lưu Dương đi xuống lấy.
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
l58 | narrator     | 再听一遍，下面几个版本不一样，不是真的故事。 | Zài tīng yí biàn, xiàmiàn jǐ ge bǎnběn bù yíyàng, bú shì zhēn de gùshi. | Nghe lại lần nữa nhé, mấy bản dưới đây khác nhau, không phải câu chuyện thật.
l59 | narrator     | 第一个版本，这次张静的菜不一样。 | Dì yí ge bǎnběn, zhè cì Zhāng Jìng de cài bù yíyàng. | Bản thứ nhất, lần này món của Tĩnh khác.
l60 | narrator     | 这次张静不想吃面，她也点了红烧肉饭。 | Zhè cì Zhāng Jìng bù xiǎng chī miàn, tā yě diǎn le hóngshāoròu fàn. | Lần này Tĩnh không muốn ăn mì, cô ấy cũng đặt cơm thịt kho.
l61 | narrator     | 两个盒子都是红烧肉饭，都不辣。 | Liǎng ge hézi dōu shì hóngshāoròu fàn, dōu bú là. | Cả hai hộp đều là cơm thịt kho, đều không cay.
l62 | narrator     | 商家没有送错，谁也没有打电话。 | Shāngjiā méiyǒu sòng cuò, shéi yě méiyǒu dǎ diànhuà. | Quán không giao nhầm, chẳng ai gọi điện thoại.
l63 | narrator     | 两个人吃得很好，张静一点也没有等。 | Liǎng ge rén chī de hěn hǎo, Zhāng Jìng yìdiǎn yě méiyǒu děng. | Hai người ăn rất ngon, Tĩnh chẳng phải đợi chút nào.
l64 | narrator     | 第二个版本，这次那个辣的盒子是刘洋的。 | Dì èr ge bǎnběn, zhè cì nàge là de hézi shì Liú Yáng de. | Bản thứ hai, lần này cái hộp cay kia là của Lưu Dương.
l65 | narrator     | 刘洋打开一看，自己的是麻婆豆腐饭，很辣。 | Liú Yáng dǎkāi yí kàn, zìjǐ de shì mápó dòufu fàn, hěn là. | Lưu Dương mở ra xem, hộp của mình là cơm đậu hũ ma bà, rất cay.
l66 | narrator     | 张静的盒子里是牛肉面，一点也没有错。 | Zhāng Jìng de hézi lǐ shì niúròu miàn, yìdiǎn yě méiyǒu cuò. | Trong hộp của Tĩnh là mì bò, chẳng nhầm chút nào.
l67 | narrator     | 刘洋最喜欢吃辣，所以他吃得很快，也不喝水。 | Liú Yáng zuì xǐhuan chī là, suǒyǐ tā chī de hěn kuài, yě bù hē shuǐ. | Lưu Dương thích ăn cay nhất, nên anh ấy ăn rất nhanh, cũng chẳng uống nước.
l68 | narrator     | 这个版本里，谁也不用打电话，谁也不用等。 | Zhège bǎnběn lǐ, shéi yě bú yòng dǎ diànhuà, shéi yě bú yòng děng. | Trong bản này, chẳng ai phải gọi điện, chẳng ai phải đợi.
l69 | narrator     | 第三个版本，这次商家说的话不一样。 | Dì sān ge bǎnběn, zhè cì shāngjiā shuō de huà bù yíyàng. | Bản thứ ba, lần này lời quán nói thì khác.
l70 | narrator     | 张静的盒子里还是麻婆豆腐饭，还是那么辣。 | Zhāng Jìng de hézi lǐ háishi mápó dòufu fàn, háishi nàme là. | Trong hộp của Tĩnh vẫn là cơm đậu hũ ma bà, vẫn cay như thế.
l71 | narrator     | 刘洋打电话问商家，商家说，二十分钟送过来。 | Liú Yáng dǎ diànhuà wèn shāngjiā, shāngjiā shuō, èrshí fēnzhōng sòng guòlai. | Lưu Dương gọi điện hỏi quán, quán nói hai mươi phút nữa giao qua.
l72 | narrator     | 十二点二十，面才来，张静多等了五分钟。 | Shí'èr diǎn èrshí, miàn cái lái, Zhāng Jìng duō děng le wǔ fēnzhōng. | Mười hai giờ hai mươi mì mới tới, Tĩnh phải đợi thêm năm phút.
l73 | narrator     | 她更饿了，可是面来了，她还是吃得很高兴。 | Tā gèng è le, kěshì miàn lái le, tā háishi chī de hěn gāoxìng. | Cô ấy càng đói hơn, nhưng mì tới rồi, cô ấy vẫn ăn rất vui.
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

## Open doubts

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
