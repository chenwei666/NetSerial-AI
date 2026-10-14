package de.kai_morich.simple_usb_terminal;

import android.app.Activity;
import android.app.Instrumentation;
import android.app.LocaleManager;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Build;
import android.os.LocaleList;
import android.os.SystemClock;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.chenwei666.netserial.ai.AiChatConversation;
import com.chenwei666.netserial.ai.AiChatMessage;
import com.chenwei666.netserial.ai.AiChatRole;
import com.chenwei666.netserial.ai.AiSuggestedCommand;
import com.chenwei666.netserial.ai.SharedPreferencesCredentialRecordStore;
import com.chenwei666.netserial.device.CliMode;
import com.chenwei666.netserial.device.DeviceProfile;
import com.chenwei666.netserial.device.Vendor;
import com.chenwei666.netserial.memory.ConfirmedMemoryStore;
import com.chenwei666.netserial.memory.EncryptedConfirmedMemoryPersistence;
import com.chenwei666.netserial.memory.MemoryRecord;
import com.chenwei666.netserial.memory.MemoryScope;
import com.chenwei666.netserial.settings.AppLocaleController;
import com.chenwei666.netserial.settings.AppSettingsStore;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.nio.charset.StandardCharsets;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Dependency-free device smoke checks against production activities and renderer.
 * Only the isolated .preview012 package is accepted. No send button or API action is used,
 * only unique synthetic memories are saved and removed, and every opened activity is closed.
 */
public final class AiChatUiInstrumentation extends Instrumentation {
    private static final String TARGET = "com.chenwei666.netserial.preview012";
    private static final String CODE = "display interface *\n// **literal** stays intact";
    private static final String DRAFT = "Synthetic UI fixture: explain network terms simply.";
    private final StringBuilder report = new StringBuilder();
    private int assertions;
    private Activity activity;
    private Activity settings;
    private Bundle arguments;
    private SharedPreferences preferences;
    private String previousAppearance;
    private String previousLanguage;
    private LocaleList previousSystemLocales;
    private final String fixture = DRAFT + " " + UUID.randomUUID();
    private final String subject = "synthetic-ui-" + UUID.randomUUID();
    private String fixtureId;
    private boolean memoryAttempted;

    @Override public void onCreate(Bundle arguments) {
        super.onCreate(arguments);
        this.arguments = arguments == null ? new Bundle() : arguments;
        start();
    }

