import { useEffect, useState } from 'react';
import { Bookmark, FolderPlus, FolderHeart, Plus } from 'lucide-react';
import { FolderCard } from './components/FolderCard';
import {
  CreateFolderDialog,
  DeleteFolderDialog,
  EditFolderDialog,
  MoveBookmarkDialog,
} from './components/FolderDialogs';
import { FolderFilterBar } from './components/FolderFilterBar';
import { SaveBookmarkSheet } from './components/SaveBookmarkSheet';
import { TopAppBarSection } from './components/TopAppBarSection';
import { TweetCard } from './components/TweetCard';
import { useBookmarksStore } from './hooks/useBookmarksStore';

export function App() {
  const store = useBookmarksStore();

  const [isDarkMode, setIsDarkMode] = useState<boolean>(() => {
    try {
      const saved = localStorage.getItem('xfolders_theme');
      if (saved === 'dark') return true;
      if (saved === 'light') return false;
      return window.matchMedia('(prefers-color-scheme: dark)').matches;
    } catch {
      return false;
    }
  });

  useEffect(() => {
    const root = document.documentElement;
    if (isDarkMode) {
      root.classList.add('dark');
    } else {
      root.classList.remove('dark');
    }
    try {
      localStorage.setItem('xfolders_theme', isDarkMode ? 'dark' : 'light');
    } catch {
      // ignore storage errors
    }
  }, [isDarkMode]);

  // Auto-dismiss snackbar toast after 3.2s
  useEffect(() => {
    if (!store.userFeedbackMessage) return;
    const timer = window.setTimeout(() => {
      store.clearFeedbackMessage();
    }, 3200);
    return () => window.clearTimeout(timer);
  }, [store.userFeedbackMessage, store]);

  return (
    <div
      dir="rtl"
      className="min-h-screen bg-[#F7F9F9] dark:bg-[#0D1117] text-[#0F1419] dark:text-[#F0F6FC] flex flex-col"
    >
      <TopAppBarSection
        totalBookmarks={store.totalBookmarksCount}
        searchQuery={store.searchQuery}
        onSearchQueryChange={store.setSearchQuery}
        activeTab={store.activeTab}
        onTabSelected={store.setActiveTab}
        isDarkMode={isDarkMode}
        onToggleTheme={() => setIsDarkMode((prev) => !prev)}
      />

      <main className="flex-1 w-full pb-24">
        {store.activeTab === 'BOOKMARKS' ? (
          <>
            <FolderFilterBar
              folders={store.folders}
              selectedFolderId={store.selectedFolderId}
              showFavoritesOnly={store.showFavoritesOnly}
              totalBookmarksCount={store.totalBookmarksCount}
              onSelectAll={() => store.selectFolder(null)}
              onToggleFavorites={store.toggleFavoritesOnly}
              onSelectFolder={(id) => store.selectFolder(id)}
              onAddNewFolder={store.openCreateFolderDialog}
            />

            <div className="max-w-6xl mx-auto px-4 sm:px-6 pt-1">
              {store.bookmarks.length === 0 ? (
                <EmptyBookmarksView
                  hasSearch={store.searchQuery.trim().length > 0}
                  onClearSearch={() => store.setSearchQuery('')}
                  onAddFirstBookmark={() => store.openManualAddSheet()}
                />
              ) : (
                <div className="grid grid-cols-1 lg:grid-cols-2 gap-4 items-start">
                  {store.bookmarks.map((bookmark) => {
                    const folder = store.folders.find(
                      (item) => item.folder.id === bookmark.folderId
                    )?.folder;
                    return (
                      <TweetCard
                        key={bookmark.id}
                        bookmark={bookmark}
                        folder={folder}
                        isRefreshing={store.refreshingBookmarkIds.has(bookmark.id)}
                        onRefreshContent={() =>
                          void store.refreshSingleBookmark(bookmark)
                        }
                        onToggleFavorite={() => store.toggleFavorite(bookmark)}
                        onMoveToFolder={() =>
                          store.openMoveBookmarkDialog(bookmark)
                        }
                        onDelete={() => store.deleteBookmark(bookmark.id)}
                        onFeedback={store.showFeedback}
                      />
                    );
                  })}
                </div>
              )}
            </div>
          </>
        ) : (
          <div className="max-w-6xl mx-auto px-4 sm:px-6 py-4 space-y-4">
            <FoldersManagerHeader
              totalFolders={store.folders.length}
              totalBookmarks={store.totalBookmarksCount}
              onCreateFolder={store.openCreateFolderDialog}
            />

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-3.5">
              {store.folders.map((item) => (
                <FolderCard
                  key={item.folder.id}
                  item={item}
                  onClick={() => store.selectFolder(item.folder.id)}
                  onEdit={() => store.openEditFolderDialog(item.folder)}
                  onDelete={() => store.promptDeleteFolder(item)}
                />
              ))}
            </div>
          </div>
        )}
      </main>

      {/* Floating Action Button */}
      <div className="fixed bottom-5 left-5 sm:bottom-7 sm:left-7 z-30">
        {store.activeTab === 'BOOKMARKS' ? (
          <button
            type="button"
            onClick={() => store.openManualAddSheet()}
            className="h-14 px-5 rounded-2xl bg-[#1D9BF0] hover:bg-[#1A8CD8] text-white font-bold text-sm sm:text-base shadow-lg shadow-[#1D9BF0]/30 flex items-center gap-2 transition-transform active:scale-95 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#1D9BF0]"
          >
            <Plus className="w-5 h-5" />
            <span>حفظ منشور</span>
          </button>
        ) : (
          <button
            type="button"
            onClick={store.openCreateFolderDialog}
            aria-label="إنشاء مجلد جديد"
            className="w-14 h-14 rounded-2xl bg-[#1D9BF0] hover:bg-[#1A8CD8] text-white shadow-lg shadow-[#1D9BF0]/30 flex items-center justify-center transition-transform active:scale-95 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#1D9BF0]"
          >
            <FolderPlus className="w-6 h-6" />
          </button>
        )}
      </div>

      {/* Snackbar Notification */}
      {store.userFeedbackMessage && (
        <div
          role="status"
          aria-live="polite"
          className="fixed bottom-24 sm:bottom-8 right-1/2 translate-x-1/2 z-50 px-4 py-3 rounded-xl bg-[#0F1419] dark:bg-[#F0F6FC] text-white dark:text-[#0F1419] text-sm font-medium shadow-xl max-w-[90vw] text-center animate-sheet-up"
        >
          {store.userFeedbackMessage}
        </div>
      )}

      {/* Save Bookmark Sheet */}
      {store.isSaveSheetOpen && (
        <SaveBookmarkSheet
          urlInput={store.pendingUrlInput}
          onUrlChange={store.onUrlInputChanged}
          parsedTweet={store.pendingParsedTweet}
          isFetchingTweet={store.isFetchingTweet}
          isOfflinePreview={store.isOfflinePreview}
          folders={store.folders}
          selectedFolderId={store.pendingSelectedFolderId}
          onSelectFolder={store.setPendingSelectedFolderId}
          userNote={store.pendingUserNote}
          onUserNoteChange={store.setPendingUserNote}
          tags={store.pendingTags}
          onTagsChange={store.setPendingTags}
          onOpenCreateFolder={store.openCreateFolderDialog}
          onSave={store.savePendingBookmark}
          onDismiss={store.closeSaveSheet}
        />
      )}

      {/* Folder & Move Dialogs */}
      {store.showCreateFolderDialog && (
        <CreateFolderDialog
          onDismiss={store.closeCreateFolderDialog}
          onConfirm={store.createFolder}
        />
      )}

      {store.folderToEdit && (
        <EditFolderDialog
          folder={store.folderToEdit}
          onDismiss={store.closeEditFolderDialog}
          onConfirm={(name, emoji, color, desc) =>
            store.updateFolder(store.folderToEdit!, name, emoji, color, desc)
          }
        />
      )}

      {store.folderToDelete && (
        <DeleteFolderDialog
          folderItem={store.folderToDelete}
          onDismiss={store.closeDeleteFolderDialog}
          onConfirm={store.confirmDeleteFolder}
        />
      )}

      {store.bookmarkToMove && (
        <MoveBookmarkDialog
          bookmark={store.bookmarkToMove}
          folders={store.folders}
          onDismiss={store.closeMoveBookmarkDialog}
          onConfirmMove={store.moveBookmarkToFolder}
        />
      )}
    </div>
  );
}

