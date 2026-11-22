# IPTV Player — by Shakhzodjon Khudaybediyev

Private IPTV ilova: Android (Phone + TV) va Windows (EXE). Kirish uchun 6 xonali TOTP (Authenticator) kodi shart.
Kod olish: https://t.me/ilhomvich_sh

## Kanallar
| ID | Kanal | Logo |
|----|-------|------|
| 1004 | Sport TV | sport.png |
| 1010 | Futbol TV ⚽ | emoji |
| 1016 | ZoʻrTV | zor_tv.png |
| 1263 | Setanta Sports 1 | setanta.png |
| 1264 | Setanta Sports 2 | setanta.png |

URL: `https://stream2|stream8.cinerama.uz/<ID>/tracks-v1a1/mono.m3u8` (server ilovada tanlanadi).

## Logolarni qo‘shish (ixtiyoriy — bo‘lmasa emoji/bosh harf chiqadi)
- Android: `zor_tv.png`, `setanta.png`, `sport.png` → `app/src/main/res/drawable-nodpi/`
- Windows: xuddi shu 3 fayl → `desktop/assets/logos/`

## GitHub Actions
1. Loyihani GitHub'ga push qiling → **Actions** → `Build`.
2. Artifactlar: `IPTV-Player-Android` (debug + release APK), `IPTV-Player-Windows` (portable .exe).
3. TOTP kalitni koddan olib tashlash: repo → Settings → Secrets → `TOTP_SECRET` qo‘shing
   (yangi kalitni Authenticator'ga ham qo‘shing). Secret bo‘lmasa default kalit ishlaydi.

## Lokal build
- Android: `gradle assembleDebug` (JDK 17, Gradle 8.10+) yoki Android Studio.
- Windows: `cd desktop && npm install && npm start` / `npm run build`.

## Xavfsizlik eslatmasi
Kalit APK/EXE ichida bo‘lgani uchun reverse-engineering bilan ajratib olinishi mumkin. Keyingi bosqich:
backend (FastAPI) orqali tekshirish + kod hashi. Hozirgi himoya: FLAG_SECURE (skrinshot yo‘q), 5 xatodan so‘ng 60 s blok,
release'da R8 minify, Windows'da content protection.
