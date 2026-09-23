# Interactive architecture

Download [describe.html](describe.html) and open it in a browser. GitHub's file
viewer displays HTML source rather than running the diagram. No server, API key,
database, or Archify installation is needed to explore the downloaded file.

Use the three request steps to follow a simple `GET /describe` request. Select
components with a click or Enter/Space to read their full details and source links.
The minimal viewer uses 18px component names, 14px secondary/connection labels,
and 16px interface text, with a canvas that never shrinks below 1200px. Narrow
screens scroll the diagram horizontally rather than shrinking its text. Zoom,
light/dark themes, and SVG export are available without menus or floating panels.

The **Runthrough** player provides a 40-second animated walkthrough of a successful
request, including the response back to the client. **Play** starts it, **Pause**
holds the current position, **Resume** continues, and **Stop** resets to the
overview. At the end, **Replay** starts again. Playback pauses when the page is
hidden or a component is selected; selecting a request tab stops it. Reduced-motion
preferences disable the travelling marker while retaining step highlights and
captions. This is an offline diagram animation, not a live API call or a video file.

```bash
curl http://localhost:8081/describe
```

The application must be running for this curl command (see the root README).
The diagram itself is documentation and does not send requests or call OpenAI.
It shows the successful retrieval path. Numbered arrows represent calls;
responses return along the call chain. The cards explain startup ingestion and
the HTTP 200 fallback when no chunks match. Source links are pinned to the
`master` commit from which this feature was created.

## Regenerate

Requires Node.js 18+ and Git. From the repository root:

```bash
node scripts/architecture.mjs generate
node scripts/architecture.mjs validate
node --test scripts/architecture-playback.test.mjs
```

The first command downloads the official [Archify](https://github.com/tt-a1i/archify)
renderer into the ignored `.cache/archify` directory at commit
`9e35d2b0b39b155553ba9fcfe0b4f2a5198dd993` (MIT license).
The initial download needs network access; later runs use that verified checkout.
There are no npm dependencies or additions to the Java runtime.

Edit [describe.architecture.json](describe.architecture.json), then regenerate
and commit both the JSON and HTML. Update `meta.repository.revision` when mapping
a newer source revision. Generation validates repository evidence and all nine
Archify showcase checks on the source graph. The presentation layer in
`scripts/architecture-viewer.mjs` and `scripts/architecture-viewer.html` then
enlarges the SVG labels and replaces the default viewer. The resulting HTML
passes Archify's nine structural checks before it replaces the previous output.
The final JSON receipt reports the SHA-256 of the minimal HTML, separately from
Archify's upstream artifact receipt. Both files are standalone and need no fonts
or scripts from a CDN.

For upstream debugging only, `node scripts/architecture.mjs upstream-visual-check`
checks the unmodified Archify viewer in `.cache/archify/describe.raw.html` after
generation. It requires Chrome/Chromium. Its results do not validate the custom
viewer. Review `docs/architecture/describe.html` separately in a browser: verify
all labels, the three request steps, component selection using mouse and keyboard,
zoom/reset, both themes, and the exported SVG. Vertical page scrolling is
intentional so text and details remain readable.
