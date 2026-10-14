package de.kai_morich.simple_usb_terminal;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chenwei666.netserial.navigation.FeatureDescriptor;

final class FeatureButtonFactory {
    private FeatureButtonFactory() { }

    static View create(Context context, FeatureDescriptor feature) {
        return create(context, feature, false);
    }

    static View create(Context context, FeatureDescriptor feature, boolean compact) {
        View card = LayoutInflater.from(context).inflate(R.layout.item_feature_card, null, false);
        TextView title = card.findViewById(R.id.feature_card_title);
        TextView summary = card.findViewById(R.id.feature_card_summary);
        ImageView icon = card.findViewById(R.id.feature_icon);
        title.setText(feature.getTitleResource());
        summary.setText(feature.getSummaryResource());
        summary.setVisibility(compact ? View.GONE : View.VISIBLE);
        icon.setImageResource(iconFor(feature));
        card.setContentDescription(context.getString(feature.getTitleResource()) + ". "
                + context.getString(feature.getSummaryResource()));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dp(context, 10);
        card.setLayoutParams(params);
        return card;
    }

    private static int iconFor(FeatureDescriptor feature) {
        switch (feature.getCategory()) {
            case CONNECTIONS: return R.drawable.ic_nav_connections;
            case TERMINAL: return R.drawable.ic_nav_terminal;
            case SETTINGS: return R.drawable.ic_nav_settings;
            case TOOLBOX:
            default: return R.drawable.ic_nav_toolbox;
        }
    }

    private static int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }
}
