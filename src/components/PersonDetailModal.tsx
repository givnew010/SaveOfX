import { useEffect, useMemo, useRef, useState } from 'react';
import type { ChangeEvent } from 'react';
import {
  Briefcase,
  ExternalLink,
  FileText,
  Film,
  Image as ImageIcon,
  ImagePlus,
  Link2,
  Pencil,
  Plus,
  StickyNote,
  Trash2,
  X,
} from 'lucide-react';
import type { FolderEntity, PersonEntity, PersonWorkType } from '../types/bookmark';
import {
  extractYouTubeEmbedUrl,
  getWorkTypeLabel,
  isDirectVideoFile,
  readImageFileAsCompressedDataUrl,
} from '../utils/personUtils';

interface PersonDetailModalProps {
  person: PersonEntity;
  folder?: FolderEntity;
  autoFocusAddWork?: boolean;
  onEditPerson: () => void;
  onAddWork: (draft: {
    type: PersonWorkType;
    title: string;
    url: string;
    note?: string;
  }) => boolean;
  onRemoveWork: (workId: string) => void;
  onDismiss: () => void;
  onFeedback: (message: string) => void;
}

const WORK_TYPE_OPTIONS: { value: PersonWorkType; label: string }[] = [
  { value: 'link', label: 'رابط عمل' },
  { value: 'photo', label: 'صورة' },
  { value: 'video', label: 'فيديو' },
  { value: 'other', label: 'أخرى' },
];

