package com.wormfarm.core.logic;

import com.wormfarm.core.model.WormSaveData;
import com.wormfarm.core.model.WormState;
import com.wormfarm.core.model.WormStats;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class WormSaveManager {
    private static String saveDir = "saves";

    public static void setSaveDir(String dir) {
        saveDir = dir;
    }

    public static void save(WormState state, WormStats stats, int targetX, int targetY, String saveName) throws IOException {
        validateSaveName(saveName);
        File dir = new File(saveDir);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Failed to create save directory: " + saveDir);
        }
        File file = new File(dir, saveName + ".save");
        WormSaveData data = new WormSaveData(state, stats, targetX, targetY);
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(data);
        }
    }

    public static boolean hasSave(String saveName) {
        File file = new File(saveDir, saveName + ".save");
        return file.exists();
    }

    public static void load(WormState state, WormStats stats, WormSaveData.TargetConsumer targetConsumer, String saveName) throws IOException, ClassNotFoundException {
        File file = new File(saveDir, saveName + ".save");
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            WormSaveData data = (WormSaveData) in.readObject();
            data.applyTo(state, stats, targetConsumer);
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
        File file = new File(saveDir, saveName + ".save");
        if (file.exists()) file.delete();
    }

    private static void validateSaveName(String saveName) {
        if (!saveName.matches("[a-zA-Z0-9_\\-]+")) {
            throw new IllegalArgumentException("Invalid save name: " + saveName);
        }
    }
}