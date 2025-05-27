package com.wormfarm.minigames.fifteenpuzzle.ui.swing;

import org.junit.jupiter.api.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.image.BufferedImage;
import java.lang.reflect.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class FifteenPuzzleVisualizerTest {
    static final int SIZE = 4;
    static final int TILE_SIZE = 40;

    static class DummyLogic {
        boolean[][] mask;
        DummyLogic(boolean[][] mask) { this.mask = mask; }
        public boolean[][] getCorrectTilesMask() { return mask; }
    }

    FifteenPuzzleVisualizer visualizer;
    int[][] solvedBoard;

    @BeforeEach
    void setUp() {
        solvedBoard = new int[][]{
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9,10,11,12},
                {13,14,15,0}
        };
        visualizer = new FifteenPuzzleVisualizer(SIZE, TILE_SIZE, new DummyLogic(new boolean[SIZE][SIZE]));
    }

    @Test
    void testPreferredSize() {
        Dimension dim = visualizer.getPreferredSize();
        assertEquals(new Dimension(SIZE*TILE_SIZE, SIZE*TILE_SIZE), dim);
    }

    @Test
    void testSetAndGetBoard() throws Exception {
        visualizer.setBoard(solvedBoard);
        Field f = visualizer.getClass().getDeclaredField("board");
        f.setAccessible(true);
        assertArrayEquals(solvedBoard, (int[][]) f.get(visualizer));
    }

    @Test
    void testClearSpritesResetsFields() throws Exception {
        Field spritesF = visualizer.getClass().getDeclaredField("loadedSprites");
        spritesF.setAccessible(true);
        List<BufferedImage> sprites = (List<BufferedImage>) spritesF.get(visualizer);
        sprites.add(new BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB));
        Field imagesF = visualizer.getClass().getDeclaredField("tileImages");
        imagesF.setAccessible(true);
        imagesF.set(visualizer, new BufferedImage[1]);
        Field idxF = visualizer.getClass().getDeclaredField("currentSpriteIndex");
        idxF.setAccessible(true);
        idxF.setInt(visualizer, 42);
        visualizer.clearSprites();
        assertTrue(((List<?>) spritesF.get(visualizer)).isEmpty());
        assertNull(imagesF.get(visualizer));
        assertEquals(-1, idxF.getInt(visualizer));
    }

    @Test
    void testPickRandomSpriteHandlesEmptySprites() throws Exception {
        Field spritesF = visualizer.getClass().getDeclaredField("loadedSprites");
        spritesF.setAccessible(true);
        ((List<?>) spritesF.get(visualizer)).clear();

        Method m = visualizer.getClass().getDeclaredMethod("pickRandomSprite");
        m.setAccessible(true);
        assertDoesNotThrow(() -> m.invoke(visualizer));

        Field idxF = visualizer.getClass().getDeclaredField("currentSpriteIndex");
        idxF.setAccessible(true);
        assertEquals(-1, idxF.getInt(visualizer));
    }

    @Test
    void testResetPuzzleImageDoesNotThrow() {
        assertDoesNotThrow(() -> visualizer.resetPuzzleImage());
    }

    @Test
    void testLoadAllSpritesDoesNotThrow() throws Exception {
        Method m = visualizer.getClass().getDeclaredMethod("loadAllSprites");
        m.setAccessible(true);
        assertDoesNotThrow(() -> m.invoke(visualizer));
    }

    @Test
    void testUpdateTilePositionsPopulatesTilePositions() throws Exception {
        visualizer.setBoard(solvedBoard);
        Field tilePositionsF = visualizer.getClass().getDeclaredField("tilePositions");
        tilePositionsF.setAccessible(true);
        Map<Integer, Point> positions = (Map<Integer, Point>) tilePositionsF.get(visualizer);
        assertEquals(15, positions.size());
        Point p = positions.get(1);
        assertEquals(0, p.x);
        assertEquals(0, p.y);
    }

    @Test
    void testAnimateMoveSetsAnimatingTrueAndEnds() throws Exception {
        visualizer.setBoard(solvedBoard);
        CountDownLatch latch = new CountDownLatch(1);
        SwingUtilities.invokeLater(() -> visualizer.animateMove(15, 3, 2, 3, 3, latch::countDown));
        assertTrue(latch.await(2, TimeUnit.SECONDS), "Callback не вызвался!");
        assertFalse(visualizer.isAnimating());
    }

    @Test
    void testAnimateMoveIgnoresWhenAnimating() {
        visualizer.setBoard(solvedBoard);
        try {
            Field animF = visualizer.getClass().getDeclaredField("animating");
            animF.setAccessible(true);
            animF.setBoolean(visualizer, true);
            CountDownLatch latch = new CountDownLatch(1);
            visualizer.animateMove(15, 3, 2, 3, 3, latch::countDown);
            assertEquals(1, latch.getCount()); // callback не вызвался
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void testAnimateShakeNoException() {
        visualizer.setBoard(solvedBoard);
        assertDoesNotThrow(() -> visualizer.animateShake(1));
    }
    @Test
    void testAnimateShakeDoesNotRepeatForSameTile() throws Exception {
        visualizer.setBoard(solvedBoard);
        visualizer.animateShake(1);
        Field shakeOffsetsF = visualizer.getClass().getDeclaredField("shakeOffsets");
        shakeOffsetsF.setAccessible(true);
        int before = ((Map<?,?>)shakeOffsetsF.get(visualizer)).size();
        visualizer.animateShake(1);
        int after = ((Map<?,?>)shakeOffsetsF.get(visualizer)).size();
        assertEquals(before, after);
    }

    @Test
    void testPaintComponentRunsWithNullBoardAndNoSprite() {
        BufferedImage img = new BufferedImage(TILE_SIZE*SIZE, TILE_SIZE*SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        assertDoesNotThrow(() -> visualizer.paintComponent(g));
    }

    @Test
    void testPaintComponentRunsWithBoardAndNoSprite() {
        visualizer.setBoard(solvedBoard);
        BufferedImage img = new BufferedImage(TILE_SIZE*SIZE, TILE_SIZE*SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        assertDoesNotThrow(() -> visualizer.paintComponent(g));
    }

    @Test
    void testPaintComponentWithNonNullTileImages() throws Exception {
        visualizer.setBoard(solvedBoard);
        Field imagesF = visualizer.getClass().getDeclaredField("tileImages");
        imagesF.setAccessible(true);
        BufferedImage[] arr = new BufferedImage[SIZE*SIZE];
        for (int i = 0; i < arr.length-1; ++i)
            arr[i] = new BufferedImage(TILE_SIZE, TILE_SIZE, BufferedImage.TYPE_INT_ARGB);
        arr[arr.length-1] = null;
        imagesF.set(visualizer, arr);
        BufferedImage img = new BufferedImage(TILE_SIZE*SIZE, TILE_SIZE*SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        assertDoesNotThrow(() -> visualizer.paintComponent(g));
    }

    @Test
    void testPaintComponentWithCorrectMask() {
        boolean[][] mask = new boolean[SIZE][SIZE];
        mask[0][0] = true; mask[3][2] = true;
        FifteenPuzzleVisualizer v = new FifteenPuzzleVisualizer(SIZE, TILE_SIZE, new DummyLogic(mask));
        v.setBoard(solvedBoard);
        BufferedImage img = new BufferedImage(TILE_SIZE*SIZE, TILE_SIZE*SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        assertDoesNotThrow(() -> v.paintComponent(g));
    }

    @Test
    void testPaintComponentWithNullLogicDoesNotCrash() {
        FifteenPuzzleVisualizer v = new FifteenPuzzleVisualizer(SIZE, TILE_SIZE, null);
        v.setBoard(solvedBoard);
        BufferedImage img = new BufferedImage(TILE_SIZE*SIZE, TILE_SIZE*SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        assertDoesNotThrow(() -> v.paintComponent(g));
    }

    @Test
    void testPaintComponentWithLogicThrowingException() {
        Object badLogic = new Object() {
            public boolean[][] getCorrectTilesMask() { throw new RuntimeException("fail"); }
        };
        FifteenPuzzleVisualizer v = new FifteenPuzzleVisualizer(SIZE, TILE_SIZE, badLogic);
        v.setBoard(solvedBoard);
        BufferedImage img = new BufferedImage(TILE_SIZE*SIZE, TILE_SIZE*SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        assertDoesNotThrow(() -> v.paintComponent(g));
    }

    @Test
    void testResizeTextureProducesCorrectSize() throws Exception {
        Method m = visualizer.getClass().getDeclaredMethod("resizeTexture", BufferedImage.class, int.class, int.class);
        m.setAccessible(true);
        BufferedImage src = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        BufferedImage out = (BufferedImage) m.invoke(null, src, 16, 16);
        assertEquals(16, out.getWidth());
        assertEquals(16, out.getHeight());
    }

    @Test
    void testHierarchyListenerClearsSprites() throws Exception {
        visualizer.setBoard(solvedBoard);
        Field spritesF = visualizer.getClass().getDeclaredField("loadedSprites");
        spritesF.setAccessible(true);
        List<BufferedImage> sprites = (List<BufferedImage>) spritesF.get(visualizer);
        sprites.add(new BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB));
        for (HierarchyListener hl : visualizer.getHierarchyListeners()) {
            hl.hierarchyChanged(new HierarchyEvent(visualizer, HierarchyEvent.DISPLAYABILITY_CHANGED, visualizer, visualizer, HierarchyEvent.DISPLAYABILITY_CHANGED));
        }
        assertTrue(sprites.isEmpty());
    }

    @Test
    void testPickRandomSpriteWithOneSprite() throws Exception {
        Field spritesF = visualizer.getClass().getDeclaredField("loadedSprites");
        spritesF.setAccessible(true);
        List<BufferedImage> sprites = (List<BufferedImage>) spritesF.get(visualizer);
        BufferedImage img = new BufferedImage(80, 80, BufferedImage.TYPE_INT_ARGB);
        sprites.add(img);
        Method m = visualizer.getClass().getDeclaredMethod("pickRandomSprite");
        m.setAccessible(true);
        assertDoesNotThrow(() -> m.invoke(visualizer));
    }

    @Test
    void testSetBoardWhileAnimatingDoesNotUpdatePositions() throws Exception {
        Field animF = visualizer.getClass().getDeclaredField("animating");
        animF.setAccessible(true);
        animF.setBoolean(visualizer, true);
        Field positionsF = visualizer.getClass().getDeclaredField("tilePositions");
        positionsF.setAccessible(true);
        Map<Integer, Point> before = new HashMap<>();
        before.put(1, new Point(123, 45));
        positionsF.set(visualizer, before);
        visualizer.setBoard(solvedBoard);
        assertEquals(before, positionsF.get(visualizer));
    }

    @Test
    void testAnimateMoveCallbackNotNull() throws Exception {
        visualizer.setBoard(solvedBoard);
        CountDownLatch latch = new CountDownLatch(1);
        visualizer.animateMove(15, 3, 2, 3, 3, latch::countDown);
        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }

    @Test
    void testAnimateMoveCallbackNullDoesNotThrow() {
        visualizer.setBoard(solvedBoard);
        assertDoesNotThrow(() -> visualizer.animateMove(15, 3, 2, 3, 3, null));
    }
}