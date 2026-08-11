package com.fluendo.jst;

import org.junit.jupiter.api.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PipelineTestSuite {

    private Element mockElem() {
        Element e = mock(Element.class);
        when(e.enumPads()).thenReturn(Collections.emptyEnumeration());
        return e;
    }

    private Pad mockPad(Element parent, int direction, Pad peer) {
        Pad p = mock(Pad.class);
        p.direction = direction;
        p.parent = parent;
        p.peer = peer;
        return p;
    }

    private int enumSize(Enumeration<?> e) {
        int n = 0;
        while (e.hasMoreElements()) {
            e.nextElement();
            n++;
        }
        return n;
    }

    @Test
    void constructor_initializesBusAndThreads() {
        Pipeline p = new Pipeline("test");
        assertNotNull(p.internalBus);
        assertNotNull(p.bus);
        assertNotNull(p.enumElements());
    }

    @Test
    void addElement_setsBusAndBaseTime() {
        Pipeline p = new Pipeline();
        Element elem = mockElem();

        assertTrue(p.add(elem));
        verify(elem).setBus(p.internalBus);
        assertEquals(1, enumSize(p.enumElements()));
    }

    @Test
    void addClockProvider_updatesDefaultClock() {
        Pipeline p = new Pipeline();
        ClockProvider cp = mock(ClockProvider.class);
        Clock clock = mock(Clock.class);

        when(cp.provideClock()).thenReturn(clock);

        assertTrue(p.add((Element) cp));
        assertEquals(clock, p.defClock);
    }

    @Test
    void removeElement_resetsClockProvider() {
        Pipeline p = new Pipeline();
        ClockProvider cp = mock(ClockProvider.class);
        Clock clock = mock(Clock.class);

        when(cp.provideClock()).thenReturn(clock);

        p.add((Element) cp);
        assertTrue(p.remove((Element) cp));

        assertNull(p.clockProvider);
        assertNotEquals(clock, p.defClock);
    }

    @Test
    void sortedEnumerator_ordersSinksFirst() {
        Pipeline p = new Pipeline();

        Element sink = mockElem();
        when(sink.isFlagSet(Element.FLAG_IS_SINK)).thenReturn(true);

        Element normal = mockElem();
        when(normal.isFlagSet(Element.FLAG_IS_SINK)).thenReturn(false);

        p.add(sink);
        p.add(normal);

        Enumeration<Element> sorted = p.enumSorted();
        assertTrue(sorted.hasMoreElements());
        assertEquals(sink, sorted.nextElement());
    }

    @Test
    void sortedEnumerator_detectsLoop() {
        Pipeline p = new Pipeline();

        Element a = mockElem();
        Element b = mockElem();

        when(a.isFlagSet(Element.FLAG_IS_SINK)).thenReturn(false);
        when(b.isFlagSet(Element.FLAG_IS_SINK)).thenReturn(false);

        Pad padA = mockPad(a, Pad.SINK, null);
        Pad padB = mockPad(b, Pad.SINK, null);

        when(a.enumPads()).thenReturn(Collections.enumeration(List.of(padA)));
        when(b.enumPads()).thenReturn(Collections.enumeration(List.of(padB)));

        p.add(a);
        p.add(b);

        Enumeration<Element> sorted = p.enumSorted();
        assertTrue(sorted.hasMoreElements());
    }

    @Test
    void sinkEnumerator_findsOnlySinks() {
        Pipeline p = new Pipeline();

        Element sink = mockElem();
        when(sink.isFlagSet(Element.FLAG_IS_SINK)).thenReturn(true);

        Element normal = mockElem();
        when(normal.isFlagSet(Element.FLAG_IS_SINK)).thenReturn(false);

        p.add(sink);
        p.add(normal);

        Enumeration<Element> sinks = p.enumSinks();
        assertTrue(sinks.hasMoreElements());
        assertEquals(sink, sinks.nextElement());
        assertFalse(sinks.hasMoreElements());
    }

    @Test
    void eosIsDetectedWhenAllSinksPostEOS() {
        Pipeline p = new Pipeline();

        Element sink1 = mockElem();
        Element sink2 = mockElem();

        when(sink1.isFlagSet(Element.FLAG_IS_SINK)).thenReturn(true);
        when(sink2.isFlagSet(Element.FLAG_IS_SINK)).thenReturn(true);

        p.add(sink1);
        p.add(sink2);

        Message eos1 = Message.newEOS(sink1);
        Message eos2 = Message.newEOS(sink2);

        p.handleSyncMessage(eos1);
        assertFalse(p.isEOS());

        p.handleSyncMessage(eos2);
        assertTrue(p.isEOS());
    }

    @Test
    void replaceMessage_replacesByTypeAndSource() {
        Pipeline p = new Pipeline();

        Element sink = mockElem();
        when(sink.isFlagSet(Element.FLAG_IS_SINK)).thenReturn(true);
        p.add(sink);

        Message m1 = Message.newEOS(sink);
        Message m2 = Message.newEOS(sink);

        p.handleSyncMessage(m1);
        p.handleSyncMessage(m2);

        assertTrue(p.isEOS());
    }

    @Test
    void stopPause_clearsMessagesAndResetsStreamTime() {
        Pipeline p = new Pipeline();
        Element elem = mockElem();
        p.add(elem);

        when(elem.setState(Element.PAUSE)).thenReturn(Element.SUCCESS);

        p.setState(Element.PAUSE);

        assertEquals(0, p.streamTime);
    }

    @Test
    void pausePlay_setsBaseTimeCorrectly() {
        Pipeline p = new Pipeline();
        Clock clock = mock(Clock.class);

        when(clock.getTime()).thenReturn(1000L);
        p.useClock(clock);

        Element elem = mockElem();
        p.add(elem);

        when(elem.setState(Element.PLAY)).thenReturn(Element.SUCCESS);

        p.setState(Element.PLAY);

        assertEquals(1000L - p.streamTime, p.baseTime);
    }

    @Test
    void asyncChildStateCausesLostState() {
        Pipeline p = spy(new Pipeline());
        Element elem = mockElem();
        p.add(elem);

        when(elem.setState(anyInt())).thenReturn(Element.ASYNC);

        doNothing().when(p).lostState();

        p.setState(Element.PLAY);

        verify(p).lostState();
    }

    @Test
    void sendEvent_dispatchesToAllSinks() {
        Pipeline p = new Pipeline();

        Element sink = mockElem();
        when(sink.isFlagSet(Element.FLAG_IS_SINK)).thenReturn(true);
        p.add(sink);

        Event ev = mock(Event.class);
        when(sink.sendEvent(ev)).thenReturn(true);

        assertTrue(p.sendEvent(ev));
        verify(sink).sendEvent(ev);
    }

    @Test
    void seekEvent_pausesThenRestoresState() {
        Pipeline p = spy(new Pipeline());
        Element sink = mockElem();
        when(sink.isFlagSet(Element.FLAG_IS_SINK)).thenReturn(true);

        p.add(sink);

        doReturn(Element.PLAY).when(p).getState(any(), any(), anyLong());
        doNothing().when(p).setState(Element.PAUSE);
        doNothing().when(p).setState(Element.PLAY);

        Event seek = mock(Event.class);
        when(seek.getType()).thenReturn(Event.SEEK);
        when(sink.sendEvent(seek)).thenReturn(true);

        assertTrue(p.sendEvent(seek));
        verify(p).setState(Element.PAUSE);
        verify(p).setState(Element.PLAY);
    }

    @Test
    void queryStopsAtFirstTrueSink() {
        Pipeline p = new Pipeline();

        Element sink1 = mockElem();
        Element sink2 = mockElem();

        when(sink1.isFlagSet(Element.FLAG_IS_SINK)).thenReturn(true);
        when(sink2.isFlagSet(Element.FLAG_IS_SINK)).thenReturn(true);

        Query q = mock(Query.class);

        when(sink1.query(q)).thenReturn(false);
        when(sink2.query(q)).thenReturn(true);

        p.add(sink1);
        p.add(sink2);

        assertTrue(p.query(q));
        verify(sink2).query(q);
    }
}
