import type {
  AccountBookmarkEntity,
  BackupPayload,
  FolderEntity,
  FolderWithCount,
  ParsedAccount,
  ParsedTweet,
  TweetBookmarkEntity,
} from '../types/bookmark';
import { TweetParser } from '../utils/tweetParser';

const FOLDERS_STORAGE_KEY = 'xfolders_folders_v1';
const BOOKMARKS_STORAGE_KEY = 'xfolders_bookmarks_v1';
const ACCOUNTS_STORAGE_KEY = 'xfolders_accounts_v1';
const ACCOUNTS_SEEDED_KEY = 'xfolders_accounts_seeded_v1';
const LAST_BACKUP_STORAGE_KEY = 'xfolders_last_backup_at_v1';

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

function readAccounts(): AccountBookmarkEntity[] {
  try {
    const raw = localStorage.getItem(ACCOUNTS_STORAGE_KEY);
    if (!raw) return [];
    const parsed = JSON.parse(raw);
    return Array.isArray(parsed) ? (parsed as AccountBookmarkEntity[]) : [];
  } catch {
    return [];
  }
}

function writeAccounts(accounts: AccountBookmarkEntity[]): void {
  localStorage.setItem(ACCOUNTS_STORAGE_KEY, JSON.stringify(accounts));
}

function findFolderIdOrDefault(folders: FolderEntity[], preferredName: string, fallbackId: number): number {
  const byName = folders.find((f) => f.name.trim() === preferredName);
  if (byName) return byName.id;
  const byId = folders.find((f) => f.id === fallbackId);
  if (byId) return byId.id;
  return folders.find((f) => f.isDefault)?.id ?? folders[0]?.id ?? 1;
}

