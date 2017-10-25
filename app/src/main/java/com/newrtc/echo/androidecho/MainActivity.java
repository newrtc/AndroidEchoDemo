package com.newrtc.echo.androidecho;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.WindowManager;


public class MainActivity extends Activity {

    AudioAec m_audio = null;

    private static final int MY_PERMISSIONS_REQUEST = 1;
    private static final String[] MANDATORY_PERMISSIONS = {
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.MODIFY_AUDIO_SETTINGS",
            "android.hardware.camera",
            "android.hardware.camera.autofocus",
            "android.permission.ACCESS_NETWORK_STATE",
            "android.hardware.wifi",
            "android.permission.CAMERA",
            "android.permission.MODIFY_AUDIO_SETTINGS",
            "android.permission.RECORD_AUDIO",
            "android.permission.INTERNET",
            "android.permission.WAKE_LOCK"
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        AudioManager audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        audioManager.setSpeakerphoneOn(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission : MANDATORY_PERMISSIONS) {
                if (checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    Log.d("SDK",permission +" request ");
                    ActivityCompat.requestPermissions(this, new String[]{permission}, MY_PERMISSIONS_REQUEST);
                }else{
                    Log.d("SDK", permission+" success ");
                }
            }
        }


        m_audio = new AudioAec();
        m_audio.StartRecorderAndPlayer();
    }
}