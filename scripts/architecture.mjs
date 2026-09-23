import { spawnSync } from 'node:child_process';
import { existsSync, mkdirSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import path from 'node:path';

// Pin the renderer so future upstream changes cannot silently alter the diagram.
const revision = '9e35d2b0b39b155553ba9fcfe0b4f2a5198dd993';
const repository = 'https://github.com/tt-a1i/archify.git';
const root = fileURLToPath(new URL('../', import.meta.url));
const cache = path.join(root, '.cache', 'archify', revision);
const source = path.join(root, 'docs/architecture/describe.architecture.json');
const output = path.join(root, 'docs/architecture/describe.html');
const mode = process.argv[2] ?? 'generate';
if (!['generate', 'validate', 'visual-check'].includes(mode)) {
  console.error('Usage: node scripts/architecture.mjs [generate|validate|visual-check]');
  process.exit(2);
}

function run(command, args, cwd = root, capture = false) {
  const result = spawnSync(command, args, {
    cwd, encoding: 'utf8', stdio: capture ? 'pipe' : 'inherit', windowsHide: true,
  });
  if (result.error) throw result.error;
  if (result.status !== 0) {
    if (capture) console.error(result.stderr);
    process.exit(result.status ?? 1);
  }
  return result.stdout?.trim();
}

if (!existsSync(path.join(cache, '.git'))) {
  mkdirSync(cache, { recursive: true });
  run('git', ['init', cache]);
}
if (!existsSync(path.join(cache, 'archify/bin/archify.mjs'))) {
  run('git', ['fetch', '--depth', '1', repository, revision], cache);
  run('git', ['checkout', '--detach', revision], cache);
}
if (run('git', ['rev-parse', 'HEAD'], cache, true) !== revision ||
    run('git', ['status', '--porcelain'], cache, true)) {
  throw new Error(`Archify cache is modified. Restore or move ${cache} before retrying.`);
}
const cli = path.join(cache, 'archify/bin/archify.mjs');
if (mode === 'visual-check') {
  run(process.execPath, [cli, 'visual-check', output, '--json']);
} else {
  run(process.execPath, [cli, 'validate', 'architecture', source,
    '--quality', 'showcase', '--repo-root', root, '--json']);
  if (mode === 'generate') {
    run(process.execPath, [cli, 'deliver', 'architecture', source, output,
      '--quality', 'showcase', '--repo-root', root, '--json']);
  }
}
