import type { Character, Session } from "../facade";
import { json } from "../facade";
import { Drawer, DrawerContent } from "./ui/drawer";
import { Popover, PopoverContent } from "./ui/popover";

type Props = {
  session: Session;
  char: string;
  close: () => void;
  select: (char: string) => void;
  copy: (char: string) => void;
  compact?: boolean;
};
export function CharacterCard({ session, char, close, select, copy, compact = false }: Props) {
  const item = json<Character>(session.characterJson(char));
  if (!item) return null;
  const detail = <Card item={item} close={close} select={select} copy={copy} />;
  if (compact)
    return (
      <Drawer open onOpenChange={(open) => !open && close()}>
        <DrawerContent>
          <div className="character-card character-sheet" aria-label={`Chữ ${item.char}`}>
            {detail}
          </div>
        </DrawerContent>
      </Drawer>
    );
  return (
    <Popover open onOpenChange={(open) => !open && close()}>
      <PopoverContent aria-label={`Chữ ${item.char}`} className="character-card character-popover">
        {detail}
      </PopoverContent>
    </Popover>
  );
}
function Card({
  item,
  close,
  select,
  copy,
}: {
  item: Character;
  close: () => void;
  select: (char: string) => void;
  copy: (char: string) => void;
}) {
  return (
    <>
      <button className="card-close" onClick={close}>
        Đóng
      </button>
      <h2>
        {item.char} <small>{item.pinyin}</small>
      </h2>
      <p>Hán-Việt: {item.han_viet}</p>
      <p>Nghĩa: {item.vi_meaning}</p>
      {item.hsk_level != null && <span className="hsk-chip">HSK {item.hsk_level}</span>}
      {item.decomp_ids && <p>Cấu tạo: {item.decomp_ids}</p>}
      <div className="components">
        {item.components.map((component, index) => (
          <button key={`${component.char}-${index}`} onClick={() => select(component.char)}>
            {component.char}
          </button>
        ))}
      </div>
      {item.lesson_words?.length ? <p>Từ trong bài: {item.lesson_words.join(" · ")}</p> : null}
      <button onClick={() => copy(item.char)}>Sao chép</button>
    </>
  );
}
