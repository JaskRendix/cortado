package com.fluendo.plugin;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class QueueTest {

  private Queue queue;

  @BeforeEach
  public void setUp() {
    queue = new Queue();
  }

  @Test
  public void testFactoryName() {
    assertEquals("queue", queue.getFactoryName(), "Factory name should match 'queue'");
  }

  @Test
  public void testDefaultProperties() {
    assertEquals(100, queue.getProperty("maxBuffers"));
    assertEquals(-1, queue.getProperty("maxSize"));
    assertEquals("false", queue.getProperty("isBuffer"));
    assertEquals(10, queue.getProperty("lowPercent"));
    assertEquals(70, queue.getProperty("highPercent"));
    assertEquals(Queue.NO_LEAK, queue.getProperty("leaky"));
    assertNull(queue.getProperty("nonExistentProperty"));
  }

  @Test
  public void testSetAndGetProperties() {
    assertTrue(queue.setProperty("maxBuffers", 50));
    assertTrue(queue.setProperty("maxSize", 1024));
    assertTrue(queue.setProperty("isBuffer", "true"));
    assertTrue(queue.setProperty("lowPercent", 20));
    assertTrue(queue.setProperty("highPercent", 80));
    assertTrue(queue.setProperty("leaky", Queue.LEAK_UPSTREAM));
    assertFalse(queue.setProperty("invalidProp", 999));

    assertEquals(50, queue.getProperty("maxBuffers"));
    assertEquals(1024, queue.getProperty("maxSize"));
    assertEquals("true", queue.getProperty("isBuffer"));
    assertEquals(20, queue.getProperty("lowPercent"));
    assertEquals(80, queue.getProperty("highPercent"));
    assertEquals(Queue.LEAK_UPSTREAM, queue.getProperty("leaky"));
  }

  @Test
  public void testLeakyConstants() {
    assertEquals(0, Queue.NO_LEAK);
    assertEquals(1, Queue.LEAK_UPSTREAM);
    assertEquals(2, Queue.LEAK_DOWNSTREAM);
  }
}
