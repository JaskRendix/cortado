package com.jcraft.jorbis;

import static org.junit.jupiter.api.Assertions.*;

import com.jcraft.jogg.Buffer;
import org.junit.jupiter.api.Test;

class FuncFloorTest {

  static class MockFloor extends FuncFloor {
    boolean packCalled;
    boolean unpackCalled;
    boolean lookCalled;
    boolean freeInfoCalled;
    boolean freeLookCalled;
    boolean freeStateCalled;
    boolean forwardCalled;
    boolean inverse1Called;
    boolean inverse2Called;

    @Override
    void pack(Object i, Buffer opb) {
      packCalled = true;
      opb.write(7, 3);
    }

    @Override
    Object unpack(Info vi, Buffer opb) {
      unpackCalled = true;
      return new Object();
    }

    @Override
    Object look(DspState vd, InfoMode mi, Object i) {
      lookCalled = true;
      return new Object();
    }

    @Override
    void freeInfo(Object i) {
      freeInfoCalled = true;
    }

    @Override
    void freeLook(Object i) {
      freeLookCalled = true;
    }

    @Override
    void freeState(Object vs) {
      freeStateCalled = true;
    }

    @Override
    int forward(Block vb, Object i, float[] in, float[] out, Object vs) {
      forwardCalled = true;
      if (out != null && out.length > 0) out[0] = 1f;
      return 0;
    }

    @Override
    Object inverse1(Block vb, Object i, Object memo) {
      inverse1Called = true;
      return new float[] {2f};
    }

    @Override
    int inverse2(Block vb, Object i, Object memo, float[] out) {
      inverse2Called = true;
      if (out != null && out.length > 0) out[0] = 3f;
      return 0;
    }
  }

  @Test
  void floorArrayShouldContainTwoFloors() {
    assertEquals(2, FuncFloor.FLOOR_P.length);
    assertNotNull(FuncFloor.FLOOR_P[0]);
    assertNotNull(FuncFloor.FLOOR_P[1]);
  }

  @Test
  void packShouldWriteBits() {
    MockFloor f = new MockFloor();
    Buffer b = new Buffer();
    b.writeinit();
    f.pack(new Object(), b);
    assertTrue(f.packCalled);
    assertTrue(b.bits() > 0);
  }

  @Test
  void unpackShouldReturnNonNull() {
    MockFloor f = new MockFloor();
    Buffer b = new Buffer();
    b.writeinit();
    b.write(5, 3);
    Buffer r = new Buffer();
    r.readinit(b.buffer(), b.bytes());
    Object o = f.unpack(new Info(), r);
    assertTrue(f.unpackCalled);
    assertNotNull(o);
  }

  @Test
  void lookShouldReturnNonNull() {
    MockFloor f = new MockFloor();
    Object o = f.look(new DspState(), new InfoMode(), new Object());
    assertTrue(f.lookCalled);
    assertNotNull(o);
  }

  @Test
  void freeInfoShouldBeCalled() {
    MockFloor f = new MockFloor();
    f.freeInfo(new Object());
    assertTrue(f.freeInfoCalled);
  }

  @Test
  void freeLookShouldBeCalled() {
    MockFloor f = new MockFloor();
    f.freeLook(new Object());
    assertTrue(f.freeLookCalled);
  }

  @Test
  void freeStateShouldBeCalled() {
    MockFloor f = new MockFloor();
    f.freeState(new Object());
    assertTrue(f.freeStateCalled);
  }

  @Test
  void forwardShouldWriteOutput() {
    MockFloor f = new MockFloor();
    float[] in = {0f};
    float[] out = {0f};
    Block vb = new Block(new DspState());
    int r = f.forward(vb, new Object(), in, out, new Object());
    assertTrue(f.forwardCalled);
    assertEquals(0, r);
    assertEquals(1f, out[0]);
  }

  @Test
  void inverse1ShouldReturnMemo() {
    MockFloor f = new MockFloor();
    Block vb = new Block(new DspState());
    Object memo = f.inverse1(vb, new Object(), null);
    assertTrue(f.inverse1Called);
    assertNotNull(memo);
  }

  @Test
  void inverse2ShouldWriteOutput() {
    MockFloor f = new MockFloor();
    float[] out = {0f};
    Block vb = new Block(new DspState());
    int r = f.inverse2(vb, new Object(), new float[] {2f}, out);
    assertTrue(f.inverse2Called);
    assertEquals(0, r);
    assertEquals(3f, out[0]);
  }

  @Test
  void methodsShouldHandleNullInputs() {
    MockFloor f = new MockFloor();

    Buffer b = new Buffer();
    b.writeinit();

    f.pack(null, b);
    f.unpack(null, b);
    f.look(null, null, null);
    f.freeInfo(null);
    f.freeLook(null);
    f.freeState(null);

    Block vb = new Block(new DspState());
    f.forward(vb, null, null, null, null);
    f.inverse1(vb, null, null);
    f.inverse2(vb, null, null, null);
  }
}
