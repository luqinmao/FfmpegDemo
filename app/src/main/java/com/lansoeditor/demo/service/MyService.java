package com.lansoeditor.demo.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.lansoeditor.demo.App;
import com.lansoeditor.demo.MyAsyncTask;
import com.lansoeditor.demo.helper.DemoFunctions;
import com.lansoeditor.demo.helper.MyVideoEditor;
import com.lansosdk.videoeditor.VideoEditor;
import com.lansosdk.videoeditor.onVideoEditorProgressListener;

/**
 * user：lqm
 * desc：
 */

public class MyService extends Service {

    private MyAsyncTask asyncTask;
    private MyVideoEditor mVideoEditor;

    private String srcVideo1;
    private String srcVideo2;
    private String srcAudio;
    private String srcPic;
    private String dstVideo;
    private int audioStartS;
    private int audioDurationS;

    @Override
    public void onCreate() {//Service被创建时回调
        super.onCreate();
        asyncTask = new MyAsyncTask();
        mVideoEditor=new MyVideoEditor();

        mVideoEditor.setOnProgessListener(new onVideoEditorProgressListener() {  //发送执行进度
            @Override
            public void onProgress(VideoEditor v, int percent) {
                Intent receiveIntent = new Intent(App.mReceiveString);
                receiveIntent.putExtra("percent",percent);
                sendBroadcast(receiveIntent);//发送广播
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {//Service被启动时回调

         srcVideo1 = intent.getStringExtra("srcVideo1");
         srcVideo2 = intent.getStringExtra("srcVideo2");
         srcAudio = intent.getStringExtra("srcAudio");
         srcPic = intent.getStringExtra("srcPic");
         dstVideo = intent.getStringExtra("dstVideo");
         audioStartS = intent.getIntExtra("audioStartS",0);
         audioDurationS = intent.getIntExtra("audioDurationS",0);

        asyncTask.setCallBack(new MyAsyncTask.TaskCallBackInterface() {
            @Override
            public void onPreExecute() {

            }

            @Override
            public void doInBackground() {
                //裁剪音乐
                // 合成两个视频、
                // 替换音乐、加水印、
                DemoFunctions.MyMixVideo(MyService.this,mVideoEditor, srcVideo1,srcVideo2,srcAudio,
                        srcPic, dstVideo,audioStartS, audioDurationS);
            }

            @Override
            public void onPostExecute() {

            }
        });
        asyncTask.execute();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
