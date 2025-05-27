package com.wormfarm.util;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.awt.Toolkit;

public class SoundUtils {
    private static float volume = 1.0f; // 0.0...1.0

    public static void setVolume(float v) {
        volume = Math.max(0, Math.min(v, 1));
    }

    public static float getVolume() {
        return volume;
    }

    public static void playSound(String resourcePath) {
        try (InputStream audioSrc = SoundUtils.class.getResourceAsStream(resourcePath);
             InputStream bufferedIn = new BufferedInputStream(audioSrc)) {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(bufferedIn);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);

            try {
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float v = volume;
                float min = gainControl.getMinimum();
                float max = gainControl.getMaximum();
                float dB;
                if (v > 0.0f) {
                    dB = (float) (20.0 * Math.log10(v));
                    dB = Math.max(min, Math.min(dB, max));
                } else {
                    dB = min;
                }
                gainControl.setValue(dB);
            } catch (Exception ignored) {
            }

            clip.start();
        } catch (Exception e) {
            Toolkit.getDefaultToolkit().beep();
        }
    }
}