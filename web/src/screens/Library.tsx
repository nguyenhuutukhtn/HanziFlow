export type Lesson = { id: string; title: string; order: number; version: number };

export function Library({ lessons, onOpen }: { lessons: Lesson[]; onOpen: (id: string) => void }) {
  return (
    <section className="page-section library-page">
      <p className="eyebrow">LỘ TRÌNH NGHE NÓI</p>
      <h1>Thư viện bài học</h1>
      <p className="subheading">Chọn một bài và học theo nhịp của bạn.</p>
      <div className="lesson-list">
        {[...lessons]
          .sort((a, b) => a.order - b.order)
          .map((lesson) => (
            <button key={lesson.id} className="lesson-card" onClick={() => onOpen(lesson.id)}>
              <span className="lesson-order">{String(lesson.order).padStart(2, "0")}</span>
              <span>
                <strong>{lesson.title}</strong>
                <small>Bài {lesson.order}</small>
              </span>
              <span className="lesson-arrow" aria-hidden="true">
                ›
              </span>
            </button>
          ))}
      </div>
    </section>
  );
}
