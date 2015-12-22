package com.geniusgithub.mediarender.jni;

import java.io.UnsupportedEncodingException;

public class PlatinumJniProxy {

    static {
        System.loadLibrary("git-platinum");
    }   
 
    public static native int startMediaRender(byte[] friendname ,byte[] uuid);
    public static native int stopMediaRender();  
    public static native boolean responseGenaEvent(int cmd, byte[] value ,byte[] data);  
    public static native boolean enableLogPrint(boolean flag);
 
    
    public static  int startMediaRender(String friendname ,String uuid){
    	if (friendname == null)friendname = "";
    	if (uuid == null)uuid = "";
    	int ret = -1;
    	try {
    		ret = startMediaRender(friendname.getBytes("utf-8"), uuid.getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
    	return ret;
    }
    
    public static  boolean responseGenaEvent(int cmd, String value, String data){
    	if (value == null)value = "";
    	if (data == null)data = "";
    	boolean ret = false;
    	try {
			ret = responseGenaEvent(cmd, value.getBytes("utf-8"), data.getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
    	return ret;
    }
    
    
}
