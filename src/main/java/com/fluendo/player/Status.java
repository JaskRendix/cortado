/* Cortado - a video player java applet
 * Copyright (C) 2004 Fluendo S.L.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Street #330, Boston, MA 02111-1307, USA.
 */

package com.fluendo.player;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.MemoryImageSource;
import java.util.ArrayList;
import java.util.List;

public class Status extends Component implements MouseListener, MouseMotionListener {
  private static final long serialVersionUID = 1L;

  private int bufferPercent;
  private boolean buffering;
  private String message;
  private Rectangle bounds;
  private final Component component;
  private final Font font = new Font("SansSerif", Font.PLAIN, 10);
  private Font boldFont;
  private boolean haveAudio;
  private boolean haveSubtitles;
  private boolean havePercent;
  private boolean seekable;
  private boolean live;
  private boolean showSpeaker;
  private boolean showSubtitles;
  private boolean clearedScreen;
  private boolean ignoreBasetime;

  private static final int NONE = -1;
  private static final int BUTTON1 = 0;
  private static final int BUTTON2 = 1;
  private static final int SEEKER = 2;
  private static final int SEEKBAR = 3;
  private static final int AUDIO = 4;
  private static final int SUBTITLES = 5;

  private int clicked = NONE;
  private final Color[] colors = {
    Color.black, Color.black, Color.black, Color.black, Color.black, Color.black
  };

  private static final int SPEAKER_WIDTH = 12;
  private static final int SPEAKER_HEIGHT = 10;
  private static final int TIME_WIDTH = 38;
  private static final int SEEK_TIME_GAP = 10;
  private static final int THUMB_WIDTH = 9;

  public static final int STATE_STOPPED = 0;
  public static final int STATE_PAUSED = 1;
  public static final int STATE_PLAYING = 2;

  private int state = STATE_STOPPED;
  private double position;
  private long time;
  private double startTime;
  private double duration;
  private long byteDuration;
  private long bytePosition;

  private static final String SPEAKER_IMAGE_DATA =
      "\0\0\0\0\0\357\0\0\357U\27"
          + "\36\0\0\0\0\357\357\0\0"
          + "\0\357U\30\0\0\0\357\0\357"
          + "\0\357\0\0\357\23\357"
          + "\357\357\0\34\357\0Z\357\0"
          + "\357\\\357\0)+F\357\0\0\357"
          + "\0\357r\357Ibz\221\357"
          + "\0\0\357\0\357r\357\357\357"
          + "\276\323\357\0Z\357\0\357"
          + "\\\0\0\0\357\357\357\0"
          + "\357\0\0\357\0\0\0\0\0\357"
          + "\357\0\0\0\357\\\0\0\0"
          + "\0\0\0\357\0\0\357\\\0\0";
  private final Image speakerImg;
  private int speakerWidth;
  private int subtitlesWidth;

  private final List<StatusListener> listeners = new ArrayList<>();

  private static Image createImage(Component component, String imageData, int width, int height) {
    int[] pixels = new int[width * height];
    for (int i = 0; i < width * height; i++) {
      pixels[i] =
          0xff000000
              | (imageData.charAt(i) << 16)
              | (imageData.charAt(i) << 8)
              | imageData.charAt(i);
    }
    return component.getToolkit().createImage(new MemoryImageSource(width, height, pixels, 0, width));
  }

  public Status(Component comp) {
    component = comp;
    speakerImg = createImage(comp, SPEAKER_IMAGE_DATA, SPEAKER_WIDTH, SPEAKER_HEIGHT);
  }

  public void addStatusListener(StatusListener listener) {
    listeners.add(listener);
  }

  public void removeStatusListener(StatusListener listener) {
    listeners.remove(listener);
  }

  public void notifyNewState(int newState) {
    for (StatusListener listener : listeners) {
      listener.onState(newState);
    }
  }

  public void notifySeek(double position) {
    for (StatusListener listener : listeners) {
      listener.onSeek(position);
    }
  }

  public void notifyAudio() {
    for (StatusListener listener : listeners) {
      listener.onAudio();
    }
  }