    @Override public void onStart() {
        Bundle results = new Bundle();
        int result = Activity.RESULT_CANCELED;
        try {
            check(TARGET.equals(getTargetContext().getPackageName()),
                    "isolated preview012 package guard");
            configureAppearance();
            Intent intent = new Intent(getTargetContext(), AiCopilotActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity = startActivitySync(intent);
            waitForIdleSync();
            check(activity instanceof AiCopilotActivity, "actual AI activity started");
            checkLayout();
            checkRenderer();
            checkKeyboardLayout();
            checkOverflowMenu();
            saveSyntheticScreenshot();
            checkMemoryConfirmationCancellation();
            checkEncryptedMemoryLifecycle();
            checkSettingsEntry();
            result = Activity.RESULT_OK;
        } catch (Throwable failure) {
            report.append("FAIL: ").append(failure.getClass().getSimpleName())
                    .append(": ").append(failure.getMessage()).append('\n');
        } finally {
            try {
                ui(this::cleanupSyntheticMemory);
            } catch (Throwable cleanup) {
                result = Activity.RESULT_CANCELED;
                report.append("FAIL: synthetic memory cleanup\n");
            }
            try {
                ui(() -> {
                    if (settings != null && !settings.isFinishing()) settings.finish();
                    if (activity != null && !activity.isFinishing()) activity.finish();
                });
                waitForIdleSync();
            } catch (Throwable cleanup) {
                result = Activity.RESULT_CANCELED;
                report.append("FAIL: activity cleanup\n");
            }
            try { restoreAppearance(); }
            catch (Throwable cleanup) {
                result = Activity.RESULT_CANCELED;
                report.append("FAIL: appearance restore\n");
            }
            if (result == Activity.RESULT_OK) report.append("PASS: ").append(assertions)
                    .append(" assertions; no API calls; synthetic memory cleaned up.\n");
            results.putInt("assertions", assertions);
            results.putString("stream", "\n" + report);
            results.putString("result", result == Activity.RESULT_OK ? "PASS" : "FAIL");
            finish(result, results);
        }
    }

    private void configureAppearance() {
        preferences = getTargetContext().getSharedPreferences("app_settings_v1", Context.MODE_PRIVATE);
        previousAppearance = preferences.getString("appearance_mode", null);
        previousLanguage = preferences.getString("language", null);
        String appearance = arguments.getString("appearance");
        String language = arguments.getString("language");
        SharedPreferences.Editor editor = preferences.edit();
        if (appearance != null) {
            check("DARK".equals(appearance) || "LIGHT".equals(appearance), "valid appearance argument");
            editor.putString("appearance_mode", appearance);
        }
        if (language != null) {
            check("en".equals(language) || "zh".equals(language), "valid language argument");
            editor.putString("language", "zh".equals(language) ? "zh-CN" : "en");
        }
        check(editor.commit(), "isolated app appearance preferences set");
        if (language != null) {
            if (Build.VERSION.SDK_INT >= 33) {
                // Android 13+ persists app locale separately from application preferences.
                // Seed that app-only state too: there is no active AppCompat delegate before launch.
                LocaleManager locales = getTargetContext().getSystemService(LocaleManager.class);
                previousSystemLocales = locales.getApplicationLocales();
                locales.setApplicationLocales(LocaleList.forLanguageTags("zh".equals(language) ? "zh-CN" : "en"));
            } else {
                ui(() -> AppLocaleController.applyStoredLanguage(getTargetContext()));
            }
        }
    }

    private void restoreAppearance() {
        if (preferences == null) return;
        SharedPreferences.Editor editor = preferences.edit();
        if (previousAppearance == null) editor.remove("appearance_mode");
        else editor.putString("appearance_mode", previousAppearance);
        if (previousLanguage == null) editor.remove("language");
        else editor.putString("language", previousLanguage);
        check(editor.commit(), "original app preferences restored");
        ui(() -> {
            if (Build.VERSION.SDK_INT >= 33 && previousSystemLocales != null) {
                getTargetContext().getSystemService(LocaleManager.class).setApplicationLocales(previousSystemLocales);
            } else if (Build.VERSION.SDK_INT < 33) {
                AppLocaleController.applyStoredLanguage(getTargetContext());
            }
            AppAppearanceController.applyNightMode(new AppSettingsStore(getTargetContext()).load().getAppearanceMode());
        });
    }

    private void checkLayout() {
        ui(() -> {
            View root = required(R.id.ai_copilot_root);
            View scroll = required(R.id.ai_chat_scroll);
            check(root.getWidth() > 0 && root.getHeight() > 0, "activity has measured bounds");
            String appearance = arguments.getString("appearance");
            if (appearance != null) {
                int actual = activity.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
                check(actual == ("DARK".equals(appearance) ? Configuration.UI_MODE_NIGHT_YES : Configuration.UI_MODE_NIGHT_NO),
                        "requested appearance applied to activity");
            }
            String language = arguments.getString("language");
            if (language != null) check(language.equals(activity.getResources().getConfiguration().locale.getLanguage()),
                    "requested language applied to activity");
            check(scroll.getHeight() > root.getHeight() / 4,
                    "collapsed context leaves useful conversation height");
            check(required(R.id.ai_chat_context_panel).getVisibility() == View.GONE,
                    "context details initially collapsed");
            check(required(R.id.ai_chat_memory_manage).isShown(), "memory manager visible");
            check(required(R.id.ai_chat_used_memory).isShown(), "used memory entry visible");
            check(required(R.id.ai_copilot_generate).isShown(), "send control visible");
            int oldRemember = activity.getResources().getIdentifier(
                    "ai_copilot_remember", "id", getTargetContext().getPackageName());
            check(oldRemember == 0 || activity.findViewById(oldRemember) == null,
                    "automatic-save memory checkbox absent");
            check(required(R.id.ai_chat_context_toggle).performClick(), "context toggle clickable");
        });
        waitForIdleSync();
        ui(() -> {
            check(required(R.id.ai_chat_context_panel).getVisibility() == View.VISIBLE,
                    "context details expand");
            check(required(R.id.ai_chat_use_memory).getVisibility() == View.VISIBLE,
                    "explicit use-confirmed-memory option exists");
            check(required(R.id.ai_copilot_include_terminal).getVisibility() == View.VISIBLE,
                    "terminal inclusion option retained");
            required(R.id.ai_chat_context_toggle).performClick();
        });
        waitForIdleSync();
    }

    private void checkRenderer() {
        ui(() -> {
            long now = System.currentTimeMillis();
            String content = "## 排查建议 / Diagnostic result\n\n**Bold fixture**\n\n先检查网线与端口指示灯，再查看接口状态。\n\n- Check cable / 检查网线\n\n```text\n"
                    + CODE + "\n```";
            AiChatConversation conversation = AiChatConversation.create("ui_fixture", "Synthetic switch", now)
                    .append(new AiChatMessage(AiChatRole.USER, "我是网络新手，交换机端口不通应该先检查什么？", now))
                    .append(new AiChatMessage(AiChatRole.ASSISTANT, content, now));
            DeviceProfile device = new DeviceProfile("Synthetic switch", Vendor.GENERIC, CliMode.UNKNOWN, 9600);
            AiChatMessageRenderer renderer = new AiChatMessageRenderer(activity, device,
                    new AiChatMessageRenderer.Actions() {
                        @Override public void copy(String text) { }
                        @Override public void reviewCommand(AiSuggestedCommand command) {
                            throw new AssertionError("test must never review or execute a command");
                        }
                        @Override public void remember(String text) { }
                    });
            LinearLayout messages = activity.findViewById(R.id.ai_chat_messages);
            ScrollView scroll = activity.findViewById(R.id.ai_chat_scroll);
            renderer.render(conversation, messages, scroll);
            List<TextView> texts = new ArrayList<>();
            collectText(messages, texts);
            boolean hasBold = false;
            boolean exactCode = false;
            for (TextView view : texts) {
                CharSequence text = view.getText();
                check(!text.toString().contains("**Bold fixture**"), "bold Markdown markers not exposed");
                if (text.toString().contains("Bold fixture") && text instanceof Spanned) {
                    Spanned styled = (Spanned) text;
                    for (StyleSpan span : styled.getSpans(0, styled.length(), StyleSpan.class)) {
                        int begin = styled.getSpanStart(span);
                        int end = styled.getSpanEnd(span);
                        if ((span.getStyle() & Typeface.BOLD) != 0
                                && styled.subSequence(begin, end).toString().contains("Bold fixture")) hasBold = true;
                    }
                }
                if ((CODE + "\n").equals(text.toString())) exactCode = true;
            }
            check(hasBold, "actual renderer emits a bold span");
            check(exactCode, "code preserves wildcard, literal asterisks and newline exactly");
        });
        waitForIdleSync();
    }

    private void checkKeyboardLayout() {
        ui(() -> {
            View input = required(R.id.ai_copilot_intent);
            input.requestFocus();
            InputMethodManager keyboard = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            keyboard.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
        });
        long deadline = SystemClock.uptimeMillis() + 4000;
        final boolean[] visible = {false};
        do {
            waitForIdleSync();
            ui(() -> {
                WindowInsetsCompat insets = ViewCompat.getRootWindowInsets(required(R.id.ai_copilot_root));
                visible[0] = insets != null && insets.isVisible(WindowInsetsCompat.Type.ime());
            });
            if (visible[0]) break;
            SystemClock.sleep(100);
        } while (SystemClock.uptimeMillis() < deadline);
        check(visible[0], "soft keyboard actually visible");
        // Wait for resize/IME animation to settle before checking actual visible rectangles.
        SystemClock.sleep(350);
        waitForIdleSync();
        try {
            ui(() -> {
                Rect frame = new Rect();
                activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
                for (int id : new int[] {R.id.ai_copilot_intent, R.id.ai_copilot_generate}) {
                    View view = required(id);
                    Rect visibleBounds = new Rect();
                    check(view.isShown() && view.getGlobalVisibleRect(visibleBounds)
                                    && visibleBounds.height() > 0 && visibleBounds.bottom <= frame.bottom,
                            id == R.id.ai_copilot_intent ? "input remains visible above keyboard" : "send remains visible above keyboard");
                }
                Rect conversation = new Rect();
                check(required(R.id.ai_chat_scroll).getGlobalVisibleRect(conversation) && conversation.height() > 0,
                        "conversation remains visible with keyboard");
            });
        } finally {
            ui(() -> {
                InputMethodManager keyboard = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                keyboard.hideSoftInputFromWindow(required(R.id.ai_copilot_intent).getWindowToken(), 0);
                required(R.id.ai_copilot_intent).clearFocus();
            });
            SystemClock.sleep(350);
            waitForIdleSync();
        }
    }

    private void checkOverflowMenu() {
        ui(() -> {
            Toolbar toolbar = activity.findViewById(R.id.ai_copilot_toolbar);
            check(toolbar.showOverflowMenu(), "overflow menu opens");
        });
        waitForIdleSync();
        AccessibilityNodeInfo label = awaitText(activity.getString(R.string.ai_chat_new));
        check(label != null && label.isVisibleToUser(), "overflow menu item text accessible and visible");
        if (label != null) label.recycle();
        ui(() -> {
            Toolbar toolbar = activity.findViewById(R.id.ai_copilot_toolbar);
            check(toolbar.hideOverflowMenu(), "overflow menu closes");
        });
        waitForIdleSync();
    }

    private void saveSyntheticScreenshot() {
        if (!"true".equalsIgnoreCase(arguments.getString("screenshot"))) return;
        ui(() -> ((ScrollView) required(R.id.ai_chat_scroll)).scrollTo(0, 0));
        waitForIdleSync();
        ui(() -> {
            View decor = activity.getWindow().getDecorView();
            Bitmap image = Bitmap.createBitmap(decor.getWidth(), decor.getHeight(), Bitmap.Config.ARGB_8888);
            try {
                decor.draw(new Canvas(image));
                File directory = getTargetContext().getExternalFilesDir(null);
                check(directory != null, "synthetic screenshot output directory available");
                File file = new File(directory, "ai-chat-synthetic.png");
                try (FileOutputStream output = new FileOutputStream(file)) {
                    check(image.compress(Bitmap.CompressFormat.PNG, 100, output), "synthetic activity screenshot written");
                }
                report.append("Screenshot: ").append(file.getAbsolutePath()).append('\n');
            } catch (IOException error) {
                throw new AssertionError("synthetic screenshot write failed", error);
            } finally { image.recycle(); }
        });
    }

    private void checkEncryptedMemoryLifecycle() {
        final ChatMemoryController[] controller = new ChatMemoryController[1];
        ui(() -> {
            controller[0] = new ChatMemoryController(activity, subject, null);
            controller[0].confirm(fixture);
        });
        waitForIdleSync();
        memoryAttempted = true;
        clickDialogText(activity.getString(R.string.chat_memory_save));
        ui(() -> {
            ConfirmedMemoryStore reloaded = memoryStore();
            MemoryRecord saved = findFixture(reloaded);
            check(saved != null, "confirmed UI save survives a new encrypted store instance");
            fixtureId = saved.getId();
            check(saved.getScope() == MemoryScope.GLOBAL, "default confirmation scope is global preferences");
            boolean recalled = false;
            for (MemoryRecord record : new ChatMemoryController(activity, subject, null).recall()) {
                if (record.getId().equals(fixtureId)) recalled = true;
            }
            check(recalled, "new controller recalls confirmed record");
            byte[] encrypted = new SharedPreferencesCredentialRecordStore(activity)
                    .load("confirmed-chat-memory-v1").getCiphertext();
            check(encrypted.length > 0 && !new String(encrypted, StandardCharsets.UTF_8).contains(fixture),
                    "stored credential bytes do not contain synthetic memory plaintext");
            reloaded.save(fixtureId, MemoryScope.GLOBAL, subject, fixture + " Edited.", System.currentTimeMillis());
            MemoryRecord edited = findFixture(memoryStore());
            check(edited != null && edited.getContent().equals(fixture + " Edited."),
                    "editing persists atomically through encrypted reload");
            memoryStore().delete(fixtureId, subject);
            check(findFixture(memoryStore()) == null, "deleting persists through encrypted reload");
            fixtureId = null;
            memoryAttempted = false;
        });
    }

    private ConfirmedMemoryStore memoryStore() {
        return new ConfirmedMemoryStore(new EncryptedConfirmedMemoryPersistence(getTargetContext()));
    }

    private MemoryRecord findFixture(ConfirmedMemoryStore store) {
        for (MemoryRecord record : store.list(subject, System.currentTimeMillis())) {
            if (record.getContent().equals(fixture) || record.getContent().equals(fixture + " Edited.")) return record;
        }
        return null;
    }

    private void cleanupSyntheticMemory() {
        if (!memoryAttempted) return;
        ConfirmedMemoryStore store = memoryStore();
        MemoryRecord record = findFixture(store);
        if (record != null) store.delete(record.getId(), subject);
        if (findFixture(memoryStore()) != null) throw new AssertionError("synthetic memory cleanup failed");
    }

    private void checkMemoryConfirmationCancellation() {
        final ChatMemoryController[] controller = new ChatMemoryController[1];
        final int[] before = new int[1];
        final int[] changed = new int[1];
        ui(() -> {
            controller[0] = new ChatMemoryController(activity, "synthetic-ui-fixture", () -> changed[0]++);
            before[0] = controller[0].recall().size();
            controller[0].confirm(DRAFT);
        });
        waitForIdleSync();
        AccessibilityNodeInfo draft = awaitText(DRAFT);
        check(draft != null, "confirmation shows editable draft before any save");
        check(draft != null && draft.isEditable(), "memory draft is editable");
        if (draft != null) draft.recycle();
        clickDialogCancel();
        ui(() -> {
            check(controller[0].recall().size() == before[0], "cancel does not change recalled memories");
            check(changed[0] == 0, "cancel does not notify a memory write");
            controller[0].showManager();
        });
        waitForIdleSync();
        AccessibilityNodeInfo manager = awaitText(activity.getString(R.string.chat_memory_title));
        check(manager != null, "production memory manager opens");
        if (manager != null) manager.recycle();
        clickDialogCancel();
    }

    private void checkSettingsEntry() {
        ActivityMonitor monitor = addMonitor(AiProviderSettingsActivity.class.getName(), null, false);
        try {
            ui(() -> {
                Toolbar toolbar = activity.findViewById(R.id.ai_copilot_toolbar);
                MenuItem item = toolbar.getMenu().findItem(R.id.ai_chat_settings);
                check(item != null && item.isVisible() && item.getIcon() != null,
                        "settings menu item has visible icon");
                View action = toolbar.findViewById(R.id.ai_chat_settings);
                check(action != null && action.isShown() && action.getWidth() > 0,
                        "settings is a real always-visible toolbar action");
                check(action != null && action.performClick(), "settings icon is clickable");
            });
            settings = waitForMonitorWithTimeout(monitor, 5000);
            check(settings instanceof AiProviderSettingsActivity, "settings icon opens provider settings");
        } finally {
            removeMonitor(monitor);
        }
    }

    private void clickDialogCancel() {
        clickDialogText(activity.getString(android.R.string.cancel));
    }

    private void clickDialogText(String label) {
        AccessibilityNodeInfo action = awaitText(label);
        check(action != null, "dialog has requested explicit action");
        if (action != null) {
            check(action.performAction(AccessibilityNodeInfo.ACTION_CLICK), "dialog action accepted");
            action.recycle();
        }
        waitForIdleSync();
        SystemClock.sleep(120);
    }

    private AccessibilityNodeInfo awaitText(String expected) {
        long until = SystemClock.uptimeMillis() + 3000;
        do {
            AccessibilityNodeInfo root = getUiAutomation().getRootInActiveWindow();
            if (root != null) {
                AccessibilityNodeInfo result = findExactText(root, expected);
                root.recycle();
                if (result != null) return result;
            }
            SystemClock.sleep(100);
        } while (SystemClock.uptimeMillis() < until);
        return null;
    }

    private AccessibilityNodeInfo findExactText(AccessibilityNodeInfo node, String text) {
        if (node.getText() != null && (text.equals(node.getText().toString())
                || ("android.widget.Button".contentEquals(node.getClassName())
                && text.equalsIgnoreCase(node.getText().toString())))) return AccessibilityNodeInfo.obtain(node);
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child == null) continue;
            AccessibilityNodeInfo found = findExactText(child, text);
            child.recycle();
            if (found != null) return found;
        }
        return null;
    }

    private View required(int id) {
        View view = activity.findViewById(id);
        if (view == null) throw new AssertionError("missing required view " + id);
        return view;
    }

    private void collectText(View view, List<TextView> texts) {
        if (view instanceof TextView) texts.add((TextView) view);
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) collectText(group.getChildAt(i), texts);
        }
    }

    private void check(boolean condition, String name) {
        if (!condition) throw new AssertionError(name);
        assertions++;
        report.append("OK ").append(name).append('\n');
    }

    /** Propagates failures to the runner instead of killing the application's main thread. */
    private void ui(Runnable action) {
        AtomicReference<Throwable> failure = new AtomicReference<>();
        runOnMainSync(() -> {
            try { action.run(); } catch (Throwable error) { failure.set(error); }
        });
        if (failure.get() != null) throw new AssertionError(failure.get().getMessage(), failure.get());
    }
}
