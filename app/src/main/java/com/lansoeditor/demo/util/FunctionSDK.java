package com.lansoeditor.demo.util;

import com.lansoeditor.demo.App;
import com.lansoeditor.demo.helper.MyVideoEditor;
import com.lansosdk.videoeditor.MediaInfo;
import com.lansosdk.videoeditor.SDKFileUtils;
import com.lansosdk.videoeditor.VideoEditor;

import static com.lansoeditor.demo.App.mPathString;

/**
 * user：lqm
 * desc：需要用到的功能命令
 */

public class FunctionSDK {

    //合成左右的视频
    public static int composeRightVideo(MyVideoEditor editor, String srcVideo1, String srcVideo2,
                                 String srcAudio, String srcPic, String dstVideo,
                                 int audioStartS, int audioDurationS) {
        MediaInfo info=new MediaInfo(srcVideo1);
        if(info.prepare()) {
            int bitrate=(int)(info.vBitRate*1.5f);
            if(bitrate>2000*1000)
                bitrate=2000*1000; //2M

            //裁剪音乐
            editor.executeAudioCutOut(srcAudio, mPathString+"audio.mp3",audioStartS,audioDurationS);
            //合成视频和增加logo
            editor.composeVideoAndLogo(srcVideo1,srcVideo2,srcPic,info.vCodecName,bitrate,dstVideo);
            SDKFileUtils.deleteFile(srcVideo1);
            SDKFileUtils.deleteFile(srcVideo2);
            //音频替换
            return AvMergeAudio(editor,dstVideo,mPathString+"audio.mp3", App.mPathString+"up2.mp4");
        }else{
            return -1;
        }
    }

    /**
     * 演示音频和视频合成
     * 如果源视频之前有音频,会首先删除音频部分
     */
    public static int AvMergeAudio(VideoEditor editor, String srcVideo,
                                   String audio, String dstPath)
    {
        int ret=-1;
        MediaInfo info=new MediaInfo(srcVideo,false);
        if(info.prepare()) {
            String video2=srcVideo;
            String video3=null;
            //如果源视频中有音频,则先删除音频
            if(info.isHaveAudio()){
                video3= SDKFileUtils.createFileInBox(info.fileSuffix);
                editor.executeDeleteAudio(video2, video3);
                video2=video3;
            }
            //合成音频
            ret=editor.executeVideoMergeAudio(video2,audio, dstPath);

            SDKFileUtils.deleteFile(srcVideo);
            SDKFileUtils.deleteFile(video2);
            SDKFileUtils.deleteFile(video3);
            SDKFileUtils.deleteFile(audio);
        }

        return ret;
    }
}
