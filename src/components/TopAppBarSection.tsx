import { useState } from 'react';
import { Bookmark, Folder, Moon, Search, Settings, Sun, Users, X } from 'lucide-react';
import type { MainTab } from '../types/bookmark';

interface TopAppBarSectionProps {
  totalBookmarks: number;
  totalAccounts: number;
  searchQuery: string;
  onSearchQueryChange: (query: string) => void;
  activeTab: MainTab;
  onTabSelected: (tab: MainTab) => void;
  isDarkMode: boolean;
  onToggleTheme: () => void;
}

export function TopAppBarSection({
  totalBookmarks,
  totalAccounts,
  searchQuery,
  onSearchQueryChange,
  activeTab,
  onTabSelected,
  isDarkMode,
  onToggleTheme,
}: TopAppBarSectionProps) {
  const [isSearchExpanded, setIsSearchExpanded] = useState(false);

  const handleToggleSearch = () => {
    setIsSearchExpanded((prev) => {
      if (prev) {
        onSearchQueryChange('');
      }
      return !prev;
    });
  };

  return (
    <header className="sticky top-0 z-30 w-full bg-white/95 dark:bg-[#161B22]/95 backdrop-blur-md border-b border-[#E2E8F0] dark:border-[#21262D]">
      <div className="max-w-6xl mx-auto">
        {/* Main Bar */}
        <div className="flex items-center justify-between px-4 sm:px-6 py-3 gap-4">
          <div className="flex items-center gap-2.5 min-w-0">
            <div className="w-10 h-10 rounded-full bg-[#1D9BF0] flex items-center justify-center shrink-0 text-white text-xl font-black select-none">
              𝕏
            </div>

            <div className="min-w-0">
              <div className="flex items-center gap-2">
                <h1 className="text-lg sm:text-xl font-bold text-[#0F1419] dark:text-[#F0F6FC] whitespace-nowrap truncate">
                  مجموعات X
                </h1>
                <span className="rounded px-1.5 py-0.5 text-[11px] font-bold bg-[#1D9BF0]/15 text-[#1D9BF0] whitespace-nowrap shrink-0">
                  Premium
                </span>
              </div>
              <p className="text-xs text-[#536471] dark:text-[#8B949E] tabular-nums whitespace-nowrap">
                {totalBookmarks} علامة مرجعية · {totalAccounts} حساب
              </p>
            </div>
          </div>

          <div className="flex items-center gap-1 shrink-0">
            <button
              type="button"
              onClick={handleToggleSearch}
              aria-label="بحث"
              className={`min-w-[44px] min-h-[44px] rounded-full flex items-center justify-center transition-colors focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#1D9BF0] ${
                isSearchExpanded
                  ? 'text-[#1D9BF0] bg-[#1D9BF0]/10'
                  : 'text-[#0F1419] dark:text-[#F0F6FC] hover:bg-[#EFF3F4] dark:hover:bg-[#21262D]'
              }`}
            >
              {isSearchExpanded ? <X className="w-5 h-5" /> : <Search className="w-5 h-5" />}
            </button>

            <button
              type="button"
              onClick={onToggleTheme}
              aria-label={isDarkMode ? 'الوضع النهاري' : 'الوضع الليلي'}
              className="min-w-[44px] min-h-[44px] rounded-full flex items-center justify-center text-[#536471] dark:text-[#8B949E] hover:bg-[#EFF3F4] dark:hover:bg-[#21262D] transition-colors focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#1D9BF0]"
            >
              {isDarkMode ? <Sun className="w-5 h-5" /> : <Moon className="w-5 h-5" />}
            </button>
          </div>
        </div>

        {/* Expandable Search Field */}
        {isSearchExpanded && (
          <div className="px-4 sm:px-6 pb-3">
            <div className="relative flex items-center">
              <Search className="w-4 h-4 text-[#1D9BF0] absolute right-4 pointer-events-none" />
              <input
                type="text"
                value={searchQuery}
                onChange={(e) => onSearchQueryChange(e.target.value)}
                placeholder={
                  activeTab === 'ACCOUNTS'
                    ? 'ابحث في الحسابات، الاسم، المعرف، النبذة، الوسم…'
                    : 'ابحث في محتوى التغريدة، اسم الكاتب، الوسم…'
                }
                autoFocus
                className="w-full h-11 pr-11 pl-10 rounded-full border border-[#E2E8F0] dark:border-[#30363D] bg-[#EFF3F4]/60 dark:bg-[#21262D]/60 text-sm text-[#0F1419] dark:text-[#F0F6FC] placeholder:text-[#536471] dark:placeholder:text-[#8B949E] focus:outline-none focus:border-[#1D9BF0] focus:bg-white dark:focus:bg-[#161B22] transition-colors"
              />
              {searchQuery.length > 0 && (
                <button
                  type="button"
                  onClick={() => onSearchQueryChange('')}
                  aria-label="مسح البحث"
                  className="absolute left-2 w-8 h-8 rounded-full flex items-center justify-center text-[#536471] dark:text-[#8B949E] hover:bg-[#E2E8F0] dark:hover:bg-[#30363D]"
                >
                  <X className="w-4 h-4" />
                </button>
              )}
            </div>
          </div>
        )}

        {/* Tab Row (Bookmarks vs Accounts vs Collections vs Settings) */}
        <div className="grid grid-cols-4" role="tablist" aria-label="الأقسام الرئيسية">
          <button
            type="button"
            role="tab"
            aria-selected={activeTab === 'BOOKMARKS'}
            onClick={() => onTabSelected('BOOKMARKS')}
            className={`relative min-h-[54px] sm:min-h-[48px] px-1 sm:px-2 py-2 sm:py-0 flex flex-col sm:flex-row items-center justify-center gap-0.5 sm:gap-1.5 text-[11px] sm:text-sm transition-colors whitespace-nowrap focus-visible:outline-2 focus-visible:outline-inset focus-visible:outline-[#1D9BF0] ${
              activeTab === 'BOOKMARKS'
                ? 'text-[#1D9BF0] font-bold'
                : 'text-[#536471] dark:text-[#8B949E] font-medium hover:text-[#0F1419] dark:hover:text-[#F0F6FC]'
            }`}
          >
            <Bookmark className="w-4 h-4 shrink-0" />
            <span>العلامات المرجعية</span>
            {activeTab === 'BOOKMARKS' && (
              <span className="absolute bottom-0 inset-x-0 h-[3px] bg-[#1D9BF0]" />
            )}
          </button>

          <button
            type="button"
            role="tab"
            aria-selected={activeTab === 'ACCOUNTS'}
            onClick={() => onTabSelected('ACCOUNTS')}
            className={`relative min-h-[54px] sm:min-h-[48px] px-1 sm:px-2 py-2 sm:py-0 flex flex-col sm:flex-row items-center justify-center gap-0.5 sm:gap-1.5 text-[11px] sm:text-sm transition-colors whitespace-nowrap focus-visible:outline-2 focus-visible:outline-inset focus-visible:outline-[#1D9BF0] ${
              activeTab === 'ACCOUNTS'
                ? 'text-[#1D9BF0] font-bold'
                : 'text-[#536471] dark:text-[#8B949E] font-medium hover:text-[#0F1419] dark:hover:text-[#F0F6FC]'
            }`}
          >
            <Users className="w-4 h-4 shrink-0" />
            <span>الحسابات</span>
            {activeTab === 'ACCOUNTS' && (
              <span className="absolute bottom-0 inset-x-0 h-[3px] bg-[#1D9BF0]" />
            )}
          </button>

          <button
            type="button"
            role="tab"
            aria-selected={activeTab === 'FOLDERS'}
            onClick={() => onTabSelected('FOLDERS')}
            className={`relative min-h-[54px] sm:min-h-[48px] px-1 sm:px-2 py-2 sm:py-0 flex flex-col sm:flex-row items-center justify-center gap-0.5 sm:gap-1.5 text-[11px] sm:text-sm transition-colors whitespace-nowrap focus-visible:outline-2 focus-visible:outline-inset focus-visible:outline-[#1D9BF0] ${
              activeTab === 'FOLDERS'
                ? 'text-[#1D9BF0] font-bold'
                : 'text-[#536471] dark:text-[#8B949E] font-medium hover:text-[#0F1419] dark:hover:text-[#F0F6FC]'
            }`}
          >
            <Folder className="w-4 h-4 shrink-0" />
            <span>المجموعات</span>
            {activeTab === 'FOLDERS' && (
              <span className="absolute bottom-0 inset-x-0 h-[3px] bg-[#1D9BF0]" />
            )}
          </button>

          <button
            type="button"
            role="tab"
            aria-selected={activeTab === 'SETTINGS'}
            onClick={() => onTabSelected('SETTINGS')}
            className={`relative min-h-[54px] sm:min-h-[48px] px-1 sm:px-2 py-2 sm:py-0 flex flex-col sm:flex-row items-center justify-center gap-0.5 sm:gap-1.5 text-[11px] sm:text-sm transition-colors whitespace-nowrap focus-visible:outline-2 focus-visible:outline-inset focus-visible:outline-[#1D9BF0] ${
              activeTab === 'SETTINGS'
                ? 'text-[#1D9BF0] font-bold'
                : 'text-[#536471] dark:text-[#8B949E] font-medium hover:text-[#0F1419] dark:hover:text-[#F0F6FC]'
            }`}
          >
            <Settings className="w-4 h-4 shrink-0" />
            <span>الإعدادات</span>
            {activeTab === 'SETTINGS' && (
              <span className="absolute bottom-0 inset-x-0 h-[3px] bg-[#1D9BF0]" />
            )}
          </button>
        </div>
      </div>
    </header>
  );
}
