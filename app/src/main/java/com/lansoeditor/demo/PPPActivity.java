package com.lansoeditor.demo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.lansoeditor.demo.helper.DemoFunctions;
import com.lansoeditor.demo.helper.GetPathFromUri;
import com.lansoeditor.demo.helper.MyVideoEditor;
import com.lansosdk.videoeditor.VideoEditor;
import com.lansosdk.videoeditor.onVideoEditorProgressListener;

import java.io.IOException;

/**
 * user：lqm
 * desc：仿picPlayPost界面
 */

public class PPPActivity extends Activity {

    public static final int RECORD_VIDEO = 100;

    private VideoView mVideoView1,mVideoView2,mVideoView3;
    private String videoPath1,videoPath2;
    private boolean mIsSelectFirstVideoView;
    private ImageView ivPreview1,ivPreview2;
    private boolean isSelectAudioFile = false;
    private String audioFile; //背景音乐
    private MediaPlayer audioPlayer;

    private int playOverNum;
    private ProgressDialog mProgressDialog;
    private MyVideoEditor mVideoEditor;
    private SeekBar seekBar;
    private TextView tvAudioLength;
    private int mAudioProgress,mAudioCupLength,mAudioDuration;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ppp);

        init();
        initView();
        initListener();

    }

    private void init() {
        getIntent().getStringExtra("videopath");
        mVideoEditor=new MyVideoEditor();
    }

    private void initView() {
        mVideoView1 =  (VideoView) findViewById(R.id.videoview1);
        mVideoView2 =  (VideoView)  findViewById(R.id.videoview2);
        ivPreview1 = (ImageView) findViewById(R.id.iv_preview1);
        ivPreview2 = (ImageView) findViewById(R.id.iv_preview2);
        mVideoView3 = (VideoView) findViewById(R.id.video_view3);

        seekBar = (SeekBar) findViewById(R.id.seek_bar);
        tvAudioLength = findViewById(R.id.tv_audio_length);

    }

    private void initListener() {
        mVideoView1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                showSelectDialog(true);
                return false;
            }
        });

        mVideoView2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                showSelectDialog(false);
                return false;
            }
        });

        mVideoView1.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                if (!TextUtils.isEmpty(audioFile)){
                    mediaPlayer.setVolume(0f,0f);
                }
            }
        });

        mVideoView2.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                if (!TextUtils.isEmpty(audioFile)){
                    mediaPlayer.setVolume(0f,0f);
                }
            }
        });

        mVideoView1.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                ivPreview1.setVisibility(View.VISIBLE);
                playOverNum +=1;
                if (playOverNum == 2){
                    if (audioPlayer != null){
                        audioPlayer.stop();
                    }

                }
            }
        });

        mVideoView2.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                ivPreview2.setVisibility(View.VISIBLE);
                playOverNum +=1;
                if (playOverNum == 2){
                    if (audioPlayer != null){
                        audioPlayer.stop();
                    }
                }
            }
        });

        mVideoEditor.setOnProgessListener(new onVideoEditorProgressListener() {
            @Override
            public void onProgress(VideoEditor v, int percent) {
                if(mProgressDialog!=null){
                    mProgressDialog.setMessage("正在处理中..."+ String.valueOf(percent)+"%");
                }
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mAudioProgress = i;

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                onPreviewVideo(seekBar);
            }
        });
    }

    //预览
    public void onPreviewVideo(View view){
        if (TextUtils.isEmpty(videoPath1)){
            Toast.makeText(PPPActivity.this,"请选择参照视频",Toast.LENGTH_SHORT).show();
            return;
        }else if (TextUtils.isEmpty(videoPath2)){
            Toast.makeText(PPPActivity.this,"请选择您的视频",Toast.LENGTH_SHORT).show();
            return;
        }
        ivPreview1.setVisibility(View.GONE);
        ivPreview2.setVisibility(View.GONE);
        mVideoView1.setVideoPath(videoPath1);
        mVideoView2.setVideoPath(videoPath2);
        mVideoView1.start();
        mVideoView2.start();

        playOverNum = 0;
        if (!TextUtils.isEmpty(audioFile)){
            try {
                if (audioPlayer == null){
                    audioPlayer =new MediaPlayer();
                    audioPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                }
                audioPlayer.reset();
                audioPlayer.setDataSource(audioFile);
                audioPlayer.prepare();
                audioPlayer.start();
                mAudioDuration = audioPlayer.getDuration();
                mAudioCupLength = (int) (mAudioProgress*mAudioDuration*0.01);
                audioPlayer.seekTo(mAudioCupLength);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    //替换背景音乐
    public void onReplaceAudio(View view){
        selectAudioFile();
    }

    //进行合成
    public void onMixVideo(View view){
        MyAsyncTask asyncTask = new MyAsyncTask();
        asyncTask.execute();
        asyncTask.setCallBack(new MyAsyncTask.TaskCallBackInterface() {
            @Override
            public void onPreExecute() {
                showProgressDialog();
            }

            @Override
            public void doInBackground() {
                //裁剪音乐
                // 合成两个视频、
                // 替换音乐、加水印、
                DemoFunctions.MyMixVideo(PPPActivity.this,mVideoEditor, videoPath1,videoPath2,audioFile,
                        App.mPathString+"image.png", App.mPathString+"up1.mp4",
                        (int) (mAudioCupLength*0.001), (int) ((mAudioDuration-mAudioCupLength)*0.001));

            }

            @Override
            public void onPostExecute() {
                calcelProgressDialog();
                if (mVideoEditor.fileExist(App.mPathString+"outtt.mp4")){
                    mVideoView3.setVideoPath(App.mPathString+"outtt.mp4");
                    mVideoView3.start();
                }
            }
        });

    }

    //选择弹框
    private void showSelectDialog(boolean isFirstVideoView){
        mIsSelectFirstVideoView = isFirstVideoView;
        View dialogView = View.inflate(this,R.layout.dialog_video_select,null);
        final CustomDialog dialog =  new CustomDialog(this,dialogView,R.style.dialog);
        dialog.show();
        TextView tvNative = (TextView) dialogView.findViewById(R.id.tv_native);
        TextView tvRecod = (TextView) dialogView.findViewById(R.id.tv_recod);
        tvNative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectNativeVideo();
                dialog.dismiss();
            }
        });
        tvRecod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PPPActivity.this,VideoRecordActivity.class);
                intent.putExtra("isPPP",true);
                startActivityForResult(intent,RECORD_VIDEO);
                dialog.dismiss();
            }
        });
    }

    //选择本地视频
    private void selectNativeVideo(){
        isSelectAudioFile = false;
        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT < 19) {
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("video/*");
        } else {
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("video/*");
        }
        startActivityForResult(Intent.createChooser(intent, "选择要导入的视频"), 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            String selectedFilepath = GetPathFromUri.getPath(this, data.getData());
            if (selectedFilepath != null && !"".equals(selectedFilepath)) {
                if (isSelectAudioFile){
                    audioFile = selectedFilepath;

                }else{
                    Intent intent = new Intent(PPPActivity.this,VideoCupActivity.class);
                    intent.putExtra("isPPP",true);
                    intent.putExtra("videoPath",selectedFilepath);
                    startActivityForResult(intent,RECORD_VIDEO);
                }
            }
        }else if (requestCode == RECORD_VIDEO){
            if (data != null) {
                if (mIsSelectFirstVideoView){
                    videoPath1 = data.getStringExtra("videopath");
                    setVideoViewThumbnail(videoPath1);
                }else{
                    videoPath2 = data.getStringExtra("videopath");
                    setVideoViewThumbnail(videoPath2);
                }
            }
        }
        else {
            finish();
        }
    }

    //设置预览图
    private void setVideoViewThumbnail(String videoPath){
        if (mIsSelectFirstVideoView){
            ivPreview1.setVisibility(View.VISIBLE);
            ivPreview1.setImageBitmap(createVideoThumbnail(videoPath));
        }else{
            ivPreview2.setVisibility(View.VISIBLE);
            ivPreview2.setImageBitmap(createVideoThumbnail(videoPath));
        }

    }

    //从视频获取帧图片
    private Bitmap createVideoThumbnail(String videoPath) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(videoPath);
        Bitmap bitmap = mmr.getFrameAtTime();
        mmr.release();
        return bitmap;
    }

    //选择背景音乐
    private void selectAudioFile(){
        isSelectAudioFile = true;
        if (audioPlayer != null && audioPlayer.isPlaying()){
            audioPlayer.stop();
        }
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
    protected void onResume() {
        super.onResume();

        if (!TextUtils.isEmpty(videoPath1)){
            ivPreview1.setVisibility(View.VISIBLE);
            ivPreview1.setImageBitmap(createVideoThumbnail(videoPath1));
        }
        if (!TextUtils.isEmpty(videoPath2)){
            ivPreview2.setVisibility(View.VISIBLE);
            ivPreview2.setImageBitmap(createVideoThumbnail(videoPath2));
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(audioPlayer!=null){
            audioPlayer.stop();
            audioPlayer.release();
            audioPlayer=null;
        }
    }

    private void showProgressDialog() {
        mProgressDialog = new ProgressDialog(PPPActivity.this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage("正在处理中...");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }
    private void calcelProgressDialog() {
        if( mProgressDialog!=null){
            mProgressDialog.cancel();
            mProgressDialog=null;
        }
    }
}
