export type Track = { name: string; display_name: string };

export type Cue = { line_id: string; start_ms: number; end_ms: number };
export type CuePlace = { speaking: number; last: number; next: number };
export type Section = { id: string; title: string; start_ms: number; end_ms: number };

export type RenderLine = {
  id: string;
  text: string;
  pinyin: string | null;
  translation: string | null;
  type: string;
  language: string;
  tokens: { hanzi: string; pinyin: string | null; showPinyin: boolean }[];

  showHanzi: boolean;
  showPinyinLine: boolean;
  showTranslation: boolean;

  lineVisible: boolean;
  placeholder: boolean;
};

export type Character = {
  char: string;
  pinyin: string;
  han_viet: string;
  vi_meaning: string;
  hsk_level?: number | null;
  decomp_ids?: string | null;
  lesson_words?: string[];
  components: { char: string }[];
};

type Session = {
  lessonTitle(): string;
  audioPath(track: string): string | null;
  timelinePath(track: string): string | null;

  referencePath(): string;
  charactersPath(): string | null;
  tracksJson(): string;

  activeCueIndex(position: number): number;
  cuePlaceJson(position: number): string;
  cueCount(): number;
  cueJson(index: number): string | null;

  linesJson(track: string): string;
  sectionsJson(): string;
  lineJson(id: string, view: string, vi: boolean, position: number): string | null;

  seekTargetMs(id: string, position: number): number | null;
  nextCueStartMs(position: number): number | null;

  previousCueStartMs(position: number): number | null;

  characterJson(char: string): string | null;
};

type Shared = {
  com: {
    hanziflow: {
      shared: {
        web: {
          LessonSessionJs: new (
            catalog: string,
            lessonId: string,
            timeline: string,
            reference: string,
            characters?: string | null,
            track?: string,
          ) => Session;

          CatalogJs: { lessonsJson(catalog: string): string };

          TrackSwitchJs: { positionFor(from: string, to: string, position: number): number };
        };
      };
    };
  };
};

export const shared = () => (globalThis as { ["HanziFlow:shared"]?: Shared })["HanziFlow:shared"];

export const json = <T>(value: string | null): T | null => (value ? (JSON.parse(value) as T) : null);

export type { Session };
