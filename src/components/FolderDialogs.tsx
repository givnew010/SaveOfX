import { useEffect, useState } from 'react';
import type { ReactNode } from 'react';
import { AlertTriangle, Check, FolderInput } from 'lucide-react';
import type {
  AccountBookmarkEntity,
  FolderEntity,
  FolderWithCount,
  PersonEntity,
  TweetBookmarkEntity,
} from '../types/bookmark';

const PRESET_EMOJIS = [
  '📁',
  '💻',
  '💡',
  '📚',
  '📈',
  '🎨',
  '🚀',
  '🧠',
  '⚡',
  '🍿',
  '📌',
  '⭐',
];

const PRESET_COLORS = [
  '#1D9BF0',
  '#10B981',
  '#F59E0B',
  '#8B5CF6',
  '#EC4899',
  '#EF4444',
  '#06B6D4',
];

function ModalBackdrop({
  onDismiss,
  titleId,
  children,
}: {
  onDismiss: () => void;
  titleId: string;
  children: ReactNode;
}) {
  useEffect(() => {
    const onKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onDismiss();
    };
    window.addEventListener('keydown', onKeyDown);
    return () => window.removeEventListener('keydown', onKeyDown);
  }, [onDismiss]);

  return (
    <div
      role="dialog"
      aria-modal="true"
      aria-labelledby={titleId}
      onClick={onDismiss}
      className="fixed inset-0 z-50 bg-black/55 backdrop-blur-xs flex items-center justify-center p-4"
    >
      <div
        onClick={(e) => e.stopPropagation()}
        className="w-full max-w-md rounded-2xl bg-white dark:bg-[#161B22] border border-[#E2E8F0] dark:border-[#30363D] p-5 shadow-xl animate-sheet-up max-h-[90vh] overflow-y-auto"
      >
        {children}
      </div>
    </div>
  );
}

export function CreateFolderDialog({
  onDismiss,
  onConfirm,
}: {
  onDismiss: () => void;
  onConfirm: (
    name: string,
    emoji: string,
    colorHex: string,
    description: string
  ) => void;
}) {
  const [name, setName] = useState('');
  const [selectedEmoji, setSelectedEmoji] = useState('📁');
  const [selectedColor, setSelectedColor] = useState('#1D9BF0');
  const [description, setDescription] = useState('');

  return (
    <ModalBackdrop onDismiss={onDismiss} titleId="create-folder-title">
      <h2
        id="create-folder-title"
        className="text-lg font-bold text-[#0F1419] dark:text-[#F0F6FC] mb-4"
      >
        إنشاء مجموعة جديدة
      </h2>

      <div className="space-y-3.5">
        <div>
          <label className="block text-xs font-semibold text-[#536471] dark:text-[#8B949E] mb-1">
            اسم المجموعة *
          </label>
          <input
            type="text"
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="مثال: أدوات التصميم"
            autoFocus
            className="w-full h-11 px-3 rounded-xl border border-[#CFD9DE] dark:border-[#30363D] bg-transparent text-sm text-[#0F1419] dark:text-[#F0F6FC] focus:outline-none focus:border-[#1D9BF0]"
          />
        </div>

        <div>
          <p className="text-xs font-semibold text-[#536471] dark:text-[#8B949E] mb-1.5">
            اختر أيقونة المجموعة:
          </p>
          <div className="flex flex-wrap gap-1.5">
            {PRESET_EMOJIS.map((emoji) => {
              const isSelected = selectedEmoji === emoji;
              return (
                <button
                  key={emoji}
                  type="button"
                  onClick={() => setSelectedEmoji(emoji)}
                  className={`w-10 h-10 rounded-lg flex items-center justify-center text-lg transition-colors ${
                    isSelected
                      ? 'bg-[#1D9BF0]/20 border-2 border-[#1D9BF0]'
                      : 'bg-[#EFF3F4] dark:bg-[#21262D] border border-[#E2E8F0] dark:border-[#30363D]'
                  }`}
                >
                  {emoji}
                </button>
              );
            })}
          </div>
        </div>

        <div>
          <p className="text-xs font-semibold text-[#536471] dark:text-[#8B949E] mb-1.5">
            اختر لون التمييز:
          </p>
          <div className="flex flex-wrap gap-2">
            {PRESET_COLORS.map((colorHex) => {
              const isSelected = selectedColor === colorHex;
              return (
                <button
                  key={colorHex}
                  type="button"
                  onClick={() => setSelectedColor(colorHex)}
                  aria-label={`لون ${colorHex}`}
                  style={{ backgroundColor: colorHex }}
                  className={`w-8 h-8 rounded-full flex items-center justify-center transition-transform ${
                    isSelected
                      ? 'ring-2 ring-offset-2 ring-[#0F1419] dark:ring-[#F0F6FC] dark:ring-offset-[#161B22] scale-105'
                      : ''
                  }`}
                >
                  {isSelected && <Check className="w-4 h-4 text-white" />}
                </button>
              );
            })}
          </div>
        </div>

        <div>
          <label className="block text-xs font-semibold text-[#536471] dark:text-[#8B949E] mb-1">
            الوصف (اختياري)
          </label>
          <textarea
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="وصف مختصر لمحتوى المجموعة"
            rows={2}
            className="w-full p-3 rounded-xl border border-[#CFD9DE] dark:border-[#30363D] bg-transparent text-sm text-[#0F1419] dark:text-[#F0F6FC] focus:outline-none focus:border-[#1D9BF0] resize-none"
          />
        </div>
      </div>

      <div className="mt-5 flex items-center justify-end gap-2">
        <button
          type="button"
          onClick={onDismiss}
          className="min-h-[42px] px-4 py-2 rounded-xl border border-[#CFD9DE] dark:border-[#30363D] text-sm font-medium text-[#0F1419] dark:text-[#F0F6FC] hover:bg-[#EFF3F4] dark:hover:bg-[#21262D] transition-colors"
        >
          إلغاء
        </button>
        <button
          type="button"
          disabled={!name.trim()}
          onClick={() => {
            if (name.trim()) {
              onConfirm(name, selectedEmoji, selectedColor, description);
            }
          }}
          className="min-h-[42px] px-5 py-2 rounded-xl bg-[#1D9BF0] hover:bg-[#1A8CD8] disabled:opacity-50 text-white text-sm font-bold transition-colors"
        >
          إنشاء المجموعة
        </button>
      </div>
    </ModalBackdrop>
  );
}

