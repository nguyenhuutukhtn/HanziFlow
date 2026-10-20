> Lesson 1 story source. Build: mandarin-course build-lesson lessons/src/lesson_001.story.md
> Story: office takeout. Anna thinks it is Li Ming's, Li Ming thinks it is Anna's, it is a colleague's.
> Present time only. Grammar: 是/不是, 还是, 的 for whose.
> Row shapes and defaults: src/mandarin_course/story/blocks.py. `> lines` are notes.

lesson_id: lesson_001
title: Đồ ăn mang về của đồng nghiệp
level: absolute_beginner
version: 3
goals: Hear whose takeout it is | Answer 是 and 不是 | Choose with 还是 | Answer 这是谁的
patterns: 这是外卖。 | 这是……的吗？ | 不是。 | 这是同事的。 | 这是谁的？ | ……还是……？ | 袋子上有……的名字。

# guided
coach_phrases: 我们一起听小故事吧。 | 我们先听短句。 | 这一轮还有越南语。

## activity Nghe chuyện
## say
roles | 我们一起听小故事吧。 | wǒmen yìqǐ tīng xiǎo gùshi ba. | Mình cùng nghe một câu chuyện ngắn nhé. | 500
voices | 我先用中文讲。再用越南语说意思。 | wǒ xiān yòng Zhōngwén jiǎng. zài yòng Yuènányǔ shuō yìsi. | Mình kể bằng tiếng Trung trước. Rồi nói nghĩa bằng tiếng Việt. | 700
scene_bag | 桌上有一袋外卖。这不是安娜的桌子。这不是李明的桌子。安娜和李明看袋子。 | zhuō shàng yǒu yí dài wàimài. zhè bú shì Ānnà de zhuōzi. zhè bú shì Lǐ Míng de zhuōzi. Ānnà hé Lǐ Míng kàn dàizi. | Trên bàn có một túi đồ ăn mang về. Đây không phải bàn của Anna. Đây không phải bàn của Lý Minh. Anna và Lý Minh nhìn túi. | 700
listen_first | 我们先听。不用回答。 | wǒmen xiān tīng. bú yòng huídá. | Mình nghe trước. Chưa cần trả lời. | 500
## story prefix=s1 from=story

## activity Giải nghĩa
## say
word_cue | 我们一起听这些词。 | wǒmen yìqǐ tīng zhèxiē cí. | Mình cùng nghe những từ này.
word_lead | 听不懂也没关系。 | tīng bù dǒng yě méi guānxi. | Nghe chưa hiểu cũng không sao. | 700
## gloss prefix=w
waimai | coach_zh | 外卖。 | wàimài. | Đồ ăn mang về.
bushi | coach_zh | 不是。 | bú shì. | Không phải.
tongshi | coach_zh | 同事。 | tóngshì. | Đồng nghiệp.
shei | coach_zh | 谁的？ | shuí de? | Của ai?
mingzi | coach_zh | 名字。 | míngzi. | Tên.
## say
map_lead | 我们听一句，再听意思。 | wǒmen tīng yí jù, zài tīng yìsi. | Mình nghe một câu, rồi nghe nghĩa.
## gloss
map_bag | coach_zh | 桌上有一袋外卖。 | zhuō shàng yǒu yí dài wàimài. | Trên bàn có một túi đồ ăn mang về.
map_unknown | coach_zh | 安娜和李明不知道是谁的。 | Ānnà hé Lǐ Míng bù zhīdào shì shuí de. | Anna và Lý Minh không biết của ai.
map_look | coach_zh | 安娜看袋子，李明也看袋子。 | Ānnà kàn dàizi, Lǐ Míng yě kàn dàizi. | Anna nhìn túi. Lý Minh cũng nhìn túi.
map_food | coach_zh | 这是外卖。 | zhè shì wàimài. | Đây là đồ ăn mang về.
map_desk_an | coach_zh | 这不是安娜的桌子。 | zhè bú shì Ānnà de zhuōzi. | Đây không phải bàn của Anna.
map_desk_lm | coach_zh | 这不是李明的桌子。 | zhè bú shì Lǐ Míng de zhuōzi. | Đây không phải bàn của Lý Minh.
map_who | coach_zh | 这是谁的？ | zhè shì shuí de? | Đây là của ai?
map_ask | anna | 这是李明的吗？ | zhè shì Lǐ Míng de ma? | Đây là của Lý Minh phải không?
map_no | li_ming | 不是。 | bú shì. | Không phải.
map_neither | coach_zh | 不是安娜的。不是李明的。 | bú shì Ānnà de. bú shì Lǐ Míng de. | Không phải của Anna. Không phải của Lý Minh.
map_name | coach_zh | 袋子上有同事的名字。 | dàizi shàng yǒu tóngshì de míngzi. | Trên túi có tên đồng nghiệp.
map_whose | coach_zh | 这是同事的。 | zhè shì tóngshì de. | Đây là của đồng nghiệp.
## say
s2_lead | 我们再听一次故事吧。 | wǒmen zài tīng yí cì gùshi ba. | Mình nghe lại chuyện một lần nữa nhé.
## story prefix=s2 from=story

