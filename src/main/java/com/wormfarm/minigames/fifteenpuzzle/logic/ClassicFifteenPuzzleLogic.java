package com.wormfarm.minigames.fifteenpuzzle.logic;

import com.wormfarm.minigames.fifteenpuzzle.api.FifteenPuzzleGame;
import com.wormfarm.minigames.fifteenpuzzle.events.PuzzleEventListener;
import java.util.ArrayList;
import java.util.List;

public class ClassicFifteenPuzzleLogic implements FifteenPuzzleGame {
    private final int size;
    private final int[][] board;
    private final List<PuzzleEventListener> listeners = new ArrayList<>();

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
            success = true;
            fireMoveEvent(x, y, true);
            if (isSolved()) {
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
}