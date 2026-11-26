import type { ParsedAccount } from '../types/bookmark';

const PROFILE_URL_REGEX =
  /https?:\/\/(?:www\.|mobile\.)?(?:x\.com|twitter\.com|fxtwitter\.com|vxtwitter\.com)\/([a-zA-Z0-9_]{1,15})(?:[/?#]\S*)?$/i;

const ANY_X_STATUS_OR_PROFILE_REGEX =
  /https?:\/\/(?:www\.|mobile\.)?(?:x\.com|twitter\.com|fxtwitter\.com|vxtwitter\.com)\/([a-zA-Z0-9_]{1,15})(?:[/?#]\S*)?/i;

const HANDLE_REGEX = /^@?([a-zA-Z0-9_]{1,15})$/;

const RESERVED_PATHS = new Set([
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

export function inferDisplayNameFromHandle(username: string): string {
  switch (username.toLowerCase()) {
    case 'elonmusk':
      return 'Elon Musk';
    case 'androiddev':
      return 'Android Developers';
    case 'x':
      return 'X';
    case 'openai':
      return 'OpenAI';
    case 'google':
      return 'Google';
    case 'naval':
      return 'Naval';
    case 'techarabic':
      return 'تقنية بالعربي';
    default:
      return (
        username
          .replace(/_/g, ' ')
          .split(' ')
          .filter((part) => part.trim().length > 0)
          .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
          .join(' ') || username
      );
  }
}

export const AccountParser = {
  extractUsername(input: string): string | null {
    const trimmed = input.trim();
    if (!trimmed) return null;

    const handleMatch = trimmed.match(HANDLE_REGEX);
    if (handleMatch && handleMatch[1]) {
      const candidate = handleMatch[1];
      if (!RESERVED_PATHS.has(candidate.toLowerCase())) {
        return candidate;
      }
      return null;
    }

    const urlMatch = trimmed.match(ANY_X_STATUS_OR_PROFILE_REGEX);
    if (urlMatch && urlMatch[1]) {
      const candidate = urlMatch[1];
      if (!RESERVED_PATHS.has(candidate.toLowerCase())) {
        return candidate;
      }
    }

    return null;
  },

  isValidAccountInput(input: string): boolean {
    return this.extractUsername(input) !== null;
  },

  isProfileUrl(input: string): boolean {
    const trimmed = input.trim();
    if (/\/status\/[0-9]+/i.test(trimmed)) return false;
    const match = trimmed.match(PROFILE_URL_REGEX);
    if (!match || !match[1]) return false;
    return !RESERVED_PATHS.has(match[1].toLowerCase());
  },

  normalizeProfileUrl(username: string): string {
    return `https://x.com/${username}`;
  },

  parseAccountInput(rawInput: string): ParsedAccount | null {
    const username = this.extractUsername(rawInput);
    if (!username) return null;

    return {
      profileUrl: this.normalizeProfileUrl(username),
      handle: `@${username}`,
      username,
      displayName: inferDisplayNameFromHandle(username),
      bio: '',
      followersCount: 0,
      followingCount: 0,
      verified: false,
      isProfileFetched: false,
    };
  },
};
