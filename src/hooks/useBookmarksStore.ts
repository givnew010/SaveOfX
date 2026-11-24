import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { BookmarkRepository } from '../data/bookmarkRepository';
import type {
  BackupPayload,
  FolderEntity,
  FolderWithCount,
  MainTab,
  ParsedTweet,
  TweetBookmarkEntity,
} from '../types/bookmark';
import { TweetFetcher } from '../utils/tweetFetcher';
import { TweetParser } from '../utils/tweetParser';

export function useBookmarksStore() {
  const [folders, setFolders] = useState<FolderEntity[]>([]);
  const [bookmarks, setBookmarks] = useState<TweetBookmarkEntity[]>([]);

  const [selectedFolderId, setSelectedFolderId] = useState<number | null>(null);
  const [showFavoritesOnly, setShowFavoritesOnly] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [activeTab, setActiveTab] = useState<MainTab>('BOOKMARKS');

  // Save Sheet state
  const [isSaveSheetOpen, setIsSaveSheetOpen] = useState(false);
  const [pendingUrlInput, setPendingUrlInput] = useState('');
  const [pendingParsedTweet, setPendingParsedTweet] = useState<ParsedTweet | null>(null);
  const [isFetchingTweet, setIsFetchingTweet] = useState(false);
  const [isOfflinePreview, setIsOfflinePreview] = useState(false);
  const [pendingSelectedFolderId, setPendingSelectedFolderId] = useState<number | null>(null);
  const [pendingUserNote, setPendingUserNote] = useState('');
  const [pendingTags, setPendingTags] = useState('');

  // Refreshing cards
  const [refreshingBookmarkIds, setRefreshingBookmarkIds] = useState<Set<number>>(new Set());

  // Dialogs
  const [showCreateFolderDialog, setShowCreateFolderDialog] = useState(false);
  const [folderToEdit, setFolderToEdit] = useState<FolderEntity | null>(null);
  const [folderToDelete, setFolderToDelete] = useState<FolderWithCount | null>(null);
  const [bookmarkToMove, setBookmarkToMove] = useState<TweetBookmarkEntity | null>(null);
  const [userFeedbackMessage, setUserFeedbackMessage] = useState<string | null>(null);
  const [lastBackupAt, setLastBackupAt] = useState<string | null>(() =>
    BookmarkRepository.getLastBackupAt()
  );

  const previewFetchTimerRef = useRef<number | null>(null);
  const previewRequestSeqRef = useRef(0);

  const reloadFromRepo = useCallback(() => {
    setFolders(BookmarkRepository.getAllFolders());
    setBookmarks(BookmarkRepository.getAllBookmarks());
  }, []);

  const showFeedback = useCallback((message: string) => {
    setUserFeedbackMessage(message);
  }, []);

  const clearFeedbackMessage = useCallback(() => {
    setUserFeedbackMessage(null);
  }, []);

  const refreshSingleBookmark = useCallback(
    async (bookmark: TweetBookmarkEntity, silent = false) => {
      if (!TweetParser.isTweetUrl(bookmark.tweetUrl)) {
        if (!silent) showFeedback('الرابط لا يشير إلى منشور X صالح');
        return;
      }

      if (!TweetFetcher.isNetworkAvailable()) {
        if (!silent) showFeedback('لا يوجد اتصال بالإنترنت — يعرض الرابط فقط');
        return;
      }

      setRefreshingBookmarkIds((prev) => {
        const next = new Set(prev);
        next.add(bookmark.id);
        return next;
      });

      const result = await TweetFetcher.fetchTweet(bookmark.tweetUrl);
      if (result.status === 'success') {
        BookmarkRepository.updateBookmarkWithFetchedTweet(bookmark, result.tweet);
        reloadFromRepo();
        if (!silent) showFeedback('تم تحديث محتوى التغريدة بنجاح');
      } else if (result.status === 'offline') {
        if (!silent) showFeedback('لا يوجد اتصال بالإنترنت');
      } else {
        if (!silent) showFeedback('تعذر جلب محتوى التغريدة حالياً');
      }

      setRefreshingBookmarkIds((prev) => {
        const next = new Set(prev);
        next.delete(bookmark.id);
        return next;
      });
    },
    [reloadFromRepo, showFeedback]
  );

  const refreshPendingBookmarks = useCallback(async () => {
    const all = BookmarkRepository.getAllBookmarks();
    const unfetched = all.filter((b) => !b.isContentFetched);
    if (unfetched.length === 0) return;

    const hasInternet = TweetFetcher.isNetworkAvailable();

    for (const bookmark of unfetched) {
      const cleanUrl = TweetParser.extractCleanUrl(bookmark.tweetUrl) ?? bookmark.tweetUrl;
      const trimmedContent = bookmark.content.trim();
      const looksLikeGarbage =
        trimmedContent.startsWith('?') ||
        trimmedContent.startsWith('&') ||
        trimmedContent.startsWith('a=') ||
        trimmedContent.startsWith('s=') ||
        trimmedContent.startsWith('منشور من منصة');

      let cleanedBookmark = bookmark;
      if (looksLikeGarbage || bookmark.tweetUrl !== cleanUrl) {
        cleanedBookmark = { ...bookmark, tweetUrl: cleanUrl, content: cleanUrl };
        BookmarkRepository.updateBookmark(cleanedBookmark);
        reloadFromRepo();
      }

      if (hasInternet && TweetParser.isTweetUrl(cleanedBookmark.tweetUrl)) {
        await refreshSingleBookmark(cleanedBookmark, true);
      }
    }
  }, [refreshSingleBookmark, reloadFromRepo]);

  const fetchPreviewForInput = useCallback((rawInput: string, immediate: boolean) => {
    if (previewFetchTimerRef.current !== null) {
      window.clearTimeout(previewFetchTimerRef.current);
      previewFetchTimerRef.current = null;
    }

    const requestSeq = ++previewRequestSeqRef.current;

    const run = async () => {
      setIsFetchingTweet(true);
      setIsOfflinePreview(false);

      const result = await TweetFetcher.fetchTweet(rawInput);
      if (previewRequestSeqRef.current !== requestSeq) return;

      if (result.status === 'success') {
        setPendingParsedTweet(result.tweet);
        setPendingUrlInput(result.tweet.tweetUrl);
        setIsOfflinePreview(false);
      } else if (result.status === 'offline') {
        setPendingParsedTweet(result.fallbackTweet);
        setPendingUrlInput(result.fallbackTweet.tweetUrl);
        setIsOfflinePreview(true);
      } else {
        setPendingParsedTweet(result.fallbackTweet);
        setPendingUrlInput(result.fallbackTweet.tweetUrl);
        setIsOfflinePreview(!TweetFetcher.isNetworkAvailable());
      }
      setIsFetchingTweet(false);
    };

    if (immediate) {
      void run();
    } else {
      previewFetchTimerRef.current = window.setTimeout(() => {
        void run();
      }, 400);
    }
  }, []);

  const onReceiveSharedText = useCallback(
    (rawText: string) => {
      const parsed = TweetParser.parseSharedText(rawText);
      setPendingUrlInput(parsed.tweetUrl);
      setPendingParsedTweet(parsed);
      setPendingUserNote('');
      setPendingTags('');
      setIsSaveSheetOpen(true);
      fetchPreviewForInput(rawText, true);
    },
    [fetchPreviewForInput]
  );

  // Initial seed + pending refresh + check incoming shared URL query param
  useEffect(() => {
    const seeded = BookmarkRepository.seedInitialDataIfEmpty();
    setFolders(seeded.folders);
    setBookmarks(seeded.bookmarks);
    void refreshPendingBookmarks();

    const params = new URLSearchParams(window.location.search);
    const sharedText = params.get('text') || params.get('url') || params.get('share');
    if (sharedText && sharedText.trim()) {
      onReceiveSharedText(sharedText.trim());
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const foldersWithCount = useMemo(
    () => BookmarkRepository.getFoldersWithCount(folders, bookmarks),
    [folders, bookmarks]
  );

  const filteredBookmarks = useMemo(() => {
    const query = searchQuery.trim().toLowerCase();
    return bookmarks.filter((b) => {
      const matchFolder = selectedFolderId === null || b.folderId === selectedFolderId;
      const matchFav = !showFavoritesOnly || b.isFavorite;
      const matchQuery =
        query.length === 0 ||
        b.content.toLowerCase().includes(query) ||
        b.authorName.toLowerCase().includes(query) ||
        b.authorHandle.toLowerCase().includes(query) ||
        (b.userNote?.toLowerCase().includes(query) ?? false) ||
        b.tags.toLowerCase().includes(query);
      return matchFolder && matchFav && matchQuery;
    });
  }, [bookmarks, selectedFolderId, showFavoritesOnly, searchQuery]);

  const effectivePendingFolderId =
    pendingSelectedFolderId ?? foldersWithCount[0]?.folder.id ?? null;

  const selectFolder = useCallback((folderId: number | null) => {
    setSelectedFolderId(folderId);
    if (folderId !== null) {
      setShowFavoritesOnly(false);
      setActiveTab('BOOKMARKS');
    }
  }, []);

  const toggleFavoritesOnly = useCallback(() => {
    setShowFavoritesOnly((prev) => {
      const next = !prev;
      if (next) {
        setSelectedFolderId(null);
        setActiveTab('BOOKMARKS');
      }
      return next;
    });
  }, []);

  const openManualAddSheet = useCallback(
    (initialUrl = '') => {
      setPendingUrlInput(initialUrl);
      setPendingParsedTweet(
        initialUrl.trim() ? TweetParser.parseSharedText(initialUrl) : null
      );
      setIsFetchingTweet(false);
      setIsOfflinePreview(false);
      setPendingUserNote('');
      setPendingTags('');
      setIsSaveSheetOpen(true);
      if (initialUrl.trim() && TweetParser.isTweetUrl(initialUrl)) {
        fetchPreviewForInput(initialUrl, true);
      }
    },
    [fetchPreviewForInput]
  );

  const closeSaveSheet = useCallback(() => {
    if (previewFetchTimerRef.current !== null) {
      window.clearTimeout(previewFetchTimerRef.current);
      previewFetchTimerRef.current = null;
    }
    previewRequestSeqRef.current += 1;
    setIsSaveSheetOpen(false);
    setPendingParsedTweet(null);
    setIsFetchingTweet(false);
    setIsOfflinePreview(false);
    setPendingUrlInput('');
    setPendingUserNote('');
    setPendingTags('');
  }, []);

  const onUrlInputChanged = useCallback(
    (newUrl: string) => {
      setPendingUrlInput(newUrl);
      if (!newUrl.trim()) {
        if (previewFetchTimerRef.current !== null) {
          window.clearTimeout(previewFetchTimerRef.current);
          previewFetchTimerRef.current = null;
        }
        previewRequestSeqRef.current += 1;
        setPendingParsedTweet(null);
        setIsFetchingTweet(false);
        setIsOfflinePreview(false);
        return;
      }

      const baseline = TweetParser.parseSharedText(newUrl);
      setPendingParsedTweet(baseline);

      if (TweetParser.isTweetUrl(newUrl)) {
        fetchPreviewForInput(newUrl, false);
      } else {
        if (previewFetchTimerRef.current !== null) {
          window.clearTimeout(previewFetchTimerRef.current);
          previewFetchTimerRef.current = null;
        }
        previewRequestSeqRef.current += 1;
        setIsFetchingTweet(false);
        setIsOfflinePreview(false);
      }
    },
    [fetchPreviewForInput]
  );

  const savePendingBookmark = useCallback(() => {
    const rawUrl = pendingUrlInput.trim();
    if (!rawUrl) return;

    const parsed = pendingParsedTweet ?? TweetParser.parseSharedText(rawUrl);
    const folderId = effectivePendingFolderId ?? 1;
    const serializedMedia = TweetParser.serializeMediaItems(parsed.mediaItems);

    const saved = BookmarkRepository.insertBookmark({
      folderId,
      tweetUrl: parsed.tweetUrl,
      tweetId: parsed.tweetId,
      authorName: parsed.authorName,
      authorHandle: parsed.authorHandle,
      authorAvatarUrl: parsed.authorAvatarUrl,
      content: parsed.content,
      mediaUrl: parsed.mediaUrl,
      mediaType: parsed.mediaType,
      mediaJson: serializedMedia,
      postedAt: parsed.postedAt,
      userNote: pendingUserNote.trim() || undefined,
      tags: pendingTags.trim(),
      isFavorite: false,
      likesCount: parsed.likesCount,
      repostsCount: parsed.repostsCount,
      quoteAuthorName: parsed.quoteAuthorName,
      quoteAuthorHandle: parsed.quoteAuthorHandle,
      quoteText: parsed.quoteText,
      isContentFetched: parsed.isContentFetched,
    });

    reloadFromRepo();
    closeSaveSheet();
    showFeedback(
      saved.isContentFetched
        ? 'تم حفظ التغريدة ومحتواها في المجموعة بنجاح'
        : 'تم حفظ الرابط (سيتم جلب المحتوى عند توفر الإنترنت)'
    );

    if (!saved.isContentFetched && TweetParser.isTweetUrl(saved.tweetUrl)) {
      void refreshSingleBookmark(saved, true);
    }
  }, [
    pendingUrlInput,
    pendingParsedTweet,
    effectivePendingFolderId,
    pendingUserNote,
    pendingTags,
    reloadFromRepo,
    closeSaveSheet,
    showFeedback,
    refreshSingleBookmark,
  ]);

  const toggleFavorite = useCallback(
    (bookmark: TweetBookmarkEntity) => {
      BookmarkRepository.toggleFavorite(bookmark.id);
      reloadFromRepo();
    },
    [reloadFromRepo]
  );

  const moveBookmarkToFolder = useCallback(
    (newFolderId: number) => {
      if (!bookmarkToMove) return;
      BookmarkRepository.moveBookmark(bookmarkToMove.id, newFolderId);
      reloadFromRepo();
      setBookmarkToMove(null);
      showFeedback('تم نقل التغريدة إلى المجموعة الجديدة');
    },
    [bookmarkToMove, reloadFromRepo, showFeedback]
  );

  const deleteBookmark = useCallback(
    (bookmarkId: number) => {
      BookmarkRepository.deleteBookmark(bookmarkId);
      reloadFromRepo();
      showFeedback('تم حذف العلامة المرجعية');
    },
    [reloadFromRepo, showFeedback]
  );

  const createFolder = useCallback(
    (name: string, emoji: string, colorHex: string, description: string) => {
      if (!name.trim()) return;
      const created = BookmarkRepository.insertFolder({
        name: name.trim(),
        iconEmoji: emoji.trim() || '📁',
        colorHex: colorHex.trim() || '#1D9BF0',
        description: description.trim(),
        isDefault: false,
      });
      reloadFromRepo();
      setShowCreateFolderDialog(false);
      setPendingSelectedFolderId(created.id);
      showFeedback('تم إنشاء المجموعة بنجاح');
    },
    [reloadFromRepo, showFeedback]
  );

  const updateFolder = useCallback(
    (
      folder: FolderEntity,
      name: string,
      emoji: string,
      colorHex: string,
      description: string
    ) => {
      if (!name.trim()) return;
      BookmarkRepository.updateFolder({
        ...folder,
        name: name.trim(),
        iconEmoji: emoji.trim() || folder.iconEmoji,
        colorHex: colorHex.trim() || folder.colorHex,
        description: description.trim(),
      });
      reloadFromRepo();
      setFolderToEdit(null);
      showFeedback('تم تحديث بيانات المجموعة');
    },
    [reloadFromRepo, showFeedback]
  );

  const confirmDeleteFolder = useCallback(
    (moveToGeneral: boolean) => {
      if (!folderToDelete) return;
      BookmarkRepository.deleteFolder(folderToDelete.folder.id, moveToGeneral);
      if (selectedFolderId === folderToDelete.folder.id) {
        setSelectedFolderId(null);
      }
      reloadFromRepo();
      setFolderToDelete(null);
      showFeedback('تم حذف المجموعة بنجاح');
    },
    [folderToDelete, selectedFolderId, reloadFromRepo, showFeedback]
  );

  const exportBackupData = useCallback((): BackupPayload => {
    const payload = BookmarkRepository.exportBackup();
    const timestamp = BookmarkRepository.markBackupExported();
    setLastBackupAt(timestamp);
    return payload;
  }, []);

  const restoreBackupData = useCallback(
    (payload: BackupPayload, mode: 'replace' | 'merge') => {
      const result = BookmarkRepository.restoreBackup(payload, mode);
      reloadFromRepo();
      setSelectedFolderId(null);
      setShowFavoritesOnly(false);
      if (mode === 'replace') {
        showFeedback(
          `تمت استعادة ${result.foldersAdded} مجموعة و ${result.bookmarksAdded} علامة مرجعية بنجاح`
        );
      } else {
        showFeedback(
          `تم دمج ${result.foldersAdded} مجموعة جديدة و ${result.bookmarksAdded} علامة مرجعية جديدة`
        );
      }
      return result;
    },
    [reloadFromRepo, showFeedback]
  );

  return {
    folders: foldersWithCount,
    bookmarks: filteredBookmarks,
    totalBookmarksCount: bookmarks.length,
    selectedFolderId,
    showFavoritesOnly,
    searchQuery,
    activeTab,
    isSaveSheetOpen,
    pendingUrlInput,
    pendingParsedTweet,
    isFetchingTweet,
    isOfflinePreview,
    pendingSelectedFolderId: effectivePendingFolderId,
    pendingUserNote,
    pendingTags,
    refreshingBookmarkIds,
    showCreateFolderDialog,
    folderToEdit,
    folderToDelete,
    bookmarkToMove,
    userFeedbackMessage,
    lastBackupAt,
    setActiveTab,
    selectFolder,
    toggleFavoritesOnly,
    setSearchQuery,
    onReceiveSharedText,
    openManualAddSheet,
    closeSaveSheet,
    onUrlInputChanged,
    setPendingSelectedFolderId,
    setPendingUserNote,
    setPendingTags,
    savePendingBookmark,
    refreshSingleBookmark,
    showFeedback,
    clearFeedbackMessage,
    toggleFavorite,
    openMoveBookmarkDialog: setBookmarkToMove,
    closeMoveBookmarkDialog: () => setBookmarkToMove(null),
    moveBookmarkToFolder,
    deleteBookmark,
    openCreateFolderDialog: () => setShowCreateFolderDialog(true),
    closeCreateFolderDialog: () => setShowCreateFolderDialog(false),
    createFolder,
    openEditFolderDialog: setFolderToEdit,
    closeEditFolderDialog: () => setFolderToEdit(null),
    updateFolder,
    promptDeleteFolder: setFolderToDelete,
    closeDeleteFolderDialog: () => setFolderToDelete(null),
    confirmDeleteFolder,
    exportBackupData,
    restoreBackupData,
  };
}
