package Bots;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static Bots.Main.guildLocales;

public class LocaleManager {
    public static HashMap<String, HashMap<String, String>> languages = new HashMap<>();
    public static void init(JDA bot) {
        languages.put("english", readLocale("locales/en.txt"));
        languages.put("polski", readLocale("locales/pl.txt"));
        languages.put("nederlands", readLocale("locales/nl.txt"));
        languages.put("dansk", readLocale("locales/dk.txt"));
        languages.put("español", readLocale("locales/es.txt"));
        for (Guild g : bot.getGuilds()) {
            guildLocales.putIfAbsent(g.getIdLong(), languages.get("english"));
        }
    }

    private static HashMap<String, String> readLocale(String localeFile) {
        File file = new File(localeFile);
        List<String> lines = new ArrayList<>();
        try {
           lines = Files.readAllLines(Path.of(file.getAbsolutePath()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        HashMap<String, String> localeMap = new HashMap<>();

        for (String line : lines) {
            if (line.equals("\n") || line.startsWith("/") || !line.contains("=")) continue;

            String[] lineSplit = line.split("=", 2);
            if (lineSplit.length > 1) {
                localeMap.put(lineSplit[0], serializeString(lineSplit[1]));
            } else {
                System.err.println("Problematic locale: " + line);
            }
        }

        // missing key handler
        if (!localeFile.equals("locales/en.txt")) {
            boolean isMissing = false;
            for (String k : languages.get("english").keySet()) {
                if (!localeMap.containsKey(k)) {
                    if (!isMissing) {
                        System.err.println(localeFile + " seems to be missing some keys.");
                        isMissing = true;
                    }
                    System.err.println("MISSING KEY: " + k);
                    localeMap.put(k, languages.get("english").get(k)); // if the language is missing anything, fallback to english.
                }
            }
        }
        return localeMap;
    }

    static Pattern serializePattern = Pattern.compile("\\{(\\d+)}");

    // for use in CommandEvent.localise or when the lang map has to be passed in manually.
    public static String managerLocalise(String key, Map<String, String> lang, Object... args) {
        String localisedString = lang.get(key);
        localisedString = localisedString.replaceAll("\\\\n", "\n");
        if (args.length != 0) return String.format(localisedString, args);
        return localisedString;
    }

    private static String serializeString(String localeInput) {
        String[] localeStrings = localeInput.split("//");
        localeInput = localeInput.replace("//" + localeStrings[localeStrings.length-1], "").trim();
        localeInput = localeInput.replaceAll("\\{nl}", "\n");

        Matcher matcher = serializePattern.matcher(localeInput);

        while (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                try {
                    String match = matcher.group(i);
                    localeInput = localeInput.replace("{" + match + "}", "%" + match + "$s");
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println("Issue: " + localeInput);
                }
            }
        }
        return localeInput;
    }

}
