package com.robot.log;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LogWindowSourceTest {
    private LogWindowSource logWindowSource;
    private final int queueLength = 5;

    @BeforeEach
    public void setUp() {
        logWindowSource = new LogWindowSource(queueLength);
    }

    @Test
    public void testInitialSetup() {
        assertNotNull(logWindowSource);
        assertEquals(0, logWindowSource.size());
    }

    @Test
    public void testRegisterListener() {
        LogChangeListener listener = new MockLogChangeListener();
        logWindowSource.registerListener(listener);
    }

    @Test
    public void testUnregisterListener() {
        LogChangeListener listener = new MockLogChangeListener();
        logWindowSource.registerListener(listener);
        logWindowSource.unregisterListener(listener);
    }

    @Test
    public void testAppendLogEntry() {
        LogLevel level = LogLevel.Debug;
        String message = "Test log message";
        logWindowSource.append(level, message);
        assertEquals(1, logWindowSource.size());
        List<LogEntry> entries = new ArrayList<>();
        logWindowSource.all().forEach(entries::add);
        assertEquals(level, entries.get(0).getLevel());
        assertEquals(message, entries.get(0).getMessage());
    }

    @Test
    public void testLogSizeLimit() {
        for (int i = 0; i < queueLength + 1; i++) {
            logWindowSource.append(LogLevel.Debug, "Message " + i);
        }
        assertEquals(queueLength, logWindowSource.size());
        List<LogEntry> entries = new ArrayList<>();
        logWindowSource.all().forEach(entries::add);
        assertEquals("Message 1", entries.get(0).getMessage());
    }

    @Test
    public void testRangeMethod() {
        for (int i = 0; i < queueLength; i++) {
            logWindowSource.append(LogLevel.Debug, "Message " + i);
        }
        Iterable<LogEntry> range = logWindowSource.range(1, 3);
        List<LogEntry> rangeList = new ArrayList<>();
        range.forEach(rangeList::add);
        assertEquals(3, rangeList.size());
        assertEquals("Message 1", rangeList.get(0).getMessage());
        assertEquals("Message 3", rangeList.get(2).getMessage());
    }

    @Test
    public void testAllMethod() {
        for (int i = 0; i < queueLength; i++) {
            logWindowSource.append(LogLevel.Debug, "Message " + i);
        }
        Iterable<LogEntry> allEntries = logWindowSource.all();
        List<LogEntry> allList = new ArrayList<>();
        allEntries.forEach(allList::add);
        assertEquals(queueLength, allList.size());
    }

    private static class MockLogChangeListener implements LogChangeListener {
        @Override
        public void onLogChanged() {

        }
    }
}