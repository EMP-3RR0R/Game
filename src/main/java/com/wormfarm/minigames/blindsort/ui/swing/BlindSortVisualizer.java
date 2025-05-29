package com.wormfarm.minigames.blindsort.ui.swing;

import com.wormfarm.minigames.blindsort.api.BlindSortGame;
import com.wormfarm.minigames.blindsort.events.BlindSortEventListener;
import com.wormfarm.minigames.blindsort.logic.BlindSortLogic;
import com.wormfarm.minigames.blindsort.logic.BlindSortLogic.ArrayOwner;
import com.wormfarm.util.SoundUtils;
import com.wormfarm.settings.AppLocale;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

public class BlindSortVisualizer extends JPanel implements BlindSortEventListener {
    final BlindSortLogic gameLogic;
    BlindSortController controllerCallback;
    ResourceBundle messages;

    private final int cubeWidth;
    private final int cubeHeight;
    private final int cubeSpacing = 15;
    private final int rowSpacing = 50;
    private final int rowPadding = 30;
    private final int labelOffset = 20;

    Integer playerSelectedIndex = null;
    Integer playerHoveredIndex = null;

    final Map<ArrayOwner, AnimationState> animationStates = new ConcurrentHashMap<>();

    private final Color PLAYER_CUBE_COLOR = new Color(70, 130, 180);
    private final Color FAST_AI_CUBE_COLOR = new Color(255, 165, 0);
    private final Color SLOW_AI_CUBE_COLOR = new Color(100, 149, 237);
    private final Color TEXT_COLOR = Color.WHITE;
    private final Color SELECTED_BORDER_COLOR = Color.RED;
    private final Color DEFAULT_BORDER_COLOR = Color.DARK_GRAY;

    class AnimationState {
        Timer timer;
        int[] swappingIndices = null;
        float progress = 0f;
        ArrayOwner owner;
        Integer liftedIndex = null;
        float liftProgress = 0f;
        boolean lifting = false;

        int[][] numbersBeingSwapped;
        private int originalX1, originalY1, originalX2, originalY2;

        AnimationState(ArrayOwner owner) {
            this.owner = owner;
            this.timer = new Timer(16, e -> {
                if (swappingIndices != null) {
                    progress += 0.05f;
                    if (progress >= 1f) {
                        progress = 1f;
                        timer.stop();
                        swappingIndices = null;
                        numbersBeingSwapped = null;

                        if (owner == ArrayOwner.PLAYER) {
                            if (controllerCallback != null) {
                                controllerCallback.handlePlayerSwapAnimationFinished();
                            }
                        } else {
                            gameLogic.setAnimationInProgress(false, owner);
                        }
                    }
                } else if (liftedIndex != null) {
                    float speed = 0.08f;
                    if (lifting) {
                        liftProgress += speed;
                        if (liftProgress >= 1f) liftProgress = 1f;
                    } else {
                        liftProgress -= speed;
                        if (liftProgress <= 0f) {
                            liftProgress = 0f;
                            liftedIndex = null;
                            timer.stop();
                        }
                    }
                }
                repaint();
            });
        }

        public void startSwapAnimation(int idx1, int idx2) {
            this.swappingIndices = new int[]{idx1, idx2};
            this.numbersBeingSwapped = new int[2][];
            int[][] currentNumbers = gameLogic.getNumbersCopy(owner);
            this.numbersBeingSwapped[0] = Arrays.copyOf(currentNumbers[idx1], 4);
            this.numbersBeingSwapped[1] = Arrays.copyOf(currentNumbers[idx2], 4);

            originalX1 = (idx1 * (cubeWidth + cubeSpacing)) + cubeSpacing;
            originalY1 = owner.ordinal() * (cubeHeight + rowSpacing + labelOffset) + rowPadding + labelOffset + 5;
            originalX2 = (idx2 * (cubeWidth + cubeSpacing)) + cubeSpacing;
            originalY2 = owner.ordinal() * (cubeHeight + rowSpacing + labelOffset) + rowPadding + labelOffset + 5;

            this.progress = 0f;
            if (!timer.isRunning()) {
                timer.start();
            }
            if (owner != ArrayOwner.PLAYER) {
                gameLogic.setAnimationInProgress(true, owner);
            }
        }

