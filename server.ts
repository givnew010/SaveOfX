import express from 'express';
import type { Request, Response } from 'express';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const PORT = 3000;
const TWEET_URL_REGEX =
  /https?:\/\/(?:www\.|mobile\.)?(?:x\.com|twitter\.com|fxtwitter\.com|vxtwitter\.com)\/([a-zA-Z0-9_]+)\/status\/([0-9]+)(?:\S*)?/i;

interface TweetMediaItem {
  type: 'photo' | 'video' | 'gif';
  url: string;
  thumbnailUrl?: string;
  durationSeconds?: number;
}

interface ParsedTweet {
  tweetUrl: string;
  tweetId: string;
  authorName: string;
  authorHandle: string;
  authorAvatarUrl?: string;
  content: string;
  mediaUrl?: string;
  mediaType?: 'photo' | 'video' | 'gif';
  mediaItems: TweetMediaItem[];
  postedAt: string;
  likesCount: number;
  repostsCount: number;
  quoteAuthorName?: string;
  quoteAuthorHandle?: string;
  quoteText?: string;
  isContentFetched: boolean;
}

interface FxMediaItem {
  type?: string;
  url?: string;
  thumbnail_url?: string;
  duration?: number;
}

interface FxTweetResponse {
  code?: number;
  tweet?: {
    id?: string;
    text?: string;
    raw_text?: { text?: string };
    author?: {
      name?: string;
      screen_name?: string;
      avatar_url?: string;
    };
    created_at?: string;
    created_timestamp?: number;
    likes?: number;
    retweets?: number;
    media?: {
      all?: FxMediaItem[];
      photos?: FxMediaItem[];
      videos?: FxMediaItem[];
    };
    quote?: {
      text?: string;
      author?: {
        name?: string;
        screen_name?: string;
        avatar_url?: string;
      };
    };
  };
}

interface VxMediaItem {
  type?: string;
  url?: string;
  thumbnail_url?: string;
  duration_millis?: number;
}

interface VxTweetResponse {
  tweetID?: string;
  text?: string;
  user_name?: string;
  user_screen_name?: string;
  user_profile_image_url?: string;
  likes?: number;
  retweets?: number;
  date?: string;
  date_epoch?: number;
  media_extended?: VxMediaItem[];
}

interface XOEmbedResponse {
  author_name?: string;
  html?: string;
}

function formatEpochDate(epochSeconds: number): string {
  try {
    return new Intl.DateTimeFormat('en-GB', {
      day: 'numeric',
      month: 'short',
      year: 'numeric',
    }).format(new Date(epochSeconds * 1000));
  } catch {
    return '';
  }
}

async function fetchJsonWithTimeout<T>(url: string, timeoutMs = 10000): Promise<T | null> {
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), timeoutMs);
  try {
    const res = await fetch(url, {
      signal: controller.signal,
      headers: {
        'User-Agent': 'XFoldersWeb/1.0',
        Accept: 'application/json',
      },
    });
    if (!res.ok) return null;
    return (await res.json()) as T;
  } catch {
    return null;
  } finally {
    clearTimeout(timer);
  }
}

function parseFxTwitterJson(root: FxTweetResponse, cleanUrl: string): ParsedTweet | null {
  const tweetObj = root?.tweet;
  if (!tweetObj) return null;

  const text =
    (tweetObj.raw_text?.text && tweetObj.raw_text.text.trim() ? tweetObj.raw_text.text : '') ||
    tweetObj.text ||
    '';

  const authorName = tweetObj.author?.name?.trim() || 'X User';
  const screenName = tweetObj.author?.screen_name?.trim() || 'x';
  const avatarUrl = tweetObj.author?.avatar_url?.trim() || undefined;

  const createdTs = tweetObj.created_timestamp || 0;
  const postedAt =
    createdTs > 0
      ? formatEpochDate(createdTs)
      : (tweetObj.created_at || '').slice(0, 16);

  const rawMediaList =
    tweetObj.media?.all && tweetObj.media.all.length > 0
      ? tweetObj.media.all
      : [...(tweetObj.media?.photos || []), ...(tweetObj.media?.videos || [])];

  const mediaItems: TweetMediaItem[] = rawMediaList
    .filter((m) => Boolean(m.url && m.url.trim()))
    .map((m) => {
      const rawType = (m.type || 'photo').toLowerCase();
      const normalizedType: 'photo' | 'video' | 'gif' = rawType.includes('video')
        ? 'video'
        : rawType.includes('gif')
          ? 'gif'
          : 'photo';
      return {
        type: normalizedType,
        url: m.url!.trim(),
        thumbnailUrl: m.thumbnail_url?.trim() || undefined,
        durationSeconds: typeof m.duration === 'number' ? m.duration : undefined,
      };
    });

  let mediaTypeSummary: 'photo' | 'video' | 'gif' | undefined;
  if (mediaItems.some((m) => m.type === 'video')) {
    mediaTypeSummary = 'video';
  } else if (mediaItems.some((m) => m.type === 'gif')) {
    mediaTypeSummary = 'gif';
  } else if (mediaItems.length > 0) {
    mediaTypeSummary = 'photo';
  }

  const primaryMediaUrl = mediaItems[0]?.thumbnailUrl || mediaItems[0]?.url;

  return {
    tweetUrl: cleanUrl,
    tweetId: tweetObj.id?.trim() || cleanUrl.split('/').pop() || '',
    authorName,
    authorHandle: `@${screenName}`,
    authorAvatarUrl: avatarUrl,
    content: text.trim() ? text : mediaItems.length > 0 ? '' : cleanUrl,
    mediaUrl: primaryMediaUrl,
    mediaType: mediaTypeSummary,
    mediaItems,
    postedAt,
    likesCount: tweetObj.likes || 0,
    repostsCount: tweetObj.retweets || 0,
    quoteAuthorName: tweetObj.quote?.author?.name?.trim() || undefined,
    quoteAuthorHandle: tweetObj.quote?.author?.screen_name?.trim()
      ? `@${tweetObj.quote.author.screen_name.trim()}`
      : undefined,
    quoteText: tweetObj.quote?.text?.trim() || undefined,
    isContentFetched: true,
  };
}

