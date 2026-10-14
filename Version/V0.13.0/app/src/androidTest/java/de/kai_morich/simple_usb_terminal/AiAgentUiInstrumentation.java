package de.kai_morich.simple_usb_terminal;

import android.app.Activity;
import android.app.Instrumentation;
import android.app.LocaleManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Build;
import android.os.LocaleList;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.CheckBox;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import com.chenwei666.netserial.ai.RequestCancellation;
import com.chenwei666.netserial.settings.AppLocaleController;
import com.chenwei666.netserial.settings.AppSettingsStore;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;

/** Isolated preview UI tests. No model request, terminal, or device command is invoked. */
public final class AiAgentUiInstrumentation extends Instrumentation {
    private AiAgentActivity activity;
    private int checks;
    private final StringBuilder report = new StringBuilder();
    private Bundle arguments;
    private SharedPreferences preferences;
    private SharedPreferences chatPreferences;
    private String previousAppearance;
    private String previousLanguage;
    private LocaleList previousSystemLocales;
    private boolean hadMemoryPreference;
    private boolean previousMemoryPreference;

    @Override public void onCreate(Bundle arguments) {
        super.onCreate(arguments);
        this.arguments = arguments == null ? new Bundle() : arguments;
        start();
    }

    @Override public void onStart() {
        int result = Activity.RESULT_CANCELED;
        try {
            check("com.chenwei666.netserial.preview013".equals(getTargetContext().getPackageName()), "preview-only guard");
            configureAppearance();
            check(!getTargetContext().getPackageManager().getActivityInfo(
                    new ComponentName(getTargetContext(), AiAgentActivity.class), 0).exported, "execution activity nonexported");
            activity = (AiAgentActivity) startActivitySync(new Intent(getTargetContext(), AiAgentActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            waitForIdleSync();
            runOnMainSync(() -> {
                check(!activity.findViewById(R.id.agent_start).isEnabled(), "no session cannot start");
                check(!activity.findViewById(R.id.agent_stop).isEnabled(), "idle cannot stop");
                check((activity.getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_SECURE) != 0,
                        "screen capture guarded");
                check(!activity.findViewById(R.id.agent_goal).isSaveEnabled(), "task text not saved to instance state");
                Toolbar toolbar = activity.findViewById(R.id.agent_toolbar);
                check(toolbar.getMenu().size() == 1, "settings action always available");
                String language = arguments.getString("language");
                if (language != null) {
                    check(language.equals(activity.getResources().getConfiguration().locale.getLanguage()), "requested task language applied");
                    String label = ((android.widget.TextView) activity.findViewById(R.id.agent_start)).getText().toString();
                    check(("zh".equals(language) ? "开始自动执行" : "Start automatic task").equals(label), "task action translated");
                }
                String appearance = arguments.getString("appearance");
                if (appearance != null) {
                    int actual = activity.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                    check(actual == ("DARK".equals(appearance) ? Configuration.UI_MODE_NIGHT_YES : Configuration.UI_MODE_NIGHT_NO), "requested task appearance applied");
                }
                CheckBox memory = activity.findViewById(R.id.agent_use_memory);
                check(memory.isShown(), "confirmed-memory checkbox visible");
                check(activity.findViewById(R.id.agent_memory_manage).isShown(), "memory manager entry visible");
                if (Build.VERSION.SDK_INT >= 23) {
                    boolean next = !memory.isChecked();
                    memory.setChecked(next);
                    check(chatPreferences.getBoolean("use_confirmed_memory", !next) == next, "task memory preference shared with chat");
                } else {
                    check(!memory.isEnabled() && !activity.findViewById(R.id.agent_memory_manage).isEnabled(), "legacy Android memory controls disabled");
                }
            });
            checkQuestionCancel();
            checkTargetConfirmation();
            checkLocalPasswordCancel();
            checkLocalPasswordSubmit();
            checkFinalEventDelivery();
            result = Activity.RESULT_OK;
        } catch (Throwable failure) {
            report.append("FAIL: ").append(failure.getClass().getSimpleName()).append(" ")
                    .append(failure.getMessage()).append('\n');
        } finally {
            runOnMainSync(() -> { if (activity != null) activity.finish(); });
            try { restoreAppearance(); }
            catch (Throwable failure) { result = Activity.RESULT_CANCELED; report.append("FAIL: isolated settings restoration\n"); }
        }
        Bundle resultBundle = new Bundle();
        resultBundle.putInt("assertions", checks);
        resultBundle.putString("result", result == Activity.RESULT_OK ? "PASS" : "FAIL");
        resultBundle.putString("stream", report + "Assertions: " + checks + "\n");
        finish(result, resultBundle);
    }

    private void configureAppearance() {
        preferences = getTargetContext().getSharedPreferences("app_settings_v1", Context.MODE_PRIVATE);
        previousAppearance = preferences.getString("appearance_mode", null);
        previousLanguage = preferences.getString("language", null);
        chatPreferences = getTargetContext().getSharedPreferences("ai_chat_ui_v1", Context.MODE_PRIVATE);
        hadMemoryPreference = chatPreferences.contains("use_confirmed_memory");
        previousMemoryPreference = chatPreferences.getBoolean("use_confirmed_memory", true);
        String appearance = arguments.getString("appearance");
        String language = arguments.getString("language");
        SharedPreferences.Editor editor = preferences.edit();
        if (appearance != null) {
            check("LIGHT".equals(appearance) || "DARK".equals(appearance), "valid task appearance argument");
            editor.putString("appearance_mode", appearance);
        }
        if (language != null) {
            check("zh".equals(language) || "en".equals(language), "valid task language argument");
            editor.putString("language", "zh".equals(language) ? "zh-CN" : "en");
        }
        check(editor.commit(), "isolated task appearance preferences applied");
        if (language != null) {
            if (Build.VERSION.SDK_INT >= 33) {
                LocaleManager locales = getTargetContext().getSystemService(LocaleManager.class);
                previousSystemLocales = locales.getApplicationLocales();
                locales.setApplicationLocales(LocaleList.forLanguageTags("zh".equals(language) ? "zh-CN" : "en"));
            } else runOnMainSync(() -> AppLocaleController.applyStoredLanguage(getTargetContext()));
        }
    }

    private void restoreAppearance() {
        if (preferences == null) return;
        SharedPreferences.Editor editor = preferences.edit();
        if (previousAppearance == null) editor.remove("appearance_mode");
        else editor.putString("appearance_mode", previousAppearance);
        if (previousLanguage == null) editor.remove("language");
        else editor.putString("language", previousLanguage);
        check(editor.commit(), "original appearance preferences restored");
        SharedPreferences.Editor chatEditor = chatPreferences.edit();
        if (hadMemoryPreference) chatEditor.putBoolean("use_confirmed_memory", previousMemoryPreference);
        else chatEditor.remove("use_confirmed_memory");
        check(chatEditor.commit(), "original memory preference restored exactly");
        runOnMainSync(() -> {
            if (Build.VERSION.SDK_INT >= 33 && previousSystemLocales != null) {
                getTargetContext().getSystemService(LocaleManager.class).setApplicationLocales(previousSystemLocales);
            } else if (Build.VERSION.SDK_INT < 33) AppLocaleController.applyStoredLanguage(getTargetContext());
            AppAppearanceController.applyNightMode(new AppSettingsStore(getTargetContext()).load().getAppearanceMode());
        });
    }

    private void checkQuestionCancel() throws Exception {
        RequestCancellation request = new RequestCancellation();
        AtomicReference<Object> outcome = new AtomicReference<>();
        Thread waiter = invokeQuestion(request, null, "Synthetic question: which VLAN?", outcome);
        AlertDialog dialog = waitDialog();
        check(dialog != null && dialog.isShowing(), "follow-up dialog appears");
        runOnMainSync(() -> {
            EditText entry = firstEdit((ViewGroup) dialog.getWindow().getDecorView());
            check(entry != null && !entry.isSaveEnabled(), "follow-up text not saved");
            entry.setText("password=synthetic-secret");
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
            check(entry.getError() != null && dialog.isShowing(), "recognized secret blocked before submission");
        });
        request.cancel();
        waiter.join(2000);
        waitForIdleSync();
        check(!waiter.isAlive(), "cancellation releases worker promptly");
        check(outcome.get() instanceof Throwable, "cancelled follow-up fails closed");
        check(!dialog.isShowing(), "cancelled dialog dismissed");
    }

    private void checkTargetConfirmation() throws Exception {
        RequestCancellation request = new RequestCancellation();
        AtomicReference<Object> outcome = new AtomicReference<>();
        Thread waiter = invokeQuestion(request, "synthetic-switch", "reboot", outcome);
        AlertDialog dialog = waitDialog();
        runOnMainSync(() -> {
            EditText entry = firstEdit((ViewGroup) dialog.getWindow().getDecorView());
            entry.setText("wrong-switch");
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
            check(entry.getError() != null && dialog.isShowing(), "wrong target cannot approve");
            entry.setText("synthetic-switch");
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
        });
        waiter.join(2000);
        waitForIdleSync();
        check(!waiter.isAlive(), "typed target releases worker");
        check("synthetic-switch".equals(outcome.get()), "exact typed target approved only local synthetic prompt");
        check(!dialog.isShowing(), "approved dialog dismissed");
    }

    private Thread invokeQuestion(RequestCancellation request, String target, String question,
                                  AtomicReference<Object> outcome) throws Exception {
        Method method = AiAgentActivity.class.getDeclaredMethod("awaitAnswer", int.class,
                RequestCancellation.class, String.class, String.class);
        method.setAccessible(true);
        Thread thread = new Thread(() -> {
            try { outcome.set(method.invoke(activity, 0, request, target, question)); }
            catch (Throwable error) { outcome.set(error); }
        }, "synthetic-agent-dialog-test");
        thread.start();
        return thread;
    }

    private void checkLocalPasswordCancel() throws Exception {
        RequestCancellation request = new RequestCancellation();
        AtomicReference<Object> outcome = new AtomicReference<>();
        Thread waiter = invokePassword(request, outcome);
        AlertDialog dialog = waitDialog();
        AtomicReference<EditText> input = new AtomicReference<>();
        runOnMainSync(() -> {
            EditText entry = firstEdit((ViewGroup) dialog.getWindow().getDecorView());
            input.set(entry);
            check(!entry.isSaveEnabled() && !entry.isLongClickable(), "password input save and long-press disabled");
            check(entry.getTransformationMethod() instanceof android.text.method.PasswordTransformationMethod,
                    "password masked");
            check(!entry.onTextContextMenuItem(android.R.id.copy), "password copy blocked");
            check((dialog.getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_SECURE) != 0,
                    "local credential dialog capture guarded");
            entry.setText("Synthetic123");
        });
        request.cancel();
        waiter.join(2000);
        waitForIdleSync();
        check(!waiter.isAlive() && outcome.get() instanceof Throwable, "password cancellation fails closed");
        runOnMainSync(() -> check(input.get().length() == 0 && !dialog.isShowing(),
                "cancelled local password buffer cleared and dialog dismissed"));
    }

    private void checkLocalPasswordSubmit() throws Exception {
        RequestCancellation request = new RequestCancellation();
        AtomicReference<Object> outcome = new AtomicReference<>();
        Thread waiter = invokePassword(request, outcome);
        AlertDialog dialog = waitDialog();
        AtomicReference<EditText> input = new AtomicReference<>();
        runOnMainSync(() -> {
            EditText entry = firstEdit((ViewGroup) dialog.getWindow().getDecorView());
            input.set(entry);
            entry.setText("abcdefgh");
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
            check(entry.getError() != null && dialog.isShowing(), "password requires digit and letter");
            entry.setText("Synthetic123;");
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
            check(dialog.isShowing(), "password command separator rejected");
            entry.setText("Synthetic123");
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
        });
        waiter.join(2000);
        waitForIdleSync();
        check(!waiter.isAlive() && outcome.get() instanceof char[], "local password ownership transferred as char array");
        char[] received = (char[]) outcome.get();
        try { check(java.util.Arrays.equals(received, "Synthetic123".toCharArray()), "local value transferred without model call"); }
        finally { java.util.Arrays.fill(received, '\0'); outcome.set(null); }
        runOnMainSync(() -> {
            check(input.get().length() == 0, "submitted local password buffer cleared immediately");
            android.widget.LinearLayout log = activity.findViewById(R.id.agent_events);
            check(!containsText(log, "Synthetic123"), "password absent from event cards");
        });
    }

    private boolean containsText(View view, String needle) {
        if (view instanceof android.widget.TextView && ((android.widget.TextView) view).getText().toString().contains(needle)) return true;
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) if (containsText(group.getChildAt(i), needle)) return true;
        }
        return false;
    }