export function EditFolderDialog({
  folder,
  onDismiss,
  onConfirm,
}: {
  folder: FolderEntity;
  onDismiss: () => void;
  onConfirm: (
    name: string,
    emoji: string,
    colorHex: string,
    description: string
  ) => void;
}) {
  const [name, setName] = useState(folder.name);
  const [selectedEmoji, setSelectedEmoji] = useState(folder.iconEmoji);
  const [selectedColor, setSelectedColor] = useState(folder.colorHex);
  const [description, setDescription] = useState(folder.description);

  return (
    <ModalBackdrop onDismiss={onDismiss} titleId="edit-folder-title">
      <h2
        id="edit-folder-title"
        className="text-lg font-bold text-[#0F1419] dark:text-[#F0F6FC] mb-4"
      >
        تعديل المجموعة
      </h2>

      <div className="space-y-3.5">
        <div>
          <label className="block text-xs font-semibold text-[#536471] dark:text-[#8B949E] mb-1">
            اسم المجموعة
          </label>
          <input
            type="text"
            value={name}
            onChange={(e) => setName(e.target.value)}
            autoFocus
            className="w-full h-11 px-3 rounded-xl border border-[#CFD9DE] dark:border-[#30363D] bg-transparent text-sm text-[#0F1419] dark:text-[#F0F6FC] focus:outline-none focus:border-[#1D9BF0]"
          />
        </div>

        <div>
          <p className="text-xs font-semibold text-[#536471] dark:text-[#8B949E] mb-1.5">
            الأيقونة:
          </p>
          <div className="flex flex-wrap gap-1.5">
            {PRESET_EMOJIS.map((emoji) => {
              const isSelected = selectedEmoji === emoji;
              return (
                <button
                  key={emoji}
                  type="button"
                  onClick={() => setSelectedEmoji(emoji)}
                  className={`w-10 h-10 rounded-lg flex items-center justify-center text-lg transition-colors ${
                    isSelected
                      ? 'bg-[#1D9BF0]/20 border-2 border-[#1D9BF0]'
                      : 'bg-[#EFF3F4] dark:bg-[#21262D] border border-[#E2E8F0] dark:border-[#30363D]'
                  }`}
                >
                  {emoji}
                </button>
              );
            })}
          </div>
        </div>

        <div>
          <p className="text-xs font-semibold text-[#536471] dark:text-[#8B949E] mb-1.5">
            اللون:
          </p>
          <div className="flex flex-wrap gap-2">
            {PRESET_COLORS.map((colorHex) => {
              const isSelected = selectedColor === colorHex;
              return (
                <button
                  key={colorHex}
                  type="button"
                  onClick={() => setSelectedColor(colorHex)}
                  aria-label={`لون ${colorHex}`}
                  style={{ backgroundColor: colorHex }}
                  className={`w-8 h-8 rounded-full flex items-center justify-center transition-transform ${
                    isSelected
                      ? 'ring-2 ring-offset-2 ring-[#0F1419] dark:ring-[#F0F6FC] dark:ring-offset-[#161B22] scale-105'
                      : ''
                  }`}
                >
                  {isSelected && <Check className="w-4 h-4 text-white" />}
                </button>
              );
            })}
          </div>
        </div>

        <div>
          <label className="block text-xs font-semibold text-[#536471] dark:text-[#8B949E] mb-1">
            الوصف
          </label>
          <textarea
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            rows={2}
            className="w-full p-3 rounded-xl border border-[#CFD9DE] dark:border-[#30363D] bg-transparent text-sm text-[#0F1419] dark:text-[#F0F6FC] focus:outline-none focus:border-[#1D9BF0] resize-none"
          />
        </div>
      </div>

      <div className="mt-5 flex items-center justify-end gap-2">
        <button
          type="button"
          onClick={onDismiss}
          className="min-h-[42px] px-4 py-2 rounded-xl border border-[#CFD9DE] dark:border-[#30363D] text-sm font-medium text-[#0F1419] dark:text-[#F0F6FC] hover:bg-[#EFF3F4] dark:hover:bg-[#21262D] transition-colors"
        >
          إلغاء
        </button>
        <button
          type="button"
          disabled={!name.trim()}
          onClick={() => {
            if (name.trim()) {
              onConfirm(name, selectedEmoji, selectedColor, description);
            }
          }}
          className="min-h-[42px] px-5 py-2 rounded-xl bg-[#1D9BF0] hover:bg-[#1A8CD8] disabled:opacity-50 text-white text-sm font-bold transition-colors"
        >
          حفظ التعديلات
        </button>
      </div>
    </ModalBackdrop>
  );
}

