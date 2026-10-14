package de.kai_morich.simple_usb_terminal;

import android.app.Activity;
import android.app.Instrumentation;
import android.app.LocaleManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.chenwei666.netserial.network.toolbox.AdvancedProbes;
import com.chenwei666.netserial.network.toolbox.ProbeSession;
import com.chenwei666.netserial.network.toolbox.ToolRequest;
import com.chenwei666.netserial.network.toolbox.ToolResult;
import com.chenwei666.netserial.network.toolbox.ToolboxService;
import com.chenwei666.netserial.settings.AppLocaleController;
import com.chenwei666.netserial.settings.AppSettingsStore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

/** Isolated device checks: only loopback network traffic, synthetic inputs and own app settings. */
public final class ToolboxInstrumentation extends Instrumentation {
    private static final String TARGET = "com.chenwei666.netserial.preview012";
    private static final String[] IDS = {"HEALTH", "PING", "TCP", "TRACE", "MTU", "ADDRESS", "DNS", "HTTP", "TLS", "INFO", "DISCOVER", "WOL", "CIDR", "MAC", "PORTS", "EXTRACT"};
    private final StringBuilder report = new StringBuilder();
    private int assertions;
    private Bundle arguments;
    private NetworkToolsActivity activity;
    private ToolboxService service;
    private SharedPreferences preferences, toolPreferences;
    private String oldAppearance, oldLanguage;
    private Integer oldTimeout;
    private LocaleList oldSystemLocales;

