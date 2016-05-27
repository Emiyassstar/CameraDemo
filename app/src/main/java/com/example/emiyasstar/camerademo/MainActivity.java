package com.example.emiyasstar.camerademo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;

import cn.redcdn.log.CustomLog;
import cn.redcdn.log.LogcatFileManager;

public class MainActivity extends AppCompatActivity {

    private static String TAG="MainActivity";
    SurfaceView surfaceView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogcatFileManager.getInstance().setLogDir("com.example.camerademo");
        LogcatFileManager.getInstance().start("camerademo");
        CustomLog.d(TAG, "onCreate: ");
        setContentView(R.layout.activity_main);
        surfaceView=(SurfaceView) findViewById(R.id.local_video_surfaceview);
;
    }

    @Override
    protected void onResume() {
        super.onResume();
        CustomLog.d(TAG, "onResume: ");
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
