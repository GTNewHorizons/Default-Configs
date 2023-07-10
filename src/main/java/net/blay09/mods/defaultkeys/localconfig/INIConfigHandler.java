package net.blay09.mods.defaultkeys.localconfig;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class INIConfigHandler {

    private static final Logger logger = LogManager.getLogger();

    public static void backup(PrintWriter writer, List<LocalConfigEntry> entries, File configFile) {
        boolean[] foundProperty = new boolean[entries.size()];
        List<LocalConfigEntry> notEntries = new ArrayList<>();
        for (LocalConfigEntry entry : entries) {
            if (entry.not) {
                notEntries.add(entry);
            }
        }
        String category = "";
        boolean isInQuotes = false;
        try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
            String line;
            lineLoop: while ((line = reader.readLine()) != null) {
                StringBuilder buffer = new StringBuilder();
                charLoop: for (int i = 0; i < line.length(); i++) {
                    char c = line.charAt(i);
                    if (isInQuotes) {
                        if (c == '"') {
                            isInQuotes = false;
                        }
                        buffer.append(c);
                    } else {
                        String name;
                        switch (c) {
                            case ';':
                                break charLoop;
                            case '"':
                                isInQuotes = true;
                                buffer.append(c);
                                break;
                            case '[':
                                buffer = new StringBuilder();
                                break;
                            case ']':
                                category = buffer.toString();
                                buffer = new StringBuilder();
                                break;
                            case '=':
                                name = buffer.toString()
                                    .trim();
                                String value = line.substring(i + 1)
                                    .trim();
                                for (int j = 0; j < entries.size(); j++) {
                                    LocalConfigEntry entry = entries.get(j);
                                    if (entry.passesProperty(category, name, "*")) {
                                        foundProperty[j] = true;
                                        if (entry.containsWildcard()) {
                                            for (LocalConfigEntry notEntry : notEntries) {
                                                if (notEntry.passesProperty(category, name, "*")) {
                                                    continue lineLoop;
                                                }
                                            }
                                        }
                                        writer.println(
                                            entry.getIdentifier(entry.file, category, "*", name) + "=" + value);
                                        break;
                                    }
                                }
                                continue lineLoop;
                            default:
                                buffer.append(c);
                        }
                    }
                }
            }
            for (int i = 0; i < foundProperty.length; i++) {
                if (!foundProperty[i] && !entries.get(i).not) {
                    logger.warn(
                        "Failed to backup local value {}: property not found",
                        entries.get(i)
                            .getIdentifier());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void restore(List<LocalConfigEntry> entries, File configFile) {
        boolean[] foundProperty = new boolean[entries.size()];
        List<LocalConfigEntry> notEntries = new ArrayList<>();
        for (LocalConfigEntry entry : entries) {
            if (entry.not) {
                notEntries.add(entry);
            }
        }
        File backupFile = new File(configFile.getParentFile(), configFile.getName() + ".bak");
        try {
            FileUtils.copyFile(configFile, backupFile);
        } catch (IOException e) {
            logger.error("Could not create backup file {}: {}", backupFile, e);
        }
        try {
            List<String> lines = FileUtils.readLines(configFile);
            try (PrintWriter writer = new PrintWriter(configFile)) {
                String category = "";
                boolean isInQuotes = false;
                for (String line : lines) {
                    StringBuilder buffer = new StringBuilder();
                    charLoop: for (int i = 0; i < line.length(); i++) {
                        char c = line.charAt(i);
                        if (isInQuotes) {
                            if (c == '"') {
                                isInQuotes = false;
                            }
                            buffer.append(c);
                        } else {
                            String name;
                            switch (c) {
                                case ';':
                                    break charLoop;
                                case '"':
                                    isInQuotes = true;
                                    buffer.append(c);
                                    break;
                                case '[':
                                    buffer = new StringBuilder();
                                    break;
                                case ']':
                                    category = buffer.toString();
                                    buffer = new StringBuilder();
                                    break;
                                case '=':
                                    name = buffer.toString()
                                        .trim();
                                    for (int j = 0; j < entries.size(); j++) {
                                        LocalConfigEntry entry = entries.get(j);
                                        if (entry.passesProperty(category, name, "*")) {
                                            foundProperty[j] = true;
                                            if (entry.containsWildcard()) {
                                                for (LocalConfigEntry notEntry : notEntries) {
                                                    if (notEntry.passesProperty(category, name, "*")) {
                                                        break charLoop;
                                                    }
                                                }
                                            }
                                            line = line.substring(0, i) + "=" + entry.value;
                                            break;
                                        }
                                    }
                                    break charLoop;
                                default:
                                    buffer.append(c);
                            }
                        }
                    }
                    writer.println(line);
                }
            }
            for (int i = 0; i < foundProperty.length; i++) {
                if (!foundProperty[i] && !entries.get(i).not) {
                    logger.warn(
                        "Failed to restore local value {}: property not found",
                        entries.get(i)
                            .getIdentifier());
                }
            }
        } catch (IOException e) {
            logger.error("Failed to restore local values in {}: {}", configFile, e);
            try {
                FileUtils.copyFile(backupFile, configFile);
            } catch (IOException e2) {
                logger.error("Could not restore config file {} from backup: {}", configFile, e2);
            }
        }
    }

}
