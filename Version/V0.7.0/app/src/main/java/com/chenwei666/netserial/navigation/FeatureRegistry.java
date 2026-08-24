package com.chenwei666.netserial.navigation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import de.kai_morich.simple_usb_terminal.R;

/** The single source of truth for discoverable application features. */
public final class FeatureRegistry {
    private final List<FeatureDescriptor> features;
    private final Map<FeatureId, FeatureDescriptor> byId;

    public FeatureRegistry(List<FeatureDescriptor> features) {
        if (features == null || features.isEmpty()) throw new IllegalArgumentException("Features required");
        List<FeatureDescriptor> copy = new ArrayList<>(features);
        EnumMap<FeatureId, FeatureDescriptor> index = new EnumMap<>(FeatureId.class);
        for (FeatureDescriptor feature : copy) {
            if (index.put(feature.getId(), feature) != null) {
                throw new IllegalArgumentException("Duplicate feature " + feature.getId());
            }
        }
        this.features = Collections.unmodifiableList(copy);
        this.byId = Collections.unmodifiableMap(index);
    }

    public static FeatureRegistry createDefault() {
        List<FeatureDescriptor> values = new ArrayList<>();
        values.add(feature(FeatureId.USB_CONNECTIONS, FeatureCategory.CONNECTIONS,
                R.string.nav_connections, R.string.feature_usb_summary, "usb serial 串口 连接"));
        values.add(feature(FeatureId.REMOTE_CONNECTIONS, FeatureCategory.CONNECTIONS,
                R.string.remote_terminal_menu, R.string.feature_remote_summary, "ssh telnet remote 远程 堡垒机"));
        values.add(feature(FeatureId.SESSION_WORKSPACE, FeatureCategory.CONNECTIONS,
                R.string.sessions_menu, R.string.feature_sessions_summary, "session workspace 会话 多窗口"));
        values.add(feature(FeatureId.TERMINAL_WORKSPACE, FeatureCategory.TERMINAL,
                R.string.nav_terminal, R.string.terminal_hub_subtitle, "terminal cli console 终端 命令行"));
        values.add(feature(FeatureId.AI_COPILOT, FeatureCategory.TOOLBOX,
                R.string.ai_copilot_short, R.string.feature_ai_summary, "ai copilot diagnose 诊断 记忆"));
        values.add(feature(FeatureId.COMMAND_LIBRARY, FeatureCategory.TOOLBOX,
                R.string.command_library_menu, R.string.feature_commands_summary, "command tab autocomplete 命令 补全"));
        values.add(feature(FeatureId.NETWORK_TOOLS, FeatureCategory.TOOLBOX,
                R.string.network_tools_menu, R.string.feature_network_summary, "ping dns trace tcp mtu mac cidr 网络"));
        values.add(feature(FeatureId.OPERATIONS_CENTER, FeatureCategory.TOOLBOX,
                R.string.operations_title, R.string.feature_operations_summary, "health compliance batch topology 运维 巡检 合规"));
        values.add(feature(FeatureId.ADVANCED_TOOLKIT, FeatureCategory.TOOLBOX,
                R.string.advanced_toolkit_title, R.string.feature_advanced_summary,
                "credential vault http tftp transfer runbook signature 凭据 文件传输 运行手册 签名"));
        values.add(feature(FeatureId.CHANGE_TASK, FeatureCategory.TOOLBOX,
                R.string.change_menu, R.string.feature_change_summary, "change ticket evidence rollback 变更 回滚"));
        values.add(feature(FeatureId.CONFIG_DIFF, FeatureCategory.TOOLBOX,
                R.string.config_diff_menu, R.string.feature_diff_summary, "config diff compare 配置 对比"));
        values.add(feature(FeatureId.CONFIG_SNAPSHOTS, FeatureCategory.TOOLBOX,
                R.string.snapshot_menu, R.string.feature_snapshots_summary, "backup snapshot history 备份 快照"));
        values.add(feature(FeatureId.DEVICE_MEMORY, FeatureCategory.SETTINGS,
                R.string.terminal_profile_menu, R.string.feature_memory_summary, "device profile memory 设备 档案 记忆"));
        values.add(feature(FeatureId.APP_SETTINGS, FeatureCategory.SETTINGS,
                R.string.app_settings_menu, R.string.feature_app_settings_summary, "language theme update terminal settings 语言 主题 更新"));
        values.add(feature(FeatureId.AI_SETTINGS, FeatureCategory.SETTINGS,
                R.string.ai_settings_menu, R.string.feature_ai_settings_summary, "provider api key model endpoint 厂商 密钥 模型"));
        return new FeatureRegistry(values);
    }

    public List<FeatureDescriptor> all() { return features; }

    public FeatureDescriptor require(FeatureId id) {
        FeatureDescriptor result = byId.get(id);
        if (result == null) throw new IllegalArgumentException("Unknown feature " + id);
        return result;
    }

    public List<FeatureDescriptor> forCategory(FeatureCategory category) {
        List<FeatureDescriptor> result = new ArrayList<>();
        for (FeatureDescriptor feature : features) if (feature.getCategory() == category) result.add(feature);
        return Collections.unmodifiableList(result);
    }

    public List<FeatureDescriptor> search(String query, FeatureTextResolver resolver) {
        if (resolver == null) throw new IllegalArgumentException("Text resolver required");
        List<FeatureDescriptor> result = new ArrayList<>();
        for (FeatureDescriptor feature : features) if (feature.matches(query, resolver)) result.add(feature);
        return Collections.unmodifiableList(result);
    }

    private static FeatureDescriptor feature(FeatureId id, FeatureCategory category,
                                             int title, int summary, String keywords) {
        return new FeatureDescriptor(id, category, title, summary, keywords);
    }
}
