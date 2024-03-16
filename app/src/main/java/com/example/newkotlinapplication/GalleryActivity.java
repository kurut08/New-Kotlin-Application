package com.example.newkotlinapplication;

import android.os.Bundle;
import android.os.Environment;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.File;
import java.util.ArrayList;
public class GalleryActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photos);

        RecyclerView photosRecyclerView = findViewById(R.id.photosRecyclerView);
        photosRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        ArrayList<File> photoFiles = loadImagesFromStorage();
        PhotosAdapter photosAdapter = new PhotosAdapter(this, photoFiles);
        photosRecyclerView.setAdapter(photosAdapter);
    }
    private ArrayList<File> loadImagesFromStorage() {
        ArrayList<File> images = new ArrayList<>();
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        File[] files = storageDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && (file.getName().endsWith(".jpg") || file.getName().endsWith(".jpeg"))) {
                    images.add(file);
                }
            }
        }

        return images;
    }
}
