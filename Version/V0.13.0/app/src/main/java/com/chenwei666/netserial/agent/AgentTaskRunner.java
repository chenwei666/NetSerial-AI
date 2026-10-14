package com.chenwei666.netserial.agent;

import android.content.Context;
import android.os.Build;
import com.chenwei666.netserial.ai.*;
import com.chenwei666.netserial.change.ChangeTaskStore;
import com.chenwei666.netserial.device.*;
import com.chenwei666.netserial.memory.ConfirmedMemoryStore;
import com.chenwei666.netserial.memory.EncryptedConfirmedMemoryPersistence;
import com.chenwei666.netserial.memory.MemoryRecord;
import com.chenwei666.netserial.safety.*;
import com.chenwei666.netserial.web.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CancellationException;

/** App adapter: existing configured providers, one live lease, local secret and target gates. */
public final class AgentTaskRunner implements AutoCloseable {
    public interface Listener {
        void onEvent(String text);
        String ask(String question, RequestCancellation cancellation) throws Exception;
        boolean confirm(String target, String command, RequestCancellation cancellation) throws Exception;
        char[] requestWebPassword(String username, RequestCancellation cancellation) throws Exception;
    }
    private final Context context;
    private final String token;
    private final Listener listener;
    private final boolean chinese;
    private volatile boolean closed;
    private volatile AgentSessionBridge.Lease lease;
    private volatile RequestCancellation active;
    private DeviceProfile initialProfile;

    public AgentTaskRunner(Context context, String token, Listener listener) {
        this.context = context.getApplicationContext(); this.token = token; this.listener = listener;
        chinese = Locale.getDefault().getLanguage().startsWith("zh");
    }
    public void run(String goal, RequestCancellation cancellation) throws Exception {
        if (closed || active != null) throw new IllegalStateException("Runner cannot be reused");
        active = cancellation;
        AgentSessionBridge.Lease current;
        try { current = AgentSessionBridge.shared().acquire(token); }
        catch (Exception failure) { listener.onEvent(text("任务未开始：", "Task did not start: ") + safeFailure(failure)); throw failure; }
        lease = current;
        try {
        if (closed || cancellation.isCancelled()) { current.close(); throw new CancellationException(); }
        initialProfile = new DeviceProfileStore(context).load();
        List<ProviderProfile> profiles = orderedProfiles(new ProviderProfileManager(
                new SharedPreferencesProviderProfilePersistence(context)).load());
        if (profiles.isEmpty()) throw new IllegalStateException(text("请先在 AI 设置中选择厂商和模型。", "Configure a provider and model in AI settings first."));
        ProviderCredentialService credentials = new ProviderCredentialService(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                ? CredentialVaultFactory.create(context) : new UnavailableCredentialVault());
        // Pin this task to the selected provider. Changing providers must be an explicit new task.
        ProviderProfile selected = profiles.get(0);
        AiChatProvider provider = AiChatProviderFactory.create(selected, credentials);
        String memory = confirmedMemory();
        listener.onEvent(text("本次 AI：", "Task AI: ") + selected.getProviderId() + " / " + selected.getModel());
        AgentTaskEngine.Session session = new AgentTaskEngine.Session() {
            public String target() { return current.getTarget(); }
            public boolean valid() { return !closed && current.isValid(); }
            public String execute(String command, RequestCancellation signal) throws Exception { return current.execute(command, signal); }
        };
        AgentTaskEngine.Listener events = new AgentTaskEngine.Listener() {
            public void onEvent(String text) { listener.onEvent(text); }
            public String ask(String question, RequestCancellation signal) throws Exception { return listener.ask(question, signal); }
            public boolean confirm(String target, String command, RequestCancellation signal) throws Exception {
                return listener.confirm(target, command, signal);
            }
            public String webAccess(AgentAction action, Vendor vendor, String platform, RequestCancellation signal) throws Exception {
                return applyWeb(action, vendor, platform, signal);
            }
        };
            new AgentTaskEngine(new AgentTaskEngine.Model() {
                private Vendor vendor = initialProfile.getVendor();
                public void identified(Vendor detected, String platform) { vendor = detected; }
                public String next(List<AiChatMessage> messages) throws Exception {
                    checkTarget(false);
                    AiChatRequest request = new AiChatRequest(messages, vendor, CliMode.UNKNOWN,
                            initialProfile.getName(), "", memory, chinese ? "Simplified Chinese" : "English", true);
                    return provider.chat(request, cancellation).getContent();
                }
            }, session, events, this::checkTarget, chinese).run(goal, cancellation);
        } catch (Exception failure) {
            if (!cancellation.isCancelled()) listener.onEvent(text("任务停止：", "Task stopped: ") + safeFailure(failure));
            throw failure;
        } finally { current.close(); }
    }

    private void checkTarget(boolean high) throws Exception {
        if (closed || active == null || active.isCancelled() || lease == null || !lease.isValid()) throw new CancellationException();
        DeviceProfile now = new DeviceProfileStore(context).load();
        if (!initialProfile.getName().equals(now.getName())
                || !initialProfile.getManagementAddress().equals(now.getManagementAddress())
                || initialProfile.getEnvironment() != now.getEnvironment()
                || initialProfile.isProtectedDevice() != now.isProtectedDevice()
                || initialProfile.getBaudRate() != now.getBaudRate()) {
            throw new IllegalStateException(text("设备档案或目标发生变化，请从终端重新进入。", "Device profile/target changed. Reopen from the terminal."));
        }
        if (high) {
            TargetSafetyDecision decision = new TargetSafetyPolicy().evaluate(now, lease.getTarget(), RiskLevel.R3_HIGH,
                    new ChangeTaskStore(context).loadActive(), System.currentTimeMillis());
            if (!decision.isAllowed()) throw new IllegalStateException(text("受保护设备的变更任务或维护窗口不符合要求。", "Protected target needs a matching authorized change task/window."));
        }
    }

