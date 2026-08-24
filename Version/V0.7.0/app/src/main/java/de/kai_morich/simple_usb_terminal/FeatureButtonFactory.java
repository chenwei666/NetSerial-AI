package de.kai_morich.simple_usb_terminal;

import android.content.Context;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.chenwei666.netserial.navigation.FeatureDescriptor;
import com.google.android.material.button.MaterialButton;

final class FeatureButtonFactory {
    private FeatureButtonFactory() { }

    static MaterialButton create(Context context, FeatureDescriptor feature) {
        MaterialButton button = new MaterialButton(context, null,
                com.google.android.material.R.attr.materialButtonOutlinedStyle);
        button.setAllCaps(false);
        button.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        button.setTextAlignment(MaterialButton.TEXT_ALIGNMENT_VIEW_START);
        button.setText(context.getString(feature.getTitleResource()) + "\n"
                + context.getString(feature.getSummaryResource()));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dp(context, 8);
        button.setLayoutParams(params);
        button.setMinHeight(dp(context, 64));
        return button;
    }

    private static int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }
}
