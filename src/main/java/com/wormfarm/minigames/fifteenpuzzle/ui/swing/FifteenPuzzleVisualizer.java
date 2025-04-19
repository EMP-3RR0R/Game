package com.wormfarm.minigames.fifteenpuzzle.ui.swing;

import javax.swing.*;
import java.awt.*;

public class FifteenPuzzleVisualizer extends JPanel {
    private int[][] board = null;
    private final int tileSize;

    public FifteenPuzzleVisualizer(int size, int tileSize) {
        this.tileSize = tileSize;
        setPreferredSize(new Dimension(size * tileSize, size * tileSize));
    }

    public void setBoard(int[][] board) {
        this.board = board;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (board == null) return;
        int size = board.length;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                int value = board[i][j];
                int x = j * tileSize;
                int y = i * tileSize;
                if (value != 0) {
                    g.setColor(Color.LIGHT_GRAY);
                    g.fillRect(x + 2, y + 2, tileSize - 4, tileSize - 4);
                    g.setColor(Color.BLACK);
                    g.drawRect(x + 2, y + 2, tileSize - 4, tileSize - 4);
                    g.setFont(new Font("Arial", Font.BOLD, 24));
                    String text = String.valueOf(value);
                    FontMetrics fm = g.getFontMetrics();
                    int textWidth = fm.stringWidth(text);
                    int textHeight = fm.getHeight();
                    g.drawString(text, x + (tileSize - textWidth) / 2, y + (tileSize + textHeight / 2) / 2);
                }
            }
        }
        // Draw grid
        g.setColor(Color.GRAY);
        for (int i = 0; i <= board.length; i++) {
            g.drawLine(0, i * tileSize, board.length * tileSize, i * tileSize);
            g.drawLine(i * tileSize, 0, i * tileSize, board.length * tileSize);
        }
    }
}