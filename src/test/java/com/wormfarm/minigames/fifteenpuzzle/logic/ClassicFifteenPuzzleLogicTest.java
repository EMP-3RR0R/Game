package com.wormfarm.minigames.fifteenpuzzle.logic;

import com.wormfarm.minigames.fifteenpuzzle.events.PuzzleEventListener;
import org.junit.jupiter.api.*;

import org.mockito.Mockito;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ClassicFifteenPuzzleLogicTest {

    ClassicFifteenPuzzleLogic logic;

    @BeforeEach
    void setUp() {
        logic = new ClassicFifteenPuzzleLogic(4);
        logic.solvePuzzle();
    }

    @Test
    void testInitialSolvedState() {
        assertTrue(logic.isSolved());
        int[][] board = logic.getBoardCopy();
        for (int i = 0, cnt = 1; i < 4; i++)
            for (int j = 0; j < 4; j++)
                if (i == 3 && j == 3)
                    assertEquals(0, board[i][j]);
                else
                    assertEquals(cnt++, board[i][j]);
        assertEquals(0, logic.getMoveCount());
    }

    @Test
    void testMoveTile_ValidAndInvalid() {
        assertTrue(logic.moveTile(3, 2));
        int[][] board = logic.getBoardCopy();
        assertEquals(0, board[3][2]);
        assertEquals(15, board[3][3]);
        assertFalse(logic.moveTile(0, 0));
        assertEquals(1, board[0][0]);
    }

    @Test
    void testMoveTile_MoveBackAndForth() {
        assertTrue(logic.moveTile(3, 2));
        assertTrue(logic.moveTile(3, 3));
        assertTrue(logic.isSolved());
    }

    @Test
    void testMoveTile_ImpossibleMoveDoesntChangeBoard() {
        int[][] before = logic.getBoardCopy();
        assertFalse(logic.moveTile(0, 1));
        assertArrayEquals(before, logic.getBoardCopy());
    }

    @Test
    void testMoveCount_IncrementsOnValidOnly() {
        assertEquals(0, logic.getMoveCount());
        logic.moveTile(3, 2);
        assertEquals(1, logic.getMoveCount());
        logic.moveTile(3, 1);
        assertEquals(2, logic.getMoveCount());
        logic.moveTile(0, 0); // невалидный
        assertEquals(2, logic.getMoveCount());
    }

    @Test
    void testResetBoard_AfterSolve_NotSolved() {
        logic.resetBoard();
        assertFalse(logic.isSolved());
        assertEquals(0, logic.getMoveCount());
    }

    @Test
    void testSolvePuzzle_FixesAnyState() {
        logic.resetBoard();
        assertFalse(logic.isSolved());
        logic.solvePuzzle();
        assertTrue(logic.isSolved());
        assertEquals(0, logic.getMoveCount());
    }

    @Test
    void testGetCorrectTilesMask_AfterSolve() {
        boolean[][] mask = logic.getCorrectTilesMask();
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                assertTrue(mask[i][j], "Tile at ("+i+","+j+") should be correct");
    }

    @Test
    void testGetCorrectTilesMask_AfterMoves() {
        logic.moveTile(3, 2);
        boolean[][] mask = logic.getCorrectTilesMask();
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                if ((i == 3 && (j == 2 || j == 3)))
                    assertFalse(mask[i][j]);
                else
                    assertTrue(mask[i][j]);
    }

    @Test
    void testBoardCopyIsDeepCopy() {
        int[][] copy = logic.getBoardCopy();
        copy[0][0] = 111;
        assertNotEquals(111, logic.getBoardCopy()[0][0]);
    }

    @Test
    void testAddAndRemoveEventListener() {
        PuzzleEventListener listener = Mockito.mock(PuzzleEventListener.class);
        logic.addEventListener(listener);
        logic.moveTile(3, 2);
        Mockito.verify(listener).onMove(3, 2, true);
        logic.removeEventListener(listener);
        logic.moveTile(3, 3);
        Mockito.verifyNoMoreInteractions(listener);
    }

    @Test
    void testEventListener_OnWin() {
        AtomicBoolean winCalled = new AtomicBoolean(false);
        logic.addEventListener(new PuzzleEventListener() {
            public void onMove(int x, int y, boolean s) {}
            public void onWin() { winCalled.set(true);}
        });
        logic.moveTile(3, 2);
        logic.moveTile(3, 3);
        assertTrue(winCalled.get());
    }

    @Test
    void testShuffleBoardProducesSolvableState() {
        logic.shuffleBoard(80);
        logic.solvePuzzle();
        assertTrue(logic.isSolved());
    }

    @Test
    void testShuffleBoardMoveCountReset() {
        logic.moveTile(3,2); // 1 ход
        logic.shuffleBoard(10);
        assertEquals(0, logic.getMoveCount());
    }

    @Test
    void testTimerWorksCorrectly() throws InterruptedException {
        logic.resetBoard();
        Thread.sleep(10);
        logic.stopTimer();
        long elapsed = logic.getElapsedTimeMillis();
        assertTrue(elapsed >= 5 && elapsed < 1000, "Elapsed: " + elapsed);
    }

    @Test
    void testElapsedTimeBeforeStop() throws InterruptedException {
        logic.resetBoard();
        Thread.sleep(10);
        long elapsed = logic.getElapsedTimeMillis();
        assertTrue(elapsed >= 5 && elapsed < 1000, "Elapsed: " + elapsed);
    }

    @Test
    void testMultipleListeners_AllAreCalled() {
        AtomicInteger moveCalls = new AtomicInteger(0);
        AtomicInteger winCalls = new AtomicInteger(0);
        PuzzleEventListener l1 = new PuzzleEventListener() {
            public void onMove(int x, int y, boolean s) { moveCalls.incrementAndGet();}
            public void onWin() { winCalls.incrementAndGet();}
        };
        PuzzleEventListener l2 = new PuzzleEventListener() {
            public void onMove(int x, int y, boolean s) { moveCalls.incrementAndGet();}
            public void onWin() { winCalls.incrementAndGet();}
        };
        logic.addEventListener(l1);
        logic.addEventListener(l2);
        logic.moveTile(3,2);
        logic.moveTile(3,3);
        assertTrue(winCalls.get() >= 2);
        assertTrue(moveCalls.get() >= 2);
    }

    @Test
    void testGetSizeReturnsCorrectValue() {
        assertEquals(4, logic.getSize());
        ClassicFifteenPuzzleLogic small = new ClassicFifteenPuzzleLogic(2);
        assertEquals(2, small.getSize());
    }

    @Test
    void testSmallestAndLargestAllowedBoard() {
        ClassicFifteenPuzzleLogic small = new ClassicFifteenPuzzleLogic(2);
        small.solvePuzzle();
        assertTrue(small.isSolved());
        ClassicFifteenPuzzleLogic big = new ClassicFifteenPuzzleLogic(10);
        big.solvePuzzle();
        assertTrue(big.isSolved());
    }

    @Test
    void testGetEmptyXandYThrowsIfNoZero() throws Exception {
        ClassicFifteenPuzzleLogic l2 = new ClassicFifteenPuzzleLogic(3);
        int[][] b = l2.getBoardCopy();
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                b[i][j] = i*3+j+1;
        java.lang.reflect.Field f = ClassicFifteenPuzzleLogic.class.getDeclaredField("board");
        f.setAccessible(true);
        f.set(l2, b);
        assertThrows(IllegalStateException.class, l2::getEmptyX);
        assertThrows(IllegalStateException.class, l2::getEmptyY);
    }

    @Test
    void testIsValidMove_Correctness() throws Exception {
        java.lang.reflect.Method m = ClassicFifteenPuzzleLogic.class.getDeclaredMethod("isValidMove", int.class, int.class);
        m.setAccessible(true);
        assertTrue((Boolean)m.invoke(logic, 3,2));
        assertTrue((Boolean)m.invoke(logic, 2,3));
        assertFalse((Boolean)m.invoke(logic, 0,0));
    }

    @Test
    void testGetAdjacentTilesReturnsAllPossible() throws Exception {
        java.lang.reflect.Method m = ClassicFifteenPuzzleLogic.class.getDeclaredMethod("getAdjacentTiles", int.class, int.class);
        m.setAccessible(true);
        List<int[]> adj = (List<int[]>) m.invoke(logic, 1,1);
        assertEquals(4, adj.size());
        adj = (List<int[]>) m.invoke(logic, 0,0);
        assertEquals(2, adj.size());
    }

    @Test
    void testNoDuplicateListeners() {
        PuzzleEventListener l = Mockito.mock(PuzzleEventListener.class);
        logic.addEventListener(l);
        logic.addEventListener(l);
        logic.moveTile(3,2);
        Mockito.verify(l, Mockito.times(1)).onMove(3,2,true);
    }

    @Test
    void testRemoveEventListenerNotInListNoException() {
        PuzzleEventListener l = Mockito.mock(PuzzleEventListener.class);
        logic.removeEventListener(l);
    }

    @Test
    void testMultipleSolves_AlwaysSolved() {
        logic.resetBoard();
        logic.solvePuzzle();
        logic.solvePuzzle();
        assertTrue(logic.isSolved());
    }

    @Test
    void testSolvePuzzleFiresEventsEvenIfAlreadySolved() {
        PuzzleEventListener l = Mockito.mock(PuzzleEventListener.class);
        logic.addEventListener(l);
        logic.solvePuzzle();
        Mockito.verify(l, Mockito.atLeastOnce()).onWin();
    }

    @Test
    void testMoveTileAfterWinIncrementsMoveCount() {
        logic.moveTile(3,2);
        logic.moveTile(3,3);
        int moves = logic.getMoveCount();
        logic.moveTile(3,2);
        assertEquals(moves+1, logic.getMoveCount());
    }

    @Test
    void testEdgeCase_MoveTileOnEmptyCellReturnsFalse() {
        assertFalse(logic.moveTile(3,3));
        assertEquals(0, logic.getMoveCount());
    }
}