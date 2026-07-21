package com.josem.thewatcher.game;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class TheWatcherConfig {
    private static final Path CONFIG_PATH = Path.of("config", "thewatcher-common.properties");

    private static boolean loaded;
    private static double fearIncreaseMultiplier = 1.0D;
    private static double fearDecreaseMultiplier = 1.0D;
    private static double torchFearReduction = 0.4D;
    private static boolean fakeCrashEnabled = true;
    private static boolean actionEchoesEnabled = true;
    private static boolean itemHauntingEnabled = true;
    private static boolean hotbarDriftEnabled = true;
    private static boolean environmentalEventsEnabled = true;
    private static boolean animalStaringEnabled = true;
    private static boolean shadowEnabled = true;
    private static int shadowFearThreshold = 100;
    private static String fearBarAnchor = "RIGHT";
    private static int fearBarYOffset = 0;

    private TheWatcherConfig() {}

    public static synchronized void load() {
        if (loaded) return;
        loaded = true;

        Properties properties = defaultProperties();
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            if (Files.exists(CONFIG_PATH)) {
                try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                    properties.load(reader);
                }
            } else {
                try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                    properties.store(writer, "The Watcher common config");
                }
            }
        } catch (IOException ignored) {}

        fearIncreaseMultiplier = readDouble(properties, "fearIncreaseMultiplier", 1.0D, 0.0D, 10.0D);
        fearDecreaseMultiplier = readDouble(properties, "fearDecreaseMultiplier", 1.0D, 0.0D, 10.0D);
        torchFearReduction = readDouble(properties, "torchFearReduction", 0.4D, 0.0D, 1.0D);
        fakeCrashEnabled = readBoolean(properties, "fakeCrashEnabled", true);
        actionEchoesEnabled = readBoolean(properties, "actionEchoesEnabled", true);
        itemHauntingEnabled = readBoolean(properties, "itemHauntingEnabled", true);
        hotbarDriftEnabled = readBoolean(properties, "hotbarDriftEnabled", true);
        environmentalEventsEnabled = readBoolean(properties, "environmentalEventsEnabled", true);
        animalStaringEnabled = readBoolean(properties, "animalStaringEnabled", true);
        shadowEnabled = readBoolean(properties, "shadowEnabled", true);
        shadowFearThreshold = readInt(properties, "shadowFearThreshold", 100);
        fearBarAnchor = properties.getProperty("fearBarAnchor", "RIGHT");
        fearBarYOffset = readInt(properties, "fearBarYOffset", 0);
    }

    public static synchronized void save() {
        Properties properties = new Properties();
        properties.setProperty("fearIncreaseMultiplier", Double.toString(fearIncreaseMultiplier));
        properties.setProperty("fearDecreaseMultiplier", Double.toString(fearDecreaseMultiplier));
        properties.setProperty("torchFearReduction", Double.toString(torchFearReduction));
        properties.setProperty("fakeCrashEnabled", Boolean.toString(fakeCrashEnabled));
        properties.setProperty("actionEchoesEnabled", Boolean.toString(actionEchoesEnabled));
        properties.setProperty("itemHauntingEnabled", Boolean.toString(itemHauntingEnabled));
        properties.setProperty("hotbarDriftEnabled", Boolean.toString(hotbarDriftEnabled));
        properties.setProperty("environmentalEventsEnabled", Boolean.toString(environmentalEventsEnabled));
        properties.setProperty("animalStaringEnabled", Boolean.toString(animalStaringEnabled));
        properties.setProperty("shadowEnabled", Boolean.toString(shadowEnabled));
        properties.setProperty("shadowFearThreshold", Integer.toString(shadowFearThreshold));
        properties.setProperty("fearBarAnchor", fearBarAnchor);
        properties.setProperty("fearBarYOffset", Integer.toString(fearBarYOffset));

        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                properties.store(writer, "The Watcher common config");
            }
        } catch (IOException ignored) {}
    }

    public static double fearIncreaseMultiplier() { load(); return fearIncreaseMultiplier; }
    public static double fearDecreaseMultiplier() { load(); return fearDecreaseMultiplier; }
    public static double torchFearReduction() { load(); return torchFearReduction; }
    public static boolean fakeCrashEnabled() { load(); return fakeCrashEnabled; }
    public static boolean actionEchoesEnabled() { load(); return actionEchoesEnabled; }
    public static boolean itemHauntingEnabled() { load(); return itemHauntingEnabled; }
    public static boolean hotbarDriftEnabled() { load(); return hotbarDriftEnabled; }
    public static boolean environmentalEventsEnabled() { load(); return environmentalEventsEnabled; }
    public static boolean animalStaringEnabled() { load(); return animalStaringEnabled; }
    public static boolean shadowEnabled() { load(); return shadowEnabled; }
    public static int shadowFearThreshold() { load(); return shadowFearThreshold; }
    public static String fearBarAnchor() { load(); return fearBarAnchor; }
    public static int fearBarYOffset() { load(); return fearBarYOffset; }

    public static void setFearIncreaseMultiplier(double v) { fearIncreaseMultiplier = v; }
    public static void setFearDecreaseMultiplier(double v) { fearDecreaseMultiplier = v; }
    public static void setTorchFearReduction(double v) { torchFearReduction = v; }
    public static void setFakeCrashEnabled(boolean v) { fakeCrashEnabled = v; }
    public static void setActionEchoesEnabled(boolean v) { actionEchoesEnabled = v; }
    public static void setItemHauntingEnabled(boolean v) { itemHauntingEnabled = v; }
    public static void setHotbarDriftEnabled(boolean v) { hotbarDriftEnabled = v; }
    public static void setEnvironmentalEventsEnabled(boolean v) { environmentalEventsEnabled = v; }
    public static void setAnimalStaringEnabled(boolean v) { animalStaringEnabled = v; }
    public static void setShadowEnabled(boolean v) { shadowEnabled = v; }
    public static void setShadowFearThreshold(int v) { shadowFearThreshold = Math.max(1, Math.min(100, v)); }
    public static void setFearBarAnchor(String v) { fearBarAnchor = v; }
    public static void setFearBarYOffset(int v) { fearBarYOffset = v; }

    private static Properties defaultProperties() {
        Properties properties = new Properties();
        properties.setProperty("fearIncreaseMultiplier", "1.0");
        properties.setProperty("fearDecreaseMultiplier", "1.0");
        properties.setProperty("torchFearReduction", "0.4");
        properties.setProperty("fakeCrashEnabled", "true");
        properties.setProperty("actionEchoesEnabled", "true");
        properties.setProperty("itemHauntingEnabled", "true");
        properties.setProperty("hotbarDriftEnabled", "true");
        properties.setProperty("environmentalEventsEnabled", "true");
        properties.setProperty("animalStaringEnabled", "true");
        properties.setProperty("shadowEnabled", "true");
        properties.setProperty("shadowFearThreshold", "100");
        properties.setProperty("fearBarAnchor", "RIGHT");
        properties.setProperty("fearBarYOffset", "0");
        return properties;
    }

    private static double readDouble(Properties properties, String key, double fallback, double min, double max) {
        try {
            double value = Double.parseDouble(properties.getProperty(key, Double.toString(fallback)).trim());
            return Math.max(min, Math.min(max, value));
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static boolean readBoolean(Properties properties, String key, boolean fallback) {
        String value = properties.getProperty(key);
        return value == null ? fallback : Boolean.parseBoolean(value.trim());
    }

    private static int readInt(Properties properties, String key, int fallback) {
        try {
            return Integer.parseInt(properties.getProperty(key, Integer.toString(fallback)).trim());
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }
}
