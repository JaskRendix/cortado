package com.jcraft.jorbis;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InfoModeTest {

    @Nested
    @DisplayName("Default State Tests")
    class DefaultState {

        @Test
        @DisplayName("All fields should default to zero")
        void defaultsAreZero() {
            InfoMode mode = new InfoMode();

            assertEquals(0, mode.getBlockflag());
            assertEquals(0, mode.getWindowtype());
            assertEquals(0, mode.getTransformtype());
            assertEquals(0, mode.getMapping());
        }
    }

    @Nested
    @DisplayName("Setter/Getter Tests")
    class SetterGetterTests {

        @Test
        @DisplayName("Setting and getting blockflag works")
        void blockflagSetGet() {
            InfoMode mode = new InfoMode();
            mode.setBlockflag(5);
            assertEquals(5, mode.getBlockflag());
        }

        @Test
        @DisplayName("Setting and getting windowtype works")
        void windowtypeSetGet() {
            InfoMode mode = new InfoMode();
            mode.setWindowtype(3);
            assertEquals(3, mode.getWindowtype());
        }

        @Test
        @DisplayName("Setting and getting transformtype works")
        void transformtypeSetGet() {
            InfoMode mode = new InfoMode();
            mode.setTransformtype(7);
            assertEquals(7, mode.getTransformtype());
        }

        @Test
        @DisplayName("Setting and getting mapping works")
        void mappingSetGet() {
            InfoMode mode = new InfoMode();
            mode.setMapping(2);
            assertEquals(2, mode.getMapping());
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCases {

        @Test
        @DisplayName("Negative values should be accepted (Vorbis spec allows them)")
        void negativeValuesAllowed() {
            InfoMode mode = new InfoMode();

            mode.setBlockflag(-1);
            mode.setWindowtype(-5);
            mode.setTransformtype(-10);
            mode.setMapping(-3);

            assertEquals(-1, mode.getBlockflag());
            assertEquals(-5, mode.getWindowtype());
            assertEquals(-10, mode.getTransformtype());
            assertEquals(-3, mode.getMapping());
        }

        @Test
        @DisplayName("Large values should be stored without truncation")
        void largeValuesAllowed() {
            InfoMode mode = new InfoMode();

            mode.setBlockflag(Integer.MAX_VALUE);
            mode.setWindowtype(Integer.MAX_VALUE);
            mode.setTransformtype(Integer.MAX_VALUE);
            mode.setMapping(Integer.MAX_VALUE);

            assertEquals(Integer.MAX_VALUE, mode.getBlockflag());
            assertEquals(Integer.MAX_VALUE, mode.getWindowtype());
            assertEquals(Integer.MAX_VALUE, mode.getTransformtype());
            assertEquals(Integer.MAX_VALUE, mode.getMapping());
        }

        @Test
        @DisplayName("Zero values should be stored correctly")
        void zeroValues() {
            InfoMode mode = new InfoMode();

            mode.setBlockflag(0);
            mode.setWindowtype(0);
            mode.setTransformtype(0);
            mode.setMapping(0);

            assertEquals(0, mode.getBlockflag());
            assertEquals(0, mode.getWindowtype());
            assertEquals(0, mode.getTransformtype());
            assertEquals(0, mode.getMapping());
        }
    }

    @Nested
    @DisplayName("Mutation Safety Tests")
    class MutationSafety {

        @Test
        @DisplayName("Multiple consecutive writes should overwrite previous values")
        void overwriteValues() {
            InfoMode mode = new InfoMode();

            mode.setBlockflag(1);
            mode.setBlockflag(2);
            mode.setBlockflag(3);

            assertEquals(3, mode.getBlockflag());
        }

        @Test
        @DisplayName("Fields should not affect each other")
        void fieldsIndependent() {
            InfoMode mode = new InfoMode();

            mode.setBlockflag(10);
            mode.setWindowtype(20);
            mode.setTransformtype(30);
            mode.setMapping(40);

            assertAll(
                () -> assertEquals(10, mode.getBlockflag()),
                () -> assertEquals(20, mode.getWindowtype()),
                () -> assertEquals(30, mode.getTransformtype()),
                () -> assertEquals(40, mode.getMapping())
            );
        }
    }
}
