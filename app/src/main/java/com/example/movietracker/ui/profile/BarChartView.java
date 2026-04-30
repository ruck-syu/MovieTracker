package com.example.movietracker.ui.profile;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

public class BarChartView extends View {
    private static final float LABEL_TEXT_SIZE = 28f;
    private static final float VALUE_TEXT_SIZE = 26f;
    private static final float ROW_HEIGHT = 52f;
    private static final float BAR_HEIGHT = 24f;
    private static final float HORIZONTAL_PADDING = 24f;
    private static final float LABEL_WIDTH = 120f;

    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint barTrackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Map<String, Integer> data = new LinkedHashMap<>();
    private int maxValue = 0;

    public BarChartView(Context context) {
        super(context);
        init();
    }

    public BarChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BarChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        labelPaint.setColor(Color.parseColor("#616161"));
        labelPaint.setTextSize(LABEL_TEXT_SIZE);

        valuePaint.setColor(Color.parseColor("#212121"));
        valuePaint.setTextSize(VALUE_TEXT_SIZE);
        valuePaint.setTextAlign(Paint.Align.RIGHT);

        barPaint.setColor(Color.parseColor("#4FC3F7"));
        barTrackPaint.setColor(Color.parseColor("#E0E0E0"));
    }

    public void setData(Map<String, Integer> newData) {
        data.clear();
        maxValue = 0;
        if (newData != null) {
            for (Map.Entry<String, Integer> entry : newData.entrySet()) {
                int value = Math.max(0, entry.getValue() == null ? 0 : entry.getValue());
                data.put(entry.getKey(), value);
                if (value > maxValue) {
                    maxValue = value;
                }
            }
        }
        requestLayout();
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int rowCount = Math.max(1, data.size());
        int desiredHeight = (int) (HORIZONTAL_PADDING * 2 + rowCount * ROW_HEIGHT);
        int resolvedHeight = resolveSize(desiredHeight, heightMeasureSpec);
        setMeasuredDimension(width, resolvedHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (data.isEmpty()) {
            return;
        }

        float y = HORIZONTAL_PADDING + ROW_HEIGHT / 2f;
        float chartLeft = HORIZONTAL_PADDING + LABEL_WIDTH;
        float chartRight = getWidth() - HORIZONTAL_PADDING;
        float chartWidth = Math.max(0f, chartRight - chartLeft);

        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            String label = entry.getKey();
            int value = entry.getValue();

            canvas.drawText(label, HORIZONTAL_PADDING, y + 10f, labelPaint);
            float top = y - BAR_HEIGHT / 2f;
            float bottom = y + BAR_HEIGHT / 2f;
            canvas.drawRoundRect(chartLeft, top, chartRight, bottom, 12f, 12f, barTrackPaint);

            float fraction = maxValue <= 0 ? 0f : (float) value / (float) maxValue;
            float barRight = chartLeft + (chartWidth * fraction);
            if (barRight > chartLeft) {
                canvas.drawRoundRect(chartLeft, top, barRight, bottom, 12f, 12f, barPaint);
            }

            canvas.drawText(String.valueOf(value), chartRight - 6f, y + 10f, valuePaint);
            y += ROW_HEIGHT;
        }
    }
}