export const BookmarkRepository = {
  seedInitialDataIfEmpty(): {
    folders: FolderEntity[];
    bookmarks: TweetBookmarkEntity[];
    accounts: AccountBookmarkEntity[];
  } {
    let folders = readFolders();
    let bookmarks = readBookmarks();
    let accounts = readAccounts();

    if (folders.length === 0) {
      const now = Date.now();
      folders = [
        {
          id: 1,
          name: 'العلامات المرجعية العامة',
          iconEmoji: '📌',
          colorHex: '#1D9BF0',
          description: 'المجموعة الافتراضية لجميع التغريدات غير المصنفة',
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

    const accountsAlreadySeeded =
      typeof localStorage !== 'undefined' && localStorage.getItem(ACCOUNTS_SEEDED_KEY) === '1';

    if (accounts.length === 0 && !accountsAlreadySeeded) {
      const now = Date.now();
      accounts = [
        {
          id: 1,
          folderId: findFolderIdOrDefault(folders, 'تقنية وبرمجة', 2),
          profileUrl: 'https://x.com/AndroidDev',
          handle: '@AndroidDev',
          displayName: 'Android Developers',
          bio: 'The official Android Developers account on X. News, Jetpack Compose updates, Kotlin tips, and developer resources.',
          followersCount: 2450000,
          followingCount: 410,
          verified: true,
          savedAt: now - 1000,
          userNote: 'الحساب الرسمي لمتابعة أخبار أندرويد و Jetpack Compose',
          tags: 'android,kotlin,compose',
          isFavorite: true,
          isProfileFetched: true,
        },
        {
          id: 2,
          folderId: findFolderIdOrDefault(folders, 'أفكار وإلهام', 3),
          profileUrl: 'https://x.com/naval',
          handle: '@naval',
          displayName: 'Naval',
          bio: 'Angel investor, entrepreneur, and philosopher. Notes on wealth, leverage, startups, and clear thinking.',
          followersCount: 2800000,
          followingCount: 0,
          verified: true,
          savedAt: now - 2000,
          userNote: 'تغريدات عميقة في ريادة الأعمال وبناء الثروة',
          tags: 'startups,wisdom,wealth',
          isFavorite: true,
          isProfileFetched: true,
        },
        {
          id: 3,
          folderId: findFolderIdOrDefault(folders, 'تقنية وبرمجة', 2),
          profileUrl: 'https://x.com/OpenAI',
          handle: '@OpenAI',
          displayName: 'OpenAI',
          bio: 'Creating safe AGI that benefits all of humanity. Updates on research, models, and developer tools.',
          followersCount: 4200000,
          followingCount: 18,
          verified: true,
          savedAt: now - 3000,
          userNote: 'متابعة أحدث إصدارات نماذج الذكاء الاصطناعي',
          tags: 'ai,llm,research',
          isFavorite: false,
          isProfileFetched: true,
        },
      ];
      writeAccounts(accounts);
      try {
        localStorage.setItem(ACCOUNTS_SEEDED_KEY, '1');
      } catch {
        // ignore storage errors
      }
    }

    return {
      folders: this.sortFolders(folders),
      bookmarks: this.sortBookmarks(bookmarks),
      accounts: this.sortAccounts(accounts),
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

  sortAccounts(accounts: AccountBookmarkEntity[]): AccountBookmarkEntity[] {
    return [...accounts].sort((a, b) => b.savedAt - a.savedAt);
  },

  getAllFolders(): FolderEntity[] {
    return this.sortFolders(readFolders());
  },

  getAllBookmarks(): TweetBookmarkEntity[] {
    return this.sortBookmarks(readBookmarks());
  },

  getAllAccounts(): AccountBookmarkEntity[] {
    return this.sortAccounts(readAccounts());
  },

  getFoldersWithCount(
    folders: FolderEntity[],
    bookmarks: TweetBookmarkEntity[],
    accounts: AccountBookmarkEntity[] = []
  ): FolderWithCount[] {
    return folders.map((folder) => ({
      folder,
      count: bookmarks.filter((b) => b.folderId === folder.id).length,
      accountCount: accounts.filter((a) => a.folderId === folder.id).length,
    }));
  },

  insertFolder(input: Omit<FolderEntity, 'id' | 'createdAt'>): FolderEntity {
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
    let accounts = readAccounts();

    if (moveToGeneral) {
      const defaultFolder = folders.find((f) => f.isDefault);
      const targetId = defaultFolder?.id ?? 1;
      if (targetId !== folderId) {
        bookmarks = bookmarks.map((b) =>
          b.folderId === folderId ? { ...b, folderId: targetId } : b
        );
        accounts = accounts.map((a) =>
          a.folderId === folderId ? { ...a, folderId: targetId } : a
        );
      }
    } else {
      bookmarks = bookmarks.filter((b) => b.folderId !== folderId);
      accounts = accounts.filter((a) => a.folderId !== folderId);
    }

    writeBookmarks(bookmarks);
    writeAccounts(accounts);
    writeFolders(folders.filter((f) => f.id !== folderId));
  },

  insertBookmark(input: Omit<TweetBookmarkEntity, 'id' | 'savedAt'>): TweetBookmarkEntity {
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
    const bookmarks = readBookmarks().map((b) => (b.id === updated.id ? updated : b));
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
      authorHandle: parsed.authorHandle.trim() ? parsed.authorHandle : existing.authorHandle,
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

  // Accounts CRUD
  insertAccount(input: Omit<AccountBookmarkEntity, 'id' | 'savedAt'>): AccountBookmarkEntity {
    const accounts = readAccounts();
    const newAccount: AccountBookmarkEntity = {
      ...input,
      id: nextId(accounts),
      savedAt: Date.now(),
    };
    writeAccounts([newAccount, ...accounts]);
    try {
      localStorage.setItem(ACCOUNTS_SEEDED_KEY, '1');
    } catch {
      // ignore storage errors
    }
    return newAccount;
  },

  updateAccount(updated: AccountBookmarkEntity): void {
    const accounts = readAccounts().map((a) => (a.id === updated.id ? updated : a));
    writeAccounts(accounts);
  },

  updateAccountWithFetchedProfile(
    existing: AccountBookmarkEntity,
    parsed: ParsedAccount
  ): AccountBookmarkEntity {
    const updated: AccountBookmarkEntity = {
      ...existing,
      profileUrl: parsed.profileUrl,
      handle: parsed.handle,
      displayName: parsed.displayName.trim() ? parsed.displayName : existing.displayName,
      avatarUrl: parsed.avatarUrl ?? existing.avatarUrl,
      bio: parsed.bio.trim() ? parsed.bio : existing.bio,
      followersCount: parsed.followersCount,
      followingCount: parsed.followingCount,
      verified: parsed.verified,
      isProfileFetched: parsed.isProfileFetched,
    };
    this.updateAccount(updated);
    return updated;
  },

  deleteAccount(id: number): void {
    const accounts = readAccounts().filter((a) => a.id !== id);
    writeAccounts(accounts);
  },

  toggleAccountFavorite(id: number): void {
    const accounts = readAccounts().map((a) =>
      a.id === id ? { ...a, isFavorite: !a.isFavorite } : a
    );
    writeAccounts(accounts);
  },

  moveAccount(id: number, newFolderId: number): void {
    const accounts = readAccounts().map((a) =>
      a.id === id ? { ...a, folderId: newFolderId } : a
    );
    writeAccounts(accounts);
  },

  getLastBackupAt(): string | null {
    try {
      return localStorage.getItem(LAST_BACKUP_STORAGE_KEY);
    } catch {
      return null;
    }
  },

  markBackupExported(): string {
    const iso = new Date().toISOString();
    try {
      localStorage.setItem(LAST_BACKUP_STORAGE_KEY, iso);
    } catch {
      // ignore storage errors
    }
    return iso;
  },

  exportBackup(): BackupPayload {
    return {
      app: 'X Folders',
      version: 1,
      exportedAt: new Date().toISOString(),
      folders: this.getAllFolders(),
      bookmarks: this.getAllBookmarks(),
      accounts: this.getAllAccounts(),
    };
  },

  parseBackupJson(rawJson: string): BackupPayload | null {
    try {
      const parsed = JSON.parse(rawJson) as Record<string, unknown>;
      if (!parsed || typeof parsed !== 'object') return null;
      if (!Array.isArray(parsed.folders) || !Array.isArray(parsed.bookmarks)) {
        return null;
      }

      const now = Date.now();
      const validFolders: FolderEntity[] = [];
      for (const raw of parsed.folders) {
        if (!raw || typeof raw !== 'object') continue;
        const item = raw as Record<string, unknown>;
        const name = typeof item.name === 'string' ? item.name.trim() : '';
        if (!name) continue;
        validFolders.push({
          id: typeof item.id === 'number' && Number.isFinite(item.id) ? item.id : validFolders.length + 1,
          name,
          iconEmoji: typeof item.iconEmoji === 'string' && item.iconEmoji.trim() ? item.iconEmoji : '📁',
          colorHex: typeof item.colorHex === 'string' && item.colorHex.trim() ? item.colorHex : '#1D9BF0',
          description: typeof item.description === 'string' ? item.description : '',
          createdAt: typeof item.createdAt === 'number' && Number.isFinite(item.createdAt) ? item.createdAt : now,
          isDefault: Boolean(item.isDefault),
        });
      }

      const validBookmarks: TweetBookmarkEntity[] = [];
      for (const raw of parsed.bookmarks) {
        if (!raw || typeof raw !== 'object') continue;
        const item = raw as Record<string, unknown>;
        const tweetUrl = typeof item.tweetUrl === 'string' ? item.tweetUrl.trim() : '';
        if (!tweetUrl) continue;
        const mediaType =
          item.mediaType === 'photo' || item.mediaType === 'video' || item.mediaType === 'gif'
            ? item.mediaType
            : undefined;
        validBookmarks.push({
          id: typeof item.id === 'number' && Number.isFinite(item.id) ? item.id : validBookmarks.length + 1,
          folderId: typeof item.folderId === 'number' && Number.isFinite(item.folderId) ? item.folderId : 1,
          tweetUrl,
          tweetId: typeof item.tweetId === 'string' ? item.tweetId : '',
          authorName: typeof item.authorName === 'string' && item.authorName.trim() ? item.authorName : 'X User',
          authorHandle: typeof item.authorHandle === 'string' && item.authorHandle.trim() ? item.authorHandle : '@x',
          authorAvatarUrl: typeof item.authorAvatarUrl === 'string' ? item.authorAvatarUrl : undefined,
          content: typeof item.content === 'string' ? item.content : tweetUrl,
          mediaUrl: typeof item.mediaUrl === 'string' ? item.mediaUrl : undefined,
          postedAt: typeof item.postedAt === 'string' ? item.postedAt : '',
          savedAt: typeof item.savedAt === 'number' && Number.isFinite(item.savedAt) ? item.savedAt : now,
          userNote: typeof item.userNote === 'string' && item.userNote.trim() ? item.userNote : undefined,
          tags: typeof item.tags === 'string' ? item.tags : '',
          isFavorite: Boolean(item.isFavorite),
          likesCount: typeof item.likesCount === 'number' && Number.isFinite(item.likesCount) ? item.likesCount : 0,
          repostsCount: typeof item.repostsCount === 'number' && Number.isFinite(item.repostsCount) ? item.repostsCount : 0,
          isContentFetched: Boolean(item.isContentFetched),
          mediaType,
          mediaJson: typeof item.mediaJson === 'string' ? item.mediaJson : undefined,
          quoteAuthorName: typeof item.quoteAuthorName === 'string' ? item.quoteAuthorName : undefined,
          quoteAuthorHandle: typeof item.quoteAuthorHandle === 'string' ? item.quoteAuthorHandle : undefined,
          quoteText: typeof item.quoteText === 'string' ? item.quoteText : undefined,
        });
      }

      const validAccounts: AccountBookmarkEntity[] = [];
      if (Array.isArray(parsed.accounts)) {
        for (const raw of parsed.accounts) {
          if (!raw || typeof raw !== 'object') continue;
          const item = raw as Record<string, unknown>;
          const profileUrl = typeof item.profileUrl === 'string' ? item.profileUrl.trim() : '';
          const handle = typeof item.handle === 'string' ? item.handle.trim() : '';
          if (!profileUrl && !handle) continue;
          const resolvedHandle = handle
            ? handle.startsWith('@')
              ? handle
              : `@${handle}`
            : `@${profileUrl.split('/').filter(Boolean).pop() || 'x'}`;
          const resolvedUrl = profileUrl || `https://x.com/${resolvedHandle.replace(/^@/, '')}`;
          validAccounts.push({
            id: typeof item.id === 'number' && Number.isFinite(item.id) ? item.id : validAccounts.length + 1,
            folderId: typeof item.folderId === 'number' && Number.isFinite(item.folderId) ? item.folderId : 1,
            profileUrl: resolvedUrl,
            handle: resolvedHandle,
            displayName:
              typeof item.displayName === 'string' && item.displayName.trim()
                ? item.displayName.trim()
                : resolvedHandle.replace(/^@/, ''),
            avatarUrl: typeof item.avatarUrl === 'string' ? item.avatarUrl : undefined,
            bio: typeof item.bio === 'string' ? item.bio : '',
            followersCount:
              typeof item.followersCount === 'number' && Number.isFinite(item.followersCount)
                ? item.followersCount
                : 0,
            followingCount:
              typeof item.followingCount === 'number' && Number.isFinite(item.followingCount)
                ? item.followingCount
                : 0,
            verified: Boolean(item.verified),
            savedAt: typeof item.savedAt === 'number' && Number.isFinite(item.savedAt) ? item.savedAt : now,
            userNote: typeof item.userNote === 'string' && item.userNote.trim() ? item.userNote : undefined,
            tags: typeof item.tags === 'string' ? item.tags : '',
            isFavorite: Boolean(item.isFavorite),
            isProfileFetched: Boolean(item.isProfileFetched),
          });
        }
      }

      if (validFolders.length === 0 && validBookmarks.length === 0 && validAccounts.length === 0) {
        return null;
      }

      return {
        app: typeof parsed.app === 'string' ? parsed.app : 'X Folders',
        version: typeof parsed.version === 'number' ? parsed.version : 1,
        exportedAt: typeof parsed.exportedAt === 'string' ? parsed.exportedAt : new Date().toISOString(),
        folders: validFolders,
        bookmarks: validBookmarks,
        accounts: validAccounts,
      };
    } catch {
      return null;
    }
  },

  restoreBackup(
    payload: BackupPayload,
    mode: 'replace' | 'merge'
  ): { foldersAdded: number; bookmarksAdded: number; accountsAdded: number } {
    const incomingAccounts = payload.accounts ?? [];

    if (mode === 'replace') {
      let folders: FolderEntity[] = payload.folders.map((f, idx) => ({
        ...f,
        id: f.id > 0 ? f.id : idx + 1,
      }));

      const seenFolderIds = new Set<number>();
      const folderIdMap = new Map<number, number>();
      let maxFolderId = folders.reduce((m, f) => Math.max(m, f.id), 0);
      folders = folders.map((f) => {
        if (!seenFolderIds.has(f.id)) {
          seenFolderIds.add(f.id);
          folderIdMap.set(f.id, f.id);
          return f;
        }
        const newId = ++maxFolderId;
        folderIdMap.set(f.id, newId);
        return { ...f, id: newId };
      });

      if (folders.length === 0) {
        folders = [
          {
            id: 1,
            name: 'العلامات المرجعية العامة',
            iconEmoji: '📌',
            colorHex: '#1D9BF0',
            description: 'المجموعة الافتراضية لجميع التغريدات غير المصنفة',
            createdAt: Date.now(),
            isDefault: true,
          },
        ];
      }

      if (!folders.some((f) => f.isDefault)) {
        folders[0] = { ...folders[0], isDefault: true };
      }

      const defaultFolderId = folders.find((f) => f.isDefault)?.id ?? folders[0].id;
      const validFolderIdSet = new Set(folders.map((f) => f.id));

      const bookmarks: TweetBookmarkEntity[] = payload.bookmarks.map((b, idx) => {
        const mappedFolderId = folderIdMap.get(b.folderId) ?? b.folderId;
        return {
          ...b,
          id: idx + 1,
          folderId: validFolderIdSet.has(mappedFolderId) ? mappedFolderId : defaultFolderId,
        };
      });

      const accounts: AccountBookmarkEntity[] = incomingAccounts.map((a, idx) => {
        const mappedFolderId = folderIdMap.get(a.folderId) ?? a.folderId;
        return {
          ...a,
          id: idx + 1,
          folderId: validFolderIdSet.has(mappedFolderId) ? mappedFolderId : defaultFolderId,
        };
      });

      writeFolders(folders);
      writeBookmarks(bookmarks);
      writeAccounts(accounts);
      try {
        localStorage.setItem(ACCOUNTS_SEEDED_KEY, '1');
      } catch {
        // ignore storage errors
      }
      return {
        foldersAdded: folders.length,
        bookmarksAdded: bookmarks.length,
        accountsAdded: accounts.length,
      };
    }

    // Merge mode
    const existingFolders = readFolders();
    const existingBookmarks = readBookmarks();
    const existingAccounts = readAccounts();

    const defaultFolder = existingFolders.find((f) => f.isDefault) ?? existingFolders[0];
    const defaultFolderId = defaultFolder?.id ?? 1;

    const folderIdMap = new Map<number, number>();
    let nextFolderId = nextId(existingFolders);
    const mergedFolders = [...existingFolders];
    let foldersAdded = 0;

    for (const incoming of payload.folders) {
      const matchByDefault = incoming.isDefault && defaultFolder;
      const matchByName = existingFolders.find(
        (ef) => ef.name.trim().toLowerCase() === incoming.name.trim().toLowerCase()
      );
      const matched = matchByDefault ? defaultFolder : matchByName;
      if (matched) {
        folderIdMap.set(incoming.id, matched.id);
      } else {
        const assignedId = nextFolderId++;
        folderIdMap.set(incoming.id, assignedId);
        mergedFolders.push({
          ...incoming,
          id: assignedId,
          isDefault: false,
        });
        foldersAdded += 1;
      }
    }

    const existingUrlSet = new Set(existingBookmarks.map((b) => b.tweetUrl.trim().toLowerCase()));
    let nextBookmarkId = nextId(existingBookmarks);
    const mergedBookmarks = [...existingBookmarks];
    let bookmarksAdded = 0;

    for (const incoming of payload.bookmarks) {
      const normalizedUrl = incoming.tweetUrl.trim().toLowerCase();
      if (existingUrlSet.has(normalizedUrl)) continue;
      existingUrlSet.add(normalizedUrl);
      const targetFolderId = folderIdMap.get(incoming.folderId) ?? defaultFolderId;
      mergedBookmarks.push({
        ...incoming,
        id: nextBookmarkId++,
        folderId: targetFolderId,
      });
      bookmarksAdded += 1;
    }

    const existingAccountUrlSet = new Set(
      existingAccounts.map((a) => a.profileUrl.trim().toLowerCase())
    );
    let nextAccountId = nextId(existingAccounts);
    const mergedAccounts = [...existingAccounts];
    let accountsAdded = 0;

    for (const incoming of incomingAccounts) {
      const normalizedUrl = incoming.profileUrl.trim().toLowerCase();
      if (existingAccountUrlSet.has(normalizedUrl)) continue;
      existingAccountUrlSet.add(normalizedUrl);
      const targetFolderId = folderIdMap.get(incoming.folderId) ?? defaultFolderId;
      mergedAccounts.push({
        ...incoming,
        id: nextAccountId++,
        folderId: targetFolderId,
      });
      accountsAdded += 1;
    }

    writeFolders(mergedFolders);
    writeBookmarks(mergedBookmarks);
    writeAccounts(mergedAccounts);
    try {
      localStorage.setItem(ACCOUNTS_SEEDED_KEY, '1');
    } catch {
      // ignore storage errors
    }
    return { foldersAdded, bookmarksAdded, accountsAdded };
  },
};
