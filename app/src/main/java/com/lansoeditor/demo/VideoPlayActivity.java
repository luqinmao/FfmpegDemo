package com.lansoeditor.demo;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.VideoView;

/**
 * user：lqm
 * desc：播放视频预览
 */

public class VideoPlayActivity extends Activity {


    private String videoPath;
    private VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_play);

        videoView = (VideoView)findViewById(R.id.video_view);

        initView();

    }

    private void initView() {
        videoPath =  getIntent().getStringExtra("videopath");

        if (!TextUtils.isEmpty(videoPath)){
            videoView.setVideoPath(videoPath);
            videoView.start();
        }

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                videoView.start();
            }
        });

    }

}