function parseVxTwitterJson(root: VxTweetResponse, cleanUrl: string): ParsedTweet | null {
  if (!root || (root.text == null && root.user_name == null)) return null;

  const text = root.text || '';
  const authorName = root.user_name?.trim() || 'X User';
  const screenName = root.user_screen_name?.trim() || 'x';
  const avatarUrl = root.user_profile_image_url?.trim() || undefined;
  const dateEpoch = root.date_epoch || 0;
  const postedAt =
    dateEpoch > 0 ? formatEpochDate(dateEpoch) : (root.date || '').slice(0, 16);

  const mediaItems: TweetMediaItem[] = (root.media_extended || [])
    .filter((m) => Boolean(m.url && m.url.trim()))
    .map((m) => {
      const rawType = (m.type || 'image').toLowerCase();
      const normalizedType: 'photo' | 'video' | 'gif' =
        rawType === 'video' ? 'video' : rawType === 'gif' ? 'gif' : 'photo';
      return {
        type: normalizedType,
        url: m.url!.trim(),
        thumbnailUrl: m.thumbnail_url?.trim() || undefined,
        durationSeconds:
          typeof m.duration_millis === 'number' ? m.duration_millis / 1000 : undefined,
      };
    });

  let mediaTypeSummary: 'photo' | 'video' | 'gif' | undefined;
  if (mediaItems.some((m) => m.type === 'video')) {
    mediaTypeSummary = 'video';
  } else if (mediaItems.some((m) => m.type === 'gif')) {
    mediaTypeSummary = 'gif';
  } else if (mediaItems.length > 0) {
    mediaTypeSummary = 'photo';
  }

  return {
    tweetUrl: cleanUrl,
    tweetId: root.tweetID?.trim() || cleanUrl.split('/').pop() || '',
    authorName,
    authorHandle: `@${screenName}`,
    authorAvatarUrl: avatarUrl,
    content: text.trim() ? text : mediaItems.length > 0 ? '' : cleanUrl,
    mediaUrl: mediaItems[0]?.thumbnailUrl || mediaItems[0]?.url,
    mediaType: mediaTypeSummary,
    mediaItems,
    postedAt,
    likesCount: root.likes || 0,
    repostsCount: root.retweets || 0,
    isContentFetched: true,
  };
}

