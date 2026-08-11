package com.fluendo.jst.testplugins;

import com.fluendo.jst.Element;

public class TestElementB extends Element {

    public TestElementB() {
        super("testB");
    }

    @Override
    public int typeFind(byte[] data, int offset, int length) {
        return (length > 0 && data[offset] == 'B') ? 20 : -1;
    }

    @Override
    public String getMime() {
        return "test/b";
    }

    @Override
    public String getFactoryName() {
        return "TestElementB";
    }
}
