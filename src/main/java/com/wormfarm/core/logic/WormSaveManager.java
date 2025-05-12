package com.wormfarm.core.logic;

import com.wormfarm.core.model.WormState;
import com.wormfarm.core.model.WormStats;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class WormSaveManager {
    private static String saveDir = "saves";

    public static void setSaveDir(String dir) {
        saveDir = dir;
    }

    // Для обычных пользовательских сейвов по имени
    public static void save(WormState state, WormStats stats, int targetX, int targetY, String saveName) throws IOException {
        validateSaveName(saveName);
        File file = new File(saveDir, saveName + ".save");
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(state);
            oos.writeObject(stats);
            oos.writeInt(targetX);
            oos.writeInt(targetY);
        }
    }

    // Для автосейва по абсолютному пути, без проверки имени
    public static void saveToAbsolutePath(WormState state, WormStats stats, int targetX, int targetY, String filePath) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(state);
            oos.writeObject(stats);
            oos.writeInt(targetX);
            oos.writeInt(targetY);
        }
    }

    public static boolean hasSave(String saveName) {
        File file = new File(saveDir, saveName + ".save");
        return file.exists();
    }

    // Для обычных пользовательских сейвов по имени
    public static void load(WormState state, WormStats stats, BiConsumer<Integer, Integer> targetSetter, String saveName) throws IOException, ClassNotFoundException {
        validateSaveName(saveName);
        File file = new File(saveDir, saveName + ".save");
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            WormState loadedState = (WormState) ois.readObject();
            WormStats loadedStats = (WormStats) ois.readObject();
            int x = ois.readInt();
            int y = ois.readInt();
            state.copyFrom(loadedState);
            stats.copyFrom(loadedStats);
            targetSetter.accept(x, y);
        }
    }

    // Для автосейва по абсолютному пути, без проверки имени
    public static void loadFromAbsolutePath(WormState state, WormStats stats, BiConsumer<Integer, Integer> targetSetter, String filePath) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            WormState loadedState = (WormState) ois.readObject();
            WormStats loadedStats = (WormStats) ois.readObject();
            int x = ois.readInt();
            int y = ois.readInt();
            state.copyFrom(loadedState);
            stats.copyFrom(loadedStats);
            targetSetter.accept(x, y);
        }
    }

    public static List<String> listSaves() {
        File dir = new File(saveDir);
        List<String> saves = new ArrayList<>();
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles((d, name) -> name.endsWith(".save"));
            if (files != null) {
                for (File f : files) {
                    String name = f.getName();
                    if (name.endsWith(".save")) {
                        saves.add(name.substring(0, name.length() - 5)); // remove .save
                    }
                }
            }
        }
        return saves;
    }

    public static void deleteSave(String saveName) {
        validateSaveName(saveName);
        File file = new File(saveDir, saveName + ".save");
        if (file.exists()) file.delete();
    }

    private static void validateSaveName(String saveName) {
        if (!saveName.matches("[a-zA-Z0-9_\\-]+")) {
            throw new IllegalArgumentException("Invalid save name: " + saveName);
        }
    }
}