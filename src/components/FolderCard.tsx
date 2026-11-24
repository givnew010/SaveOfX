import { Bookmark, Pencil, Trash2 } from 'lucide-react';
import type { FolderWithCount } from '../types/bookmark';

interface FolderCardProps {
  item: FolderWithCount;
  onClick: () => void;
  onEdit: () => void;
  onDelete: () => void;
}

export function FolderCard({ item, onClick, onEdit, onDelete }: FolderCardProps) {
  const { folder, count } = item;
  const folderColor = folder.colorHex || '#1D9BF0';

  return (
    <div
      role="button"
      tabIndex={0}
      onClick={onClick}
      onKeyDown={(e) => {
        if (e.key === 'Enter' || e.key === ' ') {
          e.preventDefault();
          onClick();
        }
      }}
      style={{
        borderColor: `${folderColor}40`,
      }}
      className="w-full text-right rounded-2xl bg-white dark:bg-[#161B22] border p-4 shadow-xs hover:shadow-sm transition-all cursor-pointer focus-visible:outline-2 focus-visible:outline-[#1D9BF0]"
    >
      <div className="flex items-center justify-between gap-3">
        <div className="flex items-center gap-3 min-w-0 flex-1">
          <div
            style={{
              backgroundColor: `${folderColor}26`,
              borderColor: `${folderColor}80`,
            }}
            className="w-[46px] h-[46px] rounded-full border-[1.5px] flex items-center justify-center text-[22px] shrink-0 select-none"
          >
            {folder.iconEmoji}
          </div>

          <div className="min-w-0">
            <div className="flex items-center gap-2 min-w-0">
              <h3 className="text-base font-bold text-[#0F1419] dark:text-[#F0F6FC] truncate">
                {folder.name}
              </h3>
              {folder.isDefault && (
                <span className="rounded px-1.5 py-0.5 text-[10px] font-bold bg-[#1D9BF0]/15 text-[#1D9BF0] whitespace-nowrap shrink-0">
                  افتراضي
                </span>
              )}
            </div>

            <div className="flex items-center gap-1 mt-0.5 text-xs text-[#536471] dark:text-[#8B949E] tabular-nums">
              <Bookmark className="w-3.5 h-3.5 shrink-0" style={{ color: folderColor }} />
              <span>{count} علامة مرجعية</span>
            </div>
          </div>
        </div>

        <div
          className="flex items-center gap-0.5 shrink-0"
          onClick={(e) => e.stopPropagation()}
        >
          <button
            type="button"
            onClick={onEdit}
            aria-label="تعديل المجلد"
            title="تعديل المجلد"
            className="min-w-[44px] min-h-[44px] rounded-full flex items-center justify-center text-[#536471] dark:text-[#8B949E] hover:bg-[#EFF3F4] dark:hover:bg-[#21262D] transition-colors"
          >
            <Pencil className="w-[18px] h-[18px]" />
          </button>

          {!folder.isDefault && (
            <button
              type="button"
              onClick={onDelete}
              aria-label="حذف المجلد"
              title="حذف المجلد"
              className="min-w-[44px] min-h-[44px] rounded-full flex items-center justify-center text-[#EF4444]/85 hover:bg-[#EF4444]/10 transition-colors"
            >
              <Trash2 className="w-[18px] h-[18px]" />
            </button>
          )}
        </div>
      </div>

      {folder.description.trim() && (
        <p className="mt-2.5 text-sm text-[#536471] dark:text-[#8B949E] line-clamp-2">
          {folder.description}
        </p>
      )}
    </div>
  );
}
