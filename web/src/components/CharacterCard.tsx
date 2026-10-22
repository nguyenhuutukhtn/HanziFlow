import type { Character, Session } from "../facade";

import { json } from "../facade";

export function CharacterCard({
  session,
  char,
  close,
  select,
  copy,
}: {
  session: Session;
  char: string;
  close: () => void;

  select: (char: string) => void;
  copy: (char: string) => void;
}) {
  const item = json<Character>(session.characterJson(char));

  if (!item) return null;

  return (
    <aside className="character-card" aria-label={`Chữ ${item.char}`}>
      <button onClick={close}>Đóng</button>
      <h2>
        {item.char} <small>{item.pinyin}</small>
      </h2>
      <p>Hán-Việt: {item.han_viet}</p>
      <p>Nghĩa: {item.vi_meaning}</p>
      <div className="components">
        {item.components.map((component, index) => (
          <button key={`${component.char}-${index}`} onClick={() => select(component.char)}>
            {component.char}
          </button>
        ))}
      </div>
      <button onClick={() => copy(item.char)}>Sao chép chữ</button>
    </aside>
  );
}