function FoldersManagerHeader({
  totalFolders,
  totalBookmarks,
  onCreateFolder,
}: {
  totalFolders: number;
  totalBookmarks: number;
  onCreateFolder: () => void;
}) {
  return (
    <div className="rounded-2xl bg-white dark:bg-[#161B22] border border-[#1D9BF0]/35 p-4 sm:p-5">
      <div className="flex flex-wrap items-center justify-between gap-3 mb-2.5">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-full bg-[#1D9BF0]/15 text-[#1D9BF0] flex items-center justify-center shrink-0">
            <FolderHeart className="w-5 h-5" />
          </div>
          <div>
            <h2 className="text-base font-bold text-[#0F1419] dark:text-[#F0F6FC]">
              مجموعات العلامات المرجعية
            </h2>
            <p className="text-xs text-[#536471] dark:text-[#8B949E] tabular-nums">
              {totalFolders} مجلدات · {totalBookmarks} علامة مرجعية
            </p>
          </div>
        </div>

        <button
          type="button"
          onClick={onCreateFolder}
          className="min-h-[38px] px-3.5 py-1.5 rounded-lg bg-[#1D9BF0] hover:bg-[#1A8CD8] text-white text-xs font-bold flex items-center gap-1.5 transition-colors whitespace-nowrap"
        >
          <Plus className="w-4 h-4" />
          <span>مجلد جديد</span>
        </button>
      </div>

      <p className="text-xs sm:text-sm text-[#536471] dark:text-[#8B949E] leading-relaxed">
        ميزة مجلدات العلامات المرجعية مستوحاة من ميزة X Premium لتنظيم تغريداتك المفضلة وسهولة الرجوع إليها في أي وقت.
      </p>
    </div>
  );
}

function EmptyBookmarksView({
  hasSearch,
  onClearSearch,
  onAddFirstBookmark,
}: {
  hasSearch: boolean;
  onClearSearch: () => void;
  onAddFirstBookmark: () => void;
}) {
  return (
    <div className="py-16 px-4 flex flex-col items-center justify-center text-center max-w-md mx-auto gap-3.5">
      <div className="w-20 h-20 rounded-full bg-[#1D9BF0]/12 border-2 border-[#1D9BF0]/30 text-[#1D9BF0] flex items-center justify-center">
        <Bookmark className="w-9 h-9" />
      </div>

      {hasSearch ? (
        <>
          <h3 className="text-base font-bold text-[#0F1419] dark:text-[#F0F6FC]">
            لم يتم العثور على نتائج مطابقة
          </h3>
          <p className="text-sm text-[#536471] dark:text-[#8B949E]">
            جرب البحث بكلمات أخرى أو مسح نص البحث الحالي
          </p>
          <button
            type="button"
            onClick={onClearSearch}
            className="min-h-[42px] px-5 py-2 rounded-xl bg-[#1D9BF0]/15 hover:bg-[#1D9BF0]/25 text-[#1D9BF0] font-bold text-sm transition-colors"
          >
            مسح البحث
          </button>
        </>
      ) : (
        <>
          <h3 className="text-base font-bold text-[#0F1419] dark:text-[#F0F6FC]">
            لا توجد علامات مرجعية في هذا المجلد
          </h3>
          <p className="text-sm text-[#536471] dark:text-[#8B949E] leading-relaxed">
            يمكنك حفظ أي تغريدة مباشرة من منصة 𝕏 عبر لصق رابط التغريدة هنا أو فتح التطبيق عبر رابط المشاركة.
          </p>
          <button
            type="button"
            onClick={onAddFirstBookmark}
            className="mt-1 min-h-[44px] px-5 py-2.5 rounded-xl bg-[#1D9BF0] hover:bg-[#1A8CD8] text-white font-bold text-sm flex items-center gap-2 transition-colors"
          >
            <Plus className="w-4 h-4" />
            <span>إضافة تغريدة الآن</span>
          </button>
        </>
      )}
    </div>
  );
}

export default App;
