package com.example.mplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationReciver extends BroadcastReceiver {
    public static final String ACTION_NEXT = "NEXT";
    public static final String ACTION_PREV = "PREV";
    public static final String ACTION_PLAY = "PLAY";

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intent1=new Intent(context,musicService.class);
         if(intent.getAction()!=null)
         {
             switch (intent.getAction())
             {
                 case ACTION_NEXT:
                     intent1.putExtra("myActionName",intent.getAction());
                     context.startService(intent1);
                     break;
                 case ACTION_PREV:
                     intent1.putExtra("myActionName",intent.getAction());
                     context.startService(intent1);
                     break;
                 case ACTION_PLAY:
                     intent1.putExtra("myActionName",intent.getAction());
                     context.startService(intent1);
                     break;
             }
         }
    }
}