## activity Hỏi đáp
## teach prefix=sig_ma
rule | 句子最后的“吗”，表示这是“是”或者“不是”的问题。 | jùzi zuìhòu de “ma”, biǎoshì zhè shì “shì” huòzhě “bú shì” de wèntí. | Từ này ở cuối câu cho biết đây là câu hỏi có hoặc không.
ex | 这是外卖吗？ | zhè shì wàimài ma? | Đây là đồ ăn mang về phải không?
ans | 是。 | shì. | Phải.
## teach prefix=sig_who
rule | 听到“谁的”，你说这是谁的。 | tīng dào “shuí de”, nǐ shuō zhè shì shuí de. | Nghe thấy cụm này, bạn nói đây là của ai.
ex | 这是谁的？ | zhè shì shuí de? | Của ai?
ans | 同事的。 | tóngshì de. | Của đồng nghiệp.
## teach prefix=sig_or
rule | 听到“还是”，你选这一样，或者那一样。 | tīng dào “háishi”, nǐ xuǎn zhè yí yàng, huòzhě nà yí yàng. | Nghe thấy từ này, bạn chọn một trong hai.
ex | 这是李明的还是同事的？ | zhè shì Lǐ Míng de háishi tóngshì de? | Của Lý Minh hay của đồng nghiệp?
ans | 同事的。 | tóngshì de. | Của đồng nghiệp.
## say
ask_lead | 我们先听短句。 | wǒmen xiān tīng duǎn jù. | Mình nghe câu ngắn trước nhé.
ask_how | 你答“是”，或者“不是”。 | nǐ dá “shì”, huòzhě “bú shì”. | Bạn chỉ cần trả lời có hoặc không. | 500
## ask prefix=r1 from=qa_core vi praise
## say
r1_name | 名字。 | míngzi. | Tên. | 300
r1_chunk | 同事的名字。李明的名字还是同事的名字。 | tóngshì de míngzi. Lǐ Míng de míngzi háishi tóngshì de míngzi. | Tên đồng nghiệp. Tên Lý Minh hay tên đồng nghiệp.
## ask prefix=r1_long from=qa_long vi
## say
again_cue | 听不懂，再听一次。 | tīng bù dǒng, zài tīng yí cì. | Nghe chưa hiểu thì nghe lại một lần. | 500
r2_go | 我们再听。你答。 | wǒmen zài tīng. nǐ dá. | Mình nghe lại. Bạn trả lời.
r2_food | 外卖。 | wàimài. | Đồ ăn mang về. | 300
## ask prefix=r2a from=qa_core[:2]
## say
r2_who | 谁的。 | shuí de. | Của ai. | 300
## ask prefix=r2b from=qa_core[2:3]
## say
r2_name | 名字。 | míngzi. | Tên. | 300
## ask prefix=r2c from=qa_core[3:]
## say
r2_or | 听到“还是”，你选这一样，或者那一样。 | tīng dào “háishi”, nǐ xuǎn zhè yí yàng, huòzhě nà yí yàng. | Nghe thấy từ này, bạn chọn một trong hai.
## ask prefix=r2_long from=qa_long
## say
s_mid_lead | 我们再听一次故事吧。 | wǒmen zài tīng yí cì gùshi ba. | Mình nghe lại chuyện một lần nữa nhé.
## story prefix=s_mid from=story
## say
r3_lead | 这一轮听中文。 | zhè yì lún tīng Zhōngwén. | Vòng này nghe tiếng Trung.
## ask prefix=r3 from=qa_core+qa_long reverse

