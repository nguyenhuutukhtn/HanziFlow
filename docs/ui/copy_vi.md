# UI — Vietnamese UI copy

> Part of the UI spec. Start at [README.md](README.md) for principles, navigation and the section map.

## 8. UI copy (Vietnamese)

Organized by screen, matching §3. These are the authoritative strings;
implementation should treat them as the string-resource source, not placeholder
text to be rewritten ad hoc.

**Bottom navigation**
- Học
- Ôn tập
- Cài đặt

**Learn**
- Title: Học
- Empty state: "Chưa có bài học nào." / "Đồng bộ để tải danh sách bài học mới nhất." / button: "Đồng bộ ngay"
- Sync in progress: "Đang đồng bộ..."
- Sync success: "Đã cập nhật danh sách bài học"
- Sync failure: "Không đồng bộ được. Kiểm tra kết nối mạng."
- Offline banner: "Không có kết nối mạng. Các bài đã tải vẫn nghe được."
- Card actions: "Nghe" · "Tải xuống" · "Hủy" · "Thử lại" · "Cập nhật" · "Nghe bản cũ" · "Xóa"
- Passed badge: "Đã qua"
- Update badge: "Có bản mới"
- Delete confirm: "Xóa bài học này?" / "Bạn có thể tải lại sau." / "Xóa" / "Hủy"
- Continue card prefix: "Đang nghe:"

**Lesson detail**
- "Trình độ:" · "Kích thước:" · "Thời lượng:"
- "Chỉ tải qua Wi-Fi"
- Button: "Tải xuống bài học"

**Now Playing**
- View-mode chips: "Ẩn" · "Pinyin" · "Hán + pinyin" · "Hán tự"
- Vietnamese switch label: "Tiếng Việt"
- Track switch: "Guided" · "Luyện tập" (kept as-is — these are already established English loan terms for Vietnamese learners of this course; do not translate to avoid inconsistency with the lesson JSON's own `track` naming)
- Overflow menu: "Kiểm tra bài" · "Tải lại bài" · "Báo cáo vấn đề"
- Loop chip: "Đang lặp: {dòng}" (dismiss icon has TalkBack label "Dừng lặp")
- model_answer placeholder: "Đến lượt bạn — nói trước khi nghe"
- Reload-needed snackbar: "Không phát được bài này. Hãy tải lại." / action: "Tải lại"

**Lookup sheet**
- Button: "Phát âm"
- "Sao chép" · "Mở trong Pleco" · "Chia sẻ đến..."

**Review**
- Title: Ôn tập
- "Đã qua" · "Chưa qua" · "Chưa có lượt làm nào"
- "{n} lượt làm"
- Buttons: "Làm bài" · "Làm lại"
- Exit confirm: "Thoát bài kiểm tra?" / "Lượt làm sẽ không được lưu." / "Thoát" / "Tiếp tục làm"
- Correct feedback: "Đúng rồi!"
- Incorrect feedback: "Chưa đúng — câu này sẽ quay lại sau."
- Result pass: "Qua bài kiểm tra!" / "{correct}/{total} câu đúng · {mistakes} lần sai đã được làm lại" / "Về trang Ôn tập" / "Nghe lại bài học"
- Question counter: "Câu {n} / {total}"
- Replay button: "Nghe lại"

**Settings**
- Section headers: "HIỂN THỊ" · "TẢI XUỐNG" · "TÀI KHOẢN" · "DÀNH CHO NHÀ PHÁT TRIỂN"
- "Hiển thị mặc định (Guided)" · "Hiển thị mặc định (Luyện tập)"
- "Hiện tiếng Việt" · "Giao diện tối" · "Dùng màu theo hình nền máy"
- "Chỉ tải qua Wi-Fi" · "Dung lượng đã dùng"
- "Đăng xuất" · "Đăng nhập để lưu tiến độ trên nhiều máy"
- "Đồng bộ lần cuối: {time}"

**Generic errors**
- No network, generic action: "Không có kết nối mạng."
- Unexpected error: "Đã có lỗi xảy ra. Vui lòng thử lại."
