package com.zack.xjht;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

import com.yanzhenjie.nohttp.Logger;
import com.yanzhenjie.nohttp.NoHttp;
import com.yanzhenjie.nohttp.OkHttpNetworkExecutor;
import com.yanzhenjie.nohttp.cache.DBCacheStore;
import com.yanzhenjie.nohttp.cookie.DBCookieStore;
import com.zack.xjht.Utils.CrashHandler;
import com.zack.xjht.Utils.SoundPlayUtil;

public class App extends Application implements Thread.UncaughtExceptionHandler {

    private static App mInstance;
    private static Context context;
//    private PowerManager.WakeLock wl;

    public static Context getContext() {
        return context;
    }

    @SuppressLint("InvalidWakeLockTag")
    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        context = this.getApplicationContext();
//        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
//        wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "PowerManager");
//        wl.acquire();

        NoHttp.initialize(this, new NoHttp.Config()
                .setConnectTimeout(30 * 1000)
                .setReadTimeout(30 * 1000)
                .setCacheStore(new DBCacheStore(this).setEnable(true))
                .setCookieStore(new DBCookieStore(this).setEnable(false))
                .setNetworkExecutor(new OkHttpNetworkExecutor()));
        Logger.setDebug(false);
        Logger.setTag("NoHttp");

        if (!Constants.isDebug) {
            CrashHandler.getInstance().init(getApplicationContext());
        }

        SoundPlayUtil.getInstance().init(getApplicationContext());
    }

    public static App getInstance() {
        return mInstance;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        Intent intent = new Intent(this, getTopActivity());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public Class getTopActivity() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        String className = manager.getRunningTasks(1).get(0).topActivity.getClassName();
        Log.i("App", "getTopActivity class name: " + className);
        Class cls = null;
        try {
            cls = Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return cls;
    }


}