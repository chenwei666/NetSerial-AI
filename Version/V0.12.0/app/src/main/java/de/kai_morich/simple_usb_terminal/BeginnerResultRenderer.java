package de.kai_morich.simple_usb_terminal;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.chenwei666.netserial.guided.DiagnosticFinding;
import com.chenwei666.netserial.guided.GuidedReportFormatter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import java.util.List;

/** Rendering only: no probes, device commands, or cloud calls. */
final class BeginnerResultRenderer {
    private final Context context;
    private final boolean english;
    BeginnerResultRenderer(Context context, boolean english) {
        this.context = context;
        this.english = english;
    }

    void render(LinearLayout container, List<DiagnosticFinding> findings) {
        container.removeAllViews();
        GuidedReportFormatter formatter = new GuidedReportFormatter();
        for (DiagnosticFinding finding : findings) {
            MaterialCardView card = new MaterialCardView(context);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(-1, -2);
            cardParams.topMargin = dp(12);
            card.setLayoutParams(cardParams);
            card.setRadius(dp(16));
            LinearLayout body = new LinearLayout(context);
            body.setOrientation(LinearLayout.VERTICAL);
            body.setPadding(dp(16), dp(16), dp(16), dp(12));
            TextView title = text(GuidedReportFormatter.status(finding.getStatus(), english)
                    + " · " + finding.getTitle().get(english));
            title.setTypeface(title.getTypeface(), Typeface.BOLD);
            title.setTextSize(17);
            body.addView(title);
            body.addView(text(finding.getExplanation().get(english)));
            TextView next = text(context.getString(R.string.guide_next, finding.getNextStep().get(english)));
            next.setTypeface(next.getTypeface(), Typeface.BOLD);
            body.addView(next);
            if (!finding.getEvidence().isEmpty()) {
                MaterialButton toggle = new MaterialButton(context, null,
                        androidx.appcompat.R.attr.borderlessButtonStyle);
                toggle.setText(R.string.guide_evidence_toggle);
                TextView raw = text(formatter.sanitize(finding.getEvidence()));
                raw.setTypeface(Typeface.MONOSPACE);
                raw.setTextIsSelectable(true);
                raw.setVisibility(View.GONE);
                toggle.setOnClickListener(view -> raw.setVisibility(
                        raw.getVisibility() == View.GONE ? View.VISIBLE : View.GONE));
                body.addView(toggle);
                body.addView(raw);
            }
            card.addView(body);
            container.addView(card);
        }
    }

    TextView text(String value) {
        TextView text = new TextView(context);
        text.setText(value);
        text.setTextSize(15);
        text.setPadding(0, dp(6), 0, dp(6));
        text.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        return text;
    }

    private int dp(int value) { return Math.round(value * context.getResources().getDisplayMetrics().density); }
}