    @Override public void onCreate(Bundle arguments) {
        super.onCreate(arguments);
        this.arguments = arguments == null ? new Bundle() : arguments;
        start();
    }
    @Override public void onStart() {
        int outcome = Activity.RESULT_CANCELED;
        Bundle results = new Bundle();
        try {
            check(TARGET.equals(getTargetContext().getPackageName()), "preview012 isolation guard");
            configure();
            activity = (NetworkToolsActivity) startActivitySync(new Intent(getTargetContext(), NetworkToolsActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            waitForIdleSync();
            toolPreferences = activity.getPreferences(Context.MODE_PRIVATE);
            oldTimeout = toolPreferences.contains("timeout_seconds") ? toolPreferences.getInt("timeout_seconds", 3) : null;
            service = new ToolboxService(getTargetContext());
            checkCatalog();
            screenshot("toolbox-catalog.png");
            checkToolForms();
            checkLoopbackCore();
            checkLoopbackTcp();
            checkLoopbackHttp();
            checkOfflineAndInfo();
            checkPingUi();
            screenshot("toolbox-ping.png");
            checkCancellation();
            outcome = Activity.RESULT_OK;
        } catch (Throwable failure) {
            report.append("FAIL ").append(failure.getClass().getSimpleName()).append(": ").append(failure.getMessage()).append('\n');
        } finally {
            try { ui(() -> { if (activity != null && !activity.isFinishing()) activity.finish(); }); waitForIdleSync(); restore(); }
            catch (Throwable failure) { outcome = Activity.RESULT_CANCELED; report.append("FAIL cleanup: ").append(failure.getMessage()).append('\n'); }
            if (outcome == Activity.RESULT_OK) report.append("PASS ").append(assertions).append(" assertions; loopback only; no AI, external traffic or LAN scans.\n");
            results.putString("result", outcome == Activity.RESULT_OK ? "PASS" : "FAIL");
            results.putInt("assertions", assertions);
            results.putString("stream", "\n" + report);
            finish(outcome, results);
        }
    }
    private void configure() {
        preferences = getTargetContext().getSharedPreferences("app_settings_v1", Context.MODE_PRIVATE);
        oldAppearance = preferences.getString("appearance_mode", null);
        oldLanguage = preferences.getString("language", null);
        SharedPreferences.Editor edit = preferences.edit();
        String appearance = arguments.getString("appearance"), language = arguments.getString("language");
        if (appearance != null) {
            check(appearance.equals("LIGHT") || appearance.equals("DARK"), "valid requested appearance");
            edit.putString("appearance_mode", appearance);
        }
        if (language != null) {
            check(language.equals("zh") || language.equals("en"), "valid requested language");
            edit.putString("language", language.equals("zh") ? "zh-CN" : "en");
        }
        check(edit.commit(), "preview-only preferences configured");
        if (language != null && Build.VERSION.SDK_INT >= 33) {
            LocaleManager manager = getTargetContext().getSystemService(LocaleManager.class);
            oldSystemLocales = manager.getApplicationLocales();
            manager.setApplicationLocales(LocaleList.forLanguageTags(language.equals("zh") ? "zh-CN" : "en"));
        } else if (language != null) ui(() -> AppLocaleController.applyStoredLanguage(getTargetContext()));
        ui(() -> AppAppearanceController.applyNightMode(new AppSettingsStore(getTargetContext()).load().getAppearanceMode()));
    }
    private void restore() {
        if (preferences == null) return;
        SharedPreferences.Editor edit = preferences.edit();
        if (oldAppearance == null) edit.remove("appearance_mode"); else edit.putString("appearance_mode", oldAppearance);
        if (oldLanguage == null) edit.remove("language"); else edit.putString("language", oldLanguage);
        check(edit.commit(), "app language/theme preferences restored");
        if (toolPreferences != null) {
            SharedPreferences.Editor toolEdit = toolPreferences.edit();
            if (oldTimeout == null) toolEdit.remove("timeout_seconds"); else toolEdit.putInt("timeout_seconds", oldTimeout);
            check(toolEdit.commit(), "tool timeout preference restored");
        }
        ui(() -> {
            if (Build.VERSION.SDK_INT >= 33 && oldSystemLocales != null) getTargetContext().getSystemService(LocaleManager.class).setApplicationLocales(oldSystemLocales);
            else if (Build.VERSION.SDK_INT < 33) AppLocaleController.applyStoredLanguage(getTargetContext());
            AppAppearanceController.applyNightMode(new AppSettingsStore(getTargetContext()).load().getAppearanceMode());
        });
    }
    private void checkCatalog() {
        ui(() -> {
            check(required(R.id.toolbox_catalog).isShown(), "real tool catalog visible");
            View catalog = required(R.id.toolbox_catalog_items);
            check(countToolCards(catalog) == 16, "catalog has exactly sixteen selectable tools");
            check(catalog.getWidth() > 0, "catalog measured on current phone/tablet display");
            String language = arguments.getString("language"), appearance = arguments.getString("appearance");
            if (language != null) check(language.equals(activity.getResources().getConfiguration().locale.getLanguage()), "requested locale applied");
            if (appearance != null) check((activity.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                    == (appearance.equals("DARK") ? Configuration.UI_MODE_NIGHT_YES : Configuration.UI_MODE_NIGHT_NO), "requested light/dark theme applied");
            ((EditText) required(R.id.toolbox_search)).setText("WOL");
            check(countToolCards(catalog) == 1 && catalog.findViewWithTag("tool:WOL") != null, "catalog search filters to WOL");
            ((EditText) required(R.id.toolbox_search)).setText("zz-no-such-tool-zz");
            check(countToolCards(catalog) == 0, "unmatched search shows no stale cards");
            ((EditText) required(R.id.toolbox_search)).setText("");
            check(countToolCards(catalog) == 16, "clearing search restores catalog");
            check(catalog.findViewWithTag("tool:PING").performClick(), "real Ping catalog card opens detail");
            check(required(R.id.toolbox_detail).isShown(), "tool detail visible after card click");
            invoke("backToCatalog", null);
        });
        waitForIdleSync();
    }
    private void checkToolForms() {
        for (String id : IDS) {
            ui(() -> {
                select(id);
                check(required(R.id.toolbox_start).isShown(), id + " start control shown");
                check(((TextView) required(R.id.toolbox_description)).getText().length() > 0, id + " has localized instructions");
                boolean target = !id.equals("INFO") && !id.equals("PORTS");
                check((required(R.id.toolbox_target_layout).getVisibility() == View.VISIBLE) == target, id + " target field appropriate");
                boolean option = id.equals("DNS") || id.equals("TCP") || id.equals("TLS") || id.equals("DISCOVER") || id.equals("WOL") || id.equals("HEALTH") || id.equals("CIDR");
                check((required(R.id.toolbox_option_layout).getVisibility() == View.VISIBLE) == option, id + " options appropriate");
                check((required(R.id.toolbox_count_layout).getVisibility() == View.VISIBLE) == (id.equals("PING") || id.equals("TRACE")), id + " packet/hop count appropriate");
            });
        }
        waitForIdleSync();
    }
    private ToolRequest request(String target, String option) { return new ToolRequest(target, option, 1000, 1, false); }
    private ToolResult run(String id, ToolRequest request) throws Exception {
        ProbeSession session = new ProbeSession(15000, null);
        try { return service.run(id, request, session); } finally { session.cancel(); }
    }
    private void checkLoopbackCore() throws Exception {
        ToolResult ping = run("PING", request("127.0.0.1", ""));
        check(ping.status == ToolResult.Status.OK, "actual unprivileged Android loopback Ping succeeds");
        check(ping.samples.size() == 1 && !Double.isNaN(ping.samples.get(0)) && ping.samples.get(0) >= 0, "actual Ping has numeric packet RTT");
        check(ping.raw.contains("bytes from") && ping.export().contains("ms"), "actual ping raw evidence and millisecond display");
        ToolResult trace = run("TRACE", request("127.0.0.1", ""));
        check(trace.status == ToolResult.Status.OK && trace.rows.size() == 1, "actual root-free TTL trace reaches loopback in one hop");
        check(trace.export().contains("127.0.0.1") && trace.export().contains("ms"), "trace displays returned destination and RTT");
        ToolResult mtu = run("MTU", request("127.0.0.1", ""));
        check(mtu.status == ToolResult.Status.OK || mtu.status == ToolResult.Status.UNSUPPORTED, "MTU either verified or explicitly unsupported on this Android build");
        if (mtu.status == ToolResult.Status.OK) check(mtu.export().contains(">= 1500") && !mtu.raw.toLowerCase().contains("not permitted"), "successful MTU contains verified lower bound, not a permission error");
    }
    private void checkLoopbackTcp() throws Exception {
        int port;
        try (ServerSocket server = loopbackServer()) {
            port = server.getLocalPort();
            ToolResult open = run("TCP", request("127.0.0.1", String.valueOf(port)));
            check(open.status == ToolResult.Status.OK && open.export().contains("Open") && open.export().contains("ms"), "TCP reports actual localhost listening port and elapsed time");
            try (Socket accepted = server.accept()) { check(accepted.getInetAddress().isLoopbackAddress(), "TCP fixture connection stays on loopback"); }
            ToolResult health = run("HEALTH", request("127.0.0.1", String.valueOf(port)));
            check(health.status == ToolResult.Status.OK, "loopback health check passes Ping and the listening TCP port");
            check(health.export().contains("no DNS query is needed"), "literal-IP health check skips external DNS queries explicitly");
            try (Socket accepted = server.accept()) { check(accepted.getInetAddress().isLoopbackAddress(), "health TCP fixture stays on loopback"); }
        }
        ToolResult refused = run("TCP", request("127.0.0.1", String.valueOf(port)));
        check(refused.status == ToolResult.Status.FAILED && refused.export().contains("refused"), "TCP refusal differs from timeout and success");
        Method literal = ToolboxService.class.getDeclaredMethod("isLiteralAddress", String.class);
        literal.setAccessible(true);
        check(Boolean.TRUE.equals(literal.invoke(null, "::1")) && Boolean.TRUE.equals(literal.invoke(null, "2001:db8::1")), "IPv6 health targets classified lexically without DNS");
        check(Boolean.FALSE.equals(literal.invoke(null, "switch.example")), "hostname health targets retain DNS step");
        ProbeSession expired = new ProbeSession(1, null);
        SystemClock.sleep(3);
        boolean deadline = false;
        try { service.run("HEALTH", request("127.0.0.1", "443"), expired); }
        catch (ProbeSession.DeadlineExceeded expected) { deadline = true; }
        finally { expired.cancel(); }
        check(deadline, "health deadline remains DeadlineExceeded rather than user cancellation");
    }
    private void checkLoopbackHttp() throws Exception {
        try (HttpFixture fixture = new HttpFixture("HTTP/1.1 200 OK\r\nContent-Length: 0\r\nConnection: close\r\n\r\n")) {
            ToolResult response = run("HTTP", request(fixture.url(), ""));
            fixture.await();
            check(response.status == ToolResult.Status.OK && response.export().contains("200"), "HTTP parses actual localhost status 200");
            check(fixture.request.get() != null && fixture.request.get().startsWith("HEAD "), "HTTP sends only HEAD request");
        }
        try (ServerSocket redirectTarget = loopbackServer(); HttpFixture fixture = new HttpFixture(
                "HTTP/1.1 302 Found\r\nLocation: http://127.0.0.1:" + redirectTarget.getLocalPort() + "/redirected\r\nContent-Length: 0\r\nConnection: close\r\n\r\n")) {
            ToolResult response = run("HTTP", request(fixture.url(), ""));
            fixture.await();
            check(response.export().contains("302"), "HTTP reports original redirect status");
            redirectTarget.setSoTimeout(350);
            boolean followed = false;
            try (Socket unexpected = redirectTarget.accept()) { followed = true; }
            catch (SocketTimeoutException expected) { }
            check(!followed, "HTTP never automatically follows redirect");
        }
    }
    private void checkOfflineAndInfo() throws Exception {
        check(run("CIDR", request("192.168.5.10/24", "")).export().contains("192.168.5.0"), "IPv4 subnet computed offline");
        check(run("CIDR", request("2001:db8::1/64", "")).status == ToolResult.Status.OK, "IPv6 subnet computed offline");
        check(run("MAC", request("02:00:00:00:00:01", "")).export().contains("Locally"), "random/local MAC does not invent manufacturer");
        check(run("EXTRACT", request("fixture IP 192.0.2.12 MAC 00:11:22:33:44:55", "")).export().contains("192.0.2.12"), "synthetic text extraction remains local");
        check(run("PORTS", request("", "")).rows.size() > 0, "offline common-port reference populated");
        check(run("ADDRESS", request("127.0.0.1", "")).export().contains("Loopback"), "literal address classification detects loopback");
        ToolResult info = run("INFO", request("", ""));
        check(info.status == ToolResult.Status.OK || info.status == ToolResult.Status.FAILED || info.status == ToolResult.Status.UNSUPPORTED, "network info handles available or missing default network without active probes");
        rejectedDiscovery("8.8.8.0/30");
        rejectedDiscovery("192.168.0.0/16");
        byte[] packet = AdvancedProbes.magicPacket("02:00:00:00:00:01");
        check(packet.length == 102 && packet[0] == (byte)255 && packet[6] == 2 && packet[101] == 1, "WOL magic packet validated without broadcasting");
    }
    private void rejectedDiscovery(String input) throws Exception {
        boolean rejected = false;
        try { run("DISCOVER", request(input, "80")); } catch (IllegalArgumentException expected) { rejected = true; }
        check(rejected, "discovery rejects unsafe range before any scan: " + input);
    }
    private void checkPingUi() {
        ui(() -> {
            select("PING");
            ((EditText) required(R.id.network_target)).setText("127.0.0.1");
            ((EditText) required(R.id.toolbox_count)).setText("1");
            ((EditText) required(R.id.toolbox_timeout)).setText("1");
            check(required(R.id.toolbox_start).performClick(), "actual UI start Ping action accepted");
        });
        awaitCompleted(10000);
        ui(() -> {
            ToolResult result = latest();
            check(result != null && result.status == ToolResult.Status.OK, "UI receives real Ping success");
            check(textOf(required(R.id.toolbox_result_rows)).contains("ms"), "visible Ping result rows show numeric millisecond values");
            check(required(R.id.toolbox_trend).getVisibility() == View.VISIBLE, "actual Ping trend view enabled");
            check(required(R.id.toolbox_report).isEnabled(), "completed report action enabled");
            required(R.id.toolbox_result_card).requestRectangleOnScreen(new android.graphics.Rect(0, 0,
                    required(R.id.toolbox_result_card).getWidth(), Math.min(300, required(R.id.toolbox_result_card).getHeight())), true);
        });
        waitForIdleSync();
    }
    private void checkCancellation() {
        ui(() -> {
            select("PING");
            ((EditText) required(R.id.network_target)).setText("127.0.0.1");
            ((EditText) required(R.id.toolbox_count)).setText("20");
            required(R.id.toolbox_start).performClick();
            check(required(R.id.toolbox_stop).getVisibility() == View.VISIBLE, "running probe exposes stop control");
            required(R.id.toolbox_stop).performClick();
            check(latest() != null && latest().status == ToolResult.Status.CANCELLED, "explicit stop reports cancelled");
            select("CIDR");
            ((EditText) required(R.id.network_target)).setText("192.0.2.1/24");
            ((EditText) required(R.id.network_port)).setText("");
            required(R.id.toolbox_start).performClick();
        });
        awaitCompleted(6000);
        SystemClock.sleep(350);
        waitForIdleSync();
        ui(() -> check(latest() != null && latest().title.equals("CIDR") && latest().status == ToolResult.Status.OK,
                "cancelled Ping cannot overwrite a subsequent tool result"));
    }
    private void awaitCompleted(long timeout) {
        long deadline = SystemClock.uptimeMillis() + timeout;
        final boolean[] ready = {false};
        do {
            waitForIdleSync();
            ui(() -> ready[0] = required(R.id.toolbox_stop).getVisibility() == View.GONE && latest() != null);
            if (ready[0]) return;
            SystemClock.sleep(50);
        } while (SystemClock.uptimeMillis() < deadline);
        throw new AssertionError("UI probe completion deadline");
    }
    private void select(String id) { invoke("selectTool", id); }
    private void invoke(String name, String argument) {
        try {
            Method method = argument == null ? NetworkToolsActivity.class.getDeclaredMethod(name) : NetworkToolsActivity.class.getDeclaredMethod(name, String.class);
            method.setAccessible(true);
            if (argument == null) method.invoke(activity); else method.invoke(activity, argument);
        } catch (Exception failure) { throw new AssertionError("Activity method " + name, failure); }
    }
    private ToolResult latest() {
        try { Field field = NetworkToolsActivity.class.getDeclaredField("latest"); field.setAccessible(true); return (ToolResult) field.get(activity); }
        catch (Exception failure) { throw new AssertionError("Cannot inspect actual result", failure); }
    }
    private int countToolCards(View view) {
        int count = view.getTag() instanceof String && ((String) view.getTag()).startsWith("tool:") ? 1 : 0;
        if (view instanceof ViewGroup) for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) count += countToolCards(((ViewGroup) view).getChildAt(i));
        return count;
    }
    private String textOf(View view) {
        StringBuilder text = new StringBuilder(view instanceof TextView ? ((TextView) view).getText() : "");
        if (view instanceof ViewGroup) for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) text.append('\n').append(textOf(((ViewGroup) view).getChildAt(i)));
        return text.toString();
    }
    private View required(int id) {
        View view = activity.findViewById(id);
        if (view == null) throw new AssertionError("Missing view: " + id);
        return view;
    }
    private void screenshot(String name) {
        if (!"true".equalsIgnoreCase(arguments.getString("screenshot"))) return;
        waitForIdleSync();
        ui(() -> {
            View decor = activity.getWindow().getDecorView();
            Bitmap image = Bitmap.createBitmap(decor.getWidth(), decor.getHeight(), Bitmap.Config.ARGB_8888);
            try {
                decor.draw(new Canvas(image));
                File directory = getTargetContext().getExternalFilesDir(null);
                check(directory != null, "own screenshot directory available");
                File file = new File(directory, name);
                try (FileOutputStream output = new FileOutputStream(file)) { check(image.compress(Bitmap.CompressFormat.PNG, 100, output), "own-view synthetic screenshot saved: " + name); }
                report.append("Screenshot: ").append(file.getAbsolutePath()).append('\n');
            } catch (IOException failure) { throw new AssertionError("Screenshot failed", failure); }
            finally { image.recycle(); }
        });
    }
    private static ServerSocket loopbackServer() throws IOException {
        ServerSocket server = new ServerSocket(0, 4, InetAddress.getByAddress(new byte[]{127,0,0,1}));
        server.setSoTimeout(3000);
        return server;
    }
    private static final class HttpFixture implements AutoCloseable {
        final ServerSocket server;
        final Thread worker;
        final AtomicReference<String> request = new AtomicReference<>();
        final AtomicReference<Throwable> failure = new AtomicReference<>();
        HttpFixture(String response) throws IOException {
            server = loopbackServer();
            worker = new Thread(() -> {
                try (Socket client = server.accept()) {
                    client.setSoTimeout(3000);
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    int matched = 0; byte[] delimiter = {'\r', '\n', '\r', '\n'};
                    while (bytes.size() < 8192) {
                        int value = client.getInputStream().read();
                        if (value < 0) break;
                        bytes.write(value); matched = value == delimiter[matched] ? matched + 1 : value == '\r' ? 1 : 0;
                        if (matched == 4) break;
                    }
                    request.set(new String(bytes.toByteArray(), StandardCharsets.ISO_8859_1));
                    client.getOutputStream().write(response.getBytes(StandardCharsets.ISO_8859_1));
                    client.getOutputStream().flush();
                } catch (Throwable error) { failure.set(error); }
            }, "toolbox-loopback-fixture");
            worker.setDaemon(true); worker.start();
        }
        String url() { return "http://127.0.0.1:" + server.getLocalPort() + "/fixture"; }
        void await() throws Exception { worker.join(3500); if (worker.isAlive() || failure.get() != null) throw new AssertionError("Local HTTP fixture failed", failure.get()); }
        @Override public void close() throws Exception { server.close(); worker.join(3500); }
    }
    private void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
        assertions++; report.append("OK ").append(message).append('\n');
    }
    private void ui(Runnable action) {
        AtomicReference<Throwable> failure = new AtomicReference<>();
        runOnMainSync(() -> { try { action.run(); } catch (Throwable error) { failure.set(error); } });
        if (failure.get() != null) throw new AssertionError(failure.get().getMessage(), failure.get());
    }
}