  public void notifySubtitles(int x, int y) {
    for (StatusListener listener : listeners) {
      listener.onSubtitles(x, y);
    }
  }

  @Override
  public void update(Graphics g) {
    paint(g);
  }

  private void paintBox(Graphics g) {
    g.setColor(Color.darkGray);
    g.drawRect(0, 0, bounds.width - 1, bounds.height - 1);
    g.setColor(Color.black);
    g.fillRect(1, 1, bounds.width - 2, bounds.height - 2);
  }

  private void paintPercent(Graphics g) {
    if (havePercent) {
      g.setColor(Color.white);
      g.drawString(bufferPercent + "%", bounds.width - 26 - speakerWidth - subtitlesWidth, bounds.height - 2);
    }
  }

  private void paintButton1(Graphics g) {
    int x = 1;
    int y = 1;
    int w = bounds.height - 2;
    int h = bounds.height - 2;
    g.setColor(Color.darkGray);
    g.drawRect(x, y, w, h);
    g.setColor(colors[BUTTON1]);
    g.fillRect(x + 1, y + 1, w - 1, h - 1);
    if (state == STATE_PLAYING) {
      g.setColor(Color.white);
      if (live) {
        g.fillRect((int) (w * .4), (int) (w * .4), (int) (w * .5), (int) (w * .5));
      } else {
        g.fillRect((int) (w * .4), (int) (h * .4), (int) (w * .2), (int) (h * .5));
        g.fillRect((int) (w * .7), (int) (h * .4), (int) (w * .2), (int) (h * .5));
      }
    } else {
      int[] triangleX = {(int) (w * .4), (int) (w * .4), (int) (w * .9)};
      int[] triangleY = {(int) (w * .3), (int) (w * .9), (int) (w * .6)};
      g.setColor(Color.white);
      g.fillPolygon(triangleX, triangleY, 3);
    }
  }

  private void paintButton2(Graphics g) {
    int x = bounds.height + 1;
    int y = 1;
    int w = bounds.height - 2;
    int h = bounds.height - 2;
    g.setColor(Color.darkGray);
    g.drawRect(x, y, w, h);
    g.setColor(colors[BUTTON2]);
    g.fillRect(x + 1, y + 1, w - 1, h - 1);
    g.setColor(Color.white);
    g.fillRect(bounds.height + (int) (w * .4), (int) (w * .4), (int) (w * .5), (int) (w * .5));
  }

  private void paintMessage(Graphics g, int pos) {
    if (message != null) {
      g.setColor(Color.white);
      g.drawString(message, pos, bounds.height - 2);
    }
  }

  private void paintBuffering(Graphics g, int pos) {
    g.setColor(Color.white);
    g.drawString("Buffering", pos, bounds.height - 2);
  }

  private Rectangle getSeekBarRect() {
    return new Rectangle(
        bounds.height * 2 + 1,
        2,
        bounds.width - SEEK_TIME_GAP - TIME_WIDTH - speakerWidth - subtitlesWidth - (bounds.height * 2),
        bounds.height - 4);
  }

  private Rectangle getThumbRect() {
    Rectangle seekRect = getSeekBarRect();
    int availableWidth = seekRect.width - THUMB_WIDTH;
    int pos = (int) (availableWidth * position);
    return new Rectangle(pos + seekRect.x, 1, THUMB_WIDTH, bounds.height - 2);
  }

  private void paintSeekBar(Graphics g) {
    Rectangle sr = getSeekBarRect();
    Rectangle tr = getThumbRect();
    g.setColor(Color.darkGray);
    g.drawRect(sr.x, sr.y, sr.width, sr.height);
    g.setColor(Color.gray);
    g.fillRect(sr.x + 2, sr.y + 3, tr.x - (sr.x + 2), sr.height - 6);
    g.setColor(Color.white);
    g.drawLine(tr.x + 1, tr.y, tr.x + tr.width - 1, tr.y);
    g.drawLine(tr.x + 1, tr.y + tr.height, tr.x + tr.width - 1, tr.y + tr.height);
    g.drawLine(tr.x, tr.y + 1, tr.x, tr.y + tr.height - 1);
    g.drawLine(tr.x + tr.width, tr.y + 1, tr.x + tr.width, tr.y + tr.height - 1);
    g.setColor(colors[SEEKER]);
    g.fillRect(tr.x + 1, tr.y + 1, tr.width - 1, tr.height - 1);
  }

