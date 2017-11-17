package com.lansoeditor.demo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.VideoView;

import com.lansoeditor.demo.helper.DemoFunctions;
import com.lansoeditor.demo.helper.GetPathFromUri;
import com.lansoeditor.demo.helper.MyVideoEditor;
import com.lansoeditor.demo.util.PrefUtils;
import com.lansosdk.videoeditor.VideoEditor;
import com.lansosdk.videoeditor.onVideoEditorProgressListener;

/**
 * user：lqm
 * desc：合成左右视频界面测试
 */

public class VideoPlayTestActivity extends Activity implements View.OnClickListener{

    private VideoView videoView1,videoView2,videoView3;
    private Button button1,button2,button3,button4,button5;

    private String path,path1,path2;
    private boolean isFirst = true;
    private MyVideoEditor mEditor;
    private ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_play_test);


        videoView1 = (VideoView) findViewById(R.id.videoview1);
        videoView2 = (VideoView) findViewById(R.id.videoview2);
        videoView3 = (VideoView) findViewById(R.id.videoview3);
        button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);
        button4 = (Button) findViewById(R.id.button4);
        button5 = (Button) findViewById(R.id.button5);

        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
        button3.setOnClickListener(this);
        button4.setOnClickListener(this);
        button5.setOnClickListener(this);

        initView();
    }

    private void initView() {
        path =  getIntent().getStringExtra("videopath");
        path1 = PrefUtils.getString(this,"video1","");
        path2 = PrefUtils.getString(this,"video2","");
        if (!TextUtils.isEmpty(path1)){
            videoView1.setVideoPath(path1);
            videoView1.start();
        }
        if (!TextUtils.isEmpty(path2)){
            videoView2.setVideoPath(path2);
            videoView2.start();
        }
        videoView1.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                videoView1.start();
            }
        });

        videoView2.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                videoView2.start();
            }
        });

        videoView3.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                videoView3.start();
            }
        });

        mEditor=new MyVideoEditor();
        mEditor.setOnProgessListener(new onVideoEditorProgressListener() {

            @Override
            public void onProgress(VideoEditor v, int percent) {
                if(mProgressDialog!=null){
                    mProgressDialog.setMessage("正在处理中..."+ String.valueOf(percent)+"%");
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.button1:
                Intent intent1 =  new Intent(VideoPlayTestActivity.this, VideoRecordActivity.class);
                intent1.putExtra("isFirst",true);
                intent1.putExtra("isMixVideo",true);
                startActivity(intent1);
                break;

            case R.id.button2:
                Intent intent2 =  new Intent(VideoPlayTestActivity.this, VideoRecordActivity.class);
                intent2.putExtra("isFirst",false);
                intent2.putExtra("isMixVideo",true);
                startActivity(intent2);
                break;

            case R.id.button3:
                new SubAsyncTask().execute();
                break;

            case R.id.button4:
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
                break;

            case R.id.button5:
                Intent intent3 = new Intent(VideoPlayTestActivity.this,VideoCupActivity.class);
                intent3.putExtra("videoPath",path2);
                startActivity(intent3);

                break;
        }

    }

    public class SubAsyncTask extends AsyncTask<Object, Object, Boolean> {
        @Override
        protected void onPreExecute() {
            showProgressDialog();
            super.onPreExecute();
        }
        @Override
        protected synchronized Boolean doInBackground(Object... params) {
            DemoFunctions.mixVideo2(mEditor, path1,path2, App.mPathString+"output.mp4");
            return null;
        }
        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            calcelProgressDialog();
            videoView3.setVideoPath(App.mPathString+"output.mp4");
            videoView3.start();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
           String selectedFilepath = GetPathFromUri.getPath(this, data.getData());
            if (selectedFilepath != null && !"".equals(selectedFilepath)) {
                path1 = selectedFilepath;
                videoView1.setVideoPath(path1);
                videoView1.start();
                videoView2.setVideoPath(path2);
                videoView2.start();
            }
        } else {
            finish();
        }
    }


    private void showProgressDialog()
    {
        mProgressDialog = new ProgressDialog(VideoPlayTestActivity.this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage("正在处理中...");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }
    private void calcelProgressDialog()
    {
        if( mProgressDialog!=null){
            mProgressDialog.cancel();
            mProgressDialog=null;
        }
    }
}
