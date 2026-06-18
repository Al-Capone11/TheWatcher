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
    private static boolean fakeCrashEnabled = true;
    private static boolean actionEchoesEnabled = true;
    private static boolean itemHauntingEnabled = true;
    private static boolean hotbarDriftEnabled = true;
    private static boolean environmentalEventsEnabled = true;
    private static boolean animalStaringEnabled = true;

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
        } catch (IOException ignored) {
            // Keep safe defaults if the config file cannot be read or written.
        }

        fearIncreaseMultiplier = readDouble(properties, "fearIncreaseMultiplier", 1.0D, 0.0D, 10.0D);
        fearDecreaseMultiplier = readDouble(properties, "fearDecreaseMultiplier", 1.0D, 0.0D, 10.0D);
        fakeCrashEnabled = readBoolean(properties, "fakeCrashEnabled", true);
        actionEchoesEnabled = readBoolean(properties, "actionEchoesEnabled", true);
        itemHauntingEnabled = readBoolean(properties, "itemHauntingEnabled", true);
        hotbarDriftEnabled = readBoolean(properties, "hotbarDriftEnabled", true);
        environmentalEventsEnabled = readBoolean(properties, "environmentalEventsEnabled", true);
        animalStaringEnabled = readBoolean(properties, "animalStaringEnabled", true);
    }

    public static double fearIncreaseMultiplier() { load(); return fearIncreaseMultiplier; }
    public static double fearDecreaseMultiplier() { load(); return fearDecreaseMultiplier; }
    public static boolean fakeCrashEnabled() { load(); return fakeCrashEnabled; }
    public static boolean actionEchoesEnabled() { load(); return actionEchoesEnabled; }
    public static boolean itemHauntingEnabled() { load(); return itemHauntingEnabled; }
    public static boolean hotbarDriftEnabled() { load(); return hotbarDriftEnabled; }
    public static boolean environmentalEventsEnabled() { load(); return environmentalEventsEnabled; }
    public static boolean animalStaringEnabled() { load(); return animalStaringEnabled; }

    private static Properties defaultProperties() {
        Properties properties = new Properties();
        properties.setProperty("fearIncreaseMultiplier", "1.0");
        properties.setProperty("fearDecreaseMultiplier", "1.0");
        properties.setProperty("fakeCrashEnabled", "true");
        properties.setProperty("actionEchoesEnabled", "true");
        properties.setProperty("itemHauntingEnabled", "true");
        properties.setProperty("hotbarDriftEnabled", "true");
        properties.setProperty("environmentalEventsEnabled", "true");
        properties.setProperty("animalStaringEnabled", "true");
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
}
