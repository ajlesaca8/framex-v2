# frameX TV v2 — Handoff

GPLv3 fork of Nuvio TV with frameX branding and frameX catalogue/stream APIs.
Sibling `framex-v1` is untouched.

## Standing rules

- Keep `LICENSE` + `NOTICE`. Credit upstream Nuvio TV (GPLv3).
- Do not phone home to Nuvio GitHub releases.
- Catalogue + streams: frameX hosts only (`FrameXHosts` / `/api/home`, `/api/details`, `/api/stream`).
- Plugins / in-app updates / custom addon servers disabled by default (`AppFeaturePolicy` + flavor flags).
- No BRAVIA install without explicit approval.

## GitHub

- Repo: https://github.com/ajlesaca8/framex-v2  
- Release APKs: https://github.com/ajlesaca8/framex-v2/releases/tag/v1.0.0  
- BRAVIA download: `frameX-v2-1.0.0-armeabi-v7a.apk`

## Package

| Variant | applicationId |
|---|---|
| fullDebug | `com.framextv.v2.debug` |
| fullRelease | `com.framextv.v2` |

Build debug: `./gradlew :app:assembleFullDebug`  
Build production: `./gradlew :app:assembleFullRelease`  
(set `JAVA_HOME` to Android Studio JBR on Windows).

Release APKs: `app/build/outputs/apk/full/release/`  
- BRAVIA UR3 → `app-full-armeabi-v7a-release.apk` (`com.framextv.v2`)  
- Emulator x86_64 → `app-full-x86_64-release.apk`  
Launcher label: **frameX** (no Debug). Signed with debug keystore fallback until a frameX release keystore is configured.

## 16. Change log (append-only)

### 2026-11-10 — Production fullRelease

- Kept Select Server + Atlantic embed WebView build; switched shipping target from debug → **fullRelease**.
- `versionCode` **1063**, `versionName` **1.0.0**, `applicationId` **`com.framextv.v2`** (not `.debug`).
- Uninstalled `com.framextv.v2.debug` from BRAVIA + emulator; installed release APKs and launched `com.framextv.v2/.MainActivity`.

### 2026-11-10 — Launcher icon fix

- Replaced leftover Nuvio play-button `mipmap/*/ic_launcher*.png` (+ `drawable/ic_launcher.png`) with centered frameX mark (theme tints preserved).
- Adaptive icon: transparent orange-X foreground + black background + monochrome layer (fixes off-center vector + Material You invert).
- Reinstalled fullRelease on emulator + BRAVIA; app drawer shows **frameX** with amber X on black.

### 2026-11-10 — Skip first-run intro

- Default `hasSeenAuthQrOnFirstLaunch` → true (QR / account intro skipped; Settings sign-in still available).
- Default experience mode → **ADVANCED**; `hasChosenLayout` → true (MODERN).
- Startup `startDestination` always **Home** (no experience / layout welcome screens).

### 2026-11-10 — Embed captcha harden

- BRAVIA Atlantic play showed in-page **“Confirm you're not a robot”** QR modal (not a window.open popup).
- Pass 1: MutationObserver strip + captcha host block (still visible — likely cross-origin iframe).
- Pass 2: block non-allowlisted HTML document loads; remove large foreign iframes — **broke playback** (play icon, no stream).
- Pass 3: reverted HTML/size iframe nuke; keep ad-host block + same-origin captcha strip + **auto-click play** for TV WebView.
- Pass 4: **auto-close** robot/QR modal — click X via `elementFromPoint` top-right, then remove card; if video already playing, remove covering foreign iframes.
- Confirmed working on BRAVIA (play + captcha auto-dismiss).


### 2026-11-09 — Initial scaffold + frameX data

- Copied Nuvio TV source → `framex-v2/` (GPLv3). Restored `core/build` packages skipped by robocopy `/XD build`.
- Renamed package / applicationId to `com.framextv.v2`; debug signing; DoVi native off in `local.properties`.
- Rebrand: `app_name` frameX, launcher adaptive icons from v1 mark, user-facing Nuvio→frameX strings; licenses still credit Nuvio upstream.
- Disabled in-app updates / plugins / custom servers; empty `GITHUB_OWNER`/`GITHUB_REPO`.
- FrameX data bridge: `data/framex/*` + builtin addon; Catalog/Meta/Stream repos short-circuit to frameX APIs.
- Emulator smoke (`emulator-5554`): Home rails (Trending/Popular), Details (Project Hail Mary), stream picker shows frameX 1080p sources. Caps in `qa-shots/09–11`.
- BRAVIA install: **not done** — waiting for explicit approval.

### 2026-11-09 — High branding pass

- Launcher `app_name`: debug + all locales (incl. `values-hu/string.xml`) → frameX / frameX Debug.
- Wordmarks / login / splash / TV banners regenerated with frameX mark; `assets/nuviotv.png` + `nuvio_text.png` replaced.
- User-facing string sweep EN + 34 locales; `licenses_attributions_nuvio_title` kept as **Nuvio TV**; body credits `NuvioMedia/NuvioTV`.
- Verify: `aapt` `application-label:'frameX Debug'` / `application-label-hu:'frameX'`; emulator splash + home in `qa-shots/14–15`. ANR once on cold install (dialog title showed frameX Debug).
- BRAVIA install: **done** — `armeabi-v7a` debug APK as `com.framextv.v2.debug` (UR3 is 32-bit; arm64 APK failed `NO_MATCHING_ABIS`). Launched MainActivity.

### 2026-11-10 — Atlantic embed API (candidate source)

- Docs: https://atlantic.bz/embed-api
- Public surface is **iframe embed only** (not FrameX-style JSON streams):
  - Movie: `https://atlantic.st/embed/movie/{tmdbId}`
  - TV: `https://atlantic.st/embed/tv/{tmdbId}/{season}/{episode}`
- IDs are **TMDB numeric** (not IMDb `tt…`). Free, no key.
- Backend hosts exist (`api.atlantic.st` / `.bz`) but no documented `/api/stream` source list for native players.
- Candidate use: optional WebView embed fallback — evaluate separately from FrameX HLS sources.
- Feasibility note (2026-11-10): BRAVIA + emulator have System WebView.
- **Implemented as optional Select Server row** (not a FrameX replacement):
  - First row **Atlantic** (Embed tag) → resolves TMDB id → `EmbedWebViewScreen` loads atlantic.st embed.
  - Remaining rows stay FrameX `/api/providers` → native ExoPlayer/libmpv.
  - Embed WebView popup hardening: block `window.open`/new windows, block top-level off-site redirects, intercept known ad hosts (incl. `justice.hls.lol` / Monetag-style).

### 2026-11-10 — Pre-play Select Server (website-style)

- Stream route now opens `FrameXServerSelectScreen` instead of Nuvio stream list.
- Loads `GET /api/providers` (Available / Offline + FHD/4K/Multi-Lang tags from taglines).
- Choosing an available server calls `GET /api/stream?...&provider=<id>` then launches the player.
- Installed on emulator (`x86_64`) and BRAVIA (`armeabi-v7a`).
