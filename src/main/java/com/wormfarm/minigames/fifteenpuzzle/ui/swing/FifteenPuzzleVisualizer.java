package com.wormfarm.minigames.fifteenpuzzle.ui.swing;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FifteenPuzzleVisualizer extends JPanel {
    private int[][] board;
    private final int size;
    private final int tileSize;

    // Анимация перемещения
    private final java.util.Map<Integer, Point> tilePositions = new java.util.HashMap<>();
    private final java.util.Map<Integer, Point> tileTargetPositions = new java.util.HashMap<>();
    private Timer animationTimer;
    private boolean animating = false;

    // Анимация "дрожи"
    private final java.util.Map<Integer, Point> shakeOffsets = new java.util.HashMap<>();

    // Список всех подходящих картинок (BufferedImage), которые были загружены
    private List<BufferedImage> loadedSprites = new ArrayList<>();
    // Текущий рабочий массив нарезанных тайлов
    private BufferedImage[] tileImages = null;
    // Индекс текущей картинки (в loadedSprites)
    private int currentSpriteIndex = -1;

    private final Random random = new Random();

    // --- Текстура земли для фона (уменьшенная) ---
    private static BufferedImage groundTexture = null;
    private static final int GROUND_TEXTURE_SIZE = 64;

    // --- Логика для маски правильных тайлов ---
    private final Object logic;

    static {
        try (InputStream in = FifteenPuzzleVisualizer.class.getResourceAsStream("/images/ground.png")) {
            if (in != null) {
                BufferedImage original = ImageIO.read(in);
                if (original != null) {
                    groundTexture = resizeTexture(original, GROUND_TEXTURE_SIZE, GROUND_TEXTURE_SIZE);
                }
            }
        } catch (Exception e) {
            groundTexture = null;
        }
    }

    // Уменьшение текстуры до нужного размера
    private static BufferedImage resizeTexture(BufferedImage img, int targetW, int targetH) {
        Image scaled = img.getScaledInstance(targetW, targetH, Image.SCALE_SMOOTH);
        BufferedImage small = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = small.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(scaled, 0, 0, null);
        g2.dispose();
        return small;
    }

    public FifteenPuzzleVisualizer(int size, int tileSize, Object logic) {
        this.size = size;
        this.tileSize = tileSize;
        setPreferredSize(new Dimension(size * tileSize, size * tileSize));
        this.logic = logic;

        loadAllSprites();
        pickRandomSprite();

        this.addHierarchyListener(new HierarchyListener() {
            @Override
            public void hierarchyChanged(HierarchyEvent e) {
                if ((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0
                        && !isDisplayable()) {
                    clearSprites();
                }
            }
        });
    }

    // Загружает все FifteenPuzzleSpriteN начиная с 1, пока не встретится ошибка
    private void loadAllSprites() {
        loadedSprites.clear();
        int n = 1;
        while (true) {
            String path = String.format("/images/FifteenPuzzle/FifteenPuzzleSprite%d.png", n);
            try (InputStream in = getClass().getResourceAsStream(path)) {
                if (in == null) break;
                BufferedImage sprite = ImageIO.read(in);
                if (sprite != null) {
                    loadedSprites.add(sprite);
                } else {
                    break;
                }
            } catch (Exception ex) {
                break;
            }
            n++;
        }
    }

    // Очищает список загруженных картинок (вызывать при закрытии)
    public void clearSprites() {
        loadedSprites.clear();
        tileImages = null;
        currentSpriteIndex = -1;
    }

    // Выбор рандомной картинки из списка (и нарезка её на тайлы)
    private void pickRandomSprite() {
        if (loadedSprites.isEmpty()) {
            tileImages = null;
            currentSpriteIndex = -1;
            repaint();
            return;
        }
        int idx = random.nextInt(loadedSprites.size());
        currentSpriteIndex = idx;
        BufferedImage sprite = loadedSprites.get(idx);

        int minSide = Math.min(sprite.getWidth(), sprite.getHeight());
        int x0 = (sprite.getWidth()  - minSide) / 2;
        int y0 = (sprite.getHeight() - minSide) / 2;
        BufferedImage square = sprite.getSubimage(x0, y0, minSide, minSide);

        int dim = size;
        int tileW = square.getWidth() / dim;
        int tileH = square.getHeight() / dim;
        tileImages = new BufferedImage[dim * dim];
        for (int r = 0; r < dim; r++) {
            for (int c = 0; c < dim; c++) {
                int idxFlat = r * dim + c;
                if (idxFlat < tileImages.length - 1) {
                    tileImages[idxFlat] = square.getSubimage(c * tileW, r * tileH, tileW, tileH);
                } else {
                    tileImages[idxFlat] = null; // пустая клетка
                }
            }
        }
        repaint();
    }

    public void setBoard(int[][] board) {
        this.board = board;
        if (!animating) {
            updateTilePositions();
        }
        repaint();
    }

    // Сброс игры: выбираем новый спрайт
    public void resetPuzzleImage() {
        pickRandomSprite();
    }

    // Обновить позиции тайлов без анимации
    private void updateTilePositions() {
        tilePositions.clear();
        if (board == null) return;
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                int value = board[r][c];
                if (value != 0) {
                    tilePositions.put(value, new Point(c * tileSize, r * tileSize));
                }
            }
        }
    }

    // Запустить анимацию движения value из (fromRow,fromCol) в (toRow,toCol)
    public void animateMove(final int value, int fromRow, int fromCol, int toRow, int toCol, Runnable callback) {
        if (animating) return;
        animating = true;

        Point from = new Point(fromCol * tileSize, fromRow * tileSize);
        Point to = new Point(toCol * tileSize, toRow * tileSize);
        tilePositions.put(value, new Point(from));
        tileTargetPositions.put(value, to);

        int frames = 12;
        int[] currFrame = {0};

        animationTimer = new Timer(1000 / 60, e -> {
            currFrame[0]++;
            double progress = currFrame[0] / (double) frames;
            if (progress > 1.0) progress = 1.0;
            int x = (int) (from.x + (to.x - from.x) * progress);
            int y = (int) (from.y + (to.y - from.y) * progress);
            tilePositions.put(value, new Point(x, y));
            repaint();

            if (progress >= 1.0) {
                ((Timer) e.getSource()).stop();
                tilePositions.put(value, to);
                tileTargetPositions.remove(value);
                animating = false;
                updateTilePositions();
                repaint();
                if (callback != null) callback.run();
            }
        });
        animationTimer.start();
    }

    public boolean isAnimating() {
        return animating;
    }

    // Анимация "дрожи"
    public void animateShake(int value) {
        final int shakeDistance = 1; // пикселей
        final int shakeFrames = 8;
        final int[] shakePattern = {+1, -2, +2, -2, +2, -1, 0, 0};

        if (shakeOffsets.containsKey(value)) return;

        Timer timer = new Timer(1000 / 60, null);
        final int[] frame = {0};
        timer.addActionListener(e -> {
            if (frame[0] < shakeFrames) {
                int dx = shakePattern[frame[0]] * shakeDistance;
                shakeOffsets.put(value, new Point(dx, 0));
                repaint();
                frame[0]++;
            } else {
                shakeOffsets.remove(value);
                repaint();
                ((Timer) e.getSource()).stop();
            }
        });
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 1. Рисуем фон — землю текстурой тайлом
        if (groundTexture != null) {
            int texW = groundTexture.getWidth();
            int texH = groundTexture.getHeight();
            for (int y = 0; y < getHeight(); y += texH) {
                for (int x = 0; x < getWidth(); x += texW) {
                    g.drawImage(groundTexture, x, y, this);
                }
            }
        } else {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        // 2. Получаем маску правильных тайлов (через функцию логики)
        boolean[][] correctMask = null;
        if (logic != null) {
            try {
                Method method = logic.getClass().getMethod("getCorrectTilesMask");
                Object maskObj = method.invoke(logic);
                if (maskObj instanceof boolean[][])
                    correctMask = (boolean[][]) maskObj;
            } catch (Exception ex) {
                // Нет такого метода — просто не отмечаем ничего
            }
        }

        // 3. Клетки
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                g.setColor(Color.LIGHT_GRAY);
                g.drawRect(c * tileSize, r * tileSize, tileSize, tileSize);
            }
        }

        // 4. Тайлы
        if (board != null) {
            for (int r = 0; r < size; r++) {
                for (int c = 0; c < size; c++) {
                    int value = board[r][c];
                    if (value != 0) {
                        Point pos = tilePositions.getOrDefault(value, new Point(c * tileSize, r * tileSize));
                        Point shake = shakeOffsets.getOrDefault(value, new Point(0, 0));
                        int x = pos.x + shake.x;
                        int y = pos.y + shake.y;

                        boolean isCorrect = correctMask != null && correctMask[r][c];

                        if (tileImages != null && value - 1 >= 0 && value - 1 < tileImages.length && tileImages[value - 1] != null) {
                            g.drawImage(tileImages[value - 1], x + 2, y + 2, tileSize - 4, tileSize - 4, this);
                            g.setColor(isCorrect ? Color.GREEN : Color.GRAY);
                            g.drawRect(x + 2, y + 2, tileSize - 4, tileSize - 4);
                            // Более толстая рамка для правильных
                            if (isCorrect) {
                                Graphics2D g2d = (Graphics2D) g;
                                Stroke oldStroke = g2d.getStroke();
                                g2d.setStroke(new BasicStroke(3f));
                                g2d.setColor(Color.GREEN);
                                g2d.drawRect(x + 2, y + 2, tileSize - 5, tileSize - 5);
                                g2d.setStroke(oldStroke);
                            }
                        } else {
                            g.setColor(new Color(220, 180, 80));
                            g.fillRect(x + 2, y + 2, tileSize - 4, tileSize - 4);
                            g.setColor(isCorrect ? Color.GREEN : Color.BLACK);
                            g.drawRect(x + 2, y + 2, tileSize - 4, tileSize - 4);
                            if (isCorrect) {
                                Graphics2D g2d = (Graphics2D) g;
                                Stroke oldStroke = g2d.getStroke();
                                g2d.setStroke(new BasicStroke(3f));
                                g2d.setColor(Color.GREEN);
                                g2d.drawRect(x + 2, y + 2, tileSize - 5, tileSize - 5);
                                g2d.setStroke(oldStroke);
                            }
                            g.setFont(new Font("Arial", Font.BOLD, tileSize / 2));
                            FontMetrics fm = g.getFontMetrics();
                            String text = String.valueOf(value);
                            int textX = x + (tileSize - fm.stringWidth(text)) / 2;
                            int textY = y + (tileSize + fm.getAscent() - fm.getDescent()) / 2;
                            g.setColor(Color.BLACK);
                            g.drawString(text, textX, textY);
                        }
                    }
                }
            }
        }
    }
}