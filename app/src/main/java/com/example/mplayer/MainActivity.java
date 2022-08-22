package com.example.mplayer;


import static com.example.mplayer.ApplicationClass.ACTION_NEXT;
import static com.example.mplayer.ApplicationClass.ACTION_PLAY;
import static com.example.mplayer.ApplicationClass.ACTION_PREV;
import static com.example.mplayer.ApplicationClass.CHANNEL_ID_2;
import static com.example.mplayer.PlayerActivity.mediaPlayer;
import static com.example.mplayer.PlayerActivity.position;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.session.MediaSession;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements ServiceConnection {
    ListView listView;
    static ArrayList<musicLibrary> arr;
    static int pos=0;
    dbManager db=null;
    musicAdapter madp;
    musicService mService;
    MediaSessionCompat mediaSession;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView= (ListView) findViewById(R.id.ListView);
        mediaSession=new MediaSessionCompat(this,"playerAudio");
        runTimePermission();
        startService(new Intent(this, musicService.class));
    }

    public void runTimePermission(){
        Dexter.withContext(this).withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                    displaySing();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                     permissionToken.continuePermissionRequest();
                    }
                }).check();
    }
    public ArrayList<musicLibrary> findSong(Context context){
        arr=new ArrayList<>();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] strings={
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA
        };
        Cursor cursor=context.getContentResolver().query(uri,strings,null,null,null);
        if(cursor!=null)
        {
            while (cursor.moveToNext())
            {
                String album=cursor.getString(0);
                String artist=cursor.getString(1);
                String duration=cursor.getString(2);
                String title=cursor.getString(3);
                String path=cursor.getString(4);
                musicLibrary ms=new musicLibrary(path,title,artist,album,duration);
                arr.add(ms);
            }
            cursor.close();
        }
        return arr;
    }
    public  void  displaySing(){
       madp =new musicAdapter(MainActivity.this,findSong(this));
       listView.setAdapter(madp);
       listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
           @Override
           public void onItemClick(AdapterView<?> parent, View view, int positions, long id) {
               dbManager db=new dbManager(MainActivity.this);
               db.addrecord(position);
               pos=positions;
               position=positions;
               addPosition(positions);
             Intent intent=new Intent(getApplicationContext(),PlayerActivity.class);
             intent.putExtra("position",position);
             showNotification(R.drawable.ic_baseline_pause_circle_outline_24);
             startActivity(intent);
           }
       });
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
                .setSmallIcon(R.drawable.songimage)
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
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.my_menu,menu);
        MenuItem menuItem=menu.findItem(R.id.searchMenu);
        SearchView searchView=(SearchView)menuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                ArrayList<musicLibrary> result=new ArrayList<>();
                for(musicLibrary x: arr){
                    if(x.getTitle().contains(query) || x.getArtist().contains(query) || x.getAlbum().contains(query))
                        result.add(x);
                }
                ((musicAdapter)listView.getAdapter()).update(result);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                ArrayList<musicLibrary> result=new ArrayList<>();
                    for (musicLibrary x : arr) {
                        if (x.getTitle().toLowerCase(Locale.ROOT).contains(newText) )
                            result.add(x);
                    }
                    ((musicAdapter) listView.getAdapter()).update(result);
                return false;
            }

        });
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus)
                {
                    displaySing();
                }
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    public void gotoPlayerActivity(View view) {
        try {
            Intent intent =new Intent(getApplicationContext(),PlayerActivity.class);
            int p=search();
            Log.e("Position",p+"");
            pos=p;
            if(mediaPlayer!=null && mediaPlayer.isPlaying()) p=-1;
            intent.putExtra("position",p);
            startActivity(intent);
            showNotification(R.drawable.ic_baseline_pause_circle_outline_24);
        }catch (Exception e)
        {
            Toast.makeText(getApplicationContext(), e+"e", Toast.LENGTH_SHORT).show();

        }
    }
    public int search() // it is used for search the last played index of the song
    {
        int prevPositoin=0;
        db=new dbManager(this);
        Cursor cs=null;
        Log.e("cs",cs+"");
        try {
            cs  = db.getData();
            if(cs==null || cs.getColumnCount()==0)
            {
               return prevPositoin;
            }
                while (cs.moveToNext()) {
                    prevPositoin = Integer.parseInt(cs.getString(1));

            }
        }catch (Exception ignored){

        }

       return prevPositoin;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        musicService.MyBinder binder= (musicService.MyBinder)service;
        mService=binder.getService();

    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
      mService=null;
        Log.e("DisConnected",mService+"");
    }
    public void addPosition(int position) // to add the position of the song in the database
    {
        dbManager db=new dbManager(this);
        db.addrecord(position);
    }
}