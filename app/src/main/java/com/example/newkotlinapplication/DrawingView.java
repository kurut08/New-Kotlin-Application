package com.example.newkotlinapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

public class DrawingView extends View {
    public class Stroke {
        public int color;
        public int strokeWidth;
        public Path path;
        public Stroke(int color, int strokeWidth, Path path) {
            this.color = color;
            this.strokeWidth = strokeWidth;
            this.path = path;
        }
    }
    private static final float TOUCH_TOLERANCE = 4;
    private float mX, mY;
    private Path path;

    private Paint paint;

    private ArrayList<Stroke> paths = new ArrayList<>();
    private int currentColor;
    private int strokeWidth;
    private Bitmap bitmap;
    private Canvas canvas;
    private Paint bitmapPaint = new Paint(Paint.DITHER_FLAG);

    public DrawingView(Context context) {
        this(context, null);
    }

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setAlpha(0xff);

    }
    public void initDrawingView(int height, int width) {
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        currentColor = Color.GREEN;
        strokeWidth = 20;
    }
    public void setColor(int color) {
        currentColor = color;
    }
    public void setStrokeWidth(int width) {
        strokeWidth = width;
    }
    public void undoDrawing() {
        if (paths.size() != 0) {
            paths.remove(paths.size() - 1);
            invalidate();
        }
    }
    public Bitmap saveBitmap() {
        return bitmap;
    }
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        int backgroundColor = Color.WHITE;
        this.canvas.drawColor(backgroundColor);
        for (Stroke fp : paths) {
            paint.setColor(fp.color);
            paint.setStrokeWidth(fp.strokeWidth);
            this.canvas.drawPath(fp.path, paint);
        }
        canvas.drawBitmap(bitmap, 0, 0, bitmapPaint);
        canvas.restore();
    }
    private void touchStart(float x, float y) {
        path = new Path();
        Stroke fp = new Stroke(currentColor, strokeWidth, path);
        paths.add(fp);
        path.reset();
        path.moveTo(x, y);
        mX = x;
        mY = y;
    }
    private void touchMove(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);

        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            path.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }
    private void touchUp() {
        path.lineTo(mX, mY);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStart(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touchUp();
                invalidate();
                break;
        }
        return true;
    }
}
