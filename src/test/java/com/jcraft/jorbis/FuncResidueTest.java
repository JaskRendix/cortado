package com.jcraft.jorbis;

import com.jcraft.jogg.Buffer;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class FuncResidueTest {

    private FuncResidue original;

    /** Replace RESIDUE_P[index] with a mock */
    private void injectMock(int index, FuncResidue mock) throws Exception {
        Field f = FuncResidue.class.getDeclaredField("RESIDUE_P");
        f.setAccessible(true);
        FuncResidue[] arr = (FuncResidue[]) f.get(null);
        original = arr[index];
        arr[index] = mock;
    }

    /** Restore original implementation */
    private void restore(int index) throws Exception {
        if (original == null) return;
        Field f = FuncResidue.class.getDeclaredField("RESIDUE_P");
        f.setAccessible(true);
        FuncResidue[] arr = (FuncResidue[]) f.get(null);
        arr[index] = original;
    }

    @AfterEach
    void cleanup() throws Exception {
        // restore index 0 by default
        restore(0);
    }

    @Test
    void testPackIsCalled() throws Exception {
        FuncResidue mockRes = mock(FuncResidue.class);
        injectMock(0, mockRes);

        Buffer buffer = mock(Buffer.class);

        FuncResidue.RESIDUE_P[0].pack("vr", buffer);

        verify(mockRes).pack("vr", buffer);
    }

    @Test
    void testUnpackReturnsValue() throws Exception {
        FuncResidue mockRes = mock(FuncResidue.class);
        injectMock(0, mockRes);

        Info info = mock(Info.class);
        Buffer buffer = mock(Buffer.class);

        when(mockRes.unpack(info, buffer)).thenReturn("RESULT");

        Object out = FuncResidue.RESIDUE_P[0].unpack(info, buffer);
        assertEquals("RESULT", out);
    }

    @Test
    void testLookIsCalled() throws Exception {
        FuncResidue mockRes = mock(FuncResidue.class);
        injectMock(0, mockRes);

        DspState vd = mock(DspState.class);
        InfoMode vm = mock(InfoMode.class);

        FuncResidue.RESIDUE_P[0].look(vd, vm, "vr");

        verify(mockRes).look(vd, vm, "vr");
    }

    @Test
    void testFreeInfoIsCalled() throws Exception {
        FuncResidue mockRes = mock(FuncResidue.class);
        injectMock(0, mockRes);

        FuncResidue.RESIDUE_P[0].freeInfo("X");

        verify(mockRes).freeInfo("X");
    }

    @Test
    void testFreeLookIsCalled() throws Exception {
        FuncResidue mockRes = mock(FuncResidue.class);
        injectMock(0, mockRes);

        FuncResidue.RESIDUE_P[0].freeLook("Y");

        verify(mockRes).freeLook("Y");
    }

    @Test
    void testForwardReturnsErrorCode() throws Exception {
        FuncResidue mockRes = mock(FuncResidue.class);
        injectMock(0, mockRes);

        Block block = mock(Block.class);
        float[][] in = new float[2][128];

        when(mockRes.forward(block, "LOOK", in, 2)).thenReturn(-42);

        int out = FuncResidue.RESIDUE_P[0].forward(block, "LOOK", in, 2);
        assertEquals(-42, out);
    }

    @Test
    void testForwardHandlesNullBlock() throws Exception {
        FuncResidue mockRes = mock(FuncResidue.class);
        injectMock(0, mockRes);

        float[][] in = new float[1][64];

        when(mockRes.forward(null, "LOOK", in, 1)).thenReturn(-5);

        int out = FuncResidue.RESIDUE_P[0].forward(null, "LOOK", in, 1);
        assertEquals(-5, out);
    }

    @Test
    void testForwardHandlesZeroChannels() throws Exception {
        FuncResidue mockRes = mock(FuncResidue.class);
        injectMock(0, mockRes);

        float[][] in = new float[0][];

        when(mockRes.forward(any(), any(), eq(in), eq(0))).thenReturn(-1);

        int out = FuncResidue.RESIDUE_P[0].forward(mock(Block.class), "LOOK", in, 0);
        assertEquals(-1, out);
    }

    @Test
    void testInverseReturnsErrorCode() throws Exception {
        FuncResidue mockRes = mock(FuncResidue.class);
        injectMock(0, mockRes);

        Block block = mock(Block.class);
        float[][] in = new float[2][128];
        int[] nonzero = new int[]{1, 0};

        when(mockRes.inverse(block, "LOOK", in, nonzero, 2)).thenReturn(-99);

        int out = FuncResidue.RESIDUE_P[0].inverse(block, "LOOK", in, nonzero, 2);
        assertEquals(-99, out);
    }

    @Test
    void testInverseHandlesNullArrays() throws Exception {
        FuncResidue mockRes = mock(FuncResidue.class);
        injectMock(0, mockRes);

        Block block = mock(Block.class);

        when(mockRes.inverse(block, "LOOK", null, null, 0)).thenReturn(-7);

        int out = FuncResidue.RESIDUE_P[0].inverse(block, "LOOK", null, null, 0);
        assertEquals(-7, out);
    }

    @Test
    void testInverseHandlesMismatchedChannelCounts() throws Exception {
        FuncResidue mockRes = mock(FuncResidue.class);
        injectMock(0, mockRes);

        Block block = mock(Block.class);
        float[][] in = new float[3][];
        int[] nonzero = new int[]{1, 0};

        when(mockRes.inverse(block, "LOOK", in, nonzero, 3)).thenReturn(-3);

        int out = FuncResidue.RESIDUE_P[0].inverse(block, "LOOK", in, nonzero, 3);
        assertEquals(-3, out);
    }

    @Test
    void testStaticTableIsFinal() throws Exception {
        Field f = FuncResidue.class.getDeclaredField("RESIDUE_P");
        assertTrue(java.lang.reflect.Modifier.isFinal(f.getModifiers()));
        assertTrue(java.lang.reflect.Modifier.isStatic(f.getModifiers()));
    }
}
