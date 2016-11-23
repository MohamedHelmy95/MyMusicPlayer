package com.example.eng_mohamed.mymusicplayer;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import static android.content.Context.NOTIFICATION_SERVICE;
import static android.support.v4.app.ServiceCompat.START_STICKY;

/**
 * Created by Eng_Mohamed on 11/9/2016.
 */

public class MusicService extends Service implements MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener ,AudioManager.OnAudioFocusChangeListener{
    private MediaPlayer player;
    private List<SongModel> songs;
    private int songPos;
    private String songTitle;
    private static final int NOTIFY_ID=1;
    private final IBinder musicBind = new MusicBinder();
    private boolean shuffle,paused;
    private Random rand;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        player.stop();
        player.release();
        return false;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        songPos=0;
        player=new MediaPlayer();
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
                updateSongPosition(songPos);
                Intent notIntent = new Intent(getApplicationContext(), MainActivity.class);
                notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                PendingIntent pendInt = PendingIntent.getActivity(getApplication(), 0,
                        notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                Notification.Builder builder = new Notification.Builder(getApplicationContext());

                builder.setContentIntent(pendInt)
                        .setSmallIcon(R.drawable.ic_audiotrack)
                        .setTicker(songTitle)
                        .setOngoing(true)
                        .setContentTitle("SimpleMusicPlayer")
                        .setContentText(songTitle);
                Notification not = builder.build();

                startForeground(NOTIFY_ID, not);
            }
        });
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
        rand=new Random();
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);

        if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            // could not get audio focus.
            pausePlayer();
            Toast.makeText(getApplicationContext(),"Cannot Play Song Stop any other Media Players",Toast.LENGTH_LONG).show();

        }

    }

    private void updateSongPosition(int songPos) {
        Intent in=new Intent(getString(R.string.whatBCRLooksFor));
        in.putExtra(getString(R.string.intentExtra),songPos);
        LocalBroadcastManager.getInstance(this).sendBroadcast(in);
    }

    public void playTrack(){
        player.reset();
        SongModel song=songs.get(songPos);
        songTitle=song.getTitle();
        long playing=song.getID();
        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                playing);
        try{
            player.setDataSource(getApplicationContext(), trackUri);
        }
        catch(Exception e){
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }

        try {
            player.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(player.getCurrentPosition()>0){
            mp.reset();
            playNext();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }



    @Override
    public void onDestroy() {
        stopForeground(true);
        super.onDestroy();
    }

    public void setSongs(List<SongModel> songs) {
        this.songs = songs;
    }

    public void setSongPosition(int songPos) {
        this.songPos = songPos;
    }
    public int getSongPosition() {
        return songPos;
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        if (focusChange != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            // could not get audio focus.
            if(player.isPlaying())
                pausePlayer();
            Toast.makeText(getApplicationContext(),"Cannot Play Song Stop any other Media Players",Toast.LENGTH_LONG).show();
        }
        else{
            if(paused){resumePlayer();}

        }
    }


    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    public boolean isPng(){
        return player.isPlaying();
    }

    public void pausePlayer(){
        player.pause();
        paused=true;
    }

    public void resumePlayer(){
        player.start();
        paused=false;
    }
    public void playPrev(){
        songPos--;
        if(songPos<0) songPos=songs.size()-1;
        playTrack();
    }
    public void playNext() {
        if (shuffle) {
            int newSong = songPos;
            while (newSong == songPos) {
                newSong = rand.nextInt(songs.size());
            }
            songPos = newSong;
        } else {
            songPos++;
            if (songPos >= songs.size()) songPos = 0;
        }

        playTrack();
    }
    public int getSongSeekPos(){
        return player.getCurrentPosition();
    }
    public void seekTo(int song,int seek){
        setSongPosition(song);
        playTrack();
        player.seekTo(seek);
    }
    public void setShuffle() {
        if(shuffle) shuffle=false;
        else shuffle=true;
    }

    public boolean isShuffle() {
        return shuffle;
    }


}