## activity Nói theo
## say
say_lead | 你听一遍，再跟我说。 | nǐ tīng yí biàn, zài gēn wǒ shuō. | Bạn nghe một lần, rồi nói theo mình. | 600
## buildup prefix=b_ask speaker=anna
full | 这是李明的吗？ | zhè shì Lǐ Míng de ma? | Đây là của Lý Minh phải không?
c1 | 李明的 | Lǐ Míng de | Của Lý Minh.
c2 | 是李明的 | shì Lǐ Míng de | Là của Lý Minh.

## buildup prefix=b_ts speaker=coach_zh
full | 这是同事的。 | zhè shì tóngshì de. | Đây là của đồng nghiệp.
c1 | 同事的 | tóngshì de | Của đồng nghiệp.
c2 | 是同事的 | shì tóngshì de | Là của đồng nghiệp.

## buildup prefix=b_or speaker=coach_zh
full | 这是安娜的还是同事的？ | zhè shì Ānnà de háishi tóngshì de? | Của Anna hay của đồng nghiệp?
c1 | 同事的 | tóngshì de | Của đồng nghiệp.
c2 | 还是同事的 | háishi tóngshì de | Hay của đồng nghiệp.

## buildup prefix=b_food speaker=coach_zh
full | 这是同事的外卖吗？ | zhè shì tóngshì de wàimài ma? | Đây là đồ ăn mang về của đồng nghiệp phải không?
c1 | 同事的 | tóngshì de | Của đồng nghiệp.
c2 | 同事的外卖 | tóngshì de wàimài | Đồ ăn mang về của đồng nghiệp.

## buildup prefix=b_who speaker=coach_zh
full | 这是谁的外卖？ | zhè shì shuí de wàimài? | Đây là đồ ăn mang về của ai?
c1 | 谁的 | shuí de | Của ai.
c2 | 是谁的 | shì shuí de | Là của ai.

## buildup prefix=b_bag speaker=coach_zh
full | 袋子上有李明的名字还是同事的名字？ | dàizi shàng yǒu Lǐ Míng de míngzi háishi tóngshì de míngzi? | Trên túi có tên Lý Minh hay tên đồng nghiệp?
c1 | 同事的名字 | tóngshì de míngzi | Tên đồng nghiệp.
c2 | 李明的名字还是同事的名字 | Lǐ Míng de míngzi háishi tóngshì de míngzi | Tên Lý Minh hay tên đồng nghiệp.


## shadow prefix=sh from=story
## shadow prefix=pvsh from=pov
## shadow prefix=rtsh from=retold
## shadow prefix=ansh from=anna

