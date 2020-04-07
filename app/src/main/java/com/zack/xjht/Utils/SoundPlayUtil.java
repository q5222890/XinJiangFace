package com.zack.xjht.Utils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.util.Log;

import java.io.IOException;

/**
 *
 */
public class SoundPlayUtil {
    private static final String TAG = "SoundPlayUtil";
    private static SoundPlayUtil ourInstance = new SoundPlayUtil();

    private SoundPool soundPool;
    private Context mContext;
    public static SoundPlayUtil getInstance() {
        return ourInstance;
    }

    private SoundPlayUtil() {
    }

    public void init(Context context){
        mContext =context;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Log.i(TAG, "sdk >= 21");
            SoundPool.Builder builder = new SoundPool.Builder();
            builder.setMaxStreams(1);
            AudioAttributes.Builder attrBuild =new AudioAttributes.Builder();
            attrBuild.setLegacyStreamType(AudioManager.STREAM_MUSIC);
            AudioAttributes audioAttributes = attrBuild.build();
            builder.setAudioAttributes(audioAttributes);
            soundPool = builder.build();
        }else {
            Log.i(TAG, "sdk < 21");
            soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        }
    }

    public int play(String fileName){
        AssetManager assetManager = mContext.getAssets();
        AssetFileDescriptor assetFileDescriptor = null;
        try {
            assetFileDescriptor = assetManager.openFd(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int load = soundPool.load(assetFileDescriptor, 1);
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
//                Log.i(TAG, "onLoadComplete: "+sampleId+":"+status);
                int play = soundPool.play(sampleId, 1.0f, 1.0f, 1, 0, 1.0f);
//                Log.i(TAG, "onLoadComplete play: "+play);
            }
        });
        return load;
    }

    public int play(int rawId){

        int load = soundPool.load(mContext, rawId, 1);
//        Log.i(TAG, "play load: "+load);
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
//                Log.i(TAG, "onLoadComplete: "+sampleId+":"+status);
                int play = soundPool.play(sampleId, 1.0f, 1.0f, 1, 0, 1.0f);
//                Log.i(TAG, "onLoadComplete play: "+play);
            }
        });
        return load;
    }

    public void pause(int streamID){
        soundPool.pause(streamID);
    }

    public void stop(int streamID){
        soundPool.stop(streamID);
    }

    public void release(){
        if(soundPool !=null){
            soundPool.release();
        }
    }
}
