package com.jcraft.jogg;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SyncStateTest {

    @Test
    void clearShouldNullData() {
        SyncState s = new SyncState();
        s.data = new byte[10];
        s.clear();
        assertNull(s.data);
    }

    @Test
    void bufferShouldGrowStorage() {
        SyncState s = new SyncState();
        int pos = s.buffer(1000);
        assertEquals(0, pos);
        assertNotNull(s.data);
        assertTrue(s.storage >= 1000);
    }

    @Test
    void bufferShouldCompactReturnedBytes() {
        SyncState s = new SyncState();
        s.buffer(10);
        s.wrote(10);

        s.returned = 5;
        s.data[5] = 99;

        s.buffer(10);
        assertEquals(5, s.fill);
        assertEquals(99, s.data[0]);
    }

    @Test
    void wroteShouldRejectOverflow() {
        SyncState s = new SyncState();

        // Manually constrain storage
        s.data = new byte[5];
        s.storage = 5;

        s.fill = 5;
        assertEquals(-1, s.wrote(1));
    }

    @Test
    void pageseekShouldReturnZeroWhenNotEnoughHeaderBytes() {
        SyncState s = new SyncState();
        s.buffer(10);
        s.wrote(10);

        Page p = new Page();
        assertEquals(0, s.pageseek(p));
    }

    @Test
    void pageseekShouldDetectInvalidCapturePattern() {
        SyncState s = new SyncState();
        s.buffer(30);
        byte[] d = s.data;

        // Not "OggS"
        d[0] = 'X';
        d[1] = 'g';
        d[2] = 'g';
        d[3] = 'S';

        s.wrote(30);

        Page p = new Page();
        int ret = s.pageseek(p);

        assertTrue(ret < 0);
        assertTrue(s.returned > 0);
    }

    @Test
    void pageseekShouldReturnZeroIfPartialPage() {
        SyncState s = new SyncState();
        s.buffer(27);
        byte[] d = s.data;

        // Valid capture pattern
        d[0] = 'O';
        d[1] = 'g';
        d[2] = 'g';
        d[3] = 'S';

        // Segment count = 1
        d[26] = 1;

        s.wrote(27);

        Page p = new Page();
        assertEquals(0, s.pageseek(p));
    }

    @Test
    void pageseekShouldRejectChecksumMismatch() {
        SyncState s = new SyncState();
        s.buffer(50);
        byte[] d = s.data;

        // Valid capture pattern
        d[0] = 'O';
        d[1] = 'g';
        d[2] = 'g';
        d[3] = 'S';

        // Segment count = 1
        d[26] = 1;
        d[27] = 20; // body size

        // Fake header + body
        for (int i = 0; i < 50; i++) d[i] = (byte) i;

        s.wrote(50);

        Page p = new Page();
        int ret = s.pageseek(p);

        assertTrue(ret < 0);
        assertTrue(s.returned > 0);
    }

    @Test
    void pageseekShouldReturnFullPageOnSuccess() {
        SyncState s = new SyncState();
        s.buffer(50);
        byte[] d = s.data;

        d[0] = 'O';
        d[1] = 'g';
        d[2] = 'g';
        d[3] = 'S';

        for (int i = 4; i < 22; i++) d[i] = 0;

        d[22] = d[23] = d[24] = d[25] = 0;

        d[26] = 1;
        d[27] = 20;

        for (int i = 28; i < 48; i++) d[i] = 1;

        s.wrote(48);

        Page p = new Page();
        p.header_base = s.data;
        p.header = 0;
        p.header_len = 28;
        p.body_base = s.data;
        p.body = 28;
        p.body_len = 20;
        p.checksum();

        int ret = s.pageseek(p);

        assertEquals(48, ret);
    }

    @Test
    void pageoutShouldReturnOneOnValidPage() {
        SyncState s = new SyncState();
        s.buffer(50);
        byte[] d = s.data;

        d[0] = 'O';
        d[1] = 'g';
        d[2] = 'g';
        d[3] = 'S';

        for (int i = 4; i < 22; i++) d[i] = 0;

        d[22] = d[23] = d[24] = d[25] = 0;

        d[26] = 1;
        d[27] = 20;

        for (int i = 28; i < 48; i++) d[i] = 1;

        s.wrote(48);

        Page p = new Page();
        p.header_base = s.data;
        p.header = 0;
        p.header_len = 28;
        p.body_base = s.data;
        p.body = 28;
        p.body_len = 20;
        p.checksum();

        assertEquals(1, s.pageout(p));
    }

    @Test
    void pageoutShouldReturnMinusOneOnUnsynced() {
        SyncState s = new SyncState();
        s.buffer(30);
        byte[] d = s.data;

        // Invalid capture pattern
        d[0] = 'X';
        d[1] = 'g';
        d[2] = 'g';
        d[3] = 'S';

        s.wrote(30);

        Page p = new Page();
        assertEquals(-1, s.pageout(p));
    }

    @Test
    void resetShouldClearState() {
        SyncState s = new SyncState();
        s.buffer(10);
        s.wrote(10);
        s.returned = 5;
        s.unsynced = 1;
        s.headerbytes = 3;
        s.bodybytes = 7;

        s.reset();

        assertEquals(0, s.fill);
        assertEquals(0, s.returned);
        assertEquals(0, s.unsynced);
        assertEquals(0, s.headerbytes);
        assertEquals(0, s.bodybytes);
    }
}
