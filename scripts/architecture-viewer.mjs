import { readFileSync } from 'node:fs';

const escape = (value) => String(value).replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));

// GitHub READMEs can display SVG images but cannot execute the HTML player.
export function createReadmePreview(html) {
  const svg = html.match(/<svg\b[\s\S]*?<\/svg>/)?.[0];
  if (!svg) throw new Error('The interactive viewer did not contain an SVG.');
  const style = '<style>text{font-family:Arial,Helvetica,sans-serif;fill:#202822}.t-muted{fill:#505a53}.c-mask{fill:#fff}[data-node-id]>rect:not(.c-mask){fill:#fff;stroke:#89958b}path[data-edge-id]{stroke:#89958b;fill:none}marker polygon{fill:#505a53}</style>';
  return '<?xml version="1.0" encoding="UTF-8"?>\n' + svg
    .replace('<svg ', '<svg xmlns="http://www.w3.org/2000/svg" width="1200" height="650" ')
    .replace('role="group"', 'role="img"')
    .replace(/ tabindex="0"| role="button"| aria-pressed="false"/g, '')
    .replace(/(<svg\b[^>]*>)/, `$1${style}<rect width="1200" height="650" fill="#fff"/>`) + '\n';
}

// Archify owns graph layout and routing. This small presentation layer replaces
// its dense default controls, without changing the graph's nodes or connections.
export function createViewer(rendered, specification) {
  let svg = rendered.match(/<svg\b[\s\S]*?<\/svg>/)?.[0];
  if (!svg) throw new Error('Archify did not produce an SVG.');
  svg = svg.replace(/<!-- Legend -->[\s\S]*?<\/svg>/, '</svg>')
    .replace(/<g\b[^>]*data-semantic-sigil=[\s\S]*?<\/g>/g, '')
    .replace(/<rect[^>]*fill="url\(#grid\)"[^>]*\/>/g, '')
    .replace('role="img"', 'role="group"')
    .replace('data-quality-profile="showcase"', 'data-quality-profile="showcase" id="architecture"');
  // Set real SVG font sizes, so exported text is as legible as the live viewer.
  svg = svg.replace(/<text\b[^>]*>[\s\S]*?<\/text>/g, text => {
    const isTitle = text.includes('data-node-label');
    text = text.replace(/font-size="[^"]+"/, `font-size="${isTitle ? 18 : 14}"`);
    if (text.includes('class="t-muted"') && text.includes('data-detail="context"')) {
      text = text.replace(/y="([\d.]+)"/, (_, y) => `y="${Number(y) + 6}"`);
    }
    return text;
  });
  // Expand edge-label backgrounds together with the text; never mask a node.
  svg = svg.replace(/<g\b[^>]*data-edge-from=[\s\S]*?<\/g>/g, group => {
    const label = group.match(/<text[^>]*x="([\d.]+)" y="([\d.]+)"[^>]*>(.*?)<\/text>/);
    if (!label) return group;
    const [, x, y, text] = label;
    const width = text.length * 7.5 + 16;
    return group.replace(/<rect\b[^>]*\/>/, `<rect x="${Number(x) - width / 2}" y="${Number(y) - 15}" width="${width}" height="22" rx="2" class="c-mask"/>`);
  });
  const template = readFileSync(new URL('./architecture-viewer.html', import.meta.url), 'utf8');
  const data = JSON.stringify(specification).replace(/</g, '\\u003c');
  const license = readFileSync(new URL('../docs/architecture/ARCHIFY-LICENSE.txt', import.meta.url), 'utf8');
  svg = svg.replace(/(<svg\b[^>]*>)/, `$1\n<metadata>${escape(license)}</metadata>`);
  return template.replace('<!-- GRAPH -->', svg)
    .replace('/* PLAYBACK */', readFileSync(new URL('./architecture-playback.mjs', import.meta.url), 'utf8').replace('export function', 'function'))
    .replace('/* SPECIFICATION */', data)
    .replace('<!-- LICENSE -->', `<!--\n${license.replace(/-->/g, '-- >')}\n-->`)
    .replace('<!-- NOTES -->', specification.cards.map(card => `<section><h2>${escape(card.title)}</h2>${card.items.map(item => `<p>${escape(item)}</p>`).join('')}</section>`).join('\n'))
    .replace(/[ \t]+$/gm, '');
}
