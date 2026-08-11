package com.fluendo.jst;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SinkTest {

    private Sink sink;
    private Buffer buffer;
    private Event event;
    private Query query;
    private Clock clock;
    private Clock.ClockID clockID;

    @BeforeEach
    void setup() {
        // Provide missing abstract method getFactoryName()
        sink = new Sink() {
            @Override
            public String getFactoryName() {
                return "test-sink";
            }

            @Override
            protected int preroll(Buffer buf) {
                return Pad.OK;
            }

            @Override
            protected int render(Buffer buf) {
                return Pad.OK;
            }
        };

        buffer = mock(Buffer.class);
        event = mock(Event.class);
        query = mock(Query.class);
        clock = mock(Clock.class);
        clockID = mock(Clock.ClockID.class);

        sink.clock = clock;
    }

    @Test
    void testPrerollReturnsOK() {
        assertEquals(Pad.OK, sink.preroll(buffer));
    }

    @Test
    void testRenderReturnsOK() {
        assertEquals(Pad.OK, sink.render(buffer));
    }

    @Test
    void testSendEventDelegatesToPad() {
        Pad pad = spy(sink.sinkpad);
        sink.sinkpad = pad;

        when(pad.pushEvent(event)).thenReturn(true);

        assertTrue(sink.sendEvent(event));
        verify(pad).pushEvent(event);
    }

    @Test
    void testQueryDurationDelegatesToPeer() {
        Pad peer = mock(Pad.class);
        when(peer.query(query)).thenReturn(true);

        // Pad.getPeer() returns the peer pad; we mock it by overriding getPeer()
        Pad pad = spy(sink.sinkpad);
        doReturn(peer).when(pad).getPeer();
        sink.sinkpad = pad;

        when(query.getType()).thenReturn(Query.DURATION);

        assertTrue(sink.query(query));
        verify(peer).query(query);
    }

    @Test
    void testQueryPositionTimeFormatPlayState() {
        sink.currentState = Element.PLAY;
        sink.baseTime = 1000;
        sink.segPosition = 200;
        sink.segStart = 300;

        when(query.getType()).thenReturn(Query.POSITION);
        when(query.parsePositionFormat()).thenReturn(Format.TIME);

        when(clock.getTime()).thenReturn(5000L);

        sink.query(query);

        long expected = 5000L - 1000 + 200 + 300;
        verify(query).setPosition(Format.TIME, expected);
    }

    @Test
    void testDoSyncWithoutClockReturnsOK() {
        sink.clock = null;

        WaitStatus status = sink.doSync(1000);
        assertEquals(WaitStatus.OK, status.status());
    }

    @Test
    void testDoSyncSchedulesClock() {
        when(clock.newSingleShotID(anyLong())).thenReturn(clockID);
        when(clockID.waitID()).thenReturn(new WaitStatus().withStatus(WaitStatus.OK));

        WaitStatus status = sink.doSync(1000);

        assertEquals(WaitStatus.OK, status.status());
        verify(clock).newSingleShotID(anyLong());
        verify(clockID).waitID();
    }

    @Test
    void testSetPropertyMaxLateness() {
        assertTrue(sink.setProperty("max-lateness", "123"));
        assertEquals(123, sink.maxLateness);
    }

    @Test
    void testSetPropertyUnknownReturnsFalse() {
        assertFalse(sink.setProperty("unknown", "value"));
    }
}