  private void paintTime(Graphics g) {
    long t = time;
    if (ignoreBasetime) t -= (long) startTime;
    if (t < 0) return;
    long sec = t % 60;
    long min = t / 60;
    long hour = min / 60;
    min %= 60;
    this.bounds = getBounds();
    int end = bounds.width - speakerWidth - subtitlesWidth - TIME_WIDTH;
    g.setColor(Color.white);
    g.drawString(
        hour + ":" + (min < 10 ? "0" + min : min) + ":" + (sec < 10 ? "0" + sec : sec),
        end,
        bounds.height - 2);
  }

  private void paintSpeaker(Graphics g) {
    if (haveAudio) {
      g.drawImage(
          speakerImg,
          bounds.width - SPEAKER_WIDTH - subtitlesWidth,
          bounds.height - SPEAKER_HEIGHT - 1,
          null);
    }
  }

  private Rectangle getSubtitlesBounds() {
    int x = bounds.width - subtitlesWidth + 1;
    int y = 1;
    int w = bounds.height * 3 / 2 - 2;
    int h = bounds.height - 2;
    return new Rectangle(x, y, w, h);
  }

  private void paintSubtitles(Graphics g) {
    if (haveSubtitles) {
      Rectangle sb = getSubtitlesBounds();
      int fontHeight = bounds.height - 2;
      g.setColor(Color.darkGray);
      g.drawRect(sb.x, sb.y, sb.width, sb.height);
      g.setColor(colors[SUBTITLES]);
      g.fillRect(sb.x + 1, sb.y + 1, sb.width - 1, sb.height - 1);
      if (boldFont == null) boldFont = new Font("SansSerif", Font.BOLD, fontHeight);
      g.setColor(Color.white);
      Font previousFont = g.getFont();
      g.setFont(boldFont);
      FontMetrics fm = g.getFontMetrics();
      float ccW = fm.stringWidth("CC");
      float ccH = fm.getAscent() - fm.getDescent();
      float buttonMidx = sb.x + sb.width / 2.0f;
      float buttonMidy = sb.y + sb.height / 2.0f;
      g.drawString(
          "CC", (int) (buttonMidx - ccW / 2.0f + 0.5f), (int) (buttonMidy + ccH / 2.0f + 0.5f));
      g.setFont(previousFont);
    }
  }

  @Override
  public void paint(Graphics g) {
    if (!isVisible() && clearedScreen) return;
    this.bounds = getBounds();
    if (!isVisible() && !clearedScreen) {
      g.clearRect(bounds.x, bounds.y, bounds.width, bounds.height);
      clearedScreen = true;
      return;
    }
    clearedScreen = false;
    int pos;
    Image img = component.createImage(bounds.width, bounds.height);
    if (img == null) return;
    Graphics g2 = img.getGraphics();
    if (g2 == null) return;
    g2.setFont(font);
    paintBox(g2);
    if (!buffering) {
      paintButton1(g2);
    }
    if (!live) {
      paintButton2(g2);
      pos = bounds.height * 2;
    } else {
      pos = bounds.height;
    }
    if (buffering) {
      paintPercent(g2);
      paintBuffering(g2, pos + 3);
    } else if (state == STATE_STOPPED || !seekable) {
      paintMessage(g2, pos + 3);
      paintTime(g2);
    } else if (seekable) {
      paintSeekBar(g2);
      paintTime(g2);
    }
    if (showSpeaker) {
      paintSpeaker(g2);
    }
    if (showSubtitles) {
      paintSubtitles(g2);
    }
    g.drawImage(img, bounds.x, bounds.y, null);
    img.flush();
  }

