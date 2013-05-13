package com.geniusgithub.mediarender.center;

import com.geniusgithub.mediarender.RenderApplication;

import android.content.Context;

public class MediaRenderProxy {

	private static  MediaRenderProxy mInstance;
	private Context mContext;
	
	private MediaRenderProxy(Context context) {
		mContext = context;
	}

	public static synchronized MediaRenderProxy getInstance() {
		if (mInstance == null){
			mInstance  = new MediaRenderProxy(RenderApplication.getInstance());
		}
		return mInstance;
	}

}
