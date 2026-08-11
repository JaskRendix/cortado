package com.fluendo.jst;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ElementTest {

    private static class TestPad extends Pad {
        boolean activated = false;

        TestPad(String name) {
            super(Pad.SINK, name);
        }

        @Override
        protected boolean activateFunc(int mode) {
            activated = (mode == Pad.MODE_PUSH);
            return true;
        }
    }

    private static class TestBus extends Bus {
        AtomicInteger posted = new AtomicInteger(0);

        @Override
        public void post(Message message) {
            posted.incrementAndGet();
        }
    }

    private static class TestElement extends Element {
        TestElement() {
            super("test");
        }

        @Override
        public String getFactoryName() {
            return "test-factory";
        }
    }

    private TestElement element;

    @BeforeEach
    void setup() {
        element = new TestElement();
    }

    @Test
    void testInitialState() {
        assertEquals(Element.STOP, element.currentState);
        assertEquals(Element.NONE, element.nextState);
        assertEquals(Element.NONE, element.pendingState);
        assertEquals(Element.SUCCESS, element.lastReturn);
    }

    @Test
    void testAddPad() {
        TestPad pad = new TestPad("sink");
        assertTrue(element.addPad(pad));
        assertEquals(pad, element.getPad("sink"));
    }

    @Test
    void testRemovePad() {
        TestPad pad = new TestPad("sink");
        element.addPad(pad);
        assertTrue(element.removePad(pad));
        assertNull(element.getPad("sink"));
    }

    @Test
    void testPadListeners() {
        AtomicInteger added = new AtomicInteger();
        AtomicInteger removed = new AtomicInteger();
        AtomicInteger noMore = new AtomicInteger();

        PadListener listener = new PadListener() {
            @Override
            public void padAdded(Pad pad) {
                added.incrementAndGet();
            }

            @Override
            public void padRemoved(Pad pad) {
                removed.incrementAndGet();
            }

            @Override
            public void noMorePads() {
                noMore.incrementAndGet();
            }
        };

        element.addPadListener(listener);

        TestPad pad = new TestPad("sink");
        element.addPad(pad);
        element.removePad(pad);
        element.noMorePads();

        assertEquals(1, added.get());
        assertEquals(1, removed.get());
        assertEquals(1, noMore.get());
    }

    @Test
    void testPostMessage() {
        TestBus bus = new TestBus();
        element.setBus(bus);

        element.postMessage(Message.newStateDirty(element));
        assertEquals(1, bus.posted.get());
    }

    @Test
    void testStateTransitionStopToPause() {
        int result = element.setState(Element.PAUSE);
        assertEquals(Element.SUCCESS, result);
    }

    @Test
    void testStateTransitionPauseToPlay() {
        element.currentState = Element.PAUSE;
        int result = element.setState(Element.PLAY);
        assertEquals(Element.SUCCESS, result);
    }

    @Test
    void testStateTransitionPlayToPause() {
        element.currentState = Element.PLAY;
        int result = element.setState(Element.PAUSE);
        assertEquals(Element.SUCCESS, result);
    }

    @Test
    void testPadsActivateOnStopPause() {
        TestPad pad = new TestPad("sink");
        element.addPad(pad);

        element.currentState = Element.STOP;
        element.setState(Element.PAUSE);

        assertTrue(pad.activated);
    }

    @Test
    void testPadsDeactivateOnPauseStop() {
        TestPad pad = new TestPad("sink");
        element.addPad(pad);

        element.currentState = Element.PAUSE;
        element.setState(Element.STOP);

        assertFalse(pad.activated);
    }

    @Test
    void testAbortState() {
        element.pendingState = Element.PLAY;
        element.lastReturn = Element.SUCCESS;

        element.abortState();

        assertEquals(Element.FAILURE, element.lastReturn);
    }

    @Test
    void testLostState() {
        TestBus bus = new TestBus();
        element.setBus(bus);

        element.currentState = Element.PAUSE;
        element.pendingState = Element.NONE;
        element.lastReturn = Element.SUCCESS;

        element.lostState();

        assertEquals(2, bus.posted.get());
        assertEquals(Element.ASYNC, element.lastReturn);
    }

    @Test
    void testGetState() {
        int[] resState = new int[1];
        int[] resPending = new int[1];

        int ret = element.getState(resState, resPending, 0);

        assertEquals(Element.SUCCESS, ret);
        assertEquals(Element.STOP, resState[0]);
        assertEquals(Element.NONE, resPending[0]);
    }
}