## activity Đổi một chi tiết
## say
pov_cue | 我们再听一次吧。 | wǒmen zài tīng yí cì ba. | Mình nghe lại một lần nữa nhé.
pov_lead | 这次袋子上有李明的名字。这是李明的。 | zhè cì dàizi shàng yǒu Lǐ Míng de míngzi. zhè shì Lǐ Míng de. | Lần này trên túi có tên Lý Minh. Đây là của Lý Minh. | 700
## story prefix=pov from=pov
## ask prefix=p1 from=pov vi
## say
p2_go | 我们再听。你答。 | wǒmen zài tīng. nǐ dá. | Mình nghe lại. Bạn trả lời.
## ask prefix=p2 from=pov
## say
p3_lead | 这一轮听中文。 | zhè yì lún tīng Zhōngwén. | Vòng này nghe tiếng Trung.
## ask prefix=p3 from=pov reverse

## activity Túi của Anna
## say
an_lead | 这次袋子上有安娜的名字。这是安娜的。 | zhè cì dàizi shàng yǒu Ānnà de míngzi. zhè shì Ānnà de. | Lần này trên túi có tên Anna. Đây là của Anna. | 700
## story prefix=an_s from=anna
## gloss prefix=an from=anna
## say
an_vi | 这一轮还有越南语。 | zhè yì lún hái yǒu Yuènányǔ. | Vòng này mình vẫn có tiếng Việt nhé.
## ask prefix=a1 from=anna[:10] vi
## say
an_word | 名字。 | míngzi. | Tên. | 300
## ask prefix=a_bag from=anna[10:12] vi
## say
an_chunk | 安娜的名字。同事的名字还是安娜的名字。 | Ānnà de míngzi. tóngshì de míngzi háishi Ānnà de míngzi. | Tên Anna. Tên đồng nghiệp hay tên Anna.
## ask prefix=a2 from=anna[12:] vi
## ask prefix=a4 from=anna_more vi
## say
a_go | 我们再听。你答。 | wǒmen zài tīng. nǐ dá. | Mình nghe lại. Bạn trả lời.
## ask prefix=a_mid from=anna
## ask prefix=a_mid2 from=anna_more
## say
a_test_lead | 这一轮听中文。 | zhè yì lún tīng Zhōngwén. | Vòng này nghe tiếng Trung.
## story prefix=an_s2 from=anna
## ask prefix=a3 from=anna reverse
## ask prefix=a3b from=anna_more reverse

## activity Kể lại
## say
rt_lead | 我们用别的话说一次。 | wǒmen yòng bié de huà shuō yí cì. | Mình kể lại bằng lời khác. | 500
## gloss prefix=rt from=retold
## story prefix=rt2 from=retold
## say
k_ask | 这一轮还有越南语。 | zhè yì lún hái yǒu Yuènányǔ. | Vòng này mình vẫn có tiếng Việt nhé.
## ask prefix=k1 from=retell vi
## say
k2_go | 我们再听。你答。 | wǒmen zài tīng. nǐ dá. | Mình nghe lại. Bạn trả lời.
## ask prefix=k2 from=retell
## say
k3_lead | 这一轮听中文。 | zhè yì lún tīng Zhōngwén. | Vòng này nghe tiếng Trung.
## ask prefix=k3 from=retell reverse

## activity Nghe lại
## say
close_cue | 我们再听一次故事吧。意思你知道。 | wǒmen zài tīng yí cì gùshi ba. yìsi nǐ zhīdào. | Mình nghe lại chuyện một lần nữa. Nghĩa thì bạn đã biết.
## zh
close_lead | 袋子上有同事的名字。这是同事的。 | dàizi shàng yǒu tóngshì de míngzi. zhè shì tóngshì de. | Trên túi có tên đồng nghiệp. Đây là của đồng nghiệp. | 600
## story prefix=s3 from=story
## zh
close_2 | 用别的话说一次。 | yòng bié de huà shuō yí cì. | Kể lại bằng lời khác.
## story prefix=s4 from=retold
## zh
close_3 | 最后再听一次。 | zuìhòu zài tīng yí cì. | Lần cuối, nghe lại một lần nữa.
## story prefix=s5 from=story
## say
end_today | 今天先听到这里。 | jīntiān xiān tīng dào zhèlǐ. | Hôm nay mình nghe đến đây thôi.
end | 明天我们再听故事。 | míngtiān wǒmen zài tīng gùshi. | Ngày mai mình cùng nghe lại chuyện.

