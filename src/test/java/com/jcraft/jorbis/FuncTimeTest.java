package com.jcraft.jorbis;

import com.jcraft.jogg.Buffer;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class FuncTimeTest {

    private FuncTime original;

    /** Replace TIME_P[0] with a mock */
    private void injectMock(FuncTime mock) throws Exception {
        Field f = FuncTime.class.getDeclaredField("TIME_P");
        f.setAccessible(true);
        FuncTime[] arr = (FuncTime[]) f.get(null);
        original = arr[0];
        arr[0] = mock;
    }

    /** Restore original Time0 implementation */
    private void restore() throws Exception {
        if (original == null) return;
        Field f = FuncTime.class.getDeclaredField("TIME_P");
        f.setAccessible(true);
        FuncTime[] arr = (FuncTime[]) f.get(null);
        arr[0] = original;
    }

    @AfterEach
    void cleanup() throws Exception {
        restore();
    }

    @Test
    void testPackIsCalled() throws Exception {
        FuncTime mockTime = mock(FuncTime.class);
        injectMock(mockTime);

        Buffer buffer = mock(Buffer.class);

        FuncTime.TIME_P[0].pack("info", buffer);

        verify(mockTime).pack("info", buffer);
    }

    @Test
    void testUnpackReturnsValue() throws Exception {
        FuncTime mockTime = mock(FuncTime.class);
        injectMock(mockTime);

        Info info = mock(Info.class);
        Buffer buffer = mock(Buffer.class);

        when(mockTime.unpack(info, buffer)).thenReturn("RESULT");

        Object out = FuncTime.TIME_P[0].unpack(info, buffer);
        assertEquals("RESULT", out);
    }

    @Test
    void testLookIsCalled() throws Exception {
        FuncTime mockTime = mock(FuncTime.class);
        injectMock(mockTime);

        DspState vd = mock(DspState.class);
        InfoMode vm = mock(InfoMode.class);

        FuncTime.TIME_P[0].look(vd, vm, "imap");

        verify(mockTime).look(vd, vm, "imap");
    }

    @Test
    void testFreeInfoIsCalled() throws Exception {
        FuncTime mockTime = mock(FuncTime.class);
        injectMock(mockTime);

        FuncTime.TIME_P[0].freeInfo("X");

        verify(mockTime).freeInfo("X");
    }

    @Test
    void testFreeLookIsCalled() throws Exception {
        FuncTime mockTime = mock(FuncTime.class);
        injectMock(mockTime);

        FuncTime.TIME_P[0].freeLook("Y");

        verify(mockTime).freeLook("Y");
    }

    @Test
    void testForwardReturnsErrorCode() throws Exception {
        FuncTime mockTime = mock(FuncTime.class);
        injectMock(mockTime);

        Block block = mock(Block.class);
        when(mockTime.forward(block, "LM")).thenReturn(-42);

        int out = FuncTime.TIME_P[0].forward(block, "LM");
        assertEquals(-42, out);
    }

    @Test
    void testForwardHandlesNullBlock() throws Exception {
        FuncTime mockTime = mock(FuncTime.class);
        injectMock(mockTime);

        when(mockTime.forward(null, "LM")).thenReturn(-5);

        int out = FuncTime.TIME_P[0].forward(null, "LM");
        assertEquals(-5, out);
    }

    @Test
    void testInverseReturnsErrorCode() throws Exception {
        FuncTime mockTime = mock(FuncTime.class);
        injectMock(mockTime);

        Block block = mock(Block.class);
        float[] in = new float[128];
        float[] out = new float[128];

        when(mockTime.inverse(block, "LM", in, out)).thenReturn(-99);

        int result = FuncTime.TIME_P[0].inverse(block, "LM", in, out);
        assertEquals(-99, result);
    }

    @Test
    void testInverseHandlesNullArrays() throws Exception {
        FuncTime mockTime = mock(FuncTime.class);
        injectMock(mockTime);

        Block block = mock(Block.class);

        when(mockTime.inverse(block, "LM", null, null)).thenReturn(-7);

        int result = FuncTime.TIME_P[0].inverse(block, "LM", null, null);
        assertEquals(-7, result);
    }

    @Test
    void testInverseHandlesMismatchedArraySizes() throws Exception {
        FuncTime mockTime = mock(FuncTime.class);
        injectMock(mockTime);

        Block block = mock(Block.class);
        float[] in = new float[10];
        float[] out = new float[5];

        when(mockTime.inverse(block, "LM", in, out)).thenReturn(-3);

        int result = FuncTime.TIME_P[0].inverse(block, "LM", in, out);
        assertEquals(-3, result);
    }

    @Test
    void testStaticTableIsFinal() throws Exception {
        Field f = FuncTime.class.getDeclaredField("TIME_P");
        assertTrue(java.lang.reflect.Modifier.isFinal(f.getModifiers()));
        assertTrue(java.lang.reflect.Modifier.isStatic(f.getModifiers()));
    }
}
