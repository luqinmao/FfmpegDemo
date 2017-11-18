package com.lansoeditor.demo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.lansoeditor.demo.helper.CustomProgressDialog;
import com.lansoeditor.demo.helper.GetPathFromUri;
import com.lansoeditor.demo.helper.MyVideoEditor;
import com.lansoeditor.demo.util.PrefUtils;
import com.lansosdk.videoeditor.MediaInfo;
import com.lansosdk.videoeditor.VideoEditor;
import com.lansosdk.videoeditor.onVideoEditorProgressListener;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static com.lansoeditor.demo.App.mPathString;
import static com.lansoeditor.demo.PPPActivity.RECORD_VIDEO;

/**
 * user：lqm
 * desc：视频裁剪
 */

public class VideoCupActivity extends Activity {

    private static final String TAG = "VideoTrimActivity";
    private static final int SLICE_COUNT = 9;


    private LinearLayout mFrameListView;
    private View mHandlerLeft;
    private View mHandlerRight;

    private CustomProgressDialog mProcessingDialog;
    private ProgressDialog mProgressDialog;

    private VideoView mPreview;

    private long mSelectedBeginMs;
    private long mSelectedEndMs;
    private long mDurationMs;

    private int mVideoFrameCount;
    private int mSlicesTotalLength;

    private Handler mHandler = new Handler();
    private MediaInfo mMediaInfo;
    private boolean isRunning = false;
    private MyVideoEditor myVideoEditor;
    private String selectedFilepath;
    private TextView tVDuration;
    private boolean isDoCupPic = true;  //视频裁剪编辑
    private String mIntentVideo;
    private boolean isPPP;
    private View frameView;  //采取范围框

    private DisplayMetrics dm;
    private int lastX;
    private int lastY;
    private int cupX = 0,cupY = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mProcessingDialog = new CustomProgressDialog(this);

       mIntentVideo =  getIntent().getStringExtra("videoPath");
        isPPP = getIntent().getBooleanExtra("isPPP",false);

        if (TextUtils.isEmpty(mIntentVideo)){
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
        }else{
            selectedFilepath = mIntentVideo;
            init(selectedFilepath);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        play();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopTrackPlayProgress();
    }

