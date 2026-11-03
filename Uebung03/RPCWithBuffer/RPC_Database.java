package RPCWithBuffer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;

final class RPC_Database {

    private final Path databaseFile;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Map<Integer, String> records = new LinkedHashMap<>();

    RPC_Database(Path databaseFile) throws IOException {
        this.databaseFile = databaseFile.toAbsolutePath();

        if (Files.exists(this.databaseFile)) {
            try (Reader reader = Files.newBufferedReader(this.databaseFile, StandardCharsets.UTF_8)) {
                JsonElement content = JsonParser.parseReader(reader);
                if (!content.isJsonObject()) {
                    throw new IOException("Die Datenbank muss ein JSON-Objekt enthalten.");
                }
                JsonObject data = content.getAsJsonObject();
                for (Map.Entry<String, JsonElement> entry : data.entrySet()) {
                    if (!entry.getValue().isJsonPrimitive()
                            || !entry.getValue().getAsJsonPrimitive().isString()) {
                        throw new IOException("Die Datenbank darf nur String-Datensätze enthalten.");
                    }
                    records.put(Integer.parseInt(entry.getKey()), entry.getValue().getAsString());
                }
            } catch (JsonParseException | NumberFormatException e) {
                throw new IOException("Ungültige JSON-Datenbank.", e);
            }
        }
    }

    synchronized String getRecord(int index) {
        return records.get(index);
    }

    synchronized int getSize() {
        return records.size();
    }

    synchronized void addRecord(int index, String record) throws IOException {
        Map<Integer, String> updatedRecords = new LinkedHashMap<>(records);
        updatedRecords.put(index, record);

        // Erst vollständig schreiben, dann die Datenbankdatei atomar ersetzen.
        Path temporaryFile = Files.createTempFile(databaseFile.getParent(), "rpc-database-", ".json");
        try {
            try (Writer writer = Files.newBufferedWriter(temporaryFile, StandardCharsets.UTF_8)) {
                gson.toJson(updatedRecords, writer);
            }
            Files.move(temporaryFile, databaseFile,
                    StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);

            // Der neue Zustand wird erst nach erfolgreicher Speicherung sichtbar.
            records.clear();
            records.putAll(updatedRecords);
        } finally {
            Files.deleteIfExists(temporaryFile);
        }
    }
}
