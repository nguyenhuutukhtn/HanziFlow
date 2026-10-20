package com.hanziflow.shared.transcript

import com.hanziflow.shared.ReferenceLine
import com.hanziflow.shared.Token
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TranscriptRenderingTest {
    private val zhLine = ReferenceLine(
        id = "s1_food", type = "narration", language = "zh", speaker = "coach_zh",
        text = "这是外卖。", pinyin = "zhè shì wàimài.", translation = "Đây là đồ ăn mang về.",
        tokens = listOf(
            Token(hanzi = "这", pinyin = "zhè"),
            Token(hanzi = "是", pinyin = "shì"),
            Token(hanzi = "外卖", pinyin = "wàimài", itemId = "w_waimai"),
            Token(hanzi = "。"),
        ),
    )
    private val viLine = zhLine.copy(id = "s1_food_vi", language = "vi", text = "Đây là đồ ăn mang về.", tokens = null)
    private val modelAnswerLine = zhLine.copy(id = "r1_food_a", type = "model_answer")

    @Test
    fun viLine_hiddenWhenToggleOff() {
        val decision = TranscriptRendering.decide(viLine, TranscriptViewMode.HANZI_ONLY, showVietnamese = false, modelAnswerRevealed = true)
        assertFalse(decision.lineVisible)
    }

    @Test
    fun bcp47ViLine_isTreatedAsVietnamese() {
        val published = viLine.copy(language = "vi-VN")
        val off = TranscriptRendering.decide(published, TranscriptViewMode.HANZI_WITH_PINYIN, showVietnamese = false, modelAnswerRevealed = true)
        val on = TranscriptRendering.decide(published, TranscriptViewMode.HANZI_WITH_PINYIN, showVietnamese = true, modelAnswerRevealed = true)
        assertFalse(off.lineVisible)
        assertTrue(on.lineVisible)
        assertFalse(on.showTranslation)
    }

    @Test
    fun viLine_shownWhenToggleOn() {
        val decision = TranscriptRendering.decide(viLine, TranscriptViewMode.HANZI_ONLY, showVietnamese = true, modelAnswerRevealed = true)
        assertTrue(decision.lineVisible)
    }

    @Test
    fun zhLine_showsTranslationOnlyWhenToggleOn() {
        val off = TranscriptRendering.decide(zhLine, TranscriptViewMode.HANZI_ONLY, showVietnamese = false, modelAnswerRevealed = true)
        val on = TranscriptRendering.decide(zhLine, TranscriptViewMode.HANZI_ONLY, showVietnamese = true, modelAnswerRevealed = true)
        assertFalse(off.showTranslation)
        assertTrue(on.showTranslation)
    }

    @Test
    fun zhLine_translationSuppressedWhenNextViLineSaysTheSame() {
        val next = viLine.copy(
            language = "vi-VN",
            text = "  đây là ĐỒ ĂN mang về  ",
        )
        val decision = TranscriptRendering.decide(
            zhLine,
            TranscriptViewMode.HANZI_WITH_PINYIN,
            showVietnamese = true,
            modelAnswerRevealed = true,
            nextLine = next,
        )
        assertFalse(decision.showTranslation)
    }

    @Test
    fun zhLine_translationKeptWhenNextViLineDiffers() {
        val next = viLine.copy(language = "vi-VN", text = "Đây là câu khác.")
        val decision = TranscriptRendering.decide(
            zhLine,
            TranscriptViewMode.HANZI_WITH_PINYIN,
            showVietnamese = true,
            modelAnswerRevealed = true,
            nextLine = next,
        )
        assertTrue(decision.showTranslation)
    }

    @Test
    fun hiddenView_showsNoHanziOrPinyin() {
        val decision = TranscriptRendering.decide(zhLine, TranscriptViewMode.HIDDEN, showVietnamese = false, modelAnswerRevealed = true)
        assertFalse(decision.showHanzi)
        assertFalse(decision.showPinyinLine)
        assertTrue(decision.tokens.isEmpty())
    }

    @Test
    fun pinyinOnlyView_showsPinyinLineNotTokens() {
        val decision = TranscriptRendering.decide(zhLine, TranscriptViewMode.PINYIN_ONLY, showVietnamese = false, modelAnswerRevealed = true)
        assertFalse(decision.showHanzi)
        assertTrue(decision.showPinyinLine)
        assertTrue(decision.tokens.isEmpty())
    }

    @Test
    fun hanziWithPinyinView_putsPinyinOverEveryTokenExceptPunctuation() {
        val decision = TranscriptRendering.decide(zhLine, TranscriptViewMode.HANZI_WITH_PINYIN, showVietnamese = false, modelAnswerRevealed = true)
        assertTrue(decision.showHanzi)
        assertEquals(listOf(true, true, true, false), decision.tokens.map { it.showPinyin })
    }

    @Test
    fun hanziOnlyView_neverShowsPinyin() {
        val decision = TranscriptRendering.decide(zhLine, TranscriptViewMode.HANZI_ONLY, showVietnamese = false, modelAnswerRevealed = true)
        assertTrue(decision.showHanzi)
        assertTrue(decision.tokens.all { !it.showPinyin })
    }

    @Test
    fun modelAnswer_beforeItsCue_isPlaceholderRegardlessOfView() {
        TranscriptViewMode.entries.forEach { view ->
            val decision = TranscriptRendering.decide(modelAnswerLine, view, showVietnamese = true, modelAnswerRevealed = false)
            assertTrue(decision.placeholder, "expected placeholder for view $view")
            assertTrue(decision.tokens.isEmpty())
        }
    }

    @Test
    fun modelAnswer_afterItsCue_rendersNormally() {
        val decision = TranscriptRendering.decide(modelAnswerLine, TranscriptViewMode.HANZI_ONLY, showVietnamese = false, modelAnswerRevealed = true)
        assertFalse(decision.placeholder)
        assertTrue(decision.showHanzi)
    }

    @Test
    fun punctuationToken_isDetected() {
        assertTrue(TranscriptRendering.isPunctuationToken(Token(hanzi = "。")))
        assertTrue(TranscriptRendering.isPunctuationToken(Token(hanzi = "？")))
        assertFalse(TranscriptRendering.isPunctuationToken(Token(hanzi = "外卖")))
    }
}
