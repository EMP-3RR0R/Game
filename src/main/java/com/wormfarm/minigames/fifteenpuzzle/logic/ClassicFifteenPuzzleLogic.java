package com.wormfarm.minigames.fifteenpuzzle.logic;

import com.wormfarm.minigames.fifteenpuzzle.api.FifteenPuzzleGame;
import com.wormfarm.minigames.fifteenpuzzle.events.PuzzleEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ClassicFifteenPuzzleLogic implements FifteenPuzzleGame {
    private final int size;
    private final int[][] board;
    private final List<PuzzleEventListener> listeners = new ArrayList<>();
    private final Random random = new Random();

    private int moveCount = 0;
    private long startTime = 0;
    private long endTime = 0;
    private boolean isShuffling = false;

    public ClassicFifteenPuzzleLogic(int size) {
        this.size = size;
        this.board = new int[size][size];
        resetBoard();
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public int[][] getBoardCopy() {
        int[][] copy = new int[size][size];
        for (int i = 0; i < size; i++) {
            System.arraycopy(board[i], 0, copy[i], 0, size);
        }
        return copy;
    }

    @Override
    public boolean moveTile(int x, int y) {
        boolean success = false;
        if (isValidMove(x, y)) {
            int emptyX = getEmptyX(), emptyY = getEmptyY();
            board[emptyX][emptyY] = board[x][y];
            board[x][y] = 0;
            if (!isShuffling) moveCount++;
            success = true;
            fireMoveEvent(x, y, true);
            if (!isShuffling && isSolved()) {
                stopTimer();
                fireWinEvent();
            }
        } else {
            fireMoveEvent(x, y, false);
        }
        return success;
    }

    @Override
    public boolean isSolved() {
        int count = 1;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (i == size - 1 && j == size - 1) {
                    return board[i][j] == 0;
                }
                if (board[i][j] != count++) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void resetBoard() {
        int count = 1;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                board[i][j] = count++;
            }
        }
        board[size - 1][size - 1] = 0;
        moveCount = 0;
        startTimer();
        shuffleBoard(5000 + random.nextInt(10000));
    }

    public void shuffleBoard(int minMoves) {
        isShuffling = true;
        int lastX = getEmptyX();
        int lastY = getEmptyY();
        for (int i = 0; i < minMoves; i++) {
            List<int[]> moves = getAdjacentTiles(lastX, lastY);
            int[] move = moves.get(random.nextInt(moves.size()));
            moveTile(move[0], move[1]);
            lastX = getEmptyX();
            lastY = getEmptyY();
        }
        moveCount = 0; // не считаем ходы при перемешивании
        startTimer();
        isShuffling = false;
    }

    private List<int[]> getAdjacentTiles(int emptyX, int emptyY) {
        List<int[]> list = new ArrayList<>();
        if (emptyX > 0) list.add(new int[] {emptyX - 1, emptyY});
        if (emptyX < size - 1) list.add(new int[] {emptyX + 1, emptyY});
        if (emptyY > 0) list.add(new int[] {emptyX, emptyY - 1});
        if (emptyY < size - 1) list.add(new int[] {emptyX, emptyY + 1});
        return list;
    }

    private boolean isValidMove(int x, int y) {
        int emptyX = getEmptyX();
        int emptyY = getEmptyY();
        return (Math.abs(emptyX - x) + Math.abs(emptyY - y)) == 1;
    }

    private int getEmptyX() {
        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++)
                if (board[i][j] == 0) return i;
        throw new IllegalStateException("Empty tile not found");
    }

    private int getEmptyY() {
        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++)
                if (board[i][j] == 0) return j;
        throw new IllegalStateException("Empty tile not found");
    }

    public boolean[][] getCorrectTilesMask() {
        boolean[][] mask = new boolean[size][size];
        int expected = 1;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (i == size - 1 && j == size - 1) {
                    mask[i][j] = board[i][j] == 0;
                } else {
                    mask[i][j] = board[i][j] == expected;
                }
                expected++;
            }
        }
        return mask;
    }

    /** Решает пятнашки "читерски" — выставляет их в правильное состояние */
    public void solvePuzzle() {
        int count = 1;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                board[i][j] = count++;
            }
        }
        board[size - 1][size - 1] = 0;
        moveCount = 0;
        stopTimer();
        fireMoveEvent(-1, -1, true);
        fireWinEvent();
    }

    @Override
    public void addEventListener(PuzzleEventListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeEventListener(PuzzleEventListener listener) {
        listeners.remove(listener);
    }

    private void fireMoveEvent(int x, int y, boolean success) {
        for (PuzzleEventListener l : listeners) l.onMove(x, y, success);
    }

    private void fireWinEvent() {
        for (PuzzleEventListener l : listeners) l.onWin();
    }

    // --- Move counter and timer ---

    public int getMoveCount() {
        return moveCount;
    }

    public void startTimer() {
        startTime = System.currentTimeMillis();
        endTime = 0;
    }

    public void stopTimer() {
        endTime = System.currentTimeMillis();
    }

    public long getElapsedTimeMillis() {
        return (endTime > 0 ? endTime : System.currentTimeMillis()) - startTime;
    }
}