    private Thread invokePassword(RequestCancellation request, AtomicReference<Object> outcome) throws Exception {
        Method method = AiAgentActivity.class.getDeclaredMethod("awaitWebPassword", int.class,
                RequestCancellation.class, String.class);
        method.setAccessible(true);
        Thread thread = new Thread(() -> {
            try { outcome.set(method.invoke(activity, 0, request, "synthetic-web-user")); }
            catch (Throwable error) { outcome.set(error); }
        }, "synthetic-local-credential-test");
        thread.start();
        return thread;
    }

    private void checkFinalEventDelivery() throws Exception {
        Field signal = AiAgentActivity.class.getDeclaredField("cancellation");
        signal.setAccessible(true);
        RequestCancellation finished = new RequestCancellation();
        finished.cancel();
        signal.set(activity, finished);
        Method event = AiAgentActivity.class.getDeclaredMethod("onRunnerEvent", int.class, String.class);
        event.setAccessible(true);
        event.invoke(activity, 0, "Synthetic final receipt after runner close");
        event.invoke(activity, -1, "Synthetic stale receipt must disappear");
        waitForIdleSync();
        runOnMainSync(() -> {
            View log = activity.findViewById(R.id.agent_events);
            check(containsText(log, "Synthetic final receipt after runner close"), "final receipt survives normal signal cancellation");
            check(!containsText(log, "Synthetic stale receipt must disappear"), "generation gate still rejects stale receipts");
        });
        signal.set(activity, null);
    }

    private AlertDialog waitDialog() throws Exception {
        Field field = AiAgentActivity.class.getDeclaredField("pendingDialog");
        field.setAccessible(true);
        AtomicReference<AlertDialog> found = new AtomicReference<>();
        long deadline = SystemClock.elapsedRealtime() + 3000;
        while (SystemClock.elapsedRealtime() < deadline) {
            runOnMainSync(() -> {
                try { found.set((AlertDialog) field.get(activity)); }
                catch (IllegalAccessException error) { throw new AssertionError(error); }
            });
            if (found.get() != null && found.get().isShowing()) return found.get();
            SystemClock.sleep(30);
        }
        throw new AssertionError("dialog did not appear");
    }

    private EditText firstEdit(ViewGroup group) {
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            if (child instanceof EditText) return (EditText) child;
            if (child instanceof ViewGroup) {
                EditText match = firstEdit((ViewGroup) child);
                if (match != null) return match;
            }
        }
        return null;
    }

    private void check(boolean condition, String label) {
        if (!condition) throw new AssertionError(label);
        checks++;
        report.append("PASS: ").append(label).append('\n');
    }
}
