package com.chenwei666.netserial.config;

import android.content.Context;
import android.util.AtomicFile;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ConfigSnapshotStore {
    private static final int MAX_SNAPSHOTS = 20;
    private static final int MAX_BYTES = 4_000_000;
    private static final Type LIST_TYPE = new TypeToken<ArrayList<ConfigSnapshot>>() { }.getType();
    private final File file;
    private final AtomicFile atomicFile;
    private final Gson gson = new Gson();

    public ConfigSnapshotStore(Context context) {
        file = new File(context.getApplicationContext().getFilesDir(), "config-snapshots-v1.json");
        atomicFile = new AtomicFile(file);
    }

    public synchronized List<ConfigSnapshot> load() {
        if (!file.exists() || file.length() > MAX_BYTES) return Collections.emptyList();
        try (FileInputStream input = atomicFile.openRead()) {
            byte[] bytes = new byte[(int) file.length()];
            int offset = 0;
            while (offset < bytes.length) {
                int count = input.read(bytes, offset, bytes.length - offset);
                if (count < 0) break;
                offset += count;
            }
            List<ConfigSnapshot> values = gson.fromJson(new String(bytes, 0, offset, StandardCharsets.UTF_8), LIST_TYPE);
            if (values == null || values.size() > MAX_SNAPSHOTS) return Collections.emptyList();
            List<ConfigSnapshot> validated = new ArrayList<>();
            for (ConfigSnapshot value : values) validated.add(new ConfigSnapshot(value.getLabel(),
                    value.getCapturedAtMillis(), value.getNormalizedText()));
            return validated;
        } catch (Exception exception) {
            return Collections.emptyList();
        }
    }

    public synchronized void add(ConfigSnapshot snapshot) {
        List<ConfigSnapshot> values = new ArrayList<>(load());
        values.add(0, snapshot);
        while (values.size() > MAX_SNAPSHOTS) values.remove(values.size() - 1);
        byte[] bytes = gson.toJson(values).getBytes(StandardCharsets.UTF_8);
        if (bytes.length > MAX_BYTES) throw new IllegalArgumentException("snapshot archive too large");
        FileOutputStream output = null;
        try {
            output = atomicFile.startWrite();
            output.write(bytes);
            output.getFD().sync();
            atomicFile.finishWrite(output);
        } catch (Exception exception) {
            if (output != null) atomicFile.failWrite(output);
            throw new IllegalStateException("Unable to save snapshot", exception);
        }
    }
}