export function PersonDetailModal({
  person,
  folder,
  autoFocusAddWork = false,
  onEditPerson,
  onAddWork,
  onRemoveWork,
  onDismiss,
  onFeedback,
}: PersonDetailModalProps) {
  const workPhotoInputRef = useRef<HTMLInputElement | null>(null);

  const [showAddForm, setShowAddForm] = useState(autoFocusAddWork || person.works.length === 0);
  const [workType, setWorkType] = useState<PersonWorkType>('link');
  const [workTitle, setWorkTitle] = useState('');
  const [workUrl, setWorkUrl] = useState('');
  const [workNote, setWorkNote] = useState('');
  const [filterType, setFilterType] = useState<'all' | PersonWorkType>('all');
  const [avatarError, setAvatarError] = useState(false);
  const [isReadingPhoto, setIsReadingPhoto] = useState(false);

  useEffect(() => {
    setAvatarError(false);
  }, [person.avatarUrl]);

  useEffect(() => {
    const onKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onDismiss();
    };
    window.addEventListener('keydown', onKeyDown);
    return () => window.removeEventListener('keydown', onKeyDown);
  }, [onDismiss]);

  const folderColor = folder?.colorHex || '#1D9BF0';
  const initial = person.name.trim().slice(0, 1) || 'ش';

  const filteredWorks = useMemo(() => {
    if (filterType === 'all') return person.works;
    return person.works.filter((w) => w.type === filterType);
  }, [person.works, filterType]);

  const handlePhotoUpload = async (e: ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    e.target.value = '';
    if (!file) return;
    setIsReadingPhoto(true);
    try {
      const dataUrl = await readImageFileAsCompressedDataUrl(file, 720);
      setWorkType('photo');
      setWorkUrl(dataUrl);
      if (!workTitle.trim()) {
        setWorkTitle(file.name.replace(/\.[^.]+$/, '') || 'صورة');
      }
    } catch {
      onFeedback('تعذر قراءة الصورة المختارة');
    } finally {
      setIsReadingPhoto(false);
    }
  };

  const handleSubmitWork = () => {
    if (!workUrl.trim()) {
      onFeedback('يرجى إدخال الرابط أو اختيار صورة');
      return;
    }
    const ok = onAddWork({
      type: workType,
      title: workTitle,
      url: workUrl,
      note: workNote,
    });
    if (ok) {
      setWorkTitle('');
      setWorkUrl('');
      setWorkNote('');
    }
  };

  const tagsList = useMemo(
    () =>
      person.tags
        .split(',')
        .map((t) => t.trim())
        .filter((t) => t.length > 0),
    [person.tags]
  );

  return (
    <div
      role="dialog"
      aria-modal="true"
      aria-labelledby="person-detail-title"
      onClick={onDismiss}
      className="fixed inset-0 z-40 bg-black/60 backdrop-blur-xs flex items-end sm:items-center justify-center sm:p-4"
    >
      <div
        onClick={(e) => e.stopPropagation()}
        className="w-full sm:max-w-2xl rounded-t-3xl sm:rounded-2xl bg-white dark:bg-[#161B22] border-t sm:border border-[#E2E8F0] dark:border-[#30363D] shadow-2xl max-h-[92vh] overflow-y-auto animate-sheet-up"
      >
        <div className="sm:hidden flex justify-center pt-2.5 pb-1">
          <span className="w-10 h-1 rounded-full bg-[#E2E8F0] dark:bg-[#30363D]" />
        </div>

        <div className="p-5 pb-7 space-y-5">
          {/* Profile Header */}
          <div className="flex items-start justify-between gap-3">
            <div className="flex items-center gap-3.5 min-w-0 flex-1">
              <div
                style={{
                  background: `linear-gradient(135deg, ${folderColor}, #8B5CF6)`,
                }}
                className="w-16 h-16 rounded-full shrink-0 overflow-hidden flex items-center justify-center text-white font-bold text-2xl border-2 border-[#E2E8F0] dark:border-[#30363D]"
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
              </div>

              <div className="min-w-0 flex-1">
                <h2
                  id="person-detail-title"
                  className="text-xl font-bold text-[#0F1419] dark:text-[#F0F6FC] truncate"
                >
                  {person.name}
                </h2>
                {person.role.trim() && (
                  <p className="mt-0.5 text-sm text-[#536471] dark:text-[#8B949E] inline-flex items-center gap-1.5">
                    <Briefcase className="w-4 h-4 text-[#1D9BF0] shrink-0" />
                    <span>{person.role}</span>
                  </p>
                )}
                {(folder || tagsList.length > 0) && (
                  <div className="mt-2 flex flex-wrap items-center gap-1.5">
                    {folder && (
                      <span
                        style={{
                          backgroundColor: `${folderColor}1A`,
                          borderColor: `${folderColor}55`,
                          color: folderColor,
                        }}
                        className="inline-flex items-center gap-1 rounded-xl border px-2 py-0.5 text-xs font-semibold"
                      >
                        <span>{folder.iconEmoji}</span>
                        <span>{folder.name}</span>
                      </span>
                    )}
                    {tagsList.map((tag, idx) => (
                      <span
                        key={`${tag}-${idx}`}
                        className="rounded-xl bg-[#EFF3F4] dark:bg-[#21262D] px-2 py-0.5 text-xs font-medium text-[#1D9BF0]"
                      >
                        #{tag}
                      </span>
                    ))}
                  </div>
                )}
              </div>
            </div>

            <div className="flex items-center gap-1 shrink-0">
              <button
                type="button"
                onClick={onEditPerson}
                aria-label="تعديل بيانات الشخص"
                title="تعديل بيانات الشخص"
                className="min-w-[40px] min-h-[40px] rounded-full flex items-center justify-center text-[#536471] dark:text-[#8B949E] hover:bg-[#EFF3F4] dark:hover:bg-[#21262D] transition-colors"
              >
                <Pencil className="w-4 h-4" />
              </button>
              <button
                type="button"
                onClick={onDismiss}
                aria-label="إغلاق"
                className="min-w-[40px] min-h-[40px] rounded-full flex items-center justify-center text-[#536471] dark:text-[#8B949E] hover:bg-[#EFF3F4] dark:hover:bg-[#21262D] transition-colors"
              >
                <X className="w-5 h-5" />
              </button>
            </div>
          </div>

          {person.bio.trim() && (
            <p
              dir="auto"
              className="text-sm leading-relaxed text-[#0F1419] dark:text-[#F0F6FC] whitespace-pre-line rounded-xl bg-[#EFF3F4]/45 dark:bg-[#21262D]/45 p-3"
            >
              {person.bio}
            </p>
          )}

          {person.userNote && person.userNote.trim() && (
            <div className="rounded-xl bg-[#F59E0B]/12 border border-[#F59E0B]/30 p-3 flex items-start gap-2">
              <StickyNote className="w-4 h-4 text-[#D97706] shrink-0 mt-0.5" />
              <div className="min-w-0">
                <p className="text-[11px] font-bold text-[#D97706]">ملاحظتي الخاصة:</p>
                <p className="text-sm text-[#0F1419] dark:text-[#F0F6FC] whitespace-pre-line">
                  {person.userNote}
                </p>
              </div>
            </div>
          )}

          {/* Works Header & Toggle Add Form */}
          <div className="flex flex-wrap items-center justify-between gap-2 pt-1 border-t border-[#E2E8F0] dark:border-[#30363D]">
            <h3 className="text-base font-bold text-[#0F1419] dark:text-[#F0F6FC]">
              الأعمال والروابط والوسائط ({person.works.length})
            </h3>

            <button
              type="button"
              onClick={() => setShowAddForm((prev) => !prev)}
              className="min-h-[38px] px-3.5 py-1.5 rounded-xl bg-[#1D9BF0] hover:bg-[#1A8CD8] text-white text-xs font-bold inline-flex items-center gap-1.5 transition-colors"
            >
              <Plus className="w-4 h-4" />
              <span>{showAddForm ? 'إخفاء نموذج الإضافة' : 'إضافة عمل / وسائط'}</span>
            </button>
          </div>

          {/* Add Work / Link / Photo / Video Form */}
          {showAddForm && (
            <div className="rounded-2xl bg-[#EFF3F4]/60 dark:bg-[#21262D]/60 border border-[#1D9BF0]/30 p-4 space-y-3">
              {/* Type Tabs */}
              <div className="flex flex-wrap gap-1.5" role="group" aria-label="نوع العمل">
                {WORK_TYPE_OPTIONS.map((opt) => {
                  const selected = workType === opt.value;
                  return (
                    <button
                      key={opt.value}
                      type="button"
                      onClick={() => setWorkType(opt.value)}
                      className={`min-h-[34px] px-3 py-1 rounded-xl text-xs font-bold transition-colors ${
                        selected
                          ? 'bg-[#1D9BF0] text-white'
                          : 'bg-white dark:bg-[#161B22] border border-[#CFD9DE] dark:border-[#30363D] text-[#0F1419] dark:text-[#F0F6FC]'
                      }`}
                    >
                      {opt.label}
                    </button>
                  );
                })}
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
                <div>
                  <label
                    htmlFor="work-title-input"
                    className="block text-xs font-semibold text-[#536471] dark:text-[#8B949E] mb-1"
                  >
                    عنوان العمل أو الوصف
                  </label>
                  <input
                    id="work-title-input"
                    type="text"
                    value={workTitle}
                    onChange={(e) => setWorkTitle(e.target.value)}
                    placeholder="مثال: فيلم Inception أو صورة من الكواليس"
                    className="w-full h-10 px-3 rounded-xl border border-[#CFD9DE] dark:border-[#30363D] bg-white dark:bg-[#161B22] text-sm text-[#0F1419] dark:text-[#F0F6FC] focus:outline-none focus:border-[#1D9BF0]"
                  />
                </div>

                <div>
                  <label
                    htmlFor="work-url-input"
                    className="block text-xs font-semibold text-[#536471] dark:text-[#8B949E] mb-1"
                  >
                    {workType === 'photo'
                      ? 'رابط الصورة أو اختر من الجهاز *'
                      : workType === 'video'
                        ? 'رابط الفيديو (YouTube / MP4 / ...) *'
                        : 'الرابط (URL) *'}
                  </label>
                  <input
                    id="work-url-input"
                    type="text"
                    dir="ltr"
                    value={workUrl.startsWith('data:') ? '' : workUrl}
                    onChange={(e) => setWorkUrl(e.target.value)}
                    placeholder={
                      workUrl.startsWith('data:')
                        ? 'تم إرفاق صورة من الجهاز'
                        : 'https://...'
                    }
                    className="w-full h-10 px-3 rounded-xl border border-[#CFD9DE] dark:border-[#30363D] bg-white dark:bg-[#161B22] text-sm text-[#0F1419] dark:text-[#F0F6FC] focus:outline-none focus:border-[#1D9BF0]"
                  />
                </div>
              </div>

              {workType === 'photo' && (
                <div className="flex flex-wrap items-center gap-2">
                  <input
                    ref={workPhotoInputRef}
                    type="file"
                    accept="image/*"
                    onChange={(e) => void handlePhotoUpload(e)}
                    className="hidden"
                  />
                  <button
                    type="button"
                    disabled={isReadingPhoto}
                    onClick={() => workPhotoInputRef.current?.click()}
                    className="min-h-[36px] px-3 py-1.5 rounded-xl bg-white dark:bg-[#161B22] border border-[#1D9BF0]/40 text-[#1D9BF0] text-xs font-bold inline-flex items-center gap-1.5 hover:bg-[#1D9BF0]/10 transition-colors"
                  >
                    <ImagePlus className="w-4 h-4" />
                    <span>{isReadingPhoto ? 'جاري التحميل…' : 'رفع صورة من الجهاز'}</span>
                  </button>
                  {workUrl.startsWith('data:image/') && (
                    <img
                      src={workUrl}
                      alt="معاينة الصورة"
                      className="w-12 h-12 rounded-lg object-cover border border-[#CFD9DE] dark:border-[#30363D]"
                    />
                  )}
                </div>
              )}

              <div>
                <label
                  htmlFor="work-note-input"
                  className="block text-xs font-semibold text-[#536471] dark:text-[#8B949E] mb-1"
                >
                  ملاحظة إضافية (اختياري)
                </label>
                <input
                  id="work-note-input"
                  type="text"
                  value={workNote}
                  onChange={(e) => setWorkNote(e.target.value)}
                  placeholder="تفاصيل إضافية حول العمل…"
                  className="w-full h-10 px-3 rounded-xl border border-[#CFD9DE] dark:border-[#30363D] bg-white dark:bg-[#161B22] text-sm text-[#0F1419] dark:text-[#F0F6FC] focus:outline-none focus:border-[#1D9BF0]"
                />
              </div>

              <div className="flex justify-end">
                <button
                  type="button"
                  disabled={!workUrl.trim()}
                  onClick={handleSubmitWork}
                  className="min-h-[40px] px-4 py-2 rounded-xl bg-[#1D9BF0] hover:bg-[#1A8CD8] disabled:opacity-50 text-white text-xs sm:text-sm font-bold inline-flex items-center gap-1.5 transition-colors"
                >
                  <Plus className="w-4 h-4" />
                  <span>إضافة إلى الملف</span>
                </button>
              </div>
            </div>
          )}

          {/* Filter Chips */}
          {person.works.length > 0 && (
            <div className="flex flex-wrap items-center gap-1.5">
              {(
                [
                  { id: 'all', label: `الكل (${person.works.length})` },
                  { id: 'link', label: 'روابط' },
                  { id: 'photo', label: 'صور' },
                  { id: 'video', label: 'فيديو' },
                  { id: 'other', label: 'أخرى' },
                ] as const
              ).map((tab) => {
                const selected = filterType === tab.id;
                return (
                  <button
                    key={tab.id}
                    type="button"
                    onClick={() => setFilterType(tab.id)}
                    className={`min-h-[32px] px-3 py-1 rounded-xl text-xs font-semibold transition-colors ${
                      selected
                        ? 'bg-[#1D9BF0]/15 text-[#1D9BF0] border border-[#1D9BF0]/50 font-bold'
                        : 'bg-[#EFF3F4] dark:bg-[#21262D] text-[#536471] dark:text-[#8B949E]'
                    }`}
                  >
                    {tab.label}
                  </button>
                );
              })}
            </div>
          )}

          {/* Works List */}
          {filteredWorks.length === 0 ? (
            <div className="rounded-xl border border-dashed border-[#CFD9DE] dark:border-[#30363D] p-6 text-center text-sm text-[#536471] dark:text-[#8B949E]">
              {person.works.length === 0
                ? 'لا توجد أعمال أو وسائط مضافة لهذا الشخص بعد — استخدم النموذج أعلاه لإضافة رابط أو صورة أو فيديو.'
                : 'لا توجد عناصر ضمن هذا التصنيف.'}
            </div>
          ) : (
            <div className="space-y-2.5">
              {filteredWorks.map((work) => {
                const embedUrl =
                  work.type === 'video' ? extractYouTubeEmbedUrl(work.url) : null;
                const directVideo =
                  work.type === 'video' && isDirectVideoFile(work.url);

                return (
                  <div
                    key={work.id}
                    className="rounded-xl border border-[#E2E8F0] dark:border-[#30363D] bg-[#EFF3F4]/35 dark:bg-[#21262D]/35 p-3 space-y-2"
                  >
                    <div className="flex items-start justify-between gap-2.5">
                      <div className="flex items-start gap-2.5 min-w-0 flex-1">
                        <div className="w-9 h-9 rounded-lg bg-[#1D9BF0]/15 text-[#1D9BF0] flex items-center justify-center shrink-0 mt-0.5">
                          {work.type === 'photo' ? (
                            <ImageIcon className="w-4 h-4 text-[#10B981]" />
                          ) : work.type === 'video' ? (
                            <Film className="w-4 h-4 text-[#8B5CF6]" />
                          ) : work.type === 'other' ? (
                            <FileText className="w-4 h-4 text-[#F59E0B]" />
                          ) : (
                            <Link2 className="w-4 h-4 text-[#1D9BF0]" />
                          )}
                        </div>

                        <div className="min-w-0 flex-1">
                          <div className="flex flex-wrap items-center gap-1.5">
                            <span className="text-sm font-bold text-[#0F1419] dark:text-[#F0F6FC]">
                              {work.title}
                            </span>
                            <span className="rounded-md bg-white dark:bg-[#161B22] border border-[#E2E8F0] dark:border-[#30363D] px-1.5 py-0.5 text-[10px] font-semibold text-[#536471] dark:text-[#8B949E]">
                              {getWorkTypeLabel(work.type)}
                            </span>
                          </div>

                          {work.note && (
                            <p className="text-xs text-[#536471] dark:text-[#8B949E] mt-0.5">
                              {work.note}
                            </p>
                          )}

                          {!work.url.startsWith('data:') && (
                            <a
                              href={work.url}
                              target="_blank"
                              rel="noopener noreferrer"
                              dir="ltr"
                              className="mt-1 inline-flex items-center gap-1 text-xs text-[#1D9BF0] hover:underline max-w-full truncate"
                            >
                              <ExternalLink className="w-3 h-3 shrink-0" />
                              <span className="truncate">{work.url}</span>
                            </a>
                          )}
                        </div>
                      </div>

                      <button
                        type="button"
                        onClick={() => onRemoveWork(work.id)}
                        aria-label={`حذف ${work.title}`}
                        title="حذف العنصر"
                        className="min-w-[36px] min-h-[36px] rounded-lg flex items-center justify-center text-[#EF4444]/85 hover:bg-[#EF4444]/10 transition-colors shrink-0"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </div>

                    {work.type === 'photo' && (
                      <div className="rounded-xl overflow-hidden border border-[#E2E8F0] dark:border-[#30363D] bg-black/5 max-h-60">
                        <img
                          src={work.url}
                          alt={work.title}
                          referrerPolicy="no-referrer"
                          className="w-full max-h-60 object-contain mx-auto"
                        />
                      </div>
                    )}

                    {work.type === 'video' && embedUrl && (
                      <div className="rounded-xl overflow-hidden border border-[#E2E8F0] dark:border-[#30363D] aspect-video bg-black">
                        <iframe
                          src={embedUrl}
                          title={work.title}
                          className="w-full h-full"
                          allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                          allowFullScreen
                        />
                      </div>
                    )}

                    {work.type === 'video' && directVideo && (
                      <div className="rounded-xl overflow-hidden border border-[#E2E8F0] dark:border-[#30363D] bg-black">
                        <video
                          src={work.url}
                          controls
                          preload="metadata"
                          className="w-full max-h-64"
                        />
                      </div>
                    )}
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
