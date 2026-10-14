package de.kai_morich.simple_usb_terminal;

import android.app.Activity;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chenwei666.netserial.ai.ChatMarkdown;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.MaterialColors;

/** Native text only: no WebView, HTML parser, remote image loader, or clickable AI links. */
final class ChatMarkdownView {
    interface CopyAction { void copy(String value); }
    private final Activity activity;
    private final CopyAction copyAction;

    ChatMarkdownView(Activity activity, CopyAction copyAction) {
        this.activity = activity;
        this.copyAction = copyAction;
    }

    void append(String source, LinearLayout parent) {
        ChatMarkdown.Document document = ChatMarkdown.parse(source);
        for (ChatMarkdown.Block block : document.blocks) {
            if (block.kind == ChatMarkdown.Kind.CODE) {
                addCode(block, parent);
            } else if (block.kind == ChatMarkdown.Kind.RULE) {
                View rule = new View(activity);
                rule.setBackgroundColor(color(parent, com.google.android.material.R.attr.colorOutlineVariant));
                LinearLayout.LayoutParams parameters = new LinearLayout.LayoutParams(-1, dp(1));
                parameters.setMargins(0, dp(10), 0, dp(10));
                parent.addView(rule, parameters);
            } else {
                TextView text = textView(parent, 15);
                SpannableString formatted = new SpannableString(block.text);
                for (ChatMarkdown.Span span : block.spans) {
                    if (span.style == ChatMarkdown.Style.STRONG) {
                        formatted.setSpan(new StyleSpan(Typeface.BOLD), span.start, span.end,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    } else {
                        formatted.setSpan(new TypefaceSpan("monospace"), span.start, span.end,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        formatted.setSpan(new BackgroundColorSpan(color(parent,
                                com.google.android.material.R.attr.colorSurfaceContainerHighest)),
                                span.start, span.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
                text.setText(formatted);
                text.setPadding(0, dp(4), 0, dp(5));
                if (block.kind == ChatMarkdown.Kind.HEADING) {
                    text.setTypeface(text.getTypeface(), Typeface.BOLD);
                    text.setTextSize(block.level <= 2 ? 19 : 16);
                    text.setPadding(0, dp(10), 0, dp(5));
                } else if (block.kind == ChatMarkdown.Kind.QUOTE) {
                    text.setTextColor(color(parent, com.google.android.material.R.attr.colorOnSurfaceVariant));
                    text.setPadding(dp(12), dp(8), dp(12), dp(8));
                    text.setBackground(background(parent,
                            com.google.android.material.R.attr.colorSurfaceContainerHigh));
                }
                parent.addView(text, new LinearLayout.LayoutParams(-1, -2));
            }
        }
        if (document.truncated) {
            TextView note = textView(parent, 12);
            note.setText(R.string.chat_render_truncated);
            note.setTextColor(color(parent, com.google.android.material.R.attr.colorOnSurfaceVariant));
            note.setPadding(0, dp(8), 0, dp(8));
            parent.addView(note);
        }
    }

    private void addCode(ChatMarkdown.Block block, LinearLayout parent) {
        LinearLayout box = new LinearLayout(activity);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(12), dp(4), dp(12), dp(10));
        box.setBackground(background(parent, com.google.android.material.R.attr.colorSurfaceContainerHighest));
        LinearLayout.LayoutParams boxParameters = new LinearLayout.LayoutParams(-1, -2);
        boxParameters.setMargins(0, dp(8), 0, dp(8));

        LinearLayout header = new LinearLayout(activity);
        header.setGravity(Gravity.CENTER_VERTICAL);
        TextView label = textView(parent, 12);
        label.setText(block.language.isEmpty() ? activity.getString(R.string.chat_render_code) : block.language);
        label.setMaxLines(2);
        label.setTextColor(color(parent, com.google.android.material.R.attr.colorOnSurfaceVariant));
        header.addView(label, new LinearLayout.LayoutParams(0, -2, 1));
        MaterialButton copy = new MaterialButton(activity, null,
                androidx.appcompat.R.attr.borderlessButtonStyle);
        copy.setText(R.string.chat_render_copy_code);
        copy.setAllCaps(false);
        copy.setTextSize(12);
        copy.setMinWidth(0);
        copy.setMinimumWidth(0);
        copy.setMinHeight(dp(48));
        copy.setMinimumHeight(dp(48));
        copy.setInsetTop(0);
        copy.setInsetBottom(0);
        copy.setOnClickListener(view -> copyAction.copy(block.text));
        header.addView(copy, new LinearLayout.LayoutParams(-2, -2));
        box.addView(header, new LinearLayout.LayoutParams(-1, -2));

        HorizontalScrollView scroll = new HorizontalScrollView(activity);
        scroll.setFillViewport(true);
        TextView code = textView(parent, 13);
        code.setTypeface(Typeface.MONOSPACE);
        code.setHorizontallyScrolling(true);
        code.setText(block.text);
        code.setPadding(0, dp(3), dp(8), dp(6));
        scroll.addView(code, new HorizontalScrollView.LayoutParams(-2, -2));
        box.addView(scroll, new LinearLayout.LayoutParams(-1, -2));
        parent.addView(box, boxParameters);
    }

    private TextView textView(View themeAnchor, int size) {
        TextView text = new TextView(activity);
        text.setTextSize(size);
        text.setTextColor(color(themeAnchor, com.google.android.material.R.attr.colorOnSurface));
        text.setLineSpacing(dp(3), 1.05f);
        text.setTextIsSelectable(true);
        text.setAutoLinkMask(0);
        text.setLinksClickable(false);
        return text;
    }

    private GradientDrawable background(View anchor, int attribute) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color(anchor, attribute));
        drawable.setCornerRadius(dp(10));
        return drawable;
    }

    private int color(View view, int attribute) { return MaterialColors.getColor(view, attribute); }
    private int dp(int value) { return Math.round(value * activity.getResources().getDisplayMetrics().density); }
}
