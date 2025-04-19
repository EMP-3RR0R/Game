package com.wormfarm.minigames.fifteenpuzzle.ui.swing;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.awt.Toolkit;

public class SoundUtils {
    public static void playSound(String resourcePath) {
        try (InputStream audioSrc = SoundUtils.class.getResourceAsStream(resourcePath);
             InputStream bufferedIn = new BufferedInputStream(audioSrc)) {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(bufferedIn);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
        } catch (Exception e) {
            // Если не удалось — просто системный бип
            Toolkit.getDefaultToolkit().beep();
        }
    }
}