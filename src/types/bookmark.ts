export type MediaKind = 'photo' | 'video' | 'gif';

export interface TweetMediaItem {
  type: MediaKind;
  url: string;
  thumbnailUrl?: string;
  durationSeconds?: number;
}

export interface ParsedTweet {
  tweetUrl: string;
  tweetId: string;
  authorName: string;
  authorHandle: string;
  authorAvatarUrl?: string;
  content: string;
  mediaUrl?: string;
  mediaType?: MediaKind;
  mediaItems: TweetMediaItem[];
  postedAt: string;
  likesCount: number;
  repostsCount: number;
  quoteAuthorName?: string;
  quoteAuthorHandle?: string;
  quoteText?: string;
  isContentFetched: boolean;
}

export interface FolderEntity {
  id: number;
  name: string;
  iconEmoji: string;
  colorHex: string;
  description: string;
  createdAt: number;
  isDefault: boolean;
}

export interface TweetBookmarkEntity {
  id: number;
  folderId: number;
  tweetUrl: string;
  tweetId: string;
  authorName: string;
  authorHandle: string;
  authorAvatarUrl?: string;
  content: string;
  mediaUrl?: string;
  postedAt: string;
  savedAt: number;
  userNote?: string;
  tags: string;
  isFavorite: boolean;
  likesCount: number;
  repostsCount: number;
  isContentFetched: boolean;
  mediaType?: MediaKind;
  mediaJson?: string;
  quoteAuthorName?: string;
  quoteAuthorHandle?: string;
  quoteText?: string;
}

export interface FolderWithCount {
  folder: FolderEntity;
  count: number;
}

export type MainTab = 'BOOKMARKS' | 'FOLDERS';
