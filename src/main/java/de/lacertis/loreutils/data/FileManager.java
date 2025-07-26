package de.lacertis.loreutils.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class FileManager {

    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve("loreutils");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    static {
        try {
            Files.createDirectories(CONFIG_DIR);

        } catch (IOException e) {
            throw new RuntimeException("Could not create config directory: " + CONFIG_DIR, e);
        }
    }

    public static void createJsonFile(String name, Object defaultObj) throws IOException {
        Path file = getFilePath(name);
        if (Files.notExists(file)) {
            try (Writer writer = Files.newBufferedWriter(file, StandardOpenOption.CREATE_NEW)) {
                GSON.toJson(defaultObj, writer);
            }
        }
    }

    public static void createJsonFileInSubfolder(String subFolder, String name, Object defaultObj) throws IOException {
        Path folder = CONFIG_DIR.resolve(subFolder);
        if (!Files.exists(folder)) {
            Files.createDirectories(folder);
        }
        Path file = folder.resolve(name.endsWith(".json") ? name : name + ".json");
        if (Files.notExists(file)) {
            try (Writer writer = Files.newBufferedWriter(file, StandardOpenOption.CREATE_NEW)) {
                GSON.toJson(defaultObj, writer);
            }
        }
    }

    public static <T> T loadJson(String name, Class<T> type) throws IOException {
        Path file = getFilePath(name);
        if (Files.notExists(file)) {
            T defaultObj = null;
            try {
                defaultObj = type.getDeclaredConstructor().newInstance();
            } catch (Exception ex) {
                throw new IOException("Could not instantiate default object", ex);
            }
            createJsonFile(name, defaultObj);
            return defaultObj;
        }
        try (Reader reader = Files.newBufferedReader(file)) {
            return GSON.fromJson(reader, type);
        }
    }

    public static void saveJson(String name, Object obj) throws IOException {
        Path file = getFilePath(name);
        Files.createDirectories(CONFIG_DIR);
        try (Writer writer = Files.newBufferedWriter(file, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            GSON.toJson(obj, writer);
        }
    }

    public static boolean deleteJson(String name) throws IOException {
        Path file = getFilePath(name);
        return Files.deleteIfExists(file);
    }

    public static List<String> listAllJsons() throws IOException {
        List<String> result = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(CONFIG_DIR, "*.json")) {
            for (Path path : stream) {
                result.add(path.getFileName().toString());
            }
        }
        return result;
    }

    public static <T> void editJson(String name, Class<T> type, Consumer<T> editor) throws IOException {
        T data = loadJson(name, type);
        editor.accept(data);
        saveJson(name, data);
    }

    private static Path getFilePath(String name) {
        return CONFIG_DIR.resolve(name.endsWith(".json") ? name : name + ".json");
    }
}