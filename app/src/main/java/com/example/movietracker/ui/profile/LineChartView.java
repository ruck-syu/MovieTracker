package com.example.movietracker.ui.profile;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LineChartView extends View {
    private static final float PADDING = 28f;
    private static final float LABEL_TEXT_SIZE = 22f;
    private static final float POINT_RADIUS = 5f;

    private final Paint axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Map<String, Integer> data = new LinkedHashMap<>();
    private int maxValue = 0;

    public LineChartView(Context context) {
        super(context);
        init();
    }

    public LineChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LineChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        axisPaint.setColor(Color.parseColor("#BDBDBD"));
        axisPaint.setStrokeWidth(2f);

        linePaint.setColor(Color.parseColor("#42A5F5"));
        linePaint.setStrokeWidth(4f);
        linePaint.setStyle(Paint.Style.STROKE);

        pointPaint.setColor(Color.parseColor("#1E88E5"));
        pointPaint.setStyle(Paint.Style.FILL);

        labelPaint.setColor(Color.parseColor("#616161"));
        labelPaint.setTextSize(LABEL_TEXT_SIZE);
        labelPaint.setTextAlign(Paint.Align.CENTER);

        valuePaint.setColor(Color.parseColor("#212121"));
        valuePaint.setTextSize(LABEL_TEXT_SIZE);
        valuePaint.setTextAlign(Paint.Align.CENTER);
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

        float left = PADDING * 1.4f;
        float right = getWidth() - PADDING;
        float top = PADDING;
        float bottom = getHeight() - (PADDING * 1.8f);

        canvas.drawLine(left, bottom, right, bottom, axisPaint);
        canvas.drawLine(left, top, left, bottom, axisPaint);

        List<Map.Entry<String, Integer>> entries = new ArrayList<>(data.entrySet());
        int count = entries.size();
        float step = count <= 1 ? 0f : (right - left) / (count - 1);
        Path path = new Path();

        for (int i = 0; i < count; i++) {
            Map.Entry<String, Integer> entry = entries.get(i);
            float x = left + (step * i);
            float valueRatio = maxValue <= 0 ? 0f : (float) entry.getValue() / (float) maxValue;
            float y = bottom - ((bottom - top) * valueRatio);

            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }

            canvas.drawCircle(x, y, POINT_RADIUS, pointPaint);
            canvas.drawText(String.valueOf(entry.getValue()), x, y - 10f, valuePaint);
            canvas.drawText(entry.getKey(), x, bottom + 26f, labelPaint);
        }

        if (count > 1) {
            canvas.drawPath(path, linePaint);
        }
    }
}