function parseOEmbedJson(
  root: XOEmbedResponse,
  handle: string,
  tweetId: string,
  cleanUrl: string
): ParsedTweet | null {
  if (!root || !root.html) return null;
  const match = root.html.match(/<p[^>]*>([\s\S]*?)<\/p>/i);
  if (!match || !match[1]) return null;

  const extractedText = match[1]
    .replace(/<br\s*\/?>/gi, '\n')
    .replace(/<[^>]*>/g, '')
    .replace(/&amp;/g, '&')
    .replace(/&lt;/g, '<')
    .replace(/&gt;/g, '>')
    .replace(/&quot;/g, '"')
    .replace(/&#39;/g, "'")
    .trim();

  if (!extractedText) return null;

  return {
    tweetUrl: cleanUrl,
    tweetId,
    authorName: root.author_name?.trim() || handle,
    authorHandle: `@${handle}`,
    content: extractedText,
    mediaItems: [],
    postedAt: formatEpochDate(Math.floor(Date.now() / 1000)),
    likesCount: 0,
    repostsCount: 0,
    isContentFetched: true,
  };
}

interface FxUserResponse {
  code?: number;
  user?: {
    id?: string;
    name?: string;
    screen_name?: string;
    avatar_url?: string;
    description?: string;
    followers?: number;
    following?: number;
    verification?: {
      verified?: boolean;
    };
  };
}

interface ParsedAccount {
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

const ACCOUNT_RESERVED_PATHS = new Set([
  'home',
  'explore',
  'search',
  'notifications',
  'messages',
  'i',
  'intent',
  'settings',
  'compose',
  'hashtag',
  'share',
  'login',
  'signup',
]);

function extractAccountUsername(rawInput: string): string | null {
  const trimmed = rawInput.trim();
  if (!trimmed) return null;
  const directMatch = trimmed.match(/^@?([a-zA-Z0-9_]{1,15})$/);
  if (directMatch && directMatch[1]) {
    return ACCOUNT_RESERVED_PATHS.has(directMatch[1].toLowerCase()) ? null : directMatch[1];
  }
  const urlMatch = trimmed.match(
    /https?:\/\/(?:www\.|mobile\.)?(?:x\.com|twitter\.com|fxtwitter\.com|vxtwitter\.com)\/([a-zA-Z0-9_]{1,15})(?:[/?#]\S*)?/i
  );
  if (urlMatch && urlMatch[1]) {
    return ACCOUNT_RESERVED_PATHS.has(urlMatch[1].toLowerCase()) ? null : urlMatch[1];
  }
  return null;
}

function parseFxUserJson(root: FxUserResponse, fallbackUsername: string): ParsedAccount | null {
  const user = root?.user;
  if (!user) return null;
  const username = user.screen_name?.trim() || fallbackUsername;
  const displayName = user.name?.trim() || username;
  return {
    profileUrl: `https://x.com/${username}`,
    handle: `@${username}`,
    username,
    displayName,
    avatarUrl: user.avatar_url?.trim() || undefined,
    bio: user.description?.trim() || '',
    followersCount: typeof user.followers === 'number' ? user.followers : 0,
    followingCount: typeof user.following === 'number' ? user.following : 0,
    verified: Boolean(user.verification?.verified),
    isProfileFetched: true,
  };
}

async function createServer() {
  const app = express();
  app.use(express.json());

  app.get('/api/tweet', async (req: Request, res: Response) => {
    const rawUrl = typeof req.query.url === 'string' ? req.query.url : '';
    const match = rawUrl.match(TWEET_URL_REGEX);

    if (!match) {
      res.status(400).json({ ok: false, reason: 'رابط غير صالح' });
      return;
    }

    const handle = match[1];
    const tweetId = match[2];
    const cleanUrl = `https://x.com/${handle}/status/${tweetId}`;

    const fxRaw = await fetchJsonWithTimeout<FxTweetResponse>(
      `https://api.fxtwitter.com/${handle}/status/${tweetId}`
    );
    if (fxRaw) {
      const parsed = parseFxTwitterJson(fxRaw, cleanUrl);
      if (parsed) {
        res.json({ ok: true, tweet: parsed });
        return;
      }
    }

    const vxRaw = await fetchJsonWithTimeout<VxTweetResponse>(
      `https://api.vxtwitter.com/${handle}/status/${tweetId}`
    );
    if (vxRaw) {
      const parsed = parseVxTwitterJson(vxRaw, cleanUrl);
      if (parsed) {
        res.json({ ok: true, tweet: parsed });
        return;
      }
    }

    const oEmbedRaw = await fetchJsonWithTimeout<XOEmbedResponse>(
      `https://publish.twitter.com/oembed?url=${encodeURIComponent(cleanUrl)}&omit_script=true`
    );
    if (oEmbedRaw) {
      const parsed = parseOEmbedJson(oEmbedRaw, handle, tweetId, cleanUrl);
      if (parsed) {
        res.json({ ok: true, tweet: parsed });
        return;
      }
    }

    res.status(502).json({ ok: false, reason: 'تعذر جلب تفاصيل المنشور' });
  });

  app.get('/api/account', async (req: Request, res: Response) => {
    const rawInput = typeof req.query.input === 'string' ? req.query.input : '';
    const username = extractAccountUsername(rawInput);

    if (!username) {
      res.status(400).json({ ok: false, reason: 'اسم المستخدم أو رابط الحساب غير صالح' });
      return;
    }

    const fxUser = await fetchJsonWithTimeout<FxUserResponse>(
      `https://api.fxtwitter.com/${encodeURIComponent(username)}`
    );
    if (fxUser) {
      const parsed = parseFxUserJson(fxUser, username);
      if (parsed) {
        res.json({ ok: true, account: parsed });
        return;
      }
    }

    res.status(502).json({ ok: false, reason: 'تعذر جلب بيانات الحساب حالياً' });
  });

  const isProd = process.env.NODE_ENV === 'production';

  if (!isProd) {
    const { createServer: createViteServer } = await import('vite');
    const vite = await createViteServer({
      server: { middlewareMode: true, hmr: false },
      appType: 'spa',
    });
    app.use(vite.middlewares);
  } else {
    const distPath = path.resolve(__dirname, 'dist');
    if (fs.existsSync(distPath)) {
      app.use(express.static(distPath));
      app.get('*', (_req: Request, res: Response) => {
        res.sendFile(path.join(distPath, 'index.html'));
      });
    }
  }

  app.listen(PORT, '0.0.0.0', () => {
    console.log(`X Folders server listening on http://0.0.0.0:${PORT}`);
  });
}

createServer().catch((err) => {
  console.error('Failed to start server:', err);
  process.exit(1);
});
