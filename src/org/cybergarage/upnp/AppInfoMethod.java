package org.cybergarage.upnp;

import com.letv.smartControl.R;
import com.letv.upnpControl.tools.Engine;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
/**
 * 
 * @author youbin
 *
 */
public class AppInfoMethod {  
    Context context ;  
    PackageManager pm ;

    public AppInfoMethod(Context context) {  
        this.context = context;  
        pm = context.getPackageManager();  
    }  
    /* 
     * 获取程序 图标 
     */  
    public Drawable getAppIcon(String packname){  
        
        ApplicationInfo info = null;  
      try {  
             info = pm.getApplicationInfo(packname, 0);   
            
        } catch (NameNotFoundException e) {  
            // TODO Auto-generated catch block  
            e.printStackTrace();  
          //  Drawable drawable=Engine.getInstance().getContext().getResources().getDrawable(R.drawable.ic_home_app_kankan);           
            return null;
        }  

          return info.loadIcon(pm);  
    }  
      
    /* 
     *获取程序的版本号   
     */  
    public String getAppVersion(String packname){  
        PackageInfo packinfo =null;
          try {  
               packinfo = pm.getPackageInfo(packname, 0);  
        
            } catch (NameNotFoundException e) {  
                e.printStackTrace();  
                 
            }  
          return packinfo.versionName;  
    }  
      
  
    /* 
     * 获取程序的名字  
     */  
    public String getAppName(String packname){  
        
        ApplicationInfo info =null;
          try {  
                  info = pm.getApplicationInfo(packname, 0);   
               
            } catch (NameNotFoundException e) {  
                // TODO Auto-generated catch block  
                e.printStackTrace();  
               
            }  
          return info.loadLabel(pm).toString();  
    }  
    /* 
     * 获取程序的权限 
     */  
    public String[] getAppPremission(String packname){  
        PackageInfo packinfo =null;
          try {  
               packinfo =    pm.getPackageInfo(packname, PackageManager.GET_PERMISSIONS);  
            
            
  
            } catch (NameNotFoundException e) {  
                e.printStackTrace();  
                 
            }  
          //获取到所有的权限   
          return packinfo.requestedPermissions;  
    }  
      
      
    /* 
     * 获取程序的签名  
     */  
    public String getAppSignature(String packname){  
        PackageInfo packinfo =null;
          try {  
               packinfo =    pm.getPackageInfo(packname, PackageManager.GET_SIGNATURES);  
         
  
            } catch (NameNotFoundException e) {  
                e.printStackTrace();  
                
            }  
          //获取到所有的权限   
          return packinfo.signatures[0].toCharsString();  
    }  
}
    