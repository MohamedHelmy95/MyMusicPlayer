package com.example.eng_mohamed.mymusicplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

public class SongActivity extends AppCompatActivity implements Button.OnClickListener
,SeekBar.OnSeekBarChangeListener{
    TextView mTitle;
    TextView mArtist;
    TextView mCurrTime;
    TextView mDuration;
    ImageButton mRewind;
    ImageButton mPrev;
    ImageButton mNext;
    ImageButton mFastForward;
    ToggleButton mPlayPause;
    SeekBar mSeekBar;
    SongModel mSong;
    MusicService mService;

    @Override
    protected void onPause() {
        Log.e("Song Act","Pause");
        SharedPreferences preferences=getSharedPreferences("lastPlayed",MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("id", mService.getSongPosition());
        editor.putInt("seekTo",mService.getSongSeekPos());
        editor.putBoolean("firstRun", false);
        editor.apply();

//        LocalBroadcastManager.getInstance(this).unregisterReceiver(handleTimeChange);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.e("Song Act","Destroy");
        super.onDestroy();
    }

    @Override
    protected void onResume() {
//        LocalBroadcastManager.getInstance(this).registerReceiver(handleTimeChange,
//                new IntentFilter(getString(R.string.syncBarService)));
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mSong=getIntent().getParcelableExtra("Song");
        mService=MainActivity.getService();
        int seek=getIntent().getIntExtra("seekPos",0);
        mService.seekTo(seek);
        mPlayPause=(ToggleButton) findViewById(R.id.det_playPause);
        mPlayPause.setOnClickListener(this);
        mNext=(ImageButton)findViewById(R.id.det_nextTrack);
        mNext.setOnClickListener(this);
        mRewind=(ImageButton)findViewById(R.id.det_Rewind);
        mRewind.setOnClickListener(this);
        mFastForward=(ImageButton)findViewById(R.id.det_FastForward);
        mFastForward.setOnClickListener(this);
        mPrev=(ImageButton)findViewById(R.id.det_preTrack);
        mPrev.setOnClickListener(this);
        mTitle=(TextView)findViewById(R.id.det_title);
        mArtist=(TextView)findViewById(R.id.det_Artist);
        mCurrTime=(TextView)findViewById(R.id.currTime);
        mDuration=(TextView)findViewById(R.id.endTime);
        mSeekBar=(SeekBar)findViewById(R.id.seekBar);
        mSeekBar.setOnSeekBarChangeListener(this);
        mSeekBar.setMax(mService.getDuration());
        mSeekBar.setProgress(seek);
        updateProgressBar();
        mTitle.setText(mSong.getTitle());
        mArtist.setText(mSong.getArtist());
        mDuration.setText(getDurationFormated(mService.getDuration()));
        if(mService.isPng())
            mPlayPause.setChecked(true);
    }
    private Handler mHandler=new Handler();
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            mCurrTime.setText(getDurationFormated(mService.getSongSeekPos()));
            mSeekBar.setProgress(mService.getSongSeekPos());
            mHandler.postDelayed(this, 100);
        }
    };
    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }
    @Override
    public void onClick(View v) {
        if(v==mNext){
            mService.playNext();
            mSong=mService.getSong();
            mTitle.setText(mSong.getTitle());
            mArtist.setText(mSong.getArtist());
            mDuration.setText(getDurationFormated(mService.getDuration()));

        }
        else if(v==mPrev){
            mService.playPrev();
            mSong=mService.getSong();
            mTitle.setText(mSong.getTitle());
            mArtist.setText(mSong.getArtist());
            mDuration.setText(getDurationFormated(mService.getDuration()));

        }
        else if(v==mPlayPause){
            if (mPlayPause.isChecked()) { // Checked - Pause icon visible
                mService.resumePlayer();
                updateProgressBar();
            } else { // Unchecked - Play icon visible
                mService.pausePlayer();
            }
        }
        else if(v==mFastForward){
            fastForward();

        }
        else if(v==mRewind){
            rewind();
        }

    }
    public void fastForward(){
           mService.fastForward();
           mCurrTime.setText(getDurationFormated(mService.getSongSeekPos()));

    }
    public void rewind(){
        mService.rewind();
        mCurrTime.setText(getDurationFormated(mService.getSongSeekPos()));
    }
    public String getDurationFormated(int Duration){
        int seconds = (int) (Duration / 1000) % 60 ;
        int minutes = (int) ((Duration / (1000*60)) % 60);
        String duration=minutes+"."+seconds;
    return duration;
    }
    int progress = 0;

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        this.progress=progress;

        mCurrTime.setText(getDurationFormated(mService.getSongSeekPos()));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        progress=seekBar.getProgress();
        mService.seekTo(progress);
        mCurrTime.setText(getDurationFormated(seekBar.getProgress()));
        mHandler.removeCallbacks(mUpdateTimeTask);
        updateProgressBar();

    }
//    BroadcastReceiver handleTimeChange=new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            int newPos=intent.getIntExtra(getString(R.string.syncBarServiceEx),0);
//            mCurrTime.setText(getDurationFormated(newPos));
//            mSeekBar.setProgress(newPos);
//        }
//    };



}
