package com.lansosdk.videoeditor;

import android.content.Context;
import android.content.res.AssetManager;


public class LanSoEditor {

		  public static void initSo(Context context, String argv)
		  {
		    	    nativeInit(context,context.getAssets(),argv);
		  }
	    public static void unInitSo()
	    {
	    		nativeUninit();
	    }
	    public static native void nativeInit(Context ctx, AssetManager ass, String filename);
	    public static native void nativeUninit();
	    
}
