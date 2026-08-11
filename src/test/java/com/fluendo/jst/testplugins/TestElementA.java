package com.fluendo.jst.testplugins;

import com.fluendo.jst.Element;

public class TestElementA extends Element {

    public TestElementA() {
        super("testA");
    }

    @Override
    public int typeFind(byte[] data, int offset, int length) {
        return (length > 0 && data[offset] == 'A') ? 10 : -1;
    }

    @Override
    public String getMime() {
        return "test/a";
    }

    @Override
    public String getFactoryName() {
        return "TestElementA";
    }
}