    private void startTrackPlayProgress() {
        stopTrackPlayProgress();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mPreview.getCurrentPosition() >= mSelectedEndMs) {
                    mPreview.seekTo((int) mSelectedBeginMs);
                }
                mHandler.postDelayed(this, 100);
            }
        }, 100);
    }

    private void stopTrackPlayProgress() {
        mHandler.removeCallbacksAndMessages(null);
    }

    private void play() {
        if (mPreview != null) {
            mPreview.seekTo((int) mSelectedBeginMs);
            mPreview.start();
            startTrackPlayProgress();
        }
    }

    private void init(String videoPath) {
        setContentView(R.layout.activity_video_cup);
        tVDuration = (TextView) findViewById(R.id.duration);
        mPreview = (VideoView) findViewById(R.id.preview);
        frameView = findViewById(R.id.frameview);

        myVideoEditor = new MyVideoEditor();
        if(isRunning==false){
            isDoCupPic = true;
            new SubAsyncTask().execute();  //开始VideoEditor方法的处理==============>
        }
        myVideoEditor.setOnProgessListener(new onVideoEditorProgressListener() {

            @Override
            public void onProgress(VideoEditor v, int percent) {
                // TODO Auto-generated method stub
                if(mProgressDialog!=null){
                    mProgressDialog.setMessage("正在处理中..."+String.valueOf(percent)+"%");
                }
            }
        });

        initFrameView();
    }

    //裁剪框
    private void initFrameView() {
        dm = getResources().getDisplayMetrics();
        frameView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                final int screenWidth = dm.widthPixels;
                final int screenHeight = dm.heightPixels;
                        switch(action){
                            case MotionEvent.ACTION_DOWN:
                                lastX = (int) event.getRawX();// 获取触摸事件触摸位置的原始X坐标
                                lastY = (int) event.getRawY();
                                break;
                            case MotionEvent.ACTION_MOVE:
                                int dx = (int) event.getRawX() - lastX;
                                int dy = (int) event.getRawY() - lastY;
                                int l = v.getLeft() + dx;
//                                int b = v.getBottom() + dy;
                                int b = v.getBottom();
                                int r = v.getRight() + dx;
//                                int t = v.getTop() + dy;
                                int t = v.getTop();
                                // 下面判断移动是否超出屏幕
                                if (l < 0) {
                                    l = 0;
                                    r = l + v.getWidth();
                                }
                                if (t < 0) {
                                    t = 0;
                                    b = t + v.getHeight();
                                }
                                if (r > screenWidth) {
                                    r = screenWidth;
                                    l = r - v.getWidth();
                                }
                                if (b > screenHeight) {
                                    b = screenHeight;
                                    t = b - v.getHeight();
                                }
                                v.layout(l, t, r, b);
                                lastX = (int) event.getRawX();
                                lastY = (int) event.getRawY();
                                v.postInvalidate();
                                break;
                            case MotionEvent.ACTION_UP:
                                if (mMediaInfo.prepare()){
                                    cupX =  (int) (((double)frameView.getX()/screenWidth)*mMediaInfo.vWidth);
                                }
                                break;
                            default:
                                break;
                }
                return true;
            }
        });

    }

    //裁剪时长范围
    private void initVideoFrameList() {
        mFrameListView = (LinearLayout) findViewById(R.id.video_frame_list);
        mHandlerLeft = findViewById(R.id.handler_left);
        mHandlerRight = findViewById(R.id.handler_right);

        mHandlerLeft.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                float viewX = v.getX();
                float movedX = event.getX();
                float finalX = viewX + movedX;
                updateHandlerLeftPosition(finalX);

                if (action == MotionEvent.ACTION_UP) {
                    calculateRange();
                }

                return true;
            }
        });

        mHandlerRight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                float viewX = v.getX();
                float movedX = event.getX();
                float finalX = viewX + movedX;
                updateHandlerRightPosition(finalX);

                if (action == MotionEvent.ACTION_UP) {
                    calculateRange();
                }

                return true;
            }
        });

        mFrameListView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onGlobalLayout() {
                mFrameListView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                final int sliceEdge = mFrameListView.getWidth() / SLICE_COUNT;
                mSlicesTotalLength = sliceEdge * SLICE_COUNT;
                Log.i(TAG, "slice edge: " + sliceEdge);
                final float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());

                new AsyncTask<Void, Bitmap, Void>() {
                    @Override
                    protected Void doInBackground(Void... v) {
                        for (int i = 1; i <= SLICE_COUNT; ++i) {
                            String nan ="";
                            if (i<10){
                                nan = "0000"+i;
                            }else if (i>=10 && i<100){
                                nan = "000"+i;
                            }else if (i>=100 && i<1000){
                                nan = "00"+i;
                            }else if (i>=1000 && i<10000){
                                nan = "0"+i;
                            }
                            ///storage/emulated/0/lansongBox/image_00000.jpg
                            String picString = mPathString+"image_"+nan+".jpeg";
                            Bitmap bitmap= BitmapFactory.decodeFile(picString);
                            if (bitmap != null){
                                publishProgress(bitmap);
                            }
                        }
                        return null;
                    }

                    @Override
                    protected void onProgressUpdate(Bitmap... values) {
                        super.onProgressUpdate(values);
                        Bitmap frame = values[0];
                        if (frame != null) {
                            View root = LayoutInflater.from(VideoCupActivity.this).inflate(R.layout.frame_item, null);

//                            int rotation = frame.getRotation();
                            ImageView thumbnail = (ImageView) root.findViewById(R.id.thumbnail);
                            thumbnail.setImageBitmap(frame);
//                            thumbnail.setRotation(rotation);
                            FrameLayout.LayoutParams thumbnailLP = (FrameLayout.LayoutParams) thumbnail.getLayoutParams();
//                            if (rotation == 90 || rotation == 270) {
//                                thumbnailLP.leftMargin = thumbnailLP.rightMargin = (int) px;
//                            } else {
                            thumbnailLP.topMargin = thumbnailLP.bottomMargin = (int) px;
//                            }
                            thumbnail.setLayoutParams(thumbnailLP);

                            LinearLayout.LayoutParams rootLP = new LinearLayout.LayoutParams(sliceEdge, sliceEdge);
                            mFrameListView.addView(root, rootLP);
                        }
                    }
                }.execute();
            }
        });
    }

    private void updateHandlerLeftPosition(float movedPosition) {
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mHandlerLeft.getLayoutParams();
        if ((movedPosition + mHandlerLeft.getWidth()) > mHandlerRight.getX()) {
            lp.leftMargin = (int) (mHandlerRight.getX() - mHandlerLeft.getWidth());
        } else if (movedPosition < 0) {
            lp.leftMargin = 0;
        } else {
            lp.leftMargin = (int) movedPosition;
        }
        mHandlerLeft.setLayoutParams(lp);
    }

    private void updateHandlerRightPosition(float movedPosition) {
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mHandlerRight.getLayoutParams();
        lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
        lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        if (movedPosition < (mHandlerLeft.getX() + mHandlerLeft.getWidth())) {
            lp.leftMargin = (int) (mHandlerLeft.getX() + mHandlerLeft.getWidth());
        } else if ((movedPosition + (mHandlerRight.getWidth() / 2)) > (mFrameListView.getX() + mSlicesTotalLength)) {
            lp.leftMargin = (int) ((mFrameListView.getX() + mSlicesTotalLength) - (mHandlerRight.getWidth() / 2));
        } else {
            lp.leftMargin = (int) movedPosition;
        }
        mHandlerRight.setLayoutParams(lp);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            selectedFilepath = GetPathFromUri.getPath(this, data.getData());
            Log.i(TAG, "Select file: " + selectedFilepath);
            if (selectedFilepath != null && !"".equals(selectedFilepath)) {
                init(selectedFilepath);
            }
        } else {
            finish();
        }
    }

    private float clamp(float origin) {
        if (origin < 0) {
            return 0;
        }
        if (origin > 1) {
            return 1;
        }
        return origin;
    }

    private void calculateRange() {
        float beginPercent = 1.0f * ((mHandlerLeft.getX() + mHandlerLeft.getWidth() / 2) - mFrameListView.getX()) / mSlicesTotalLength;
        float endPercent = 1.0f * ((mHandlerRight.getX() + mHandlerRight.getWidth() / 2) - mFrameListView.getX()) / mSlicesTotalLength;
        beginPercent = clamp(beginPercent);
        endPercent = clamp(endPercent);

        Log.i(TAG, "begin percent: " + beginPercent + " end percent: " + endPercent);

        mSelectedBeginMs = (long) (beginPercent * mDurationMs);
        mSelectedEndMs = (long) (endPercent * mDurationMs);

        Log.i(TAG, "new range: " + mSelectedBeginMs + "-" + mSelectedEndMs);
        updateRangeText();
        play();
    }

    //下一步按钮
    public void onDone(View v) {
        isDoCupPic =false;
        new SubAsyncTask().execute();
    }

    public void onBack(View v) {
        finish();
    }

    private String formatTime(long timeMs) {
        return String.format(Locale.CHINA, "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(timeMs),
                TimeUnit.MILLISECONDS.toSeconds(timeMs) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeMs))
        );
    }

    private void updateRangeText() {
        TextView range = (TextView) findViewById(R.id.range);
        range.setText("剪裁范围: " + formatTime(mSelectedBeginMs) + " - " + formatTime(mSelectedEndMs));
    }


    ////////////////////////////////////////////////////////////

    /**
     * 第二步:创建一个AsyncTask,并在backgroud中执行VideoEditor的方法.(当然您也可以创建一个Thread,在Thread中执行)
     */
    public class SubAsyncTask extends AsyncTask<Object, Object, Boolean> {
        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            showProgressDialog();
            isRunning = true;
            super.onPreExecute();
        }

        @Override
        protected synchronized Boolean doInBackground(Object... params) {
            // TODO Auto-generated method stub
            /**
             * 真正执行的代码,因演示的方法过多, 用每个方法的ID的形式来区分, 您实际使用中, 可直接填入具体方法的代码.
             */
            if (isDoCupPic) {
                myVideoEditor.getPicFromVideo(selectedFilepath, mPathString + "image_%05d" + ".jpeg");
//                myVideoEditor.executeGetSomeFrames(selectedFilepath, mPathString + "image_%05d" + ".jpeg",(float) 9/20);
            } else {
//                myVideoEditor.executeVideoCutOut(selectedFilepath, mPathString + "cupout.mp4", (int) mSelectedBeginMs / 1000, (int) (mSelectedEndMs - mSelectedBeginMs) / 1000);
                  myVideoEditor.executeVideoExactCut(selectedFilepath, mPathString + "cupout.mp4",
                          (int) mSelectedBeginMs / 1000, (int) (mSelectedEndMs - mSelectedBeginMs) / 1000,
                           480,640,cupX,cupY,1000,false);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);

            if (isDoCupPic) {
                calcelProgressDialog();
                isRunning = false;

                //执行接下来的步骤
                mMediaInfo = new MediaInfo(selectedFilepath);
                if (mMediaInfo.prepare()) {
                    mSelectedEndMs = mDurationMs = (long) (mMediaInfo.aDuration * 1000);
                    tVDuration.setText("时长: " + formatTime(mDurationMs));
                    Log.i(TAG, "video duration: " + mDurationMs);

                    mVideoFrameCount = mMediaInfo.aTotalFrames;
                    Log.i(TAG, "video frame count: " + mVideoFrameCount);
                }

                mPreview.setVideoPath(selectedFilepath);
                mPreview.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        play();
                    }
                });

                initVideoFrameList();
            } else {
                calcelProgressDialog();
                if (isPPP){
                    Intent intent = new Intent(VideoCupActivity.this, PPPActivity.class);
                    intent.putExtra("videopath",mPathString+"cupout.mp4");
                    setResult(RECORD_VIDEO, intent);
                    finish();

                }else{
                    if (TextUtils.isEmpty(mIntentVideo)){
                        VideoEditActivity.start(VideoCupActivity.this, mPathString+"cupout.mp4");
                    }else{
                        PrefUtils.setString(VideoCupActivity.this,"video2",mPathString+"cupout.mp4");
                        Intent intent=new Intent(VideoCupActivity.this,VideoPlayTestActivity.class);
                        intent.putExtra("videopath", mPathString+"cupout.mp4");
                        startActivity(intent);
                    }
                }
            }

        }

    }
    private void showProgressDialog()
    {
        mProgressDialog = new ProgressDialog(VideoCupActivity.this);
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
