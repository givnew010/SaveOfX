import { useRef, useState } from 'react';
import type { ChangeEvent, DragEvent } from 'react';
import {
  AlertCircle,
  CheckCircle2,
  ClipboardPaste,
  Copy,
  DatabaseBackup,
  Download,
  Moon,
  RotateCcw,
  Sun,
  Upload,
} from 'lucide-react';
import { BookmarkRepository } from '../data/bookmarkRepository';
import type { BackupPayload } from '../types/bookmark';

interface SettingsViewProps {
  totalFolders: number;
  totalBookmarks: number;
  lastBackupAt: string | null;
  isDarkMode: boolean;
  onSetDarkMode: (dark: boolean) => void;
  onExportBackup: () => BackupPayload;
  onRestoreBackup: (payload: BackupPayload, mode: 'replace' | 'merge') => void;
  onFeedback: (message: string) => void;
}

function formatDateTime(iso: string): string {
  try {
    return new Intl.DateTimeFormat('ar', {
      dateStyle: 'medium',
      timeStyle: 'short',
    }).format(new Date(iso));
  } catch {
    return iso;
  }
}

export function SettingsView({
  totalFolders,
  totalBookmarks,
  lastBackupAt,
  isDarkMode,
  onSetDarkMode,
  onExportBackup,
  onRestoreBackup,
  onFeedback,
}: SettingsViewProps) {
  const fileInputRef = useRef<HTMLInputElement | null>(null);
  const [restoreMode, setRestoreMode] = useState<'merge' | 'replace'>('merge');
  const [pendingBackup, setPendingBackup] = useState<BackupPayload | null>(null);
  const [pendingSourceName, setPendingSourceName] = useState<string>('');
  const [restoreError, setRestoreError] = useState<string | null>(null);
  const [showPasteBox, setShowPasteBox] = useState(false);
  const [pastedJson, setPastedJson] = useState('');
  const [isDragging, setIsDragging] = useState(false);

  const handleDownloadBackup = () => {
    const payload = onExportBackup();
    const json = JSON.stringify(payload, null, 2);
    const blob = new Blob([json], { type: 'application/json;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const datePart = new Date().toISOString().slice(0, 10);
    const anchor = document.createElement('a');
    anchor.href = url;
    anchor.download = `x-folders-backup-${datePart}.json`;
    document.body.appendChild(anchor);
    anchor.click();
    document.body.removeChild(anchor);
    URL.revokeObjectURL(url);
    onFeedback('تم تنزيل ملف النسخة الاحتياطية بنجاح');
  };

  const handleCopyBackup = async () => {
    try {
      const payload = onExportBackup();
      const json = JSON.stringify(payload, null, 2);
      await navigator.clipboard.writeText(json);
      onFeedback('تم نسخ بيانات النسخة الاحتياطية إلى الحافظة');
    } catch {
      onFeedback('تعذر نسخ البيانات إلى الحافظة');
    }
  };

  const processRawBackupText = (rawText: string, sourceLabel: string) => {
    setRestoreError(null);
    const parsed = BookmarkRepository.parseBackupJson(rawText);
    if (!parsed) {
      setPendingBackup(null);
      setPendingSourceName('');
      setRestoreError(
        'الملف أو النص المحدد لا يحتوي على نسخة احتياطية صالحة لتطبيق X Folders.'
      );
      return;
    }
    setPendingBackup(parsed);
    setPendingSourceName(sourceLabel);
  };

  const handleFileChange = async (e: ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    e.target.value = '';
    if (!file) return;
    try {
      const text = await file.text();
      processRawBackupText(text, file.name);
    } catch {
      setRestoreError('تعذر قراءة الملف المحدد.');
    }
  };

  const handleDrop = async (e: DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    setIsDragging(false);
    const file = e.dataTransfer.files?.[0];
    if (!file) return;
    try {
      const text = await file.text();
      processRawBackupText(text, file.name);
    } catch {
      setRestoreError('تعذر قراءة الملف المسحوب.');
    }
  };

  const handleParsePasted = () => {
    if (!pastedJson.trim()) return;
    processRawBackupText(pastedJson, 'نص JSON ملصق');
  };

  const handleConfirmRestore = () => {
    if (!pendingBackup) return;
    onRestoreBackup(pendingBackup, restoreMode);
    setPendingBackup(null);
    setPendingSourceName('');
    setPastedJson('');
    setShowPasteBox(false);
    setRestoreError(null);
  };

  return (
    <div className="max-w-4xl mx-auto px-4 sm:px-6 py-5 space-y-5">
      {/* Backup Card */}
      <section className="rounded-2xl bg-white dark:bg-[#161B22] border border-[#E2E8F0] dark:border-[#30363D] p-5 space-y-4">
        <div className="flex items-start gap-3">
          <div className="w-10 h-10 rounded-full bg-[#1D9BF0]/15 text-[#1D9BF0] flex items-center justify-center shrink-0">
            <DatabaseBackup className="w-5 h-5" />
          </div>
          <div className="min-w-0 flex-1">
            <h2 className="text-base font-bold text-[#0F1419] dark:text-[#F0F6FC]">
              النسخ الاحتياطي للبيانات
            </h2>
            <p className="text-xs sm:text-sm text-[#536471] dark:text-[#8B949E] mt-0.5 leading-relaxed">
              احفظ نسخة كاملة من مجموعاتك وعلاماتك المرجعية وملاحظاتك في ملف JSON لنقلها أو استعادتها في أي وقت.
            </p>
          </div>
        </div>

        <div className="flex flex-wrap items-center justify-between gap-2 rounded-xl bg-[#EFF3F4]/60 dark:bg-[#21262D]/60 px-4 py-3 text-xs sm:text-sm">
          <div className="flex items-center gap-2 text-[#0F1419] dark:text-[#F0F6FC] font-medium tabular-nums">
            <span>{totalFolders} مجموعات</span>
            <span aria-hidden="true" className="text-[#536471] dark:text-[#8B949E]">
              ·
            </span>
            <span>{totalBookmarks} علامة مرجعية</span>
          </div>
          <div className="text-xs text-[#536471] dark:text-[#8B949E]">
            {lastBackupAt
              ? `آخر نسخة احتياطية: ${formatDateTime(lastBackupAt)}`
              : 'لم يتم إنشاء نسخة احتياطية بعد'}
          </div>
        </div>

        <div className="flex flex-wrap items-center gap-2.5">
          <button
            type="button"
            onClick={handleDownloadBackup}
            className="min-h-[44px] px-5 py-2.5 rounded-xl bg-[#1D9BF0] hover:bg-[#1A8CD8] text-white text-sm font-bold flex items-center gap-2 transition-colors whitespace-nowrap"
          >
            <Download className="w-4 h-4" />
            <span>تنزيل نسخة احتياطية (JSON)</span>
          </button>

          <button
            type="button"
            onClick={() => void handleCopyBackup()}
            className="min-h-[44px] px-4 py-2.5 rounded-xl border border-[#CFD9DE] dark:border-[#30363D] bg-transparent hover:bg-[#EFF3F4] dark:hover:bg-[#21262D] text-[#0F1419] dark:text-[#F0F6FC] text-sm font-medium flex items-center gap-2 transition-colors whitespace-nowrap"
          >
            <Copy className="w-4 h-4" />
            <span>نسخ البيانات</span>
          </button>
        </div>
      </section>

      {/* Restore Card */}
      <section className="rounded-2xl bg-white dark:bg-[#161B22] border border-[#E2E8F0] dark:border-[#30363D] p-5 space-y-4">
        <div className="flex items-start gap-3">
          <div className="w-10 h-10 rounded-full bg-[#10B981]/15 text-[#10B981] flex items-center justify-center shrink-0">
            <RotateCcw className="w-5 h-5" />
          </div>
          <div className="min-w-0 flex-1">
            <h2 className="text-base font-bold text-[#0F1419] dark:text-[#F0F6FC]">
              استعادة البيانات من نسخة احتياطية
            </h2>
            <p className="text-xs sm:text-sm text-[#536471] dark:text-[#8B949E] mt-0.5 leading-relaxed">
              استورد ملف نسخة احتياطية بصيغة JSON لاسترجاع جميع المجموعات والتغريدات المحفوظة.
            </p>
          </div>
        </div>

        {/* Restore Mode Selector */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
          <button
            type="button"
            onClick={() => setRestoreMode('merge')}
            className={`text-right p-3.5 rounded-xl border transition-colors ${
              restoreMode === 'merge'
                ? 'border-[#1D9BF0] bg-[#1D9BF0]/10'
                : 'border-[#E2E8F0] dark:border-[#30363D] hover:bg-[#EFF3F4]/50 dark:hover:bg-[#21262D]/50'
            }`}
          >
            <p
              className={`text-sm font-bold ${
                restoreMode === 'merge'
                  ? 'text-[#1D9BF0]'
                  : 'text-[#0F1419] dark:text-[#F0F6FC]'
              }`}
            >
              دمج مع البيانات الحالية
            </p>
            <p className="text-xs text-[#536471] dark:text-[#8B949E] mt-1">
              يضيف المجموعات والتغريدات الجديدة فقط دون حذف بياناتك الحالية.
            </p>
          </button>

          <button
            type="button"
            onClick={() => setRestoreMode('replace')}
            className={`text-right p-3.5 rounded-xl border transition-colors ${
              restoreMode === 'replace'
                ? 'border-[#1D9BF0] bg-[#1D9BF0]/10'
                : 'border-[#E2E8F0] dark:border-[#30363D] hover:bg-[#EFF3F4]/50 dark:hover:bg-[#21262D]/50'
            }`}
          >
            <p
              className={`text-sm font-bold ${
                restoreMode === 'replace'
                  ? 'text-[#1D9BF0]'
                  : 'text-[#0F1419] dark:text-[#F0F6FC]'
              }`}
            >
              استبدال البيانات الحالية
            </p>
            <p className="text-xs text-[#536471] dark:text-[#8B949E] mt-1">
              يستبدل جميع المجموعات والعلامات الحالية بمحتوى النسخة الاحتياطية.
            </p>
          </button>
        </div>

        {/* Dropzone / File Picker */}
        <input
          ref={fileInputRef}
          type="file"
          accept=".json,application/json"
          onChange={(e) => void handleFileChange(e)}
          className="hidden"
        />

        <div
          onDragOver={(e) => {
            e.preventDefault();
            setIsDragging(true);
          }}
          onDragLeave={() => setIsDragging(false)}
          onDrop={(e) => void handleDrop(e)}
          className={`rounded-xl border border-dashed p-5 text-center transition-colors ${
            isDragging
              ? 'border-[#1D9BF0] bg-[#1D9BF0]/10'
              : 'border-[#CFD9DE] dark:border-[#30363D] bg-[#EFF3F4]/35 dark:bg-[#21262D]/35'
          }`}
        >
          <Upload className="w-7 h-7 text-[#1D9BF0] mx-auto mb-2" />
          <p className="text-sm font-medium text-[#0F1419] dark:text-[#F0F6FC]">
            اسحب ملف النسخة الاحتياطية هنا أو اختره من جهازك
          </p>
          <p className="text-xs text-[#536471] dark:text-[#8B949E] mt-1 mb-3">
            صيغة الملف المدعومة: JSON (.json)
          </p>

          <div className="flex flex-wrap items-center justify-center gap-2">
            <button
              type="button"
              onClick={() => fileInputRef.current?.click()}
              className="min-h-[40px] px-4 py-2 rounded-xl bg-[#1D9BF0] hover:bg-[#1A8CD8] text-white text-xs sm:text-sm font-bold inline-flex items-center gap-1.5 transition-colors"
            >
              <Upload className="w-4 h-4" />
              <span>اختيار ملف JSON</span>
            </button>

            <button
              type="button"
              onClick={() => setShowPasteBox((prev) => !prev)}
              className="min-h-[40px] px-4 py-2 rounded-xl border border-[#CFD9DE] dark:border-[#30363D] hover:bg-[#EFF3F4] dark:hover:bg-[#21262D] text-[#0F1419] dark:text-[#F0F6FC] text-xs sm:text-sm font-medium inline-flex items-center gap-1.5 transition-colors"
            >
              <ClipboardPaste className="w-4 h-4" />
              <span>لصق نص JSON يدوياً</span>
            </button>
          </div>
        </div>

        {/* Manual JSON Paste Box */}
        {showPasteBox && (
          <div className="space-y-2.5">
            <textarea
              dir="ltr"
              value={pastedJson}
              onChange={(e) => setPastedJson(e.target.value)}
              placeholder='{"app":"X Folders","version":1,"folders":[...],"bookmarks":[...]}'
              rows={4}
              className="w-full p-3 rounded-xl border border-[#CFD9DE] dark:border-[#30363D] bg-transparent font-mono text-xs text-[#0F1419] dark:text-[#F0F6FC] focus:outline-none focus:border-[#1D9BF0]"
            />
            <div className="flex justify-end">
              <button
                type="button"
                disabled={!pastedJson.trim()}
                onClick={handleParsePasted}
                className="min-h-[40px] px-4 py-2 rounded-xl bg-[#1D9BF0] hover:bg-[#1A8CD8] disabled:opacity-50 text-white text-xs font-bold transition-colors"
              >
                فحص البيانات
              </button>
            </div>
          </div>
        )}

        {/* Validation Error */}
        {restoreError && (
          <div className="rounded-xl bg-[#EF4444]/10 border border-[#EF4444]/30 p-3.5 flex items-center gap-2.5 text-sm text-[#EF4444]">
            <AlertCircle className="w-5 h-5 shrink-0" />
            <span>{restoreError}</span>
          </div>
        )}

        {/* Parsed Backup Preview & Confirm */}
        {pendingBackup && (
          <div className="rounded-xl bg-[#10B981]/10 border border-[#10B981]/30 p-4 space-y-3">
            <div className="flex items-start gap-2.5">
              <CheckCircle2 className="w-5 h-5 text-[#10B981] shrink-0 mt-0.5" />
              <div className="min-w-0 flex-1">
                <p className="text-sm font-bold text-[#0F1419] dark:text-[#F0F6FC]">
                  النسخة الاحتياطية جاهزة للاستعادة ({pendingSourceName})
                </p>
                <p className="text-xs text-[#536471] dark:text-[#8B949E] mt-1 tabular-nums">
                  {pendingBackup.folders.length} مجموعات · {pendingBackup.bookmarks.length} علامة مرجعية · تاريخ التصدير: {formatDateTime(pendingBackup.exportedAt)}
                </p>
              </div>
            </div>

            <div className="flex flex-wrap items-center justify-end gap-2">
              <button
                type="button"
                onClick={() => {
                  setPendingBackup(null);
                  setPendingSourceName('');
                }}
                className="min-h-[40px] px-4 py-2 rounded-xl border border-[#CFD9DE] dark:border-[#30363D] text-xs sm:text-sm font-medium text-[#0F1419] dark:text-[#F0F6FC] hover:bg-black/5 dark:hover:bg-white/5 transition-colors"
              >
                إلغاء
              </button>
              <button
                type="button"
                onClick={handleConfirmRestore}
                className="min-h-[40px] px-5 py-2 rounded-xl bg-[#10B981] hover:bg-[#059669] text-white text-xs sm:text-sm font-bold transition-colors"
              >
                {restoreMode === 'replace'
                  ? 'تأكيد الاستبدال والاستعادة'
                  : 'تأكيد الدمج والاستعادة'}
              </button>
            </div>
          </div>
        )}
      </section>

      {/* Appearance Card */}
      <section className="rounded-2xl bg-white dark:bg-[#161B22] border border-[#E2E8F0] dark:border-[#30363D] p-5 flex flex-wrap items-center justify-between gap-4">
        <div>
          <h2 className="text-base font-bold text-[#0F1419] dark:text-[#F0F6FC]">
            مظهر التطبيق
          </h2>
          <p className="text-xs sm:text-sm text-[#536471] dark:text-[#8B949E] mt-0.5">
            اختر بين الوضع النهاري والوضع الليلي.
          </p>
        </div>

        <div className="flex items-center gap-1 p-1 rounded-xl bg-[#EFF3F4] dark:bg-[#21262D]">
          <button
            type="button"
            onClick={() => onSetDarkMode(false)}
            className={`min-h-[38px] px-3.5 py-1.5 rounded-lg text-xs sm:text-sm font-medium flex items-center gap-1.5 transition-colors ${
              !isDarkMode
                ? 'bg-white text-[#0F1419] shadow-xs font-bold'
                : 'text-[#536471] dark:text-[#8B949E] hover:text-[#0F1419] dark:hover:text-[#F0F6FC]'
            }`}
          >
            <Sun className="w-4 h-4" />
            <span>نهاري</span>
          </button>

          <button
            type="button"
            onClick={() => onSetDarkMode(true)}
            className={`min-h-[38px] px-3.5 py-1.5 rounded-lg text-xs sm:text-sm font-medium flex items-center gap-1.5 transition-colors ${
              isDarkMode
                ? 'bg-[#161B22] text-[#F0F6FC] shadow-xs font-bold'
                : 'text-[#536471] dark:text-[#8B949E] hover:text-[#0F1419] dark:hover:text-[#F0F6FC]'
            }`}
          >
            <Moon className="w-4 h-4" />
            <span>ليلي</span>
          </button>
        </div>
      </section>
    </div>
  );
}
