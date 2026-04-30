package com.example.movietracker.ui.profile;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class VerticalBarChartView extends View {
    private static final float PADDING = 28f;
    private static final float LABEL_TEXT_SIZE = 22f;

    private final Paint axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Map<String, Integer> data = new LinkedHashMap<>();
    private int maxValue = 0;

    public VerticalBarChartView(Context context) {
        super(context);
        init();
    }

    public VerticalBarChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VerticalBarChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        axisPaint.setColor(Color.parseColor("#BDBDBD"));
        axisPaint.setStrokeWidth(2f);

        barPaint.setColor(Color.parseColor("#66BB6A"));
        barPaint.setStyle(Paint.Style.FILL);

        valuePaint.setColor(Color.parseColor("#212121"));
        valuePaint.setTextAlign(Paint.Align.CENTER);
        valuePaint.setTextSize(LABEL_TEXT_SIZE);

        labelPaint.setColor(Color.parseColor("#616161"));
        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setTextSize(LABEL_TEXT_SIZE);
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
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (data.isEmpty()) {
            return;
        }

        float left = PADDING;
        float right = getWidth() - PADDING;
        float top = PADDING;
        float bottom = getHeight() - (PADDING * 1.8f);

        canvas.drawLine(left, bottom, right, bottom, axisPaint);
        canvas.drawLine(left, top, left, bottom, axisPaint);

        List<Map.Entry<String, Integer>> entries = new ArrayList<>(data.entrySet());
        int count = entries.size();
        float totalWidth = right - left;
        float slotWidth = count == 0 ? 0f : totalWidth / count;
        float barWidth = slotWidth * 0.55f;

        for (int i = 0; i < count; i++) {
            Map.Entry<String, Integer> entry = entries.get(i);
            float centerX = left + slotWidth * i + slotWidth / 2f;
            float barLeft = centerX - barWidth / 2f;
            float barRight = centerX + barWidth / 2f;

            float ratio = maxValue <= 0 ? 0f : (float) entry.getValue() / (float) maxValue;
            float barTop = bottom - ((bottom - top) * ratio);

            canvas.drawRoundRect(barLeft, barTop, barRight, bottom, 8f, 8f, barPaint);
            canvas.drawText(String.valueOf(entry.getValue()), centerX, barTop - 10f, valuePaint);
            canvas.drawText(entry.getKey(), centerX, bottom + 26f, labelPaint);
        }
    }
}
