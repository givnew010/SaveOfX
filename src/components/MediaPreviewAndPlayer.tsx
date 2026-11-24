import { useEffect, useState } from 'react';
import { ExternalLink, ImageOff, Play, Video, X } from 'lucide-react';
import type { TweetMediaItem } from '../types/bookmark';

interface TweetMediaGalleryProps {
  mediaItems: TweetMediaItem[];
  tweetUrl: string;
}

function formatDuration(seconds?: number): string | null {
  if (!seconds || seconds <= 0) return null;
  const total = Math.floor(seconds);
  const mins = Math.floor(total / 60);
  const secs = total % 60;
  return `${mins}:${secs.toString().padStart(2, '0')}`;
}

export function TweetMediaGallery({ mediaItems, tweetUrl }: TweetMediaGalleryProps) {
  const [activePhotoUrl, setActivePhotoUrl] = useState<string | null>(null);
  const [activeVideoItem, setActiveVideoItem] = useState<TweetMediaItem | null>(null);

  if (mediaItems.length === 0) return null;

  const photos = mediaItems.filter((item) => item.type === 'photo');
  const videos = mediaItems.filter((item) => item.type === 'video' || item.type === 'gif');

  return (
    <>
      <div className="w-full flex flex-col gap-1.5">
        {videos.map((video, idx) => (
          <VideoThumbnailBox
            key={`${video.url}-${idx}`}
            item={video}
            onClick={() => setActiveVideoItem(video)}
          />
        ))}

        {photos.length === 1 && (
          <PhotoBox
            url={photos[0].url}
            className="w-full max-h-72 min-h-40 aspect-video"
            onClick={() => setActivePhotoUrl(photos[0].url)}
          />
        )}

        {photos.length === 2 && (
          <div className="grid grid-cols-2 gap-1.5">
            {photos.map((photo, idx) => (
              <PhotoBox
                key={`${photo.url}-${idx}`}
                url={photo.url}
                className="w-full aspect-square"
                onClick={() => setActivePhotoUrl(photo.url)}
              />
            ))}
          </div>
        )}

        {photos.length >= 3 && (
          <div className="grid grid-cols-2 gap-1.5">
            {photos.slice(0, 4).map((photo, idx) => (
              <PhotoBox
                key={`${photo.url}-${idx}`}
                url={photo.url}
                className="w-full aspect-[6/5]"
                onClick={() => setActivePhotoUrl(photo.url)}
              />
            ))}
          </div>
        )}
      </div>

      {activePhotoUrl && (
        <FullScreenImageDialog
          imageUrl={activePhotoUrl}
          onDismiss={() => setActivePhotoUrl(null)}
        />
      )}

      {activeVideoItem && (
        <InAppVideoPlayerDialog
          videoItem={activeVideoItem}
          tweetUrl={tweetUrl}
          onDismiss={() => setActiveVideoItem(null)}
        />
      )}
    </>
  );
}

function PhotoBox({
  url,
  className,
  onClick,
}: {
  url: string;
  className: string;
  onClick: () => void;
}) {
  const [hasError, setHasError] = useState(false);

  return (
    <button
      type="button"
      onClick={onClick}
      className={`relative overflow-hidden rounded-xl border border-[#E2E8F0] dark:border-[#30363D] bg-[#EFF3F4]/60 dark:bg-[#21262D]/60 flex items-center justify-center group focus-visible:outline-2 focus-visible:outline-[#1D9BF0] ${className}`}
    >
      {!hasError ? (
        <img
          src={url}
          alt="صورة مرفقة بالتغريدة"
          referrerPolicy="no-referrer"
          loading="lazy"
          onError={() => setHasError(true)}
          className="w-full h-full object-cover transition-transform duration-200 group-hover:scale-[1.02]"
        />
      ) : (
        <div className="flex flex-col items-center justify-center gap-1 text-[#536471] dark:text-[#8B949E] p-4">
          <ImageOff className="w-6 h-6" />
          <span className="text-xs">تعذر تحميل الصورة</span>
        </div>
      )}
    </button>
  );
}

