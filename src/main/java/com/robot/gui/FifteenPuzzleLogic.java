package com.robot.gui;

public class FifteenPuzzleLogic {
    private final int size;
    private final int[][] board;

    public FifteenPuzzleLogic(int size) {
        this.size = size;
        this.board = new int[size][size];
        resetBoard();
    }

    public void resetBoard() {
        int count = 1;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                board[i][j] = count++;
            }
        }
        board[size - 1][size - 1] = 0; // Последняя позиция пустая
    }

    public boolean moveTile(int x, int y) {
        // Логика перемещения плитки
        if (isValidMove(x, y)) {
            int emptyX = getEmptyX();
            int emptyY = getEmptyY();
            board[emptyX][emptyY] = board[x][y];
            board[x][y] = 0;
            return true;
        }
        return false;
    }

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

    private boolean isValidMove(int x, int y) {
        int emptyX = getEmptyX();
        int emptyY = getEmptyY();
        return (Math.abs(emptyX - x) + Math.abs(emptyY - y)) == 1;
    }

    private int getEmptyX() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (board[i][j] == 0) return i;
            }
        }
        throw new IllegalStateException("Empty tile not found!");
    }

    private int getEmptyY() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (board[i][j] == 0) return j;
            }
        }
        throw new IllegalStateException("Empty tile not found!");
    }

    public int[][] getBoard() {
        return board;
    }
}