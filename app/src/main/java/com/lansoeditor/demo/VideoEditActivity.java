package com.lansoeditor.demo;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.lansoeditor.demo.helper.DemoFunctions;
import com.lansoeditor.demo.helper.GetPathFromUri;
import com.lansosdk.videoeditor.VideoEditor;

import java.io.IOException;

import static com.lansoeditor.demo.App.mPathString;

/**
 * user：lqm
 * desc：视频编辑（加水印，换音乐）
 */

public class VideoEditActivity extends Activity {

    private static final String TAG = "VideoEditActivity";
    private static final String MP4_PATH = "MP4_PATH";

    private SurfaceView mPreviewView;

    private boolean mIsMixAudio = false;
    private boolean mIsUseWatermark = true;
    private ImageView mIvLogo; //logo水印
    private String mAudioPath; //背景音乐
    private ImageButton mIvAutdio;
    private MediaPlayer audioPlayer,audioPlayer2;
    private SurfaceHolder holder;
    private VideoEditor mVideoEditor;
    private String mVideoPath;

    public static void start(Activity activity, String mp4Path) {
        Intent intent = new Intent(activity, VideoEditActivity.class);
        intent.putExtra(MP4_PATH, mp4Path);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_video_edit);

        initView();
    }

    private void initView() {
        mVideoPath = getIntent().getStringExtra(MP4_PATH);
        mPreviewView = (SurfaceView) findViewById(R.id.surfaceview);
        mIvLogo = (ImageView)findViewById(R.id.iv_logo);
        mIvAutdio = (ImageButton)findViewById(R.id.mix_audio_setting_button);

        mVideoEditor = new VideoEditor();

        audioPlayer=new MediaPlayer();
        audioPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            audioPlayer.setDataSource(mVideoPath);
            holder=mPreviewView.getHolder();
            holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            holder.addCallback(new MyCallBack());
            audioPlayer.prepare();
            audioPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    audioPlayer.start();
                    audioPlayer.setLooping(true);
                }
            });
            audioPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    audioPlayer.start();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //调整混音的音量
    public void onClickAudioMixSetting(View v) {
        if (mIsMixAudio) {

            if (audioPlayer != null){
                audioPlayer.setVolume(1f,1f);
            }
            if (audioPlayer2 != null){
                audioPlayer2.setVolume(0f,0f);
            }
            mIsMixAudio = !mIsMixAudio;
            mIvAutdio.setImageResource(R.mipmap.btn_set_volume);
        } else {
            if (audioPlayer != null){
                audioPlayer.start();
                audioPlayer.setVolume(0f,0f);
            }
            if (audioPlayer2 != null){
                audioPlayer2.start();
                audioPlayer2.setVolume(1f,1f);
            }
            mIsMixAudio = !mIsMixAudio;
            mIvAutdio.setImageResource(R.mipmap.btn_reset);
        }

    }

    public void onClickBack(View v) {
        finish();
    }

    //添加水印
    public void onClickToggleWatermark(View v) {
        mIsUseWatermark = !mIsUseWatermark;
        mIvLogo.setVisibility(mIsUseWatermark ? View.VISIBLE : View.GONE );

    }

    //选择背景音乐
    public void onClickMix(View v) {
        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT < 19) {
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("audio/*");
        } else {
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("audio/*");
        }
        startActivityForResult(Intent.createChooser(intent, "请选择混音文件："), 0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            String selectedFilepath = GetPathFromUri.getPath(this, data.getData());
            Log.i(TAG, "Select file: " + selectedFilepath);
            if (selectedFilepath != null && !"".equals(selectedFilepath)) {
                mAudioPath = selectedFilepath;
                mIsMixAudio = true;
                mIvAutdio.setImageResource(R.mipmap.btn_reset);
                playDstAudio(mAudioPath);
            }
        }
    }


    @Override
    protected void onDestroy() {
        if(audioPlayer!=null){
            audioPlayer.stop();
            audioPlayer.release();
            audioPlayer=null;
        }
        if(audioPlayer2!=null){
            audioPlayer2.stop();
            audioPlayer2.release();
            audioPlayer2=null;
        }

        super.onDestroy();

    }

    //下一步按钮
    public void onSaveEdit(View v) {
        new SubAsyncTask().execute();

    }

    private void playDstAudio(String dstAudio){  //播音频预览
        try {
            if (audioPlayer2 == null){
                audioPlayer2 = new MediaPlayer();
                audioPlayer2.setAudioStreamType(AudioManager.STREAM_MUSIC);
            }
            audioPlayer2.reset();
            audioPlayer2.setDataSource(dstAudio);
            audioPlayer2.prepare();
            audioPlayer2.start();

            if (audioPlayer != null){
                audioPlayer.setVolume(0,0);
            }

        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class MyCallBack implements SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            if (audioPlayer != null){
                audioPlayer.setDisplay(holder);
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
//            if (null != audioPlayer) {
//                audioPlayer.release();
//                audioPlayer = null;
//            }
        }
    }

    public class SubAsyncTask extends AsyncTask<Object, Object, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(VideoEditActivity.this,"开始处理...",Toast.LENGTH_SHORT).show();
        }
        @Override
        protected synchronized Boolean doInBackground(Object... params) {
            if (mIsMixAudio){
                DemoFunctions.demoAVMergeAndLogo(VideoEditActivity.this,mVideoEditor, mVideoPath,mAudioPath,mPathString+"image.png", mPathString+"outt.mp4");
            }else{
                DemoFunctions.demoAddPicture(VideoEditActivity.this,mVideoEditor,mVideoPath,mPathString+"image.png",mPathString+"outt.mp4");
            }
            return null;
        }
        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            Intent intent = new Intent(VideoEditActivity.this,VideoPlayActivity.class);
            if (mIsMixAudio){
                intent.putExtra("videopath",mPathString+"outtt.mp4");
            }else{
                intent.putExtra("videopath",mPathString+"outt.mp4");
            }
            startActivity(intent);

            finish();
        }
    }

}
