package com.cryo.utils;

import org.apache.commons.validator.routines.UrlValidator;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;

public class Utilities {

    public static final String[] SKILL_NAME = {"Attack", "Defence", "Strength", "Constitution", "Ranged",
            "Prayer", "Magic", "Cooking", "Woodcutting", "Fletching", "Fishing", "Firemaking", "Crafting",
            "Smithing", "Mining", "Herblore", "Agility", "Thieving", "Slayer", "Farming", "Runecrafting",
            "Hunter", "Construction", "Summoning", "Dungeoneering"};

    public static final String[] EMOJIS = {"<:attack:565821919094374411>", "<:defence:565821953332740108>",
            "<:strength:565821989680447488>", "<:constitution:565822021120819200>", "<:ranged:565822058102259713>",
            "<:prayer:565822083104505866>"};

    private static Random RANDOM = new Random();

    private static String[] SCHEMES = {"http", "https"};

    private static UrlValidator validator;

    static {
        validator = new UrlValidator(SCHEMES);
    }

    public static int getSkill(String name) {
        for (int i = 0; i < SKILL_NAME.length; i++) {
            if (SKILL_NAME[i].equalsIgnoreCase(name)) return i;
        }
        return -1;
    }

    public static String getItemPicture(String itemName) {
        return null;
    }

    public static boolean isValidURL(String url) {
        return validator.isValid(url);
    }

    public static final int random(int maxValue) {
        if (maxValue <= 0) return 0;
        return RANDOM.nextInt(maxValue);
    }

    @SuppressWarnings({"rawtypes"})
    public static Class[] getClasses(String packageName) throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<File>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile().replaceAll("%20", " ")));
        }
        ArrayList<Class> classes = new ArrayList<Class>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        return classes.toArray(new Class[classes.size()]);
    }

    @SuppressWarnings("rawtypes")
    private static List<Class> findClasses(File directory, String packageName) {
        List<Class> classes = new ArrayList<Class>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                try {
                    classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
                } catch (Throwable e) {

                }
            }
        }
        return classes;
    }
}
