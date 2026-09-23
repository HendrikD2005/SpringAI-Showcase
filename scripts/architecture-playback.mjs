// Clock and frame scheduling are injectable so pause/resume can be tested
// without a browser or real-time waits.
export function createPlayback({ steps, duration = 4000, onUpdate,
  now = () => performance.now(), request = callback => requestAnimationFrame(callback),
  cancel = handle => cancelAnimationFrame(handle) }) {
  const total = steps * duration;
  let state = 'stopped', elapsed = 0, startedAt = 0, handle = null;
  function emit() {
    onUpdate({ state, elapsed, total, index: Math.min(steps - 1, Math.floor(elapsed / duration)),
      progress: elapsed === total ? 1 : (elapsed % duration) / duration });
  }
  function unschedule() { if (handle !== null) cancel(handle); handle = null; }
  function updateTime() { elapsed = Math.min(total, Math.max(0, now() - startedAt)); }
  function tick() {
    handle = null;
    if (state !== 'playing') return;
    updateTime();
    if (elapsed === total) state = 'ended';
    emit();
    if (state === 'playing') handle = request(tick);
  }
  return {
    play() {
      if (state === 'playing') return;
      if (state === 'ended') elapsed = 0;
      state = 'playing'; startedAt = now() - elapsed;
      emit(); handle = request(tick);
    },
    pause() {
      if (state !== 'playing') return;
      unschedule(); updateTime(); state = elapsed === total ? 'ended' : 'paused'; emit();
    },
    stop() { unschedule(); elapsed = 0; state = 'stopped'; emit(); },
  };
}
