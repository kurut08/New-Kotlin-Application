package com.example.newkotlinapplication;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;

public class PhotosAdapter extends RecyclerView.Adapter<PhotosAdapter.ViewHolder> {

    private Context context;
    private ArrayList<File> photos;

    public PhotosAdapter(Context context, ArrayList<File> photos) {
        this.context = context;
        this.photos = photos;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        File photoFile = photos.get(position);
        Glide.with(context).load(photoFile).into(holder.imageView);

        holder.imageView.setOnClickListener(v -> {
            showFullImage(context, photoFile);
        });
        holder.imageView.setOnLongClickListener(v -> {
            showDeleteConfirmationDialog(photoFile, position);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
        }
    }
    private void showFullImage(Context context, File imageFile) {
        final Dialog dialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.dialog_full_image);

        ImageView fullImageView = dialog.findViewById(R.id.full_image_view);
        Glide.with(context).load(imageFile).into(fullImageView);

        fullImageView.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
    private void showDeleteConfirmationDialog(File photoFile, int position) {
        new AlertDialog.Builder(context)
                .setMessage("Czy chcesz usunąć to zdjęcie?")
                .setPositiveButton("Usuń", (dialog, which) -> deletePhoto(photoFile, position))
                .setNegativeButton("Anuluj", null)
                .show();
    }

    private void deletePhoto(File photoFile, int position) {
        if (photoFile.delete()) {
            photos.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, photos.size());
            Toast.makeText(context, "Zdjęcie usunięte", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Błąd podczas usuwania zdjęcia", Toast.LENGTH_SHORT).show();
        }
    }
}