export function DeleteFolderDialog({
  folderItem,
  onDismiss,
  onConfirm,
}: {
  folderItem: FolderWithCount;
  onDismiss: () => void;
  onConfirm: (moveToGeneral: boolean) => void;
}) {
  const [moveToGeneral, setMoveToGeneral] = useState(true);

  return (
    <ModalBackdrop onDismiss={onDismiss} titleId="delete-folder-title">
      <div className="flex flex-col items-center text-center mb-3">
        <div className="w-12 h-12 rounded-full bg-[#EF4444]/15 text-[#EF4444] flex items-center justify-center mb-2">
          <AlertTriangle className="w-6 h-6" />
        </div>
        <h2
          id="delete-folder-title"
          className="text-lg font-bold text-[#0F1419] dark:text-[#F0F6FC]"
        >
          حذف مجموعة &quot;{folderItem.folder.name}&quot;؟
        </h2>
      </div>

      <p className="text-sm text-[#536471] dark:text-[#8B949E] text-center mb-4">
        تحتوي هذه المجموعة على {folderItem.count} علامة مرجعية و {folderItem.accountCount} حساب و {folderItem.peopleCount} شخص.
      </p>

      {(folderItem.count > 0 || folderItem.accountCount > 0 || folderItem.peopleCount > 0) && (
        <label className="flex items-center gap-2.5 p-2.5 rounded-xl bg-[#EFF3F4]/60 dark:bg-[#21262D]/60 cursor-pointer mb-4">
          <input
            type="checkbox"
            checked={moveToGeneral}
            onChange={(e) => setMoveToGeneral(e.target.checked)}
            className="w-4 h-4 accent-[#1D9BF0]"
          />
          <span className="text-xs sm:text-sm text-[#0F1419] dark:text-[#F0F6FC]">
            نقل المحتوى إلى المجموعة الافتراضية بدلاً من حذفه
          </span>
        </label>
      )}

      <div className="flex items-center justify-end gap-2">
        <button
          type="button"
          onClick={onDismiss}
          className="min-h-[42px] px-4 py-2 rounded-xl border border-[#CFD9DE] dark:border-[#30363D] text-sm font-medium text-[#0F1419] dark:text-[#F0F6FC] hover:bg-[#EFF3F4] dark:hover:bg-[#21262D] transition-colors"
        >
          إلغاء
        </button>
        <button
          type="button"
          onClick={() => onConfirm(moveToGeneral)}
          className="min-h-[42px] px-5 py-2 rounded-xl bg-[#EF4444] hover:bg-[#DC2626] text-white text-sm font-bold transition-colors"
        >
          حذف المجموعة
        </button>
      </div>
    </ModalBackdrop>
  );
}

