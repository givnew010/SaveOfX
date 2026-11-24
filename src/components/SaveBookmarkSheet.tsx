import { useEffect, useState } from 'react';
import {
  BookmarkPlus,
  Check,
  CloudOff,
  Image as ImageIcon,
  Link2,
  Loader2,
  Plus,
  StickyNote,
  Video,
  X,
} from 'lucide-react';
import type { FolderWithCount, ParsedTweet } from '../types/bookmark';

interface SaveBookmarkSheetProps {
  urlInput: string;
  onUrlChange: (url: string) => void;
  parsedTweet: ParsedTweet | null;
  isFetchingTweet: boolean;
  isOfflinePreview: boolean;
  folders: FolderWithCount[];
  selectedFolderId: number | null;
  onSelectFolder: (folderId: number) => void;
  userNote: string;
  onUserNoteChange: (note: string) => void;
  tags: string;
  onTagsChange: (tags: string) => void;
  onOpenCreateFolder: () => void;
  onSave: () => void;
  onDismiss: () => void;
}

export function SaveBookmarkSheet({
  urlInput,
  onUrlChange,
  parsedTweet,
  isFetchingTweet,
  isOfflinePreview,
  folders,
  selectedFolderId,
  onSelectFolder,
  userNote,
  onUserNoteChange,
  tags,
  onTagsChange,
  onOpenCreateFolder,
  onSave,
  onDismiss,
}: SaveBookmarkSheetProps) {
  const [avatarError, setAvatarError] = useState(false);

  useEffect(() => {
    setAvatarError(false);
  }, [parsedTweet?.authorAvatarUrl]);

  useEffect(() => {
    const onKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onDismiss();
    };
    window.addEventListener('keydown', onKeyDown);
    return () => window.removeEventListener('keydown', onKeyDown);
  }, [onDismiss]);

  const isSaveEnabled = urlInput.trim().length > 0 && selectedFolderId !== null;
  const photosCount =
    parsedTweet?.mediaItems.filter((item) => item.type === 'photo').length ?? 0;
  const videosCount =
    parsedTweet?.mediaItems.filter(
      (item) => item.type === 'video' || item.type === 'gif'
    ).length ?? 0;

  return (
    <div
      role="dialog"
      aria-modal="true"
      aria-labelledby="save-sheet-title"
      onClick={onDismiss}
      className="fixed inset-0 z-40 bg-black/55 backdrop-blur-xs flex items-end sm:items-center justify-center sm:p-4"
    >
      <div
        onClick={(e) => e.stopPropagation()}
        className="w-full sm:max-w-lg rounded-t-3xl sm:rounded-2xl bg-white dark:bg-[#161B22] border-t sm:border border-[#E2E8F0] dark:border-[#30363D] shadow-2xl max-h-[90vh] overflow-y-auto animate-sheet-up"
      >
        {/* Drag Handle (mobile) */}
        <div className="sm:hidden flex justify-center pt-2.5 pb-1">
          <span className="w-10 h-1 rounded-full bg-[#E2E8F0] dark:bg-[#30363D]" />
        </div>

        <div className="p-5 pb-7">
          {/* Header */}
          <div className="flex items-center justify-between gap-3 mb-4">
            <div className="flex items-center gap-2.5">
              <div className="w-9 h-9 rounded-full bg-[#1D9BF0]/15 text-[#1D9BF0] flex items-center justify-center shrink-0">
                <BookmarkPlus className="w-5 h-5" />
              </div>
              <h2
                id="save-sheet-title"
                className="text-lg font-bold text-[#0F1419] dark:text-[#F0F6FC]"
              >
                حفظ التغريدة في مجموعة
              </h2>
            </div>

            <button
              type="button"
              onClick={onDismiss}
              aria-label="إغلاق"
              className="min-w-[44px] min-h-[44px] rounded-full flex items-center justify-center text-[#536471] dark:text-[#8B949E] hover:bg-[#EFF3F4] dark:hover:bg-[#21262D] transition-colors"
            >
              <X className="w-5 h-5" />
            </button>
          </div>

          {/* URL Input */}
          <div>
            <label className="block text-xs font-semibold text-[#536471] dark:text-[#8B949E] mb-1">
              رابط التغريدة (X / Twitter)
            </label>
            <div className="relative flex items-center">
              <Link2 className="w-4 h-4 text-[#1D9BF0] absolute right-3.5 pointer-events-none" />
              <input
                type="url"
                dir="ltr"
                value={urlInput}
                onChange={(e) => onUrlChange(e.target.value)}
                placeholder="https://x.com/username/status/..."
                autoFocus
                className="w-full h-11 pr-10 pl-3 rounded-xl border border-[#CFD9DE] dark:border-[#30363D] bg-[#EFF3F4]/25 dark:bg-[#21262D]/25 text-sm text-[#0F1419] dark:text-[#F0F6FC] font-mono focus:outline-none focus:border-[#1D9BF0]"
              />
            </div>
          </div>

          {/* Fetching Indicator OR Parsed Tweet Preview */}
          {isFetchingTweet ? (
            <div className="mt-3 rounded-xl bg-[#1D9BF0]/10 border border-[#1D9BF0]/25 p-3.5 flex items-center gap-3">
              <Loader2 className="w-5 h-5 text-[#1D9BF0] animate-spin shrink-0" />
              <span className="text-sm font-medium text-[#1D9BF0]">
                جاري جلب نص التغريدة والوسائط المرفقة…
              </span>
            </div>
          ) : parsedTweet ? (
            <div className="mt-3 rounded-xl bg-[#EFF3F4]/55 dark:bg-[#21262D]/55 border border-[#E2E8F0] dark:border-[#30363D] p-3 space-y-2">
              <div className="flex items-center gap-2.5">
                {parsedTweet.authorAvatarUrl && !avatarError && (
                  <img
                    src={parsedTweet.authorAvatarUrl}
                    alt={parsedTweet.authorName}
                    referrerPolicy="no-referrer"
                    onError={() => setAvatarError(true)}
                    className="w-8 h-8 rounded-full object-cover shrink-0"
                  />
                )}
                <div className="min-w-0">
                  <p className="text-sm font-bold text-[#0F1419] dark:text-[#F0F6FC] truncate">
                    {parsedTweet.authorName}
                  </p>
                  <p
                    dir="ltr"
                    className="text-xs text-[#536471] dark:text-[#8B949E] font-mono truncate text-right"
                  >
                    {parsedTweet.authorHandle}
                  </p>
                </div>
              </div>

              {parsedTweet.isContentFetched && parsedTweet.content.trim() ? (
                <p
                  dir="auto"
                  className="text-xs sm:text-sm text-[#0F1419] dark:text-[#F0F6FC] line-clamp-4 whitespace-pre-line"
                >
                  {parsedTweet.content}
                </p>
              ) : (isOfflinePreview || !parsedTweet.isContentFetched) ? (
                <div className="flex items-center gap-1.5 text-xs text-[#536471] dark:text-[#8B949E]">
                  <CloudOff className="w-3.5 h-3.5 shrink-0" />
                  <span>
                    لا يوجد اتصال بالإنترنت — سيتم حفظ الرابط فقط وعرضه لاحقاً عند التوفر
                  </span>
                </div>
              ) : null}

              {parsedTweet.mediaItems.length > 0 && (
                <div className="flex flex-wrap items-center gap-2 pt-1">
                  {photosCount > 0 && (
                    <span className="inline-flex items-center gap-1 rounded-lg bg-[#1D9BF0]/15 text-[#1D9BF0] px-2 py-1 text-xs font-semibold">
                      <ImageIcon className="w-3.5 h-3.5" />
                      <span>{photosCount} صور مرفقة</span>
                    </span>
                  )}
                  {videosCount > 0 && (
                    <span className="inline-flex items-center gap-1 rounded-lg bg-[#8B5CF6]/15 text-[#8B5CF6] px-2 py-1 text-xs font-semibold">
                      <Video className="w-3.5 h-3.5" />
                      <span>فيديو / GIF مرفق</span>
                    </span>
                  )}
                </div>
              )}
            </div>
          ) : null}

          {/* Folder Selection */}
          <div className="mt-4">
            <div className="flex items-center justify-between mb-2">
              <span className="text-sm font-bold text-[#0F1419] dark:text-[#F0F6FC]">
                اختر مجموعة الحفظ:
              </span>
              <button
                type="button"
                onClick={onOpenCreateFolder}
                className="inline-flex items-center gap-1 px-2 py-1 rounded-lg text-xs font-bold text-[#1D9BF0] hover:bg-[#1D9BF0]/10 transition-colors"
              >
                <Plus className="w-4 h-4" />
                <span>مجموعة جديدة</span>
              </button>
            </div>

            <div className="flex flex-wrap gap-2">
              {folders.map(({ folder }) => {
                const isSelected = selectedFolderId === folder.id;
                const color = folder.colorHex || '#1D9BF0';
                return (
                  <button
                    key={folder.id}
                    type="button"
                    onClick={() => onSelectFolder(folder.id)}
                    style={
                      isSelected
                        ? {
                            backgroundColor: `${color}28`,
                            borderColor: color,
                            color,
                          }
                        : undefined
                    }
                    className={`min-h-[40px] px-3 py-1.5 rounded-xl flex items-center gap-1.5 text-sm transition-colors ${
                      isSelected
                        ? 'border-2 font-bold'
                        : 'border border-[#E2E8F0] dark:border-[#30363D] bg-[#EFF3F4]/55 dark:bg-[#21262D]/55 text-[#0F1419] dark:text-[#F0F6FC] font-medium hover:bg-[#EFF3F4] dark:hover:bg-[#21262D]'
                    }`}
                  >
                    <span>{folder.iconEmoji}</span>
                    <span>{folder.name}</span>
                    {isSelected && <Check className="w-4 h-4 shrink-0" />}
                  </button>
                );
              })}
            </div>
          </div>

          {/* User Note Field */}
          <div className="mt-4">
            <label className="block text-xs font-semibold text-[#536471] dark:text-[#8B949E] mb-1">
              أضف ملاحظة شخصية (اختياري)
            </label>
            <div className="relative">
              <StickyNote className="w-4 h-4 text-[#536471] dark:text-[#8B949E] absolute right-3.5 top-3.5 pointer-events-none" />
              <textarea
                value={userNote}
                onChange={(e) => onUserNoteChange(e.target.value)}
                placeholder="مثال: ثريد مفيد لتطوير التطبيقات…"
                rows={2}
                className="w-full pr-10 pl-3 py-2.5 rounded-xl border border-[#CFD9DE] dark:border-[#30363D] bg-[#EFF3F4]/25 dark:bg-[#21262D]/25 text-sm text-[#0F1419] dark:text-[#F0F6FC] focus:outline-none focus:border-[#1D9BF0] resize-none"
              />
            </div>
          </div>

          {/* Tags Field */}
          <div className="mt-3">
            <label className="block text-xs font-semibold text-[#536471] dark:text-[#8B949E] mb-1">
              وسوم التصنيف (مفصولة بفواصل)
            </label>
            <input
              type="text"
              value={tags}
              onChange={(e) => onTagsChange(e.target.value)}
              placeholder="ذكاء_اصطناعي, تقنية, برمجة"
              className="w-full h-11 px-3 rounded-xl border border-[#CFD9DE] dark:border-[#30363D] bg-[#EFF3F4]/25 dark:bg-[#21262D]/25 text-sm text-[#0F1419] dark:text-[#F0F6FC] focus:outline-none focus:border-[#1D9BF0]"
            />
          </div>

          {/* Primary Save Button */}
          <button
            type="button"
            disabled={!isSaveEnabled}
            onClick={onSave}
            className="mt-5 w-full h-12 rounded-xl bg-[#1D9BF0] hover:bg-[#1A8CD8] disabled:opacity-50 text-white font-bold text-base flex items-center justify-center gap-2 transition-colors"
          >
            <BookmarkPlus className="w-5 h-5" />
            <span>حفظ في المجموعة الآن</span>
          </button>
        </div>
      </div>
    </div>
  );
}
