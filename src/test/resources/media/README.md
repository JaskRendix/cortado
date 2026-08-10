# Test Media Assets

This directory contains standard-compliant Ogg audio and video test files generated via FFmpeg, used for automated unit and integration testing across the Cortado media pipeline.

## Media Files Overview

* **Audio-Only Assets (`.ogg`, `.oga`)**
  * `test-audio.ogg` / `test-audio.oga`: 5-second 440Hz sine wave tone encoded with Vorbis.
  * `test-silence.ogg` / `test-silence.oga`: 5-second silent stereo audio track.

* **Video & Multiplexed Assets (`.ogv`)**
  * `test-video-audio.ogv`: 5-second multiplexed stream containing a color test pattern (Theora) and a 440Hz tone (Vorbis).
  * `test-video-only.ogv`: 5-second video-only stream featuring a color test pattern (Theora) with no audio track.
  * `test-video-silent.ogv`: 5-second stream containing SMPTE color bars paired with a silent stereo audio track.

---

## Regeneration Commands

```bash
# 1. Ensure the media directory exists
mkdir -p src/test/resources/media

# 2. Generate Audio Assets (.ogg / .oga)
ffmpeg -y -f lavfi -i "sine=frequency=440:duration=5" src/test/resources/media/test-audio.ogg
ffmpeg -y -f lavfi -i "anullsrc=r=44100:cl=stereo" -t 5 src/test/resources/media/test-silence.ogg
ffmpeg -y -f lavfi -i "sine=frequency=440:duration=5" -c:a libvorbis src/test/resources/media/test-audio.oga
ffmpeg -y -f lavfi -i "anullsrc=r=44100:cl=stereo" -t 5 -c:a libvorbis src/test/resources/media/test-silence.oga

# 3. Generate Video Assets (.ogv)
ffmpeg -y -f lavfi -i "testsrc=duration=5:size=640x480:rate=30" -f lavfi -i "sine=frequency=440:duration=5" -c:v libtheora -qscale:v 7 -c:a libvorbis -qscale:a 5 src/test/resources/media/test-video-audio.ogv
ffmpeg -y -f lavfi -i "testsrc=duration=5:size=640x480:rate=30" -c:v libtheora -qscale:v 7 src/test/resources/media/test-video-only.ogv
ffmpeg -y -f lavfi -i "smptebars=duration=5:size=640x480:rate=30" -f lavfi -i "anullsrc=r=44100:cl=stereo" -t 5 -c:v libtheora -qscale:v 7 -c:a libvorbis src/test/resources/media/test-video-silent.ogv
```
