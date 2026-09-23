# Interactive architecture

Download [describe.html](describe.html) and open it in a browser. GitHub's file
viewer displays HTML source rather than running the diagram. No server, API key,
database, or Archify installation is needed to explore the downloaded file.

Use the three guided views to follow a simple `GET /describe` request. Click
components to inspect their source links; use search, zoom, relationship tracing,
theme switching, and the export menu to explore the architecture.

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
node scripts/architecture.mjs visual-check
```

The first command downloads the official [Archify](https://github.com/tt-a1i/archify)
renderer into the ignored `.cache/archify` directory at commit
`9e35d2b0b39b155553ba9fcfe0b4f2a5198dd993` (MIT license).
The initial download needs network access; later runs use that verified checkout.
There are no npm dependencies or additions to the Java runtime.

Edit [describe.architecture.json](describe.architecture.json), then regenerate
and commit both the JSON and HTML. Update `meta.repository.revision` when mapping
a newer source revision. Generation validates repository evidence and all nine
Archify showcase checks before atomically replacing the HTML. JSON receipts
printed by the renderer contain the specification and artifact SHA-256 hashes.

`visual-check` additionally requires Chrome/Chromium and writes browser evidence
sidecars beside the HTML. These local review files are ignored. Automated browser
measurements do not replace visual inspection of the light and dark screenshots.
