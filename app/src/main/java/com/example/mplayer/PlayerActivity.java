package com.example.mplayer;

import static com.example.mplayer.ApplicationClass.ACTION_NEXT;
import static com.example.mplayer.ApplicationClass.ACTION_PLAY;
import static com.example.mplayer.ApplicationClass.ACTION_PREV;
import static com.example.mplayer.ApplicationClass.CHANNEL_ID_2;
import static com.example.mplayer.MainActivity.arr;
import static com.example.mplayer.MainActivity.pos;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.OnSwipe;
import androidx.core.app.NotificationCompat;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import pl.droidsonroids.gif.GifImageView;

public class PlayerActivity extends AppCompatActivity implements ServiceConnection , actionPlaying {

    TextView startduration,totalDuration,songName,ArtistName;
    ImageButton palyOrPause, next,prev;
    ImageView music_image;
    SeekBar seekBar;
    RelativeLayout relativeLayout;
    swipelistener swipe;
    String nameOfSong="";
    static int position=0;
    musicService mService;
    MediaSessionCompat mediaSession;
    static ArrayList<musicLibrary> songList=new ArrayList<>();
    static Uri uri;
    static MediaPlayer mediaPlayer;
    private Handler handler=new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        startduration=(TextView) findViewById(R.id.duration_start);
        totalDuration=(TextView) findViewById(R.id.duration_total);
        seekBar=(SeekBar) findViewById(R.id.seekbaar);
        palyOrPause=(ImageButton)findViewById(R.id.playOrPause);
        next=(ImageButton)findViewById(R.id.nextSong);
        prev=(ImageButton)findViewById(R.id.perviousSong);
        music_image=(ImageView)findViewById(R.id.big_music_image);
        songName=(TextView)findViewById(R.id.NameOfSong);
        ArtistName=(TextView)findViewById(R.id.NameOfArtist);
        relativeLayout=(RelativeLayout)findViewById(R.id.player_activity);
        getIntentMethod();
        swipe=new swipelistener(relativeLayout);
        mediaSession=new MediaSessionCompat(this,"playerAudio");
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(mediaPlayer!=null && fromUser){
                    mediaPlayer.seekTo(progress*1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mediaPlayer !=null){
                    int mCurrentPositionOfSeekBar=mediaPlayer.getCurrentPosition()/1000;
                    seekBar.setProgress(mCurrentPositionOfSeekBar);
                    startduration.setText(formatedTime(mCurrentPositionOfSeekBar));
                    if(mCurrentPositionOfSeekBar==mediaPlayer.getDuration()/1000){//for auto next play song
                        position++;
                        addPosition(position);
                        uri = Uri.parse(songList.get(position).getPath());
                        nameOfSong=arr.get(position).getTitle();
                        mediaPlayer.stop();
                        mediaPlayer.release();
                        mediaPlayer=MediaPlayer.create(getApplicationContext(),uri);
                        mediaPlayer.start();
                        seekBar.setMax(mediaPlayer.getDuration()/1000);
                        totalDuration.setText(formatedTime(mediaPlayer.getDuration()/1000));
                        songName.setText(nameOfSong);
                        imgageSetter(position);
                        setArtistName(position);
                    }
                }
                handler.postDelayed(this,1000);
            }
        });
