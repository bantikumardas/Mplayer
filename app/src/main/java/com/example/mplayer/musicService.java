package com.example.mplayer;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

import java.security.Provider;
import java.util.List;
import java.util.Map;

public class musicService extends Service {
    public static final String ACTION_NEXT = "NEXT";
    public static final String ACTION_PREV = "PREV";
    public static final String ACTION_PLAY = "PLAY";
    actionPlaying aPlaying;
    private IBinder mBinder=new MyBinder();



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String actionName=intent.getStringExtra("myActionName");
        if(actionName!=null)
        {
            switch (actionName)
            {
                case ACTION_NEXT:
                    if(aPlaying!=null)
                        aPlaying.playNext();
                    break;
                case ACTION_PREV:
                    if(aPlaying!=null)
                       aPlaying.playPrev();
                    break;
                case ACTION_PLAY:
                    if(aPlaying!=null)
                       aPlaying.playPause();
                    break;
            }
        }
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    public class MyBinder extends Binder{
        musicService getService()
        {
            return musicService.this;
        }
    }
    public void setCallBack(actionPlaying aPlaying)
    {
        this.aPlaying=aPlaying;
    }

}
