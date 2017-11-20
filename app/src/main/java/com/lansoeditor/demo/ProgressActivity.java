package com.lansoeditor.demo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.lansosdk.videoeditor.SDKFileUtils;

/**
 * user：lqm
 * desc：进度界面
 */

public class ProgressActivity extends Activity {

    private TextView mTextView;
    private ProgressBar mProgressBar;
    private BroadcastReceiver mReceiver;
    private VideoView videoView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        mTextView = findViewById(R.id.textView);
        mProgressBar = findViewById(R.id.progressBar);
        videoView = findViewById(R.id.videoview);

        initReceive();

    }

    private void initReceive() {
        //执行进度广播
        IntentFilter intentFilter = new IntentFilter(App.mReceiveString);
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(App.mReceiveString.equals(intent.getAction())){
                    int percent = intent.getIntExtra("percent",0);
                    mProgressBar.setProgress(percent);
                    mTextView.setText("正在处理中..."+ String.valueOf(percent)+"%");
                }
            }
        };
        registerReceiver(mReceiver, intentFilter);
    }

    public void onPlay(View view){
        String videoPath = App.mPathString+"up2.mp4";
        if (SDKFileUtils.fileExist(videoPath)){
            videoView.setVideoPath(videoPath);
            videoView.start();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }
}
