package de.kai_morich.simple_usb_terminal;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.google.android.material.color.MaterialColors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** Small bounded RTT chart; missing replies are gaps/crosses, never zero latency. */
public final class ToolboxTrendView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private List<Double> samples = Collections.emptyList();
    public ToolboxTrendView(Context context, AttributeSet attributes) { super(context, attributes); }
    public void setSamples(List<Double> values) {
        samples = new ArrayList<>(values.subList(0, Math.min(20, values.size())));
        setContentDescription(getContext().getString(R.string.tb_trend));
        invalidate();
    }
    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (samples.isEmpty()) return;
        float density = getResources().getDisplayMetrics().density;
        float left = 44 * density, top = 20 * density;
        float right = getWidth() - 12 * density, bottom = getHeight() - 22 * density;
        if (right <= left || bottom <= top) return;
        double maximum = 1;
        for (Double sample : samples) if (valid(sample)) maximum = Math.max(maximum, sample);
        paint.setTextSize(11 * getResources().getDisplayMetrics().scaledDensity);
        paint.setStrokeWidth(density);
        paint.setColor(MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurface));
        canvas.drawText(String.format(Locale.ROOT, "%.1f", maximum), 0, top, paint);
        canvas.drawText("0", 0, bottom, paint);
        canvas.drawLine(left, bottom, right, bottom, paint);
        float previousX = 0, previousY = 0;
        boolean previousValid = false;
        for (int i = 0; i < samples.size(); i++) {
            float x = samples.size() == 1 ? (left + right) / 2 : left + i * (right - left) / (samples.size() - 1);
            Double sample = samples.get(i);
            paint.setColor(MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurface));
            canvas.drawText(String.valueOf(i + 1), x - 3 * density, bottom + 16 * density, paint);
            paint.setStrokeWidth(2 * density);
            if (!valid(sample)) {
                paint.setColor(MaterialColors.getColor(this, androidx.appcompat.R.attr.colorError));
                float y = top + (bottom - top) / 2;
                canvas.drawLine(x - 4 * density, y - 4 * density, x + 4 * density, y + 4 * density, paint);
                canvas.drawLine(x - 4 * density, y + 4 * density, x + 4 * density, y - 4 * density, paint);
                previousValid = false;
                continue;
            }
            float y = bottom - (float) (sample / maximum) * (bottom - top);
            paint.setColor(MaterialColors.getColor(this, androidx.appcompat.R.attr.colorPrimary));
            if (previousValid) canvas.drawLine(previousX, previousY, x, y, paint);
            canvas.drawCircle(x, y, 3 * density, paint);
            previousX = x; previousY = y; previousValid = true;
        }
    }
    private boolean valid(Double value) { return value != null && !value.isNaN() && !value.isInfinite() && value >= 0; }
}
