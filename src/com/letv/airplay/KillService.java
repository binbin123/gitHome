package com.letv.airplay;

import java.util.List;

import com.letv.upnpControl.tools.LetvLog;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
/**
 * 
 * Kill con.youku.tv when it is not ontop.
 * @author Jamin
 *
 */
class KillService{
	private static final String TAG = "airplay.KillService";
	
	private AirplayService service = null;
	
	private mThread thread = null;
	
	private boolean stop;
	
	KillService(AirplayService airplayService){
		LetvLog.d(TAG, "airplay KillService start");
		service = airplayService;
		SetStop(true);
		thread = new mThread();
		thread.start();
		LetvLog.d(TAG, "airplay KillService stop");
	}
	
	private void SetStop(boolean flag){
		stop = flag;
		LetvLog.d(TAG, "SetStop :" + stop);
	}
	
	public void Release(){
		LetvLog.d(TAG, "airplay Release KillService start");
		SetStop(false);
		LetvLog.d(TAG, "airplay Release KillService end");
	}
	class mThread extends Thread{
		
		public void run(){
			
			super.run();
		
			while(stop){
			
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		    ActivityManager am = (ActivityManager)service.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);  
		    List<RunningTaskInfo> listActivity = am.getRunningTasks(1);  
		    boolean isAppRunning = false;  
		    String MY_PKG_NAME = "com.youku.tv";
		    String MY_PKG_NAME_SERVICE = "com.youku.tv:multiscreen";
		    List<ActivityManager.RunningServiceInfo> listService = am.getRunningServices(100);
		    String packageName = listActivity.get(0).topActivity.getPackageName();
		    
		    if(MY_PKG_NAME.equals(packageName)){
		    	continue;
		    }
		    
		
		    
		    for (ActivityManager.RunningServiceInfo info : listService) {
		    	if (info.process.equals(MY_PKG_NAME_SERVICE)) {
		    		isAppRunning = true;
		    		break;
		    	}
		    }
			
			if(true == isAppRunning){
				LetvLog.d(TAG, "Gone with the wind!");
				am.forceStopPackage(MY_PKG_NAME);
			}
			
		}
			
		}
	}
	
}