package com.hanziflow.shared.modes

data class Mode(val id: String, val label: String, val whenToUse: String)

object ModeInfo {
    val all = listOf(
        Mode("hieu", "Hiểu", "Nghe lần đầu: có giải thích tiếng Việt sau mỗi câu."),
        Mode("tu_vung", "Từ vựng", "Giải thích từ, mẫu câu và dạng câu hỏi bằng tiếng Trung."),
        Mode("nghe", "Nghe", "Nghe thuần tiếng Trung — khi đi bộ, lái xe, làm việc nhà."),
        Mode("phan_xa", "Phản xạ", "Tự trả lời câu hỏi trong khoảng lặng, rồi nghe đáp án."),
        Mode("noi", "Nói", "Nói theo từng cụm, từ ngắn đến dài."),
    )
}
