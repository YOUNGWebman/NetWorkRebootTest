package com.example.networkreboottest;

import android.content.BroadcastReceiver;


import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.File;

public class BootReceiver extends BroadcastReceiver {
    static final String action_boot ="android.intent.action.BOOT_COMPLETED";
    File f1= new File("/data/rpRbtest.txt");
    public void onReceive (Context context, Intent intent) {
        Log.e("charge start", "启动完成");
        if(!f1.exists())
        { return;}
        if (intent.getAction().equals(action_boot)){

            Log.e("charge start", "启动完成2");
            Intent mBootIntent = new Intent(context, MainActivity.class);
            // 下面这句话必须加上才能开机自动运行app的界面
            mBootIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(mBootIntent);
        }
    }
}
