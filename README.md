# frameX TV v2

GPLv3 Android TV client (Nuvio TV fork) with frameX branding, catalogue/streams, Select Server, and optional Atlantic embed playback.

Upstream credit: [NuvioMedia/NuvioTV](https://github.com/NuvioMedia/NuvioTV) (GPLv3). See `LICENSE` + `NOTICE`.

## Download

Get the latest APK from **[Releases](https://github.com/ajlesaca8/framex-v2/releases)**.

| APK | Use on |
|---|---|
| `frameX-v2-*-armeabi-v7a.apk` | Most Android TVs / Sony BRAVIA (32-bit) |
| `frameX-v2-*-arm64-v8a.apk` | 64-bit Android TV / devices |
| `frameX-v2-*-universal.apk` | Works on all ABIs (larger) |

Package ID: `com.framextv.v2` · Label: **frameX**

## Build

```bash
# Windows: set JAVA_HOME to Android Studio JBR
./gradlew :app:assembleFullRelease
```

APKs land in `app/build/outputs/apk/full/release/`.

## Notes

- First-run QR / experience / layout intro is skipped (opens on Home).
- Atlantic embed includes popup + captcha auto-dismiss guards.
- In-app updates / plugins / custom addon servers disabled by default.
