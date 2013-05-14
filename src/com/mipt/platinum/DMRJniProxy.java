package com.mipt.platinum;

import java.io.UnsupportedEncodingException;

public class DMRJniProxy {

    static {
        System.loadLibrary("mipt-platinum");
    }
    
    public static final int DLNA_DMR_TYPE_NORMAL = 0;
    public static final int DLNA_DMR_TYPE_AI_QI_YI = 1;
    
 
    public static native int initRender(byte[] name ,byte[] uid);
    public static native int stopRender();  
    public static native boolean SendGenaEvent(int cmd, byte[] value ,byte[] data);  
    
    
    public static native void setRenderConfig(byte[] Manufacturer, byte[] ManufacturerURL,  
    											byte[] ModelName, byte[] ModelNumer,
    											byte[] ModelDescription, byte[] ModelURL);
    public static native void setDMRType(int type);
    
    public static native boolean enableLogPrint(boolean flag);
    public static native int testInflect();        
    //////////////////////////////////////////////////////////////////////////////////////////           
    public static  int initRender(String name ,String uid){
    	if (name == null){
    		name = "";
    	}
    	if (uid == null){
    		uid = "";
    	}
    	int ret = -1;
    	try {
    		ret = initRender(name.getBytes("utf-8"), uid.getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
    	return ret;
    }
    
    public static  boolean SendGenaEvent(int cmd, String value, String data){
    	if (value == null){
    		value = "";
    	}
    	if (data == null){
    		data = "";
    	}
    	boolean ret = false;
    	try {
			ret = SendGenaEvent(cmd, value.getBytes("utf-8"), data.getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
    	return ret;
    }
    
    
    public static void setRenderConfig(String Manufacturer,String ManufacturerURL, 
    											String ModelName, String ModelNumer, 
    											String ModelDescription, String ModelURL){
    	if (Manufacturer == null)Manufacturer = "";
    	if (ManufacturerURL == null)ManufacturerURL = "";
    	if (ModelName == null)ModelName = "";
    	if (ModelNumer == null)ModelNumer = "";
    	if (ModelDescription == null)ModelDescription = "";
    	if (ModelURL == null)ModelURL = "";
    	
    	try {
			setRenderConfig(Manufacturer.getBytes("utf-8"), ManufacturerURL.getBytes("utf-8"), 
					ModelName.getBytes("utf-8"), ModelNumer.getBytes("utf-8"), 
					ModelDescription.getBytes("utf-8"), ModelURL.getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
    }
    
}
