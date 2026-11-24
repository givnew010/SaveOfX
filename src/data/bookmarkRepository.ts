import type {
  FolderEntity,
  FolderWithCount,
  ParsedTweet,
  TweetBookmarkEntity,
} from '../types/bookmark';
import { TweetParser } from '../utils/tweetParser';

const FOLDERS_STORAGE_KEY = 'xfolders_folders_v1';
const BOOKMARKS_STORAGE_KEY = 'xfolders_bookmarks_v1';

function nextId(items: { id: number }[]): number {
  return items.reduce((max, item) => (item.id > max ? item.id : max), 0) + 1;
}

function readFolders(): FolderEntity[] {
  try {
    const raw = localStorage.getItem(FOLDERS_STORAGE_KEY);
    if (!raw) return [];
    const parsed = JSON.parse(raw);
    return Array.isArray(parsed) ? (parsed as FolderEntity[]) : [];
  } catch {
    return [];
  }
}

function writeFolders(folders: FolderEntity[]): void {
  localStorage.setItem(FOLDERS_STORAGE_KEY, JSON.stringify(folders));
}

function readBookmarks(): TweetBookmarkEntity[] {
  try {
    const raw = localStorage.getItem(BOOKMARKS_STORAGE_KEY);
    if (!raw) return [];
    const parsed = JSON.parse(raw);
    return Array.isArray(parsed) ? (parsed as TweetBookmarkEntity[]) : [];
  } catch {
    return [];
  }
}

function writeBookmarks(bookmarks: TweetBookmarkEntity[]): void {
  localStorage.setItem(BOOKMARKS_STORAGE_KEY, JSON.stringify(bookmarks));
}

