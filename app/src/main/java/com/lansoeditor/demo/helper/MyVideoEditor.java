package com.lansoeditor.demo.helper;

import com.lansosdk.videoeditor.VideoEditor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @user  lqm
 * @desc  继承VideoEditor，扩展
 */

public class MyVideoEditor extends VideoEditor {

    /**
     * 左右视频合成测试
     * 视频水平镜像，即把视频左半部分镜像显示在右半部分
     * 【此方法用到编解码】
     * @param srcPath　源视频路径
     * @param decoder　　指定解码器
     * @param dstPath　　目标视频路径
     * @return
     */
    public int  mixVideo(String srcPath, String decoder, int bitrate, String dstPath)
    {
        //ffmpeg -i 2x.mp4 -vf "crop=iw/2:ih:0:0,split[left][tmp];[tmp]hflip[right];[left][right] hstack" -acodec copy 2x_hmirror.mp4
       //ffmpeg -i input.mkv -filter_complex "[0:v]pad=w=2*iw[main];[0:v]hflip[overlay];[main][overlay]overlay=x=w"symmetry.mkv
      //ffmpeg -i d:\aa.mp4 -i d:bb.mp4 -filter_complex "[0:v]pad=w=2*iw[main];[main][1:v]overlay=x=w" d:\cc.mp4

        if(fileExist(srcPath)){

            String filter= String.format(Locale.getDefault(),"[0:v]pad=w=2*iw[main];[main][1:v]overlay=x=w");

            List<String> cmdList=new ArrayList<String>();

            cmdList.add("-vcodec");
            cmdList.add(decoder);

            cmdList.add("-i");
            cmdList.add(srcPath);
            cmdList.add("-i");
            cmdList.add(srcPath);

            cmdList.add("-filter_complex");
            cmdList.add(filter);

//            cmdList.add("-acodec");
//            cmdList.add("copy");
            cmdList.add("-vcodec");
            cmdList.add("lansoh264_enc"); //编码器
            cmdList.add("-pix_fmt"); //可用的像素格式
            cmdList.add("yuv420p");

            cmdList.add("-b:v"); //设置视频码率
            cmdList.add(checkBitRate(bitrate));

            cmdList.add("-y");
            cmdList.add(dstPath);

            String[] command=new String[cmdList.size()];
            for(int i=0;i<cmdList.size();i++){
                command[i]=(String)cmdList.get(i);
            }
            return  executeVideoEditor(command);

        }else{
            return VIDEO_EDITOR_EXECUTE_FAILED;
        }
    }

    public int  mixVideo2(String srcPath1, String srcPath2, String decoder, int bitrate, String dstPath)
    {
        //ffmpeg -i 2x.mp4 -vf "crop=iw/2:ih:0:0,split[left][tmp];[tmp]hflip[right];[left][right] hstack" -acodec copy 2x_hmirror.mp4
        //ffmpeg -i input.mkv -filter_complex "[0:v]pad=w=2*iw[main];[0:v]hflip[overlay];[main][overlay]overlay=x=w"symmetry.mkv
        //ffmpeg -i d:\aa.mp4 -i d:bb.mp4 -filter_complex "[0:v]pad=w=2*iw[main];[main][1:v]overlay=x=w" d:\cc.mp4

        if(fileExist(srcPath1)){

            String filter= String.format(Locale.getDefault(),"[0:v]pad=w=2*iw[main];[main][1:v]overlay=x=w");

            List<String> cmdList=new ArrayList<String>();

            cmdList.add("-vcodec");
            cmdList.add(decoder);

            cmdList.add("-i");
            cmdList.add(srcPath1);
            cmdList.add("-i");
            cmdList.add(srcPath2);

            cmdList.add("-filter_complex");
            cmdList.add(filter);

//            cmdList.add("-acodec");
//            cmdList.add("copy");
            cmdList.add("-vcodec");
            cmdList.add("lansoh264_enc"); //编码器
            cmdList.add("-pix_fmt"); //可用的像素格式
            cmdList.add("yuv420p");

            cmdList.add("-b:v"); //设置视频码率
            cmdList.add(checkBitRate(bitrate));

            cmdList.add("-y");
            cmdList.add(dstPath);

            String[] command=new String[cmdList.size()];
            for(int i=0;i<cmdList.size();i++){
                command[i]=(String)cmdList.get(i);
            }
            return  executeVideoEditor(command);

        }else{
            return VIDEO_EDITOR_EXECUTE_FAILED;
        }
    }

    /**
     * 从视频获取图像帧
     */
    public int  getPicFromVideo(String srcPath, String dstPath)
    {
        //ffmpeg -i d:\aa.mp4 -r 1  -vframes 9 d:\image_%05d.jpeg
        if(fileExist(srcPath)){

            List<String> cmdList=new ArrayList<String>();

            cmdList.add("-vcodec");
            cmdList.add("lansoh264_dec");

            cmdList.add("-i");
            cmdList.add(srcPath);

            cmdList.add("-r");
            cmdList.add("1");

            cmdList.add("-vframes");
            cmdList.add("9");

//            cmdList.add("-vcodec");
//            cmdList.add("lansoh264_enc"); //编码器

//            cmdList.add("-y");
            cmdList.add(dstPath);

            String[] command=new String[cmdList.size()];
            for(int i=0;i<cmdList.size();i++){
                command[i]=(String)cmdList.get(i);
            }
            return  executeVideoEditor(command);

        }else{
            return VIDEO_EDITOR_EXECUTE_FAILED;
        }
    }

}