    private String applyWeb(AgentAction action, Vendor vendor, String platform, RequestCancellation signal) throws Exception {
        checkTarget(true);
        if (vendor == Vendor.H3C_COMWARE && !("Comware 5".equals(platform) || "Comware 7".equals(platform)))
            throw new IllegalStateException(text("尚未识别 Comware 5/7，不能猜测 Web 模板。请读取完整版本信息。", "Comware 5/7 was not identified. Read full version information before using a Web template."));
        char[] password = listener.requestWebPassword(action.getUsername(), signal);
        if (password == null) throw new CancellationException();
        try (WebAccessRequest request = new WebAccessRequest(vendor, platform, action.getUsername(), password,
                action.isHttps(), action.isHttp()); WebAccessPlan plan = new WebAccessPlanFactory().create(request)) {
            String summary = text("本地 Web 管理配置；可能创建或覆盖管理员及其权限。\n", "Local Web configuration may create/replace administrator credentials and privileges.\n")
                    + (action.isHttp() ? text("包含明文 HTTP。\n", "Includes plaintext HTTP.\n") : "") + plan.redactedBatch();
            if (!listener.confirm(lease.getTarget(), summary, signal)) throw new CancellationException();
            checkTarget(true);
            List<String> commands = plan.getCommands();
            List<String> redacted = plan.getRedactedCommands();
            for (int i = 0; i < commands.size(); i++) {
                checkTarget(true);
                listener.onEvent(text("本地模板下发：", "Local template command: ") + redacted.get(i));
                if (!commands.get(i).equals(redacted.get(i))) {
                    lease.executeSensitive(commands.get(i), redacted.get(i), password, signal);
                } else lease.execute(commands.get(i), signal);
            }
            // These are trusted local template queries, not arbitrary model pipe/redirect instructions.
            for (String command : plan.getVerificationCommands()) {
                checkTarget(true);
                listener.onEvent(text("本地检查命令：", "Local check command: ") + command);
                lease.execute(command, signal);
            }
            return text("Web 模板命令已逐步下发并返回 CLI 提示符。为避免延迟口令回显泄露，后续输出已屏蔽；这不证明 Web 访问或账号登录成功。请重新连接后检查 Web 状态。配置未自动保存；不自动回退。",
                    "Web template commands were sent sequentially and CLI prompts returned. Output is withheld to protect delayed credential echoes; Web reachability/login is not verified. Reconnect and inspect Web state. Startup configuration was not saved; no automatic rollback.");
        } finally { Arrays.fill(password, '\0'); }
    }

    private String confirmedMemory() {
        boolean enabled = context.getSharedPreferences("ai_chat_ui_v1", Context.MODE_PRIVATE).getBoolean("use_confirmed_memory", true);
        if (!enabled || Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return "";
        String subject = "Default switch".equals(initialProfile.getName()) ? "" : initialProfile.getName();
        List<MemoryRecord> records = new ConfirmedMemoryStore(new EncryptedConfirmedMemoryPersistence(context))
                .recall(subject, 5, System.currentTimeMillis());
        StringBuilder result = new StringBuilder();
        for (MemoryRecord record : records) result.append("- ").append(record.getContent()).append('\n');
        listener.onEvent(text("本次使用已确认记忆：", "Confirmed memories used: ") + records.size());
        return result.toString();
    }
    private static List<ProviderProfile> orderedProfiles(ProviderProfilesState state) {
        List<ProviderProfile> result = new ArrayList<>();
        for (ProviderProfile p : state.getProfiles()) if (state.isActive(p)) result.add(p);
        // A task requires an explicitly selected profile; never silently choose another provider.
        return result;
    }
    private String safeFailure(Exception failure) {
        if (failure instanceof IllegalArgumentException)
            return text("参数或模型动作格式不符合要求，该动作未下发。请检查任务参数或选择能稳定输出 JSON 的模型。", "Invalid parameters or model action format; this action was not sent. Check parameters or choose a model with reliable JSON output.");
        if (failure instanceof AiProviderException || failure instanceof AiFailoverException)
            return text("AI 请求失败，请检查厂商设置、网络或额度；任务未重试。", "AI request failed. Check provider settings, network or quota; no retry was made.");
        String message = failure.getMessage();
        // Engine/bridge exceptions contain controlled reasons; raw provider and transport causes are hidden.
        if (failure instanceof IllegalStateException || failure instanceof java.io.IOException)
            return message == null ? text("执行未完成，请检查已有记录。", "Execution incomplete; inspect the recorded steps.")
                    : bounded(new com.chenwei666.netserial.terminal.SensitiveTextRedactor().redact(message));
        return text("执行未完成，请检查已有记录和设备状态。", "Execution incomplete; inspect recorded steps and device state.");
    }
    private String text(String zh, String en) { return chinese ? zh : en; }
    private static String bounded(String value) { return value.substring(0, Math.min(value.length(), 500)); }
    @Override public void close() {
        closed = true;
        RequestCancellation signal = active;
        if (signal != null) signal.cancel();
        AgentSessionBridge.Lease current = lease;
        if (current != null) current.close();
    }
}
