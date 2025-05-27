package com.wormfarm.settings;

import java.io.*;

public class UserSettings implements Serializable {
    private static final long serialVersionUID = 1L;

    private String language = "en";
    private float volume = 1.0f;

    public String getLanguage() { return language; }
    public void setLanguage(String lang) { language = lang; }

    public float getVolume() { return volume; }
    public void setVolume(float v) { volume = Math.max(0, Math.min(v, 1)); }

    public static void save(UserSettings settings, File file) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(settings);
        }
    }

    public static UserSettings load(File file) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (UserSettings) ois.readObject();
        }
    }
}