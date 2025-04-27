package com.wormfarm.core.logic;

import com.wormfarm.core.model.WormSaveData;
import com.wormfarm.core.model.WormState;
import com.wormfarm.core.model.WormStats;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class WormSaveManager {
    private static final String SAVE_DIR = "saves";

    public static void save(WormState state, WormStats stats, String saveName) throws IOException {
        File dir = new File(SAVE_DIR);
        if (!dir.exists()) dir.mkdirs();
        File file = new File(dir, saveName + ".save");
        WormSaveData data = new WormSaveData(state, stats);
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(data);
        }
    }

    public static boolean hasSave(String saveName) {
        File file = new File(SAVE_DIR, saveName + ".save");
        return file.exists();
    }

    public static void load(WormState state, WormStats stats, String saveName) throws IOException, ClassNotFoundException {
        File file = new File(SAVE_DIR, saveName + ".save");
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            WormSaveData data = (WormSaveData) in.readObject();
            data.applyTo(state, stats);
        }
    }

    public static List<String> listSaves() {
        File dir = new File(SAVE_DIR);
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
        File file = new File(SAVE_DIR, saveName + ".save");
        if (file.exists()) file.delete();
    }
}