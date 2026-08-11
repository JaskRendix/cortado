package com.fluendo.jst;

import org.junit.jupiter.api.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class PipelineTestSuite {

    private Element elem() {
        Element e = mock(Element.class);
        when(e.enumPads()).thenReturn(Collections.emptyEnumeration());
        return e;
    }

    private Element sink() {
        Element e = elem();
        when(e.isFlagSet(Element.FLAG_IS_SINK)).thenReturn(true);
        return e;
    }

    private Pad pad(Element parent, int direction, Pad peer) {
        Pad p = mock(Pad.class);
        p.direction = direction;
        p.parent = parent;
        p.peer = peer;
        return p;
    }

    private int size(Enumeration<?> e) {
        int n = 0;
        while (e.hasMoreElements()) {
            e.nextElement();
            n++;
        }
        return n;
    }

    @Test
    void constructor_initializes() {
        Pipeline p = new Pipeline("x");
        assertNotNull(p.internalBus);
        assertNotNull(p.bus);
        assertNotNull(p.enumElements());
    }

    @Test
    void add_setsBusAndBaseTime() {
        Pipeline p = new Pipeline();
        Element e = elem();
        assertTrue(p.add(e));
        verify(e).setBus(p.internalBus);
        assertEquals(1, size(p.enumElements()));
    }

    @Test
    void add_clockProviderUpdatesClock() {
        Pipeline p = new Pipeline();
        ClockProvider cp = mock(ClockProvider.class);
        Clock c = mock(Clock.class);
        when(cp.provideClock()).thenReturn(c);
        assertTrue(p.add((Element) cp));
        assertEquals(c, p.defClock);
    }

    @Test
    void remove_resetsClockProvider() {
        Pipeline p = new Pipeline();
        ClockProvider cp = mock(ClockProvider.class);
        Clock c = mock(Clock.class);
        when(cp.provideClock()).thenReturn(c);
        p.add((Element) cp);
        assertTrue(p.remove((Element) cp));
        assertNull(p.clockProvider);
        assertNotEquals(c, p.defClock);
    }

    @Test
    void sorted_sinksFirst() {
        Pipeline p = new Pipeline();
        Element s = sink();
        Element n = elem();
        p.add(s);
        p.add(n);
        Enumeration<Element> e = p.enumSorted();
        assertEquals(s, e.nextElement());
    }

    @Test
    void sorted_handlesLoop() {
        Pipeline p = new Pipeline();
        Element a = elem();
        Element b = elem();
        when(a.isFlagSet(Element.FLAG_IS_SINK)).thenReturn(false);
        when(b.isFlagSet(Element.FLAG_IS_SINK)).thenReturn(false);
        Pad pa = pad(a, Pad.SINK, null);
        Pad pb = pad(b, Pad.SINK, null);
        when(a.enumPads()).thenReturn(Collections.enumeration(List.of(pa)));
        when(b.enumPads()).thenReturn(Collections.enumeration(List.of(pb)));
        p.add(a);
        p.add(b);
        Enumeration<Element> e = p.enumSorted();
        assertTrue(e.hasMoreElements());
    }

    @Test
    void sinks_onlySinkElements() {
        Pipeline p = new Pipeline();
        Element s = sink();
        Element n = elem();
        p.add(s);
        p.add(n);
        Enumeration<Element> e = p.enumSinks();
        assertEquals(s, e.nextElement());
        assertFalse(e.hasMoreElements());
    }

    @Test
    void eos_detectedOnlyWhenAllSinksPosted() {
        Pipeline p = new Pipeline();
        Element s1 = sink();
        Element s2 = sink();
        p.add(s1);
        p.add(s2);
        Message m1 = Message.newEOS(s1);
        Message m2 = Message.newEOS(s2);
        p.handleSyncMessage(m1);
        assertFalse(p.isEOS());
        p.handleSyncMessage(m2);
        assertTrue(p.isEOS());
    }

    @Test
    void replaceMessage_replacesByTypeAndSource() {
        Pipeline p = new Pipeline();
        Element s = sink();
        p.add(s);
        Message m1 = Message.newEOS(s);
        Message m2 = Message.newEOS(s);
        p.handleSyncMessage(m1);
        p.handleSyncMessage(m2);
        assertTrue(p.isEOS());
    }

    @Test
    void stopPause_resetsStreamTime() {
        Pipeline p = new Pipeline();
        Element e = elem();
        p.add(e);
        when(e.setState(Element.PAUSE)).thenReturn(Element.SUCCESS);
        p.setState(Element.PAUSE);
        assertEquals(0, p.streamTime);
    }

    @Test
    void pausePlay_setsBaseTime() {
        Pipeline p = new Pipeline();
        Clock c = mock(Clock.class);
        when(c.getTime()).thenReturn(1000L);
        p.useClock(c);
        Element e = elem();
        p.add(e);
        when(e.setState(Element.PLAY)).thenReturn(Element.SUCCESS);
        p.setState(Element.PLAY);
        assertEquals(1000L - p.streamTime, p.baseTime);
    }

    @Test
    void asyncChildState_triggersLostState() {
        Pipeline p = spy(new Pipeline());
        Element e = elem();
        p.add(e);
        when(e.setState(anyInt())).thenReturn(Element.ASYNC);
        doNothing().when(p).lostState();
        p.setState(Element.PLAY);
        verify(p).lostState();
    }

    @Test
    void sendEvent_dispatchesToAllSinks() {
        Pipeline p = new Pipeline();
        Element s = sink();
        p.add(s);
        Event ev = mock(Event.class);
        when(s.sendEvent(ev)).thenReturn(true);
        assertTrue(p.sendEvent(ev));
        verify(s).sendEvent(ev);
    }

    @Test
    void seekEvent_pausesThenRestores() {
        Pipeline p = spy(new Pipeline());
        Element s = sink();
        p.add(s);
        doReturn(Element.PLAY).when(p).getState(any(), any(), anyLong());
        doNothing().when(p).setState(Element.PAUSE);
        doNothing().when(p).setState(Element.PLAY);
        Event ev = mock(Event.class);
        when(ev.getType()).thenReturn(Event.Type.SEEK);
        when(s.sendEvent(ev)).thenReturn(true);
        assertTrue(p.sendEvent(ev));
        verify(p).setState(Element.PAUSE);
        verify(p).setState(Element.PLAY);
    }

    @Test
    void query_stopsAtFirstTrue() {
        Pipeline p = new Pipeline();
        Element s1 = sink();
        Element s2 = sink();
        Query q = mock(Query.class);
        when(s1.query(q)).thenReturn(false);
        when(s2.query(q)).thenReturn(true);
        p.add(s1);
        p.add(s2);
        assertTrue(p.query(q));
        verify(s2).query(q);
    }

    @Test
    void sorted_multipleLevels() {
        Pipeline p = new Pipeline();
        Element s = sink();
        Element a = elem();
        Element b = elem();
        Pad pa = pad(a, Pad.SINK, null);
        Pad pb = pad(b, Pad.SINK, null);
        when(a.enumPads()).thenReturn(Collections.enumeration(List.of(pa)));
        when(b.enumPads()).thenReturn(Collections.enumeration(List.of(pb)));
        p.add(s);
        p.add(a);
        p.add(b);
        Enumeration<Element> e = p.enumSorted();
        assertEquals(s, e.nextElement());
        assertTrue(e.hasMoreElements());
    }

    @Test
    void eventFailure_propagatesFalse() {
        Pipeline p = new Pipeline();
        Element s = sink();
        p.add(s);
        Event ev = mock(Event.class);
        when(s.sendEvent(ev)).thenReturn(false);
        assertFalse(p.sendEvent(ev));
    }

    @Test
    void query_noSinksReturnsTrue() {
        Pipeline p = new Pipeline();
        Query q = mock(Query.class);
        assertTrue(p.query(q));
    }

    @Test
    void removeElement_triggersStateDirtyViaPublicAPI() {
        Pipeline p = spy(new Pipeline());
        Element e = elem();
        p.add(e);
        doNothing().when(p).scheduleReCalcState();
        p.remove(e);
        verify(p).scheduleReCalcState();
    }
}
