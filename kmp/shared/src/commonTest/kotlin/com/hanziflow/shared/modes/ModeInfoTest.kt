package com.hanziflow.shared.modes

import kotlin.test.Test
import kotlin.test.assertEquals

class ModeInfoTest {
    @Test
    fun listsModesInPlaybackOrderWithVietnameseGuidance() {
        assertEquals(listOf("hieu", "tu_vung", "nghe", "phan_xa", "noi"), ModeInfo.all.map { it.id })
        assertEquals(listOf("Hiểu", "Từ vựng", "Nghe", "Phản xạ", "Nói"), ModeInfo.all.map { it.label })
        assertEquals(
            listOf(
                "Nghe lần đầu: có giải thích tiếng Việt sau mỗi câu.",
                "Giải thích từ, mẫu câu và dạng câu hỏi bằng tiếng Trung.",
                "Nghe thuần tiếng Trung — khi đi bộ, lái xe, làm việc nhà.",
                "Tự trả lời câu hỏi trong khoảng lặng, rồi nghe đáp án.",
                "Nói theo từng cụm, từ ngắn đến dài.",
            ),
            ModeInfo.all.map { it.whenToUse },
        )
    }
}