  public void setBufferPercent(boolean buffering, int bufferPercent) {
    boolean changed = this.buffering != buffering;
    changed |= this.bufferPercent != bufferPercent;
    if (changed) {
      this.buffering = buffering;
      this.bufferPercent = bufferPercent;
      component.repaint();
    }
  }

  public void setTime(double seconds) {
    if (clicked == NONE) {
      double newPosition;
      if (seconds < duration || seekable) {
        time = (long) seconds;
      } else {
        time = (long) duration;
      }
      if (duration > -1) {
        newPosition = ((double) time - startTime) / duration;
        if (newPosition != position) {
          position = newPosition;
          component.repaint();
        }
      } else {
        newPosition = ((double) bytePosition) / (double) byteDuration;
        position = newPosition;
        component.repaint();
      }
    }
  }

  public void setIgnoreBasetime(boolean ignore) {
    ignoreBasetime = ignore;
  }

  public void setStartTime(double seconds) {
    startTime = Math.max(seconds, 0);
    component.repaint();
  }

  public void setDuration(double seconds) {
    duration = seconds;
    component.repaint();
  }

  public void setByteDuration(long bytes) {
    this.byteDuration = bytes;
    if (duration == -1) {
      position = ((double) bytePosition) / (double) byteDuration;
      component.repaint();
    }
  }

  public void setBytePosition(long bytes) {
    this.bytePosition = bytes;
    if (duration == -1) {
      position = ((double) bytePosition) / (double) byteDuration;
      component.repaint();
    }
  }

  public void setMessage(String message) {
    this.message = message;
    component.repaint();
  }

  public void setHaveAudio(boolean hasAudio) {
    this.haveAudio = hasAudio;
    component.repaint();
  }

  public void setHaveSubtitles(boolean hasSubtitles) {
    this.haveSubtitles = hasSubtitles;
    subtitlesWidth =
        showSubtitles && haveSubtitles && bounds != null
            ? bounds.height * 3 / 2
            : 0;
    component.repaint();
  }

  public void setHavePercent(boolean havePercent) {
    this.havePercent = havePercent;
    component.repaint();
  }

  public void setSeekable(boolean seekable) {
    this.seekable = seekable;
    component.repaint();
  }

  public void setLive(boolean live) {
    this.live = live;
    component.repaint();
  }

  public void setShowSpeaker(boolean showSpeaker) {
    this.showSpeaker = showSpeaker;
    speakerWidth = showSpeaker ? SPEAKER_WIDTH : 0;
    component.repaint();
  }

  public void setShowSubtitles(boolean showSubtitles) {
    this.showSubtitles = showSubtitles;
    subtitlesWidth =
        showSubtitles && haveSubtitles && bounds != null
            ? bounds.height * 3 / 2
            : 0;
    component.repaint();
  }

  public void setState(int newState) {
    if (state != newState) {
      state = newState;
      component.repaint();
    }
  }

  private boolean intersectButton1(MouseEvent e) {
    if (bounds == null) return false;
    return (e.getX() >= 0 && e.getX() <= bounds.height - 2 && e.getY() > 0 && e.getY() <= bounds.height - 2);
  }

  private boolean intersectButton2(MouseEvent e) {
    if (bounds == null) return false;
    return (e.getX() >= bounds.height
        && e.getX() <= bounds.height + bounds.height - 2
        && e.getY() > 0
        && e.getY() <= bounds.height - 2);
  }

  private Rectangle getAudioBounds() {
      if (bounds == null) {
          return new Rectangle();
      }

      return new Rectangle(
          bounds.width - SPEAKER_WIDTH - subtitlesWidth,
          bounds.height - SPEAKER_HEIGHT - 1,
          SPEAKER_WIDTH,
          SPEAKER_HEIGHT);
  }

  private boolean intersectAudio(MouseEvent e) {
      return getAudioBounds().contains(e.getPoint());
  }

  private boolean intersectSeeker(MouseEvent e) {
      bounds = getBounds();
      return getThumbRect().contains(e.getPoint());
  }

