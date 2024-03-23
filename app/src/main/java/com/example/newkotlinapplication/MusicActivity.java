package com.example.newkotlinapplication;



import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.example.newkotlinapplication.MusicService.MusicBinder;

public class MusicActivity extends AppCompatActivity implements MediaController.MediaPlayerControl {
    private RecyclerView recyclerView;
    private List<Song> songs = new ArrayList<>();
    private MusicService musicService = new MusicService();
    private Intent playIntent = null;
    private boolean musicBound = false;
    private MusicController controller;
    private boolean paused = false;
    private boolean playbackPaused = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);
        displaySongs();
    }

    private void getSongList() {
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.DATA + " LIKE ? AND " + MediaStore.Audio.Media.DURATION + " >= ?";
        String[] selectionArgs = new String[]{"%/Music/%", "15000"};
        Cursor musicCursor = musicResolver.query(musicUri, null, selection, selectionArgs, null);
        if ((musicCursor != null) && musicCursor.moveToFirst()) {
            int titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int durationColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
            int artistColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int albumIdColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisDuration = musicCursor.getString(durationColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String thisAlbumId = musicCursor.getString(albumIdColumn);
                Uri albumArtUri = Uri.parse("content://media/external/audio/albumart");
                Uri albumArtContentUri = ContentUris.withAppendedId(albumArtUri, Long.parseLong(thisAlbumId));
                songs.add(new Song(thisId, thisTitle, thisDuration, thisArtist, albumArtContentUri));
            } while (musicCursor.moveToNext());
            musicCursor.close();
        } else {
            Log.d("MyTag", "The song list is empty");
        }
    }
    private void displaySongs() {
        setController();
        recyclerView = findViewById(R.id.song_list);
        recyclerView.setHasFixedSize(true);
        getSongList();
        Collections.sort(songs, new Comparator<Song>() {
            @Override
            public int compare(Song a, Song b) {
                return a.getName().compareTo(b.getName());
            }
        });
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        RVAdapter adapter = new RVAdapter(songs);
        recyclerView.setAdapter(adapter);
    }

    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicBinder binder = (MusicBinder) service;
            musicService = binder.getService();
            musicService.setList(songs);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };


    @Override
    public void onStart() {
        super.onStart();
        if (playIntent == null) {
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }
    public void songPicked(View view) throws IOException {
        musicService.setSong(Integer.parseInt(view.getTag().toString()));
        musicService.playSong();
        if (playbackPaused) {
            setController();
            playbackPaused = false;
        }
        controller.show(0);
    }
    @Override
    public void onDestroy() {
        stopService(playIntent);
        musicService = null;
        super.onDestroy();
    }
    private void setController() {
        controller = new MusicController(this);
        controller.setPrevNextListeners(
                v -> {
                    try {
                        playNext();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                },
                v -> {
                    try {
                        playPrev();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
        controller.setMediaPlayer(this);
        controller.setAnchorView(findViewById(R.id.song_list));
        controller.setEnabled(true);
    }

    private void playNext() throws IOException {
        musicService.playNext();
        if (playbackPaused) {
            setController();
            playbackPaused = false;
        }
        controller.show(0);
    }

    private void playPrev() throws IOException {
        musicService.playPrev();
        if (playbackPaused) {
            setController();
            playbackPaused = false;
        }
        controller.show(0);
    }
    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 1;
    }

    @Override
    public int getBufferPercentage() {
        int duration = musicService.getDuration();
        if (duration > 0) {
            return (musicService.getPosition() * 100) / duration;
        }
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        if (musicService != null && musicBound && musicService.isPlaying()) {
            return musicService.getPosition();
        } else {
            return 0;
        }
    }
    @Override
    public void start() {
        musicService.go();
    }

    @Override
    public void pause() {
        playbackPaused = true;
        musicService.pausePlayer();
    }

    @Override
    public void seekTo(int p0) {
        musicService.seek(p0);
    }

    @Override
    public boolean isPlaying() {
        if (musicService != null && musicBound) {
            return musicService.isPlaying();
        }
        return false;
    }

    @Override
    public int getDuration() {
        if (musicService != null && musicBound && musicService.isPlaying()) {
            return musicService.getDuration();
        } else {
            return 0;
        }
    }
    public void shuffleSongs(View view) {
        musicService.setShuffle();
    }

    public void stopSong(View view) {
        stopService(playIntent);
        musicService = null;
        System.exit(0);
    }
    @Override
    protected void onPause() {
        super.onPause();
        paused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (paused) {
            setController();
            paused = false;
        }
    }

    @Override
    protected void onStop() {
        controller.hide();
        super.onStop();
    }

//https://code.tutsplus.com/create-a-music-player-on-android-user-controls--mobile-22787t
    //SHUFFLE
}
