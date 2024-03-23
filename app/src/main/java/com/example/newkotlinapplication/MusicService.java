package com.example.newkotlinapplication;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.util.List;
import java.util.Random;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private List<Song> songs;
    private int songPosition = 0;
    private IBinder musicBinder = new MusicBinder(this);
    private Random random;
    private String songTitle = "";
    private int notifyId = 1;
    private boolean shuffle = false;

    @Override
    public void onCreate() {
        super.onCreate();
        songPosition = 0;
        mediaPlayer = new MediaPlayer();
        initMusicPlayer();
        random = new Random();
    }

    private void initMusicPlayer() {
        mediaPlayer.setWakeMode(
                getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build();
        mediaPlayer.setAudioAttributes(audioAttributes);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
    }

    public void setList(List<Song> theSongs) {
        songs = theSongs;
    }
    public void playSong() throws IOException {
        mediaPlayer.reset();
        Song playSong = songs.get(songPosition);
        songTitle = playSong.getName();
        long currentSongId = playSong.getId();
        Uri trackUri = ContentUris.withAppendedId(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currentSongId
        );
        try {
            mediaPlayer.setDataSource(getApplicationContext(), trackUri);
        } catch (Exception e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        mediaPlayer.prepare();
    }
    
    public void setSong(int songIndex) {
        songPosition = songIndex;
    }
    public class MusicBinder extends Binder {
        private MusicService service;

        public MusicBinder(MusicService service) {
            this.service = service;
        }

        public MusicService getService() {
            return service;
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        return musicBinder;
    }
    @Override
    public boolean onUnbind(Intent intent) {
        mediaPlayer.stop();
        mediaPlayer.release();
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp)  {
        if (mediaPlayer.getCurrentPosition() > 0) {
            mp.reset();
            try {
                playNext();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra){
        mp.reset();
        return false;
    }
    public int getPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public void pausePlayer() {
        mediaPlayer.pause();
    }

    public void seek(int position) {
        mediaPlayer.seekTo(position);
    }

    public void go() {
        mediaPlayer.start();
    }
    public void playPrev() throws IOException {
        songPosition--;
        if (songPosition < 0) {
            songPosition = songs.size() - 1;
        }
        playSong();
    }

    public void playNext() throws IOException {
        if (shuffle) {
            int newSong = songPosition;
            while (newSong == songPosition) {
                newSong = random.nextInt(songs.size());
            }
            songPosition = newSong;
        } else {
            songPosition++;
            if (songPosition >= songs.size()) songPosition = 0;
        }
        playSong();
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
        String channelId = "my_channel_id";

        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class),
                        PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_launcher_foreground)
                .setOngoing(true)
                .setContentTitle("Playing")
                .setContentText(songTitle)
                .setContentIntent(pendingIntent)
                .setTicker(songTitle)
                .build();

        createNotificationChannel(channelId, "My Music Player");
        startForeground(notifyId, notification);
    }
    public void createNotificationChannel(String channelId, String channelName) {
        NotificationChannel notificationChannel;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(notificationChannel);
        }
    }
    @Override
    public void onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            stopForeground(STOP_FOREGROUND_REMOVE);
        } else {
            stopForeground(true);
        }
    }
    public void setShuffle(){
        shuffle = !shuffle;
    }

}

