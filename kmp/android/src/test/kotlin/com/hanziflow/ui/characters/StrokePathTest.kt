package com.hanziflow.ui.characters

import android.graphics.RectF
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class StrokePathTest {
    @Test
    fun `real Hanzi Writer paths have non-degenerate bounds`() {
        listOf(
            "M 554 702 Q 608 714 761 729 Q 774 728 784 742 Q 785 755 760 767 L 554 702 Z",
            "M 167 675 Q 185 630 185 543 Q 185 467 184 302 C 184 272 224 284 225 314 L 216 677 C 192 695 157 703 167 675 Z",
            "M 230 500 Q 279 558 334 639 Q 362 688 388 710 C 398 720 394 734 362 772 L 230 500 Z",
        ).forEach { svg ->
            val bounds = RectF().also { strokePath(svg).computeBounds(it, true) }
            assertTrue("$svg should be wider than zero", bounds.width() > 0f)
            assertTrue("$svg should be taller than zero", bounds.height() > 0f)
        }
    }
}
