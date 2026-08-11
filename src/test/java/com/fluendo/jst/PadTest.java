package com.fluendo.jst;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class PadTest {

    private static class TestPad extends Pad {
        AtomicInteger chainCalls = new AtomicInteger();
        AtomicInteger eventCalls = new AtomicInteger();
        AtomicInteger capsCalls = new AtomicInteger();
        boolean activateCalled = false;

        TestPad(int direction, String name) {
            super(direction, name);
        }

        @Override
        protected int chainFunc(Buffer buffer) {
            chainCalls.incrementAndGet();
            return OK;
        }

        @Override
        protected boolean eventFunc(Event event) {
            eventCalls.incrementAndGet();
            return true;
        }

        @Override
        protected boolean setCapsFunc(Caps caps) {
            capsCalls.incrementAndGet();
            return true;
        }

        @Override
        protected boolean activateFunc(int mode) {
            activateCalled = true;
            return true;
        }
    }

    private TestPad src;
    private TestPad sink;

    @BeforeEach
    void setup() {
        src = new TestPad(Pad.SRC, "src");
        sink = new TestPad(Pad.SINK, "sink");
    }

    @Test
    void testLinkSuccess() {
        assertTrue(src.link(sink));
        assertEquals(sink, src.getPeer());
        assertEquals(src, sink.getPeer());
    }

    @Test
    void testLinkWrongDirection() {
        TestPad wrong = new TestPad(Pad.SINK, "wrong");
        assertFalse(wrong.link(sink));
    }

    @Test
    void testLinkAlreadyLinked() {
        assertTrue(src.link(sink));
        TestPad otherSink = new TestPad(Pad.SINK, "other");
        assertFalse(src.link(otherSink));
    }

    @Test
    void testUnlink() {
        src.link(sink);
        src.unlink();
        assertNull(src.getPeer());
        assertNull(sink.getPeer());
    }

    @Test
    void testPushNotLinked() {
        Buffer buf = new Buffer();
        assertEquals(Pad.NOT_LINKED, src.push(buf));
    }

    @Test
    void testPushLinked() {
        src.link(sink);
        Buffer buf = new Buffer();
        assertEquals(Pad.OK, src.push(buf));
        assertEquals(1, sink.chainCalls.get());
    }

    @Test
    void testPushEventNotLinked() {
        Event e = Event.newEOS();
        assertFalse(src.pushEvent(e));
    }

    @Test
    void testPushEventLinked() {
        src.link(sink);
        Event e = Event.newEOS();
        assertTrue(src.pushEvent(e));
        assertEquals(1, sink.eventCalls.get());
    }

    @Test
    void testFlushingOnFlushStart() {
        src.link(sink);
        Event e = Event.newFlushStart();
        src.pushEvent(e);
        assertTrue(sink.isFlushing());
    }

    @Test
    void testFlushingOnFlushStop() {
        src.link(sink);
        sink.setFlushing(true);
        Event e = Event.newFlushStop();
        src.pushEvent(e);
        assertFalse(sink.isFlushing());
    }

    @Test
    void testCapsNegotiation() {
        src.link(sink);

        Caps caps = new Caps("audio/raw");
        Buffer buf = new Buffer();
        buf.caps = caps;

        assertEquals(Pad.OK, src.push(buf));
        assertEquals(caps, sink.getCaps());
        assertEquals(1, sink.capsCalls.get());
    }

    @Test
    void testCapsNegotiationFailure() {
        TestPad failingSink = new TestPad(Pad.SINK, "sink") {
            @Override
            protected boolean setCapsFunc(Caps caps) {
                return false;
            }
        };

        src.link(failingSink);

        Caps caps = new Caps("audio/raw");
        Buffer buf = new Buffer();
        buf.caps = caps;

        assertEquals(Pad.NOT_NEGOTIATED, src.push(buf));
    }

    @Test
    void testActivatePushMode() {
        assertTrue(src.activate(Pad.MODE_PUSH));
        assertTrue(src.activateCalled);
        assertFalse(src.isFlushing());
    }

    @Test
    void testActivateNoneMode() {
        TestPad pad = new TestPad(Pad.SINK, "pad");

        pad.activate(Pad.MODE_PUSH);

        assertTrue(pad.activate(Pad.MODE_NONE));
        assertTrue(pad.isFlushing());
    }

    @Test
    void testActivateIdempotent() {
        src.activate(Pad.MODE_PUSH);
        src.activateCalled = false;

        assertTrue(src.activate(Pad.MODE_PUSH));
        assertFalse(src.activateCalled);
    }

    @Test
    void testChainWhileFlushing() {
        src.link(sink);
        sink.setFlushing(true);

        Buffer buf = new Buffer();
        assertEquals(Pad.WRONG_STATE, src.push(buf));
    }

    @Test
    void testTaskLifecycle() throws InterruptedException {
        TestPad pad = new TestPad(Pad.SINK, "taskpad");

        assertTrue(pad.startTask("test-task"));
        Thread.sleep(10);

        assertTrue(pad.pauseTask());
        Thread.sleep(10);

        assertTrue(pad.stopTask());
    }
}