# practice
coach_phrases: 听故事。 | 你来回答。 | 再听一次。
minutes: 5

## activity Nghe chuyện
## say
c1_open | 听故事。桌上有一袋外卖。安娜和李明不知道是谁的。 | tīng gùshi. zhuō shàng yǒu yí dài wàimài. Ānnà hé Lǐ Míng bù zhīdào shì shuí de. | Nghe chuyện. Trên bàn có một túi đồ ăn mang về. Anna và Lý Minh không biết của ai. | 700
## story prefix=p1 from=story
## zh
p2_lead | 再听一次。 | zài tīng yí cì. | Nghe lại lần nữa.
## story prefix=p2 from=story
## zh
p3_lead | 再听一次。 | zài tīng yí cì. | Nghe lại lần nữa.
## story prefix=p3 from=story

# data
> Delivery tags: first occurrence only; repeats stay untagged.
## tags
roles | gentle
scene_bag | warm
listen_first | reassuring
s1_ask_lm | curious
s1_lm_no | surprised
s1_name | surprised
s1_colleague | warm
end_today | reassuring

## lines story
food | coach_zh | 这是外卖。 | zhè shì wàimài. | Đây là đồ ăn mang về. | 1000
who | coach_zh | 这是谁的？ | zhè shì shuí de? | Đây là của ai? | 900
desk_an | coach_zh | 这不是安娜的桌子。 | zhè bú shì Ānnà de zhuōzi. | Đây không phải bàn của Anna. | 1000
desk_lm | coach_zh | 这不是李明的桌子。 | zhè bú shì Lǐ Míng de zhuōzi. | Đây không phải bàn của Lý Minh. | 1000
look | coach_zh | 安娜看袋子，李明也看袋子。 | Ānnà kàn dàizi, Lǐ Míng yě kàn dàizi. | Anna nhìn túi. Lý Minh cũng nhìn túi. | 1400
ask_lm | anna | 这是李明的吗？ | zhè shì Lǐ Míng de ma? | Đây là của Lý Minh phải không? | 900
lm_no | li_ming | 不是。这是安娜的吗？ | bú shì. zhè shì Ānnà de ma? | Không phải. Đây là của Anna phải không? | 900
anna_no | anna | 不是。 | bú shì. | Không phải. | 1400
neither | coach_zh | 不是安娜的。不是李明的。 | bú shì Ānnà de. bú shì Lǐ Míng de. | Không phải của Anna. Không phải của Lý Minh. | 1100
name | coach_zh | 袋子上有同事的名字。 | dàizi shàng yǒu tóngshì de míngzi. | Trên túi có tên đồng nghiệp. | 1200
result | coach_zh | 这是同事的外卖。 | zhè shì tóngshì de wàimài. | Đây là đồ ăn mang về của đồng nghiệp. | 900
colleague | coach_zh | 这是同事的。 | zhè shì tóngshì de. | Đây là của đồng nghiệp. | 1400

## lines pov
pov_food | coach_zh | 这是外卖。 | zhè shì wàimài. | Đây là đồ ăn mang về. | 1000
pov_ask | anna | 这是李明的吗？ | zhè shì Lǐ Míng de ma? | Đây là của Lý Minh phải không? | 900
pov_yes | li_ming | 是。 | shì. | Phải. | 1200
pov_name | coach_zh | 袋子上有李明的名字。 | dàizi shàng yǒu Lǐ Míng de míngzi. | Trên túi có tên Lý Minh. | 1100
pov_his | coach_zh | 这是李明的。 | zhè shì Lǐ Míng de. | Đây là của Lý Minh. | 1000
pov_not | coach_zh | 不是同事的。 | bú shì tóngshì de. | Không phải của đồng nghiệp. | 1400

