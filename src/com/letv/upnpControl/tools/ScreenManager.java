package com.letv.upnpControl.tools;


import java.util.Stack;
import android.app.Activity;

/**
 * @title: 
 * @description: Activity鍫嗘爤绠＄悊
 * @company: 涔愯缃戜俊鎭妧鏈紙鍖椾含锛夎偂浠芥湁闄愬叕鍙�
 * @author 浜庡涵榫�
 * @version 1.0
 * @created 2012-2-26 涓婂崍1:36:15
 * @changeRecord
 */
public class ScreenManager {
	private Stack<Activity> activityStack = new Stack<Activity>();//Activity鏍�	
	private static ScreenManager instance;
	private  ScreenManager(){}
	
	public synchronized static ScreenManager getScreenManager(){
		if(instance == null){
			instance = new ScreenManager();
		}
		return instance;
	}
	
	public void popActivity(){
		Activity activity = activityStack.lastElement();
		if(activity != null){
			activity.finish();
			activity = null;
		}
	}
	
	public void popActivity(Activity activity){
		if(activity != null){
			activity.finish();
			activityStack.remove(activity);
			activity = null;
		}
	}
	
	public Activity currentActivity(){
		if(activityStack.size() == 0){
			return null;
		}
		Activity activity = activityStack.lastElement();
		return activity;
	}
	
	public void pushActivity(Activity activity){
//		if(activityStack == null){
//			activityStack = new Stack<Activity>();
//		}
		activityStack.add(activity);
//		if(currentActivity() != null){
//			LetvLog.e("TAG", "currentActivity() = " + currentActivity().getClass());
//			LetvLog.e("TAG", "activity.getClass() = " + activity.getClass());
//			if (!currentActivity().getClass().equals(activity.getClass())) {
//				activityStack.add(activity);
//			}
//		}
//		else{
//			activityStack.add(activity);
//		}
	}
	
	public void popAllActivityExceptOne(Class<? extends Activity> cls){
		Activity act=null;
		while(true){
			Activity activity = currentActivity();
			if(activity == null){
				break;
			}
			if(activity.getClass().equals(cls)){
				act=activity;
				activityStack.removeElement(activity);
				continue;
			}else {
				popActivity(activity);
			}
			
		}
		if (act!=null) {
			pushActivity(act);
		}
	}
	
	
	public void exitApp(){
		while(true){
			Activity activity = currentActivity();
			if(activity == null){
				break;
			}
			popActivity(activity);
		}
	}
}