export const BookmarkRepository = {
  seedInitialDataIfEmpty(): {
    folders: FolderEntity[];
    bookmarks: TweetBookmarkEntity[];
  } {
    let folders = readFolders();
    let bookmarks = readBookmarks();

    if (folders.length === 0) {
      const now = Date.now();
      folders = [
        {
          id: 1,
          name: 'العلامات المرجعية العامة',
          iconEmoji: '📌',
          colorHex: '#1D9BF0',
          description: 'المجلد الافتراضي لجميع التغريدات غير المصنفة',
          createdAt: now,
          isDefault: true,
        },
        {
          id: 2,
          name: 'تقنية وبرمجة',
          iconEmoji: '💻',
          colorHex: '#3B82F6',
          description: 'شروحات برمجية، أدوات الذكاء الاصطناعي، ومصادر التطوير',
          createdAt: now + 1,
          isDefault: false,
        },
        {
          id: 3,
          name: 'أفكار وإلهام',
          iconEmoji: '💡',
          colorHex: '#F59E0B',
          description: 'اقتباسات مميزة، أفكار مشاريع، وتجارب ملهمة',
          createdAt: now + 2,
          isDefault: false,
        },
        {
          id: 4,
          name: 'للقراءة والاطلاع',
          iconEmoji: '📚',
          colorHex: '#10B981',
          description: 'ثريدات طويلة ومقالات تستحق القراءة المتأنية',
          createdAt: now + 3,
          isDefault: false,
        },
        {
          id: 5,
          name: 'استثمار واقتصاد',
          iconEmoji: '📈',
          colorHex: '#8B5CF6',
          description: 'تحليلات مالية، استثمار، واستراتيجيات ريادة الأعمال',
          createdAt: now + 4,
          isDefault: false,
        },
      ];
      writeFolders(folders);

      if (bookmarks.length === 0) {
        bookmarks = [
          {
            id: 1,
            folderId: 2,
            tweetUrl: 'https://x.com/AndroidDev/status/184512398471923011',
            tweetId: '184512398471923011',
            authorName: 'Android Developers',
            authorHandle: '@AndroidDev',
            content:
              'Build beautiful, fluid user experiences with Jetpack Compose 🚀 Check out modern navigation and adaptive window size classes in our latest guide.',
            postedAt: 'قبل ساعتين',
            savedAt: now - 1000,
            tags: 'android,compose,kotlin',
            userNote:
              'مرجع مهم جداً لتصميم واجهات قابلة للتكيف على الأجهزة اللوحية والمطوية',
            isFavorite: true,
            likesCount: 1420,
            repostsCount: 385,
            isContentFetched: true,
          },
          {
            id: 2,
            folderId: 3,
            tweetUrl: 'https://x.com/Naval/status/1002103360646823936',
            tweetId: '1002103360646823936',
            authorName: 'Naval',
            authorHandle: '@naval',
            content:
              'How to Get Rich (without getting lucky):\nSeek wealth, not money or status. Wealth is having assets that earn while you sleep. Money is how we transfer time and wealth.',
            postedAt: 'منذ 3 أيام',
            savedAt: now - 2000,
            tags: 'mindset,wealth,wisdom',
            userNote: 'الثريد الشهير لنافال عن صناعة الثروة والرافعة المالية',
            isFavorite: true,
            likesCount: 289000,
            repostsCount: 68400,
            isContentFetched: true,
          },
          {
            id: 3,
            folderId: 4,
            tweetUrl: 'https://x.com/TechArabic/status/18392019485729104',
            tweetId: '18392019485729104',
            authorName: 'تقنية بالعربي',
            authorHandle: '@TechArabic',
            content:
              'ثريد شامل: أفضل 10 أدوات مجانية تعمل بالذكاء الاصطناعي لاختصار ساعات العمل اليومية وتلخيص المستندات وإعداد العروض التقديمية 🧵👇',
            postedAt: 'أمس',
            savedAt: now - 3000,
            tags: 'ذكاء_اصطناعي,أدوات,انتاجية',
            userNote: 'أدوات مفيدة لتلخيص الاجتماعات وكتابة التقارير',
            isFavorite: false,
            likesCount: 3450,
            repostsCount: 920,
            isContentFetched: true,
          },
        ];
        writeBookmarks(bookmarks);
      }
    }

    return {
      folders: this.sortFolders(folders),
      bookmarks: this.sortBookmarks(bookmarks),
    };
  },

  sortFolders(folders: FolderEntity[]): FolderEntity[] {
    return [...folders].sort((a, b) => {
      if (a.isDefault !== b.isDefault) return a.isDefault ? -1 : 1;
      return a.createdAt - b.createdAt;
    });
  },

  sortBookmarks(bookmarks: TweetBookmarkEntity[]): TweetBookmarkEntity[] {
    return [...bookmarks].sort((a, b) => b.savedAt - a.savedAt);
  },

  getAllFolders(): FolderEntity[] {
    return this.sortFolders(readFolders());
  },

  getAllBookmarks(): TweetBookmarkEntity[] {
    return this.sortBookmarks(readBookmarks());
  },

  getFoldersWithCount(
    folders: FolderEntity[],
    bookmarks: TweetBookmarkEntity[]
  ): FolderWithCount[] {
    return folders.map((folder) => ({
      folder,
      count: bookmarks.filter((b) => b.folderId === folder.id).length,
    }));
  },

  insertFolder(
    input: Omit<FolderEntity, 'id' | 'createdAt'>
  ): FolderEntity {
    const folders = readFolders();
    const newFolder: FolderEntity = {
      ...input,
      id: nextId(folders),
      createdAt: Date.now(),
    };
    writeFolders([...folders, newFolder]);
    return newFolder;
  },

  updateFolder(updated: FolderEntity): void {
    const folders = readFolders().map((f) => (f.id === updated.id ? updated : f));
    writeFolders(folders);
  },

  deleteFolder(folderId: number, moveToGeneral: boolean): void {
    const folders = readFolders();
    let bookmarks = readBookmarks();

    if (moveToGeneral) {
      const defaultFolder = folders.find((f) => f.isDefault);
      const targetId = defaultFolder?.id ?? 1;
      if (targetId !== folderId) {
        bookmarks = bookmarks.map((b) =>
          b.folderId === folderId ? { ...b, folderId: targetId } : b
        );
      }
    } else {
      bookmarks = bookmarks.filter((b) => b.folderId !== folderId);
    }

    writeBookmarks(bookmarks);
    writeFolders(folders.filter((f) => f.id !== folderId));
  },

  insertBookmark(
    input: Omit<TweetBookmarkEntity, 'id' | 'savedAt'>
  ): TweetBookmarkEntity {
    const bookmarks = readBookmarks();
    const newBookmark: TweetBookmarkEntity = {
      ...input,
      id: nextId(bookmarks),
      savedAt: Date.now(),
    };
    writeBookmarks([newBookmark, ...bookmarks]);
    return newBookmark;
  },

  updateBookmark(updated: TweetBookmarkEntity): void {
    const bookmarks = readBookmarks().map((b) =>
      b.id === updated.id ? updated : b
    );
    writeBookmarks(bookmarks);
  },

  updateBookmarkWithFetchedTweet(
    existing: TweetBookmarkEntity,
    parsed: ParsedTweet
  ): TweetBookmarkEntity {
    const serializedMedia = TweetParser.serializeMediaItems(parsed.mediaItems);
    const updated: TweetBookmarkEntity = {
      ...existing,
      tweetUrl: parsed.tweetUrl,
      tweetId: parsed.tweetId.trim() ? parsed.tweetId : existing.tweetId,
      authorName: parsed.authorName.trim() ? parsed.authorName : existing.authorName,
      authorHandle: parsed.authorHandle.trim()
        ? parsed.authorHandle
        : existing.authorHandle,
      authorAvatarUrl: parsed.authorAvatarUrl ?? existing.authorAvatarUrl,
      content: parsed.content,
      mediaUrl: parsed.mediaUrl,
      mediaType: parsed.mediaType,
      mediaJson: serializedMedia,
      postedAt: parsed.postedAt.trim() ? parsed.postedAt : existing.postedAt,
      likesCount: parsed.likesCount,
      repostsCount: parsed.repostsCount,
      quoteAuthorName: parsed.quoteAuthorName,
      quoteAuthorHandle: parsed.quoteAuthorHandle,
      quoteText: parsed.quoteText,
      isContentFetched: parsed.isContentFetched,
    };
    this.updateBookmark(updated);
    return updated;
  },

  deleteBookmark(id: number): void {
    const bookmarks = readBookmarks().filter((b) => b.id !== id);
    writeBookmarks(bookmarks);
  },

  toggleFavorite(id: number): void {
    const bookmarks = readBookmarks().map((b) =>
      b.id === id ? { ...b, isFavorite: !b.isFavorite } : b
    );
    writeBookmarks(bookmarks);
  },

  moveBookmark(id: number, newFolderId: number): void {
    const bookmarks = readBookmarks().map((b) =>
      b.id === id ? { ...b, folderId: newFolderId } : b
    );
    writeBookmarks(bookmarks);
  },
};