        public void startLiftAnimation(int index, boolean lift) {
            if (liftedIndex != null && liftedIndex == index && this.lifting == lift) {
                return;
            }
            liftedIndex = index;
            lifting = lift;
            if (!timer.isRunning()) {
                timer.start();
            }
        }

        public void stopAnimation() {
            timer.stop();
            swappingIndices = null;
            numbersBeingSwapped = null;
            liftedIndex = null;
            progress = 0f;
            liftProgress = 0f;
            if (owner != ArrayOwner.PLAYER) {
                gameLogic.setAnimationInProgress(false, owner);
            }
        }
    }

    public BlindSortVisualizer(BlindSortLogic gameLogic, int width, int height) {
        this.gameLogic = gameLogic;
        this.messages = ResourceBundle.getBundle("com.wormfarm.gui.messages", AppLocale.getLocale());
        gameLogic.addEventListener(this);

        int numbersCount = gameLogic.getNumbersCopy().length;
        int totalHorizontalSpacing = (numbersCount + 1) * cubeSpacing;
        int baseCubeWidth = (width - totalHorizontalSpacing) / numbersCount;
        int baseCubeHeight = (height - (3 * labelOffset) - (2 * rowSpacing) - (2 * rowPadding)) / 3;
        this.cubeWidth = Math.max(30, (int) (baseCubeWidth * 0.85));
        this.cubeHeight = Math.max(40, (int) (baseCubeHeight * 0.85));

        setPreferredSize(new Dimension(width, height));
        setFocusable(true);
        setBackground(new Color(40, 44, 52));

        for (ArrayOwner owner : ArrayOwner.values()) {
            animationStates.put(owner, new AnimationState(owner));
        }

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
                handlePlayerHover(-1);
            }
        });
    }

    public void setControllerCallback(BlindSortController controllerCallback) {
        this.controllerCallback = controllerCallback;
    }

    public void handlePlayerHover(int newHoveredIndex) {
        if (gameLogic.isAnimationInProgress(ArrayOwner.PLAYER)) {
            if (playerHoveredIndex != null) {
                AnimationState playerAnimState = animationStates.get(ArrayOwner.PLAYER);
                if (playerAnimState.liftedIndex != null) {
                    playerAnimState.startLiftAnimation(playerAnimState.liftedIndex, false);
                }
                playerHoveredIndex = null;
            }
            repaint();
            return;
        }

        if (!gameLogic.isGameActive() || gameLogic.isPaused()) {
            if (playerHoveredIndex != null) {
                AnimationState playerAnimState = animationStates.get(ArrayOwner.PLAYER);
                if (playerAnimState.liftedIndex != null) {
                    playerAnimState.startLiftAnimation(playerAnimState.liftedIndex, false);
                }
                playerHoveredIndex = null;
            }
            repaint();
            return;
        }

        AnimationState playerAnimState = animationStates.get(ArrayOwner.PLAYER);

        if (playerSelectedIndex != null) {
            if (newHoveredIndex != -1 && newHoveredIndex != playerSelectedIndex) {
                if (playerHoveredIndex == null || playerHoveredIndex != newHoveredIndex) {
                    if (playerHoveredIndex != null) {
                        playerAnimState.startLiftAnimation(playerHoveredIndex, false);
                    }
                    playerAnimState.startLiftAnimation(newHoveredIndex, true);
                    playerHoveredIndex = newHoveredIndex;
                }
            } else if (playerHoveredIndex != null && (newHoveredIndex == -1 || newHoveredIndex == playerSelectedIndex)) {
                playerAnimState.startLiftAnimation(playerHoveredIndex, false);
                playerHoveredIndex = null;
            }
        } else {
            if (newHoveredIndex != -1) {
                if (playerHoveredIndex == null || playerHoveredIndex != newHoveredIndex) {
                    if (playerHoveredIndex != null) {
                        playerAnimState.startLiftAnimation(playerHoveredIndex, false);
                    }
                    playerAnimState.startLiftAnimation(newHoveredIndex, true);
                    playerHoveredIndex = newHoveredIndex;
                }
            } else if (playerHoveredIndex != null) {
                playerAnimState.startLiftAnimation(playerHoveredIndex, false);
                playerHoveredIndex = null;
            }
        }
        repaint();
    }

    public int getCubeWidth() { return cubeWidth; }
    public int getCubeHeight() { return cubeHeight; }
    public int getCubeSpacing() { return cubeSpacing; }
    public int getRowSpacing() { return rowSpacing; }
    public int getRowPadding() { return rowPadding; }
    public int getLabelOffset() { return labelOffset; }

    public void setPlayerSelectedIndex(Integer index) {
        if (index != null) {
            animationStates.get(ArrayOwner.PLAYER).startLiftAnimation(index, true);
            this.playerSelectedIndex = index;
            this.playerHoveredIndex = null;
        } else {
            if (this.playerSelectedIndex != null) {
                animationStates.get(ArrayOwner.PLAYER).startLiftAnimation(this.playerSelectedIndex, false);
            }
            this.playerSelectedIndex = null;
            this.playerHoveredIndex = null;
        }
        repaint();
    }

    public void startPlayerSwapAnimation(int idx1, int idx2) {
        animationStates.get(ArrayOwner.PLAYER).startSwapAnimation(idx1, idx2);
    }

    public void updateNumbersCache(ArrayOwner owner) {
        repaint();
    }

    int getCubeIndexAt(int mouseX, int mouseY, ArrayOwner owner) {
        int rowY = owner.ordinal() * (cubeHeight + rowSpacing + labelOffset) + rowPadding + labelOffset + 5;

        if (mouseY >= rowY && mouseY <= rowY + cubeHeight) {
            int x = mouseX - cubeSpacing;
            if (x < 0) return -1;
            int index = x / (cubeWidth + cubeSpacing);
            if (index >= 0 && index < gameLogic.getNumbersCopy(owner).length) {
                if (x % (cubeWidth + cubeSpacing) < cubeWidth) {
                    return index;
                }
            }
        }
        return -1;
    }

    String getStrategyName(String strategyKey) {
        return messages.getString("strategy." + strategyKey);
    }

    public void updateLocale() {
        this.messages = ResourceBundle.getBundle("com.wormfarm.gui.messages", AppLocale.getLocale());
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawArrayRow(g2d, ArrayOwner.PLAYER, 0, messages.getString("blindsort.player"));
        drawArrayRow(g2d, ArrayOwner.FAST_AI, 1, getStrategyName(gameLogic.getFastAIStrategyName()));
        drawArrayRow(g2d, ArrayOwner.SLOW_AI, 2, getStrategyName(gameLogic.getSlowAIStrategyName()));
    }

    private void drawArrayRow(Graphics2D g2d, ArrayOwner owner, int rowIndex, String label) {
        int yOffset = rowIndex * (cubeHeight + rowSpacing + labelOffset) + rowPadding;

        g2d.setColor(TEXT_COLOR);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString(label, cubeSpacing, yOffset + labelOffset);

        int visibleDigits = gameLogic.getVisibleDigitsForOwner(owner);
        AnimationState state = animationStates.get(owner);

        int[][] currentNumbers = gameLogic.getNumbersCopy(owner);

        for (int i = 0; i < currentNumbers.length; i++) {
            int[] digitsToDraw = currentNumbers[i];
            if (state.swappingIndices != null && state.owner == owner) {
                if (i == state.swappingIndices[0]) {
                    digitsToDraw = state.numbersBeingSwapped[0];
                } else if (i == state.swappingIndices[1]) {
                    digitsToDraw = state.numbersBeingSwapped[1];
                }
            }
            drawNumberCube(g2d, i, digitsToDraw, visibleDigits, state);
        }
    }

    private void drawNumberCube(Graphics2D g2d, int index, int[] digits,
                                int visibleDigits, AnimationState state) {
        int x = (index * (cubeWidth + cubeSpacing)) + cubeSpacing;
        int y = state.owner.ordinal() * (cubeHeight + rowSpacing + labelOffset) + rowPadding + labelOffset + 5;

        if (state.swappingIndices != null) {
            int idx1 = state.swappingIndices[0];
            int idx2 = state.swappingIndices[1];
            float p = state.progress;

            int currentX1 = (int) (state.originalX1 * (1 - p) + state.originalX2 * p);
            int currentX2 = (int) (state.originalX2 * (1 - p) + state.originalX1 * p);

            float arcHeight = cubeHeight * 1.0f;

            if (index == idx1) {
                x = currentX1;
                y = (int) (state.originalY1 - (arcHeight * Math.sin(p * Math.PI)));
            } else if (index == idx2) {
                x = currentX2;
                y = (int) (state.originalY2 + (arcHeight * Math.sin(p * Math.PI)));
            }
        }

        if (state.liftedIndex != null && state.liftedIndex == index) {
            if (state.swappingIndices == null) {
                float liftHeight = (float) cubeHeight * 0.2f;
                y -= (int) (liftHeight * state.liftProgress);
            }
        }

        Color cubeColor;
        switch (state.owner) {
            case PLAYER:
                cubeColor = PLAYER_CUBE_COLOR;
                break;
            case FAST_AI:
                cubeColor = FAST_AI_CUBE_COLOR;
                break;
            case SLOW_AI:
                cubeColor = SLOW_AI_CUBE_COLOR;
                break;
            default:
                cubeColor = Color.GRAY;
        }

        Color borderColor = DEFAULT_BORDER_COLOR;
        if (state.owner == ArrayOwner.PLAYER) {
            if (playerSelectedIndex != null && playerSelectedIndex == index) {
                borderColor = SELECTED_BORDER_COLOR;
            } else if (playerHoveredIndex != null && playerHoveredIndex == index) {
                if (state.swappingIndices == null) {
                    borderColor = SELECTED_BORDER_COLOR;
                }
            }
        }

        g2d.setColor(cubeColor);
        g2d.fillRoundRect(x, y, cubeWidth, cubeHeight, 15, 15);
        g2d.setColor(borderColor);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(x, y, cubeWidth, cubeHeight, 15, 15);

        g2d.setColor(TEXT_COLOR);
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        FontMetrics fm = g2d.getFontMetrics();
        int digitHeight = fm.getHeight();

        int currentOwnerVisibleDigits = gameLogic.getVisibleDigitsForOwner(state.owner);
        if (gameLogic.isFinalStage(state.owner)) {
            currentOwnerVisibleDigits = 4;
        }

        for (int i = 0; i < 4; i++) {
            String charToDraw;
            if (i < currentOwnerVisibleDigits) {
                charToDraw = String.valueOf(digits[i]);
            } else {
                charToDraw = "?";
            }
            int charWidth = fm.stringWidth(charToDraw);
            int charX = x + (cubeWidth / 4) * i + (cubeWidth / 8) - (charWidth / 2);
            int charY = y + (cubeHeight / 2) + (digitHeight / 3);
            g2d.drawString(charToDraw, charX, charY);
        }
    }

    @Override
    public void onSwap(int index1, int index2, boolean success, ArrayOwner owner) {
        if (success && owner != ArrayOwner.PLAYER) {
            animationStates.get(owner).startSwapAnimation(index1, index2);
        }
    }

    @Override
    public void onDigitRevealed(int newVisibleDigits, ArrayOwner owner) {
        repaint();
    }

    @Override
    public void onFinalStageStarted(ArrayOwner owner) {
        repaint();
    }

    @Override
    public void onWin() {
        resetAnimation();
    }

    @Override
    public void onLose() {
        resetAnimation();
    }

    public void resetAnimation() {
        playerSelectedIndex = null;
        playerHoveredIndex = null;
        for (AnimationState state : animationStates.values()) {
            state.stopAnimation();
        }
        repaint();
    }
}