  private boolean intersectSeekbar(MouseEvent e) {
      bounds = getBounds();
      return getSeekBarRect().contains(e.getPoint());
  }

  private boolean intersectSubtitles(MouseEvent e) {
      return bounds != null
          && getSubtitlesBounds().contains(e.getPoint());
  }

  private int findComponent(MouseEvent e) {
    if (!buffering && intersectButton1(e)) return BUTTON1;
    if (intersectButton2(e)) return BUTTON2;
    if (showSpeaker && haveAudio && intersectAudio(e)) return AUDIO;
    if (showSubtitles && haveSubtitles && intersectSubtitles(e)) return SUBTITLES;
    if (seekable && intersectSeeker(e)) return SEEKER;
    if (seekable && intersectSeekbar(e)) return SEEKBAR;
    return NONE;
  }

  public void cancelMouseOperation() {
    for (int n = 0; n < colors.length; ++n) {
      colors[n] = Color.black;
    }
    clicked = NONE;
  }

  @Override
  public void mouseClicked(MouseEvent e) {}

  @Override
  public void mouseEntered(MouseEvent e) {}

  @Override
  public void mouseExited(MouseEvent e) {}

  @Override
  public void mousePressed(MouseEvent e) {
    e.translatePoint(-1, -1);
    clicked = findComponent(e);
    if (clicked == SEEKBAR && state != STATE_STOPPED) {
      clicked = SEEKER;
      colors[SEEKER] = Color.gray;
      mouseDragged(e);
    }
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    e.translatePoint(-1, -1);
    int comp = findComponent(e);
    if (clicked != comp) {
      if (clicked == SEEKER) comp = clicked;
      else return;
    }

    switch (comp) {
      case BUTTON1 -> {
        if (state == STATE_PLAYING) {
          state = live ? STATE_STOPPED : STATE_PAUSED;
        } else {
          state = STATE_PLAYING;
        }
        notifyNewState(state);
      }
      case BUTTON2 -> {
        state = STATE_STOPPED;
        notifyNewState(state);
      }
      case SEEKER -> {
        if (state != STATE_STOPPED) notifySeek(position);
      }
      case SEEKBAR -> {}
      case AUDIO -> notifyAudio();
      case SUBTITLES -> notifySubtitles(e.getX(), e.getY());
      default -> {}
    }
    clicked = NONE;
    component.repaint();
  }

  @Override
  public void mouseDragged(MouseEvent e) {
    if (seekable && clicked == SEEKER) {
      e.translatePoint(-1, -1);
      Rectangle sr = getSeekBarRect();
      int availableWidth = sr.width - THUMB_WIDTH;
      int thumbLeft = e.getX() - sr.x - THUMB_WIDTH / 2;
      double newPosition = thumbLeft / (double) availableWidth;

      newPosition = Math.max(0.0, Math.min(1.0, newPosition));

      if (newPosition != position) {
        position = newPosition;
        time = (long) (startTime + duration * position);
        component.repaint();
      }
    }
  }

  private boolean testIntersection(boolean in, int idx) {
    if (in) {
      if (colors[idx] != Color.gray) {
        colors[idx] = Color.gray;
        return true;
      }
    } else {
      if (colors[idx] != Color.black) {
        colors[idx] = Color.black;
        return true;
      }
    }
    return false;
  }

  @Override
  public void mouseMoved(MouseEvent e) {
    boolean needRepaint = false;
    e.translatePoint(-1, -1);
    if (!buffering && testIntersection(intersectButton1(e), BUTTON1)) needRepaint = true;
    if (testIntersection(intersectButton2(e), BUTTON2)) needRepaint = true;
    if (seekable && testIntersection(intersectSeeker(e), SEEKER)) needRepaint = true;
    if (haveAudio && showSpeaker && testIntersection(intersectAudio(e), AUDIO)) needRepaint = true;
    if (haveSubtitles && showSubtitles && testIntersection(intersectSubtitles(e), SUBTITLES))
      needRepaint = true;
    if (needRepaint) component.repaint();
  }
}
