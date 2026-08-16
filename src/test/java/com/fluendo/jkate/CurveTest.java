package com.fluendo.jkate;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CurveTest {

  @Test
  @DisplayName("Constructors & Accessors: Default and parameterized constructors work as expected")
  void testConstructorsAndGettersSetters() throws KateException {
    KateCurveType curveType = KateCurveType.createCurveType(2); // Linear
    double[][] points = {{0.0, 0.0}, {1.0, 1.0}};

    Curve curve1 = new Curve();
    curve1.setType(curveType);
    curve1.setNpts(2);
    curve1.setPts(points);

    assertEquals(curveType, curve1.getType());
    assertEquals(2, curve1.getNpts());
    assertArrayEquals(points, curve1.getPts());

    Curve curve2 = new Curve(curveType, 2, points);
    assertEquals(curve1, curve2);
  }

  @Test
  @DisplayName("Equals and HashCode: Equal objects match and have same hash")
  void testEqualsAndHashCode() throws KateException {
    KateCurveType t1 = KateCurveType.createCurveType(0);
    KateCurveType t2 = KateCurveType.createCurveType(1);

    double[][] p1 = {{1.0, 2.0}};
    double[][] p2 = {{3.0, 4.0}};

    Curve c1 = new Curve(t1, 1, p1);
    Curve c2 = new Curve(t1, 1, p1);
    Curve c3 = new Curve(t2, 1, p1);
    Curve c4 = new Curve(t1, 1, p2);

    assertEquals(c1, c1);
    assertEquals(c1, c2);
    assertEquals(c1.hashCode(), c2.hashCode());
    assertNotEquals(c1, c3);
    assertNotEquals(c1, c4);
    assertNotEquals(c1, null);
    assertNotEquals(c1, "SomeString");
  }

  @Test
  @DisplayName("ToString: Returns a non-null formatted string")
  void testToString() throws KateException {
    KateCurveType t = KateCurveType.createCurveType(0);
    double[][] p = {{0.0, 0.0}};
    Curve curve = new Curve(t, 1, p);

    String str = curve.toString();
    assertNotNull(str);
    assertTrue(str.contains("npts=1"));
    assertTrue(str.contains("pts="));
  }
}
