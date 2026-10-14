package de.kai_morich.simple_usb_terminal;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import com.chenwei666.netserial.guided.GuidedScenario;

/** Localized presentation shared by the home screen and diagnostic guide. */
public final class BeginnerTaskCatalog {
    private BeginnerTaskCatalog() { }

    @StringRes public static int title(GuidedScenario scenario) {
        switch (scenario) {
            case NO_INTERNET: return R.string.beginner_task_internet;
            case SLOW_NETWORK: return R.string.beginner_task_slow;
            case DNS_FAILURE: return R.string.beginner_task_dns;
            case SWITCH_LOGIN: return R.string.beginner_task_login;
            case PORT_LINK: return R.string.beginner_task_port;
            case VLAN_GATEWAY: return R.string.beginner_task_vlan;
            case IDENTIFY_DEVICE: return R.string.beginner_task_identify;
            case SWITCH_HEALTH: return R.string.beginner_task_health;
            case CONFIG_BACKUP: return R.string.beginner_task_backup;
            case ASK_AI: return R.string.beginner_task_ai;
            default: throw new IllegalArgumentException("Unknown scenario: " + scenario);
        }
    }

    @StringRes public static int description(GuidedScenario scenario) {
        switch (scenario) {
            case NO_INTERNET: return R.string.beginner_task_internet_desc;
            case SLOW_NETWORK: return R.string.beginner_task_slow_desc;
            case DNS_FAILURE: return R.string.beginner_task_dns_desc;
            case SWITCH_LOGIN: return R.string.beginner_task_login_desc;
            case PORT_LINK: return R.string.beginner_task_port_desc;
            case VLAN_GATEWAY: return R.string.beginner_task_vlan_desc;
            case IDENTIFY_DEVICE: return R.string.beginner_task_identify_desc;
            case SWITCH_HEALTH: return R.string.beginner_task_health_desc;
            case CONFIG_BACKUP: return R.string.beginner_task_backup_desc;
            case ASK_AI: return R.string.beginner_task_ai_desc;
            default: throw new IllegalArgumentException("Unknown scenario: " + scenario);
        }
    }

    @DrawableRes public static int icon(GuidedScenario scenario) {
        switch (scenario) {
            case NO_INTERNET: return R.drawable.beginner_network;
            case SLOW_NETWORK: return R.drawable.beginner_speed;
            case DNS_FAILURE: return R.drawable.beginner_network;
            case SWITCH_LOGIN: return R.drawable.ic_nav_terminal;
            case PORT_LINK: return R.drawable.ic_nav_connections;
            case VLAN_GATEWAY: return R.drawable.beginner_network;
            case IDENTIFY_DEVICE: return R.drawable.ic_search_white_24dp;
            case SWITCH_HEALTH: return R.drawable.beginner_health;
            case CONFIG_BACKUP: return R.drawable.beginner_backup;
            case ASK_AI: return R.drawable.beginner_chat;
            default: throw new IllegalArgumentException("Unknown scenario: " + scenario);
        }
    }

    /** Common symptom vocabulary supplements the short labels in either UI language. */
    static String searchAliases(GuidedScenario scenario) {
        switch (scenario) {
            case NO_INTERNET: return "断网 无网络 不能上网 wifi internet offline connectivity ping tcp";
            case SLOW_NETWORK: return "卡顿 掉线 延迟 丢包 速度 slow unstable latency packet loss mtu trace";
            case DNS_FAILURE: return "域名 解析 dns domain website url";
            case SWITCH_LOGIN: return "登录 登陆 密码 ssh telnet login password";
            case PORT_LINK: return "网线 端口 光口 接口 链路 cable port interface link";
            case VLAN_GATEWAY: return "地址 网段 dhcp vlan gateway address subnet";
            case IDENTIFY_DEVICE: return "品牌 厂商 识别 华三 华为 思科 锐捷 h3c huawei cisco ruijie vendor identify";
            case SWITCH_HEALTH: return "健康 巡检 温度 告警 health inspection temperature warning";
            case CONFIG_BACKUP: return "备份 配置 对比 恢复 backup config compare snapshot";
            case ASK_AI: return "ai 人工智能 帮助 help chat question";
            default: throw new IllegalArgumentException("Unknown scenario: " + scenario);
        }
    }
}
