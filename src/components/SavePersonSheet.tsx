import { useEffect, useRef, useState } from 'react';
import type { ChangeEvent } from 'react';
import {
  Briefcase,
  Check,
  ImagePlus,
  Link2,
  Plus,
  StickyNote,
  Trash2,
  User,
  UserPlus,
  X,
} from 'lucide-react';
import type { FolderWithCount, PersonEntity } from '../types/bookmark';
import { readImageFileAsCompressedDataUrl } from '../utils/personUtils';

interface SavePersonSheetProps {
  personToEdit: PersonEntity | null;
  folders: FolderWithCount[];
  selectedFolderId: number | null;
  onSelectFolder: (folderId: number) => void;
  onOpenCreateFolder: () => void;
  onSave: (draft: {
    name: string;
    role: string;
    avatarUrl?: string;
    bio: string;
    userNote?: string;
    tags: string;
    folderId: number;
  }) => void;
  onDismiss: () => void;
  onFeedback: (message: string) => void;
}

export function SavePersonSheet({
  personToEdit,
  folders,
  selectedFolderId,
  onSelectFolder,
  onOpenCreateFolder,
  onSave,
  onDismiss,
  onFeedback,
}: SavePersonSheetProps) {
  const fileInputRef = useRef<HTMLInputElement | null>(null);

  const [name, setName] = useState(personToEdit?.name ?? '');
  const [role, setRole] = useState(personToEdit?.role ?? '');
  const [avatarUrl, setAvatarUrl] = useState(personToEdit?.avatarUrl ?? '');
  const [bio, setBio] = useState(personToEdit?.bio ?? '');
  const [userNote, setUserNote] = useState(personToEdit?.userNote ?? '');
  const [tags, setTags] = useState(personToEdit?.tags ?? '');
  const [avatarError, setAvatarError] = useState(false);
  const [isReadingImage, setIsReadingImage] = useState(false);

  useEffect(() => {
    setAvatarError(false);
  }, [avatarUrl]);

  useEffect(() => {
    const onKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onDismiss();
    };
    window.addEventListener('keydown', onKeyDown);
    return () => window.removeEventListener('keydown', onKeyDown);
  }, [onDismiss]);

  const handleImagePick = async (e: ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    e.target.value = '';
    if (!file) return;
    setIsReadingImage(true);
    try {
      const dataUrl = await readImageFileAsCompressedDataUrl(file, 480);
      setAvatarUrl(dataUrl);
      setAvatarError(false);
    } catch {
      onFeedback('تعذر قراءة الصورة المختارة');
    } finally {
      setIsReadingImage(false);
    }
  };

  const isSaveEnabled = name.trim().length > 0 && selectedFolderId !== null;
  const fallbackInitial = name.trim().slice(0, 1) || 'ش';

  return (
    <div
      role="dialog"
      aria-modal="true"
      aria-labelledby="save-person-sheet-title"
      onClick={onDismiss}
      className="fixed inset-0 z-40 bg-black/55 backdrop-blur-xs flex items-end sm:items-center justify-center sm:p-4"
    >
      <div
        onClick={(e) => e.stopPropagation()}
        className="w-full sm:max-w-lg rounded-t-3xl sm:rounded-2xl bg-white dark:bg-[#161B22] border-t sm:border border-[#E2E8F0] dark:border-[#30363D] shadow-2xl max-h-[90vh] overflow-y-auto animate-sheet-up"
      >
        <div className="sm:hidden flex justify-center pt-2.5 pb-1">
          <span className="w-10 h-1 rounded-full bg-[#E2E8F0] dark:bg-[#30363D]" />
        </div>

        <div className="p-5 pb-7 space-y-4">
          {/* Header */}
          <div className="flex items-center justify-between gap-3">
            <div className="flex items-center gap-2.5">
              <div className="w-9 h-9 rounded-full bg-[#1D9BF0]/15 text-[#1D9BF0] flex items-center justify-center shrink-0">
                <UserPlus className="w-5 h-5" />
              </div>
              <h2
                id="save-person-sheet-title"
                className="text-lg font-bold text-[#0F1419] dark:text-[#F0F6FC]"
              >
                {personToEdit ? 'تعديل ملف الشخص' : 'إضافة شخص جديد'}
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

          {/* Photo Preview & Upload / URL */}
          <div className="rounded-2xl bg-[#EFF3F4]/55 dark:bg-[#21262D]/55 border border-[#E2E8F0] dark:border-[#30363D] p-3.5 flex items-center gap-3.5">
            <div className="w-16 h-16 rounded-full bg-gradient-to-br from-[#1D9BF0] to-[#8B5CF6] text-white font-bold text-2xl flex items-center justify-center shrink-0 overflow-hidden border-2 border-white dark:border-[#161B22] shadow-xs">
              {avatarUrl.trim() && !avatarError ? (
                <img
                  src={avatarUrl}
                  alt={name || 'صورة الشخص'}
                  referrerPolicy="no-referrer"
                  onError={() => setAvatarError(true)}
                  className="w-full h-full object-cover"
                />
              ) : (
                <span>{fallbackInitial}</span>
              )}
            </div>

            <div className="flex-1 min-w-0 space-y-2">
              <input
                ref={fileInputRef}
                type="file"
                accept="image/*"
                onChange={(e) => void handleImagePick(e)}
                className="hidden"
              />
              <div className="flex flex-wrap items-center gap-2">
                <button
                  type="button"
                  disabled={isReadingImage}
                  onClick={() => fileInputRef.current?.click()}
                  className="min-h-[36px] px-3 py-1.5 rounded-xl bg-[#1D9BF0]/15 hover:bg-[#1D9BF0]/25 text-[#1D9BF0] text-xs font-bold inline-flex items-center gap-1.5 transition-colors"
                >
                  <ImagePlus className="w-4 h-4" />
                  <span>{isReadingImage ? 'جاري التحميل…' : 'اختيار صورة من الجهاز'}</span>
                </button>

                {avatarUrl.trim() && (
                  <button
                    type="button"
                    onClick={() => setAvatarUrl('')}
                    aria-label="إزالة الصورة"
                    className="min-h-[36px] px-2.5 py-1.5 rounded-xl text-[#EF4444] hover:bg-[#EF4444]/10 text-xs font-medium inline-flex items-center gap-1 transition-colors"
                  >
                    <Trash2 className="w-3.5 h-3.5" />
                    <span>إزالة</span>
                  </button>
                )}
              </div>

              <div className="relative flex items-center">
                <Link2 className="w-3.5 h-3.5 text-[#536471] dark:text-[#8B949E] absolute right-3 pointer-events-none" />
                <input
                  id="person-avatar-url"
                  type="text"
                  dir="ltr"
                  value={avatarUrl.startsWith('data:') ? '' : avatarUrl}
                  onChange={(e) => setAvatarUrl(e.target.value)}
                  placeholder={
                    avatarUrl.startsWith('data:')
                      ? 'تم اختيار صورة من الجهاز (أو الصق رابط صورة بديل)'
                      : 'أو الصق رابط الصورة (https://...)'
                  }
                  className="w-full h-9 pr-8 pl-2.5 rounded-lg border border-[#CFD9DE] dark:border-[#30363D] bg-white dark:bg-[#161B22] text-xs text-[#0F1419] dark:text-[#F0F6FC] focus:outline-none focus:border-[#1D9BF0]"
                />
              </div>
            </div>
          </div>

          {/* Name */}
          <div>
            <label
              htmlFor="person-name-input"
              className="block text-xs font-semibold text-[#536471] dark:text-[#8B949E] mb-1"
            >
              الاسم *
            </label>
            <div className="relative flex items-center">
              <User className="w-4 h-4 text-[#1D9BF0] absolute right-3.5 pointer-events-none" />
              <input
                id="person-name-input"
                type="text"
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="مثال: ليوناردو دي كابريو أو أحمد حلمي"
                autoFocus
                className="w-full h-11 pr-10 pl-3 rounded-xl border border-[#CFD9DE] dark:border-[#30363D] bg-[#EFF3F4]/25 dark:bg-[#21262D]/25 text-sm text-[#0F1419] dark:text-[#F0F6FC] focus:outline-none focus:border-[#1D9BF0]"
              />
            </div>
          </div>

          {/* Role / Profession */}
          <div>
            <label
              htmlFor="person-role-input"
              className="block text-xs font-semibold text-[#536471] dark:text-[#8B949E] mb-1"
            >
              الصفة أو التخصص (اختياري)
            </label>
            <div className="relative flex items-center">
              <Briefcase className="w-4 h-4 text-[#536471] dark:text-[#8B949E] absolute right-3.5 pointer-events-none" />
              <input
                id="person-role-input"
                type="text"
                value={role}
                onChange={(e) => setRole(e.target.value)}
                placeholder="مثال: ممثل، مخرج، كاتب، صانع محتوى…"
                className="w-full h-11 pr-10 pl-3 rounded-xl border border-[#CFD9DE] dark:border-[#30363D] bg-[#EFF3F4]/25 dark:bg-[#21262D]/25 text-sm text-[#0F1419] dark:text-[#F0F6FC] focus:outline-none focus:border-[#1D9BF0]"
              />
            </div>
          </div>

          {/* Folder Selection */}
          <div>
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

          {/* Bio */}
          <div>
            <label
              htmlFor="person-bio-input"
              className="block text-xs font-semibold text-[#536471] dark:text-[#8B949E] mb-1"
            >
              نبذة تعريفية (اختياري)
            </label>
            <textarea
              id="person-bio-input"
              value={bio}
              onChange={(e) => setBio(e.target.value)}
              placeholder="معلومات مختصرة عن الشخص وأبرز مجالاته…"
              rows={2}
              className="w-full px-3 py-2.5 rounded-xl border border-[#CFD9DE] dark:border-[#30363D] bg-[#EFF3F4]/25 dark:bg-[#21262D]/25 text-sm text-[#0F1419] dark:text-[#F0F6FC] focus:outline-none focus:border-[#1D9BF0] resize-none"
            />
          </div>

          {/* Personal Note */}
          <div>
            <label
              htmlFor="person-note-input"
              className="block text-xs font-semibold text-[#536471] dark:text-[#8B949E] mb-1"
            >
              ملاحظة شخصية (اختياري)
            </label>
            <div className="relative">
              <StickyNote className="w-4 h-4 text-[#536471] dark:text-[#8B949E] absolute right-3.5 top-3.5 pointer-events-none" />
              <textarea
                id="person-note-input"
                value={userNote}
                onChange={(e) => setUserNote(e.target.value)}
                placeholder="أفلام مقترحة أو ملاحظات للمراجعة…"
                rows={2}
                className="w-full pr-10 pl-3 py-2.5 rounded-xl border border-[#CFD9DE] dark:border-[#30363D] bg-[#EFF3F4]/25 dark:bg-[#21262D]/25 text-sm text-[#0F1419] dark:text-[#F0F6FC] focus:outline-none focus:border-[#1D9BF0] resize-none"
              />
            </div>
          </div>

          {/* Tags */}
          <div>
            <label
              htmlFor="person-tags-input"
              className="block text-xs font-semibold text-[#536471] dark:text-[#8B949E] mb-1"
            >
              وسوم التصنيف (مفصولة بفواصل)
            </label>
            <input
              id="person-tags-input"
              type="text"
              value={tags}
              onChange={(e) => setTags(e.target.value)}
              placeholder="ممثل, سينما, دراما"
              className="w-full h-11 px-3 rounded-xl border border-[#CFD9DE] dark:border-[#30363D] bg-[#EFF3F4]/25 dark:bg-[#21262D]/25 text-sm text-[#0F1419] dark:text-[#F0F6FC] focus:outline-none focus:border-[#1D9BF0]"
            />
          </div>

          {/* Primary Save Button */}
          <button
            type="button"
            disabled={!isSaveEnabled}
            onClick={() => {
              if (!selectedFolderId) return;
              onSave({
                name,
                role,
                avatarUrl,
                bio,
                userNote,
                tags,
                folderId: selectedFolderId,
              });
            }}
            className="w-full h-12 rounded-xl bg-[#1D9BF0] hover:bg-[#1A8CD8] disabled:opacity-50 text-white font-bold text-base flex items-center justify-center gap-2 transition-colors"
          >
            <UserPlus className="w-5 h-5" />
            <span>{personToEdit ? 'حفظ التعديلات' : 'حفظ الشخص في المجموعة'}</span>
          </button>
        </div>
      </div>
    </div>
  );
}
