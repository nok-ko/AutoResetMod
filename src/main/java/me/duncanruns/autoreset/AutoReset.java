package me.duncanruns.autoreset;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Pair;
import net.minecraft.world.gen.GeneratorOptions;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

public class AutoReset implements ModInitializer {
    public static final String MOD_ID = "autoreset";
    public static final String MOD_NAME = "Auto Reset Mod";
    public static boolean isPlaying = false;
    public static Logger LOGGER = LogManager.getLogger();
    public static String seed = "";
    public static boolean isSetSeed;

    public static void log(Level level, String message) {
        LOGGER.log(level, "[" + MOD_NAME + "] " + message);
    }

    // this Scanner code is the least idiomatic Java I've ever written but it works, okay?!
    // I should really use regexes here
    public static String getLastSeed() throws IOException {
        String seed = "";
        File file = new File("attempts.txt");
        if (file.exists() && file.canRead()) {
            Scanner fileReader = new Scanner(file);
            String string = "";
            while (fileReader.hasNextLine()) {
                string = fileReader.nextLine();
            }
            string = string.trim();
            fileReader.close();
            if (string.contains(";") && string.length() > 1) {
                // The middle bit
                seed = string.substring(string.indexOf(";")+1, string.lastIndexOf(";"));
            }
        }
        return seed;
    }

    public static  Pair<Integer, String> getNextAttempt(long seedValue) {
        String category = AutoReset.isSetSeed ? "Set" : "Random";
        HashMap<String, Integer> attemptsPerCategory = new HashMap<>();
        try {
            File file = new File("attempts.txt");
            int attemptCount;
            if (file.exists()) {
                Scanner fileReader = new Scanner(file);
                String string;
                while (fileReader.hasNextLine()) {
                    string = fileReader.nextLine();
                    try {
                        attemptCount = Integer.parseInt(string.substring(0, string.indexOf(";")));
                    } catch (NumberFormatException ignored) {
                        attemptCount = 0;
                    }
                    attemptsPerCategory.put(string.substring(string.lastIndexOf(";")+1).trim(), attemptCount);
                }
                fileReader.close();
            }
            attemptCount = attemptsPerCategory.getOrDefault(category, 0);
            attemptCount++;
            FileWriter fileWriter = new FileWriter(file, true); // Append mode

            String seedOrRandom = AutoReset.isSetSeed ? AutoReset.seed : String.valueOf(seedValue);


            fileWriter.append(String.format("%d;%s;%s\n", attemptCount, seedOrRandom, category));
            fileWriter.close();
            return new Pair<>(attemptCount, category);
        } catch (IOException ignored) {
            return new Pair<>(-1, category);
        }
    }

    @Override
    public void onInitialize() {
        log(Level.INFO, "Initializing");
        try {
            seed = getLastSeed();
        } catch (IOException e) {
            e.initCause(new IOException("Catastrophically failed to load seed from file!"));
            e.printStackTrace();
        }
    }

}