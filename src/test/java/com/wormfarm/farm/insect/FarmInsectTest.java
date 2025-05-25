package com.wormfarm.farm.insect;

import org.junit.jupiter.api.*;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class FarmInsectTest {

    // Простая реализация для тестирования абстракта
    static class TestInsect extends FarmInsect {
        boolean idleCalled = false;
        boolean workingCalled = false;
        boolean customCalled = false;
        boolean arrivedCalled = false;

        public TestInsect(String name, int x, int y, int speed) {
            super(name, x, y, speed);
        }

        @Override
        protected void onIdle() {
            idleCalled = true;
        }

        @Override
        protected void onWorking() {
            workingCalled = true;
        }

        @Override
        protected void onCustom() {
            customCalled = true;
        }

        @Override
        protected void onArrived() {
            arrivedCalled = true;
            super.onArrived();
        }
    }

    @Test
    void testConstructorAndGetters() {
        TestInsect insect = new TestInsect("test", 10, 20, 3);
        assertEquals("test", insect.getName());
        assertEquals(10, insect.getX());
        assertEquals(20, insect.getY());
        assertEquals(3, insect.getSpeed());
        assertEquals(FarmInsect.State.IDLE, insect.getState());
        assertNull(insect.getTargetX());
        assertNull(insect.getTargetY());
        assertNotNull(insect.getId());
    }

    @Test
    void testUniqueId() {
        HashSet<String> ids = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            TestInsect insect = new TestInsect("bug", 0, 0, 1);
            assertTrue(ids.add(insect.getId()));
        }
    }

    @Test
    void testSetTarget() {
        TestInsect insect = new TestInsect("test", 0, 0, 1);
        insect.setTarget(5, 6);
        assertEquals(5, insect.getTargetX());
        assertEquals(6, insect.getTargetY());
    }

    @Test
    void testGoToSetsMovingStateAndTarget() {
        TestInsect insect = new TestInsect("test", 0, 0, 1);
        insect.goTo(3, 4);
        assertEquals(FarmInsect.State.MOVING, insect.getState());
        assertEquals(3, insect.getTargetX());
        assertEquals(4, insect.getTargetY());
    }

    @Test
    void testTickCallsOnIdle() {
        TestInsect insect = new TestInsect("test", 0, 0, 1);
        insect.state = FarmInsect.State.IDLE;
        insect.tick();
        assertTrue(insect.idleCalled);
    }

    @Test
    void testTickCallsOnWorking() {
        TestInsect insect = new TestInsect("test", 0, 0, 1);
        insect.state = FarmInsect.State.WORKING;
        insect.tick();
        assertTrue(insect.workingCalled);
    }

    @Test
    void testTickCallsOnCustom() {
        TestInsect insect = new TestInsect("test", 0, 0, 1);
        insect.state = FarmInsect.State.CUSTOM;
        insect.tick();
        assertTrue(insect.customCalled);
    }

    @Test
    void testTickMovingToTargetAndArrived() {
        TestInsect insect = new TestInsect("test", 0, 0, 2);
        insect.setTarget(4, 0);
        insect.state = FarmInsect.State.MOVING;
        insect.tick();
        // move by 2 units
        assertEquals(2, insect.getX());
        assertEquals(0, insect.getY());
        assertFalse(insect.arrivedCalled);

        insect.tick(); // move by 2 more, should arrive now
        assertEquals(4, insect.getX());
        assertEquals(0, insect.getY());
        assertTrue(insect.arrivedCalled);
        assertEquals(FarmInsect.State.WORKING, insect.getState());
    }

    @Test
    void testMoveToTargetWithNullTargetGoesIdle() {
        TestInsect insect = new TestInsect("test", 0, 0, 2);
        insect.setTarget(null, null);
        insect.state = FarmInsect.State.MOVING;
        insect.tick();
        assertEquals(FarmInsect.State.IDLE, insect.getState());
    }

    @Test
    void testSerializeFormatAndToString() {
        TestInsect insect = new TestInsect("bug", 7, 8, 3);
        insect.state = FarmInsect.State.WORKING;
        insect.setTarget(12, 13);
        String ser = insect.serialize();
        assertTrue(ser.contains("bug"));
        assertTrue(ser.contains("WORKING"));
        assertTrue(ser.contains("12"));
        assertTrue(insect.toString().contains("bug"));
        assertTrue(insect.toString().contains("x=7"));
    }
}