import type { ParsedAccount } from '../types/bookmark';
import { AccountParser } from './accountParser';

export type AccountFetchResult =
  | { status: 'success'; account: ParsedAccount }
  | { status: 'offline'; fallbackAccount: ParsedAccount }
  | { status: 'failed'; fallbackAccount: ParsedAccount | null; reason: string };

export const AccountFetcher = {
  isNetworkAvailable(): boolean {
    if (typeof navigator === 'undefined') return true;
    return navigator.onLine !== false;
  },

  async fetchAccount(rawInput: string): Promise<AccountFetchResult> {
    const fallback = AccountParser.parseAccountInput(rawInput);
    if (!fallback) {
      return {
        status: 'failed',
        fallbackAccount: null,
        reason: 'اسم المستخدم أو رابط الحساب غير صالح',
      };
    }

    if (!this.isNetworkAvailable()) {
      return {
        status: 'offline',
        fallbackAccount: fallback,
      };
    }

    try {
      const response = await fetch(
        `/api/account?input=${encodeURIComponent(fallback.profileUrl)}`
      );
      const data = (await response.json()) as {
        ok?: boolean;
        account?: ParsedAccount;
        reason?: string;
      };

      if (response.ok && data.ok && data.account) {
        return {
          status: 'success',
          account: data.account,
        };
      }

      return {
        status: 'failed',
        fallbackAccount: fallback,
        reason: data.reason || 'تعذر جلب بيانات الحساب حالياً',
      };
    } catch {
      return {
        status: 'failed',
        fallbackAccount: fallback,
        reason: 'تعذر جلب بيانات الحساب حالياً',
      };
    }
  },
};
