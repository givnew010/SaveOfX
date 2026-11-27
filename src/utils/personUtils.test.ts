import test from 'node:test';
import assert from 'node:assert/strict';
import {
  buildPersonDedupeKey,
  createPersonWorkItem,
  detectWorkTypeFromUrl,
  extractYouTubeEmbedUrl,
  normalizePersonInput,
  summarizePersonWorks,
} from './personUtils';

test('normalizePersonInput trims fields, normalizes avatarUrl, and rejects blank names', () => {
  assert.equal(normalizePersonInput({ name: '   ' }), null);

  const normalized = normalizePersonInput({
    name: '  عمر الشريف  ',
    role: ' ممثل عالمي ',
    avatarUrl: 'example.com/omar.jpg',
    bio: '  نبذة  ',
    userNote: '   ',
    tags: ' سينما,كلاسيك ',
  });

  assert.ok(normalized);
  assert.equal(normalized.name, 'عمر الشريف');
  assert.equal(normalized.role, 'ممثل عالمي');
  assert.equal(normalized.avatarUrl, 'https://example.com/omar.jpg');
  assert.equal(normalized.bio, 'نبذة');
  assert.equal(normalized.userNote, undefined);
  assert.equal(normalized.tags, 'سينما,كلاسيك');
});

test('detectWorkTypeFromUrl classifies photos, videos, and links accurately', () => {
  assert.equal(detectWorkTypeFromUrl('https://cdn.example.com/poster.png'), 'photo');
  assert.equal(detectWorkTypeFromUrl('data:image/jpeg;base64,abc123'), 'photo');
  assert.equal(detectWorkTypeFromUrl('https://www.youtube.com/watch?v=dQw4w9WgXcQ'), 'video');
  assert.equal(detectWorkTypeFromUrl('https://example.com/clip.mp4'), 'video');
  assert.equal(detectWorkTypeFromUrl('https://www.imdb.com/name/nm0001725/'), 'link');
});

test('extractYouTubeEmbedUrl converts standard, short, and shorts URLs', () => {
  assert.equal(
    extractYouTubeEmbedUrl('https://www.youtube.com/watch?v=zSWdZVtXT7E'),
    'https://www.youtube.com/embed/zSWdZVtXT7E'
  );
  assert.equal(
    extractYouTubeEmbedUrl('https://youtu.be/zSWdZVtXT7E'),
    'https://www.youtube.com/embed/zSWdZVtXT7E'
  );
  assert.equal(
    extractYouTubeEmbedUrl('https://youtube.com/shorts/abc123XYZ'),
    'https://www.youtube.com/embed/abc123XYZ'
  );
  assert.equal(extractYouTubeEmbedUrl('https://example.com/page'), null);
});

test('createPersonWorkItem & summarizePersonWorks build and count works cleanly', () => {
  assert.equal(createPersonWorkItem({ title: 'empty', url: '   ' }), null);

  const w1 = createPersonWorkItem(
    { type: 'link', title: 'IMDb', url: 'imdb.com/title/tt1375666/' },
    1000,
    'a'
  );
  const w2 = createPersonWorkItem(
    { type: 'photo', title: 'Poster', url: 'https://example.com/p.jpg' },
    2000,
    'b'
  );
  const w3 = createPersonWorkItem(
    { type: 'video', title: 'Trailer', url: 'https://youtu.be/abc' },
    3000,
    'c'
  );

  assert.ok(w1 && w2 && w3);
  assert.equal(w1.url, 'https://imdb.com/title/tt1375666/');

  const summary = summarizePersonWorks([w1, w2, w3]);
  assert.deepEqual(summary, {
    total: 3,
    links: 1,
    photos: 1,
    videos: 1,
    others: 0,
  });

  assert.equal(
    buildPersonDedupeKey({ name: ' ليوناردو ', role: ' ممثل ' }),
    'ليوناردو::ممثل'
  );
});
