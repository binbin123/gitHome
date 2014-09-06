package com.letv.upnpControl.tools;

import android.content.Intent;
import android.graphics.drawable.Drawable;

public class PhoneCleanMemoryInfo {  
    
    private String appLabel;    //应用程序标签   
    private Drawable appIcon ;  //应用程序图像   
    private Intent intent ;     //启动应用程序的Intent ，一般是Action为Main和Category为Lancher的Activity   
    private String pkgName ;    //应用程序所对应的包名   
    private int size=0;  
    public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public PhoneCleanMemoryInfo(){}  
      
    public String getAppLabel() {  
        return appLabel;  
    }  
    public void setAppLabel(String appName) {  
        this.appLabel = appName;  
    }  
    public Drawable getAppIcon() {  
        return appIcon;  
    }  
    public void setAppIcon(Drawable appIcon) {  
        this.appIcon = appIcon;  
    }  
    public Intent getIntent() {  
        return intent;  
    }  
    public void setIntent(Intent intent) {  
        this.intent = intent;  
    }  
    public String getPkgName(){  
        return pkgName ;  
    }  
    public void setPkgName(String pkgName){  
        this.pkgName=pkgName ;  
    }  
}  

