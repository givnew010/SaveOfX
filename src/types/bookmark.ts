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

export interface ParsedAccount {
  profileUrl: string;
  handle: string;
  username: string;
  displayName: string;
  avatarUrl?: string;
  bio: string;
  followersCount: number;
  followingCount: number;
  verified: boolean;
  isProfileFetched: boolean;
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

export interface AccountBookmarkEntity {
  id: number;
  folderId: number;
  profileUrl: string;
  handle: string;
  displayName: string;
  avatarUrl?: string;
  bio: string;
  followersCount: number;
  followingCount: number;
  verified: boolean;
  savedAt: number;
  userNote?: string;
  tags: string;
  isFavorite: boolean;
  isProfileFetched: boolean;
}

export type PersonWorkType = 'link' | 'photo' | 'video' | 'other';

export interface PersonWorkItem {
  id: string;
  type: PersonWorkType;
  title: string;
  url: string;
  note?: string;
  addedAt: number;
}

export interface PersonEntity {
  id: number;
  folderId: number;
  name: string;
  role: string;
  avatarUrl?: string;
  bio: string;
  userNote?: string;
  tags: string;
  works: PersonWorkItem[];
  isFavorite: boolean;
  savedAt: number;
}

export interface FolderWithCount {
  folder: FolderEntity;
  count: number;
  accountCount: number;
  peopleCount: number;
}

export type MainTab = 'BOOKMARKS' | 'ACCOUNTS' | 'PEOPLE' | 'FOLDERS' | 'SETTINGS';

export interface BackupPayload {
  app: string;
  version: number;
  exportedAt: string;
  folders: FolderEntity[];
  bookmarks: TweetBookmarkEntity[];
  accounts?: AccountBookmarkEntity[];
  people?: PersonEntity[];
}
