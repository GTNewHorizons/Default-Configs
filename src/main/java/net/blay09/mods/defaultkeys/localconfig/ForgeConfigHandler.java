package net.blay09.mods.defaultkeys.localconfig;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ForgeConfigHandler {

    private static final Logger logger = LogManager.getLogger();

    public static void backup(PrintWriter writer, List<LocalConfigEntry> entries, File configFile) {
        boolean[] foundProperty = new boolean[entries.size()];
        List<LocalConfigEntry> notEntries = new ArrayList<>();
        for (LocalConfigEntry entry : entries) {
            if (entry.not) {
                notEntries.add(entry);
            }
        }
        List<String> categoryPath = new ArrayList<>();
        boolean isInQuotes = false;
        boolean isInList = false;
        boolean consumeList = false;
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
                    } else if (isInList) {
                        if (line.trim().equals(">")) {
                            isInList = false;
                            if (consumeList) {
                                writer.println();
                            }
                            continue lineLoop;
                        } else if (consumeList) {
                            writer.print(line.trim().replace(",", ",,"));
                            writer.print(", ");
                            continue lineLoop;
                        }
                    } else {
                        String category;
                        String name;
                        String type;
                        switch (c) {
                            case '#':
                                break charLoop;
                            case '"':
                                isInQuotes = true;
                                buffer.append(c);
                                break;
                            case '{':
                                categoryPath.add(
                                    buffer.toString()
                                        .trim());
                                buffer = new StringBuilder();
                                break;
                            case '}':
                                categoryPath.remove(categoryPath.size() - 1);
                                break;
                            case '<':
                                isInList = true;
                                consumeList = false;
                                category = StringUtils.join(categoryPath, ".");
                                name = buffer.toString()
                                    .trim();
                                type = name.substring(0, 1);
                                name = name.substring(2);
                                for (int j = 0; j < entries.size(); j++) {
                                    LocalConfigEntry entry = entries.get(j);
                                    if (entry.passesProperty(category, name, type)) {
                                        foundProperty[j] = true;
                                        if (entry.containsWildcard()) {
                                            for (LocalConfigEntry notEntry : notEntries) {
                                                if (notEntry.passesProperty(category, name, type)) {
                                                    continue lineLoop;
                                                }
                                            }
                                        }
                                        consumeList = true;
                                        writer.print(entry.getIdentifier(entry.file, category, type, name));
                                        writer.print("=");
                                        continue lineLoop;
                                    }
                                }
                                continue lineLoop;
                            case '=':
                                category = StringUtils.join(categoryPath, ".");
                                name = buffer.toString()
                                    .trim();
                                type = name.substring(0, 1);
                                name = name.substring(2);
                                String value = line.substring(i + 1);
                                for (int j = 0; j < entries.size(); j++) {
                                    LocalConfigEntry entry = entries.get(j);
                                    if (entry.passesProperty(category, name, type)) {
                                        foundProperty[j] = true;
                                        if (entry.containsWildcard()) {
                                            for (LocalConfigEntry notEntry : notEntries) {
                                                if (notEntry.passesProperty(category, name, type)) {
                                                    continue lineLoop;
                                                }
                                            }
                                        }
                                        writer.println(
                                            entry.getIdentifier(entry.file, category, type, name) + "=" + value);
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
                List<String> categoryPath = new ArrayList<>();
                boolean isInQuotes = false;
                boolean isInList = false;
                boolean discardList = false;
                lineLoop: for (String line : lines) {
                    StringBuilder buffer = new StringBuilder();
                    charLoop: for (int i = 0; i < line.length(); i++) {
                        char c = line.charAt(i);
                        if (isInQuotes) {
                            if (c == '"') {
                                isInQuotes = false;
                            }
                            buffer.append(c);
                        } else if (isInList) {
                            if (line.trim().equals(">")) {
                                isInList = false;
                            }
                            if (discardList) {
                                continue lineLoop;
                            }
                        } else {
                            String category;
                            String name;
                            String type;
                            switch (c) {
                                case '#':
                                    break charLoop;
                                case '"':
                                    isInQuotes = true;
                                    buffer.append(c);
                                    break;
                                case '{':
                                    categoryPath.add(
                                        buffer.toString()
                                            .trim());
                                    buffer = new StringBuilder();
                                    break;
                                case '}':
                                    categoryPath.remove(categoryPath.size() - 1);
                                    break;
                                case '<':
                                    isInList = true;
                                    discardList = false;
                                    category = StringUtils.join(categoryPath, ".");
                                    name = buffer.toString()
                                        .trim();
                                    type = name.substring(0, 1);
                                    name = name.substring(2);
                                    for (int j = 0; j < entries.size(); j++) {
                                        LocalConfigEntry entry = entries.get(j);
                                        if (entry.passesProperty(category, name, type)) {
                                            foundProperty[j] = true;
                                            if (entry.containsWildcard()) {
                                                for (LocalConfigEntry notEntry : notEntries) {
                                                    if (notEntry.passesProperty(category, name, type)) {
                                                        break charLoop;
                                                    }
                                                }
                                            }
                                            discardList = true;
                                            String indent = StringUtils.repeat(' ', categoryPath.size() * 4);
                                            writer.println(line);
                                            String escapedValue = entry.value.replaceAll(",,", "\n");
                                            List<String> values = Arrays.asList(StringUtils.splitPreserveAllTokens(escapedValue, ","));
                                            if (!values.isEmpty())
                                                values = values.subList(0, values.size() - 1);
                                            for (String value : values) {
                                                writer.print(indent);
                                                writer.print("    ");
                                                writer.println(value.replace('\n', ',').trim());
                                            }
                                            writer.print(indent);
                                            writer.println(" >");
                                            continue lineLoop;
                                        }
                                    }
                                    break charLoop;
                                case '=':
                                    category = StringUtils.join(categoryPath, ".");
                                    name = buffer.toString()
                                        .trim();
                                    type = name.substring(0, 1);
                                    name = name.substring(2);
                                    for (int j = 0; j < entries.size(); j++) {
                                        LocalConfigEntry entry = entries.get(j);
                                        if (entry.passesProperty(category, name, type)) {
                                            foundProperty[j] = true;
                                            if (entry.containsWildcard()) {
                                                for (LocalConfigEntry notEntry : notEntries) {
                                                    if (notEntry.passesProperty(category, name, type)) {
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