export function MoveBookmarkDialog({
  bookmark,
  folders,
  onDismiss,
  onConfirmMove,
}: {
  bookmark: TweetBookmarkEntity;
  folders: FolderWithCount[];
  onDismiss: () => void;
  onConfirmMove: (newFolderId: number) => void;
}) {
  const [selectedId, setSelectedId] = useState(bookmark.folderId);

  return (
    <ModalBackdrop onDismiss={onDismiss} titleId="move-bookmark-title">
      <div className="flex items-center gap-2 mb-4">
        <FolderInput className="w-5 h-5 text-[#1D9BF0]" />
        <h2
          id="move-bookmark-title"
          className="text-base font-bold text-[#0F1419] dark:text-[#F0F6FC]"
        >
          نقل التغريدة إلى مجموعة أخرى
        </h2>
      </div>

      <div className="space-y-2 max-h-72 overflow-y-auto">
        {folders.map(({ folder }) => {
          const isSelected = selectedId === folder.id;
          const color = folder.colorHex || '#1D9BF0';
          return (
            <button
              key={folder.id}
              type="button"
              onClick={() => setSelectedId(folder.id)}
              style={
                isSelected
                  ? {
                      backgroundColor: `${color}22`,
                      borderColor: color,
                      color,
                    }
                  : undefined
              }
              className={`w-full min-h-[46px] px-3 py-2.5 rounded-xl flex items-center justify-between transition-colors ${
                isSelected
                  ? 'border-[1.5px] font-bold'
                  : 'border border-[#E2E8F0] dark:border-[#30363D] bg-[#EFF3F4]/40 dark:bg-[#21262D]/40 text-[#0F1419] dark:text-[#F0F6FC] hover:bg-[#EFF3F4] dark:hover:bg-[#21262D]'
              }`}
            >
              <div className="flex items-center gap-2">
                <span className="text-lg">{folder.iconEmoji}</span>
                <span className="text-sm">{folder.name}</span>
              </div>
              {isSelected && <Check className="w-4 h-4 shrink-0" />}
            </button>
          );
        })}
      </div>

      <div className="mt-5 flex items-center justify-end gap-2">
        <button
          type="button"
          onClick={onDismiss}
          className="min-h-[42px] px-4 py-2 rounded-xl border border-[#CFD9DE] dark:border-[#30363D] text-sm font-medium text-[#0F1419] dark:text-[#F0F6FC] hover:bg-[#EFF3F4] dark:hover:bg-[#21262D] transition-colors"
        >
          إلغاء
        </button>
        <button
          type="button"
          onClick={() => onConfirmMove(selectedId)}
          className="min-h-[42px] px-5 py-2 rounded-xl bg-[#1D9BF0] hover:bg-[#1A8CD8] text-white text-sm font-bold transition-colors"
        >
          نقل
        </button>
      </div>
    </ModalBackdrop>
  );
}

