import { useEffect, useLayoutEffect, useMemo, useRef, useState } from 'react';
import {
  Briefcase,
  Copy,
  Film,
  FolderInput,
  FolderOpen,
  Heart,
  Image as ImageIcon,
  Layers,
  Link2,
  Pencil,
  Plus,
  StickyNote,
  Trash2,
} from 'lucide-react';
import type { FolderEntity, PersonEntity } from '../types/bookmark';
import { summarizePersonWorks } from '../utils/personUtils';

interface PersonCardProps {
  person: PersonEntity;
  folder?: FolderEntity;
  onOpenProfile: (autoFocusAddWork?: boolean) => void;
  onEdit: () => void;
  onToggleFavorite: () => void;
  onMoveToFolder: () => void;
  onDelete: () => void;
  onFeedback: (message: string) => void;
}

export function PersonCard({
  person,
  folder,
  onOpenProfile,
  onEdit,
  onToggleFavorite,
  onMoveToFolder,
  onDelete,
  onFeedback,
}: PersonCardProps) {
  const [avatarError, setAvatarError] = useState(false);
  const [isBioExpanded, setIsBioExpanded] = useState(false);
  const [isBioTruncated, setIsBioTruncated] = useState(false);
  const bioRef = useRef<HTMLParagraphElement | null>(null);

  const folderColor = folder?.colorHex || '#1D9BF0';
  const initial = person.name.trim().slice(0, 1) || 'ش';
  const worksSummary = useMemo(() => summarizePersonWorks(person.works), [person.works]);
  const photoPreviewItems = useMemo(
    () => person.works.filter((w) => w.type === 'photo').slice(0, 3),
    [person.works]
  );

  useEffect(() => {
    setAvatarError(false);
  }, [person.avatarUrl]);

  useEffect(() => {
    setIsBioExpanded(false);
    setIsBioTruncated(false);
  }, [person.id, person.bio]);

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
  }, [person.bio, isBioExpanded]);

  const tagsList = useMemo(
    () =>
      person.tags
        .split(',')
        .map((t) => t.trim())
        .filter((t) => t.length > 0),
    [person.tags]
  );

  const handleCopySummary = async () => {
    const lines = [
      person.role ? `${person.name} — ${person.role}` : person.name,
      person.bio ? person.bio : '',
      ...person.works.map((w) => `• ${w.title}: ${w.url}`),
    ]
      .filter(Boolean)
      .join('\n');

    try {
      await navigator.clipboard.writeText(lines);
      onFeedback('تم نسخ ملف الشخص وأعماله إلى الحافظة');
    } catch {
      onFeedback('تعذر نسخ البيانات');
    }
  };

  return (
    <article className="flex flex-col justify-between rounded-2xl bg-white dark:bg-[#161B22] border border-[#E2E8F0] dark:border-[#30363D] p-4 shadow-xs transition-colors">
      <div>
        {/* Header: Avatar, Name, Role, Open Profile */}
        <div className="flex items-center justify-between gap-3">
          <div className="flex items-center gap-3 min-w-0 flex-1">
            <button
              type="button"
              onClick={() => onOpenProfile(false)}
              aria-label={`فتح ملف ${person.name}`}
              style={{
                background: `linear-gradient(135deg, ${folderColor}, #8B5CF6)`,
              }}
              className="w-[52px] h-[52px] rounded-full shrink-0 overflow-hidden flex items-center justify-center text-white font-bold text-xl select-none border-2 border-[#E2E8F0] dark:border-[#30363D]"
            >
              {person.avatarUrl && !avatarError ? (
                <img
                  src={person.avatarUrl}
                  alt={person.name}
                  referrerPolicy="no-referrer"
                  onError={() => setAvatarError(true)}
                  className="w-full h-full object-cover"
                />
              ) : (
                <span>{initial}</span>
              )}
            </button>

            <div className="min-w-0 flex-1">
              <button
                type="button"
                onClick={() => onOpenProfile(false)}
                className="text-right block max-w-full hover:text-[#1D9BF0] transition-colors"
              >
                <span className="text-base font-bold text-[#0F1419] dark:text-[#F0F6FC] truncate block">
                  {person.name}
                </span>
              </button>

              {person.role.trim() && (
                <div className="mt-0.5 inline-flex items-center gap-1 text-xs font-medium text-[#536471] dark:text-[#8B949E]">
                  <Briefcase className="w-3.5 h-3.5 text-[#1D9BF0] shrink-0" />
                  <span className="truncate">{person.role}</span>
                </div>
              )}
            </div>
          </div>

          <button
            type="button"
            onClick={() => onOpenProfile(true)}
            className="min-h-[34px] px-2.5 py-1 rounded-xl bg-[#1D9BF0]/12 hover:bg-[#1D9BF0]/20 text-[#1D9BF0] text-xs font-bold inline-flex items-center gap-1 shrink-0 transition-colors"
          >
            <Plus className="w-3.5 h-3.5" />
            <span>إضافة عمل</span>
          </button>
        </div>

        {/* Bio */}
        {person.bio.trim() && (
          <div className="mt-3">
            <p
              ref={bioRef}
              dir="auto"
              className={`text-[14px] leading-[1.6] text-[#0F1419] dark:text-[#F0F6FC] whitespace-pre-line break-words ${
                isBioExpanded ? '' : 'line-clamp-4'
              }`}
            >
              {person.bio}
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
        )}

        {/* Works Summary Strip */}
        <button
          type="button"
          onClick={() => onOpenProfile(false)}
          className="mt-3 w-full text-right rounded-xl bg-[#EFF3F4]/70 dark:bg-[#21262D]/70 hover:bg-[#EFF3F4] dark:hover:bg-[#21262D] border border-[#E2E8F0] dark:border-[#30363D] px-3 py-2.5 transition-colors"
        >
          <div className="flex items-center justify-between gap-2">
            <span className="text-xs font-bold text-[#0F1419] dark:text-[#F0F6FC] inline-flex items-center gap-1.5">
              <Layers className="w-3.5 h-3.5 text-[#1D9BF0]" />
              <span>الأعمال والوسائط المحفوظة ({worksSummary.total})</span>
            </span>
            <span className="text-[11px] font-bold text-[#1D9BF0]">عرض الملف</span>
          </div>

          <div className="mt-1.5 flex flex-wrap items-center gap-2 text-[11px] text-[#536471] dark:text-[#8B949E] tabular-nums">
            <span className="inline-flex items-center gap-1">
              <Link2 className="w-3 h-3 text-[#1D9BF0]" />
              <span>{worksSummary.links} روابط</span>
            </span>
            <span aria-hidden="true">·</span>
            <span className="inline-flex items-center gap-1">
              <ImageIcon className="w-3 h-3 text-[#10B981]" />
              <span>{worksSummary.photos} صور</span>
            </span>
            <span aria-hidden="true">·</span>
            <span className="inline-flex items-center gap-1">
              <Film className="w-3 h-3 text-[#8B5CF6]" />
              <span>{worksSummary.videos} فيديو</span>
            </span>
          </div>

          {photoPreviewItems.length > 0 && (
            <div className="mt-2 flex items-center gap-2">
              {photoPreviewItems.map((photo) => (
                <div
                  key={photo.id}
                  className="w-12 h-12 rounded-lg overflow-hidden border border-[#CFD9DE] dark:border-[#30363D] bg-black/5 shrink-0"
                >
                  <img
                    src={photo.url}
                    alt={photo.title}
                    referrerPolicy="no-referrer"
                    className="w-full h-full object-cover"
                  />
                </div>
              ))}
            </div>
          )}
        </button>

        {/* Personal Note */}
        {person.userNote && person.userNote.trim() && (
          <div className="mt-2.5 rounded-xl bg-[#F59E0B]/12 border border-[#F59E0B]/30 p-2.5 flex items-start gap-2">
            <StickyNote className="w-4 h-4 text-[#D97706] shrink-0 mt-0.5" />
            <div className="min-w-0">
              <p className="text-[11px] font-bold text-[#D97706]">ملاحظتي الخاصة:</p>
              <p className="text-sm text-[#0F1419] dark:text-[#F0F6FC] whitespace-pre-line">
                {person.userNote}
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

      {/* Bottom Actions Row */}
      <div className="mt-3 pt-1">
        <div className="flex items-center justify-between rounded-xl bg-[#EFF3F4]/50 dark:bg-[#21262D]/50 px-1 py-0.5">
          <button
            type="button"
            onClick={() => onOpenProfile(false)}
            aria-label={`فتح ملف ${person.name} والأعمال`}
            title="فتح الملف الشخصي والأعمال"
            className="min-w-[44px] min-h-[44px] rounded-lg flex items-center justify-center text-[#1D9BF0] hover:bg-[#1D9BF0]/10 transition-colors"
          >
            <FolderOpen className="w-5 h-5" />
          </button>

          <button
            type="button"
            onClick={onToggleFavorite}
            aria-label={`تفضيل ${person.name}`}
            title="المفضلة"
            className={`min-w-[44px] min-h-[44px] rounded-lg flex items-center justify-center transition-colors ${
              person.isFavorite
                ? 'text-[#EF4444] hover:bg-[#EF4444]/10'
                : 'text-[#536471] dark:text-[#8B949E] hover:bg-black/5 dark:hover:bg-white/5'
            }`}
          >
            <Heart className={`w-5 h-5 ${person.isFavorite ? 'fill-[#EF4444]' : ''}`} />
          </button>

          <button
            type="button"
            onClick={onMoveToFolder}
            aria-label={`تغيير مجموعة ${person.name}`}
            title="تغيير المجموعة"
            className="min-w-[44px] min-h-[44px] rounded-lg flex items-center justify-center text-[#536471] dark:text-[#8B949E] hover:bg-black/5 dark:hover:bg-white/5 transition-colors"
          >
            <FolderInput className="w-5 h-5" />
          </button>

          <button
            type="button"
            onClick={onEdit}
            aria-label={`تعديل بيانات ${person.name}`}
            title="تعديل بيانات الشخص"
            className="min-w-[44px] min-h-[44px] rounded-lg flex items-center justify-center text-[#536471] dark:text-[#8B949E] hover:bg-black/5 dark:hover:bg-white/5 transition-colors"
          >
            <Pencil className="w-5 h-5" />
          </button>

          <button
            type="button"
            onClick={() => void handleCopySummary()}
            aria-label={`نسخ ملخص ${person.name}`}
            title="نسخ ملخص الشخص وأعماله"
            className="min-w-[44px] min-h-[44px] rounded-lg flex items-center justify-center text-[#536471] dark:text-[#8B949E] hover:bg-black/5 dark:hover:bg-white/5 transition-colors"
          >
            <Copy className="w-5 h-5" />
          </button>

          <button
            type="button"
            onClick={onDelete}
            aria-label={`حذف ${person.name}`}
            title="حذف الشخص"
            className="min-w-[44px] min-h-[44px] rounded-lg flex items-center justify-center text-[#EF4444]/85 hover:bg-[#EF4444]/10 transition-colors"
          >
            <Trash2 className="w-5 h-5" />
          </button>
        </div>
      </div>
    </article>
  );
}
