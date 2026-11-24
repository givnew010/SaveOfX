import type { ParsedTweet } from '../types/bookmark';
import { TweetParser } from './tweetParser';

export type TweetFetchResult =
  | { status: 'success'; tweet: ParsedTweet }
  | { status: 'offline'; fallbackTweet: ParsedTweet }
  | { status: 'failed'; fallbackTweet: ParsedTweet; reason: string };

export const TweetFetcher = {
  isNetworkAvailable(): boolean {
    if (typeof navigator === 'undefined') return true;
    return navigator.onLine !== false;
  },

  async fetchTweet(rawUrlOrText: string): Promise<TweetFetchResult> {
    const fallback = TweetParser.parseSharedText(rawUrlOrText);
    const handleAndId = TweetParser.extractHandleAndId(rawUrlOrText);

    if (!handleAndId) {
      return {
        status: 'failed',
        fallbackTweet: fallback,
        reason: 'رابط غير صالح',
      };
    }

    if (!this.isNetworkAvailable()) {
      return {
        status: 'offline',
        fallbackTweet: {
          ...fallback,
          content: fallback.tweetUrl,
          isContentFetched: false,
        },
      };
    }

    try {
      const response = await fetch(
        `/api/tweet?url=${encodeURIComponent(fallback.tweetUrl)}`
      );
      const data = (await response.json()) as {
        ok?: boolean;
        tweet?: ParsedTweet;
        reason?: string;
      };

      if (response.ok && data.ok && data.tweet) {
        return {
          status: 'success',
          tweet: data.tweet,
        };
      }

      return {
        status: 'failed',
        fallbackTweet: {
          ...fallback,
          content: fallback.tweetUrl,
          isContentFetched: false,
        },
        reason: data.reason || 'تعذر جلب تفاصيل المنشور',
      };
    } catch {
      return {
        status: 'failed',
        fallbackTweet: {
          ...fallback,
          content: fallback.tweetUrl,
          isContentFetched: false,
        },
        reason: 'تعذر جلب تفاصيل المنشور',
      };
    }
  },
};
