package org.cybergarage.upnp;

import android.content.Intent;
import android.graphics.drawable.Drawable;

/**
 * 
 * @author youbin
 *
 */

public class AppInfo
{
    
    private String appLabel;
    
    private Drawable appIcon;
    
    private Intent intent;
    
    private String pkgName;
    
    private String versionName;
    
    private int versionCode;
    
    public AppInfo()
    {
    }
    
    public int getVersionCode()
    {
        return versionCode;
    }
    
    public void setVersionCode(int versionCode)
    {
        this.versionCode = versionCode;
    }
    
    public String getAppversion()
    {
        return versionName;
    }
    
    public void setAppversion(String appversion)
    {
        this.versionName = appversion;
    }
    
    public String getAppLabel()
    {
        return appLabel;
    }
    
    public void setAppLabel(String appName)
    {
        this.appLabel = appName;
    }
    
    public Drawable getAppIcon()
    {
        return appIcon;
    }
    
    public void setAppIcon(Drawable appIcon)
    {
        this.appIcon = appIcon;
    }
    
    public Intent getIntent()
    {
        return intent;
    }
    
    public void setIntent(Intent intent)
    {
        this.intent = intent;
    }
    
    public String getPkgName()
    {
        return pkgName;
    }
    
    public void setPkgName(String pkgName)
    {
        this.pkgName = pkgName;
    }
}
