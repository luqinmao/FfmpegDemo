package com.lansoeditor.demo;

import android.os.AsyncTask;

/**
 * user：lqm
 * desc：异步任务
 */

public class MyAsyncTask extends AsyncTask<Object, Object, Boolean> {
    private static TaskCallBackInterface mTaskCallBack;

    @Override
    protected void onPreExecute() {
        //
        if (mTaskCallBack != null){
            mTaskCallBack.onPreExecute();
        }
        super.onPreExecute();
    }
    @Override
    protected synchronized Boolean doInBackground(Object... params) {
        //
        if (mTaskCallBack != null){
            mTaskCallBack.doInBackground();
        }
        return null;
    }
    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        //
        if (mTaskCallBack != null){
            mTaskCallBack.onPostExecute();
        }
    }


    public static void setCallBack(TaskCallBackInterface callBack) {
        mTaskCallBack = callBack;
    }

    public interface TaskCallBackInterface {

        void onPreExecute();

        void doInBackground();

        void onPostExecute();
    }
}
