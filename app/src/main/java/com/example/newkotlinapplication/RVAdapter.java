package com.example.newkotlinapplication;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;


public class RVAdapter extends RecyclerView.Adapter<RVAdapter.SongViewHolder> {
    private List<Song> songs;

    public RVAdapter(List<Song> songs) {
        this.songs = songs;
    }

    public static class SongViewHolder extends RecyclerView.ViewHolder {
        TextView songName;
        TextView songLength;
        TextView songArtist;
        ImageView songCover;

        public SongViewHolder(View itemView) {
            super(itemView);
            songName = itemView.findViewById(R.id.song_name);
            songLength = itemView.findViewById(R.id.song_length);
            songArtist = itemView.findViewById(R.id.song_artist);
            songCover = itemView.findViewById(R.id.song_art);
        }
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    @Override
    public SongViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.song, viewGroup, false);
        return new SongViewHolder(v);
    }

    @Override
    public void onBindViewHolder(SongViewHolder songViewHolder, int idx) {
        String duration_minutes_seconds = String.format("%02d:%02d", Integer.parseInt(songs.get(idx).getDuration()) / (60 * 1000), (Integer.parseInt(songs.get(idx).getDuration()) / 1000) % 60);
        songViewHolder.songName.setText(songs.get(idx).getName());
        songViewHolder.songLength.setText(duration_minutes_seconds);
        songViewHolder.songArtist.setText(songs.get(idx).getArtist());
        songViewHolder.songCover.setImageURI(songs.get(idx).getCover());
        songViewHolder.itemView.setTag(idx);
    }
}


