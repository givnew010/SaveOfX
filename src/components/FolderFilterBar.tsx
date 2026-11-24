import { Plus } from 'lucide-react';
import type { FolderWithCount } from '../types/bookmark';

interface FolderFilterBarProps {
  folders: FolderWithCount[];
  selectedFolderId: number | null;
  showFavoritesOnly: boolean;
  totalBookmarksCount: number;
  onSelectAll: () => void;
  onToggleFavorites: () => void;
  onSelectFolder: (folderId: number) => void;
  onAddNewFolder: () => void;
}

export function FolderFilterBar({
  folders,
  selectedFolderId,
  showFavoritesOnly,
  totalBookmarksCount,
  onSelectAll,
  onToggleFavorites,
  onSelectFolder,
  onAddNewFolder,
}: FolderFilterBarProps) {
  const isAllSelected = selectedFolderId === null && !showFavoritesOnly;

  return (
    <div className="w-full max-w-6xl mx-auto px-4 sm:px-6 py-3">
      <div className="flex items-center gap-2 overflow-x-auto no-scrollbar pb-1">
        {/* All Bookmarks Filter Button */}
        <FilterButton
          selected={isAllSelected}
          onClick={onSelectAll}
          label="كل العلامات"
          icon="📑"
          count={totalBookmarksCount}
          accentColor="#1D9BF0"
        />

        {/* Favorites Filter Button */}
        <FilterButton
          selected={showFavoritesOnly}
          onClick={onToggleFavorites}
          label="المفضلة"
          icon="⭐"
          accentColor="#EF4444"
        />

        {/* Folder Filter Buttons */}
        {folders.map((item) => {
          const folder = item.folder;
          const isSelected = selectedFolderId === folder.id && !showFavoritesOnly;
          return (
            <FilterButton
              key={folder.id}
              selected={isSelected}
              onClick={() => onSelectFolder(folder.id)}
              label={folder.name}
              icon={folder.iconEmoji}
              count={item.count}
              accentColor={folder.colorHex || '#1D9BF0'}
            />
          );
        })}

        {/* Add Folder Quick Button */}
        <button
          type="button"
          onClick={onAddNewFolder}
          className="shrink-0 min-h-[40px] px-3.5 py-2 rounded-full border border-[#E2E8F0] dark:border-[#30363D] bg-[#EFF3F4]/70 dark:bg-[#21262D]/70 hover:bg-[#EFF3F4] dark:hover:bg-[#21262D] text-[#1D9BF0] text-xs font-semibold flex items-center gap-1.5 transition-colors whitespace-nowrap focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#1D9BF0]"
        >
          <Plus className="w-4 h-4" />
          <span>مجموعة جديدة</span>
        </button>
      </div>
    </div>
  );
}

interface FilterButtonProps {
  selected: boolean;
  onClick: () => void;
  label: string;
  icon: string;
  count?: number;
  accentColor: string;
}

function FilterButton({
  selected,
  onClick,
  label,
  icon,
  count,
  accentColor,
}: FilterButtonProps) {
  return (
    <button
      type="button"
      onClick={onClick}
      style={
        selected
          ? {
              backgroundColor: `${accentColor}22`,
              borderColor: accentColor,
              color: accentColor,
            }
          : undefined
      }
      className={`shrink-0 min-h-[40px] px-3.5 py-1.5 rounded-full flex items-center gap-2 text-sm transition-colors whitespace-nowrap focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#1D9BF0] ${
        selected
          ? 'border-[1.5px] font-bold'
          : 'border border-[#E2E8F0] dark:border-[#30363D] bg-white dark:bg-[#161B22] text-[#0F1419] dark:text-[#F0F6FC] font-medium hover:bg-[#EFF3F4]/60 dark:hover:bg-[#21262D]/60'
      }`}
    >
      <span className="text-sm leading-none">{icon}</span>
      <span>{label}</span>
      {typeof count === 'number' && (
        <span
          style={selected ? { backgroundColor: accentColor } : undefined}
          className={`px-1.5 py-0.5 rounded-full text-[11px] font-bold tabular-nums leading-none ${
            selected
              ? 'text-white'
              : 'bg-[#EFF3F4] dark:bg-[#21262D] text-[#536471] dark:text-[#8B949E]'
          }`}
        >
          {count}
        </span>
      )}
    </button>
  );
}
