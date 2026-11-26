import { useEffect, useLayoutEffect, useMemo, useRef, useState } from 'react';
import {
  BadgeCheck,
  Copy,
  ExternalLink,
  FolderInput,
  Heart,
  Loader2,
  RefreshCw,
  Share2,
  StickyNote,
  Trash2,
  UserCheck,
  Users,
} from 'lucide-react';
import type { AccountBookmarkEntity, FolderEntity } from '../types/bookmark';

interface AccountCardProps {
  account: AccountBookmarkEntity;
  folder?: FolderEntity;
  isRefreshing: boolean;
  onRefreshProfile: () => void;
  onToggleFavorite: () => void;
  onMoveToFolder: () => void;
  onDelete: () => void;
  onFeedback: (message: string) => void;
}

function formatCompactCount(count: number): string {
  if (count >= 1_000_000) {
    return `${(count / 1_000_000).toFixed(1)}M`;
  }
  if (count >= 1_000) {
    return `${(count / 1_000).toFixed(1)}K`;
  }
  return String(count);
}

export function AccountCard({
  account,
  folder,
  isRefreshing,
  onRefreshProfile,
  onToggleFavorite,
  onMoveToFolder,
  onDelete,
  onFeedback,
}: AccountCardProps) {
  const [avatarError, setAvatarError] = useState(false);
  const [isBioExpanded, setIsBioExpanded] = useState(false);
  const [isBioTruncated, setIsBioTruncated] = useState(false);
  const bioRef = useRef<HTMLParagraphElement | null>(null);

  const folderColor = folder?.colorHex || '#1D9BF0';
  const initial = account.displayName.trim().slice(0, 1) || account.handle.replace(/^@/, '').slice(0, 1) || '𝕏';

  useEffect(() => {
    setAvatarError(false);
  }, [account.avatarUrl]);

  useEffect(() => {
    setIsBioExpanded(false);
    setIsBioTruncated(false);
  }, [account.id, account.bio]);

  useLayoutEffect(() => {
    const el = bioRef.current;
    if (!el || isBioExpanded) return;

    const checkOverflow = () => {
      setIsBioTruncated(el.scrollHeight > el.clientHeight + 1);
    };

    checkOverflow();

    if (typeof ResizeObserver === 'undefined') return;
    const observer = new ResizeObserver(checkOverflow);
    observer.observe(el);
    return () => observer.disconnect();
  }, [account.bio, isBioExpanded]);

  const tagsList = useMemo(
    () =>
      account.tags
        .split(',')
        .map((t) => t.trim())
        .filter((t) => t.length > 0),
    [account.tags]
  );

  const handleCopyLink = async () => {
    try {
      await navigator.clipboard.writeText(account.profileUrl);
      onFeedback('تم نسخ رابط الحساب إلى الحافظة');
    } catch {
      onFeedback('تعذر نسخ الرابط');
    }
  };

  const handleShare = async () => {
    const shareBody = `${account.displayName} (${account.handle})\n${account.profileUrl}`;

    if (typeof navigator !== 'undefined' && typeof navigator.share === 'function') {
      try {
        await navigator.share({
          title: account.displayName,
          text: shareBody,
          url: account.profileUrl,
        });
        return;
      } catch {
        // Fallback to clipboard
      }
    }

    try {
      await navigator.clipboard.writeText(shareBody);
      onFeedback('تم نسخ رابط الحساب للمشاركة');
    } catch {
      onFeedback('تعذر مشاركة الحساب');
    }
  };

  return (
    <article className="flex flex-col justify-between rounded-2xl bg-white dark:bg-[#161B22] border border-[#E2E8F0] dark:border-[#30363D] p-4 shadow-xs transition-colors">
      <div>
        {/* Header: Profile Avatar & Handle */}
        <div className="flex items-center justify-between gap-3">
          <div className="flex items-center gap-3 min-w-0 flex-1">
            <div
              style={{
                background: `linear-gradient(135deg, ${folderColor}, #1D9BF0)`,
              }}
              className="w-[46px] h-[46px] rounded-full shrink-0 overflow-hidden flex items-center justify-center text-white font-bold text-lg select-none"
            >
              {account.avatarUrl && !avatarError ? (
                <img
                  src={account.avatarUrl}
                  alt={account.displayName}
                  referrerPolicy="no-referrer"
                  onError={() => setAvatarError(true)}
                  className="w-full h-full object-cover"
                />
              ) : (
                <span>{initial}</span>
              )}
            </div>

            <div className="min-w-0 flex-1">
              <div className="flex items-center gap-1 min-w-0">
                <span className="text-sm sm:text-base font-bold text-[#0F1419] dark:text-[#F0F6FC] truncate">
                  {account.displayName}
                </span>
                {account.verified && (
                  <BadgeCheck
                    className="w-4 h-4 text-[#1D9BF0] shrink-0"
                    aria-label="حساب موثق"
                  />
                )}
              </div>

              <div className="flex items-center gap-1.5 text-xs text-[#536471] dark:text-[#8B949E] min-w-0">
                <span dir="ltr" className="truncate font-mono text-[12px]">
                  {account.handle}
                </span>
              </div>
            </div>
          </div>

          <div className="flex items-center gap-1.5 shrink-0">
            {!account.isProfileFetched && (
              <button
                type="button"
                disabled={isRefreshing}
                onClick={onRefreshProfile}
                className="min-h-[32px] px-2.5 py-1 rounded-lg bg-[#1D9BF0]/12 hover:bg-[#1D9BF0]/20 text-[#1D9BF0] text-xs font-bold flex items-center gap-1 transition-colors disabled:opacity-60"
              >
                {isRefreshing ? (
                  <Loader2 className="w-3.5 h-3.5 animate-spin" />
                ) : (
                  <>
                    <RefreshCw className="w-3.5 h-3.5" />
                    <span>جلب</span>
                  </>
                )}
              </button>
            )}

            <span className="px-2 py-1 rounded-md bg-[#EFF3F4] dark:bg-[#21262D] text-[#0F1419] dark:text-[#F0F6FC] text-xs font-black select-none">
              𝕏
            </span>
          </div>
        </div>

        {/* Bio */}
        <div className="mt-3">
          {account.bio.trim() ? (
            <div>
              <p
                ref={bioRef}
                dir="auto"
                className={`text-[14px] leading-[1.6] text-[#0F1419] dark:text-[#F0F6FC] whitespace-pre-line break-words ${
                  isBioExpanded ? '' : 'line-clamp-4'
                }`}
              >
                {account.bio}
              </p>
              {(isBioTruncated || isBioExpanded) && (
                <button
                  type="button"
                  onClick={() => setIsBioExpanded((prev) => !prev)}
                  className="mt-1 inline-block text-sm font-medium text-[#1D9BF0] hover:underline rounded"
                >
                  {isBioExpanded ? 'عرض أقل' : 'عرض المزيد'}
                </button>
              )}
            </div>
          ) : (
            <p className="text-xs text-[#536471] dark:text-[#8B949E]">
              {account.isProfileFetched
                ? 'لا توجد نبذة تعريفية لهذا الحساب.'
                : 'تم حفظ معرف الحساب — اضغط على «جلب» لتحديث بيانات الملف الشخصي.'}
            </p>
          )}

          {/* Followers / Following stats */}
          {(account.followersCount > 0 || account.followingCount > 0) && (
            <div className="mt-2.5 flex items-center gap-4 text-xs text-[#536471] dark:text-[#8B949E] tabular-nums">
              {account.followingCount > 0 && (
                <span className="inline-flex items-center gap-1">
                  <UserCheck className="w-3.5 h-3.5" />
                  <strong className="text-[#0F1419] dark:text-[#F0F6FC]">
                    {formatCompactCount(account.followingCount)}
                  </strong>
                  <span>يتابع</span>
                </span>
              )}
              {account.followersCount > 0 && (
                <span className="inline-flex items-center gap-1">
                  <Users className="w-3.5 h-3.5" />
                  <strong className="text-[#0F1419] dark:text-[#F0F6FC]">
                    {formatCompactCount(account.followersCount)}
                  </strong>
                  <span>متابِع</span>
                </span>
              )}
            </div>
          )}

          {/* Optional Personal Note */}
          {account.userNote && account.userNote.trim() && (
            <div className="mt-2.5 rounded-xl bg-[#F59E0B]/12 border border-[#F59E0B]/30 p-2.5 flex items-start gap-2">
              <StickyNote className="w-4 h-4 text-[#D97706] shrink-0 mt-0.5" />
              <div className="min-w-0">
                <p className="text-[11px] font-bold text-[#D97706]">ملاحظتي الخاصة:</p>
                <p className="text-sm text-[#0F1419] dark:text-[#F0F6FC] whitespace-pre-line">
                  {account.userNote}
                </p>
              </div>
            </div>
          )}

          {/* Folder Label & Tags */}
          {(folder || tagsList.length > 0) && (
            <div className="mt-3 flex flex-wrap items-center gap-1.5">
              {folder && (
                <span
                  style={{
                    backgroundColor: `${folderColor}1A`,
                    borderColor: `${folderColor}55`,
                    color: folderColor,
                  }}
                  className="inline-flex items-center gap-1 rounded-xl border px-2.5 py-1 text-xs font-semibold"
                >
                  <span>{folder.iconEmoji}</span>
                  <span>{folder.name}</span>
                </span>
              )}

              {tagsList.map((tag, i) => (
                <span
                  key={`${tag}-${i}`}
                  dir="auto"
                  className="inline-flex items-center rounded-xl bg-[#EFF3F4]/80 dark:bg-[#21262D]/80 px-2.5 py-1 text-xs font-medium text-[#1D9BF0]"
                >
                  #{tag}
                </span>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Bottom Actions Row */}
      <div className="mt-3 pt-1">
        <div className="flex items-center justify-between rounded-xl bg-[#EFF3F4]/50 dark:bg-[#21262D]/50 px-1 py-0.5">
          <a
            href={account.profileUrl}
            target="_blank"
            rel="noopener noreferrer"
            aria-label="فتح الحساب في منصة X"
            title="فتح الحساب في منصة X"
            className="min-w-[44px] min-h-[44px] rounded-lg flex items-center justify-center text-[#1D9BF0] hover:bg-[#1D9BF0]/10 transition-colors"
          >
            <ExternalLink className="w-5 h-5" />
          </a>

          <button
            type="button"
            onClick={onToggleFavorite}
            aria-label="المفضلة"
            title="المفضلة"
            className={`min-w-[44px] min-h-[44px] rounded-lg flex items-center justify-center transition-colors ${
              account.isFavorite
                ? 'text-[#EF4444] hover:bg-[#EF4444]/10'
                : 'text-[#536471] dark:text-[#8B949E] hover:bg-black/5 dark:hover:bg-white/5'
            }`}
          >
            <Heart className={`w-5 h-5 ${account.isFavorite ? 'fill-[#EF4444]' : ''}`} />
          </button>

          <button
            type="button"
            onClick={onMoveToFolder}
            aria-label="تغيير المجموعة"
            title="تغيير المجموعة"
            className="min-w-[44px] min-h-[44px] rounded-lg flex items-center justify-center text-[#536471] dark:text-[#8B949E] hover:bg-black/5 dark:hover:bg-white/5 transition-colors"
          >
            <FolderInput className="w-5 h-5" />
          </button>

          <button
            type="button"
            onClick={() => void handleCopyLink()}
            aria-label="نسخ رابط الحساب"
            title="نسخ رابط الحساب"
            className="min-w-[44px] min-h-[44px] rounded-lg flex items-center justify-center text-[#536471] dark:text-[#8B949E] hover:bg-black/5 dark:hover:bg-white/5 transition-colors"
          >
            <Copy className="w-5 h-5" />
          </button>

          <button
            type="button"
            onClick={() => void handleShare()}
            aria-label="مشاركة الحساب"
            title="مشاركة الحساب"
            className="min-w-[44px] min-h-[44px] rounded-lg flex items-center justify-center text-[#536471] dark:text-[#8B949E] hover:bg-black/5 dark:hover:bg-white/5 transition-colors"
          >
            <Share2 className="w-5 h-5" />
          </button>

          <button
            type="button"
            onClick={onDelete}
            aria-label="حذف الحساب"
            title="حذف الحساب"
            className="min-w-[44px] min-h-[44px] rounded-lg flex items-center justify-center text-[#EF4444]/85 hover:bg-[#EF4444]/10 transition-colors"
          >
            <Trash2 className="w-5 h-5" />
          </button>
        </div>
      </div>
    </article>
  );
}
