package com.fluendo.jst;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QueryTest {

    @Test
    void testNewPosition() {
        Query q = Query.newPosition(7);

        assertEquals(Query.POSITION, q.getType());
        assertEquals(7, q.parsePositionFormat());
        assertEquals(-1, q.parsePositionValue());   // default
    }

    @Test
    void testSetPosition() {
        Query q = Query.newPosition(1);
        q.setPosition(3, 12345L);

        assertEquals(3, q.parsePositionFormat());
        assertEquals(12345L, q.parsePositionValue());
    }

    @Test
    void testPositionEdgeCases() {
        Query q = Query.newPosition(Integer.MAX_VALUE);
        q.setPosition(Integer.MIN_VALUE, Long.MIN_VALUE);

        assertEquals(Integer.MIN_VALUE, q.parsePositionFormat());
        assertEquals(Long.MIN_VALUE, q.parsePositionValue());
    }

    @Test
    void testNewDuration() {
        Query q = Query.newDuration(5);

        assertEquals(Query.DURATION, q.getType());
        assertEquals(5, q.parseDurationFormat());
        assertEquals(-1, q.parseDurationValue());   // default
    }

    @Test
    void testSetDuration() {
        Query q = Query.newDuration(2);
        q.setDuration(9, 99999L);

        assertEquals(9, q.parseDurationFormat());
        assertEquals(99999L, q.parseDurationValue());
    }

    @Test
    void testDurationEdgeCases() {
        Query q = Query.newDuration(Integer.MAX_VALUE);
        q.setDuration(Integer.MIN_VALUE, Long.MAX_VALUE);

        assertEquals(Integer.MIN_VALUE, q.parseDurationFormat());
        assertEquals(Long.MAX_VALUE, q.parseDurationValue());
    }

    @Test
    void testTypeIsImmutable() {
        Query q = Query.newPosition(1);
        assertEquals(Query.POSITION, q.getType());
        // no setter exists → compile‑time immutability
    }

    @Test
    void testDifferentQueryTypes() {
        Query q1 = Query.newPosition(1);
        Query q2 = Query.newDuration(1);

        assertNotEquals(q1.getType(), q2.getType());
    }
}
