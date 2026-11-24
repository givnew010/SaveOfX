import type { ParsedTweet, TweetMediaItem } from '../types/bookmark';

const URL_REGEX =
  /https?:\/\/(?:www\.|mobile\.)?(?:x\.com|twitter\.com|fxtwitter\.com|vxtwitter\.com)\/([a-zA-Z0-9_]+)\/status\/([0-9]+)(?:\S*)?/i;

const GENERIC_URL_REGEX = /https?:\/\/[\w\d:#@%/;$()~_?+\-=\\.&]+/i;

function formatTodayDate(): string {
  try {
    return new Intl.DateTimeFormat('en-GB', {
      day: 'numeric',
      month: 'short',
      year: 'numeric',
    }).format(new Date());
  } catch {
    return '';
  }
}

export const TweetParser = {
  isTweetUrl(text: string): boolean {
    return URL_REGEX.test(text);
  },

  extractHandleAndId(text: string): { handle: string; tweetId: string } | null {
    const match = text.match(URL_REGEX);
    if (!match || !match[1] || !match[2]) return null;
    return { handle: match[1], tweetId: match[2] };
  },

  extractCleanUrl(text: string): string | null {
    const match = text.match(URL_REGEX);
    if (match && match[1] && match[2]) {
      return `https://x.com/${match[1]}/status/${match[2]}`;
    }
    const genericMatch = text.match(GENERIC_URL_REGEX);
    if (genericMatch && genericMatch[0]) {
      return genericMatch[0];
    }
    return null;
  },

  parseSharedText(rawText: string): ParsedTweet {
    const cleanUrl = this.extractCleanUrl(rawText) ?? rawText.trim();
    const match = rawText.match(URL_REGEX);

    let handle = 'X_User';
    let tweetId = String(Date.now());

    if (match) {
      handle = match[1] || 'X_User';
      tweetId = match[2] || tweetId;
    }

    let snippet = rawText.replace(new RegExp(URL_REGEX.source, 'gi'), '').trim();
    if (snippet === rawText.trim()) {
      snippet = rawText.replace(new RegExp(GENERIC_URL_REGEX.source, 'gi'), '').trim();
    }
    snippet = snippet.replace(/^["'\-:]+|["'\-:]+$/g, '').trim();

    const lowerHandle = handle.toLowerCase();
    let displayName: string;
    switch (lowerHandle) {
      case 'elonmusk':
        displayName = 'Elon Musk';
        break;
      case 'androiddev':
        displayName = 'Android Developers';
        break;
      case 'x':
        displayName = 'X';
        break;
      case 'openai':
        displayName = 'OpenAI';
        break;
      case 'google':
        displayName = 'Google';
        break;
      default:
        displayName =
          handle
            .replace(/_/g, ' ')
            .split(' ')
            .filter((part) => part.trim().length > 0)
            .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
            .join(' ') || handle;
        break;
    }

    const hasRealSnippet =
      snippet.length > 0 && !snippet.startsWith('?') && !snippet.startsWith('&');
    const postContent = hasRealSnippet ? snippet : cleanUrl;

    return {
      tweetUrl: cleanUrl,
      tweetId,
      authorName: displayName,
      authorHandle: handle.startsWith('@') ? handle : `@${handle}`,
      content: postContent,
      mediaItems: [],
      postedAt: formatTodayDate(),
      likesCount: 0,
      repostsCount: 0,
      isContentFetched: hasRealSnippet,
    };
  },

  serializeMediaItems(items: TweetMediaItem[]): string | undefined {
    if (!items || items.length === 0) return undefined;
    try {
      return JSON.stringify(items);
    } catch {
      return undefined;
    }
  },

  deserializeMediaItems(json?: string): TweetMediaItem[] {
    if (!json || !json.trim()) return [];
    try {
      const parsed = JSON.parse(json);
      return Array.isArray(parsed) ? (parsed as TweetMediaItem[]) : [];
    } catch {
      return [];
    }
  },
};