function VideoThumbnailBox({
  item,
  onClick,
}: {
  item: TweetMediaItem;
  onClick: () => void;
}) {
  const [thumbError, setThumbError] = useState(false);
  const durationLabel = formatDuration(item.durationSeconds);

  return (
    <button
      type="button"
      onClick={onClick}
      className="relative w-full aspect-video rounded-xl overflow-hidden bg-[#0D1117] border border-[#E2E8F0] dark:border-[#30363D] flex items-center justify-center group focus-visible:outline-2 focus-visible:outline-[#1D9BF0]"
    >
      {item.thumbnailUrl && !thumbError ? (
        <img
          src={item.thumbnailUrl}
          alt="معاينة الفيديو"
          referrerPolicy="no-referrer"
          loading="lazy"
          onError={() => setThumbError(true)}
          className="w-full h-full object-cover"
        />
      ) : (
        <Video className="w-12 h-12 text-white/50" />
      )}

      <div className="absolute inset-0 bg-black/30 group-hover:bg-black/40 transition-colors" />

      <div className="relative z-10 w-14 h-14 rounded-full bg-[#1D9BF0]/95 border-2 border-white flex items-center justify-center shadow-lg transition-transform duration-200 group-hover:scale-105">
        <Play className="w-7 h-7 text-white fill-white" />
      </div>

      <div className="absolute bottom-2.5 left-2.5 z-10 flex items-center gap-1.5 rounded-md bg-black/75 px-2 py-1 text-[11px] font-bold text-white">
        <span>{item.type === 'gif' ? 'GIF' : 'فيديو'}</span>
        {durationLabel && (
          <span className="font-mono tabular-nums text-white/90">{durationLabel}</span>
        )}
      </div>
    </button>
  );
}

function FullScreenImageDialog({
  imageUrl,
  onDismiss,
}: {
  imageUrl: string;
  onDismiss: () => void;
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
      aria-label="عرض الصورة"
      onClick={onDismiss}
      className="fixed inset-0 z-50 bg-black/95 flex items-center justify-center p-4"
    >
      <img
        src={imageUrl}
        alt="عرض الصورة"
        referrerPolicy="no-referrer"
        onClick={(e) => e.stopPropagation()}
        className="max-w-full max-h-[90vh] object-contain rounded-lg"
      />

      <button
        type="button"
        onClick={onDismiss}
        aria-label="إغلاق"
        className="absolute top-4 left-4 w-11 h-11 rounded-full bg-black/60 hover:bg-black/80 text-white flex items-center justify-center transition-colors"
      >
        <X className="w-5 h-5" />
      </button>
    </div>
  );
}

function InAppVideoPlayerDialog({
  videoItem,
  tweetUrl,
  onDismiss,
}: {
  videoItem: TweetMediaItem;
  tweetUrl: string;
  onDismiss: () => void;
}) {
  const [hasError, setHasError] = useState(false);

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
      aria-label="مشغل الفيديو"
      className="fixed inset-0 z-50 bg-black/95 flex items-center justify-center p-4"
    >
      <button
        type="button"
        onClick={onDismiss}
        aria-label="إغلاق الفيديو"
        className="absolute top-4 left-4 z-10 w-11 h-11 rounded-full bg-black/70 hover:bg-black/90 text-white flex items-center justify-center transition-colors"
      >
        <X className="w-5 h-5" />
      </button>

      {!hasError ? (
        <video
          src={videoItem.url}
          poster={videoItem.thumbnailUrl}
          controls
          autoPlay
          loop={videoItem.type === 'gif'}
          playsInline
          onError={() => setHasError(true)}
          className="max-w-4xl w-full max-h-[85vh] rounded-xl bg-black"
        />
      ) : (
        <div className="flex flex-col items-center gap-4 text-center max-w-md p-6">
          <Video className="w-12 h-12 text-white/70" />
          <p className="text-white text-base font-medium">
            تعذر تشغيل هذا الفيديو مباشرة داخل التطبيق
          </p>
          <a
            href={tweetUrl}
            target="_blank"
            rel="noopener noreferrer"
            className="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl bg-[#1D9BF0] hover:bg-[#1A8CD8] text-white font-bold text-sm transition-colors"
          >
            <ExternalLink className="w-4 h-4" />
            <span>مشاهدة في X</span>
          </a>
        </div>
      )}
    </div>
  );
}
