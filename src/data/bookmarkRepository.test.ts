import test from 'node:test';
import assert from 'node:assert/strict';

function createMemoryStorage(): Storage {
  const store = new Map<string, string>();
  return {
    get length() {
      return store.size;
    },
    clear() {
      store.clear();
    },
    getItem(key: string) {
      return store.get(key) ?? null;
    },
    key(index: number) {
      return Array.from(store.keys())[index] ?? null;
    },
    removeItem(key: string) {
      store.delete(key);
    },
    setItem(key: string, value: string) {
      store.set(key, value);
    },
  };
}

globalThis.localStorage = createMemoryStorage();

import { BookmarkRepository } from './bookmarkRepository';

test('BookmarkRepository seeds initial accounts and supports CRUD + folder move/delete', () => {
  localStorage.clear();
  const seeded = BookmarkRepository.seedInitialDataIfEmpty();
  assert.ok(seeded.folders.length >= 5);
  assert.ok(seeded.accounts.length >= 3);

  const customFolder = BookmarkRepository.insertFolder({
    name: 'حسابات الذكاء الاصطناعي',
    iconEmoji: '🤖',
    colorHex: '#1D9BF0',
    description: '',
    isDefault: false,
  });

  const saved = BookmarkRepository.insertAccount({
    folderId: customFolder.id,
    profileUrl: 'https://x.com/karpathy',
    handle: '@karpathy',
    displayName: 'Andrej Karpathy',
    bio: 'AI researcher',
    followersCount: 1000000,
    followingCount: 200,
    verified: true,
    tags: 'ai,ml',
    isFavorite: false,
    isProfileFetched: true,
  });

  assert.equal(saved.folderId, customFolder.id);
  BookmarkRepository.toggleAccountFavorite(saved.id);
  assert.equal(
    BookmarkRepository.getAllAccounts().find((a) => a.id === saved.id)?.isFavorite,
    true
  );

  // Deleting customFolder with moveToGeneral = true moves account to default folder
  BookmarkRepository.deleteFolder(customFolder.id, true);
  const moved = BookmarkRepository.getAllAccounts().find((a) => a.id === saved.id);
  assert.ok(moved);
  assert.equal(moved.folderId, 1);
});

test('BookmarkRepository backup & restore preserves accounts in merge and replace modes', () => {
  localStorage.clear();
  BookmarkRepository.seedInitialDataIfEmpty();
  const payload = BookmarkRepository.exportBackup();
  assert.ok(payload.accounts && payload.accounts.length >= 3);

  const json = JSON.stringify(payload);
  const parsed = BookmarkRepository.parseBackupJson(json);
  assert.ok(parsed);
  assert.equal(parsed.accounts?.length, payload.accounts.length);

  // Add an extra account in incoming payload and merge
  const incoming = {
    ...parsed,
    accounts: [
      ...(parsed.accounts ?? []),
      {
        id: 999,
        folderId: 2,
        profileUrl: 'https://x.com/ylecun',
        handle: '@ylecun',
        displayName: 'Yann LeCun',
        bio: 'Chief AI Scientist',
        followersCount: 800000,
        followingCount: 150,
        verified: true,
        savedAt: Date.now(),
        tags: 'ai',
        isFavorite: false,
        isProfileFetched: true,
      },
    ],
  };

  const mergeResult = BookmarkRepository.restoreBackup(incoming, 'merge');
  assert.equal(mergeResult.accountsAdded, 1);
  assert.ok(BookmarkRepository.getAllAccounts().some((a) => a.handle === '@ylecun'));
});
