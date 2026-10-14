package de.kai_morich.simple_usb_terminal;

import android.app.Activity;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.chenwei666.netserial.ai.AiChatConversation;
import com.chenwei666.netserial.ai.AiChatMessage;
import com.chenwei666.netserial.ai.AiChatRole;
import com.chenwei666.netserial.ai.AiCommandExtractor;
import com.chenwei666.netserial.ai.AiSuggestedCommand;
import com.chenwei666.netserial.device.DeviceProfile;
import com.chenwei666.netserial.safety.RiskLevel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.MaterialColors;

import java.util.List;

/** Renders a bounded transcript; copy, remembering and command review remain explicit actions. */
final class AiChatMessageRenderer {
    interface Actions {
        void copy(String text);
        void remember(String text);
        void reviewCommand(AiSuggestedCommand command);
    }

    private final Activity activity;
    private final DeviceProfile device;
    private final Actions actions;
    private final AiCommandExtractor commandExtractor = new AiCommandExtractor();
    private final ChatMarkdownView markdown;

    AiChatMessageRenderer(Activity activity, DeviceProfile device, Actions actions) {
        this.activity = activity;
        this.device = device;
        this.actions = actions;
        this.markdown = new ChatMarkdownView(activity, actions::copy);
    }

    void render(AiChatConversation conversation, LinearLayout container, ScrollView scroll) {
        container.removeAllViews();
        if (conversation.getMessages().isEmpty()) {
            TextView welcome = new TextView(activity);
            welcome.setText(R.string.ai_chat_welcome);
            welcome.setTextSize(16);
            welcome.setLineSpacing(dp(5), 1.05f);
            welcome.setTextColor(MaterialColors.getColor(container,
                    com.google.android.material.R.attr.colorOnSurfaceVariant));
            welcome.setPadding(dp(16), dp(32), dp(16), dp(32));
            container.addView(welcome);
            return;
        }
        for (AiChatMessage message : conversation.getMessages()) addMessageCard(message, container);
        scroll.post(() -> scroll.fullScroll(View.FOCUS_DOWN));
    }

    private void addMessageCard(AiChatMessage message, LinearLayout container) {
        boolean user = message.getRole() == AiChatRole.USER;
        MaterialCardView card = new MaterialCardView(activity);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(-1, -2);
        cardParams.setMargins(user ? dp(20) : 0, dp(6), 0, dp(6));
        cardParams.gravity = user ? Gravity.END : Gravity.START;
        card.setLayoutParams(cardParams);
        card.setRadius(dp(18));
        card.setCardElevation(0);
        card.setStrokeWidth(user ? 0 : dp(1));
        card.setStrokeColor(MaterialColors.getColor(card, com.google.android.material.R.attr.colorOutlineVariant));
        card.setCardBackgroundColor(MaterialColors.getColor(card, user
                ? com.google.android.material.R.attr.colorPrimaryContainer
                : com.google.android.material.R.attr.colorSurface));

        LinearLayout body = new LinearLayout(activity);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(14), dp(12), dp(14), dp(6));
        TextView role = new TextView(activity);
        role.setText(user ? R.string.ai_chat_you : R.string.ai_chat_assistant);
        role.setTextSize(12);
        role.setTypeface(role.getTypeface(), Typeface.BOLD);
        role.setTextColor(MaterialColors.getColor(card, user
                ? com.google.android.material.R.attr.colorOnPrimaryContainer
                : androidx.appcompat.R.attr.colorPrimary));
        role.setPadding(0, 0, 0, dp(6));
        body.addView(role);
        if (user) {
            TextView content = new TextView(activity);
            content.setText(message.getContent());
            content.setTextSize(15);
            content.setLineSpacing(dp(3), 1.05f);
            content.setTextColor(MaterialColors.getColor(card,
                    com.google.android.material.R.attr.colorOnPrimaryContainer));
            content.setTextIsSelectable(true);
            content.setPadding(0, dp(4), 0, dp(6));
            body.addView(content);
        } else markdown.append(message.getContent(), body);

        // Only two weighted actions in this row; commands get a separate full-width entry.
        LinearLayout actionRow = new LinearLayout(activity);
        MaterialButton copy = compactButton(activity.getString(R.string.chat_render_copy_original));
        copy.setOnClickListener(view -> actions.copy(message.getContent()));
        MaterialButton remember = compactButton(activity.getString(R.string.chat_render_remember));
        remember.setOnClickListener(view -> actions.remember(message.getContent()));
        actionRow.addView(copy, new LinearLayout.LayoutParams(0, -2, 1));
        actionRow.addView(remember, new LinearLayout.LayoutParams(0, -2, 1));
        body.addView(actionRow, new LinearLayout.LayoutParams(-1, -2));
        if (!user) addCommandActions(message, body);
        card.addView(body);
        container.addView(card);
    }

    private void addCommandActions(AiChatMessage message, LinearLayout body) {
        // Security boundary: never extract a command from rendered/normalized Markdown text.
        List<AiSuggestedCommand> commands = commandExtractor.extract(
                message.getContent(), device.getVendor(), device.getCliMode());
        if (commands.isEmpty()) return;
        MaterialButton review = compactButton(activity.getString(R.string.chat_render_review_commands, commands.size()));
        review.setOnClickListener(view -> {
            String[] labels = new String[commands.size()];
            for (int i = 0; i < commands.size(); i++) {
                AiSuggestedCommand command = commands.get(i);
                labels[i] = command.getCommand() + "\n" + activity.getString(
                        command.getRisk() == RiskLevel.R4_CRITICAL
                                ? R.string.chat_render_command_blocked : R.string.chat_render_command_review);
            }
            new AlertDialog.Builder(activity)
                    .setTitle(R.string.chat_render_commands_title)
                    .setItems(labels, (dialog, position) -> {
                        AiSuggestedCommand command = commands.get(position);
                        if (command.getRisk() != RiskLevel.R4_CRITICAL) actions.reviewCommand(command);
                        else new AlertDialog.Builder(activity)
                                .setMessage(R.string.chat_render_command_blocked)
                                .setPositiveButton(android.R.string.ok, null).show();
                    })
                    .setNegativeButton(android.R.string.cancel, null).show();
        });
        body.addView(review, new LinearLayout.LayoutParams(-1, -2));
    }

    private MaterialButton compactButton(String text) {
        MaterialButton button = new MaterialButton(activity, null,
                androidx.appcompat.R.attr.borderlessButtonStyle);
        button.setText(text);
        button.setTextSize(12);
        button.setAllCaps(false);
        button.setMaxLines(2);
        button.setMinWidth(0);
        button.setMinimumWidth(0);
        button.setMinHeight(dp(48));
        button.setMinimumHeight(dp(48));
        button.setPadding(dp(4), dp(4), dp(4), dp(4));
        button.setInsetTop(0);
        button.setInsetBottom(0);
        return button;
    }

    private int dp(int value) {
        return Math.round(value * activity.getResources().getDisplayMetrics().density);
    }
}
