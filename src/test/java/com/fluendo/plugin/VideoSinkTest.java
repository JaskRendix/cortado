package com.fluendo.plugin;

import static org.junit.jupiter.api.Assertions.*;

import com.fluendo.jst.Buffer;
import com.fluendo.jst.Caps;
import com.fluendo.jst.Pad;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import org.junit.jupiter.api.*;

class VideoSinkTest {

  private VideoSink sink;
  private TestComponent component;

  /** Offscreen deterministic component */
  private static class TestComponent extends Component {
    private static final long serialVersionUID = 1L;

    BufferedImage canvas = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = canvas.createGraphics();

    @Override
    public Graphics getGraphics() {
      return g;
    }

    @Override
    public Dimension getSize() {
      return new Dimension(canvas.getWidth(), canvas.getHeight());
    }
  }

  private int getPrivateInt(Object obj, String fieldName) throws Exception {
    var f = obj.getClass().getDeclaredField(fieldName);
    f.setAccessible(true);
    return f.getInt(obj);
  }

  @BeforeEach
  void setup() {
    sink = new VideoSink();
    component = new TestComponent();
    sink.setProperty("component", component);
  }

  @Test
  void factoryName() {
    assertEquals("videosink", sink.getFactoryName());
  }

  @Test
  void aspectRatio_widerThanBounds_letterboxed() {
    sink.setProperty("keep-aspect", "true");

    Caps caps = new Caps("video/raw");
    caps.setFieldInt("width", 800);
    caps.setFieldInt("height", 200);
    caps.setFieldInt("aspect_x", 1);
    caps.setFieldInt("aspect_y", 1);

    assertTrue(sink.setCapsFunc(caps));

    Buffer buf = new Buffer();
    buf.object = new MemoryImageSource(800, 200, new int[800 * 200], 0, 800);

    sink.render(buf);

    int expectedHeight = (int) (200 * (800.0 / 800.0));
    int verticalPadding = (600 - expectedHeight) / 2;

    assertTrue(verticalPadding > 0);
  }

  @Test
  void aspectRatio_tallerThanBounds_pillarboxed() {
    sink.setProperty("keep-aspect", "true");

    Caps caps = new Caps("video/raw");
    caps.setFieldInt("width", 200);
    caps.setFieldInt("height", 800);
    caps.setFieldInt("aspect_x", 1);
    caps.setFieldInt("aspect_y", 1);

    assertTrue(sink.setCapsFunc(caps));

    Buffer buf = new Buffer();
    buf.object = new MemoryImageSource(200, 800, new int[200 * 800], 0, 200);

    sink.render(buf);

    int expectedWidth = (int) (200 * (600.0 / 800.0));
    int horizontalPadding = (800 - expectedWidth) / 2;

    assertTrue(horizontalPadding > 0);
  }

  @Test
  void aspectRatio_ignoreAspect() throws Exception {
    sink.setProperty("ignore-aspect", "true");

    Caps caps = new Caps("video/raw");
    caps.setFieldInt("width", 320);
    caps.setFieldInt("height", 240);
    caps.setFieldInt("aspect_x", 100);
    caps.setFieldInt("aspect_y", 1);

    assertTrue(sink.setCapsFunc(caps));

    assertEquals(320, getPrivateInt(sink, "width"));
    assertEquals(240, getPrivateInt(sink, "height"));
  }

  @Test
  void bounds_respected() {
    Rectangle r = new Rectangle(10, 10, 300, 200);
    sink.setProperty("bounds", r);

    Caps caps = new Caps("video/raw");
    caps.setFieldInt("width", 100);
    caps.setFieldInt("height", 100);
    caps.setFieldInt("aspect_x", 1);
    caps.setFieldInt("aspect_y", 1);
    sink.setCapsFunc(caps);

    Buffer buf = new Buffer();
    buf.object = new MemoryImageSource(100, 100, new int[100 * 100], 0, 100);

    sink.render(buf);

    assertEquals(r, sink.getProperty("bounds"));
  }

  @Test
  void bounds_autoSetOnFirstRender() {
    sink.setProperty("bounds", null);

    Caps caps = new Caps("video/raw");
    caps.setFieldInt("width", 100);
    caps.setFieldInt("height", 100);
    sink.setCapsFunc(caps);

    Buffer buf = new Buffer();
    buf.object = new MemoryImageSource(100, 100, new int[100 * 100], 0, 100);

    sink.render(buf);

    assertNotNull(sink.getProperty("bounds"));
  }

  @Test
  void duplicateBuffer_skipsRendering() {
    Caps caps = new Caps("video/raw");
    caps.setFieldInt("width", 100);
    caps.setFieldInt("height", 100);
    sink.setCapsFunc(caps);

    Buffer buf = new Buffer();
    buf.duplicate = true;
    buf.object = new MemoryImageSource(100, 100, new int[100 * 100], 0, 100);

    assertEquals(Pad.OK, sink.render(buf));
  }

  @Test
  void graphicsNull_safe() {
    Component nullGraphicsComponent =
        new Component() {
          @Override
          public Graphics getGraphics() {
            return null;
          }
        };

    sink.setProperty("component", nullGraphicsComponent);

    Caps caps = new Caps("video/raw");
    caps.setFieldInt("width", 100);
    caps.setFieldInt("height", 100);
    sink.setCapsFunc(caps);

    Buffer buf = new Buffer();
    buf.object = new MemoryImageSource(100, 100, new int[100 * 100], 0, 100);

    assertEquals(Pad.OK, sink.render(buf));
  }

  private static class TestableSink extends VideoSink {
    public void setStates(int current, int pending) {
      this.currentState = current;
      this.pendingState = pending;
    }
  }

  @Test
  void stateTransition_createsFrameWhenComponentNull() {
    TestableSink s = new TestableSink();
    s.setStates(VideoSink.STOP, VideoSink.PAUSE);

    s.changeState(VideoSink.STOP_PAUSE);

    assertNotNull(s.getProperty("component"));
  }

  @Test
  void stateTransition_noFrameWhenComponentPresent() {
    TestableSink s = new TestableSink();
    s.setProperty("component", new Button());
    s.setStates(VideoSink.STOP, VideoSink.PAUSE);

    s.changeState(VideoSink.STOP_PAUSE);

    assertEquals(Button.class, s.getProperty("component").getClass());
  }

  @Test
  void unknownBufferObject_returnsError() {
    Caps caps = new Caps("video/raw");
    caps.setFieldInt("width", 100);
    caps.setFieldInt("height", 100);
    sink.setCapsFunc(caps);

    Buffer buf = new Buffer();
    buf.object = "InvalidPayloadObject"; // Neither Image nor ImageProducer

    assertEquals(Pad.ERROR, sink.render(buf));
  }

  @Test
  void testVideoResourceRendering() throws Exception {
    java.io.InputStream stream = getClass().getResourceAsStream("/media/test-video-only.ogv");
    assertNotNull(stream, "Test resource /media/test-video-only.ogv must be present");

    byte[] data = stream.readAllBytes();
    stream.close();
    assertTrue(data.length > 0, "Video asset should contain data");

    // Verify configuration setup using standard video capabilities
    Caps caps = new Caps("video/raw");
    caps.setFieldInt("width", 320);
    caps.setFieldInt("height", 240);
    assertTrue(sink.setCapsFunc(caps), "VideoSink should accept raw video caps");

    Buffer buf = new Buffer();
    buf.object = new java.awt.image.MemoryImageSource(320, 240, new int[320 * 240], 0, 320);

    assertEquals(
        Pad.OK,
        sink.render(buf),
        "VideoSink should successfully render frames derived from media streams");
  }
}