export function MoveAccountDialog({
  account,
  folders,
  onDismiss,
  onConfirmMove,
}: {
  account: AccountBookmarkEntity;
  folders: FolderWithCount[];
  onDismiss: () => void;
  onConfirmMove: (newFolderId: number) => void;
}) {
  const [selectedId, setSelectedId] = useState(account.folderId);

  return (
    <ModalBackdrop onDismiss={onDismiss} titleId="move-account-title">
      <div className="flex items-center gap-2 mb-4">
        <FolderInput className="w-5 h-5 text-[#1D9BF0]" />
        <h2
          id="move-account-title"
          className="text-base font-bold text-[#0F1419] dark:text-[#F0F6FC]"
        >
          نقل الحساب إلى مجموعة أخرى
        </h2>
      </div>

      <div className="space-y-2 max-h-72 overflow-y-auto">
        {folders.map(({ folder }) => {
          const isSelected = selectedId === folder.id;
          const color = folder.colorHex || '#1D9BF0';
          return (
            <button
              key={folder.id}
              type="button"
              onClick={() => setSelectedId(folder.id)}
              style={
                isSelected
                  ? {
                      backgroundColor: `${color}22`,
                      borderColor: color,
                      color,
                    }
                  : undefined
              }
              className={`w-full min-h-[46px] px-3 py-2.5 rounded-xl flex items-center justify-between transition-colors ${
                isSelected
                  ? 'border-[1.5px] font-bold'
                  : 'border border-[#E2E8F0] dark:border-[#30363D] bg-[#EFF3F4]/40 dark:bg-[#21262D]/40 text-[#0F1419] dark:text-[#F0F6FC] hover:bg-[#EFF3F4] dark:hover:bg-[#21262D]'
              }`}
            >
              <div className="flex items-center gap-2">
                <span className="text-lg">{folder.iconEmoji}</span>
                <span className="text-sm">{folder.name}</span>
              </div>
              {isSelected && <Check className="w-4 h-4 shrink-0" />}
            </button>
          );
        })}
      </div>

      <div className="mt-5 flex items-center justify-end gap-2">
        <button
          type="button"
          onClick={onDismiss}
          className="min-h-[42px] px-4 py-2 rounded-xl border border-[#CFD9DE] dark:border-[#30363D] text-sm font-medium text-[#0F1419] dark:text-[#F0F6FC] hover:bg-[#EFF3F4] dark:hover:bg-[#21262D] transition-colors"
        >
          إلغاء
        </button>
        <button
          type="button"
          onClick={() => onConfirmMove(selectedId)}
          className="min-h-[42px] px-5 py-2 rounded-xl bg-[#1D9BF0] hover:bg-[#1A8CD8] text-white text-sm font-bold transition-colors"
        >
          نقل
        </button>
      </div>
    </ModalBackdrop>
  );
}

export function MovePersonDialog({
  person,
  folders,
  onDismiss,
  onConfirmMove,
}: {
  person: PersonEntity;
  folders: FolderWithCount[];
  onDismiss: () => void;
  onConfirmMove: (newFolderId: number) => void;
}) {
  const [selectedId, setSelectedId] = useState(person.folderId);

  return (
    <ModalBackdrop onDismiss={onDismiss} titleId="move-person-title">
      <div className="flex items-center gap-2 mb-4">
        <FolderInput className="w-5 h-5 text-[#1D9BF0]" />
        <h2
          id="move-person-title"
          className="text-base font-bold text-[#0F1419] dark:text-[#F0F6FC]"
        >
          نقل الشخص إلى مجموعة أخرى
        </h2>
      </div>

      <div className="space-y-2 max-h-72 overflow-y-auto">
        {folders.map(({ folder }) => {
          const isSelected = selectedId === folder.id;
          const color = folder.colorHex || '#1D9BF0';
          return (
            <button
              key={folder.id}
              type="button"
              onClick={() => setSelectedId(folder.id)}
              style={
                isSelected
                  ? {
                      backgroundColor: `${color}22`,
                      borderColor: color,
                      color,
                    }
                  : undefined
              }
              className={`w-full min-h-[46px] px-3 py-2.5 rounded-xl flex items-center justify-between transition-colors ${
                isSelected
                  ? 'border-[1.5px] font-bold'
                  : 'border border-[#E2E8F0] dark:border-[#30363D] bg-[#EFF3F4]/40 dark:bg-[#21262D]/40 text-[#0F1419] dark:text-[#F0F6FC] hover:bg-[#EFF3F4] dark:hover:bg-[#21262D]'
              }`}
            >
              <div className="flex items-center gap-2">
                <span className="text-lg">{folder.iconEmoji}</span>
                <span className="text-sm">{folder.name}</span>
              </div>
              {isSelected && <Check className="w-4 h-4 shrink-0" />}
            </button>
          );
        })}
      </div>

      <div className="mt-5 flex items-center justify-end gap-2">
        <button
          type="button"
          onClick={onDismiss}
          className="min-h-[42px] px-4 py-2 rounded-xl border border-[#CFD9DE] dark:border-[#30363D] text-sm font-medium text-[#0F1419] dark:text-[#F0F6FC] hover:bg-[#EFF3F4] dark:hover:bg-[#21262D] transition-colors"
        >
          إلغاء
        </button>
        <button
          type="button"
          onClick={() => onConfirmMove(selectedId)}
          className="min-h-[42px] px-5 py-2 rounded-xl bg-[#1D9BF0] hover:bg-[#1A8CD8] text-white text-sm font-bold transition-colors"
        >
          نقل
        </button>
      </div>
    </ModalBackdrop>
  );
}