//        method for play or pause of music
        palyOrPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              playPause();
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        });
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              playPrev();
            }
        });
       totalTimeDuration();
    }

    private void totalTimeDuration() {
        int totalTime=mediaPlayer.getDuration()/1000;
        totalDuration.setText(formatedTime(totalTime));
    }
    private  byte[] getAlbumArt(String uri)
    {
        byte[] art = new byte[0];
        try {

            MediaMetadataRetriever retriever=new MediaMetadataRetriever();
            retriever.setDataSource(uri);
            art=retriever.getEmbeddedPicture();
            retriever.release();

        }catch (Exception e){

        }
        return art;
    }
    private  void imgageSetter(int position){
        byte[] imageArt=getAlbumArt(arr.get(position).getPath());
        if(imageArt!=null)
        {
            Glide.with(getApplicationContext()).asBitmap().load(imageArt).into(music_image);

        }
        else {

            Glide.with(getApplicationContext()).load(R.drawable.songimage).into(music_image);
        }
    }

    private String formatedTime(int mCurrentPositionOfSeekBar) {
        String totalOut="";
        String totalNew="";
        String seconds=String.valueOf(mCurrentPositionOfSeekBar%60);
        String minute=String.valueOf(mCurrentPositionOfSeekBar/60);
        totalOut=minute+":"+seconds;
        totalNew=minute+":"+"0"+seconds;
        if(seconds.length()==1) return totalNew;
        else  return totalOut;
    }

    private void getIntentMethod() {
     int  index=getIntent().getIntExtra("position",-1);
     songList=arr;
     if(songList!=null  ) {
         if(index!=-1) {
             uri = Uri.parse(songList.get(index).getPath());
             nameOfSong = songList.get(index).getTitle();
             imgageSetter(index);
             setArtistName(index);
         }
         else
         {
             nameOfSong = songList.get(pos).getTitle();
             imgageSetter(pos);
             setArtistName(pos);
         }
     }
     if(mediaPlayer !=null)
     {
         if(index!=-1) {
             mediaPlayer.stop();
             mediaPlayer.release();
             mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
             mediaPlayer.start();
         }

     }
     else {
         mediaPlayer=MediaPlayer.create(getApplicationContext(),uri);
         mediaPlayer.start();
     }
     if(mediaPlayer.isPlaying()){
          palyOrPause.setImageResource(android.R.drawable.ic_media_pause);
     }else {
         palyOrPause.setImageResource(android.R.drawable.ic_media_play);
     }
     seekBar.setMax(mediaPlayer.getDuration()/1000);
        songName.setText(nameOfSong);
    }
    public void setArtistName(int position){
        String artistName=arr.get(position).getArtist();
        ArtistName.setText(artistName);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        musicService.MyBinder binder= (musicService.MyBinder)service;
        mService=binder.getService();
        mService.setCallBack(PlayerActivity.this);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mService=null;
    }

    public void showNotification(int playPause)
    {
        Intent intent=new Intent(this, PlayerActivity.class);
        PendingIntent contentIntent=PendingIntent.getActivity(this,0,intent,0);
        Intent prevIntent=new Intent(this,NotificationReciver.class).setAction(ACTION_PREV);
        PendingIntent prevPendingIntent=PendingIntent.getBroadcast(this,0,prevIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        Intent playIntent=new Intent(this,NotificationReciver.class).setAction(ACTION_PLAY);
        PendingIntent playPendingIntent=PendingIntent.getBroadcast(this,0,playIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        Intent nextIntent=new Intent(this,NotificationReciver.class).setAction(ACTION_NEXT);
        PendingIntent nextPendingIntent=PendingIntent.getBroadcast(this,0,nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

         Bitmap picture= BitmapFactory.decodeResource(getResources(),R.drawable.songimage);

        Notification notification=new NotificationCompat.Builder(this,CHANNEL_ID_2)
                .setSmallIcon(R.drawable.music)
                .setLargeIcon(picture)
                .setContentTitle(arr.get(position).getTitle())
                .setContentText(arr.get(position).getArtist())
                .addAction(R.drawable.ic_baseline_skip_previous_24,"Previous",prevPendingIntent)
                .addAction(playPause,"Play",playPendingIntent)
                .addAction(R.drawable.ic_baseline_skip_next_24,"next",nextPendingIntent)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession.getSessionToken())).setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(contentIntent).setOnlyAlertOnce(true).build();
        NotificationManager notificationManager=(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0,notification);
    }
    @Override
    protected void onPause() {
        super.onPause();
        unbindService(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent=new Intent(this, musicService.class);
        bindService(intent, this,BIND_AUTO_CREATE);
    }

    @Override
    public void playNext() {
        position++;
        pos=position;
        addPosition(position);
        try {
            nameOfSong=arr.get(position).getTitle();
            uri = Uri.parse(songList.get(position).getPath());
            imgageSetter(position);
            setArtistName(position);
        }catch (Exception e){

        }
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer=MediaPlayer.create(getApplicationContext(),uri);
        mediaPlayer.start();
        seekBar.setMax(mediaPlayer.getDuration()/1000);
        totalDuration.setText(formatedTime(mediaPlayer.getDuration()/1000));
        songName.setText(nameOfSong);
        showNotification(R.drawable.ic_baseline_pause_circle_outline_24);
    }

    @Override
    public void playPrev() {
        position--;
        pos=position;
        addPosition(position);
        try {
            uri = Uri.parse(songList.get(position).getPath());
            nameOfSong=arr.get(position).getTitle();
            imgageSetter(position);
            setArtistName(position);
          //  showNotification(R.drawable.ic_baseline_pause_circle_outline_24);
        }catch (Exception e){

        }
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer=MediaPlayer.create(getApplicationContext(),uri);
        mediaPlayer.start();
        seekBar.setMax(mediaPlayer.getDuration()/1000);
        totalDuration.setText(formatedTime(mediaPlayer.getDuration()/1000));
        songName.setText(nameOfSong);
        showNotification(R.drawable.ic_baseline_pause_circle_outline_24);
    }

    @Override
    public void playPause() {
        if(mediaPlayer.isPlaying()){
            mediaPlayer.pause();
            palyOrPause.setImageResource(android.R.drawable.ic_media_play);
            showNotification(R.drawable.ic_baseline_play_circle_filled_24);
        }else if(!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            palyOrPause.setImageResource(android.R.drawable.ic_media_pause);
            showNotification(R.drawable.ic_baseline_pause_circle_outline_24);
        }
    }

    private class swipelistener implements View.OnTouchListener{
        GestureDetector gestureDetector;
        swipelistener(View view){
            int threshold=100;
            int velocityThreshold=100;
            GestureDetector.SimpleOnGestureListener simpleOnGestureListener=new
                    GestureDetector.SimpleOnGestureListener(){
                        @Override
                        public boolean onDown(MotionEvent e) {
                            return true;
                        }

                        @Override
                        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                            float xDiff=e2.getX()-e1.getX();
                            float yDiff=e2.getY()-e1.getY();
                            try {
                                if(Math.abs(xDiff)>Math.abs(yDiff)) {
                                    if(Math.abs(xDiff)>threshold && Math.abs(velocityX)>velocityThreshold)
                                    {
                                        if(xDiff>0)
                                        {
                                            //swipe right
                                           // songName.setText("right");
                                            playNext();
                                        }
                                        else {
                                           //swipe left
                                            playPrev();
                                        }
                                        return true;
                                    }
                                }
                                else{
                                    //code for up and down swipe
                                    if(Math.abs(xDiff)>threshold && Math.abs(velocityX)>velocityThreshold)
                                    {
                                        if(yDiff>0)
                                        {
                                            //bottom swipe
                                        }else {
                                            //upswipe
                                        }

                                    }
                                }
                            }catch (Exception e)
                            {

                            }
                            return super.onFling(e1, e2, velocityX, velocityY);
                        }
                    };
            gestureDetector=new GestureDetector(simpleOnGestureListener);
            view.setOnTouchListener(this);
        }
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return gestureDetector.onTouchEvent(event);
        }
    }

    public void addPosition(int position) // to add the position of the song in the database
    {
        dbManager db=new dbManager(this);
        db.addrecord(position);
    }
}