package de.kai_morich.simple_usb_terminal;

import android.app.Activity;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.chenwei666.netserial.ai.AiChatConversation;
import com.chenwei666.netserial.ai.AiChatMessage;
import com.chenwei666.netserial.ai.AiChatRole;
import com.chenwei666.netserial.ai.AiCommandExtractor;
import com.chenwei666.netserial.ai.AiSuggestedCommand;
import com.chenwei666.netserial.device.DeviceProfile;
import com.chenwei666.netserial.safety.RiskLevel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

/** Renders a bounded AI transcript and delegates every user action to the activity. */
final class AiChatMessageRenderer {
    interface Actions {
        void copy(String text);

        void reviewCommand(AiSuggestedCommand command);
    }

    private final Activity activity;
    private final DeviceProfile device;
    private final Actions actions;
    private final AiCommandExtractor commandExtractor = new AiCommandExtractor();

    AiChatMessageRenderer(Activity activity, DeviceProfile device, Actions actions) {
        this.activity = activity;
        this.device = device;
        this.actions = actions;
    }

    void render(AiChatConversation conversation, LinearLayout container, ScrollView scroll) {
        container.removeAllViews();
        if (conversation.getMessages().isEmpty()) {
            TextView welcome = new TextView(activity);
            welcome.setText(R.string.ai_chat_welcome);
            welcome.setPadding(dp(12), dp(24), dp(12), dp(24));
            container.addView(welcome);
            return;
        }
        for (AiChatMessage message : conversation.getMessages()) {
            addMessageCard(message, container);
        }
        scroll.post(() -> scroll.fullScroll(View.FOCUS_DOWN));
    }

    private void addMessageCard(AiChatMessage message, LinearLayout container) {
        boolean user = message.getRole() == AiChatRole.USER;
        MaterialCardView card = new MaterialCardView(activity);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(user ? dp(48) : 0, dp(5), user ? 0 : dp(48), dp(5));
        cardParams.gravity = user ? Gravity.END : Gravity.START;
        card.setLayoutParams(cardParams);
        card.setRadius(dp(16));
        card.setCardElevation(0);
        card.setStrokeWidth(dp(1));

        LinearLayout body = new LinearLayout(activity);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(12), dp(10), dp(12), dp(8));
        TextView role = new TextView(activity);
        role.setText(user ? R.string.ai_chat_you : R.string.ai_chat_assistant);
        role.setTextAppearance(activity,
                androidx.appcompat.R.style.TextAppearance_AppCompat_Caption);
        body.addView(role);
        TextView content = new TextView(activity);
        content.setText(message.getContent());
        content.setTextIsSelectable(true);
        content.setPadding(0, dp(5), 0, dp(4));
        body.addView(content);

        LinearLayout actionRow = new LinearLayout(activity);
        actionRow.setOrientation(LinearLayout.HORIZONTAL);
        MaterialButton copy = compactButton(activity.getString(R.string.ai_chat_copy));
        copy.setOnClickListener(view -> actions.copy(message.getContent()));
        actionRow.addView(copy);
        if (!user) addCommandActions(message, actionRow);
        body.addView(actionRow);
        card.addView(body);
        container.addView(card);
    }

    private void addCommandActions(AiChatMessage message, LinearLayout actionRow) {
        for (AiSuggestedCommand command : commandExtractor.extract(
                message.getContent(), device.getVendor(), device.getCliMode())) {
            MaterialButton action = compactButton(activity.getString(
                    R.string.ai_chat_load_command, command.getRisk().name()));
            if (command.getRisk() == RiskLevel.R4_CRITICAL) {
                action.setEnabled(false);
            } else {
                action.setOnClickListener(view -> actions.reviewCommand(command));
            }
            actionRow.addView(action);
        }
    }

    private MaterialButton compactButton(String text) {
        MaterialButton button = new MaterialButton(activity, null,
                com.google.android.material.R.attr.materialButtonOutlinedStyle);
        button.setText(text);
        button.setTextSize(11);
        button.setAllCaps(false);
        button.setMinHeight(0);
        button.setMinimumHeight(0);
        button.setInsetTop(0);
        button.setInsetBottom(0);
        return button;
    }

    private int dp(int value) {
        return Math.round(value * activity.getResources().getDisplayMetrics().density);
    }
}
