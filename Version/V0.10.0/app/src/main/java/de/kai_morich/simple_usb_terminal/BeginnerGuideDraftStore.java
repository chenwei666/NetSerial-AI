package de.kai_morich.simple_usb_terminal;

import android.os.Bundle;

import com.chenwei666.netserial.guided.GuidedReportFormatter;
import com.chenwei666.netserial.guided.GuidedScenario;

/**
 * One bounded process-local handoff while a guide opens a terminal. There is no
 * disk persistence, credential field, connection reference, or execution callback.
 */
public final class BeginnerGuideDraftStore {
    private static Bundle draft;

    private BeginnerGuideDraftStore() { }

    /** Copies only explicitly allowed, sanitized guide inputs; never the whole view state. */
    public static synchronized void save(Bundle inputs) {
        if (inputs == null) { clear(); return; }
        String id = inputs.getString("scenario");
        if (id == null) { clear(); return; }
        try { GuidedScenario.valueOf(id); }
        catch (IllegalArgumentException invalid) { clear(); return; }
        GuidedReportFormatter formatter = new GuidedReportFormatter();
        Bundle bounded = new Bundle();
        bounded.putString("scenario", id);
        copyString(inputs, bounded, "target", 253, formatter);
        copyString(inputs, bounded, "port", 5, formatter);
        copyString(inputs, bounded, "parameter", 64, formatter);
        copyString(inputs, bounded, "evidence", GuidedReportFormatter.MAX_EVIDENCE, formatter);
        copyString(inputs, bounded, "source", 1024, formatter);
        bounded.putInt("vendor", Math.max(0, Math.min(4, inputs.getInt("vendor", 0))));
        draft = bounded;
    }

    /** Returned Bundles can be changed without altering the stored handoff. */
    public static synchronized Bundle peek() {
        return draft == null ? null : new Bundle(draft);
    }

    public static synchronized void clear() { draft = null; }

    private static void copyString(Bundle from, Bundle to, String key, int maximum,
                                   GuidedReportFormatter formatter) {
        String safe = formatter.sanitize(from.getString(key, ""));
        to.putString(key, safe.substring(0, Math.min(safe.length(), maximum)));
    }
}
