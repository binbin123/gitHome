package com.letv.upnpControl.ui;

import java.util.Timer;
import java.util.TimerTask;
import com.letv.smartControl.R;
import com.letv.upnpControl.receiver.InstallQuietReceiver;
import com.letv.upnpControl.tools.DownloadUtil;
import com.letv.upnpControl.tools.LetvLog;
import com.letv.upnpControl.tools.LetvUtils;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

public class ShowApkInstallProgress extends Activity
{
    private static final String TAG = "ShowApkInstallProgress";
    
    DownloadUtil mDownload = null;
    
    String mFileName = null;
    
    Timer mTimer = null;
    
    TimerTask mTimerTask = null;
    
    private BroadcastReceiver mInstallCompleteReceiver = new BroadcastReceiver()
    {
        
        @Override
        public void onReceive(Context arg0, Intent arg1)
        {
            // TODO Auto-generated method stub
            mDownload.mInstalledView.setVisibility(View.VISIBLE);
            
            mDownload.mInstallView.setVisibility(View.INVISIBLE);
            if (arg1.getAction().equals(InstallQuietReceiver.APP_INSTALL_FAILED))
            {
                mDownload.mInstalledView.setVisibility(View.GONE);
                String reason = arg1.getStringExtra(InstallQuietReceiver.REASON);
                mDownload.mInstalledTextView.setText(reason);
            }
            else
            {
                mDownload.mInstalledTextView.setText(getString(R.string.installed) + " " + mFileName);
            }
            
            if (mTimer != null && mTimerTask != null)
            {
                mTimer.cancel();
                mTimer = null;
                mTimerTask.cancel();
                mTimerTask = null;
                
            }
            
            mTimer = new Timer();
            mTimerTask = new TimerTask()
            {
                public void run()
                {
                    finish();
                }
            };
            mTimer.schedule(mTimerTask, 3000);
        }
    };
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        LetvLog.d(TAG, "onCreate");
        setContentView(R.layout.install);
        Intent intent = getIntent();
        String url = intent.getStringExtra("url");
        mFileName = intent.getStringExtra("fileName");
        String packageName = intent.getStringExtra("packageName");
        mDownload = new DownloadUtil(this, mFileName);
        mDownload.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url, mFileName, packageName);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(InstallQuietReceiver.APP_INSTALL_SUCCESS);
        intentFilter.addAction(InstallQuietReceiver.APP_INSTALL_FAILED);
        registerReceiver(mInstallCompleteReceiver, intentFilter);
        
    }
    
    @Override
    public void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        
        super.onAttachedToWindow();
        View view = getWindow().getDecorView();
        WindowManager.LayoutParams lp = (WindowManager.LayoutParams)view.getLayoutParams();
        lp.gravity = Gravity.RIGHT | Gravity.TOP;
        lp.x = getResources().getDimensionPixelSize(R.dimen.playqueue_dialog_marginright);
        lp.y = getResources().getDimensionPixelSize(R.dimen.playqueue_dialog_margintop);
        lp.width = getResources().getDimensionPixelSize(R.dimen.playqueue_dialog_width);
        lp.height = getResources().getDimensionPixelSize(R.dimen.playqueue_dialog_height);
        
        getWindowManager().updateViewLayout(view, lp);
    }
    
    public class UIhandler extends Handler
    {
        
        public void handleMessage(Message msg)
        {
            
            LetvLog.d(TAG, "handleMessage msg.what begin= " + msg.what);
            switch (msg.what)
            {
            
                default:
                    break;
            }
            
        }
    }
    
    protected void onNewIntent(Intent intent)
    {
        LetvLog.d(TAG, "onNewIntent intent= " + intent);
    }
    
    public void onPause()
    {
        super.onPause();
        finish();
        LetvLog.d(TAG, "onPause");
    }
    
    public void onResume()
    {
        super.onResume();
        LetvLog.d(TAG, "onResume");
    }
    
    @Override
    protected void onStop()
    {
        super.onStop();
        LetvLog.d(TAG, "onStop ");
        
    }
    
    protected void onDestroy()
    {
        super.onDestroy();
        LetvLog.d(TAG, "onDestroy");
        unregisterReceiver(mInstallCompleteReceiver);
        if (mTimer != null)
        {
            mTimer.cancel();
            mTimer = null;
            if (mTimerTask != null)
            {
                mTimerTask.cancel();
                mTimerTask = null;
            }
        }
        
    }
    
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        LetvLog.d(TAG, "onKeyDown");
        LetvUtils.showToast(Toast.LENGTH_SHORT, getString(R.string.download_background), this);
        finish();
        
        return true;
    }
    
    public boolean onKeyUp(int keyCode, KeyEvent event)
    {
        LetvLog.d(TAG, "onKeyUp");
        return true;
    }
}
