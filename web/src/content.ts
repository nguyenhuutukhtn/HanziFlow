/** The web's single published-content boundary. Offline caching can replace this module later. */
const base = () => import.meta.env.VITE_CONTENT_BASE_URL as string | undefined;

export const contentUrl = (path: string) => `${base()?.replace(/\/$/, "")}/${path}`;
export const fetchText = (path: string) => fetch(contentUrl(path)).then((response) => response.text());
export const fetchJson = async <T>(path: string) => (await fetch(contentUrl(path))).json() as Promise<T>;

export type CatalogLesson = {
  id: string;
  base_url: string;
  tracks: Record<string, { timeline: { file: string } }>;
  files: { reference: { file: string }; characters?: { file: string } };
};
export const catalogLesson = (catalog: string, lessonId: string) =>
  (JSON.parse(catalog).lessons as CatalogLesson[]).find((lesson) => lesson.id === lessonId);
