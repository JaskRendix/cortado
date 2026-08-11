package com.fluendo.jst;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ObjectTest {

    /** Minimal concrete subclass because Object is abstract */
    private static class TestObject extends com.fluendo.jst.Object {
        TestObject() {
            super("test");
        }
    }

    @Test
    void testDefaultName() {
        TestObject obj = new TestObject();
        assertEquals("test", obj.getName());
    }

    @Test
    void testSetName() {
        TestObject obj = new TestObject();
        obj.setName("newname");
        assertEquals("newname", obj.getName());
    }

    @Test
    void testParentAssignment() {
        TestObject parent = new TestObject();
        TestObject child = new TestObject();

        assertTrue(child.setParent(parent));
        assertEquals(parent, child.getParent());
    }

    @Test
    void testParentCannotBeOverwritten() {
        TestObject parent1 = new TestObject();
        TestObject parent2 = new TestObject();
        TestObject child = new TestObject();

        assertTrue(child.setParent(parent1));
        assertFalse(child.setParent(parent2));  // cannot overwrite
        assertEquals(parent1, child.getParent());
    }

    @Test
    void testUnParent() {
        TestObject parent = new TestObject();
        TestObject child = new TestObject();

        child.setParent(parent);
        child.unParent();

        assertNull(child.getParent());
    }

    @Test
    void testSetFlag() {
        TestObject obj = new TestObject();
        obj.setFlag(4);
        assertTrue(obj.isFlagSet(4));
    }

    @Test
    void testUnsetFlag() {
        TestObject obj = new TestObject();
        obj.setFlag(4);
        obj.unsetFlag(4);
        assertFalse(obj.isFlagSet(4));
    }

    @Test
    void testMultipleFlags() {
        TestObject obj = new TestObject();
        obj.setFlag(1);
        obj.setFlag(2);

        assertTrue(obj.isFlagSet(1));
        assertTrue(obj.isFlagSet(2));

        obj.unsetFlag(1);
        assertFalse(obj.isFlagSet(1));
        assertTrue(obj.isFlagSet(2));
    }

    @Test
    void testSetPropertyAlwaysFalse() {
        TestObject obj = new TestObject();
        assertFalse(obj.setProperty("x", 123));
    }

    @Test
    void testGetPropertyAlwaysNull() {
        TestObject obj = new TestObject();
        assertNull(obj.getProperty("x"));
    }
}