## lines retold
bag | coach_zh | 桌上有一袋外卖。 | zhuō shàng yǒu yí dài wàimài. | Trên bàn có một túi đồ ăn mang về. | 1000
who | coach_zh | 这是谁的外卖？ | zhè shì shuí de wàimài? | Đây là đồ ăn mang về của ai? | 900
guess_a | coach_zh | 安娜猜是李明的。 | Ānnà cāi shì Lǐ Míng de. | Anna đoán là của Lý Minh. | 900
guess_l | coach_zh | 李明猜是安娜的。 | Lǐ Míng cāi shì Ānnà de. | Lý Minh đoán là của Anna. | 900
desk_an | coach_zh | 这不是安娜的桌子。 | zhè bú shì Ānnà de zhuōzi. | Đây không phải bàn của Anna. | 900
desk_lm | coach_zh | 这不是李明的桌子。 | zhè bú shì Lǐ Míng de zhuōzi. | Đây không phải bàn của Lý Minh. | 900
look | coach_zh | 他们看袋子上的名字。 | tāmen kàn dàizi shàng de míngzi. | Họ nhìn tên trên túi. | 1000
name | coach_zh | 袋子上有同事的名字。 | dàizi shàng yǒu tóngshì de míngzi. | Trên túi có tên đồng nghiệp. | 1100
neither | coach_zh | 不是安娜的。不是李明的。 | bú shì Ānnà de. bú shì Lǐ Míng de. | Không phải của Anna. Không phải của Lý Minh. | 1100
result | coach_zh | 这是同事的。 | zhè shì tóngshì de. | Đây là của đồng nghiệp. | 1200

## lines anna
food | coach_zh | 这是外卖。 | zhè shì wàimài. | Đây là đồ ăn mang về. | 900
ask | anna | 这是安娜的吗？ | zhè shì Ānnà de ma? | Đây là của Anna phải không? | 900
yes | li_ming | 是。 | shì. | Phải. | 1000
name | coach_zh | 袋子上有安娜的名字。 | dàizi shàng yǒu Ānnà de míngzi. | Trên túi có tên Anna. | 1100
hers | coach_zh | 这是安娜的。 | zhè shì Ānnà de. | Đây là của Anna. | 900
not_lm | coach_zh | 不是李明的。 | bú shì Lǐ Míng de. | Không phải của Lý Minh. | 800
not_ts | coach_zh | 不是同事的。 | bú shì tóngshì de. | Không phải của đồng nghiệp. | 1200

## questions qa_core
food | 这是外卖吗？ | zhè shì wàimài ma? | Đây là đồ ăn mang về phải không? | 是 | 5000 | Đúng rồi.
lm | 这是李明的吗？ | zhè shì Lǐ Míng de ma? | Đây là của Lý Minh phải không? | 不是
who | 这是谁的？ | zhè shì shuí de? | Của ai? | 同事的。 | tóngshì de. | Của đồng nghiệp.
has | 袋子上有名字吗？ | dàizi shàng yǒu míngzi ma? | Trên túi có tên phải không? | 有
name_ts | 袋子上有同事的名字吗？ | dàizi shàng yǒu tóngshì de míngzi ma? | Trên túi có tên đồng nghiệp phải không? | 有 | 5000 | Tốt.

## questions qa_long
name | 袋子上有李明的名字还是同事的名字？ | dàizi shàng yǒu Lǐ Míng de míngzi háishi tóngshì de míngzi? | Trên túi có tên Lý Minh hay tên đồng nghiệp? | 同事的名字。 | tóngshì de míngzi. | Tên đồng nghiệp. | 5500

