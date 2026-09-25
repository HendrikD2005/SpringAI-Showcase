import { test } from 'node:test';
import assert from 'node:assert/strict';
import { createPlayback } from './architecture-playback.mjs';

function harness() {
  let time = 0, nextId = 0, snapshot;
  const pending = new Map();
  const player = createPlayback({ steps: 10, duration: 4000,
    now: () => time, request: callback => { pending.set(++nextId, callback); return nextId; },
    cancel: id => pending.delete(id), onUpdate: value => { snapshot = value; },
  });
  return { player, pending, get snapshot() { return snapshot; },
    advance(ms) { time += ms; const callbacks = [...pending.values()]; pending.clear(); callbacks.forEach(callback => callback()); },
  };
}

test('pause freezes position and resume excludes time spent paused', () => {
  const h = harness();
  h.player.play(); h.advance(5500); h.player.pause();
  assert.equal(h.snapshot.index, 1);
  assert.equal(h.snapshot.progress, .375);
  assert.equal(h.pending.size, 0);
  h.advance(20000);
  assert.equal(h.snapshot.elapsed, 5500);
  h.player.play(); h.advance(500);
  assert.equal(h.snapshot.elapsed, 6000);
  assert.equal(h.snapshot.state, 'playing');
});

test('stop cancels playback and the next play starts from the beginning', () => {
  const h = harness();
  h.player.play(); h.player.play();
  assert.equal(h.pending.size, 1);
  h.advance(7000); h.player.stop();
  assert.equal(h.snapshot.elapsed, 0);
  assert.equal(h.snapshot.state, 'stopped');
  assert.equal(h.pending.size, 0);
  h.advance(10000); h.player.play();
  assert.equal(h.snapshot.index, 0);
  assert.equal(h.snapshot.elapsed, 0);
});

test('completion stops scheduling and replay restarts the full runthrough', () => {
  const h = harness();
  h.player.play(); h.advance(45000);
  assert.equal(h.snapshot.state, 'ended');
  assert.equal(h.snapshot.index, 9);
  assert.equal(h.snapshot.progress, 1);
  assert.equal(h.snapshot.elapsed, 40000);
  assert.equal(h.pending.size, 0);
  h.player.play();
  assert.equal(h.snapshot.index, 0);
  assert.equal(h.snapshot.elapsed, 0);
  h.advance(4000);
  assert.equal(h.snapshot.index, 1);
});
