package com.example.eng_mohamed.mymusicplayer;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ImageButton.OnClickListener {
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private RecyclerAdapter mAdapter;
    public  ArrayList<SongModel> mSongList;
    public  boolean  playbackPaused,runOnce;
    private static MusicService musicService;
    private Intent playIntent;
    ToggleButton mPlayPause;
    ImageButton mNext,mPrevious;
    TextView mTitle,mArtist;
    public static boolean musicBound ;
    SharedPreferences preferences;
    int lastPlayed;
    int mSongPos;
    int lastPlayedSeekPos;
    SongModel[] ds= {};
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("songs",mSongList);
        super.onSaveInstanceState(outState);

    }
    @Override
    protected void onStart() {
        super.onStart();
        lastPlayed = getSharedPreferences("lastPlayed", Context.MODE_PRIVATE).getInt("id",-1);
        lastPlayedSeekPos = getSharedPreferences("lastPlayed", Context.MODE_PRIVATE).getInt("seekTo",-1);
        runOnce = getSharedPreferences("lastPlayed", Context.MODE_PRIVATE).getBoolean("firstRun",true);
        Log.e("Songpos",lastPlayed+"");
        Log.e("SongSeekpos",lastPlayedSeekPos+"");
        FetchSongList fetchSongList= new FetchSongList();
        fetchSongList.execute();
    }
    @Override
    protected void onPause() {
        preferences=getSharedPreferences("lastPlayed",MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("firstRun", runOnce);
        editor.apply();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(handleMediaPlayerSwitch);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("main Act","Resume");
        runOnce = getSharedPreferences("lastPlayed", Context.MODE_PRIVATE).getBoolean("firstRun",true);
        LocalBroadcastManager.getInstance(this).registerReceiver(handleMediaPlayerSwitch,
                new IntentFilter(getString(R.string.whatBCRLooksFor)));

    }
    @Override
    protected void onDestroy() {
        preferences=getSharedPreferences("lastPlayed",MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("id", mSongPos);
        editor.putInt("seekTo",musicService.getSongSeekPos());
        editor.putBoolean("firstRun", false);
        editor.apply();
        stopService(playIntent);
        unbindService(musicConnection);
        musicService=null;
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
     if(v==mNext){
         musicService.playNext();
     }
        else if(v==mPrevious){
         musicService.playPrev();
     }
        if(v==mPlayPause){
            if (mPlayPause.isChecked()) { // Checked - Pause icon visible
                if(lastPlayedSeekPos!=-1&&!runOnce)
                {
                    musicService.seekTo(lastPlayed,lastPlayedSeekPos);
                    runOnce=true;
                }
				else musicService.resumePlayer();
                mTitle.setText(mSongList.get(musicService.getSongPosition()).getTitle());
                mArtist.setText(mSongList.get(musicService.getSongPosition()).getArtist());
            } else { // Unchecked - Play icon visible
                musicService.pausePlayer();
            }

        }
		 
    }



    class FetchSongList extends AsyncTask<Void, Void, List<SongModel>> {


        @Override
        protected List<SongModel> doInBackground(Void... params) {
            ContentResolver musicResolver = getContentResolver();
            Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
            List<SongModel> modelList=new ArrayList<>();
            if(musicCursor!=null && musicCursor.moveToFirst()){
                //get columns
                int titleColumn = musicCursor.getColumnIndex
                        (android.provider.MediaStore.Audio.Media.TITLE);
                int idColumn = musicCursor.getColumnIndex
                        (android.provider.MediaStore.Audio.Media._ID);
                int artistColumn = musicCursor.getColumnIndex
                        (android.provider.MediaStore.Audio.Media.ARTIST);

                do {
                    long Id = musicCursor.getLong(idColumn);
                    String Title = musicCursor.getString(titleColumn);
                    String Artist = musicCursor.getString(artistColumn);
                    modelList.add(new SongModel(Id, Title, Artist));
                }
                while (musicCursor.moveToNext());
            }
            modelList=sortByTitle(modelList);
            return modelList;
        }

        @Override
        protected void onPostExecute(List<SongModel> modelList) {
            // super.onPostExecute(modelList);
            if (mSongList.size() == 0) {
                mSongList.addAll(modelList);
                mAdapter.notifyDataSetChanged();
            }
            if(playIntent==null){
                playIntent = new Intent(getApplicationContext(), MusicService.class);
                bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
                startService(playIntent);
            }
			if(lastPlayed!=-1&&!runOnce){
                mTitle.setText(mSongList.get(lastPlayed).getTitle());
                mArtist.setText(mSongList.get(lastPlayed).getArtist());
            }


        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        if(savedInstanceState==null||!savedInstanceState.containsKey("songs")) {
            mSongList = new ArrayList<SongModel>(Arrays.asList(ds));
        }
        else{mSongList=savedInstanceState.getParcelableArrayList("details");}
        mAdapter = new RecyclerAdapter(mSongList,this);
        mRecyclerView.setAdapter(mAdapter);
        mPlayPause=(ToggleButton) findViewById(R.id.playPause);
        mPlayPause.setOnClickListener(this);
        mNext=(ImageButton)findViewById(R.id.nextTrack);
        mNext.setOnClickListener(this);
        mPrevious=(ImageButton)findViewById(R.id.preTrack);
        mPrevious.setOnClickListener(this);
        mTitle=(TextView)findViewById(R.id.con_title);
        mArtist=(TextView)findViewById(R.id.con_Artist);
        LinearLayout song=(LinearLayout)findViewById(R.id.controls);
        song.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(getApplicationContext(),SongActivity.class);
                i.putExtra("Song",mSongList.get(mSongPos));
                i.putExtra("seekPos",musicService.getSongSeekPos());
                startActivity(i);
            }
        });
    }
    public static MusicService getService(){
        return musicService;
    }
    public List<SongModel> sortByTitle(List<SongModel> sorted ){
        Collections.sort(sorted, new Comparator<SongModel>(){
            public int compare(SongModel a, SongModel b){
                return a.getTitle().compareTo(b.getTitle());
            }
        });
        return sorted;
    }


    private ServiceConnection musicConnection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
            musicService=binder.getService();
            musicService.setSongs(mSongList);
            musicBound=true;
           
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound=false;
            Log.e("SerViceBound",name.toString());
        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_Shuffle:
                musicService.setShuffle();
                item.setChecked(musicService.isShuffle());
                  if(item.isChecked()){
                      item.setIcon(R.drawable.ic_shuffle_set);
                  }else{item.setIcon(R.drawable.ic_shuffle);}
                if(!musicService.isPng()) {
                    musicService.playNext();
                    mTitle.setText(mSongList.get(musicService.getSongPosition()).getTitle());
                    mArtist.setText(mSongList.get(musicService.getSongPosition()).getArtist());
                    mPlayPause.setChecked(true);
                }
                break;

        }
        return super.onOptionsItemSelected(item);
    }
    public void handleOnClick(int songPos){
        mSongPos =songPos;
        mTitle.setText(mSongList.get(songPos).getTitle());
        mArtist.setText(mSongList.get(songPos).getArtist());
        mPlayPause.setChecked(true);
        musicService.setSongPosition(songPos);
        musicService.playTrack();
        if(playbackPaused){
            playbackPaused=false;
        }

    }
    BroadcastReceiver handleMediaPlayerSwitch=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
         int newPos=intent.getIntExtra(getString(R.string.intentExtra),0);
            mSongPos =newPos;
            mTitle.setText(mSongList.get(newPos).getTitle());
            mArtist.setText(mSongList.get(newPos).getArtist());
            mPlayPause.setChecked(true);
            if(!musicService.isPng()) {
                musicService.setSongPosition(newPos);
                musicService.playTrack();
                if (playbackPaused) {
                    playbackPaused = false;
                }
            }
        }
    };


}