## questions pov
pq_lm | 这是李明的吗？ | zhè shì Lǐ Míng de ma? | Đây là của Lý Minh phải không? | 是 | 4500
pq_ts | 这是同事的吗？ | zhè shì tóngshì de ma? | Đây là của đồng nghiệp phải không? | 不是 | 4500
pq_name | 袋子上有李明的名字吗？ | dàizi shàng yǒu Lǐ Míng de míngzi ma? | Trên túi có tên Lý Minh phải không? | 有 | 4500
pq_name_no | 袋子上有同事的名字吗？ | dàizi shàng yǒu tóngshì de míngzi ma? | Trên túi có tên đồng nghiệp phải không? | 没有 | 4500
pq_who | 这是谁的？ | zhè shì shuí de? | Của ai? | 李明的。 | Lǐ Míng de. | Của Lý Minh.
pq_or | 这是李明的还是同事的？ | zhè shì Lǐ Míng de háishi tóngshì de? | Của Lý Minh hay của đồng nghiệp? | 李明的。 | Lǐ Míng de. | Của Lý Minh. | 5500
pq_or_name | 袋子上有李明的名字还是同事的名字？ | dàizi shàng yǒu Lǐ Míng de míngzi háishi tóngshì de míngzi? | Trên túi có tên Lý Minh hay tên đồng nghiệp? | 李明的名字。 | Lǐ Míng de míngzi. | Tên Lý Minh. | 5500

## questions anna
an | 这是安娜的吗？ | zhè shì Ānnà de ma? | Đây là của Anna phải không? | 是
ts | 这是同事的吗？ | zhè shì tóngshì de ma? | Đây là của đồng nghiệp phải không? | 不是
lm | 这是李明的吗？ | zhè shì Lǐ Míng de ma? | Đây là của Lý Minh phải không? | 不是
food_an | 外卖是安娜的吗？ | wàimài shì Ānnà de ma? | Đồ ăn mang về là của Anna phải không? | 是
food_ts | 外卖是同事的吗？ | wàimài shì tóngshì de ma? | Đồ ăn mang về là của đồng nghiệp phải không? | 不是
an_food | 这是安娜的外卖吗？ | zhè shì Ānnà de wàimài ma? | Đây là đồ ăn mang về của Anna phải không? | 是
ts_food | 这是同事的外卖吗？ | zhè shì tóngshì de wàimài ma? | Đây là đồ ăn mang về của đồng nghiệp phải không? | 不是
who | 这是谁的？ | zhè shì shuí de? | Của ai? | 安娜的。 | Ānnà de. | Của Anna.
food_who | 外卖是谁的？ | wàimài shì shuí de? | Đồ ăn mang về là của ai? | 安娜的。 | Ānnà de. | Của Anna.
who_food | 这是谁的外卖？ | zhè shì shuí de wàimài? | Đây là đồ ăn mang về của ai? | 安娜的。 | Ānnà de. | Của Anna. | 5200
has | 袋子上有名字吗？ | dàizi shàng yǒu míngzi ma? | Trên túi có tên phải không? | 有
name_an | 袋子上有安娜的名字吗？ | dàizi shàng yǒu Ānnà de míngzi ma? | Trên túi có tên Anna phải không? | 有
or_name | 袋子上有同事的名字还是安娜的名字？ | dàizi shàng yǒu tóngshì de míngzi háishi Ānnà de míngzi? | Trên túi có tên đồng nghiệp hay tên Anna? | 安娜的名字。 | Ānnà de míngzi. | Tên Anna. | 5500
name_ts | 袋子上有同事的名字吗？ | dàizi shàng yǒu tóngshì de míngzi ma? | Trên túi có tên đồng nghiệp phải không? | 没有
name_lm | 袋子上有李明的名字吗？ | dàizi shàng yǒu Lǐ Míng de míngzi ma? | Trên túi có tên Lý Minh phải không? | 没有
or_lm | 这是李明的还是安娜的？ | zhè shì Lǐ Míng de háishi Ānnà de? | Của Lý Minh hay của Anna? | 安娜的。 | Ānnà de. | Của Anna. | 5500
or_ts | 这是同事的还是安娜的？ | zhè shì tóngshì de háishi Ānnà de? | Của đồng nghiệp hay của Anna? | 安娜的。 | Ānnà de. | Của Anna. | 5500
or_food | 外卖是同事的还是安娜的？ | wàimài shì tóngshì de háishi Ānnà de? | Đồ ăn mang về là của đồng nghiệp hay của Anna? | 安娜的。 | Ānnà de. | Của Anna. | 5500

