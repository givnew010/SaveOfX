import type { PersonEntity, PersonWorkItem, PersonWorkType } from '../types/bookmark';

export interface PersonInputDraft {
  name: string;
  role?: string;
  avatarUrl?: string;
  bio?: string;
  userNote?: string;
  tags?: string;
}

export interface PersonWorkDraft {
  type?: PersonWorkType;
  title: string;
  url: string;
  note?: string;
}

export function normalizeUrl(rawUrl: string): string {
  const trimmed = rawUrl.trim();
  if (!trimmed) return '';
  if (/^(https?:|data:|blob:)/i.test(trimmed)) {
    return trimmed;
  }
  return `https://${trimmed}`;
}

export function detectWorkTypeFromUrl(rawUrl: string): PersonWorkType {
  const url = rawUrl.trim().toLowerCase();
  if (!url) return 'link';

  if (
    url.startsWith('data:image/') ||
    /\.(jpg|jpeg|png|webp|gif|bmp|avif|svg)(\?|#|$)/i.test(url)
  ) {
    return 'photo';
  }

  if (
    url.startsWith('data:video/') ||
    /\.(mp4|webm|mov|m4v|mkv)(\?|#|$)/i.test(url) ||
    /(youtube\.com\/watch|youtu\.be\/|youtube\.com\/shorts\/|vimeo\.com\/|dailymotion\.com\/|tiktok\.com\/)/i.test(
      url
    )
  ) {
    return 'video';
  }

  return 'link';
}

export function extractYouTubeEmbedUrl(rawUrl: string): string | null {
  try {
    const parsed = new URL(normalizeUrl(rawUrl));
    const host = parsed.hostname.replace(/^www\./, '').toLowerCase();
    if (host === 'youtu.be') {
      const id = parsed.pathname.split('/').filter(Boolean)[0];
      return id ? `https://www.youtube.com/embed/${id}` : null;
    }
    if (host === 'youtube.com' || host === 'm.youtube.com') {
      const v = parsed.searchParams.get('v');
      if (v) return `https://www.youtube.com/embed/${v}`;
      const parts = parsed.pathname.split('/').filter(Boolean);
      if ((parts[0] === 'shorts' || parts[0] === 'embed') && parts[1]) {
        return `https://www.youtube.com/embed/${parts[1]}`;
      }
    }
    return null;
  } catch {
    return null;
  }
}

export function isDirectVideoFile(rawUrl: string): boolean {
  const lower = rawUrl.trim().toLowerCase();
  return lower.startsWith('data:video/') || /\.(mp4|webm|mov|m4v)(\?|#|$)/i.test(lower);
}

export function normalizePersonInput(draft: PersonInputDraft): {
  name: string;
  role: string;
  avatarUrl?: string;
  bio: string;
  userNote?: string;
  tags: string;
} | null {
  const name = draft.name.trim();
  if (!name) return null;

  const avatarTrimmed = draft.avatarUrl?.trim();
  const avatarUrl = avatarTrimmed ? normalizeUrl(avatarTrimmed) : undefined;
  const userNoteTrimmed = draft.userNote?.trim();

  return {
    name,
    role: draft.role?.trim() ?? '',
    avatarUrl,
    bio: draft.bio?.trim() ?? '',
    userNote: userNoteTrimmed || undefined,
    tags: draft.tags?.trim() ?? '',
  };
}

export function createPersonWorkItem(
  draft: PersonWorkDraft,
  now = Date.now(),
  idSuffix = Math.random().toString(36).slice(2, 8)
): PersonWorkItem | null {
  const rawUrl = draft.url.trim();
  if (!rawUrl) return null;

  const normalizedUrl = normalizeUrl(rawUrl);
  const title = draft.title.trim() || defaultWorkTitle(draft.type ?? detectWorkTypeFromUrl(normalizedUrl));
  const type = draft.type ?? detectWorkTypeFromUrl(normalizedUrl);
  const noteTrimmed = draft.note?.trim();

  return {
    id: `work_${now}_${idSuffix}`,
    type,
    title,
    url: normalizedUrl,
    note: noteTrimmed || undefined,
    addedAt: now,
  };
}

export function defaultWorkTitle(type: PersonWorkType): string {
  switch (type) {
    case 'photo':
      return 'صورة';
    case 'video':
      return 'فيديو';
    case 'other':
      return 'مرفق';
    default:
      return 'رابط عمل';
  }
}

export function getWorkTypeLabel(type: PersonWorkType): string {
  switch (type) {
    case 'link':
      return 'رابط عمل';
    case 'photo':
      return 'صورة';
    case 'video':
      return 'فيديو';
    case 'other':
      return 'أخرى';
  }
}

export function summarizePersonWorks(works: PersonWorkItem[]): {
  total: number;
  links: number;
  photos: number;
  videos: number;
  others: number;
} {
  let links = 0;
  let photos = 0;
  let videos = 0;
  let others = 0;
  for (const w of works) {
    if (w.type === 'link') links += 1;
    else if (w.type === 'photo') photos += 1;
    else if (w.type === 'video') videos += 1;
    else others += 1;
  }
  return {
    total: works.length,
    links,
    photos,
    videos,
    others,
  };
}

export function buildPersonDedupeKey(person: Pick<PersonEntity, 'name' | 'role'>): string {
  return `${person.name.trim().toLowerCase()}::${person.role.trim().toLowerCase()}`;
}

export function readImageFileAsCompressedDataUrl(file: File, maxDimension = 512): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onerror = () => reject(new Error('تعذر قراءة ملف الصورة'));
    reader.onload = () => {
      const dataUrl = typeof reader.result === 'string' ? reader.result : '';
      if (!dataUrl) {
        reject(new Error('ملف الصورة غير صالح'));
        return;
      }
      if (typeof Image === 'undefined' || typeof document === 'undefined') {
        resolve(dataUrl);
        return;
      }
      const img = new Image();
      img.onload = () => {
        const scale = Math.min(1, maxDimension / Math.max(img.width || 1, img.height || 1));
        const targetW = Math.max(1, Math.round(img.width * scale));
        const targetH = Math.max(1, Math.round(img.height * scale));
        const canvas = document.createElement('canvas');
        canvas.width = targetW;
        canvas.height = targetH;
        const ctx = canvas.getContext('2d');
        if (!ctx) {
          resolve(dataUrl);
          return;
        }
        ctx.drawImage(img, 0, 0, targetW, targetH);
        const compressed = canvas.toDataURL('image/jpeg', 0.82);
        resolve(compressed.length < dataUrl.length ? compressed : dataUrl);
      };
      img.onerror = () => resolve(dataUrl);
      img.src = dataUrl;
    };
    reader.readAsDataURL(file);
  });
}
