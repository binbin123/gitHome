package com.letv.dmr.utils;

import java.util.Stack;

import android.app.Activity;

/**
 * 
 * @author liyunchao
 *	Activity��ջ����
 */
public class ActivityManager {
	//Activityջ
	private Stack<Activity> activityStack = new Stack<Activity>();
	private static ActivityManager instance;
	public synchronized static ActivityManager getActivityManager(){
		if(instance == null){
			instance = new ActivityManager();
		}
		return instance;
	}
	//��activityѹ���ջ
	public void pushActivity(Activity activity){
		activityStack.add(activity);
	}
	//��ȡ��ǰ��activity
	public Activity currentActivity(){
		if(activityStack.size() == 0){
			return null;
		}
		Activity activity = activityStack.lastElement();
		return activity;
	}
	//����activity
	public void popActivity(Activity activity){
		if(activity != null){
			activity.finish();
			activityStack.remove(activity);
			activity = null;
		}
	}
	//��������activity,�˳�ͼƬ��ʾ
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