## questions anna_more
or_food_lm | 外卖是安娜的还是李明的？ | wàimài shì Ānnà de háishi Lǐ Míng de? | Đồ ăn mang về là của Anna hay của Lý Minh? | 安娜的。 | Ānnà de. | Của Anna. | 5500
or_food_ts | 外卖是安娜的还是同事的？ | wàimài shì Ānnà de háishi tóngshì de? | Đồ ăn mang về là của Anna hay của đồng nghiệp? | 安娜的。 | Ānnà de. | Của Anna. | 5500
or_an_lm | 这是安娜的还是李明的？ | zhè shì Ānnà de háishi Lǐ Míng de? | Của Anna hay của Lý Minh? | 安娜的。 | Ānnà de. | Của Anna. | 5500
or_food_full_lm | 这是安娜的外卖还是李明的外卖？ | zhè shì Ānnà de wàimài háishi Lǐ Míng de wàimài? | Đồ ăn mang về của Anna hay của Lý Minh? | 安娜的外卖。 | Ānnà de wàimài. | Đồ ăn mang về của Anna. | 5500
or_food_full_ts | 这是安娜的外卖还是同事的外卖？ | zhè shì Ānnà de wàimài háishi tóngshì de wàimài? | Đồ ăn mang về của Anna hay của đồng nghiệp? | 安娜的外卖。 | Ānnà de wàimài. | Đồ ăn mang về của Anna. | 5500
who_name | 袋子上的名字是谁的？ | dàizi shàng de míngzi shì shuí de? | Tên trên túi là của ai? | 安娜的。 | Ānnà de. | Của Anna. | 5200
who_bag | 袋子上有谁的名字？ | dàizi shàng yǒu shuí de míngzi? | Trên túi có tên ai? | 安娜的。 | Ānnà de. | Của Anna. | 5200
or_name_lm | 袋子上有安娜的名字还是李明的名字？ | dàizi shàng yǒu Ānnà de míngzi háishi Lǐ Míng de míngzi? | Trên túi có tên Anna hay tên Lý Minh? | 安娜的名字。 | Ānnà de míngzi. | Tên Anna. | 5500
or_name_ts | 袋子上有安娜的名字还是同事的名字？ | dàizi shàng yǒu Ānnà de míngzi háishi tóngshì de míngzi? | Trên túi có tên Anna hay tên đồng nghiệp? | 安娜的名字。 | Ānnà de míngzi. | Tên Anna. | 5500
name_an | 这是安娜的名字吗？ | zhè shì Ānnà de míngzi ma? | Đây là tên Anna phải không? | 是
name_lm | 这是李明的名字吗？ | zhè shì Lǐ Míng de míngzi ma? | Đây là tên Lý Minh phải không? | 不是
name_ts | 这是同事的名字吗？ | zhè shì tóngshì de míngzi ma? | Đây là tên đồng nghiệp phải không? | 不是

## questions retell
food | 这是外卖吗？ | zhè shì wàimài ma? | Đây là đồ ăn mang về phải không? | 是
who | 这是谁的？ | zhè shì shuí de? | Của ai? | 同事的。 | tóngshì de. | Của đồng nghiệp.
food_ts | 外卖是同事的吗？ | wàimài shì tóngshì de ma? | Đồ ăn mang về là của đồng nghiệp phải không? | 是
or_ts | 这是同事的还是安娜的？ | zhè shì tóngshì de háishi Ānnà de? | Của đồng nghiệp hay của Anna? | 同事的。 | tóngshì de. | Của đồng nghiệp. | 5500

