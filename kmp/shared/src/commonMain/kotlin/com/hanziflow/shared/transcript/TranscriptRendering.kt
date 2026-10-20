package com.hanziflow.shared.transcript

import com.hanziflow.shared.ReferenceLine
import com.hanziflow.shared.Token

/** One dataset, several views, switched instantly. Vietnamese is an independent toggle. */
enum class TranscriptViewMode { HIDDEN, PINYIN_ONLY, HANZI_WITH_PINYIN, HANZI_ONLY }

data class TokenRenderDecision(val token: Token, val showPinyin: Boolean)

data class LineRenderDecision(
    /** When true, the line is a hidden `model_answer` placeholder: render nothing else. */
    val placeholder: Boolean,
    /** When false, the line is suppressed entirely (a vi-VN coach line with the toggle off). */
    val lineVisible: Boolean,
    val showHanzi: Boolean,
    val showPinyinLine: Boolean,
    val tokens: List<TokenRenderDecision>,
    val showTranslation: Boolean,
)

object TranscriptRendering {
    // No \p{...} Unicode property classes: Kotlin/JS regex (plain JS RegExp) doesn't
    // support them, and this file must behave the same on jvm, js, and android.
    private val NON_PINYIN_CHARS = Regex("^[\\s!\"#$%&'()*+,\\-./:;<=>?@\\[\\]^_`{|}~，。？！：；、…—·“”‘’]+$")

    /** A token made only of punctuation/whitespace is rendered without a pinyin column. */
    fun isPunctuationToken(token: Token): Boolean = NON_PINYIN_CHARS.matches(token.hanzi)

    /**
     * How to render one reference line under the current view + Vietnamese toggle +
     * model_answer reveal state. [modelAnswerRevealed] is ignored for non-model_answer
     * lines.
     */
    fun decide(
        line: ReferenceLine,
        view: TranscriptViewMode,
        showVietnamese: Boolean,
        modelAnswerRevealed: Boolean,
    ): LineRenderDecision {
        val isVietnameseLine = line.language == "vi"
        if (isVietnameseLine && !showVietnamese) {
            return LineRenderDecision(
                placeholder = false,
                lineVisible = false,
                showHanzi = false,
                showPinyinLine = false,
                tokens = emptyList(),
                showTranslation = false,
            )
        }
        if (line.type == "model_answer" && !modelAnswerRevealed) {
            return LineRenderDecision(
                placeholder = true,
                lineVisible = true,
                showHanzi = false,
                showPinyinLine = false,
                tokens = emptyList(),
                showTranslation = false,
            )
        }
        val showTranslation = showVietnamese && !isVietnameseLine && line.translation != null
        return when (view) {
            TranscriptViewMode.HIDDEN -> LineRenderDecision(
                placeholder = false, lineVisible = true, showHanzi = false, showPinyinLine = false,
                tokens = emptyList(), showTranslation = showTranslation,
            )
            TranscriptViewMode.PINYIN_ONLY -> LineRenderDecision(
                placeholder = false, lineVisible = true, showHanzi = false, showPinyinLine = true,
                tokens = emptyList(), showTranslation = showTranslation,
            )
            TranscriptViewMode.HANZI_WITH_PINYIN -> LineRenderDecision(
                placeholder = false, lineVisible = true, showHanzi = true, showPinyinLine = false,
                tokens = (line.tokens ?: emptyList()).map { TokenRenderDecision(it, showPinyin = !isPunctuationToken(it)) },
                showTranslation = showTranslation,
            )
            TranscriptViewMode.HANZI_ONLY -> LineRenderDecision(
                placeholder = false, lineVisible = true, showHanzi = true, showPinyinLine = false,
                tokens = (line.tokens ?: emptyList()).map { TokenRenderDecision(it, showPinyin = false) },
                showTranslation = showTranslation,
            )
        }
    }
}
