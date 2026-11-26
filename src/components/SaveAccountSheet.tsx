import { useEffect, useState } from 'react';
import {
  AtSign,
  BadgeCheck,
  Check,
  CloudOff,
  Loader2,
  Plus,
  StickyNote,
  UserPlus,
  Users,
  X,
} from 'lucide-react';
import type { FolderWithCount, ParsedAccount } from '../types/bookmark';

interface SaveAccountSheetProps {
  accountInput: string;
  onAccountInputChange: (input: string) => void;
  parsedAccount: ParsedAccount | null;
  isFetchingAccount: boolean;
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

export function SaveAccountSheet({
  accountInput,
  onAccountInputChange,
  parsedAccount,
  isFetchingAccount,
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
}: SaveAccountSheetProps) {
  const [avatarError, setAvatarError] = useState(false);

  useEffect(() => {
    setAvatarError(false);
  }, [parsedAccount?.avatarUrl]);

  useEffect(() => {
    const onKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onDismiss();
    };
    window.addEventListener('keydown', onKeyDown);
    return () => window.removeEventListener('keydown', onKeyDown);
  }, [onDismiss]);

  const isSaveEnabled = parsedAccount !== null && selectedFolderId !== null;
  const fallbackInitial =
    parsedAccount?.displayName.trim().slice(0, 1) ||
    parsedAccount?.username.slice(0, 1).toUpperCase() ||
    '𝕏';

  return (
    <div
      role="dialog"
      aria-modal="true"
      aria-labelledby="save-account-sheet-title"
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
                <UserPlus className="w-5 h-5" />
              </div>
              <h2
                id="save-account-sheet-title"
                className="text-lg font-bold text-[#0F1419] dark:text-[#F0F6FC]"
              >
                حفظ حساب X في مجموعة
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

          {/* Account Handle / URL Input */}
          <div>
            <label className="block text-xs font-semibold text-[#536471] dark:text-[#8B949E] mb-1">
              معرف الحساب أو الرابط (@username أو https://x.com/...)
            </label>
            <div className="relative flex items-center">
              <AtSign className="w-4 h-4 text-[#1D9BF0] absolute right-3.5 pointer-events-none" />
              <input
                type="text"
                dir="ltr"
                value={accountInput}
                onChange={(e) => onAccountInputChange(e.target.value)}
                placeholder="@AndroidDev أو https://x.com/AndroidDev"
                autoFocus
                className="w-full h-11 pr-10 pl-3 rounded-xl border border-[#CFD9DE] dark:border-[#30363D] bg-[#EFF3F4]/25 dark:bg-[#21262D]/25 text-sm text-[#0F1419] dark:text-[#F0F6FC] font-mono focus:outline-none focus:border-[#1D9BF0]"
              />
            </div>
          </div>

          {/* Fetching Indicator OR Parsed Account Preview */}
          {isFetchingAccount ? (
            <div className="mt-3 rounded-xl bg-[#1D9BF0]/10 border border-[#1D9BF0]/25 p-3.5 flex items-center gap-3">
              <Loader2 className="w-5 h-5 text-[#1D9BF0] animate-spin shrink-0" />
              <span className="text-sm font-medium text-[#1D9BF0]">
                جاري جلب بيانات الحساب والنبذة التعريفية…
              </span>
            </div>
          ) : parsedAccount ? (
            <div className="mt-3 rounded-xl bg-[#EFF3F4]/55 dark:bg-[#21262D]/55 border border-[#E2E8F0] dark:border-[#30363D] p-3 space-y-2">
              <div className="flex items-center gap-2.5">
                <div className="w-9 h-9 rounded-full bg-[#1D9BF0] text-white font-bold flex items-center justify-center shrink-0 overflow-hidden">
                  {parsedAccount.avatarUrl && !avatarError ? (
                    <img
                      src={parsedAccount.avatarUrl}
                      alt={parsedAccount.displayName}
                      referrerPolicy="no-referrer"
                      onError={() => setAvatarError(true)}
                      className="w-full h-full object-cover"
                    />
                  ) : (
                    <span>{fallbackInitial}</span>
                  )}
                </div>
                <div className="min-w-0 flex-1">
                  <div className="flex items-center gap-1">
                    <p className="text-sm font-bold text-[#0F1419] dark:text-[#F0F6FC] truncate">
                      {parsedAccount.displayName}
                    </p>
                    {parsedAccount.verified && (
                      <BadgeCheck className="w-4 h-4 text-[#1D9BF0] shrink-0" />
                    )}
                  </div>
                  <p
                    dir="ltr"
                    className="text-xs text-[#536471] dark:text-[#8B949E] font-mono truncate text-right"
                  >
                    {parsedAccount.handle}
                  </p>
                </div>
              </div>

              {parsedAccount.isProfileFetched && parsedAccount.bio.trim() ? (
                <p
                  dir="auto"
                  className="text-xs sm:text-sm text-[#0F1419] dark:text-[#F0F6FC] line-clamp-3 whitespace-pre-line"
                >
                  {parsedAccount.bio}
                </p>
              ) : isOfflinePreview || !parsedAccount.isProfileFetched ? (
                <div className="flex items-center gap-1.5 text-xs text-[#536471] dark:text-[#8B949E]">
                  <CloudOff className="w-3.5 h-3.5 shrink-0" />
                  <span>
                    سيتم حفظ معرف الحساب في المجموعة ويمكنك تحديث بياناته لاحقاً
                  </span>
                </div>
              ) : null}

              {parsedAccount.followersCount > 0 && (
                <div className="flex items-center gap-1.5 text-xs text-[#536471] dark:text-[#8B949E] tabular-nums">
                  <Users className="w-3.5 h-3.5" />
                  <span>{parsedAccount.followersCount.toLocaleString('en-US')} متابِع</span>
                </div>
              )}
            </div>
          ) : accountInput.trim().length > 0 ? (
            <p className="mt-2 text-xs text-[#EF4444]">
              أدخل معرف حساب X صحيح (مثل @AndroidDev) أو رابط ملف شخصي على منصة X.
            </p>
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
                placeholder="مثال: حساب متخصص في تطوير التطبيقات…"
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
              placeholder="تقنية, ذكاء_اصطناعي, برمجة"
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
            <UserPlus className="w-5 h-5" />
            <span>حفظ الحساب في المجموعة الآن</span>
          </button>
        </div>
      </div>
    </div>
  );
}
