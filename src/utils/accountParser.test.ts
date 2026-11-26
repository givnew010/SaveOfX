import test from 'node:test';
import assert from 'node:assert/strict';
import { AccountParser } from './accountParser';

test('AccountParser extracts handle from @username and profile URLs', () => {
  assert.equal(AccountParser.extractUsername('@AndroidDev'), 'AndroidDev');
  assert.equal(AccountParser.extractUsername('naval'), 'naval');
  assert.equal(
    AccountParser.extractUsername('https://x.com/OpenAI?s=21&t=xyz'),
    'OpenAI'
  );
  assert.equal(
    AccountParser.extractUsername('https://mobile.twitter.com/elonmusk'),
    'elonmusk'
  );
});

test('AccountParser rejects reserved X routes and invalid handles', () => {
  assert.equal(AccountParser.extractUsername('https://x.com/explore'), null);
  assert.equal(AccountParser.extractUsername('https://x.com/home'), null);
  assert.equal(AccountParser.extractUsername('not a valid handle!'), null);
  assert.equal(AccountParser.isProfileUrl('https://x.com/AndroidDev'), true);
  assert.equal(
    AccountParser.isProfileUrl('https://x.com/AndroidDev/status/123456'),
    false
  );
});

test('AccountParser builds normalized ParsedAccount object', () => {
  const parsed = AccountParser.parseAccountInput('https://twitter.com/TechArabic');
  assert.ok(parsed);
  assert.equal(parsed.handle, '@TechArabic');
  assert.equal(parsed.profileUrl, 'https://x.com/TechArabic');
  assert.equal(parsed.displayName, 'تقنية بالعربي');
  assert.equal(parsed.isProfileFetched, false);
});
