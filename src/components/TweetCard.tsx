import { useEffect, useLayoutEffect, useMemo, useRef, useState } from 'react';
import {
  BadgeCheck,
  CloudOff,
  Copy,
  ExternalLink,
  FolderInput,
  Heart,
  Link2,
  Loader2,
  RefreshCw,
  Repeat2,
  Share2,
  StickyNote,
  Trash2,
} from 'lucide-react';
import type { FolderEntity, TweetBookmarkEntity, TweetMediaItem } from '../types/bookmark';
import { TweetParser } from '../utils/tweetParser';
import { TweetMediaGallery } from './MediaPreviewAndPlayer';

interface TweetCardProps {
  bookmark: TweetBookmarkEntity;
  folder?: FolderEntity;
  isRefreshing: boolean;
  onRefreshContent: () => void;
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

export function TweetCard({
  bookmark,
  folder,
  isRefreshing,
  onRefreshContent,
  onToggleFavorite,
  onMoveToFolder,
  onDelete,
  onFeedback,
}: TweetCardProps) {
  const [avatarError, setAvatarError] = useState(false);
  const [isTextExpanded, setIsTextExpanded] = useState(false);
  const [isTextTruncated, setIsTextTruncated] = useState(false);
  const contentRef = useRef<HTMLParagraphElement | null>(null);

  const folderColor = folder?.colorHex || '#1D9BF0';
  const initial = bookmark.authorName.trim().slice(0, 1) || '𝕏';

  useEffect(() => {
    setIsTextExpanded(false);
    setIsTextTruncated(false);
  }, [bookmark.id, bookmark.content]);

  useLayoutEffect(() => {
    const el = contentRef.current;
    if (!el || isTextExpanded) return;

    const checkOverflow = () => {
      setIsTextTruncated(el.scrollHeight > el.clientHeight + 1);
    };

    checkOverflow();

    if (typeof ResizeObserver === 'undefined') return;
    const observer = new ResizeObserver(checkOverflow);
    observer.observe(el);
    return () => observer.disconnect();
  }, [bookmark.content, bookmark.isContentFetched, isTextExpanded]);

  const mediaItems: TweetMediaItem[] = useMemo(() => {
    const parsed = TweetParser.deserializeMediaItems(bookmark.mediaJson);
    if (parsed.length > 0) return parsed;
    if (bookmark.mediaUrl && bookmark.mediaUrl.trim()) {
      return [
        {
          type: bookmark.mediaType ?? 'photo',
          url: bookmark.mediaUrl,
        },
      ];
    }
    return [];
  }, [bookmark.mediaJson, bookmark.mediaUrl, bookmark.mediaType]);

  const tagsList = useMemo(
    () =>
      bookmark.tags
        .split(',')
        .map((t) => t.trim())
        .filter((t) => t.length > 0),
    [bookmark.tags]
  );

  const handleCopyLink = async () => {
    try {
      await navigator.clipboard.writeText(bookmark.tweetUrl);
      onFeedback('تم نسخ رابط التغريدة إلى الحافظة');
    } catch {
      onFeedback('تعذر نسخ الرابط');
    }
  };

  const handleShare = async () => {
    const shareBody =
      bookmark.isContentFetched && bookmark.content !== bookmark.tweetUrl
        ? `${bookmark.content}\n\n${bookmark.tweetUrl}`
        : bookmark.tweetUrl;

    if (typeof navigator !== 'undefined' && typeof navigator.share === 'function') {
      try {
        await navigator.share({
          title: bookmark.authorName,
          text: shareBody,
          url: bookmark.tweetUrl,
        });
        return;
      } catch {
        // Fallback to clipboard if user cancels or share fails
      }
    }

    try {
      await navigator.clipboard.writeText(shareBody);
      onFeedback('تم نسخ نص ورابط المنشور للمشاركة');
    } catch {
      onFeedback('تعذر مشاركة المنشور');
    }
  };

  return (
    <article
      style={{
        borderColor: undefined,
      }}
      className="flex flex-col justify-between rounded-2xl bg-white dark:bg-[#161B22] border border-[#E2E8F0] dark:border-[#30363D] p-4 shadow-xs transition-colors"
    >
      <div>
        {/* Header: Author details & X mark */}
        <div className="flex items-center justify-between gap-3">
          <div className="flex items-center gap-2.5 min-w-0 flex-1">
            <div
              style={{
                background: `linear-gradient(135deg, ${folderColor}, #1D9BF0)`,
              }}
              className="w-[42px] h-[42px] rounded-full shrink-0 overflow-hidden flex items-center justify-center text-white font-bold text-base select-none"
            >
              {bookmark.authorAvatarUrl && !avatarError ? (
                <img
                  src={bookmark.authorAvatarUrl}
                  alt={bookmark.authorName}
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
                  {bookmark.authorName}
                </span>
                <BadgeCheck
                  className="w-4 h-4 text-[#1D9BF0] shrink-0"
                  aria-label="حساب موثق"
                />
              </div>

              <div className="flex items-center gap-1.5 text-xs text-[#536471] dark:text-[#8B949E] min-w-0">
                <span dir="ltr" className="truncate font-mono text-[12px]">
                  {bookmark.authorHandle}
                </span>
                {bookmark.postedAt && (
                  <>
                    <span aria-hidden="true">·</span>
                    <span className="whitespace-nowrap shrink-0">
                      {bookmark.postedAt}
                    </span>
                  </>
                )}
              </div>
            </div>
          </div>

          <div className="flex items-center gap-1.5 shrink-0">
            {!bookmark.isContentFetched && (
              <button
                type="button"
                disabled={isRefreshing}
                onClick={onRefreshContent}
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

        {/* Body */}
        <div className="mt-3">
          {bookmark.isContentFetched ? (
            <>
              {bookmark.content.trim() && bookmark.content !== bookmark.tweetUrl && (
                <div>
                  <p
                    ref={contentRef}
                    dir="auto"
                    className={`text-[15px] leading-[1.6] text-[#0F1419] dark:text-[#F0F6FC] whitespace-pre-line break-words ${
                      isTextExpanded ? '' : 'line-clamp-6'
                    }`}
                  >
                    {bookmark.content}
                  </p>
                  {(isTextTruncated || isTextExpanded) && (
                    <button
                      type="button"
                      onClick={() => setIsTextExpanded((prev) => !prev)}
                      className="mt-1 inline-block text-sm font-medium text-[#1D9BF0] hover:underline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#1D9BF0] rounded"
                    >
                      {isTextExpanded ? 'عرض أقل' : 'عرض المزيد'}
                    </button>
                  )}
                </div>
              )}

              {mediaItems.length > 0 && (
                <div className="mt-2.5">
                  <TweetMediaGallery
                    mediaItems={mediaItems}
                    tweetUrl={bookmark.tweetUrl}
                  />
                </div>
              )}

              {bookmark.quoteText && bookmark.quoteText.trim() && (
                <div className="mt-2.5 rounded-xl bg-[#EFF3F4]/45 dark:bg-[#21262D]/50 border border-[#E2E8F0] dark:border-[#30363D] p-3">
                  <div className="flex items-center gap-1.5 mb-1">
                    <span className="text-xs font-bold text-[#0F1419] dark:text-[#F0F6FC]">
                      {bookmark.quoteAuthorName || 'اقتباس'}
                    </span>
                    {bookmark.quoteAuthorHandle && (
                      <span
                        dir="ltr"
                        className="text-xs text-[#536471] dark:text-[#8B949E] font-mono"
                      >
                        {bookmark.quoteAuthorHandle}
                      </span>
                    )}
                  </div>
                  <p
                    dir="auto"
                    className="text-sm text-[#0F1419] dark:text-[#F0F6FC] line-clamp-4 whitespace-pre-line"
                  >
                    {bookmark.quoteText}
                  </p>
                </div>
              )}

              {(bookmark.likesCount > 0 || bookmark.repostsCount > 0) && (
                <div className="mt-2.5 flex items-center gap-4 text-xs text-[#536471] dark:text-[#8B949E] tabular-nums">
                  {bookmark.repostsCount > 0 && (
                    <span className="inline-flex items-center gap-1">
                      <Repeat2 className="w-3.5 h-3.5" />
                      <span>{formatCompactCount(bookmark.repostsCount)}</span>
                    </span>
                  )}
                  {bookmark.likesCount > 0 && (
                    <span className="inline-flex items-center gap-1">
                      <Heart className="w-3.5 h-3.5" />
                      <span>{formatCompactCount(bookmark.likesCount)}</span>
                    </span>
                  )}
                </div>
              )}
            </>
          ) : (
            <a
              href={bookmark.tweetUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="block rounded-xl bg-[#EFF3F4]/50 dark:bg-[#21262D]/50 border border-[#E2E8F0] dark:border-[#30363D] p-3 hover:border-[#1D9BF0]/50 transition-colors"
            >
              <div className="flex items-center gap-1.5 text-xs text-[#536471] dark:text-[#8B949E] mb-1.5">
                <CloudOff className="w-3.5 h-3.5 shrink-0" />
                <span>يعرض الرابط فقط (يتطلب إنترنت لجلب المحتوى)</span>
              </div>
              <div className="flex items-center gap-1.5 text-[#1D9BF0] text-sm">
                <Link2 className="w-4 h-4 shrink-0" />
                <span dir="ltr" className="underline truncate font-mono text-xs">
                  {bookmark.tweetUrl}
                </span>
              </div>
            </a>
          )}

          {/* Optional Personal Note */}
          {bookmark.userNote && bookmark.userNote.trim() && (
            <div className="mt-2.5 rounded-xl bg-[#F59E0B]/12 border border-[#F59E0B]/30 p-2.5 flex items-start gap-2">
              <StickyNote className="w-4 h-4 text-[#D97706] shrink-0 mt-0.5" />
              <div className="min-w-0">
                <p className="text-[11px] font-bold text-[#D97706]">
                  ملاحظتي الخاصة:
                </p>
                <p className="text-sm text-[#0F1419] dark:text-[#F0F6FC] whitespace-pre-line">
                  {bookmark.userNote}
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
            href={bookmark.tweetUrl}
            target="_blank"
            rel="noopener noreferrer"
            aria-label="فتح في منصة X"
            title="فتح في منصة X"
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
              bookmark.isFavorite
                ? 'text-[#EF4444] hover:bg-[#EF4444]/10'
                : 'text-[#536471] dark:text-[#8B949E] hover:bg-black/5 dark:hover:bg-white/5'
            }`}
          >
            <Heart
              className={`w-5 h-5 ${bookmark.isFavorite ? 'fill-[#EF4444]' : ''}`}
            />
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
            aria-label="نسخ الرابط"
            title="نسخ الرابط"
            className="min-w-[44px] min-h-[44px] rounded-lg flex items-center justify-center text-[#536471] dark:text-[#8B949E] hover:bg-black/5 dark:hover:bg-white/5 transition-colors"
          >
            <Copy className="w-5 h-5" />
          </button>

          <button
            type="button"
            onClick={() => void handleShare()}
            aria-label="مشاركة"
            title="مشاركة"
            className="min-w-[44px] min-h-[44px] rounded-lg flex items-center justify-center text-[#536471] dark:text-[#8B949E] hover:bg-black/5 dark:hover:bg-white/5 transition-colors"
          >
            <Share2 className="w-5 h-5" />
          </button>

          <button
            type="button"
            onClick={onDelete}
            aria-label="حذف"
            title="حذف"
            className="min-w-[44px] min-h-[44px] rounded-lg flex items-center justify-center text-[#EF4444]/85 hover:bg-[#EF4444]/10 transition-colors"
          >
            <Trash2 className="w-5 h-5" />
          </button>
        </div>
      </div>
    </article>
  );
}
