package com.letv.upnpControl.receiver;


import com.letv.upnpControl.ui.PhoneCleanMemoryDialog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class ClearMemoryBroadcastReceiver extends BroadcastReceiver{
	
	public void onReceive(Context context, Intent intent) {
		String intentAction	= intent.getAction();
		
		  if(intentAction.equals("com.letv.clearMemoryFromPhone")){
			 boolean isFromPhone = intent.getBooleanExtra("isFromPhone", false);
			 Intent activity_intent = new Intent();
			 activity_intent.setClass(context, PhoneCleanMemoryDialog.class);
			 activity_intent.putExtra("isFromPhone", isFromPhone);
			 activity_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			 context.startActivity(activity_intent);
		  }
		
	}
}
