package com.example.movietracker.ui.profile;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.Map;

public class PieChartView extends View {
    private Map<String, Integer> data;
    private Paint paint;
    private RectF rectF;
    private int total = 0;
    
    // Colors for different types
    private int[] colors = {Color.parseColor("#4CAF50"), Color.parseColor("#2196F3"), Color.parseColor("#FFC107"), Color.parseColor("#FF5722")};

    public PieChartView(Context context) {
        super(context);
        init();
    }

    public PieChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PieChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        rectF = new RectF();
    }

    public void setData(Map<String, Integer> data) {
        this.data = data;
        total = 0;
        if (data != null) {
            for (int count : data.values()) {
                total += count;
            }
        }
        invalidate(); // Redraw
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int minSize = Math.min(w, h);
        int padding = 20;
        rectF.set(padding, padding, minSize - padding, minSize - padding);
        // Center it
        float dx = (w - minSize) / 2f;
        float dy = (h - minSize) / 2f;
        rectF.offset(dx, dy);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (total == 0 || data == null || data.isEmpty()) {
            paint.setColor(Color.LTGRAY);
            canvas.drawOval(rectF, paint);
            return;
        }

        float startAngle = -90f; // Start at top
        int colorIndex = 0;

        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            int count = entry.getValue();
            if (count == 0) continue;

            float sweepAngle = 360f * count / total;
            paint.setColor(colors[colorIndex % colors.length]);
            canvas.drawArc(rectF, startAngle, sweepAngle, true, paint);
            startAngle += sweepAngle;
            colorIndex++;
        }
    }
}
