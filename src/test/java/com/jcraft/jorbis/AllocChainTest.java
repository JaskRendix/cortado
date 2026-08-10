package com.jcraft.jorbis;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AllocChainTest {

    @Test
    void defaultFieldsShouldBeNull() {
        AllocChain c = new AllocChain();
        assertNull(c.ptr);
        assertNull(c.next);
    }

    @Test
    void shouldStorePointerObject() {
        AllocChain c = new AllocChain();
        Object o = new Object();
        c.ptr = o;
        assertSame(o, c.ptr);
    }

    @Test
    void shouldLinkToNextNode() {
        AllocChain a = new AllocChain();
        AllocChain b = new AllocChain();
        a.next = b;
        assertSame(b, a.next);
    }

    @Test
    void shouldSupportMultiLevelChain() {
        AllocChain a = new AllocChain();
        AllocChain b = new AllocChain();
        AllocChain c = new AllocChain();

        a.next = b;
        b.next = c;

        assertSame(c, a.next.next);
    }

    @Test
    void shouldAllowNullPointerInChain() {
        AllocChain a = new AllocChain();
        AllocChain b = new AllocChain();

        a.ptr = null;
        b.ptr = null;
        a.next = b;

        assertNull(a.ptr);
        assertNull(a.next.ptr);
    }

    @Test
    void shouldAllowCycles() {
        AllocChain a = new AllocChain();
        AllocChain b = new AllocChain();

        a.next = b;
        b.next = a;

        assertSame(a, b.next);
        assertSame(b, a.next);
    }

    @Test
    void mutationShouldNotAffectOtherNodes() {
        AllocChain a = new AllocChain();
        AllocChain b = new AllocChain();

        a.ptr = "first";
        b.ptr = "second";

        a.next = b;

        b.ptr = "changed";

        assertEquals("first", a.ptr);
        assertEquals("changed", a.next.ptr);
    }

    @Test
    void nextCanBeReassigned() {
        AllocChain a = new AllocChain();
        AllocChain b = new AllocChain();
        AllocChain c = new AllocChain();

        a.next = b;
        a.next = c;

        assertSame(c, a.next);
    }
}
