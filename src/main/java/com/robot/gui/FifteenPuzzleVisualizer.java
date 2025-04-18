package com.robot.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class FifteenPuzzleVisualizer extends JPanel {
    private final FifteenPuzzleLogic logic;
    private final int tileSize = 100;

    public FifteenPuzzleVisualizer(int size) {
        this.logic = new FifteenPuzzleLogic(size);
        this.setPreferredSize(new Dimension(size * tileSize, size * tileSize));
        this.setLayout(new GridLayout(size, size));
        drawBoard();
    }

    private void drawBoard() {
        this.removeAll(); // Удаляем старые плитки
        int[][] board = logic.getBoard();
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                JButton button = new JButton(board[i][j] == 0 ? "" : String.valueOf(board[i][j]));
                button.setFont(new Font("Arial", Font.BOLD, 24));
                button.setFocusPainted(false);
                button.addActionListener(createTileClickListener(i, j));
                this.add(button);
            }
        }
        this.revalidate();
        this.repaint();
    }

    private ActionListener createTileClickListener(int x, int y) {
        return e -> {
            if (logic.moveTile(x, y)) {
                drawBoard();
                if (logic.isSolved()) {
                    JOptionPane.showMessageDialog(this, "Вы победили!");
                }
            }
        };
    }

    public void resetGame() {
        logic.resetBoard();
        drawBoard();
    }
}