package com.example.newkotlinapplication;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.slider.RangeSlider;

import java.io.OutputStream;

public class DrawingActivity extends AppCompatActivity {
    private DrawingView drawingView;
    private ImageButton saveBtn, colorBtn, strokeBtn, undoBtn;
    private RangeSlider rangeSlider;
    private int idColor = 1;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle bundle){
        super.onCreate(bundle);
        setContentView(R.layout.drawing_view);
        drawingView = (DrawingView) findViewById(R.id.drawing_view);
        rangeSlider = (RangeSlider) findViewById(R.id.rangebar);
        undoBtn = (ImageButton) findViewById(R.id.btn_undo);
        saveBtn = (ImageButton) findViewById(R.id.btn_save);
        colorBtn = (ImageButton) findViewById(R.id.btn_color);
        strokeBtn = (ImageButton) findViewById(R.id.btn_stroke);
        undoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawingView.undoDrawing();
            }
        });
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap bmp = drawingView.saveBitmap();
                OutputStream imageOutStream = null;
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, "drawing.png");
                contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
                Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                try {
                    imageOutStream = getContentResolver().openOutputStream(uri);
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, imageOutStream);
                    imageOutStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        colorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(idColor % 3 == 1){
                    drawingView.setColor(Color.MAGENTA);
                    idColor++;
                }
                if(idColor % 3 == 2){
                    drawingView.setColor(Color.CYAN);
                    idColor++;
                }
                if(idColor % 3 == 0){
                    drawingView.setColor(Color.YELLOW);
                    idColor = 1;
                }
            }
        });
        strokeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (rangeSlider.getVisibility() == View.VISIBLE)
                    rangeSlider.setVisibility(View.GONE);
                else
                    rangeSlider.setVisibility(View.VISIBLE);
            }
        });

        rangeSlider.setValueFrom(0.0f);
        rangeSlider.setValueTo(100.0f);
        rangeSlider.addOnChangeListener(new RangeSlider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull RangeSlider slider, float value, boolean fromUser) {
                drawingView.setStrokeWidth((int) value);
            }
        });
        ViewTreeObserver viewTreeObserver = drawingView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                drawingView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int width = drawingView.getMeasuredWidth();
                int height = drawingView.getMeasuredHeight();
                drawingView.initDrawingView(height, width);
            }
        });
    }
}

