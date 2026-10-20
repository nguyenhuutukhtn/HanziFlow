package com.hanziflow.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.hanziflow.R

/**
 * Latin, pinyin and Vietnamese all use bundled Noto Sans (OFL, see
 * assets/licences/Noto_Sans_OFL.txt). Hanzi deliberately uses the system
 * sans-serif: bundling Noto Sans SC would add several megabytes for glyphs the
 * ROM already provides (documented deviation from docs/ui/tokens.md §5.2).
 */
val NotoSans = FontFamily(
    Font(R.font.noto_sans_regular, FontWeight.Normal),
    Font(R.font.noto_sans_medium, FontWeight.Medium),
    Font(R.font.noto_sans_semibold, FontWeight.SemiBold),
    Font(R.font.noto_sans_bold, FontWeight.Bold),
)

private val DefaultTypography = Typography()

val HanziFlowTypography = Typography(
    displayLarge = DefaultTypography.displayLarge.copy(fontFamily = NotoSans),
    displayMedium = DefaultTypography.displayMedium.copy(fontFamily = NotoSans),
    displaySmall = DefaultTypography.displaySmall.copy(fontFamily = NotoSans),
    headlineLarge = DefaultTypography.headlineLarge.copy(fontFamily = NotoSans),
    headlineMedium = DefaultTypography.headlineMedium.copy(fontFamily = NotoSans),
    headlineSmall = DefaultTypography.headlineSmall.copy(fontFamily = NotoSans),
    titleLarge = DefaultTypography.titleLarge.copy(fontFamily = NotoSans),
    titleMedium = DefaultTypography.titleMedium.copy(fontFamily = NotoSans),
    titleSmall = DefaultTypography.titleSmall.copy(fontFamily = NotoSans),
    bodyLarge = DefaultTypography.bodyLarge.copy(fontFamily = NotoSans),
    bodyMedium = DefaultTypography.bodyMedium.copy(fontFamily = NotoSans),
    // Vietnamese meaning role: slightly larger than pinyin, comfortably readable.
    bodySmall = DefaultTypography.bodySmall.copy(
        fontFamily = NotoSans,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelLarge = DefaultTypography.labelLarge.copy(fontFamily = NotoSans),
    // Pinyin / small meta role.
    labelMedium = DefaultTypography.labelMedium.copy(
        fontFamily = NotoSans,
        fontSize = 13.sp,
        lineHeight = 18.sp,
    ),
    labelSmall = DefaultTypography.labelSmall.copy(fontFamily = NotoSans),
)

/** The three transcript text roles plus the Vietnamese gloss, exposed as one local. */
@Immutable
data class AppTextStyles(
    /** System CJK sans (see the deviation note above); generous CJK line height. */
    val hanzi: TextStyle = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 22.sp,
        lineHeight = 34.sp,
        fontWeight = FontWeight.Normal,
    ),
    /** Ruby pinyin above Hanzi / standalone pinyin view. */
    val pinyin: TextStyle = TextStyle(
        fontFamily = NotoSans,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        fontWeight = FontWeight.Normal,
    ),
    /** Vietnamese meaning lines; distinct hue comes from [AppColors.meaning]. */
    val vietnamese: TextStyle = TextStyle(
        fontFamily = NotoSans,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Normal,
    ),
)

val LocalAppTextStyles = staticCompositionLocalOf { AppTextStyles() }

val MaterialTheme.appTextStyles: AppTextStyles
    @Composable
    @ReadOnlyComposable
    get() = LocalAppTextStyles.current
