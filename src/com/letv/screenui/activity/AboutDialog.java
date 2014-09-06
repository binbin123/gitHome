package com.letv.screenui.activity;

import org.cybergarage.util.Debug;

import com.letv.dmr.upnp.DMRService;
import com.letv.smartControl.R;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class AboutDialog extends Activity

{
	public static final String TAG = "AboutDialog";
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Debug.d(TAG,"on create");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_dialog);
        Button btn=(Button)findViewById(R.id.button_off);
        btn.setOnClickListener(new OnClickListener()
        {
            
            @Override
            public void onClick(View arg0)
            {
                // TODO Auto-generated method stub
                finish();
            }
        });
        
    }
    public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		
		Debug.d(TAG,"on ConfigurationChanged");

		finish();

	}
    
    protected void onDestroy()
    {
    	Debug.d(TAG,"on Destroy");
        super.onDestroy();
    }
}
